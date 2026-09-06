package com.tce.smart.platform.client.supplier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** 供应商通行接口继承现有认证链；不注册匿名入口、不记录请求正文。 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@ConditionalOnProperty(prefix = "smart.client.supplier", name = "enabled", havingValue = "true")
public class SupplierAccessController {
    private final SupplierAccessService service;
    public SupplierAccessController(SupplierAccessService service) { this.service = service; }

    @PostMapping("/visitor-checks")
    public Map<String, Object> verify(@RequestBody SupplierAccessRequests.Verification body) {
        return safe(() -> service.verify(body.credentialCode, body.postId));
    }
    @PostMapping("/visitor-passes")
    public Map<String, Object> record(@RequestBody SupplierAccessRequests.Event body,
            @RequestHeader(value = "Idempotency-Key", required = false) String key) {
        return safe(() -> service.record(body.verificationId, body.postId, body.direction, key));
    }
    @GetMapping("/visitor-passes")
    public List<Map<String, Object>> events() { return safe(() -> service.listEvents()); }

    /** 在MVC异常日志看到异常前移除来源message/cause，状态映射仍由同一局部策略决定。 */
    private <T> T safe(java.util.function.Supplier<T> operation) {
        try { return operation.get(); }
        catch (Exception failure) {
            int status = SupplierAccessExceptionHandler.statusOf(failure);
            if (status >= 500) log.error("供应商通行操作失败，类型={}", failure.getClass().getName());
            throw new SupplierAccessHttpException(status);
        }
    }
}
