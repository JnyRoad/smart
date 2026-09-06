package com.tce.smart.platform.controller.print;

import com.tce.smart.platform.api.dto.resp.print.PrintApiResponse;
import com.tce.smart.platform.service.print.PrintApiException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/** 错误只包含可公开的领域说明，不返回 SQL、文件路径和渲染令牌。 */
@RestControllerAdvice(basePackages = "com.tce.smart.platform.controller.print")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PrintApiAdvice {
    @ExceptionHandler(PrintApiException.class) public ResponseEntity<?> domain(PrintApiException error, HttpServletRequest request) { return error(error, PrintTemplateController.requestId(request)); }
    @ExceptionHandler({HttpMessageNotReadableException.class, ServletRequestBindingException.class, MethodArgumentTypeMismatchException.class}) public ResponseEntity<?> malformed(Exception failure, HttpServletRequest request) { return domain(new PrintApiException(422, "TEMPLATE_VALIDATION_FAILED", "请求字段或请求头无效"), request); }
    @ExceptionHandler(DataAccessException.class) public ResponseEntity<?> storage(DataAccessException failure, HttpServletRequest request) { return domain(new PrintApiException(503, "PRINT_STORAGE_UNAVAILABLE", "打印持久化尚未就绪"), request); }
    public static ResponseEntity<PrintApiResponse<Object>> error(PrintApiException failure, String requestId) {
        PrintApiResponse<Object> response = new PrintApiResponse<>(null, requestId); response.setCode(failure.getStatus()); response.setMsg(failure.getMessage());
        Map<String, Object> detail = new LinkedHashMap<>(); detail.put("code", failure.getCode()); detail.put("message", failure.getMessage()); detail.put("details", failure.getDetails()); detail.put("retryable", false); response.setError(detail); return ResponseEntity.status(failure.getStatus()).header("X-Request-Id", requestId).body(response);
    }
}
