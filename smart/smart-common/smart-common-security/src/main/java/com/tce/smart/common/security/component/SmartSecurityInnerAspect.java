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

import javax.servlet.http.HttpServletRequest;

/**
 * 服务间接口不鉴权处理逻辑。
 * <p>
 * {@code @Inner} 端点的合法调用方是内部服务：from={@code Y} 头由 Feign 接口方法上
 * 显式声明的 {@code @RequestHeader(SecurityConstants.FROM)} 参数传入
 * （注意：SmartFeignClientInterceptor 只透传入站请求已有的头，并不会自动注入 from=Y，
 * 无 from 参数的 Feign 接口、以及定时任务/异步线程发起的调用不会携带该标识——
 * 这正是灰度期审计日志要清点的对象）。
 * 网关层 SmartRequestGlobalFilter 会剥离外部请求伪造的 from 头，
 * 本切面是服务被内网直连（绕过网关）时的第二道防线。
 * <p>
 * 已知局限：from 头是明文约定，蓄意攻击者可自行携带 from=Y 绕过本切面，
 * ENFORCE 只能拦住"未带标识的直连/误配调用"，不能替代网络隔离与更强的调用方认证。
 * <p>
 * 校验模式由 {@link SmartInnerSecurityProperties} 控制（Nacos 热更新）：
 * OFF 完全放行 / AUDIT 只记录不拦截（灰度观察期）/ ENFORCE 拦截。
 * 任何配置异常（空值、绑定失败）一律回退 AUDIT，保证配置手误不会打断生产流量。
 */
@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class SmartSecurityInnerAspect {

	/** 审计日志前缀：灰度期运维按此前缀检索"绕过 Feign 直达 @Inner"的调用方，属检索契约，勿改动 */
	private static final String LOG_PREFIX_AUDIT = "[inner-audit]";

	/** 拒绝日志前缀：ENFORCE 模式拦截记录的检索契约，勿改动 */
	private static final String LOG_PREFIX_DENY = "[inner-deny]";

	/** 日志中回显请求头的最大长度，防止超长伪造头撑爆日志 */
	private static final int LOG_HEADER_MAX_LENGTH = 32;

	private final SmartInnerSecurityProperties properties;

	@SneakyThrows
	@Around("@annotation(inner)")
	public Object around(ProceedingJoinPoint point, Inner inner) {
		// @Inner(false) 显式声明不走 AOP 校验
		if (!inner.value()) {
			return point.proceed();
		}

		// mode 只读一次成本地快照：避免 Nacos 刷新落在两次读取之间产生混合决策路径
		InnerMode mode = resolveMode();
		// OFF 模式等价历史注释态，作紧急回滚兜底
		if (mode == InnerMode.OFF) {
			return point.proceed();
		}

		// 携带内部调用标识（Feign 接口显式传入的 from=Y），正常放行
		HttpServletRequest request = resolveRequest();
		String from = request == null ? null : request.getHeader(SecurityConstants.FROM);
		if (SecurityConstants.FROM_IN.equals(from)) {
			return point.proceed();
		}

		String method = point.getSignature().toShortString();
		String caller = describeCaller(request);
		// 强制模式：拒绝非内部调用
		if (mode == InnerMode.ENFORCE) {
			log.warn("{} 拒绝非内部调用 method={}, from={}, {}",
					LOG_PREFIX_DENY, method, sanitizeForLog(from), caller);
			throw new AccessDeniedException("Access is denied");
		}

		// 审计模式（含 mode=null 等异常配置的兜底）：只记录不拦截，
		// 用于灰度期清点"绕过 Feign 直达 @Inner"的调用方
		log.warn("{} 缺少内部调用标识，审计模式放行 method={}, from={}, {}",
				LOG_PREFIX_AUDIT, method, sanitizeForLog(from), caller);
		return point.proceed();
	}

	/**
	 * 读取校验模式。@RefreshScope 场景下 Nacos 配错值会导致 bean 重建失败、getMode() 抛异常，
	 * 此处必须兜底回退 AUDIT：否则一次配置手误会让全部 @Inner 端点（含合法 Feign 调用）报 500。
	 * 属安全开关的可用性权衡：配置损坏时宁可降级为"记录不拦截"，也不能造成园区级内部接口中断。
	 */
	private InnerMode resolveMode() {
		try {
			InnerMode mode = properties.getMode();
			return mode == null ? InnerMode.AUDIT : mode;
		} catch (RuntimeException ex) {
			log.error("{} 读取 security.inner.mode 失败，回退审计模式", LOG_PREFIX_AUDIT, ex);
			return InnerMode.AUDIT;
		}
	}

	/**
	 * 取当前线程绑定的 HTTP 请求。
	 * 无请求上下文（异步线程/同进程内部误调）返回 null，视同缺少内部标识，
	 * 由调用处按模式决定放行还是拒绝，保证 AUDIT 模式零中断的承诺。
	 */
	private HttpServletRequest resolveRequest() {
		ServletRequestAttributes attributes =
				(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return attributes == null ? null : attributes.getRequest();
	}

	/**
	 * 描述调用方来源（直连 IP + 经代理的 X-Forwarded-For）。
	 * 审计期的全部价值在于定位"漏网调用是谁发起的"，只记方法名无法区分调用方。
	 */
	private String describeCaller(HttpServletRequest request) {
		if (request == null) {
			return "caller=无请求上下文";
		}
		return "callerIp=" + request.getRemoteAddr()
				+ ", xff=" + sanitizeForLog(request.getHeader("X-Forwarded-For"));
	}

	/**
	 * 清洗写入日志的请求头值：去掉换行防日志伪造（CRLF 注入），截断超长值。
	 * from / X-Forwarded-For 均为外部可控输入，直接拼日志会污染审计检索结果。
	 */
	private String sanitizeForLog(String value) {
		if (value == null) {
			return null;
		}
		String cleaned = value.replace("\r", "").replace("\n", "");
		return cleaned.length() <= LOG_HEADER_MAX_LENGTH
				? cleaned
				: cleaned.substring(0, LOG_HEADER_MAX_LENGTH) + "...";
	}

}
