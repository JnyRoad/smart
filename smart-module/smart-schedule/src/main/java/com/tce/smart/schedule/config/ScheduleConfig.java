package com.tce.smart.schedule.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.lang.reflect.Method;

/**
 * @author Li.JiaJun
 * @since 2022/8/10 16:14
 */
@Slf4j
@Configuration
public class ScheduleConfig implements SchedulingConfigurer {

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
		Method[] methods = BatchProperties.Job.class.getMethods();
		int defaultPoolSize = 20;
		int corePoolSize = 0;
		if (methods.length > 0) {
			for (Method method : methods) {
				Scheduled annotation = method.getAnnotation(Scheduled.class);
				if (annotation != null) {
					corePoolSize++;
				}
			}
			if (defaultPoolSize > corePoolSize) {
				corePoolSize = defaultPoolSize;
			}
		}
		log.info("核心线程数：{}", corePoolSize);
		taskScheduler.setPoolSize(corePoolSize);
		taskRegistrar.setScheduler(taskScheduler);
	}
}
