package com.tce.smart.bridge.netty;


import com.tce.smart.bridge.netty.utils.NettyServerUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 客户端响应监听
 * @author Li.JiaJun
 * @since 2021/12/15 15:53
 */
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

	@Setter
	@Getter
	private ServerHandler serverHandler;

	NettyServerHandler(ServerHandler serverHandler){
		this.serverHandler = serverHandler;
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("异常:{}",cause.getMessage());
        ctx.fireExceptionCaught(cause);
    }

    /**
     * 从客户端收到新的数据时，这个方法会在收到消息时被调用
     *
     * @param ctx
     * @param message
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        if(log.isDebugEnabled()){
            log.debug("server receive message: {}", message.toString());
        }
		serverHandler.handle(ctx, (ByteBuf)message);
    }

    /**
     * 从客户端收到新的数据、读取完成时调用
     *
     * @param ctx
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        if(log.isDebugEnabled()) {
            log.debug("channel read complete");
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
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = socketAddress.getAddress().getHostAddress();
        int port = socketAddress.getPort();
        //此处不能使用ctx.close()，否则客户端始终无法与服务端建立连接
        log.info("Netty服务器->客户端创建连接: ip={}, port={}, channelId={}", clientIp, port, ctx.channel().id().asLongText());
		NettyServerUtils.addChannel(ctx);
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
        //断开连接时，必须关闭，否则造成资源浪费，并发量很大情况下可能造成宕机
        ctx.close();
        log.error("Netty服务器->客户端连接已断开: ip={}，port={}", clientIp, port);
		NettyServerUtils.removeChannel(ctx);
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
		log.error("Netty服务器->客户端连接超时");
    }
}
