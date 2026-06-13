package com.tce.smart.bridge.netty.utils;


import cn.hutool.core.util.StrUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyTcpClientUtils {
	private static final int TIMEOUT = 30000;

	@Setter
	@Getter
	private static ChannelHandlerContext channelHandlerContext;
	/**
	 * 响应消息缓存
	 */
	private static final Cache<String, BlockingQueue<String>> MESSAGE_CACHE = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(1000, TimeUnit.SECONDS)
			.build();

	private static final Map<String, Object> CLIENT_LOCK_CACHE = new ConcurrentHashMap<>();

	/**
	 * 等待响应消息
	 *
	 * @param key 消息唯一标识
	 * @return message
	 */
	public static String get(String key) {

		try {
			//设置超时时间
			String message = Objects.requireNonNull(MESSAGE_CACHE.getIfPresent(key)).poll(TIMEOUT, TimeUnit.MILLISECONDS);
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
			log.error("消息KEY为空");
			return false;
		}
		return MESSAGE_CACHE.getIfPresent(key) != null;
	}

    /**
     * 缓存 clientId - channel
     */
	public static final Map<String, ChannelHandlerContext> CLIENT_CHANNEL_CACHE = new ConcurrentHashMap<>();

    public static void addClient(String clientId, ChannelHandlerContext channelHandlerContext) {
        CLIENT_CHANNEL_CACHE.put(clientId, channelHandlerContext);
    }

	public static void removeClient(String clientId) {
	    CLIENT_CHANNEL_CACHE.remove(clientId);
	}

	public static void removeClient(String clientId, ChannelHandlerContext channelHandlerContext) {
	    CLIENT_CHANNEL_CACHE.remove(clientId, channelHandlerContext);
	}

    /**
     * 用于检测智能阀门控制箱服务器是否在线
     * @param clientId
     * @return
     */
    public static boolean checkClient(String clientId) {
        ChannelHandlerContext ctx = CLIENT_CHANNEL_CACHE.get(clientId);
        return Objects.nonNull(ctx);
    }

    /**
     * 关闭客户端连接
     * @param clientId
     * @return
     */
    public static void closeClient(String clientId) {
        log.info("关闭客户端连接：{}", clientId);
        ChannelHandlerContext ctx = CLIENT_CHANNEL_CACHE.get(clientId);
        if (Objects.nonNull(ctx)) {
            ctx.channel().close();
        }
    }

	/**
	 * 使用netty连接池发送消息并返回响应结果
	 *
	 * @param message  消息
	 * @return String 服务端响应内容
	 */
	public static String sendSyncMessage(String clientId, String message) {
		Object clientLock = CLIENT_LOCK_CACHE.computeIfAbsent(clientId, key -> new Object());
		synchronized (clientLock) {
		    ChannelHandlerContext ctx = CLIENT_CHANNEL_CACHE.get(clientId);
			if (Objects.isNull(ctx) || !ctx.channel().isActive()) {
				log.error("TCP服务端未连接或连接已断开，消息发送失败!");
				return null;
			}
			log.info("外置阀门clientId：{}，请求帧：{}", clientId, message);
			init(clientId);
			ctx.channel().writeAndFlush(message);
			//等待并获取服务端响应
			return NettyTcpClientUtils.get(clientId);
		}
	}
}
