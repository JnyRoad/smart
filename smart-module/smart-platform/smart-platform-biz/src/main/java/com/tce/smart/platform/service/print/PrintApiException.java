package com.tce.smart.platform.service.print;

import lombok.Getter;
import java.util.Collections;
import java.util.Map;

/** 打印领域可安全返回客户端的错误，避免泄露数据库和资源路径。 */
@Getter
public class PrintApiException extends RuntimeException {
    private final int status;
    private final String code;
    private final Map<String, Object> details;
    public PrintApiException(int status, String code, String message) { this(status, code, message, Collections.emptyMap()); }
    public PrintApiException(int status, String code, String message, Map<String, Object> details) {
        super(message); this.status = status; this.code = code; this.details = details;
    }
}
