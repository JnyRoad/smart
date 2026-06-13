package com.tce.smart.bridge.netty.tcp;

import com.tce.smart.bridge.netty.utils.NettyMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Li.JiaJun
 * @since 2021/12/20 13:46
 */
@Slf4j
public class NettyClient {

	/**
	 * 向阀门服务器发送消息
	 *
	 * @param ip
	 * @param port
	 * @return
	 */
	public static void init(String ip, String port) {
		// 首先，netty通过ServerBootstrap启动服务端
		Bootstrap client = new Bootstrap();

		//第1步 定义线程组，处理读写和链接事件，没有了accept事件
		EventLoopGroup group = new NioEventLoopGroup();
		client.group(group);

		//第2步 绑定客户端通道
		client.channel(NioSocketChannel.class);
		client.option(ChannelOption.TCP_NODELAY, true);

		//第3步 给NIoSocketChannel初始化handler， 处理读写事件
		client.handler(new ChannelInitializer<NioSocketChannel>() {
			@Override
			protected void initChannel(NioSocketChannel ch) {
				//找到他的管道 增加他的handler
				ch.pipeline().addLast(new NettyMessageEncoder());
				ch.pipeline().addLast(new NettyClientHandler());
			}
		});

		try {
			//连接服务器
			ChannelFuture future = client.connect(ip, Integer.parseInt(port)).sync();
			if (future.isSuccess()) {
				log.info("连接智能阀门控制箱服务器成功：[{}:{}]", ip, port);
			}
			//当通道关闭了，就继续往下走
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			log.error("连接失败：{}", e.getMessage(), e);
		} finally {
			group.shutdownGracefully();
		}
	}
}
