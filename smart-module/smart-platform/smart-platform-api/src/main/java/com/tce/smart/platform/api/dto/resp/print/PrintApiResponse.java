package com.tce.smart.platform.api.dto.resp.print;

import com.tce.smart.common.core.model.Result;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;

/** 保留平台统一响应码，同时提供打印契约的结构化错误与追踪标识。 */
@Getter
@Setter
public class PrintApiResponse<T> extends Result<T> {
    private String requestId;
    private Map<String, Object> error;
    private boolean replayed;
    public PrintApiResponse(T data, String requestId) { super(data); this.requestId = requestId; }
}
