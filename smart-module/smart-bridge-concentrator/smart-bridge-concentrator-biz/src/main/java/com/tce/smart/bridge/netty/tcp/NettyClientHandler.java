package com.tce.smart.bridge.netty.tcp;

import com.tce.smart.bridge.netty.utils.ConvertCodeUtils;
import com.tce.smart.bridge.netty.utils.NettyTcpClientUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author Li.JiaJun
 * @since 2021/12/20 13:53
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("客户端连接异常:{}", cause.getMessage());
		ctx.fireExceptionCaught(cause);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
		if (msg instanceof ByteBuf) {
			ByteBuf byteBuf = (ByteBuf) msg;
			//创建目标大小的数组
			byte[] bytes = new byte[byteBuf.readableBytes()];
			//把数据从bytebuf转移到byte[]
			byteBuf.getBytes(0, bytes);
			String value = ConvertCodeUtils.bytes2HexString(bytes);
			InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
			String clientIp = socketAddress.getAddress().getHostAddress();
			int port = socketAddress.getPort();

			log.info("服务器端{}返回的数据: {}", clientIp,value);

			String clientId = clientIp + ":" + port;
			NettyTcpClientUtils.receive(clientId, value);
		}
	}

	/**
	 * 从服务端收到新的数据、读取完成时调用
	 *
	 * @param ctx
	 */
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		if (log.isDebugEnabled()) {
			log.debug("client channel read complete");
		}
		ctx.flush();
	}

	/**
	 * 客户端与服务端第一次建立连接时 执行
	 *
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		ctx.channel().read();
		NettyTcpClientUtils.setChannelHandlerContext(ctx);
		InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientIp = socketAddress.getAddress().getHostAddress();
		int port = socketAddress.getPort();
		String clientId = clientIp + ":" + port;
		NettyTcpClientUtils.addClient(clientId, ctx);
		//此处不能使用ctx.close()，否则客户端始终无法与服务端建立连接
		log.info("创建连接: ip={}, channelId={}", clientIp, ctx.channel().id().asLongText());
	}

	/**
	 * 客户端与服务端 断连时 执行
	 *
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientIp = socketAddress.getAddress().getHostAddress();
		int port = socketAddress.getPort();
		String clientId = clientIp + ":" + port;
		NettyTcpClientUtils.removeClient(clientId, ctx);
		//断开连接时，必须关闭，否则造成资源浪费，并发量很大情况下可能造成宕机
		ctx.close();
		log.error("连接已断开: ip={}，port={}", clientIp, port);
	}

	/**
	 * 服务端当read超时, 会调用这个方法
	 *
	 * @param ctx
	 * @param evt
	 * @throws Exception
	 */
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		log.error("连接超时");
	}
}
