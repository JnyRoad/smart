package com.tce.smart.platform.core.service.impl;

/** 在持久实例行锁内计算额度；本类不保存进程内令牌。 */
public final class AuthOperationSchedulerQuota {
    private AuthOperationSchedulerQuota() { }
    public static int http(int total,int totalUsed,int reserved,int laneUsed,int shared,int sharedUsed,int borrowCap) {
        int own=Math.max(0,reserved-laneUsed);
        int borrow=Math.min(Math.max(0,shared-sharedUsed),Math.max(0,borrowCap-Math.max(0,laneUsed-reserved)));
        return Math.max(0,Math.min(total-totalUsed,own+borrow));
    }
    public static int inflight(int maximum,int totalUsed,int oppositeReserve,int sameUsed,int page) {
        return Math.max(0,Math.min(page,Math.min(maximum-totalUsed,maximum-oppositeReserve-sameUsed)));
    }
    public static long backoffMillis(int failures,int jitter) {
        return Math.min(300000L,1000L*(1L<<Math.min(19,Math.max(0,failures-1)))+Math.min(999,Math.max(0,jitter)));
    }
}
