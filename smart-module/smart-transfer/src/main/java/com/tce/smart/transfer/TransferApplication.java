package com.tce.smart.transfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.hadoop.hbase.HbaseTemplate;

@SpringBootConfiguration
@SpringCloudApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class TransferApplication {
	public static void main(String[] args) {
		SpringApplication.run(TransferApplication.class, args);
	}
}
