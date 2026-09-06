package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tce.smart.platform.core.dto.authoperation.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * 一个主体的有界本地工作流。PB 提供服务器冻结投影，SCH 在事务提交后访问设备。
 * 每个方法必须经 Spring 代理；领取单独提交，不得在外层事务中先领目标再等待主体锁。
 */
@Service
public class AuthOperationWorkflowService {
    private final AuthOperationService operations;
    private final AuthOperationVersionService versions;
    private final SmtAuthOperationBatchMapper batches;
    private final SmtAuthOperationTargetMapper targets;
    private final SmtAuthOperationAttemptMapper attempts;
    private final AuthOperationWorkflowMapper workflow;

    public AuthOperationWorkflowService(AuthOperationService operations, AuthOperationVersionService versions,
            SmtAuthOperationBatchMapper batches, SmtAuthOperationTargetMapper targets,
            SmtAuthOperationAttemptMapper attempts, AuthOperationWorkflowMapper workflow) {
        this.operations=operations;this.versions=versions;this.batches=batches;
        this.targets=targets;this.attempts=attempts;this.workflow=workflow;
    }

    /** 只冻结小型选择依据与精确数量，不在受理事务加载目标。 */
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public AuthOperationBatchResult accept(Selection s) { return acceptValidated(s); }

    /** PB 冻结选择与本批次必须在同一个本库短事务提交或回滚。 */
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public AuthOperationBatchResult acceptWithinTransaction(Selection s) { return acceptValidated(s); }

    private AuthOperationBatchResult acceptValidated(Selection s) {
        Objects.requireNonNull(s,"选择不能为空");
        require(s.getSourceType()!=null && s.getSourceType().matches("[1-7]"),"批次原因必须为1至7，不能使用业务sourceKind");
        require(Arrays.asList("ADD","DELETE").contains(s.getAction()),"业务动作不支持");
        require(s.getSnapshot()!=null && !s.getSnapshot().trim().isEmpty() && s.getSnapshot().length()<=1000000,"必须提供有界服务器冻结投影");
        require(s.getSourceCount()!=null && s.getSourceCount()>0,"必须冻结来源数量");
        return acceptInternal(s);
    }
    private AuthOperationBatchResult acceptInternal(Selection s) {
        String encoded="WF1:"+s.getSourceCount()+":"+s.getSnapshot();
        String fingerprint=hash(part(s.getAction())+part(s.getSourceType())+part(s.getSourceId())+part(encoded)+part(String.valueOf(s.getExpectedCount())));
        return operations.submit(AuthOperationSubmitCommand.builder().parkId(s.getParkId()).idempotencyKey(s.getIdempotencyKey())
            .action(s.getAction()).sourceType(s.getSourceType()).sourceId(s.getSourceId()).selectionSnapshot(encoded)
            .payloadFingerprint(fingerprint).expectedCount(s.getExpectedCount()).build());
    }

    /** 第一阶段只登记来源与贡献；未齐全之前绝不生成中间物理版本目标。 */
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public Expanded stage(Shard shard) {
        Objects.requireNonNull(shard,"分片不能为空");SourceIntent intent=Objects.requireNonNull(shard.getSource(),"来源不能为空");
        require(shard.getResources().size()>0 && shard.getResources().size()<=200,"来源分片资源必须为1至200个");
        SmtAuthOperationBatch batch=required(batches.selectById(shard.getBatchId()),"批次不存在");
        require(Objects.equals(intent.getBatchId(),batch.getId()) && Objects.equals(intent.getParkId(),batch.getParkId()),"来源批次或园区不匹配");
        // 一次覆盖可同时新增与撤销来源；业务批次动作不替代各来源自己的冻结意图。
        require(Arrays.asList("ADD","DELETE").contains(intent.getAction()),"来源动作必须为ADD或DELETE");
        if("STAFF_AUTH".equals(intent.getSourceKind()))require(Objects.equals(intent.getStableKey(),
            AuthWorkflow.staffStableKey(intent.getSubjectId(),shard.getStaffAuthId())),"员工稳定来源必须为staffId与authId，不得使用rowId");
        SourceVersion source=versions.reserveSourceIntent(intent);
        boolean fresh=journal(batch.getId(),shard.getPreviousCursor(),shard.getNextCursor(),"STAGE",
            shardFingerprint(shard,source),source.getSourceId(),source.getGeneration());
        SmtAuthDeleteRequest prior=workflow.request(batch.getId(),source.getSourceId(),source.getGeneration());
        Long requestId=prior==null?IdWorker.getId():prior.getId();
        AuthOperationRequestCommand request=request(batch,source,requestId,intent.getSubjectType(),intent.getPayloadSnapshot());
        if(!fresh) {
            require(prior!=null,"已提交分片缺少持久请求");List<Binding> replay=new ArrayList<>();
            for(ResourceInput input:shard.getResources()) {
                SmtAuthSourceResource c=required(workflow.contributionByKey(source.getSourceId(),source.getGeneration(),input.getResource()),"已提交分片缺少资源贡献");
                replay.add(binding(c).toBuilder().targetId(null).attemptId(null).build());
            }
            return Expanded.builder().source(source).requestId(requestId).bindings(replay).expansion(AuthOperationExpansionResult.builder()
                .batchId(batch.getId()).previousCursor(shard.getPreviousCursor()).nextCursor(shard.getNextCursor()).appendedCount(0)
                .expandedCount(batch.getExpandedCount()).status(batch.getStatus()).build()).build();
        }
        List<Binding> contributions=new ArrayList<>();Set<String> seen=new HashSet<>();
        for(ResourceInput input:shard.getResources()) {
            ResourceDecision d=versions.stageContribution(ContributionCommand.builder().sourceId(source.getSourceId()).sourceGeneration(source.getGeneration())
                .resource(input.getResource()).requestId(requestId).participation(input.getParticipation()).windows(input.getWindows()).build());
            require(seen.add(d.getResourceId()),"同来源分片资源重复");
            contributions.add(Binding.builder().sourceId(source.getSourceId()).sourceGeneration(source.getGeneration()).resourceId(d.getResourceId())
                .resourceGeneration(d.getGeneration()).requestId(requestId).build());
        }
        AuthOperationExpansionResult expansion=operations.appendTargets(AuthOperationAppendCommand.builder().batchId(batch.getId())
            .previousCursor(shard.getPreviousCursor()).nextCursor(shard.getNextCursor()).request(request).build());
        require(workflow.requestCount(batch.getId())<=sourceCount(batch),"来源数超过服务器冻结投影");
        if(shard.isFinalSourcePage())versions.sealSourceExpansion(source.getSourceId(),source.getGeneration(),source.getSourceFingerprint());
        return Expanded.builder().source(source).requestId(requestId).expansion(expansion).bindings(contributions).build();
    }

    /** 第二阶段每个事务只处理一个物理资源，完整绑定本批该资源的来源集合。 */
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public List<Binding> bindLane(Long batchId,String resourceId,long previousCursor,long nextCursor) {
        ResourceDecision decision=versions.currentDesired(resourceId);
        SmtAuthOperationBatch batch=required(batches.selectById(batchId),"批次不存在");
        require(Objects.equals(batch.getParkId(),decision.getResource().getParkId()),"资源园区与批次不符");
        require(workflow.requestCount(batchId)==sourceCount(batch) && workflow.unsealedCount(batchId)==0,"全部冻结来源登记封口后才能绑定物理目标");
        journal(batchId,previousCursor,nextCursor,"BIND",hash(part("BIND")+part(String.valueOf(batchId))+part(String.valueOf(previousCursor))
            +part(String.valueOf(nextCursor))+part(resourceId)),null,null);
        SmtAuthOperationTarget bound=workflow.laneTarget(batchId,resourceId);
        if(bound!=null) {
            // 原 lane 已冻结；晚于它的跨批次版本由持久恢复队列生成独立补偿批次。
            operations.appendTargets(AuthOperationAppendCommand.builder().batchId(batchId).previousCursor(previousCursor).nextCursor(nextCursor).build());
            List<Binding> out=new ArrayList<>();for(SmtAuthSourceResource c:workflow.targetContributions(bound.getId()))out.add(binding(c));return out;
        }
        List<SmtAuthSourceResource> contributions=workflow.laneContributions(batchId,resourceId);
        require(!contributions.isEmpty() && contributions.size()<=1000,"物理资源的批次来源必须为1至1000个");
        for(SmtAuthSourceResource c:contributions)decision=versions.stageContribution(contributionCommand(c,decision.getResource()));
        require(!decision.isRequiresMultipleWindows(),"MULTI_WINDOW_UNSUPPORTED：来源贡献已留存，禁止拉宽精确窗口");
        AuthOperationTargetCommand target=target(batch.getParkId(),contributions.get(0).getRequestId(),decision).toBuilder().id(IdWorker.getId()).build();
        AuthOperationExpansionResult expansion=operations.appendTargets(AuthOperationAppendCommand.builder().batchId(batchId).previousCursor(previousCursor)
            .nextCursor(nextCursor).target(target).build());
        require(expansion.getExpandedCount()<=batch.getExpectedCount(),"资源数量超过服务器冻结投影");
        List<Binding> out=new ArrayList<>();
        for(SmtAuthSourceResource c:contributions) {
            Binding b=binding(c).toBuilder().resourceGeneration(decision.getGeneration()).targetId(target.getId()).attemptId(null).build();
            versions.bindTarget(b);out.add(b);
        }
        return out;
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public AuthOperationBatchResult finish(Long batchId) {
        SmtAuthOperationBatch b=required(batches.selectById(batchId),"批次不存在");
        require(workflow.requestCount(batchId)==sourceCount(b) && workflow.unsealedCount(batchId)==0,"来源尚未完整封口");
        return operations.finishExpansion(batchId,b.getExpectedCount());
    }

    /** NEVER 拒绝外层事务，foundation 的领取事务单独提交后才能 prepare。 */
    @Transactional(propagation=Propagation.NEVER)
    public List<AuthOperationClaimedTarget> claim(AuthOperationClaimCommand command) { return operations.claim(command); }

    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public Prepared prepare(Binding b,AuthOperationSubmissionCommand command) { return prepareInternal(b,command); }
    private Prepared prepareInternal(Binding b,AuthOperationSubmissionCommand command) {
        require(Objects.equals(b.getTargetId(),command.getTargetId()) && Objects.equals(b.getAttemptId(),command.getAttemptId()),"绑定与提交尝试不一致");
        BindingResult gate=gateBinding(b);
        if(!"READY".equals(gate.getOutcome()))return Prepared.builder().outcome(gate.getOutcome()).binding(b).build();
        versions.bindAttempt(b);
        AuthOperationSubmissionResult result=operations.prepareSubmission(command);
        if(!result.isPersisted())throw new IllegalStateException("租约或尝试已变化，不能准备设备下发");
        return Prepared.builder().outcome("READY").binding(b).submission(result).build();
    }

    /** 外部 HTTP 已结束后，单独持久登记真实外部命令 ID。 */
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public AuthOperationSubmissionResult submitted(AuthOperationSubmissionCommand command) { return operations.markSubmitted(command); }

    /** 外部受理号与旧任务下载号必须在同一本库事务内登记。 */
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public AuthOperationSubmissionResult submittedWithinTransaction(AuthOperationSubmissionCommand command) { return operations.markSubmitted(command); }

    /** 本条事件先留存再过版本门禁，CONFIRMED 不是本条事件可信性的依据。 */
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public Received receive(Binding b,AuthOperationReceiptCommand event,ConvergenceHandler handler) { return receiveInternal(b,event,null,handler); }
    private Received receiveInternal(Binding b,AuthOperationReceiptCommand event,TargetEvidenceHandler targetHandler,ConvergenceHandler handler) {
        require(Objects.equals(b.getTargetId(),event.getTargetId()) && Objects.equals(b.getAttemptId(),event.getAttemptId()),"事件与来源绑定不一致");
        require(present(event.getEventNamespace()) && event.getEventNamespace().length()<=59,"事件命名空间必须为1至59字符");
        versions.currentDesired(b.getResourceId());
        SmtAuthSourceResource c=required(workflow.exactBinding(b.getSourceId(),b.getSourceGeneration(),b.getResourceId(),b.getTargetId(),b.getRequestId()),"事件缺少历史绑定");
        SmtAuthOperationTarget t=required(targets.selectById(b.getTargetId()),"目标不存在");
        SmtAuthOperationAttempt a=required(attempts.selectByIdAndTarget(b.getAttemptId(),b.getTargetId()),"尝试不存在");
        boolean trusted=trustedSuccess(event,t,a);
        // 同一 event 的可信分类成为不可变命名空间的一部分，拒绝由不可信重放偷偷升级。
        AuthOperationReceiptCommand stored=event.toBuilder().eventNamespace((trusted?"WF:T:":"WF:U:")+event.getEventNamespace()).trustedDeviceEvidence(trusted).build();
        AuthOperationReceiptResult receipt=operations.recordReceipt(stored);
        EvidenceResult evidence=versions.applyEvidence(Evidence.builder().binding(b).trusted(trusted).action(t.getAction())
            .sourceRowId(c.getSourceRowId()).sourceFingerprint(c.getSourceFingerprint()).build());
        boolean converged=false,physicalSettled=false;
        if(evidence.isMayApply()) {
            SourceSnapshot snapshot=SourceSnapshot.builder().sourceId(b.getSourceId()).generation(b.getSourceGeneration())
                .sourceRowId(c.getSourceRowId()).fingerprint(c.getSourceFingerprint()).build();
            // 不同事件键可描述同一次已完成物理副作用；只复用同 target、attempt、资源代次的可信 Y 事件。
            if(workflow.eventRecordConverged(receipt.getEventId())==0) {
                Long proof=workflow.completedRecordEvent(b,receipt.getEventId());
                if(proof!=null)require(workflow.inheritRecordEvent(receipt.getEventId(),proof,b)==1,"既有record证据继承门禁冲突");
            }
            if(targetHandler!=null && workflow.eventRecordConverged(receipt.getEventId())==0) {
                SmtAuthSourceCoord source=required(workflow.source(b.getSourceId()),"来源不存在");
                SourceSnapshot exact=snapshot.toBuilder().sourceKind(source.getSourceKind()).stableKey(source.getStableKey()).subjectType(source.getSubjectType())
                    .subjectId(source.getSubjectId()).action(source.getAction()).build();
                TargetEvidence context=TargetEvidence.builder().binding(b).eventId(receipt.getEventId()).taskId(a.getTaskId()).action(t.getAction())
                    .current(evidence.getCurrent()).source(exact).build();
                if(!targetHandler.apply(context))throw new IllegalStateException("当前目标record条件写回失败");
                require(workflow.finishEvent(receipt.getEventId())==1,"当前事件record收敛标记冲突");
            }
            if(workflow.eventRecordConverged(receipt.getEventId())==0)return Received.builder().receipt(receipt).evidence(evidence).sourceConverged(false).build();
            physicalSettled=trusted && "CURRENT_APPLIED".equals(evidence.getOutcome());
            versions.confirmSourceResource(snapshot.getSourceId(),snapshot.getGeneration(),b.getResourceId(),snapshot.getSourceRowId(),snapshot.getFingerprint());
            if(handler!=null && versions.pendingSourceResources(snapshot.getSourceId(),snapshot.getGeneration(),null,1).isEmpty())
                converged=convergeSourceInternal(snapshot,handler);
            if(converged)refreshTargetInternal(t.getId());
        } else if(!trusted && Objects.equals(evidence.getCurrent().getBlockingAttemptId(),b.getAttemptId()))versions.markUnknown(b);
        else if(trusted && Arrays.asList("STALE_COMPENSATE","STALE_REPLAY").contains(evidence.getOutcome())) {
            // applyEvidence已在本事务持久记录旧尝试并确保后继代次；重放不要求再次创建补偿。
            physicalSettled=Objects.equals(evidence.getCurrent().getResourceId(),b.getResourceId())
                && evidence.getCurrent().getGeneration()>b.getResourceGeneration();
        }
        return Received.builder().receipt(receipt).evidence(evidence).sourceConverged(converged).physicalSettled(physicalSettled).build();
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public Received receive(Binding b,AuthOperationReceiptCommand event,TargetEvidenceHandler targetHandler,ConvergenceHandler sourceHandler) {
        return receiveInternal(b,event,targetHandler,sourceHandler);
    }
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public Received receiveWithinTransaction(Binding b,AuthOperationReceiptCommand event,TargetEvidenceHandler targetHandler,ConvergenceHandler sourceHandler) {
        return receiveInternal(b,event,targetHandler,sourceHandler);
    }
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public Prepared prepareWithinTransaction(Binding b,AuthOperationSubmissionCommand command) { return prepareInternal(b,command); }

    /** 已应用证据可以复用；仍有未决 owner 或 blocker 时只返回等待状态。 */
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public Prepared reuse(Binding binding) {
        BindingResult result=gateBinding(binding);
        if("REUSE_APPLIED".equals(result.getOutcome())) {
            if(!localRecordCompleted(binding.getResourceId()))return Prepared.builder().outcome("REUSE_PENDING").binding(binding).build();
            SmtAuthSourceResource c=required(workflow.exactBinding(binding.getSourceId(),binding.getSourceGeneration(),binding.getResourceId(),binding.getTargetId(),binding.getRequestId()),"来源绑定不存在");
            versions.confirmSourceResource(binding.getSourceId(),binding.getSourceGeneration(),binding.getResourceId(),c.getSourceRowId(),c.getSourceFingerprint());
            workflow.confirmReused(binding.getTargetId(),binding.getResourceGeneration(),now());
        }
        return Prepared.builder().outcome(result.getOutcome()).binding(binding).build();
    }

    /** 每次只确认一个资源，超过 200 个资源的来源由调度分页调用。 */
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public void confirmResource(SourceSnapshot source,String resourceId) {
        if(localRecordCompleted(resourceId))versions.confirmSourceResource(source.getSourceId(),source.getGeneration(),resourceId,source.getSourceRowId(),source.getFingerprint());
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public boolean convergeSource(SourceSnapshot source,ConvergenceHandler handler) { return convergeSourceInternal(source,handler); }
    private boolean convergeSourceInternal(SourceSnapshot s,ConvergenceHandler handler) {
        if(handler==null)return false;
        String resource=workflow.sourceResource(s.getSourceId(),s.getGeneration());
        if(resource==null)return false;
        versions.currentDesired(resource); // 先取得主体锁，门禁与后续来源写回共用同一事务。
        if(workflow.sourcePendingRecords(s.getSourceId(),s.getGeneration())!=0)return false;
        versions.completeSource(s.getSourceId(),s.getGeneration(),s.getSourceRowId(),s.getFingerprint());
        if(workflow.sourcePendingRequests(s.getSourceId(),s.getGeneration())==0)return true;
        SmtAuthSourceCoord canonical=required(workflow.source(s.getSourceId()),"来源不存在");
        SourceSnapshot exact=s.toBuilder().sourceKind(canonical.getSourceKind()).stableKey(canonical.getStableKey()).subjectType(canonical.getSubjectType())
            .subjectId(canonical.getSubjectId()).action(canonical.getAction()).build();
        if(!handler.apply(exact))throw new IllegalStateException("业务条件收敛未命中精确来源快照");
        workflow.convergeRequests(s.getSourceId(),s.getGeneration(),now());return true;
    }

    /** 来源全部收敛后，按有界 target 分页同步逻辑目标及批次统计。 */
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public boolean refreshTarget(Long targetId) { return refreshTargetInternal(targetId); }
    private boolean refreshTargetInternal(Long targetId) {
        SmtAuthOperationTarget t=required(targets.selectById(targetId),"目标不存在");
        List<SmtAuthSourceResource> targetSources=workflow.targetContributions(targetId);
        if(targetSources.isEmpty() || !localRecordCompleted(targetSources.get(0).getResourceCoordId()))return false;
        if(Arrays.asList("QUEUED","EXECUTING","VERIFYING").contains(t.getState()) && workflow.pendingTargetRequests(targetId)==0) {
            List<SmtAuthSourceResource> contributions=workflow.targetContributions(targetId);
            require(!contributions.isEmpty() && contributions.size()<=1000,"逻辑目标缺少完整来源证据");
            String resourceId=contributions.get(0).getResourceCoordId();ResourceDecision current=versions.currentDesired(resourceId);
            for(SmtAuthSourceResource c:contributions) {
                require(resourceId.equals(c.getResourceCoordId()),"逻辑目标存在跨资源关联");
                versions.confirmSourceResource(c.getSourceCoordId(),c.getSourceGeneration(),resourceId,c.getSourceRowId(),c.getSourceFingerprint());
            }
            Long eventId=required(workflow.currentTrustedEvent(resourceId,current.getGeneration()),"缺少可追溯的当前可信事件");
            workflow.settleRetained(targetId,"RETAINED_BY_CURRENT_EVIDENCE;resource="+resourceId+";generation="+current.getGeneration()
                +";event="+eventId+";desired="+current.getDesiredFingerprint(),now());
        }
        workflow.convergeTarget(targetId,now());workflow.refreshBatch(t.getBatchId(),now());
        return "CONVERGED".equals(targets.selectById(targetId).getState());
    }
    /** 共享来源复用同一物理资源的当前 record，不要求每个来源重复写一次。 */
    private boolean localRecordCompleted(String resourceId) {
        ResourceDecision current=versions.currentDesired(resourceId);
        return current.getAppliedGeneration()==current.getGeneration() && workflow.currentTrustedEvent(resourceId,current.getGeneration())!=null;
    }
    @Transactional(readOnly=true)
    public List<Long> sourceTargets(SourceSnapshot s,Long after,int limit) {
        require(limit>0&&limit<=200,"分页上限为200");return workflow.sourceTargets(s.getSourceId(),s.getGeneration(),after,limit);
    }
    @Transactional(readOnly=true)
    public List<SmtAuthSourceResource> pendingRecovery(int park,String after,int limit) {
        require(park>0&&limit>0&&limit<=200,"恢复分页参数无效");return workflow.pendingRecovery(park,after,limit);
    }

    /** 旧物理版本重算后创建独立批次，不改变原 expected、不预留新 ADD 来源代次。 */
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public Recovery recover(ContributionCommand command) { return recoverInternal(command); }

    /** 扫描结果只携带坐标；恢复时重读持久贡献，调用方不用解释内部窗口编码。 */
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public Recovery recoverPending(String sourceId,long generation,String resourceId) {
        ResourceDecision current=versions.currentDesired(resourceId);
        SmtAuthSourceResource c=required(workflow.contribution(sourceId,generation,resourceId),"待恢复贡献不存在");
        return recoverInternal(contributionCommand(c,current.getResource()));
    }
    private Recovery recoverInternal(ContributionCommand command) {
        SmtAuthSourceResource observed=required(workflow.contributionByKey(command.getSourceId(),command.getSourceGeneration(),command.getResource()),"恢复缺少持久来源资源");
        ResourceDecision live=versions.currentDesired(observed.getResourceCoordId()); // 取得主体锁后再判定恢复原因，不能与正常展开抢 owner。
        observed=required(workflow.contributionByKey(command.getSourceId(),command.getSourceGeneration(),command.getResource()),"加锁后恢复贡献不存在");
        SmtAuthSourceCoord exact=required(workflow.source(command.getSourceId()),"恢复来源不存在");
        require(Objects.equals(exact.getGeneration(),command.getSourceGeneration()) && Objects.equals(exact.getSourceRowId(),observed.getSourceRowId())
            && Objects.equals(exact.getSourceFingerprint(),observed.getSourceFingerprint()),"恢复来源代次或快照已变化");
        boolean needsRebinding=observed.getResourceGeneration()!=live.getGeneration() || observed.getTargetId()==null;
        if(needsRebinding && live.getAppliedGeneration()==live.getGeneration() && live.getBlockingTargetId()==null && live.getBlockingAttemptId()==null
            && workflow.currentTrustedEvent(live.getResourceId(),live.getGeneration())!=null
            && workflow.sourcePendingRequests(exact.getId(),exact.getGeneration())==0)
            return Recovery.builder().outcome("REUSE_APPLIED").binding(binding(observed)).build();
        if(needsRebinding && workflow.currentIntentCount(live.getResourceId(),live.getGeneration())>0)
            return Recovery.builder().outcome("CURRENT_INTENT_PENDING").binding(binding(observed)).build();
        ContributionRecovery r=versions.recoverContribution(command);
        Binding current=r.getCurrentBinding();
        if("BLOCKED".equals(r.getOutcome()))return Recovery.builder().outcome("BLOCKED").binding(current).build();
        if(current.getTargetId()!=null) {
            SmtAuthOperationTarget existing=required(targets.selectById(current.getTargetId()),"恢复目标不存在");
            return Recovery.builder().outcome(r.getOutcome()).binding(current).compensationBatchId(existing.getBatchId()).build();
        }
        require(!r.getCurrent().isRequiresMultipleWindows(),"MULTI_WINDOW_UNSUPPORTED：不能补偿为拉宽窗口");
        SmtAuthSourceCoord s=required(workflow.source(current.getSourceId()),"来源不存在");
        String key="wf-recover-"+hash(current.getSourceId()+":"+current.getSourceGeneration()+":"+current.getResourceId()+":"+current.getResourceGeneration());
        String snapshot=key+":"+r.getCurrent().getDesiredFingerprint();
        AuthOperationBatchResult accepted=acceptInternal(Selection.builder().parkId(s.getParkId()).idempotencyKey(key).action(r.getCurrent().getAction())
            .sourceType("7").sourceId(s.getId()).snapshot(snapshot).expectedCount(1).build());
        SmtAuthOperationBatch batch=required(batches.selectById(accepted.getBatchId()),"补偿批次不存在");
        SmtAuthDeleteRequest prior=workflow.request(batch.getId(),s.getId(),s.getGeneration());Long requestId=prior==null?IdWorker.getId():prior.getId();
        AuthOperationTargetCommand target=target(s.getParkId(),requestId,r.getCurrent());
        List<SmtAuthOperationTarget> existing=targets.selectByBatchIdAndTargetKeys(batch.getId(),Collections.singletonList(target.getTargetKey()));
        Long targetId=existing.isEmpty()?IdWorker.getId():existing.get(0).getId();
        SourceVersion source=SourceVersion.builder().sourceId(s.getId()).generation(s.getGeneration()).sourceRowId(s.getSourceRowId())
            .sourceFingerprint(s.getSourceFingerprint()).action(s.getAction()).build();
        operations.appendTargets(AuthOperationAppendCommand.builder().batchId(batch.getId()).previousCursor(0L).nextCursor(1L)
            .request(request(batch,source,requestId,s.getSubjectType(),snapshot)).target(target.toBuilder().id(targetId).build()).build());
        Binding bound=current.toBuilder().requestId(requestId).targetId(targetId).attemptId(null).build();
        BindingResult gate=versions.bindRecoveryTarget(bound);operations.finishExpansion(batch.getId(),1);
        return Recovery.builder().outcome(gate.getOutcome()).compensationBatchId(batch.getId()).binding(bound).build();
    }

    private BindingResult gateBinding(Binding b) {
        SmtAuthSourceCoord source=required(workflow.source(b.getSourceId()),"来源不存在");
        SmtAuthOperationTarget target=required(targets.selectById(b.getTargetId()),"目标不存在");
        return Objects.equals(source.getBatchId(),target.getBatchId())?versions.bindTarget(b):versions.bindRecoveryTarget(b);
    }

    private static AuthOperationRequestCommand request(SmtAuthOperationBatch batch,SourceVersion s,Long id,String subjectType,String payload) {
        return AuthOperationRequestCommand.builder().id(id).parkId(batch.getParkId()).subjectType(subjectType)
            .sourceType(batch.getSourceType()).sourceRowId(s.getSourceRowId()).sourceIdentityKey(s.getSourceId()).generation(s.getGeneration())
            .identitySnapshot(part(s.getSourceRowId())+part(s.getSourceFingerprint())+part(payload)).build();
    }
    private static AuthOperationTargetCommand target(Integer park,Long requestId,ResourceDecision d) {
        ResourceKey k=d.getResource();Window w=d.getWindows().isEmpty()?null:d.getWindows().get(0);
        String resource=AuthOperationVersionService.canonicalTargetResourceId(k);
        String key=hash(part(k.getSubjectType())+part(k.getSubjectId())+part(k.getDeviceId())+part(k.getAccessType())+part(k.getResourceType())
            +part(resource)+part(String.valueOf(d.getGeneration()))+part(d.getAction())+part(w==null?null:w.getFrom().toString())+part(w==null?null:w.getTo().toString()));
        return AuthOperationTargetCommand.builder().parkId(park).requestId(requestId).targetKey(key).subjectType(k.getSubjectType()).subjectId(k.getSubjectId())
            .resourceType(k.getResourceType()).deviceId(k.getDeviceId()).resourceId(resource).accessType(k.getAccessType()).operationQueue("AUTH")
            .action(d.getAction()).operationVersion(d.getGeneration()).validFrom(w==null?null:w.getFrom()).validTo(w==null?null:w.getTo()).build();
    }
    private static int sourceCount(SmtAuthOperationBatch batch) {
        String snapshot=batch.getSelectionSnapshot();require(snapshot!=null && snapshot.startsWith("WF1:"),"批次不是工作流服务器冻结投影");
        return Integer.parseInt(snapshot.substring(4,snapshot.indexOf(':',4)));
    }
    private static Binding binding(SmtAuthSourceResource c) {
        return Binding.builder().sourceId(c.getSourceCoordId()).sourceGeneration(c.getSourceGeneration()).resourceId(c.getResourceCoordId())
            .resourceGeneration(c.getResourceGeneration()).requestId(c.getRequestId()).targetId(c.getTargetId()).attemptId(c.getAttemptId()).build();
    }
    private static ContributionCommand contributionCommand(SmtAuthSourceResource c,ResourceKey resource) {
        List<Window> windows=new ArrayList<>();if(c.getWindows()!=null && !"#".equals(c.getWindows()))for(String part:c.getWindows().split(";")) {
            String[] halves=part.split("/");windows.add(Window.builder().from(LocalDateTime.parse(halves[0])).to(LocalDateTime.parse(halves[1])).build());
        }
        return ContributionCommand.builder().sourceId(c.getSourceCoordId()).sourceGeneration(c.getSourceGeneration()).resource(resource).requestId(c.getRequestId())
            .participation("ADD".equals(c.getAction())?"INCLUDE":"EXCLUDE").windows(windows).build();
    }
    /** 页摘要及业务贡献共用事务；唯一键冲突只允许规范内容完全相同的重放。 */
    private boolean journal(Long batch,long previous,long next,String kind,String fingerprint,String source,Long generation) {
        require(previous>=0 && next>previous,"分片游标必须递增");
        String prior=workflow.shardFingerprint(batch,previous);
        if(prior!=null) { require(prior.equals(fingerprint),"分片游标内容冲突");return false; }
        try { require(workflow.insertShard(batch,previous,next,kind,fingerprint,source,generation,now())==1,"分片日志未持久化");return true; }
        catch(DuplicateKeyException duplicate) { require(fingerprint.equals(workflow.shardFingerprint(batch,previous)),"分片游标内容冲突");return false; }
    }
    private static String shardFingerprint(Shard shard,SourceVersion source) {
        SourceIntent s=shard.getSource();StringBuilder out=new StringBuilder();
        for(Object value:Arrays.asList("STAGE",shard.getBatchId(),shard.getPreviousCursor(),shard.getNextCursor(),source.getSourceId(),source.getGeneration(),
            s.getParkId(),s.getSourceKind(),s.getStableKey(),s.getSubjectType(),s.getSubjectId(),s.getSourceRowId(),s.getSourceFingerprint(),s.getIntentKey(),
            s.getBatchId(),s.getAction(),s.getPayloadSnapshot(),shard.getStaffAuthId(),shard.isFinalSourcePage()))out.append(part(value==null?null:value.toString()));
        out.append(part(windowFingerprint(s.getWindows())));List<String> resources=new ArrayList<>();
        for(ResourceInput input:shard.getResources()) {
            ResourceKey k=required(input.getResource(),"资源不能为空");StringBuilder key=new StringBuilder();
            for(Object v:Arrays.asList(k.getParkId(),k.getSubjectType(),k.getSubjectId(),k.getAccessType(),k.getDeviceId(),k.getResourceType(),k.getResourceId(),
                k.getServiceType(),k.getCredentialChannel(),input.getParticipation()))key.append(part(v==null?null:v.toString()));
            resources.add(key.append(part(windowFingerprint(input.getWindows()))).toString());
        }
        Collections.sort(resources);for(String resource:resources)out.append(part(resource));return hash(out.toString());
    }
    private static String windowFingerprint(List<Window> windows) {
        List<String> values=new ArrayList<>();for(Window w:windows)values.add(part(w.getFrom()==null?null:w.getFrom().toString())+part(w.getTo()==null?null:w.getTo().toString()));
        Collections.sort(values);StringBuilder out=new StringBuilder();for(String value:values)out.append(part(value));return out.toString();
    }
    private static String part(String s) { return s==null?"-1:":s.length()+":"+s; }
    private static String hash(String text) {
        try { byte[] bytes=MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();
            for(byte x:bytes)b.append(String.format("%02x",x&255));return b.toString(); }
        catch(Exception e) { throw new IllegalStateException("SHA-256不可用",e); }
    }
    private static void require(boolean ok,String message) { if(!ok)throw new IllegalArgumentException(message); }
    private static <T> T required(T value,String message) { require(value!=null,message);return value; }
    private static LocalDateTime now() { return LocalDateTime.now(ZoneOffset.UTC); }
    public static boolean trustedSuccess(AuthOperationReceiptCommand e, SmtAuthOperationTarget t, SmtAuthOperationAttempt a) {
        if(e==null || t==null || a==null || !e.isTrustedDeviceEvidence() || !"SUCCESS".equals(e.getResultStatus())
            || !Arrays.asList("DEVICE_ACK","DEVICE_QUERY").contains(e.getEvidenceType())) return false;
        if(!Objects.equals(e.getTargetId(),t.getId()) || !Objects.equals(e.getAttemptId(),a.getId())
            || !Objects.equals(a.getTargetId(),t.getId()) || !Objects.equals(e.getAttemptNo(),a.getAttemptNo())
            || e.getLeaseToken()==null || !Objects.equals(e.getLeaseToken(),a.getLeaseToken())
            || !Objects.equals(e.getOperationVersion(),t.getOperationVersion())
            || !Objects.equals(e.getAccessType(),t.getAccessType()) || !Objects.equals(e.getAccessType(),a.getAccessType())) return false;
        return "DIRECT".equals(e.getAccessType()) ? present(e.getExternalCommandId()) && Objects.equals(e.getExternalCommandId(),a.getExternalCommandId())
            : "ISC".equals(e.getAccessType()) && present(e.getExternalBatchId()) && Objects.equals(e.getExternalBatchId(),a.getExternalBatchId());
    }
    private static boolean present(String x) { return x!=null && !x.trim().isEmpty(); }
}
