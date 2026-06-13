package com.tce.smart.common.security.component;

import com.tce.smart.common.security.annotation.Inner;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 服务间接口不鉴权处理逻辑
 */
@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class SmartSecurityInnerAspect {
    private final HttpServletRequest request;

    @SneakyThrows
    @Around("@annotation(inner)")
    public Object around(ProceedingJoinPoint point, Inner inner) {
//        String header = request.getHeader(SecurityConstants.FROM);
//        if (inner.value() && !StringUtils.equals(SecurityConstants.FROM_IN, header)) {
//            log.warn("访问接口 {} 没有权限", point.getSignature().getName());
//            throw new AccessDeniedException("Access is denied");
//        }
        return point.proceed();
    }

}
