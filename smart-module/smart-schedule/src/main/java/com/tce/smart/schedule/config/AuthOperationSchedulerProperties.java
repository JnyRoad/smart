package com.tce.smart.schedule.config;

import com.tce.smart.platform.core.config.AuthOperationProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.*;

/** 默认关闭。共享接入实例必须显式映射，额度不能按调度进程放大。 */
@Data @Component @ConfigurationProperties(prefix="smart.auth-scheduler")
public class AuthOperationSchedulerProperties {
    private boolean enabled;
    private int queueCapacity=32;
    private int maxDispatchParks=16;
    private int pageSize=200;
    /** 同一route/lane/tick最多32个独立工作项；每项重新领取数据库租约和额度。 */
    private int dispatchWorkItems=8;
    /** 仅限制启动后继项，当前HTTP仍由可取消总截止时间约束。 */
    private int dispatchMillis=500;
    /** 每业务类每轮最多查询4页；step是一次stageNext、bindNextLane或finish调用。 */
    private int expansionPages=4;
    private int expansionSteps=32;
    /** 每片最多4个step，轮换后可借后续片，仍受全局step和墙钟检查点限制。 */
    private int expansionBatchSteps=4;
    private int expansionMillis=500;
    private int recoveryPages=2;
    private long leaseSeconds=30;
    private List<Instance> instances=new ArrayList<>();

    @Data public static class Instance {
        private String id;
        private String accessType;
        private List<Integer> parks=new ArrayList<>();
        /** ISC 单次兼容请求的最大目标数；与在途目标保留位分开配置，不能超过 200。 */
        private int iscBatchTargetSize=20;
        private int httpPerSecond=100;
        private int deleteHttp=25;
        private int addHttp=25;
        private int configHttp=15;
        private int receiptHttp=25;
        private int borrowHttp=10;
        private int maxInflight=400;
        private int deleteInflight=100;
        private int addInflight=50;
        private int perDeviceInflight=25;
        private int perParkInflight=125;
        private int perParkDeleteInflight=25,perParkAddInflight=10;
        private int perDeviceDeleteInflight=5,perDeviceAddInflight=2;
        private int minParkInflight=10;
        private int parkHttpPerSecond=40;
        private int deviceHttpPerSecond=8;
    }

    public Instance resolve(Integer parkId,String accessType) {
        Instance found=null;
        for(Instance instance:instances) if(Objects.equals(accessType,instance.getAccessType()) && instance.getParks().contains(parkId)) {
            if(found!=null)throw new IllegalStateException("同一园区接入存在重复实例映射");
            found=instance;
        }
        if(found==null || found.getId()==null || found.getId().trim().isEmpty())throw new IllegalStateException("园区接入缺少明确实例映射");
        return found;
    }

    /** HTTP 领取使用接入专属分片；恢复和本地收敛始终沿用通用分页。 */
    public int dispatchPage(Instance instance,String lane) {
        if(instance==null || !Arrays.asList("ADD","DELETE","CONFIG","RECEIPT").contains(lane))return pageSize;
        return "ISC".equals(instance.getAccessType())?Math.min(pageSize,instance.getIscBatchTargetSize()):pageSize;
    }

    public void validate(AuthOperationProperties core) {
        if(!enabled)return;
        if(!core.isEnabled() || core.getEnabledParks().isEmpty())throw new IllegalStateException("调度启用前必须配置业务灰度园区");
        if(instances.isEmpty() || instances.size()>32 || queueCapacity<1 || queueCapacity>256 || pageSize<1 || pageSize>200
            || dispatchWorkItems<1 || dispatchWorkItems>32 || dispatchMillis<1 || dispatchMillis>1000
            || expansionSteps<1 || expansionSteps>200 || expansionBatchSteps<1 || expansionBatchSteps>expansionSteps || expansionMillis<1 || expansionMillis>1000 || expansionMillis>=leaseSeconds*1000
            || expansionPages<1 || expansionPages>20 || recoveryPages<1 || recoveryPages>20 || leaseSeconds<26 || leaseSeconds>300)
            throw new IllegalStateException("调度数量或分页参数越界");
        Set<String> ids=new HashSet<>();Set<Integer> mapped=new HashSet<>();
        if(maxDispatchParks<1 || maxDispatchParks>16 || instances.stream().mapToInt(i->i.getParks().size()).sum()>maxDispatchParks)
            throw new IllegalStateException("园区并发隔离槽上限为16，必须覆盖全部实例园区工作项");
        for(Instance i:instances) {
            if(i.getId()==null || !i.getId().matches("[A-Za-z0-9_.:-]{1,100}") || !ids.add(i.getId())
                || !Arrays.asList("ISC","DIRECT").contains(i.getAccessType()) || i.getParks().isEmpty() || i.getParks().size()>100
                || new HashSet<>(i.getParks()).size()!=i.getParks().size())throw new IllegalStateException("实例标识、接入或园区映射不合法");
            for(Integer p:i.getParks()) {
                if(p==null || p<=0 || !core.getEnabledParks().contains(p))throw new IllegalStateException("实例包含未启用园区");
                resolve(p,i.getAccessType());mapped.add(p);
            }
            if(i.getHttpPerSecond()<4 || i.getHttpPerSecond()>10000 || i.getDeleteHttp()<1 || i.getAddHttp()<1 || i.getConfigHttp()<1
                || i.getReceiptHttp()<1 || i.getBorrowHttp()<0 || i.getDeleteHttp()+i.getAddHttp()+i.getConfigHttp()+i.getReceiptHttp()+i.getBorrowHttp()>i.getHttpPerSecond()
                || i.getMaxInflight()<2 || i.getDeleteInflight()<1 || i.getAddInflight()<1 || i.getDeleteInflight()+i.getAddInflight()>i.getMaxInflight()
                || i.getPerDeviceInflight()<1 || i.getPerDeviceInflight()>i.getMaxInflight())throw new IllegalStateException("实例速率或在途保留额度不合法");
            if(i.getPerParkDeleteInflight()<1 || i.getPerParkAddInflight()<1 || i.getPerParkDeleteInflight()+i.getPerParkAddInflight()>i.getPerParkInflight()
            || i.getPerDeviceDeleteInflight()<1 || i.getPerDeviceAddInflight()<1 || i.getPerDeviceDeleteInflight()+i.getPerDeviceAddInflight()>i.getPerDeviceInflight()
            || i.getMinParkInflight()<1 || i.getPerParkInflight()<i.getMinParkInflight() || i.getPerParkInflight()>i.getMaxInflight()
                || (long)i.getMinParkInflight()*i.getParks().size()>i.getMaxInflight()-Math.max(i.getDeleteInflight(),i.getAddInflight())
                || Math.min(i.getAddHttp(),i.getDeleteHttp())<("ISC".equals(i.getAccessType())?3:1)*i.getParks().size()
                || Math.min(i.getConfigHttp(),i.getReceiptHttp())<i.getParks().size()
                || i.getParkHttpPerSecond()<8 || i.getParkHttpPerSecond()>i.getHttpPerSecond()
                || i.getDeviceHttpPerSecond()<8 || i.getDeviceHttpPerSecond()>i.getParkHttpPerSecond())throw new IllegalStateException("园区设备速率或园区最低服务额度不合法");
            if("ISC".equals(i.getAccessType())) {
                int deviceAvailable=i.getPerDeviceInflight()-Math.max(i.getPerDeviceDeleteInflight(),i.getPerDeviceAddInflight());
                int parkAvailable=i.getPerParkInflight()-Math.max(i.getPerParkDeleteInflight(),i.getPerParkAddInflight());
                int otherParkReserve=(i.getParks().size()-1)*i.getMinParkInflight();
                int instanceAvailable=i.getMaxInflight()-Math.max(i.getDeleteInflight(),i.getAddInflight())-otherParkReserve;
                if(i.getIscBatchTargetSize()<1 || i.getIscBatchTargetSize()>pageSize || i.getIscBatchTargetSize()>200
                    || i.getIscBatchTargetSize()>Math.min(deviceAvailable,Math.min(parkAvailable,instanceAvailable)))
                    throw new IllegalStateException("ISC请求分片必须落在设备、园区和实例保留在途额度以内");
            }
        }
        if(!mapped.equals(core.getEnabledParks()))throw new IllegalStateException("存在未映射的启用园区");
    }
}
