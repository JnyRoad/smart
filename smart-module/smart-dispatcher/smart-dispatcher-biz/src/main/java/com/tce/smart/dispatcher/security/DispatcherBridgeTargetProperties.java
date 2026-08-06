package com.tce.smart.dispatcher.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * Dispatcher 动态 Bridge 服务令牌目标白名单。
 *
 * Nacos 配置变更后由 RefreshScope 重新绑定；调用方在每轮同步开始时读取当前值，
 * 使撤销某个目标地址能够在下一次成功同步时移除旧客户端。
 */
@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "security.dispatcher")
public class DispatcherBridgeTargetProperties {

	/** 未配置白名单时按拒绝全部动态目标处理。 */
	private String bridgeTargetAllowlist = "";
}
