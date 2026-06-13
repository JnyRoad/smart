package com.tce.smart.bridge.netty;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Netty服务端 对接水电表
 * @author Li.JiaJun
 * @since 2021/12/15 15:53
 */
@Slf4j
@Service
public class NettyServerService implements CommandLineRunner {

	@Value("${smart.server.port:6001}")
	private int port;

	@Autowired
	private ServerHandler serverHandler;

	@Async
	@Override
	public void run(String... args) {
		NettyServer nettyServer = new NettyServer();
		nettyServer.setServerHandler(serverHandler);
		nettyServer.start(port);
	}
}
