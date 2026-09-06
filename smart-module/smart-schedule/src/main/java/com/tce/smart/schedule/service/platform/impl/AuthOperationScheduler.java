package com.tce.smart.schedule.service.platform.impl;

import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.dto.authoperation.*;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSchedulerData.*;
import com.tce.smart.platform.core.dto.authtransport.AuthTransport.Run;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.SourceSnapshot;
import com.tce.smart.platform.core.entity.SmtAuthSourceResource;
import com.tce.smart.platform.core.service.impl.*;
import com.tce.smart.schedule.config.AuthOperationSchedulerProperties;
import com.tce.smart.schedule.config.AuthOperationSchedulerProperties.Instance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.*;
import java.util.*;
import java.util.concurrent.*;

/** 独立线程和有界队列隔离撤权、新增、配置、回执及本地恢复，HTTP 不占数据库事务。 */
@Slf4j @Service
public class AuthOperationScheduler {
    private AuthOperationDirectTakeoverService directTakeover;
    @org.springframework.beans.factory.annotation.Autowired public void setDirectTakeover(AuthOperationDirectTakeoverService service){this.directTakeover=service;}
    private final AuthOperationSchedulerProperties properties;
    private final AuthOperationProperties core;
    private final AuthOperationSchedulerService ledger;
    private final AuthOperationTransportFacade transport;
    private final EmployeeAuthOperationService employee;
    private final AuthOperationWorkflowService workflow;
    private final Map<String,ThreadPoolExecutor> executors=new LinkedHashMap<>();
    private final Set<String> active=ConcurrentHashMap.newKeySet();
    private String startupConfiguration;

    public AuthOperationScheduler(AuthOperationSchedulerProperties properties,AuthOperationProperties core,
            AuthOperationSchedulerService ledger,AuthOperationTransportFacade transport,
            EmployeeAuthOperationService employee,AuthOperationWorkflowService workflow) {
        this.properties=properties;this.core=core;this.ledger=ledger;this.transport=transport;this.employee=employee;this.workflow=workflow;
    }
    @PostConstruct public void start() {
        properties.validate(core);if(!properties.isEnabled())return;
        for(Instance instance:properties.getInstances())if("DIRECT".equals(instance.getAccessType())) {
            if(directTakeover==null)throw new IllegalStateException("DIRECT 持久启动门禁未装配");
            for(Integer park:instance.getParks())directTakeover.assertDirectSendEnabled(park,instance.getId());
        }
        startupConfiguration=properties.toString();
        for(String lane:Arrays.asList("DELETE","ADD","CONFIG","RECEIPT","EXPAND","RECOVERY")) {
            ThreadFactory factory=r->{Thread t=new Thread(r,"auth-"+lane.toLowerCase(Locale.ROOT));t.setDaemon(true);return t;};
            int workers=isHttp(lane)?properties.getInstances().stream().mapToInt(i->i.getParks().size()).sum():1;
            executors.put(lane,new ThreadPoolExecutor(workers,workers,0,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(properties.getQueueCapacity()),factory,new ThreadPoolExecutor.AbortPolicy()));
        }
    }
    public void tick() {
        if(!properties.isEnabled() || !core.isEnabled())return;
        if(!Objects.equals(startupConfiguration,properties.toString()))throw new IllegalStateException("调度配置变化后须校验归属并重启，不能在线侵占其它园区隔离槽");
        for(Instance instance:properties.getInstances())for(String lane:executors.keySet()) {
            if(!instance.getParks().stream().allMatch(core::enabledForPark))continue;
            if(isHttp(lane))for(Integer park:instance.getParks())enqueue(instance,lane,park);
            else enqueue(instance,lane,null);
        }
    }
    private static boolean isHttp(String lane) {return Arrays.asList("ADD","DELETE","CONFIG","RECEIPT").contains(lane);}
    private void enqueue(Instance instance,String lane,Integer park) {
        if("DIRECT".equals(instance.getAccessType()) && ("CONFIG".equals(lane) || "RECEIPT".equals(lane)))return;
        String key=instance.getId()+"/"+lane+"/"+park;if(!active.add(key))return;
        try {executors.get(lane).execute(()->{try {if(park==null)run(instance,lane);else runPark(instance,lane,park);}
            catch(Exception failure){log.warn("权限调度工作项失败 instance={} lane={} park={} type={}",instance.getId(),lane,park,failure.getClass().getSimpleName());}
            finally{active.remove(key);}});}
        catch(RejectedExecutionException full) {active.remove(key);}
    }
    long dispatchNow(){return System.nanoTime();}
    /** 同一个route工作者内连续执行有界片，active键始终由外层唯一owner释放。 */
    private void runPark(Instance instance,String lane,int park) {
        int maximum=properties.getDispatchWorkItems();
        long deadline=dispatchNow()+TimeUnit.MILLISECONDS.toNanos(properties.getDispatchMillis());
        for(int item=0;item<maximum && dispatchNow()<deadline;item++)
            if(!runParkItem(instance,lane,park))break;
    }
    /** 每项仍只执行一次精确接入组，完成租约后才允许重新预留下一项。 */
    private boolean runParkItem(Instance instance,String lane,int park) {
        if(!ledger.itemDue(instance.getId(),"P:"+lane+":"+park))return false;
        Grant grant=ledger.reservePark(policy(instance),lane,park,properties.dispatchPage(instance,lane),properties.getLeaseSeconds());
        if(grant==null)return false;
        Job job=grant.getJob();Long cursor=job.getNumberCursor();
        try {
            Run result=atPark(instance.getId(),lane,park,cursor,grant.getHttpBudget(),()-> {
                List<AuthOperationClaimedTarget> claims=grant.getClaims().get(park);
                if(claims!=null && !claims.isEmpty())return transport.submit(park,instance.getId(),claims,grant.getHttpBudget());
                List<Long> ids=grant.getPhaseIds();
                switch(grant.getPhaseOperation()) {
                case "SUBMIT":return transport.submitPreparedExact(park,instance.getId(),ids,grant.getHttpBudget());
                case "CONFIG_PROGRESS":return transport.advanceConfigExact(park,instance.getId(),ids,grant.getHttpBudget());
                case "DOWNLOAD":return transport.downloadExact(park,instance.getId(),ids,grant.getHttpBudget());
                case "RECEIPT":return transport.readReceiptExact(park,instance.getId(),ids,grant.getHttpBudget());
                default:throw new IllegalArgumentException("未知精确phase工作项");
                }
            });
            checkRun(result,grant.getHttpBudget());
            boolean success=result.getOutcome()!=null && !Arrays.asList("UNKNOWN","BACKOFF","VERIFYING","WAITING_ASSET","ERROR","FAILED").contains(result.getOutcome());
            Long next=success && !grant.getPhaseIds().isEmpty()?grant.getPhaseIds().get(grant.getPhaseIds().size()-1):cursor;
            boolean completed=ledger.complete(grant,success,next,job.getTextCursor(),job.getParkIndex(),success?0:ThreadLocalRandom.current().nextInt(1000));
            return success && completed && dispatchAdvanced(grant,park,result);
        } catch(Exception failure) {ledger.complete(grant,false,cursor,job.getTextCursor(),0,ThreadLocalRandom.current().nextInt(1000));return false;}
    }
    private static boolean dispatchAdvanced(Grant grant,int park,Run result) {
        List<AuthOperationClaimedTarget> claims=grant.getClaims().get(park);
        // prepare0的processed为0；nextCursor是本次冻结且可以发送的phase，不把空IDLE当进展。
        if(claims!=null && !claims.isEmpty())return grant.getHttpBudget()==0 && "IDLE".equals(result.getOutcome()) && result.getNextCursor()!=null;
        switch(grant.getPhaseOperation()) {
        case "SUBMIT":return result.getProcessed()>0 && Arrays.asList("WAITING_CONFIRM","WAITING_CONFIG").contains(result.getOutcome());
        case "CONFIG_PROGRESS":return result.getProcessed()>0 && "IDLE".equals(result.getOutcome());
        case "DOWNLOAD":return result.getHttpUsed()>0 && "WAITING_CONFIRM".equals(result.getOutcome());
        case "RECEIPT":return result.getProcessed()>0 || ("MORE".equals(result.getOutcome()) && result.getNextPage()>1);
        default:return false;
        }
    }
    /** 只有非空页且租约完成成功才续页；空页先持久清游标，下一tick再回绕。 */
    private boolean run(Instance instance,String lane) {
        if("DIRECT".equals(instance.getAccessType()) && ("CONFIG".equals(lane) || "RECEIPT".equals(lane)))return false;
        if("RECOVERY".equals(lane)) {
            for(String kind:Arrays.asList("EXPIRE","RECOVER","CONVERGE","REFRESH"))for(int page=0;page<properties.getRecoveryPages();page++)
                try {if(!run(instance,kind))break;}catch(Exception failure) {log.warn("权限恢复分段失败 instance={} lane={} type={}",instance.getId(),kind,failure.getClass().getSimpleName());break;}
            return false;
        }
        Policy policy=policy(instance);
        Grant grant=ledger.reserve(policy,lane,properties.getPageSize(),properties.getLeaseSeconds());
        if(grant==null)return false;
        Job job=grant.getJob();Long number=job.getNumberCursor();String text=job.getTextCursor();int park=job.getParkIndex();
        boolean nonEmpty=false;
        try {
            switch(lane) {
            case "EXPAND":
                ExpansionRound expansion=expand(policy,job);
                number=expansion.scans[1].cursor;text=expansion.scans[0].cursor==null?null:expansion.scans[0].cursor.toString();park=expansion.next;
                break;
            case "EXPIRE":
                List<Long> expired=ledger.expired(policy,number,properties.getPageSize());
                nonEmpty=!expired.isEmpty();
                for(Long id:expired) {ledger.expire(id);number=id;}
                if(expired.isEmpty())number=null;
                break;
            case "RECOVER":
                List<SmtAuthSourceResource> recoveries=ledger.recoveries(policy,text,properties.getPageSize());
                nonEmpty=!recoveries.isEmpty();
                for(SmtAuthSourceResource c:recoveries) {item(instance.getId(),"R:"+c.getId(),()->workflow.recoverPending(c.getSourceCoordId(),c.getSourceGeneration(),c.getResourceCoordId()));text=c.getId();}
                if(recoveries.isEmpty())text=null;
                break;
            case "CONVERGE":
                List<SmtAuthSourceResource> contributions=ledger.convergences(policy,text,properties.getPageSize());
                nonEmpty=!contributions.isEmpty();
                for(SmtAuthSourceResource c:contributions) {
                    item(instance.getId(),"C:"+c.getId(),()->workflow.convergeSource(SourceSnapshot.builder().sourceId(c.getSourceCoordId()).generation(c.getSourceGeneration())
                        .sourceRowId(c.getSourceRowId()).fingerprint(c.getSourceFingerprint()).build(),employee));text=c.getId();
                }
                if(contributions.isEmpty())text=null;
                break;
            case "REFRESH":
                List<Long> targets=ledger.refreshTargets(policy,number,properties.getPageSize());
                nonEmpty=!targets.isEmpty();
                for(Long id:targets) {item(instance.getId(),"T:"+id,()->workflow.refreshTarget(id));number=id;}
                if(targets.isEmpty())number=null;
                break;
            default:throw new IllegalArgumentException("未知调度队列");
            }
            return ledger.complete(grant,true,number,text,park,0) && nonEmpty;
        } catch(Exception failure) {
            ledger.complete(grant,false,job.getNumberCursor(),job.getTextCursor(),job.getParkIndex(),ThreadLocalRandom.current().nextInt(1000));
            log.warn("权限调度已延期 instance={} lane={} type={}",instance.getId(),lane,failure.getClass().getSimpleName());
            return false;
        }
    }
    long expansionNow(){return System.nanoTime();}
    /** 墙钟为启动检查点，不强行中断已经进入的短数据库事务。 */
    private ExpansionRound expand(Policy policy,Job job) {
        ExpansionRound round=new ExpansionRound(job);long deadline=expansionNow()+TimeUnit.MILLISECONDS.toNanos(properties.getExpansionMillis());
        Map<Long,Integer> stages=new HashMap<>();Set<Long> unavailable=new HashSet<>();int steps=0,empty=0,selections=0;
        while(steps<properties.getExpansionSteps() && expansionNow()<deadline && selections++<properties.getExpansionSteps()*2+2) {
            int priority=round.next;round.next=1-priority;ExpansionScan scan=round.scans[priority];
            Long batch=nextExpansionBatch(policy,priority==0?"DELETE":"ADD",scan,unavailable,deadline);
            if(batch==null){if(++empty>=2)break;continue;}empty=0;
            String key="B:"+batch;
            if(!ledger.itemDue(policy.getInstanceId(),key)){unavailable.add(batch);continue;}
            boolean finished=false;
            if(!stages.containsKey(batch))stages.put(batch,ledger.expansionStage(policy.getInstanceId(),batch));
            try {
                for(int slice=0;slice<properties.getExpansionBatchSteps() && steps<properties.getExpansionSteps() && expansionNow()<deadline;slice++) {
                    int stage=stages.getOrDefault(batch,0);steps++;
                    if(stage==0){if(!employee.stageNext(batch)){ledger.advanceExpansionStage(policy.getInstanceId(),batch,1);stages.put(batch,1);}}
                    else if(stage==1){if(employee.bindNextLane(batch,null)==null){ledger.advanceExpansionStage(policy.getInstanceId(),batch,2);stages.put(batch,2);}}
                    else {employee.finish(batch);finished=true;unavailable.add(batch);break;}
                }
                ledger.itemResult(policy.getInstanceId(),key,true,0);
                if(!finished && !scan.repeat.contains(batch))scan.repeat.addLast(batch);
            } catch(Exception failure) {
                unavailable.add(batch);ledger.itemResult(policy.getInstanceId(),key,false,ThreadLocalRandom.current().nextInt(1000));
            }
        }
        return round;
    }
    private Long nextExpansionBatch(Policy policy,String priority,ExpansionScan scan,Set<Long> unavailable,long deadline) {
        while(scan.page.isEmpty() && scan.pages<properties.getExpansionPages() && expansionNow()<deadline) {
            scan.pages++;
            List<Long> page=ledger.expansionBatches(policy,priority,scan.cursor,Math.min(100,properties.getPageSize()));
            if(!page.isEmpty()){scan.page.addAll(page);break;}
            // 到尾即在本轮回绕一次，避免下个tick才开始大批的下一步。
            if(scan.cursor!=null && !scan.wrapped){scan.cursor=null;scan.wrapped=true;continue;}
            break;
        }
        while(!scan.page.isEmpty()) {
            Long batch=scan.page.removeFirst();scan.cursor=batch;
            if(!unavailable.contains(batch))return batch;
        }
        while(!scan.repeat.isEmpty()) {
            Long batch=scan.repeat.removeFirst();if(!unavailable.contains(batch))return batch;
        }
        return null;
    }
    private static final class ExpansionScan {
        private Long cursor;private int pages;private boolean wrapped;
        private final Deque<Long> page=new ArrayDeque<>(),repeat=new ArrayDeque<>();
        private ExpansionScan(Long cursor){this.cursor=cursor;}
    }
    private static final class ExpansionRound {
        private final ExpansionScan[] scans;private int next;
        private ExpansionRound(Job job) {
            Long deletion=job.getTextCursor()==null?null:Long.valueOf(job.getTextCursor());
            scans=new ExpansionScan[]{new ExpansionScan(deletion),new ExpansionScan(job.getNumberCursor())};next=Math.floorMod(job.getParkIndex(),2);
        }
    }
    private static void checkRun(Run run,int budget) {
        if(run==null || run.getHttpUsed()<0 || run.getHttpUsed()>budget)throw new IllegalStateException("接入层违反HTTP工作预算");
        if("ERROR".equals(run.getOutcome()) || "FAILED".equals(run.getOutcome()))throw new IllegalStateException("接入分页失败");
    }
    private void item(String instance,String key,Runnable operation) {
        if(!ledger.itemDue(instance,key))return;
        try {operation.run();ledger.itemResult(instance,key,true,0);}
        catch(Exception failure) {ledger.itemResult(instance,key,false,ThreadLocalRandom.current().nextInt(1000));}
    }
    /** 一个园区回执失败只延期该园区，不阻塞共享实例中其余园区。 */
    private Run atPark(String instance,String lane,int park,Long cursor,int budget,java.util.function.Supplier<Run> operation) {
        String key="P:"+lane+":"+park;
        if(!ledger.itemDue(instance,key))return Run.builder().outcome("IDLE").nextCursor(cursor).build();
        try {
            Run result=operation.get();checkRun(result,budget);
            ledger.itemResult(instance,key,!"BACKOFF".equals(result.getOutcome()),ThreadLocalRandom.current().nextInt(1000));return result;
        } catch(Exception failure) {
            ledger.itemResult(instance,key,false,ThreadLocalRandom.current().nextInt(1000));
            // 不知道异常前已发多少请求，保守耗尽这次已预留额度。
            return Run.builder().outcome("BACKOFF").httpUsed(budget).nextCursor(cursor).build();
        }
    }
    public Snapshot snapshot(String instanceId) {
        if(!properties.isEnabled())throw new IllegalStateException("权限调度未启用");
        for(Instance instance:properties.getInstances())if(instanceId.equals(instance.getId()))return ledger.snapshot(policy(instance));
        throw new IllegalArgumentException("实例未配置");
    }
    @PreDestroy public void stop() {for(ThreadPoolExecutor executor:executors.values())executor.shutdown();}
    private static Policy policy(Instance i) {
        Policy p=new Policy();p.setInstanceId(i.getId());p.setAccessType(i.getAccessType());p.setParks(new ArrayList<>(i.getParks()));
        p.setHttpPerSecond(i.getHttpPerSecond());p.setDeleteHttp(i.getDeleteHttp());p.setAddHttp(i.getAddHttp());p.setConfigHttp(i.getConfigHttp());p.setReceiptHttp(i.getReceiptHttp());p.setBorrowHttp(i.getBorrowHttp());
        p.setMaxInflight(i.getMaxInflight());p.setDeleteInflight(i.getDeleteInflight());p.setAddInflight(i.getAddInflight());p.setPerDeviceInflight(i.getPerDeviceInflight());
        p.setPerParkDeleteInflight(i.getPerParkDeleteInflight());p.setPerParkAddInflight(i.getPerParkAddInflight());
        p.setPerDeviceDeleteInflight(i.getPerDeviceDeleteInflight());p.setPerDeviceAddInflight(i.getPerDeviceAddInflight());
        p.setPerParkInflight(i.getPerParkInflight());p.setMinParkInflight(i.getMinParkInflight());p.setParkHttpPerSecond(i.getParkHttpPerSecond());p.setDeviceHttpPerSecond(i.getDeviceHttpPerSecond());return p;
    }
}
