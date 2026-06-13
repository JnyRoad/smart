package com.tce.smart.gateway;


import com.tce.smart.common.gateway.annotation.EnableSmartDynamicRoute;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * 网关应用
 */
@EnableSmartDynamicRoute
@SpringCloudApplication
public class SmartGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartGatewayApplication.class, args);
	}
}
