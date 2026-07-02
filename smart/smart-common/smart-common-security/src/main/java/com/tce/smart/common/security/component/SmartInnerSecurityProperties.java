package com.tce.smart.common.security.component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * {@code @Inner} 内部接口校验配置。
 * <p>
 * 配置项 {@code security.inner.mode}，支持 Nacos 热更新（{@code @RefreshScope}），
 * 出问题时改配置即可秒级回滚，无需重新发版。
 */
@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "security.inner")
public class SmartInnerSecurityProperties {

	/**
	 * 校验模式：OFF 关闭 / AUDIT 只记录 / ENFORCE 拦截。
	 * 默认 AUDIT：漏配配置的服务落在零中断的观察模式，绝不默认拦截。
	 */
	private InnerMode mode = InnerMode.AUDIT;
}
