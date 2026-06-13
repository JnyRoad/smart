package com.tce.smart.bridge.isc;

import com.tce.smart.common.security.annotation.EnableSmartFeignClients;
import com.tce.smart.common.security.annotation.EnableSmartResourceServer;
import com.tce.smart.common.swagger.annotation.EnableSmartSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableSmartSwagger2
@SpringCloudApplication
@EnableSmartFeignClients
@EnableSmartResourceServer
public class SmartBridgeISCApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmartBridgeISCApplication.class, args);
	}
}
