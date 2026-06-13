package com.tce.smart.platform;

import com.tce.smart.common.security.annotation.EnableSmartFeignClients;
import com.tce.smart.common.security.annotation.EnableSmartResourceServer;
import com.tce.smart.common.swagger.annotation.EnableSmartSwagger2;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableSmartSwagger2
@SpringCloudApplication
@EnableSmartFeignClients
@EnableSmartResourceServer
@EnableCaching
@EnableAsync
@ComponentScan("com.tce.smart")
@MapperScan("com.tce.smart.platform.core.mapper")
public class SmartPlatformApplication {
	public static void main(String[] args) throws Exception{
		SpringApplication.run(SmartPlatformApplication.class, args);
	}
}
