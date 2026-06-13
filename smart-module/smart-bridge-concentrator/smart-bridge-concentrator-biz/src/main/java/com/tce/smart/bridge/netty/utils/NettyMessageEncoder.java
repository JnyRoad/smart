package com.tce.smart.bridge.netty.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author Li.JiaJun
 * @since 2021/12/20 9:30
 */
public class NettyMessageEncoder extends MessageToByteEncoder {

	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) {
		// 写入数据
		byteBuf.writeBytes(ConvertCodeUtils.hexString2Bytes(o.toString()));
	}
}
