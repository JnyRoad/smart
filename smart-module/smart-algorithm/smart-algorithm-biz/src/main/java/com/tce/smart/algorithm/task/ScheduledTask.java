package com.tce.smart.algorithm.task;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @ClassName: ScheduledTask
 * @Package com.tce.smart.yunan.lock.task
 * @Description:
 * @Author wuxinjian
 * @Date 2019-08-08 16:57
 * @Version V1.0
 */
@Slf4j
@Component
@EnableScheduling
public class ScheduledTask {


    /**
     * 定时任务
     */
    @SchedulerLock(name = "定时任务1")
    @Scheduled(cron = "0 0 0 * * ? ")
    public void task1() {
        System.out.println(System.currentTimeMillis());
    }

}
