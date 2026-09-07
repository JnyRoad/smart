package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 永久来源与资源协调。写入口必须经 Spring 代理调用；只包含本地短事务，不下发设备。
 * 锁顺序固定为主体、来源、单个资源；批量展开由调用方分片，不能在外层事务包住整个批次。
 */
@Service
public class AuthOperationVersionService {
    public static final int MAX_ACTIVE_SOURCES = 1000;
    public static final int MAX_RESOURCE_WINDOWS = 10000;
    public static final int MAX_RESOURCE_WINDOW_CHARS = 1000000;
    private final SmtAuthSubjectCoordMapper subjects;
    private final SmtAuthSourceCoordMapper sources;
    private final SmtAuthResourceCoordMapper resources;
    private final SmtAuthSourceResourceMapper contributions;
    private final SmtAuthIdentityAliasMapper aliases;

    public AuthOperationVersionService(SmtAuthSubjectCoordMapper subjects, SmtAuthSourceCoordMapper sources, SmtAuthResourceCoordMapper resources,
            SmtAuthSourceResourceMapper contributions, SmtAuthIdentityAliasMapper aliases) {
        this.subjects = subjects; this.sources = sources; this.resources = resources; this.contributions = contributions; this.aliases = aliases;
    }

    /** 冻结完整来源意图，员工 stableKey 必须由员工 ID 与权限组 ID 组成。 */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public SourceVersion reserveSourceIntent(SourceIntent command) {
        Objects.requireNonNull(command, "来源命令不能为空");
        require(command.getParkId() != null && command.getParkId() > 0, "园区无效");
        text(command.getSourceKind(),64); text(command.getStableKey(),1024);
        text(command.getSubjectType(),64); text(command.getSubjectId(),256);
        text(command.getSourceFingerprint(),64); text(command.getIntentKey(),256);
        text(command.getPayloadSnapshot(),1000000);
        require(command.getBatchId() != null && command.getBatchId() > 0, "批次无效");
        require("ADD".equals(command.getAction()) || "DELETE".equals(command.getAction()), "来源动作无效");
        String windows = encode(normalize(command.getWindows()));
        require(!"ADD".equals(command.getAction()) || !command.getWindows().isEmpty(), "新增来源必须冻结精确有效期");
        String identity = tuple(command.getParkId(),command.getSourceKind(),command.getStableKey());
        String id = hash(identity);
        ExpectedPredecessor expected=command.getExpectedPredecessor();
        String intentPayload=tuple(identity,command.getSubjectType(),command.getSubjectId(),
            command.getSourceRowId(),command.getSourceFingerprint(),command.getIntentKey(),command.getBatchId(),
            command.getAction(),windows,command.getPayloadSnapshot());
        // 不带前置证明时不追加任何字节，保持已冻结普通意图的幂等指纹。
        if(expected!=null) {
            validateExpectedPredecessor(command,expected);
            intentPayload+=tuple("EXPECTED_PREDECESSOR_V1",expected.getSourceId(),expected.getGeneration(),expected.getSourceFingerprint(),
                expected.getSourceRowId(),expected.getIntentKey(),expected.getBatchId(),expected.getAction());
        }
        String intentFingerprint = hash(intentPayload);
        lockSubject(command.getParkId(),command.getSubjectType(),command.getSubjectId());
        SmtAuthSourceCoord s = sources.lock(id);
        if (s == null) {
            require(expected==null,"前置来源不存在，不能创建后续撤销代次");
            SmtAuthSourceCoord fresh = new SmtAuthSourceCoord();
            fresh.setId(id); fresh.setParkId(command.getParkId()); fresh.setSourceKind(command.getSourceKind());
            fresh.setStableKey(command.getStableKey()); fresh.setSubjectType(command.getSubjectType()); fresh.setSubjectId(command.getSubjectId());
            fresh.setGeneration(1L); fresh.setCreateTime(now()); setIntent(fresh,command,windows,intentFingerprint);
            try { sources.insert(fresh); return sourceResult(fresh,false); }
            catch (DuplicateKeyException raced) { s = sources.lock(id); if(s == null) throw raced; }
        }
        require(identity.equals(tuple(s.getParkId(),s.getSourceKind(),s.getStableKey())), "来源哈希碰撞，必须核验完整身份");
        require(s.getSubjectType().equals(command.getSubjectType()) && s.getSubjectId().equals(command.getSubjectId()), "稳定来源主体不能变化");
        if (s.getIntentKey().equals(command.getIntentKey())) {
            require(s.getIntentFingerprint().equals(intentFingerprint), "相同幂等键的完整来源意图不一致");
            return sourceResult(s,true);
        }
        SmtAuthSourceResource history=contributions.historicalIntent(id,command.getIntentKey());
        if(history!=null) {
            require(history.getIntentFingerprint().equals(intentFingerprint),"历史幂等键的完整来源意图不一致");
            return SourceVersion.builder().sourceId(id).generation(history.getSourceGeneration()).sourceRowId(history.getSourceRowId())
                .sourceFingerprint(history.getSourceFingerprint()).action(history.getSourceAction()).state("HISTORICAL")
                .expanded(true).idempotent(true).build();
        }
        // 同键与历史键已先按完整指纹重放；仅首次reserve在主体/source锁内比较前置。
        if(expected!=null)require(s.getId().equals(expected.getSourceId()) && s.getGeneration().equals(expected.getGeneration())
            && Objects.equals(s.getSourceFingerprint(),expected.getSourceFingerprint()) && Objects.equals(s.getSourceRowId(),expected.getSourceRowId())
            && Objects.equals(s.getIntentKey(),expected.getIntentKey()) && Objects.equals(s.getBatchId(),expected.getBatchId())
            && Objects.equals(s.getAction(),expected.getAction()) && "ACTIVE".equals(s.getState()) && Integer.valueOf(1).equals(s.getExpanded()),
            "前置来源尚未精确完成或当前代次已变化");
        require("ACTIVE".equals(s.getState()) || "TOMBSTONE".equals(s.getState()), "来源展开、撤销或失败处理中不能重授");
        s.setGeneration(Math.addExact(s.getGeneration(),1L)); setIntent(s,command,windows,intentFingerprint);
        sources.update(s); return sourceResult(s,false);
    }

    private static void validateExpectedPredecessor(SourceIntent command,ExpectedPredecessor expected) {
        require("DELETE".equals(command.getAction()) && "ADD".equals(expected.getAction()),"前置证明仅支持已完成ADD后的DELETE");
        text(expected.getSourceId(),64);text(expected.getSourceFingerprint(),64);text(expected.getSourceRowId(),400);text(expected.getIntentKey(),256);
        require(expected.getGeneration()!=null && expected.getGeneration()>0 && expected.getBatchId()!=null && expected.getBatchId()>0,"前置代次或批次无效");
        require(Objects.equals(command.getSourceRowId(),expected.getSourceRowId()),"后续撤销不能改变前置物理来源行");
    }

    /** 每次只展开一个来源资源，提交后才能继续下一分片。 */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ResourceDecision stageContribution(ContributionCommand command) {
        return stageInternal(command);
    }

    private ResourceDecision stageInternal(ContributionCommand command) {
        Objects.requireNonNull(command,"贡献命令不能为空");
        SmtAuthSourceCoord source = lockSource(command.getSourceId(),command.getSourceGeneration(),null);
        ResourceKey key = validKey(command.getResource());
        require(source.getParkId().equals(key.getParkId()) && source.getSubjectType().equals(key.getSubjectType())
            && source.getSubjectId().equals(key.getSubjectId()), "来源与资源主体不匹配");
        require(command.getRequestId() != null && command.getRequestId() > 0,"请求 ID 无效");
        String participation=command.getParticipation()==null ? ("DELETE".equals(source.getAction())?"EXCLUDE":"INCLUDE") : command.getParticipation();
        require("INCLUDE".equals(participation)||"EXCLUDE".equals(participation),"资源参与意图必须为INCLUDE或EXCLUDE");
        require(!"DELETE".equals(source.getAction()) || "EXCLUDE".equals(participation),"整体删除只能排除资源");
        require(!"EXCLUDE".equals(participation)||command.getWindows().isEmpty(),"排除资源不能携带授权窗口");
        String contributionAction="INCLUDE".equals(participation)?"ADD":"DELETE";
        String contributionWindows="EXCLUDE".equals(participation)?"#":command.getWindows().isEmpty()?source.getWindows():encode(normalize(command.getWindows()));
        if("INCLUDE".equals(participation)) require(!"#".equals(contributionWindows),"保留资源必须有精确窗口");
        SmtAuthResourceCoord resource = ensureResource(key);
        String id = contributionId(source.getId(),source.getGeneration(),resource.getId());
        SmtAuthSourceResource c = contributions.selectById(id);
        if (c != null) {
            require((c.getRequestId().equals(command.getRequestId()) || contributions.historicalRequestCount(source.getId(),source.getGeneration(),resource.getId(),command.getRequestId())>0) && c.getSourceFingerprint().equals(source.getSourceFingerprint())
                && Objects.equals(c.getSourceRowId(),source.getSourceRowId()) && c.getAction().equals(contributionAction)
                && c.getWindows().equals(contributionWindows), "贡献重试资源意图或窗口不一致");
            ResourceDecision retry=reconcile(resource,false);
            rebaseContribution(c,retry.getGeneration());
            return retry;
        }
        require(source.getExpanded() == 0, "来源已封口，不能再增加分片");
        c = new SmtAuthSourceResource(); c.setId(id); c.setBindingRevision(0L); c.setSourceAction(source.getAction()); c.setIntentKey(source.getIntentKey()); c.setIntentFingerprint(source.getIntentFingerprint()); c.setSourceCoordId(source.getId()); c.setSourceGeneration(source.getGeneration());
        c.setResourceCoordId(resource.getId()); c.setResourceGeneration(resource.getGeneration()); c.setSourceRowId(source.getSourceRowId());
        c.setSourceFingerprint(source.getSourceFingerprint()); c.setWindows(contributionWindows); c.setAction(contributionAction);
        c.setState("DELETE".equals(contributionAction) ? "PENDING_REMOVE" : "ACTIVE"); c.setRequestId(command.getRequestId());
        c.setCreateTime(now()); c.setUpdateTime(now()); contributions.insert(c);
        ResourceDecision decision = reconcile(resource,source.getGeneration()>1 && "ADD".equals(contributionAction));
        c.setResourceGeneration(decision.getGeneration()); contributions.update(c);
        return decision;
    }

    /** 归档旧绑定后将当前业务贡献重算到最新资源代次，旧blocker与回执仍有独立历史坐标。 */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ContributionRecovery recoverContribution(ContributionCommand command) {
        SmtAuthSourceCoord source=lockSource(command.getSourceId(),command.getSourceGeneration(),null);
        String resourceId=hash(resourceCanonical(validKey(command.getResource())));
        SmtAuthSourceResource before=contributions.selectById(contributionId(source.getId(),source.getGeneration(),resourceId));
        Binding previous=before==null?null:binding(before);
        ResourceDecision current=stageInternal(command);
        SmtAuthSourceResource after=contributions.selectById(contributionId(source.getId(),source.getGeneration(),resourceId));
        String outcome=current.getBlockingAttemptId()!=null?"BLOCKED":current.getAppliedGeneration()==current.getGeneration()?"REUSE_APPLIED":after.getTargetId()==null?"BIND_CURRENT":"BOUND_CURRENT";
        return ContributionRecovery.builder().outcome(outcome).previousBinding(previous).currentBinding(binding(after)).current(current).build();
    }

    private void rebaseContribution(SmtAuthSourceResource current,long generation) {
        if(current.getResourceGeneration()==generation) return;
        if(current.getTargetId()!=null) {
            String historyId=hash(tuple(current.getId(),"BINDING",current.getTargetId()));
            SmtAuthSourceResource history=contributions.selectById(historyId);
            if(history==null) {
                history=new SmtAuthSourceResource();
                org.springframework.beans.BeanUtils.copyProperties(current,history);
                history.setId(historyId);history.setBindingRevision(current.getTargetId());contributions.insert(history);
            } else require(history.getTargetId().equals(current.getTargetId()) && history.getResourceGeneration().equals(current.getResourceGeneration()),"历史绑定坐标冲突");
        }
        current.setResourceGeneration(generation);current.setTargetId(null);current.setAttemptId(null);
        current.setState("DELETE".equals(current.getAction())?"PENDING_REMOVE":"ACTIVE");current.setUpdateTime(now());contributions.update(current);
    }

    private static Binding binding(SmtAuthSourceResource c) {
        return Binding.builder().sourceId(c.getSourceCoordId()).sourceGeneration(c.getSourceGeneration()).resourceId(c.getResourceCoordId())
            .resourceGeneration(c.getResourceGeneration()).requestId(c.getRequestId()).targetId(c.getTargetId()).attemptId(c.getAttemptId()).build();
    }

    /** 封口前必须为历史设备补齐本代贡献，防止覆盖遗漏旧设备。 */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void sealSourceExpansion(String sourceId,long generation,String fingerprint) {
        SmtAuthSourceCoord s = lockSource(sourceId,generation,fingerprint);
        require(contributions.unstagedCount(sourceId,generation) == 0,"来源历史资源尚未展开完毕");
        require(!contributions.currentForSource(sourceId,generation).isEmpty(),"零目标来源必须核验，不能直接封口");
        s.setExpanded(1); s.setUpdateTime(now()); sources.update(s);
    }

    /** 只允许已应用的当前精确期望收敛来源；返回快照供业务行条件删除，服务不删业务行。 */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public SourceVersion completeSource(String sourceId,long generation,String rowId,String fingerprint) {
        SmtAuthSourceCoord s = lockSource(sourceId,generation,fingerprint);
        require(Objects.equals(s.getSourceRowId(),rowId),"来源物理行已变化");
        require(s.getExpanded() == 1,"来源尚未封口");
        require(!contributions.currentForSource(sourceId,generation).isEmpty(),"空来源需要核验");
        List<SmtAuthSourceResource> list=contributions.pendingForSource(sourceId,generation,null,201);
        require(list.size()<=200,"来源超过单事务上限，请分页 confirmSourceResource 后再完成来源");
        for(SmtAuthSourceResource c:list) confirmResource(s,c,rowId,fingerprint);
        s.setState("DELETE".equals(s.getAction()) ? "TOMBSTONE" : "ACTIVE"); s.setUpdateTime(now()); sources.update(s);
        return sourceResult(s,false);
    }

    /** 大来源逐资源短事务确认，全部确认后 completeSource 只锁来源并结束门禁。 */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void confirmSourceResource(String sourceId,long generation,String resourceId,String rowId,String fingerprint) {
        SmtAuthSourceCoord s=lockSource(sourceId,generation,fingerprint);
        require(s.getExpanded()==1 && Objects.equals(s.getSourceRowId(),rowId),"来源未封口或物理行变化");
        SmtAuthSourceResource c=required(contributions.selectById(contributionId(sourceId,generation,resourceId)),"贡献不存在");
        confirmResource(s,c,rowId,fingerprint);
    }

    @Transactional(readOnly = true)
    public List<String> pendingSourceResources(String sourceId,long generation,String after,int limit) {
        require(limit>0 && limit<=200,"贡献分页参数无效");
        SmtAuthSourceCoord s=required(sources.selectById(sourceId),"来源不存在");require(s.getGeneration()==generation,"来源代次已变化");
        return Collections.unmodifiableList(contributions.pendingForSource(sourceId,generation,after,limit).stream()
            .map(SmtAuthSourceResource::getResourceCoordId).collect(Collectors.toList()));
    }

    private void confirmResource(SmtAuthSourceCoord s,SmtAuthSourceResource c,String rowId,String fingerprint) {
        SmtAuthResourceCoord r=lockResource(c.getResourceCoordId()); ResourceDecision live=decision(r,false);
        require(r.getBlockingTargetId()==null && r.getAppliedGeneration().equals(r.getGeneration())
            && r.getDesiredFingerprint().equals(live.getDesiredFingerprint()) && r.getBasisFingerprint().equals(live.getBasisFingerprint()),
            "资源当前期望尚无可信已应用证据");
        require(c.getSourceGeneration().equals(s.getGeneration()) && c.getSourceFingerprint().equals(fingerprint)
            && Objects.equals(c.getSourceRowId(),rowId),"贡献来源快照已经变化");
        c.setState("CONVERGED");c.setUpdateTime(now());contributions.update(c);
    }

    /** 验证当前来源与真实 request/target 同批次归属；跨批次保留独立逻辑目标。 */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public BindingResult bindTarget(Binding binding) {
        return bindTargetInternal(binding,false);
    }

    private BindingResult bindTargetInternal(Binding binding,boolean recovery) {
        SmtAuthSourceCoord s = lockSource(binding.getSourceId(),binding.getSourceGeneration(),null);
        SmtAuthResourceCoord r = lockResource(binding.getResourceId());
        SmtAuthSourceResource existing=required(contributions.selectById(contributionId(binding.getSourceId(),binding.getSourceGeneration(),binding.getResourceId())),"当前贡献不存在");
        SmtAuthSourceResource c = boundContribution(recovery?binding.toBuilder().requestId(existing.getRequestId()).build():binding,false);
        if(recovery) require(contributions.historicalBindingCount(s.getId(),s.getGeneration(),r.getId())>0,"补偿绑定必须保留原request/target历史");
        require(r.getGeneration() == binding.getResourceGeneration(),"目标资源代次已过期");
        ResourceDecision live=decision(r,false);
        require(r.getDesiredFingerprint().equals(live.getDesiredFingerprint()) && r.getBasisFingerprint().equals(live.getBasisFingerprint()),
            "来源集合已变化，必须重新协调资源");
        if(live.isRequiresMultipleWindows()) return BindingResult.builder().outcome("MULTI_WINDOW_UNSUPPORTED").current(live).build();
        if("ADD".equals(live.getAction())) require(contributions.targetWindowCount(binding.getTargetId(),live.getWindows().get(0).getFrom(),
            live.getWindows().get(0).getTo())==1,"目标有效期与当前精确期望不符");
        require(contributions.ownershipCount(binding,s,r,canonicalTargetResourceId(key(r)),recovery?null:s.getBatchId(),r.getAction(),false) == 1,
            "目标与请求的批次、主体、资源或代次归属不符");
        require(c.getTargetId() == null || c.getTargetId().equals(binding.getTargetId()),"贡献不能替换已绑定的逻辑目标");
        if(!c.getRequestId().equals(binding.getRequestId())) {
            require(recovery && c.getTargetId()==null,"当前代次已绑定，不能切换补偿请求");
            require(contributions.updateRequest(c.getId(),c.getRequestId(),binding.getRequestId())==1,"补偿请求映射已变化");
            c.setRequestId(binding.getRequestId());
        }
        c.setTargetId(binding.getTargetId()); c.setUpdateTime(now()); contributions.update(c);
        Long owner=r.getBlockingTargetId()!=null?r.getBlockingTargetId():contributions.executionOwner(r.getId(),r.getGeneration());
        Long reuse=Objects.equals(owner,binding.getTargetId())?null:owner;
        String outcome=r.getBlockingAttemptId()!=null?"BLOCKED":r.getAppliedGeneration().equals(r.getGeneration())?"REUSE_APPLIED":reuse!=null?"REUSE_PENDING":"READY";
        return BindingResult.builder().outcome(outcome).reuseTargetId(reuse).current(live).build();
    }

    /** 补偿批次只替换当前未绑定贡献的请求映射，原批次与正修订历史保持不变。 */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public BindingResult bindRecoveryTarget(Binding binding) {
        return bindTargetInternal(binding,true);
    }

    /** 先持久占住该物理资源，再在事务外下发；不同资源不会相互阻塞。 */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void bindAttempt(Binding binding) {
        SmtAuthSourceCoord s = lockSource(binding.getSourceId(),binding.getSourceGeneration(),null);
        SmtAuthResourceCoord r = lockResource(binding.getResourceId()); SmtAuthSourceResource c = boundContribution(binding,true);
        require(r.getGeneration() == binding.getResourceGeneration(),"旧代目标不能开始新尝试");
        ResourceDecision live=decision(r,false);
        require(r.getDesiredFingerprint().equals(live.getDesiredFingerprint()) && r.getBasisFingerprint().equals(live.getBasisFingerprint())
            && !live.isRequiresMultipleWindows(),"资源期望已变化或多窗口无法安全下发");
        require(r.getAppliedGeneration() < r.getGeneration(),"当前资源已应用，必须复用可信证据");
        Long owner=r.getBlockingTargetId()!=null?r.getBlockingTargetId():contributions.executionOwner(r.getId(),r.getGeneration());
        require(Objects.equals(owner,binding.getTargetId()),"当前目标不是资源确定执行者，必须复用owner");
        require(binding.getAttemptId()!=null && contributions.ownershipCount(binding,s,r,canonicalTargetResourceId(key(r)),null,r.getAction(),true)==1,
            "尝试与目标不匹配");
        require(r.getBlockingAttemptId()==null || sameBlocker(r,binding),"同资源已有未决尝试，不能越过物理顺序");
        c.setAttemptId(binding.getAttemptId()); c.setUpdateTime(now()); contributions.update(c);
        r.setBlockingTargetId(binding.getTargetId()); r.setBlockingAttemptId(binding.getAttemptId()); r.setBlockReason("EXECUTING");
        r.setUpdateTime(now()); resources.update(r);
    }

    /** 超时或未知提交保持同一阻塞映射，任何租约过期都不能清除它。 */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void markUnknown(Binding binding) {
        lockSourceAny(binding.getSourceId()); SmtAuthResourceCoord r=lockResource(binding.getResourceId());
        SmtAuthSourceResource c=boundContribution(binding,true);
        require(Objects.equals(c.getAttemptId(),binding.getAttemptId()) && sameBlocker(r,binding),"未知尝试必须匹配持久阻塞映射");
        r.setBlockReason("UNKNOWN"); r.setUpdateTime(now()); resources.update(r);
    }

    /** 旧可信结果只返回完整补偿依据，不能推进当前授权；证据真实性由上层设备证据门禁判定。 */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public EvidenceResult applyEvidence(Evidence evidence) {
        Objects.requireNonNull(evidence,"证据不能为空"); Binding b=Objects.requireNonNull(evidence.getBinding(),"证据绑定不能为空");
        SmtAuthSourceCoord s=lockSourceAny(b.getSourceId());
        SmtAuthResourceCoord r=lockResource(b.getResourceId()); SmtAuthSourceResource c=boundContribution(b,true);
        require(b.getAttemptId()!=null,"证据尝试未绑定");
        require(contributions.ownershipCount(b,s,r,canonicalTargetResourceId(key(r)),null,evidence.getAction(),true)==1,"证据动作或身份归属不符");
        ResourceDecision live=decision(r,false);
        if(!evidence.isTrusted()) return evidenceResult("UNTRUSTED",false,false,live);
        boolean current=c.getBindingRevision()==0 && Objects.equals(c.getAttemptId(),b.getAttemptId()) && (r.getBlockingAttemptId()==null || sameBlocker(r,b))
            && r.getGeneration()==b.getResourceGeneration() && s.getGeneration()==b.getSourceGeneration()
            && Objects.equals(s.getSourceRowId(),evidence.getSourceRowId()) && Objects.equals(s.getSourceFingerprint(),evidence.getSourceFingerprint())
            && Objects.equals(c.getSourceFingerprint(),s.getSourceFingerprint()) && Objects.equals(c.getSourceRowId(),s.getSourceRowId())
            && r.getDesiredFingerprint().equals(live.getDesiredFingerprint()) && r.getBasisFingerprint().equals(live.getBasisFingerprint())
            && r.getAction().equals(evidence.getAction());
        boolean newStaleEvidence=false;
        if(!current) {
            if(contributions.staleEvidenceCount(r.getId(),b.getAttemptId())>0)
                return evidenceResult("STALE_REPLAY",false,false,live);
            // 同一资源的旧attempt只建立一次补偿失效记录，与其在哪个共享来源上收到回执无关。
            SmtAuthSourceResource observed=new SmtAuthSourceResource();org.springframework.beans.BeanUtils.copyProperties(c,observed);
            observed.setId(hash(tuple(r.getId(),"STALE_EVIDENCE",b.getAttemptId())));observed.setBindingRevision(-b.getAttemptId());
            observed.setAttemptId(b.getAttemptId());observed.setState("STALE_OBSERVED");observed.setCreateTime(now());observed.setUpdateTime(now());
            contributions.insert(observed);newStaleEvidence=true;
            Long blockingGeneration=r.getBlockingTargetId()==null?null:contributions.targetGeneration(r.getBlockingTargetId());
            boolean mustAdvance=r.getAppliedGeneration().equals(r.getGeneration()) || b.getResourceGeneration()>=r.getGeneration()
                || (blockingGeneration!=null && blockingGeneration>=r.getGeneration());
            // 在途命令不能证明晚于旧副作用；新补偿必须位于其后，既有更高未提交代次才可复用。
            reconcile(r,mustAdvance);
        }
        // 只释放被这条可信结果证实已结束的旧尝试，其他在途尝试保持不变。
        if(sameBlocker(r,b)) { r.setBlockingTargetId(null); r.setBlockingAttemptId(null); r.setBlockReason(null); }
        if(current) { r.setAppliedGeneration(r.getGeneration()); c.setState("APPLIED"); c.setUpdateTime(now()); contributions.update(c); }
        r.setUpdateTime(now()); resources.update(r);
        return evidenceResult(current ? "CURRENT_APPLIED" : "STALE_COMPENSATE",current,newStaleEvidence,decision(r,false));
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ResourceDecision currentDesired(String resourceId) {
        SmtAuthResourceCoord identity=required(resources.selectById(resourceId),"资源不存在");
        lockSubject(identity.getParkId(),identity.getSubjectType(),identity.getSubjectId());
        // 主体锁可能等待过其他事务；使用会刷新会话缓存的加锁语句读取最新代次。
        return decision(lockResource(resourceId),false);
    }

    @Transactional(readOnly = true)
    public List<SourceBasis> auditResourceSources(String resourceId,String after,int limit) {
        require(limit>0 && limit<=200,"资源审计分页参数无效");
        return Collections.unmodifiableList(contributions.auditForResource(resourceId,after,limit).stream().map(AuthOperationVersionService::basis).collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public List<SourceVersion> pendingSources(int parkId,String after,int limit) {
        require(parkId>0 && limit>0 && limit<=200,"待处理分页参数无效");
        return Collections.unmodifiableList(sources.pending(parkId,after,limit).stream().map(s->sourceResult(s,false)).collect(Collectors.toList()));
    }

    /** 历史别名按设备、接入、资源及主体类型隔离，保存多代映射。 */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void rememberAlias(AliasCommand command) {
        ResourceKey k=validKey(command.getResource()); text(command.getAliasKind(),64); text(command.getAliasValue(),256);
        require(Arrays.asList("CARD_NO","PERSON_ID","OLD_TASK").contains(command.getAliasKind()),"历史别名种类无效");
        lockSubject(k.getParkId(),k.getSubjectType(),k.getSubjectId());
        SmtAuthResourceCoord r=lockResource(hash(resourceCanonical(k)));
        require(r.getResourceKey().equals(resourceCanonical(k)),"资源哈希碰撞");
        require(command.getResourceGeneration()>0 && command.getResourceGeneration()<=r.getGeneration(),"别名资源代次不存在");
        String id=hash(tuple(r.getId(),command.getResourceGeneration(),command.getAliasKind(),command.getAliasValue()));
        SmtAuthIdentityAlias existing=aliases.selectById(id);
        if(existing!=null) {
            require(existing.getResourceCoordId().equals(r.getId()) && existing.getResourceGeneration()==command.getResourceGeneration()
                && existing.getAliasKind().equals(command.getAliasKind()) && existing.getAliasValue().equals(command.getAliasValue()),"别名哈希碰撞"); return;
        }
        SmtAuthIdentityAlias a=new SmtAuthIdentityAlias(); a.setId(id); a.setParkId(k.getParkId()); a.setAccessType(k.getAccessType());
        a.setDeviceId(k.getDeviceId()); a.setResourceType(k.getResourceType()); a.setResourceValue(k.getResourceId()); a.setServiceType(k.getServiceType());
        a.setCredentialChannel(k.getCredentialChannel()); a.setSubjectType(k.getSubjectType()); a.setSubjectId(k.getSubjectId());
        a.setResourceCoordId(r.getId()); a.setResourceGeneration(command.getResourceGeneration()); a.setAliasKind(command.getAliasKind());
        a.setAliasValue(command.getAliasValue()); a.setCreateTime(now()); aliases.insert(a);
    }

    @Transactional(readOnly = true)
    public AliasResolution resolveLegacyAlias(ResourceKey scope,String kind,String value) {
        validKey(scope); text(kind,64); text(value,256);
        List<SmtAuthIdentityAlias> found=aliases.resolve(scope,kind,value);
        Set<String> subjects=new HashSet<>(); List<AliasMatch> matches=new ArrayList<>();
        for(SmtAuthIdentityAlias a:found) { subjects.add(tuple(a.getSubjectType(),a.getSubjectId()));
            matches.add(AliasMatch.builder().resourceId(a.getResourceCoordId()).subjectType(a.getSubjectType())
                .subjectId(a.getSubjectId()).resourceGeneration(a.getResourceGeneration()).build()); }
        return AliasResolution.builder().outcome(found.isEmpty()?"NONE":subjects.size()>1 || found.size()>200 ?"AMBIGUOUS":"UNIQUE")
            .matches(matches).build();
    }

    /** 旧 target 没有 service/channel 独立列，上层必须将此完整规范值存入 target.resourceId。 */
    public static String canonicalTargetResourceId(ResourceKey k) {
        validKey(k); String value=tuple(k.getResourceId(),k.getServiceType(),k.getCredentialChannel());
        require(value.length()<=200,"规范 target 资源标识过长"); return value;
    }

    private ResourceDecision reconcile(SmtAuthResourceCoord r,boolean forceNewGeneration) {
        ResourceDecision live=decision(r,false);
        boolean changed=forceNewGeneration || r.getGeneration()==0 || !r.getDesiredFingerprint().equals(live.getDesiredFingerprint());
        // 同一物理期望的多个来源复用资源代次和目标；来源集合指纹仍单独更新。
        if(changed) r.setGeneration(Math.addExact(r.getGeneration(),1L));
        r.setAction(live.getAction()); r.setWindows(encode(live.getWindows())); r.setDesiredFingerprint(live.getDesiredFingerprint());
        r.setBasisFingerprint(live.getBasisFingerprint()); r.setUpdateTime(now()); resources.update(r);
        return decision(r,changed);
    }

    private ResourceDecision decision(SmtAuthResourceCoord r,boolean changed) {
        // 一条SQL仅返回有效贡献元数据，先拒绝超限，再逐条加载CLOB，避免先物化大量历史和窗口。
        List<SmtAuthSourceResource> active=contributions.currentForResource(r.getId());
        require(active.size()<=MAX_ACTIVE_SOURCES,"单资源有效来源超过1000上限，必须核验");
        long count=0,chars=0;
        for(SmtAuthSourceResource c:active) {
            require(c.getWindowCount()!=null && c.getWindowLength()!=null && c.getWindowCount()>0 && c.getWindowLength()>0,"缺少有效窗口预算元数据");
            count=Math.addExact(count,c.getWindowCount());chars=Math.addExact(chars,c.getWindowLength());
            require(count<=MAX_RESOURCE_WINDOWS && chars<=MAX_RESOURCE_WINDOW_CHARS,"资源窗口总数或字符预算超限，必须核验");
        }
        List<Window> windows=new ArrayList<>();List<SourceBasis> sourceBasis=new ArrayList<>();StringBuilder fingerprint=new StringBuilder();
        for(SmtAuthSourceResource c:active) {
            String encoded=required(contributions.windowById(c.getId()),"贡献窗口不存在");
            require(encoded.length()==c.getWindowLength(),"窗口长度与元数据不符");
            List<Window> decoded=decode(encoded);require(decoded.size()==c.getWindowCount(),"窗口数量与元数据不符");
            windows.addAll(decoded);sourceBasis.add(basis(c));
            fingerprint.append(tuple(c.getSourceCoordId(),c.getSourceGeneration(),c.getCurrentSourceRowId(),c.getCurrentSourceFingerprint(),
                c.getSourceFingerprint(),c.getAction(),hash(encoded)));
        }
        List<Window> merged=normalize(windows);String action=merged.isEmpty()?"DELETE":"ADD";
        List<SmtAuthSourceResource> audit=contributions.auditForResource(r.getId(),null,201);
        return ResourceDecision.builder().resourceId(r.getId()).resource(key(r)).generation(r.getGeneration()).appliedGeneration(r.getAppliedGeneration())
            .action(action).windows(merged).desiredFingerprint(hash(tuple(action,encode(merged)))).basisFingerprint(hash(fingerprint.toString()))
            .sources(sourceBasis).auditSources(audit.stream().limit(200).map(AuthOperationVersionService::basis).collect(Collectors.toList())).auditTruncated(audit.size()>200)
            .blockingTargetId(r.getBlockingTargetId()).blockingAttemptId(r.getBlockingAttemptId()).requiresMultipleWindows(merged.size()>1).changed(changed).build();
    }

    private static SourceBasis basis(SmtAuthSourceResource c) {
        return SourceBasis.builder().sourceId(c.getSourceCoordId()).sourceGeneration(c.getSourceGeneration()).sourceRowId(c.getCurrentSourceRowId())
            .sourceFingerprint(c.getCurrentSourceFingerprint()).requestId(c.getRequestId()).action(c.getAction()).build();
    }

    private SmtAuthResourceCoord ensureResource(ResourceKey k) {
        String canonical=resourceCanonical(k), id=hash(canonical); SmtAuthResourceCoord r=resources.lock(id);
        if(r==null) {
            SmtAuthResourceCoord n=new SmtAuthResourceCoord(); n.setId(id); n.setParkId(k.getParkId()); n.setSubjectType(k.getSubjectType());
            n.setSubjectId(k.getSubjectId()); n.setAccessType(k.getAccessType()); n.setDeviceId(k.getDeviceId()); n.setResourceType(k.getResourceType());
            n.setResourceId(k.getResourceId()); n.setServiceType(k.getServiceType()); n.setCredentialChannel(k.getCredentialChannel()); n.setResourceKey(canonical);
            n.setGeneration(0L); n.setAppliedGeneration(0L); n.setAction("DELETE"); n.setWindows("#"); n.setDesiredFingerprint(hash(tuple("DELETE","#")));
            n.setBasisFingerprint(hash("")); n.setCreateTime(now()); n.setUpdateTime(now());
            try { resources.insert(n); r=n; } catch(DuplicateKeyException raced) { r=resources.lock(id); if(r==null) throw raced; }
        }
        require(r.getResourceKey().equals(canonical),"资源哈希碰撞，必须核对完整规范键"); return r;
    }
    private SmtAuthSourceResource boundContribution(Binding b,boolean targetRequired) {
        Objects.requireNonNull(b,"绑定不能为空");
        SmtAuthSourceResource c=targetRequired?contributions.bindingSnapshot(b):contributions.selectById(contributionId(b.getSourceId(),b.getSourceGeneration(),b.getResourceId()));
        c=required(c,"贡献或历史绑定不存在");
        require(c.getSourceCoordId().equals(b.getSourceId()) && c.getSourceGeneration()==b.getSourceGeneration()
            && c.getResourceCoordId().equals(b.getResourceId()) && c.getResourceGeneration()==b.getResourceGeneration()
            && c.getRequestId().equals(b.getRequestId()),"贡献版本或请求不匹配");
        if(targetRequired) require(Objects.equals(c.getTargetId(),b.getTargetId()) && b.getTargetId()!=null,"目标未绑定");
        return c;
    }
    private SmtAuthSourceCoord lockSource(String id,long gen,String fp) {
        SmtAuthSourceCoord s=lockSourceAny(id);
        require(s.getGeneration()==gen,"来源代次已经变化"); if(fp!=null) require(s.getSourceFingerprint().equals(fp),"来源指纹已经变化"); return s;
    }
    private SmtAuthSourceCoord lockSourceAny(String id) {
        text(id,64);SmtAuthSourceCoord identity=required(sources.identity(id),"来源不存在");
        lockSubject(identity.getParkId(),identity.getSubjectType(),identity.getSubjectId());
        SmtAuthSourceCoord locked=required(sources.lock(id),"来源不存在");
        require(Objects.equals(identity.getParkId(),locked.getParkId()) && Objects.equals(identity.getSubjectType(),locked.getSubjectType())
            && Objects.equals(identity.getSubjectId(),locked.getSubjectId()),"来源主体身份已变化");return locked;
    }
    private void lockSubject(Integer parkId,String type,String subjectId) {
        require(parkId!=null && parkId>0,"主体园区无效");text(type,64);text(subjectId,256);
        String id=hash(tuple(parkId,type,subjectId));SmtAuthSubjectCoord row=subjects.lock(id);
        if(row==null) {
            SmtAuthSubjectCoord fresh=new SmtAuthSubjectCoord();fresh.setId(id);fresh.setParkId(parkId);fresh.setSubjectType(type);fresh.setSubjectId(subjectId);fresh.setCreateTime(now());
            try {subjects.insert(fresh);row=fresh;}catch(DuplicateKeyException raced){row=subjects.lock(id);if(row==null)throw raced;}
        }
        require(row.getParkId().equals(parkId) && row.getSubjectType().equals(type) && row.getSubjectId().equals(subjectId),"主体协调键碰撞");
    }
    private SmtAuthResourceCoord lockResource(String id) { text(id,64); return required(resources.lock(id),"资源不存在"); }
    private static boolean sameBlocker(SmtAuthResourceCoord r,Binding b) { return b.getAttemptId()!=null && Objects.equals(r.getBlockingAttemptId(),b.getAttemptId()) && Objects.equals(r.getBlockingTargetId(),b.getTargetId()); }
    private static SourceVersion sourceResult(SmtAuthSourceCoord s,boolean retry) { return SourceVersion.builder().sourceId(s.getId()).generation(s.getGeneration()).sourceRowId(s.getSourceRowId()).sourceFingerprint(s.getSourceFingerprint()).action(s.getAction()).state(s.getState()).expanded(s.getExpanded()==1).idempotent(retry).build(); }
    private static EvidenceResult evidenceResult(String status,boolean apply,boolean compensate,ResourceDecision current) { return EvidenceResult.builder().outcome(status).mayApply(apply).compensationRequired(compensate).current(current).build(); }
    private static void setIntent(SmtAuthSourceCoord s,SourceIntent c,String windows,String fp) {
        s.setSourceRowId(c.getSourceRowId()); s.setSourceFingerprint(c.getSourceFingerprint()); s.setIntentKey(c.getIntentKey()); s.setIntentFingerprint(fp);
        s.setBatchId(c.getBatchId()); s.setAction(c.getAction()); s.setState("DELETE".equals(c.getAction())?"REVOKING":"EXPANDING"); s.setExpanded(0); s.setWindows(windows); s.setUpdateTime(now());
    }
    private static ResourceKey key(SmtAuthResourceCoord r) { return ResourceKey.builder().parkId(r.getParkId()).subjectType(r.getSubjectType()).subjectId(r.getSubjectId()).accessType(r.getAccessType()).deviceId(r.getDeviceId()).resourceType(r.getResourceType()).resourceId(r.getResourceId()).serviceType(r.getServiceType()).credentialChannel(r.getCredentialChannel()).build(); }
    private static ResourceKey validKey(ResourceKey k) { Objects.requireNonNull(k,"资源键不能为空"); require(k.getParkId()!=null && k.getParkId()>0,"园区无效"); text(k.getSubjectType(),64);text(k.getSubjectId(),256);text(k.getDeviceId(),256);text(k.getResourceType(),64);text(k.getResourceId(),256);text(k.getServiceType(),64);text(k.getCredentialChannel(),64);require("DIRECT".equals(k.getAccessType())||"ISC".equals(k.getAccessType()),"接入无效");return k; }
    private static String resourceCanonical(ResourceKey k) { String v=tuple(k.getParkId(),k.getSubjectType(),k.getSubjectId(),k.getAccessType(),k.getDeviceId(),k.getResourceType(),k.getResourceId(),k.getServiceType(),k.getCredentialChannel());require(v.length()<=1024,"规范资源键过长");return v; }
    private static String contributionId(String s,long g,String r) { return hash(tuple(s,g,r)); }
    private static LocalDateTime now() { return LocalDateTime.now(ZoneOffset.UTC); }
    private static void require(boolean condition,String message) { if(!condition) throw new IllegalArgumentException(message); }
    private static <T> T required(T object,String message) { require(object!=null,message);return object; }
    private static void text(String s,int max) { require(s!=null && !s.trim().isEmpty() && s.length()<=max,"必要标识缺失或过长"); }
    private static String tuple(Object... values) { StringBuilder b=new StringBuilder(); for(Object o:values) {String s=o==null?"":o.toString();b.append(s.length()).append(':').append(s);}return b.toString(); }
    private static String hash(String value) {
        try {byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte x:bytes)b.append(String.format("%02x",x & 255));return b.toString();}
        catch(NoSuchAlgorithmException e) {throw new IllegalStateException("缺少 SHA-256",e);}
    }
    private static List<Window> normalize(List<Window> input) {
        require(input!=null && input.size()<=10000,"有效期集合无效或超过上限"); List<Window> sorted=new ArrayList<>(input);
        for(Window w:sorted) require(w!=null && w.getFrom()!=null && w.getTo()!=null && w.getFrom().isBefore(w.getTo()),"有效期必须有明确且递增的起止时间");
        sorted.sort(Comparator.comparing(Window::getFrom).thenComparing(Window::getTo)); List<Window> result=new ArrayList<>();
        for(Window w:sorted) {
            if(result.isEmpty() || result.get(result.size()-1).getTo().isBefore(w.getFrom())) result.add(w);
            else {Window last=result.remove(result.size()-1);result.add(Window.builder().from(last.getFrom()).to(last.getTo().isAfter(w.getTo())?last.getTo():w.getTo()).build());}
        } return result;
    }
    private static String encode(List<Window> windows) { if(windows.isEmpty())return "#";return windows.stream().map(w->w.getFrom()+"/"+w.getTo()).collect(Collectors.joining(";")); }
    private static List<Window> decode(String encoded) { if("#".equals(encoded))return Collections.emptyList();List<Window> out=new ArrayList<>();for(String part:encoded.split(";")){String[] halves=part.split("/");out.add(Window.builder().from(LocalDateTime.parse(halves[0])).to(LocalDateTime.parse(halves[1])).build());}return out; }
}
