package org.springframework.cloud.openfeign;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * fallback 代理处理
 */
@Slf4j
@AllArgsConstructor
public class SmartFeignFallback<T> implements MethodInterceptor {
	private final Class<T> targetType;
	private final String targetName;
	private final Throwable cause;

	@Nullable
	@Override
	public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
		Class<?> returnType = method.getReturnType();
		if (Result.class != returnType) {
			return null;
		}
		FeignException exception = (FeignException) cause;

		byte[] content = exception.content();

		String message = StrUtil.str(content, StandardCharsets.UTF_8);

		log.error("远程接口请求失败, method:[{}.{}], serviceId:[{}], message:[{}]", targetType.getName(), method.getName(), targetName, message);
		if (StringUtils.isEmpty(message)) {
			return Result.fail("接口请求失败");
		}
		return JSONUtil.toBean(message, Result.class);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SmartFeignFallback<?> that = (SmartFeignFallback<?>) o;
		return targetType.equals(that.targetType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(targetType);
	}
}
