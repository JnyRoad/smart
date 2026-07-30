package com.tce.smart.platform.conf;

import lombok.Data;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Gateway 到 Platform 的遗留门锁兼容配置。
 *
 * <p>该配置默认关闭且调用方列表为空；缺少密钥、来源网段或园区范围时，
 * 兼容过滤器必须拒绝请求，不能退化为匿名接口。</p>
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "security.legacy-door-lock")
public class LegacyDoorLockCompatibilityProperties {

	private boolean enabled;
	private String keyId;
	private String signatureKey;
	private long maxClockSkewSeconds = 5L;
	private long maxTtlSeconds = 30L;
	private List<Client> clients = new ArrayList<>();

	/** 单个遗留调用方可使用的精确来源主机（IPv4 /32 或 IPv6 /128）和园区范围。 */
	@Data
	public static class Client {
		private String id;
		private List<String> sourceCidrs = new ArrayList<>();
		private List<Integer> parkIds = new ArrayList<>();
	}
}
