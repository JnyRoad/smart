package com.tce.smart.transfer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hadoop.hbase.HbaseTemplate;

import java.io.IOException;

@Slf4j
@Configuration
public class HBaseAutoConfiguration {
	private final String quorum = "hall-hadoop";
	private final String port = "2181";
	private final String znode = "/hbase";
	private final String maxSize = "10485760";

	@Bean
	public HbaseTemplate getHbaseHbaseTemplate(){
		org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.property.clientPort", port);
		conf.set("hbase.zookeeper.quorum", quorum);
		HbaseTemplate hbaseTemplate = new HbaseTemplate();

		hbaseTemplate.setConfiguration(conf);
		return hbaseTemplate;
	}
}