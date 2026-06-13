package com.tce.smart.platform.websocket;

import com.tce.smart.platform.service.news.SmtNewsTerminalService;
import com.tce.smart.platform.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author
 * @date 2022/3/03 14:52
 */
@Slf4j
@Component
@Lazy
@ServerEndpoint(value = "/websocket/{ip}")
public class WebSocketHandler {

    private static final Map<String, Session> SESSION_CACHE = new ConcurrentHashMap<>();

    private final SmtNewsTerminalService  smtTerminalService = SpringUtils.getBeanByType(SmtNewsTerminalService.class);

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(@PathParam("ip") String ip, Session session) {
        log.info("session连接, ip:{}, sessionId: {}", ip, session.getId());
        try {
            // sessionKey格式：token的MD5_园区ID
            SESSION_CACHE.put(ip, session);
            // 发送该终端资源
			smtTerminalService.getByTerminal(ip);
        } catch (Exception e) {
            log.error("session连接处理异常：", e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        log.info("session关闭: {}", session.getId());
        SESSION_CACHE.values().removeIf(e -> e.getId().equals(session.getId()));
        log.info("session数量: {}", SESSION_CACHE.size());
        try {
            session.close();
        } catch (IOException e) {
            log.info("session关闭异常: ", e);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("服务端收到客户端的消息: {}", message);
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("发送消息异常:", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误: ", error);
    }

    /**
     * 服务端发送消息给所有客户端
     */
    public static void sendMessageToAll(String message, String ip) {
        try {
            // 分IP推送
            for (String sessionKey : SESSION_CACHE.keySet()) {
                if (sessionKey.contains(String.valueOf(ip))) {
                    SESSION_CACHE.get(sessionKey).getBasicRemote().sendText(message);
                }
            }
        } catch (Exception e) {
            log.error("发送消息异常：", e);
        }
    }

    /**
     * 服务端发送消息给单个客户端
     */
    public static void sendMessageToOne(String message, Session session) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("发送消息异常：", e);
        }
    }
}
