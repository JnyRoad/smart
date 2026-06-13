package com.tce.smart.bridge.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
public class HBaseAutoConfiguration {

	@Value("${hbase.zookeeper.quorum}")
	private String quorum;

	@Value("${hbase.zookeeper.port}")
	private String port;

	@Value("${hbase.zookeeper.znode}")
	private String znode;
	/**
	 * 默认大小为10M，即 10485760
	 */
	@Value("${hbase.max-size:10485760}")
	private String maxSize;

	@Bean
	public org.apache.hadoop.conf.Configuration configuration() {
		return configure();
	}
//
//	@Bean
//	public HbaseTemplate hbaseTemplate() {
//		HbaseTemplate hbaseTemplate = new HbaseTemplate();
//		hbaseTemplate.setConfiguration(configure());
//		hbaseTemplate.setAutoFlush(true);
//		return hbaseTemplate;
//	}

	private org.apache.hadoop.conf.Configuration configure() {
		org.apache.hadoop.conf.Configuration configuration = HBaseConfiguration.create();
		configuration.set("hbase.zookeeper.quorum",quorum);
		configuration.set("hbase.zookeeper.property.clientPort",port);
		configuration.set("zookeeper.znode.parent",znode);
		configuration.set("hbase.client.keyvalue.maxsize", maxSize);
		return configuration;
	}
	@Bean
	public Connection connection(org.apache.hadoop.conf.Configuration configuration) {
		try {
			return ConnectionFactory.createConnection(configuration);
		} catch (IOException e) {
			log.error("create hbase connection error: {}", e.getMessage(), e);
		}
		return null;
	}
	@Bean
	public HBaseAdmin hbaseAdmin(org.apache.hadoop.conf.Configuration configuration) {
		try {
			return (HBaseAdmin) connection(configuration).getAdmin();
		} catch (IOException e) {
			log.error("create hbase admin error: {}", e.getMessage(), e);
		}
		return null;
	}
}