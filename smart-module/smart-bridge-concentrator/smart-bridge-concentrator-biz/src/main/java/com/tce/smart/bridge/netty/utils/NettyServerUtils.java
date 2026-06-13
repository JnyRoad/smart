package com.tce.smart.bridge.netty.utils;


import cn.hutool.core.util.StrUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tce.smart.bridge.core.exception.SmartException;
import com.tce.smart.bridge.netty.dto.ConcentratorEventDTO;
import com.tce.smart.bridge.netty.message.AddressMessage;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Netty服务端工具类
 * @author Li.JiaJun
 * @since 2021/12/15 15:53
 */
@Slf4j
public class NettyServerUtils {

	private static final int TIMEOUT = 30;

	private static final TimeUnit UNIT = TimeUnit.SECONDS;

	/**
	 * 响应消息缓存
	 */
	private static final Cache<String, BlockingQueue<String>> MESSAGE_CACHE = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(1000, TimeUnit.SECONDS)
			.build();

	private static final Map<String, Object> CLIENT_LOCK_CACHE = new ConcurrentHashMap<>();

	private static Object getClientLock(String clientId) {
		return CLIENT_LOCK_CACHE.computeIfAbsent(clientId, key -> new Object());
	}


	/**
	 * 等待响应消息
	 *
	 * @param key 消息唯一标识
	 * @return message
	 */
	public static String get(String key) {
		return get(key, TIMEOUT, UNIT);
	}

	/**
	 * 等待响应消息
	 *
	 * @param key 消息唯一标识
	 * @return message
	 */
	public static String get(String key, long timeout, TimeUnit unit) {
		try {
			//设置超时时间
			String message = Objects.requireNonNull(MESSAGE_CACHE.getIfPresent(key)).poll(timeout, unit);
			//删除key
			MESSAGE_CACHE.invalidate(key);
			return message;
		} catch (Exception e) {
			log.error("获取数据异常: key={}", key, e);
		}
		return null;
	}

	/**
	 * 初始化响应消息的队列
	 *
	 * @param key 消息唯一标识
	 */
	public static void init(String key) {
		MESSAGE_CACHE.put(key, new LinkedBlockingQueue<>(1));
	}

	/**
	 * 设置响应消息
	 *
	 * @param key     消息唯一标识
	 * @param message 消息
	 */
	public static void receive(String key, String message) {
		if (contains(key)) {
			Objects.requireNonNull(MESSAGE_CACHE.getIfPresent(key)).add(message);
			return;
		}
		log.warn("消息Key[{}]不存在", key);
	}

	private static boolean contains(String key) {
		if (StrUtil.isEmpty(key)) {
			throw new SmartException("消息KEY为空");
		}
		return MESSAGE_CACHE.getIfPresent(key) != null;
	}

	/**
	 * 缓存 channelId - channel
	 */
	private static final Map<String, ChannelHandlerContext> CLIENT_CHANNEL_CACHE = new ConcurrentHashMap<>();

	/**
	 * 缓存 clientId - channelId
	 */
	private static final Map<String, String> CLIENT_CHANNEL_ID_CACHE = new ConcurrentHashMap<>();

	/**
	 * 缓存 channelId - clientId
	 */
	private static final Map<String, String> CHANNEL_ID_CLIENT_CACHE = new ConcurrentHashMap<>();

	/**
	 * 缓存 clientId - 集中器对应的报文格式
	 */
	private static final Map<String, AddressMessage> CLIENT_ID_MESSAGE_CACHE = new ConcurrentHashMap<>();

	public static void addChannel(ChannelHandlerContext channelHandlerContext) {
		log.info("channel_id新增：{}", channelHandlerContext.channel().id().asLongText());
		CLIENT_CHANNEL_CACHE.put(channelHandlerContext.channel().id().asLongText(), channelHandlerContext);
	}

	public static void removeChannel(ChannelHandlerContext channelHandlerContext) {
		String channelId = channelHandlerContext.channel().id().asLongText();
		log.info("channel_id删除：{}", channelId);
		CLIENT_CHANNEL_CACHE.remove(channelId);
		String clientId = CHANNEL_ID_CLIENT_CACHE.get(channelId);
		if (Objects.nonNull(clientId)) {
			if (removeClient(clientId, channelId)) {
				removeMessage(clientId);
			}
			CHANNEL_ID_CLIENT_CACHE.remove(channelId);
		}
	}

	public static void addClient(String clientId, ChannelHandlerContext channelHandlerContext) {
		CLIENT_CHANNEL_ID_CACHE.put(clientId, channelHandlerContext.channel().id().asLongText());
		CHANNEL_ID_CLIENT_CACHE.put(channelHandlerContext.channel().id().asLongText(), clientId);
	}

	public static void removeClient(String clientId) {
		CLIENT_CHANNEL_ID_CACHE.remove(clientId);
	}

	private static boolean removeClient(String clientId, String channelId) {
		if (CLIENT_CHANNEL_ID_CACHE.remove(clientId, channelId)) {
			return true;
		}
		return false;
	}

	/**
	 * 新增集中器客户端对应的报文数据
	 * @param clientId
	 * @param waterMessage
	 */
	public static void addMessage(String clientId, AddressMessage waterMessage) {
		log.info("集中器客户端：{}，内容：{}", clientId, waterMessage);
		CLIENT_ID_MESSAGE_CACHE.put(clientId, waterMessage);
	}

	/**
	 * 获取集中器客户端对应的报文数据
	 * @param clientId
	 * @return
	 */
	public static AddressMessage getMessage(String clientId) {
		log.info("clientId：{}，集中器客户端对应的报文：{}", clientId, CLIENT_ID_MESSAGE_CACHE);
		return CLIENT_ID_MESSAGE_CACHE.get(clientId);
	}

	/**
	 * 删除集中器客户端对应的报文数据
	 * @param clientId
	 */
	public static void removeMessage(String clientId) {
		CLIENT_ID_MESSAGE_CACHE.remove(clientId);
	}

	/**
	 * 用于检测集中器是否在线
	 * @param clientId
	 * @return
	 */
	public static boolean checkClient(String clientId) {
		String ctx = CLIENT_CHANNEL_ID_CACHE.get(clientId);
		if(StrUtil.isEmpty(ctx)){
			log.info("未查询到集中器：{}", clientId);
			return false;
		}
		log.info("查询到集中器：{}", clientId);
		return true;
	}

	/**
	 * 发送报文帧并返回响应结果
	 *
	 * @param async    是否异步消息：true:是；false:否
	 * @return
	 */
	public static String sendFrame(ConcentratorEventDTO eventDTO, boolean async) {
		Object clientLock = getClientLock(eventDTO.getClientId());
		synchronized (clientLock) {
			return sendFrameLocked(eventDTO, getClientContext(eventDTO.getClientId()), async);
		}
	}

	/**
	 * 发送消息并返回响应结果
	 *
	 * @param eventDTO
	 * @param channelHandlerContext
	 * @param async                 是否异步消息：true:是；false:否
	 * @return
	 */
	public static String sendFrame(ConcentratorEventDTO eventDTO, ChannelHandlerContext channelHandlerContext, boolean async) {
		Object clientLock = getClientLock(eventDTO.getClientId());
		synchronized (clientLock) {
			return sendFrameLocked(eventDTO, channelHandlerContext, async);
		}
	}

	private static ChannelHandlerContext getClientContext(String clientId) {
		if (CLIENT_CHANNEL_ID_CACHE.containsKey(clientId)) {
			String channelId = CLIENT_CHANNEL_ID_CACHE.get(clientId);
			if (CLIENT_CHANNEL_CACHE.containsKey(channelId)) {
				return CLIENT_CHANNEL_CACHE.get(channelId);
			}
		}
		log.error("客户端未连接，发送失败: clientId={}", clientId);
		throw new SmartException("客户端未连接，发送失败");
	}

	private static String sendFrameLocked(ConcentratorEventDTO eventDTO, ChannelHandlerContext channelHandlerContext, boolean async) {
		if (!channelHandlerContext.isRemoved()) {
			if (async) {
				channelHandlerContext.writeAndFlush(eventDTO.getMessageFrame());
				return null;
			}
			String key = eventDTO.getClientId() + eventDTO.getTp();
			NettyServerUtils.init(key);
			channelHandlerContext.writeAndFlush(eventDTO.getMessageFrame());
			log.info("请求帧：{}，请求IP：{}，响应帧key：{}，等待并获取服务端响应....", eventDTO.getMessageFrame(),
					eventDTO.getClientId(), key);
			//等待并获取服务端响应
			return NettyServerUtils.get(key);
		}
		log.error("客户端连接已断开，发送失败: clientId={}", eventDTO.getClientId());
		throw new SmartException("客户端连接已断开，发送失败");
	}
}
