package com.tce.smart.dispatcher;

import com.tce.smart.common.security.annotation.EnableSmartFeignClients;
import com.tce.smart.common.swagger.annotation.EnableSmartSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.context.request.RequestContextListener;

@EnableScheduling
@EnableSmartSwagger2
@ConditionalOnProperty(name = "swagger.enabled", matchIfMissing = true)
@SpringCloudApplication
@EnableSmartFeignClients
@EnableAutoConfiguration
@EnableCaching
@ComponentScan("com.tce.smart")
public class SmartDispatcherApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmartDispatcherApplication.class, args);
	}
}
