package com.tce.smart.bridge;

import com.tce.smart.common.security.annotation.EnableSmartFeignClients;
import com.tce.smart.common.security.annotation.EnableSmartResourceServer;
import com.tce.smart.common.swagger.annotation.EnableSmartSwagger2;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableSmartSwagger2
@SpringCloudApplication
@EnableSmartFeignClients
@EnableSmartResourceServer
public class SmartBridgeApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmartBridgeApplication.class, args);
	}
}
