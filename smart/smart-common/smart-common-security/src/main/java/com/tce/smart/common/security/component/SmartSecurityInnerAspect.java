package com.tce.smart.common.security.component;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 服务间接口不鉴权处理逻辑。
 * <p>
 * {@code @Inner} 端点的合法调用方是内部 Feign（自动携带 {@code from=Y}），
 * 网关层 SmartRequestGlobalFilter 会剥离外部请求伪造的 from 头，
 * 本切面是服务被内网直连（绕过网关）时的第二道防线。
 * <p>
 * 校验模式由 {@link SmartInnerSecurityProperties} 控制（Nacos 热更新）：
 * OFF 完全放行 / AUDIT 只记录不拦截（灰度观察期）/ ENFORCE 拦截。
 */
@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class SmartSecurityInnerAspect {
	private final SmartInnerSecurityProperties properties;

	@SneakyThrows
	@Around("@annotation(inner)")
	public Object around(ProceedingJoinPoint point, Inner inner) {
		// @Inner(false) 显式声明不走 AOP 校验；OFF 模式等价历史注释态，作紧急回滚兜底
		if (!inner.value() || properties.getMode() == InnerMode.OFF) {
			return point.proceed();
		}

		// 携带内部调用标识（Feign 拦截器注入的 from=Y），正常放行
		String from = resolveFromHeader();
		if (SecurityConstants.FROM_IN.equals(from)) {
			return point.proceed();
		}

		String method = point.getSignature().toShortString();
		// 审计模式：只记录不拦截，用于灰度期清点"绕过 Feign 直达 @Inner"的调用方
		if (properties.getMode() == InnerMode.AUDIT) {
			log.warn("[inner-audit] 缺少内部调用标识，审计模式放行 method={}, from={}", method, from);
			return point.proceed();
		}

		// 强制模式：拒绝非内部调用
		log.warn("[inner-deny] 拒绝非内部调用 method={}, from={}", method, from);
		throw new AccessDeniedException("Access is denied");
	}

	/**
	 * 从当前请求上下文取 from 头。
	 * 无请求上下文（异步线程/内部误调）返回 null，视同缺少内部标识，
	 * 由调用处按模式决定放行还是拒绝，保证 AUDIT 模式零中断的承诺。
	 */
	private String resolveFromHeader() {
		ServletRequestAttributes attributes =
				(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes == null) {
			return null;
		}
		return attributes.getRequest().getHeader(SecurityConstants.FROM);
	}

}
