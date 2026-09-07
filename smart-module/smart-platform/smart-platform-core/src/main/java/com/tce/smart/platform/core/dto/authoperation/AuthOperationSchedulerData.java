package com.tce.smart.platform.core.dto.authoperation;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

/** 调度短事务输入、持久状态与有界结果，不携带人物资料。 */
public final class AuthOperationSchedulerData {
    private AuthOperationSchedulerData() { }
    @Data public static class Policy {
        private String instanceId,accessType;
        private List<Integer> parks;
        private int httpPerSecond,deleteHttp,addHttp,configHttp,receiptHttp,borrowHttp;
        private int maxInflight,deleteInflight,addInflight,perDeviceInflight;
        private int perParkDeleteInflight=25,perParkAddInflight=10,perDeviceDeleteInflight=5,perDeviceAddInflight=2;
        private int perParkInflight=125,minParkInflight=10,parkHttpPerSecond=40,deviceHttpPerSecond=8;
        private String selectedDevice;
    }
    @Data public static class State {
        private String instanceId,windowKey,policyKey;
        private int totalUsed,deleteUsed,addUsed,configUsed,receiptUsed,borrowUsed;
    }
    @Data public static class Route {
        private Integer directTakeoverVersion;
        private Integer parkId;
        private String accessType,instanceId;
    }
    @Data public static class Job {
        private String instanceId,lane,leaseToken,deviceCursor,textCursor;
        private int parkIndex,failures;
        private Long numberCursor;
        private LocalDateTime leaseUntil,nextAttemptAt,lastAdvancedAt;
    }
    @Data public static class Candidate {
        private Long id;
        private Integer parkId;
        private String deviceId,operationQueue,fairKey;
    }
    @Data public static class Grant {
        private Policy policy;
        private Integer parkId;
        private String lane;
        private List<Long> phaseIds=new ArrayList<>();
        private String phaseOperation;
        private String windowKey;
        private Job job;
        private int httpBudget;
        private Map<Integer,List<AuthOperationClaimedTarget>> claims=new LinkedHashMap<>();
    }
    @Data public static class Count {
        private Integer parkId;
        private String priority,state;
        private int targetCount;
        private LocalDateTime oldestAt;
    }
    @Data public static class PhaseWork {
        private Long id;
        private String deviceId,operation,fairKey;
    }
    @Data public static class Snapshot {
        private String instanceId;
        private List<Count> queues;
        private List<Job> jobs;
    }
}
