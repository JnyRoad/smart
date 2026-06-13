package com.tce.smart.admin;


import com.tce.smart.common.security.annotation.EnableSmartFeignClients;
import com.tce.smart.common.security.annotation.EnableSmartResourceServer;
import com.tce.smart.common.swagger.annotation.EnableSmartSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户统一管理系统
 */
@EnableSmartSwagger2
@SpringCloudApplication
@EnableSmartFeignClients
@EnableSmartResourceServer
public class SmartAdminApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmartAdminApplication.class, args);
	}

}
