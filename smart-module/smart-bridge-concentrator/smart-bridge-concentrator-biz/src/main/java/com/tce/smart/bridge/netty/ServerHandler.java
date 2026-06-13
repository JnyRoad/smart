package com.tce.smart.bridge.netty;

import com.tce.smart.bridge.netty.enums.ControlCodeEnum;
import com.tce.smart.bridge.netty.enums.EleFunctionCodeEnum;
import com.tce.smart.bridge.netty.enums.FunctionCodeEnum;
import com.tce.smart.bridge.netty.enums.WaterFunctionCodeEnum;
import com.tce.smart.bridge.netty.message.AddressMessage;
import com.tce.smart.bridge.netty.utils.ConvertCodeUtils;
import com.tce.smart.bridge.netty.utils.NettyServerUtils;
import com.tce.smart.bridge.netty.utils.SendMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;


/**
 * @author Li.JiaJun
 * @since 2021/12/16 9:01
 */
@Slf4j
@Service
public class ServerHandler {

	public void handle(ChannelHandlerContext ctx, ByteBuf byteBuf) {
		try {
			//创建目标大小的数组
			byte[] bytes = new byte[byteBuf.readableBytes()];
			//把数据从bytebuf转移到byte[]
			byteBuf.getBytes(0, bytes);
			InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
			String clientIp = socketAddress.getAddress().getHostAddress();
			int port = socketAddress.getPort();

			// 1、判断是否注册或者心跳报文
			String controlCode = ConvertCodeUtils.byte2HexString(bytes[6]);
			String functionCode = ConvertCodeUtils.byte2HexString(bytes[12]);
			// 水电表注册|心跳
			if (ControlCodeEnum.REGISTER_HEARTBEAT_RECEIVE.getCode().equals(controlCode)) {
				if (FunctionCodeEnum.REGISTER_HEARTBEAT_RECEIVE.getCode().equals(functionCode)) {
					AddressMessage waterEleMessage = new AddressMessage();
					waterEleMessage.setCountyCode(ConvertCodeUtils.byte2HexString(bytes[7]) + ConvertCodeUtils.byte2HexString(bytes[8]));
					waterEleMessage.setAddress(ConvertCodeUtils.byte2HexString(bytes[9]) + ConvertCodeUtils.byte2HexString(bytes[10]));
					waterEleMessage.setStandbyAddress(ConvertCodeUtils.byte2HexString(bytes[11]));
					// 注册|心跳
					NettyServerUtils.addClient(clientIp, ctx);
					NettyServerUtils.addMessage(clientIp, waterEleMessage);
					Object resp = SendMessageUtils.getRegisterHeartBeat(waterEleMessage);
					log.info("注册|心跳IP地址：{}，客户端端口号：{}，功能码：{}，请求帧：{}, 响应帧：{}", clientIp, port,
							functionCode, ConvertCodeUtils.bytes2HexString(bytes), resp);
					// 响应
					ctx.writeAndFlush(resp);
				}
			// 读取当前读数水电表集中器响应
			} else if (FunctionCodeEnum.DOWNLOAD_FILE_RESPONSE.getCode().equals(functionCode)
					|| EleFunctionCodeEnum.QUERY_REQUEST_RESPONSE.getCode().equals(functionCode)
					|| EleFunctionCodeEnum.QUERY_DAY_REQUEST_RESPONSE.getCode().equals(functionCode)
					|| EleFunctionCodeEnum.QUERY_FILE_RESPONSE.getCode().equals(functionCode)
					|| EleFunctionCodeEnum.BRAKE_STATE_QUERY.getCode().equals(functionCode)
					|| WaterFunctionCodeEnum.QUERY_REQUEST_RESPONSE.getCode().equals(functionCode)
					|| WaterFunctionCodeEnum.QUERY_FILE_RESPONSE.getCode().equals(functionCode)
					|| WaterFunctionCodeEnum.WATER_DAY_READING_REQUEST.getCode().equals(functionCode)
			) {
				String message = ConvertCodeUtils.bytes2HexString(bytes);
				String tp = message.substring(message.length() - 16, message.length() - 4);
				String key = clientIp + tp;
				NettyServerUtils.receive(key, message);
			}
			if (!FunctionCodeEnum.REGISTER_HEARTBEAT_RECEIVE.getCode().equals(functionCode)) {
                String message = ConvertCodeUtils.bytes2HexString(bytes);
                String tp = message.substring(message.length() - 16, message.length() - 4);
                String key = clientIp + tp;
				log.info("非注册|心跳IP地址：{}，唯一识别响应帧key：{}，接收到的报文：{}", clientIp, key, message);
			}
			byteBuf.clear();
		} catch (Exception e) {
			log.error("读取水电表集中器请求帧异常", e);
		}
	}
}
