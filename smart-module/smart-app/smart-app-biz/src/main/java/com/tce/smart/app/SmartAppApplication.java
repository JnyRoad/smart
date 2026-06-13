package com.tce.smart.app;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

import com.tce.smart.common.security.annotation.EnableSmartFeignClients;
import com.tce.smart.common.security.annotation.EnableSmartResourceServer;
import com.tce.smart.common.swagger.annotation.EnableSmartSwagger2;

@EnableSmartSwagger2
@SpringCloudApplication
@EnableSmartFeignClients
@EnableSmartResourceServer
public class SmartAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmartAppApplication.class, args);
	}
}
