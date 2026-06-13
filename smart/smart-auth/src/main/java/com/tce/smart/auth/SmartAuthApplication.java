

package com.tce.smart.auth;


import com.tce.smart.common.security.annotation.EnableSmartFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * 认证授权中心
 */
@SpringCloudApplication
@EnableSmartFeignClients
public class SmartAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartAuthApplication.class, args);
	}
}
