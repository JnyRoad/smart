package com.tce.smart.bridge;

import cn.hutool.core.thread.ThreadUtil;
import com.tce.smart.bridge.netty.tcp.NettyClient;
import com.tce.smart.bridge.netty.utils.ConvertCodeUtils;
import com.tce.smart.bridge.netty.utils.NettyTcpClientUtils;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Objects;

@Slf4j
@EnableScheduling
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableAsync
@ComponentScan(basePackages = {"com.tce.smart"})
public class SmartBridgeConcentratorApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmartBridgeConcentratorApplication.class, args);
//		test();
	}

	private static void test(){

		String ip = "10.13.170.53";
		int port = 8200;

		ThreadUtil.execAsync(() -> {
			NettyClient.init(ip, ""+port);
//			NettyClient.init("127.0.0.1", "50000");
		});

		ThreadUtil.safeSleep(5000);

		String clientId = ip+":"+port;


		String searchMessage = "07050000FF00";
		byte[] searchMessageBytes = ConvertCodeUtils.hexString2Bytes(searchMessage);
		String searchMessageCrc = ConvertCodeUtils.getCRC(searchMessageBytes);
		String command = searchMessage + searchMessageCrc;

		log.info("外置阀门查询操作帧指令：{}", command);
		String searchRespFrame = NettyTcpClientUtils.sendSyncMessage(clientId, command);
		log.info("外置阀门查询响应帧返回：{}", searchRespFrame);
	}
}
