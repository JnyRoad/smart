package com.tce.smart.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * DoorLock 遗留接口的 Gateway 兼容配置。
 *
 * 签名密钥只允许由部署环境注入；Nacos 仅保存调用方标识和网络范围，变更后由
 * RefreshScope 刷新。空名单或缺少密钥会被过滤器按关闭处理，避免误配置放开接口。
 */
@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "security.legacy-door-lock")
public class LegacyDoorLockCompatibilityProperties {

	/** 默认关闭，只有显式启用并且配置完整时才接受兼容请求。 */
	private boolean enabled;
	private String keyId;
	private String signatureKey;
	private int signatureTtlSeconds = 30;
	private List<Client> clients = new ArrayList<>();

	/** 单个遗留调用方及其允许的精确 TCP 来源主机（IPv4 /32 或 IPv6 /128）。 */
	@Data
	public static class Client {
		private String id;
		private List<String> sourceCidrs = new ArrayList<>();
	}
}
