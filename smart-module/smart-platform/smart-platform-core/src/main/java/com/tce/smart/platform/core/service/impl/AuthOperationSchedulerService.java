package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.dto.authoperation.*;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSchedulerData.*;
import com.tce.smart.platform.core.entity.SmtAuthSourceResource;
import com.tce.smart.platform.core.mapper.AuthOperationSchedulerMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

/** 数据库实例行是额度唯一权威，领取与在途占位原子提交；任何方法都不发 HTTP。 */
@Service
public class AuthOperationSchedulerService {
    private final AuthOperationSchedulerMapper mapper;
    private final AuthOperationService operations;
    public AuthOperationSchedulerService(AuthOperationSchedulerMapper mapper,AuthOperationService operations) {this.mapper=mapper;this.operations=operations;}

    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public Grant reserve(Policy p,String lane,int page,long leaseSeconds) {
        if(Arrays.asList("ADD","DELETE","CONFIG","RECEIPT").contains(lane))throw new IllegalArgumentException("HTTP工作必须指定园区");
        return reserveWork(p,lane,null,page,leaseSeconds);
    }
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public Grant reservePark(Policy p,String lane,Integer park,int page,long leaseSeconds) {
        if(p==null || p.getParks()==null || park==null || !p.getParks().contains(park) || !Arrays.asList("ADD","DELETE","CONFIG","RECEIPT").contains(lane))throw new IllegalArgumentException("工作园区不属于实例");
        if(leaseSeconds<26)throw new IllegalArgumentException("HTTP租约必须覆盖三次有界请求及提交余量");
        return reserveWork(p,lane,park,page,leaseSeconds);
    }
    private Grant reserveWork(Policy p,String lane,Integer workPark,int page,long leaseSeconds) {
        if(p==null || p.getParks()==null || p.getParks().isEmpty() || p.getParks().size()>100 || page<1 || page>200 || leaseSeconds<5 || leaseSeconds>300)
            throw new IllegalArgumentException("调度预留参数越界");
        if(p.getInstanceId()==null || p.getInstanceId().trim().isEmpty() || p.getInstanceId().length()>100
            || !Arrays.asList("ISC","DIRECT").contains(p.getAccessType())
            || p.getParks().stream().anyMatch(park->park==null || park<=0)
            || new HashSet<>(p.getParks()).size()!=p.getParks().size())throw new IllegalArgumentException("调度路由参数无效");
        if(!Arrays.asList("DELETE","ADD","CONFIG","RECEIPT","EXPAND","EXPIRE","RECOVER","CONVERGE","REFRESH").contains(lane))throw new IllegalArgumentException("未知调度队列");
        State state=mapper.lockState(p.getInstanceId());
        if(state==null) {
            try {mapper.insertState(p.getInstanceId());}catch(DuplicateKeyException concurrent) { /* 并发建行后重新锁定同一实例。 */ }
            state=mapper.lockState(p.getInstanceId());
        }
        registerRoutes(p);
        String jobLane=workPark==null?lane:lane+":"+workPark;
        Job job=mapper.lockJob(p.getInstanceId(),jobLane);
        if(job==null) {mapper.insertJob(p.getInstanceId(),jobLane);job=mapper.lockJob(p.getInstanceId(),jobLane);}
        LocalDateTime now=mapper.now();
        String policyKey=policyKey(p);
        if(state.getPolicyKey()!=null && !state.getPolicyKey().equals(policyKey))throw new IllegalStateException("同实例调度配置与持久额度不一致，拒绝跨进程放大预算");
        state.setPolicyKey(policyKey);
        if(job.getLeaseUntil()!=null && job.getLeaseUntil().isAfter(now) || job.getNextAttemptAt()!=null && job.getNextAttemptAt().isAfter(now))return null;
        String window=mapper.windowKey();
        if(!window.equals(state.getWindowKey())) {
            state.setWindowKey(window);state.setTotalUsed(0);state.setDeleteUsed(0);state.setAddUsed(0);state.setConfigUsed(0);state.setReceiptUsed(0);state.setBorrowUsed(0);
        }
        Grant grant=new Grant();grant.setPolicy(p);grant.setJob(job);grant.setWindowKey(window);grant.setParkId(workPark);grant.setLane(lane);
        int reserved=reserved(p,lane),used=used(state,lane),budget=0;
        if(workPark!=null) {
            validateLayers(p);
            int cost=("ADD".equals(lane)||"DELETE".equals(lane)) && "ISC".equals(p.getAccessType())?3:1;
            int available=AuthOperationSchedulerQuota.http(p.getHttpPerSecond(),state.getTotalUsed(),reserved,used,p.getBorrowHttp(),state.getBorrowUsed(),p.getBorrowHttp());
            int parkUsed=quota(p,workPark,"-","PARK",lane,window);
            if(available<cost || parkUsed+cost>Math.min(layerLimit(p.getParkHttpPerSecond(),lane),reserved/p.getParks().size()))return null;
            Policy scope=new Policy();org.springframework.beans.BeanUtils.copyProperties(p,scope);scope.setParks(Collections.singletonList(workPark));
            String phasePriority=Arrays.asList("ADD","DELETE").contains(lane)?lane:("ADD".equals(job.getTextCursor())?"ADD":"DELETE");
            List<PhaseWork> phases=mapper.phaseWork(scope,lane,phasePriority,job.getDeviceCursor(),job.getNumberCursor(),window,cost);
            if(phases.isEmpty() && !Arrays.asList("ADD","DELETE").contains(lane)) {
                phasePriority="ADD".equals(phasePriority)?"DELETE":"ADD";
                phases=mapper.phaseWork(scope,lane,phasePriority,job.getDeviceCursor(),job.getNumberCursor(),window,cost);
            }
            Candidate candidate=null;int slots=0;PhaseWork phase=phases.isEmpty()?null:phases.get(0);
            // 同园区在新目标与持久PREPARED之间交替，历史准备项不能被持续新增流饿死。
            if(Arrays.asList("ADD","DELETE").contains(lane) && (phase==null || job.getParkIndex()%2==0)) {
                slots=parkSlots(p,lane,workPark);
                mapper.deferSaturated(scope,lane,slots==0,now,now.plusSeconds(1),page);
                if(slots>0) {
                    List<Candidate> pageOfOne=mapper.candidates(scope,lane,job.getDeviceCursor(),now,1);
                    if(!pageOfOne.isEmpty())candidate=pageOfOne.get(0);
                }
            }
            if(candidate==null && phase==null)return null;
            String device=candidate==null?phase.getDeviceId():candidate.getDeviceId();
            int deviceUsed=quota(p,workPark,device,"DEVICE",lane,window);
            if(deviceUsed+cost>layerLimit(p.getDeviceHttpPerSecond(),lane))return null;
            if(candidate!=null) {
                scope.setSelectedDevice(device);
                List<Candidate> group=mapper.candidates(scope,lane,null,now,Math.min(page,slots));
                Map<String,List<Long>> queues=new LinkedHashMap<>();
                for(Candidate c:group)queues.computeIfAbsent(c.getOperationQueue(),key->new ArrayList<>()).add(c.getId());
                List<AuthOperationClaimedTarget> all=new ArrayList<>();
                for(Map.Entry<String,List<Long>> entry:queues.entrySet())all.addAll(operations.claim(AuthOperationClaimCommand.builder().parkId(workPark).operationQueue(entry.getKey())
                    .accessType(p.getAccessType()).targetIds(entry.getValue()).maxCount(entry.getValue().size()).leaseSeconds(leaseSeconds).build()));
                if(all.isEmpty())return null;
                // 新领取只建立冻结phase，HTTP为零；下一工作项按精确兼容组预留HTTP并发送。
                grant.getClaims().put(workPark,all);job.setDeviceCursor(candidate.getFairKey());job.setParkIndex(1);
            } else {
                int groupLimit=Math.min(200,page);
                if("DIRECT".equals(p.getAccessType()))groupLimit=Math.min(Math.min(3,groupLimit),Math.min(available,
                    Math.min(Math.min(layerLimit(p.getParkHttpPerSecond(),lane),reserved/p.getParks().size())-parkUsed,layerLimit(p.getDeviceHttpPerSecond(),lane)-deviceUsed)));
                List<Long> ids=mapper.phaseGroup(scope,lane,phase.getId(),groupLimit);
                if(ids.isEmpty())return null;
                if(ids.size()>200 || !ids.contains(phase.getId()))throw new IllegalStateException("精确组必须包含所预留的优先级种子且不超过200");
                grant.setPhaseIds(ids);grant.setPhaseOperation(phase.getOperation());
                if(!Arrays.asList("ADD","DELETE").contains(lane))job.setTextCursor("ADD".equals(phasePriority)?"DELETE":"ADD");job.setDeviceCursor(phase.getFairKey());job.setParkIndex(0);
                budget="DIRECT".equals(p.getAccessType())?ids.size():cost;
                saveQuota(p,workPark,"-","PARK",lane,window,parkUsed+budget);
                saveQuota(p,workPark,device,"DEVICE",lane,window,deviceUsed+budget);
            }
        }
        int borrowed=Math.max(0,used+budget-reserved)-Math.max(0,used-reserved);
        state.setBorrowUsed(state.getBorrowUsed()+borrowed);state.setTotalUsed(state.getTotalUsed()+budget);setUsed(state,lane,used+budget);
        if(mapper.saveState(state)!=1)throw new IllegalStateException("实例额度保存失败");
        job.setLeaseToken(UUID.randomUUID().toString());job.setLeaseUntil(now.plusSeconds(leaseSeconds));
        if(mapper.saveJob(job)!=1)throw new IllegalStateException("调度游标租约保存失败");
        grant.setHttpBudget(budget);return grant;
    }
    private static int layerLimit(int rate,String lane) {return rate*(Arrays.asList("ADD","DELETE").contains(lane)?3:1)/8;}
    private static void validateLayers(Policy p) {
        if(p.getPerParkDeleteInflight()<1 || p.getPerParkAddInflight()<1 || p.getPerParkDeleteInflight()+p.getPerParkAddInflight()>p.getPerParkInflight()
            || p.getPerDeviceDeleteInflight()<1 || p.getPerDeviceAddInflight()<1 || p.getPerDeviceDeleteInflight()+p.getPerDeviceAddInflight()>p.getPerDeviceInflight()
            || p.getMinParkInflight()<1 || p.getPerParkInflight()<p.getMinParkInflight() || p.getPerParkInflight()>p.getMaxInflight()
            || (long)p.getMinParkInflight()*p.getParks().size()>p.getMaxInflight()-Math.max(p.getAddInflight(),p.getDeleteInflight())
            || Math.min(p.getAddHttp(),p.getDeleteHttp())<("ISC".equals(p.getAccessType())?3:1)*p.getParks().size()
            || Math.min(p.getConfigHttp(),p.getReceiptHttp())<p.getParks().size()
            || p.getParkHttpPerSecond()<8 || p.getParkHttpPerSecond()>p.getHttpPerSecond() || p.getDeviceHttpPerSecond()<8 || p.getDeviceHttpPerSecond()>p.getParkHttpPerSecond())
            throw new IllegalArgumentException("园区设备层级额度不合法");
    }
    private int quota(Policy p,int park,String device,String kind,String lane,String window) {
        Integer count=mapper.quotaUsed(p.getInstanceId(),park,device,kind,lane,window);return count==null?0:count;
    }
    private void saveQuota(Policy p,int park,String device,String kind,String lane,String window,int used) {
        if(mapper.saveQuota(p.getInstanceId(),park,device,kind,lane,window,used)!=1)throw new IllegalStateException("层级额度保存失败");
    }
    private int parkSlots(Policy p,String lane,int park) {
        Map<Integer,Integer> totals=new HashMap<>(),same=new HashMap<>();int total=0,laneTotal=0;
        for(Count c:mapper.parkCounts(p)) {
            totals.merge(c.getParkId(),c.getTargetCount(),Integer::sum);total+=c.getTargetCount();
            if(lane.equals(c.getPriority())){same.merge(c.getParkId(),c.getTargetCount(),Integer::sum);laneTotal+=c.getTargetCount();}
        }
        int retained=0,laneRetained=0;
        for(Integer other:p.getParks())if(other!=park) {
            retained+=Math.max(0,p.getMinParkInflight()-totals.getOrDefault(other,0));
            laneRetained+=Math.max(0,p.getMinParkInflight()-same.getOrDefault(other,0));
        }
        int opposite="ADD".equals(lane)?p.getDeleteInflight():p.getAddInflight();
        int parkOpposite="ADD".equals(lane)?p.getPerParkDeleteInflight():p.getPerParkAddInflight();
        return Math.max(0,Math.min(Math.min(p.getPerParkInflight()-totals.getOrDefault(park,0),p.getPerParkInflight()-parkOpposite-same.getOrDefault(park,0)),
            Math.min(p.getMaxInflight()-total-retained,p.getMaxInflight()-opposite-laneTotal-laneRetained)));
    }

    /** 失败不推进任何外部页游标；租约已换主时，旧完成通知不修改新归属。 */
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public boolean complete(Grant grant,boolean success,Long numberCursor,String textCursor,int parkIndex,int jitter) {
        Job expected=grant.getJob();Job current=mapper.lockJob(expected.getInstanceId(),expected.getLane());
        if(current==null || !Objects.equals(current.getLeaseToken(),expected.getLeaseToken()))return false;
        LocalDateTime now=mapper.now();
        if(success) {
            current.setNumberCursor(numberCursor);
            current.setTextCursor(textCursor);if(grant.getParkId()==null)current.setParkIndex(parkIndex);current.setFailures(0);current.setLastAdvancedAt(now);current.setNextAttemptAt(null);
        }
        else {current.setFailures(Math.min(30,current.getFailures()+1));current.setNextAttemptAt(now.plusNanos(AuthOperationSchedulerQuota.backoffMillis(current.getFailures(),jitter)*1000000L));}
        current.setLeaseToken(null);current.setLeaseUntil(null);return mapper.saveJob(current)==1;
    }

    @Transactional(readOnly=true)
    public Snapshot snapshot(Policy p) {Snapshot s=new Snapshot();s.setInstanceId(p.getInstanceId());s.setQueues(mapper.counts(p));s.setJobs(mapper.jobs(p.getInstanceId()));return s;}

    @Transactional(readOnly=true)
    public List<Long> expansionBatches(Policy p,String priority,Long after,int limit) {
        if(!Arrays.asList("ADD","DELETE").contains(priority) || limit<1 || limit>100)throw new IllegalArgumentException("展开业务类或页大小不合法");
        return mapper.expansionBatches(p,priority,after,limit);
    }
    @Transactional(readOnly=true)
    public List<Long> expired(Policy p,Long after,int limit) {bounded(limit);return mapper.expiredClaims(p,after,mapper.now(),limit);}

    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public boolean expire(Long id) {
        if(!"EXECUTING".equals(mapper.lockTarget(id)))return false;
        LocalDateTime now=mapper.now();
        if(mapper.expireUnsubmittedAttempt(id,now)!=1)return false;
        if(mapper.requeueExpiredTarget(id,now)!=1)throw new IllegalStateException("未提交尝试作废后目标未入队");
        return true;
    }
    @Transactional(readOnly=true)
    public List<SmtAuthSourceResource> recoveries(Policy p,String after,int limit) {bounded(limit);return mapper.recoveries(p,after,limit);}
    @Transactional(readOnly=true)
    public List<SmtAuthSourceResource> convergences(Policy p,String after,int limit) {bounded(limit);return mapper.convergences(p,after,limit);}
    @Transactional(readOnly=true)
    public List<Long> refreshTargets(Policy p,Long after,int limit) {bounded(limit);return mapper.refreshTargets(p,after,limit);}
    /** 已冻结选择只前进不回退；跨tick保存展开阶段，避免每次预算都消耗在同一个空页检查。 */
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public int expansionStage(String instance,Long batch) {
        Job state=mapper.lockJob(instance,"B:"+batch);return state==null || state.getNumberCursor()==null?0:state.getNumberCursor().intValue();
    }
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public void advanceExpansionStage(String instance,Long batch,int stage) {
        if(stage<1 || stage>2)throw new IllegalArgumentException("展开阶段只允许绑定或完成");
        mapper.lockState(instance);String key="B:"+batch;Job state=mapper.lockJob(instance,key);
        if(state==null){mapper.insertJob(instance,key);state=mapper.lockJob(instance,key);}
        state.setNumberCursor(Math.max(stage,state.getNumberCursor()==null?0:state.getNumberCursor()));
        if(mapper.saveJob(state)!=1)throw new IllegalStateException("展开阶段保存失败");
    }
    /** 单个坏批次或来源有自己的持久延期，不能占住整个恢复页。 */
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public boolean itemDue(String instance,String item) {
        Job state=mapper.lockJob(instance,item);return state==null || state.getNextAttemptAt()==null || !state.getNextAttemptAt().isAfter(mapper.now());
    }
    @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public void itemResult(String instance,String item,boolean success,int jitter) {
        // 与实例预留沿用相同锁顺序，避免两个进程同时创建延期记录。
        mapper.lockState(instance);Job state=mapper.lockJob(instance,item);
        if(state==null) {if(success)return;mapper.insertJob(instance,item);state=mapper.lockJob(instance,item);}
        LocalDateTime now=mapper.now();state.setFailures(success?0:Math.min(30,state.getFailures()+1));
        state.setNextAttemptAt(success?null:now.plusNanos(AuthOperationSchedulerQuota.backoffMillis(state.getFailures(),jitter)*1000000L));
        if(success)state.setLastAdvancedAt(now);mapper.saveJob(state);
    }
    /** 路由只登记不覆盖；每次预留重新核对，归属迁移后旧进程不能继续领额度。 */
    private void registerRoutes(Policy p) {
        Map<Integer,String> owners=new HashMap<>();
        for(Route route:mapper.routes(p.getParks(),p.getAccessType()))owners.put(route.getParkId(),route.getInstanceId());
        // 多园区申请按固定顺序争唯一键，避免不同配置顺序形成循环等待。
        for(Integer park:new TreeSet<>(p.getParks())) {
            String owner=owners.get(park);
            if(owner==null) {
                try {
                    if(mapper.insertRoute(park,p.getAccessType(),p.getInstanceId())!=1)throw new IllegalStateException("调度路由登记失败");
                    owner=p.getInstanceId();
                } catch(DuplicateKeyException concurrent) {owner=mapper.routeOwner(park,p.getAccessType());}
            }
            if(!p.getInstanceId().equals(owner))throw new IllegalStateException("调度路由已属于其他实例，必须显式运维迁移后再调度");
        }
    }
    private static void bounded(int n) {if(n<1 || n>200)throw new IllegalArgumentException("分页必须为1至200");}
    private static String policyKey(Policy policy) {
        try {
            byte[] value=java.security.MessageDigest.getInstance("SHA-256").digest(policy.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder out=new StringBuilder();for(byte b:value)out.append(String.format("%02x",b&255));return out.toString();
        } catch(java.security.NoSuchAlgorithmException impossible) {throw new IllegalStateException(impossible);}
    }
    private static int reserved(Policy p,String lane) {switch(lane){case "ADD":return p.getAddHttp();case "DELETE":return p.getDeleteHttp();case "CONFIG":return p.getConfigHttp();case "RECEIPT":return p.getReceiptHttp();default:return 0;}}
    private static int used(State s,String lane) {switch(lane){case "ADD":return s.getAddUsed();case "DELETE":return s.getDeleteUsed();case "CONFIG":return s.getConfigUsed();case "RECEIPT":return s.getReceiptUsed();default:return 0;}}
    private static void setUsed(State s,String lane,int n) {switch(lane){case "ADD":s.setAddUsed(n);break;case "DELETE":s.setDeleteUsed(n);break;case "CONFIG":s.setConfigUsed(n);break;case "RECEIPT":s.setReceiptUsed(n);break;default:break;}}
}
