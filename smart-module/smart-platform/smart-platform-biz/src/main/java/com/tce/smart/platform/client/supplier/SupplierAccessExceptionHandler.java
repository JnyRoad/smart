package com.tce.smart.platform.client.supplier;

import com.tce.smart.platform.client.identity.ClientApiException;
import com.tce.smart.platform.core.client.supplier.SupplierPersistenceException;
import com.tce.smart.platform.core.client.supplier.SupplierRuleViolation;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Collections;
import java.util.Map;

/** 抢先于既有全局处理器，固定错误响应不回显异常、SQL、扫码或人员资料。 */
@RestControllerAdvice(assignableTypes = SupplierAccessController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SupplierAccessExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handle(Exception failure) {
        int status = statusOf(failure);
        return ResponseEntity.status(status).body(Collections.singletonMap("message", message(status)));
    }
    static int statusOf(Exception failure) {
        int status = 503;
        if (failure instanceof SupplierAccessHttpException) status = ((SupplierAccessHttpException) failure).getStatus();
        else if (failure instanceof ClientApiException) status = ((ClientApiException) failure).getStatus();
        else if (failure instanceof HttpMessageNotReadableException) status = 400;
        else if (failure instanceof SupplierRuleViolation) {
            switch (((SupplierRuleViolation) failure).getCode()) {
                case INVALID_INPUT: status = 400; break;
                case MISSING_PERMISSION: case UNAUTHORIZED_POST: case INACTIVE_QUALIFICATION:
                case ADMISSION_NOT_APPROVED: case QUALIFICATION_NOT_YET_VALID: case QUALIFICATION_EXPIRED:
                case AREA_NOT_AUTHORIZED: status = 403; break;
                default: status = 409;
            }
        } else if (failure instanceof SupplierPersistenceException) {
            switch (((SupplierPersistenceException) failure).getCode()) {
                case INVALID_INPUT: status = 400; break;
                case VERIFICATION_NOT_FOUND: status = 404; break;
                case VERIFICATION_CONSUMED: case IDEMPOTENCY_CONFLICT: case CONCURRENT_MODIFICATION: status = 409; break;
                default: status = 503;
            }
        }
        return status;
    }
    private String message(int status) {
        switch (status) {
            case 400: return "请求格式无效";
            case 401: return "请重新认证";
            case 403: return "当前操作未获授权或资格已失效";
            case 404: return "未找到有效记录";
            case 409: return "通行状态已变化，请刷新后重试";
            default: return "供应商通行服务暂不可用";
        }
    }
}
