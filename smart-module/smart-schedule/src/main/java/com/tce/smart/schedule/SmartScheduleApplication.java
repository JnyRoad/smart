package com.tce.smart.schedule;

import com.tce.smart.common.security.annotation.EnableSmartFeignClients;
import com.tce.smart.common.security.annotation.EnableSmartResourceServer;
import com.tce.smart.common.swagger.annotation.EnableSmartSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableSmartSwagger2
@ConditionalOnProperty(name = "swagger.enabled", matchIfMissing = true)
@SpringCloudApplication
@EnableSmartFeignClients
@EnableSmartResourceServer
@EnableCaching
@EnableAsync
public class SmartScheduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartScheduleApplication.class, args);
	}
}
