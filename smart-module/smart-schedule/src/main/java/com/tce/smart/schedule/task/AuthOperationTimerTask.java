package com.tce.smart.schedule.task;
import com.tce.smart.schedule.service.platform.impl.AuthOperationScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 新灰度队列独立触发，旧定时业务继续按原开关运行。 */
@Component
public class AuthOperationTimerTask {
    private final AuthOperationScheduler scheduler;
    public AuthOperationTimerTask(AuthOperationScheduler scheduler) {this.scheduler=scheduler;}
    @Scheduled(fixedDelayString="${smart.auth-scheduler.tick-ms:1000}")
    public void advance() {scheduler.tick();}
}
