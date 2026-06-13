package com.tce.smart.data;

import com.tce.smart.common.security.annotation.EnableSmartFeignClients;
import com.tce.smart.common.security.annotation.EnableSmartResourceServer;
import com.tce.smart.common.swagger.annotation.EnableSmartSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.SpringCloudApplication;

@EnableSmartSwagger2
@ConditionalOnProperty(name = "swagger.enabled", matchIfMissing = true)
@SpringCloudApplication
@EnableSmartFeignClients
@EnableSmartResourceServer
public class SmartDataApplication {
	public static void main(String[] args) {
//		System.setProperty("http.proxyHost", "127.0.0.1");
//		System.setProperty("https.proxyHost", "127.0.0.1");
//		System.setProperty("http.proxyPort", "8888");
//		System.setProperty("https.proxyPort", "8888");

		SpringApplication.run(SmartDataApplication.class, args);
	}
}
