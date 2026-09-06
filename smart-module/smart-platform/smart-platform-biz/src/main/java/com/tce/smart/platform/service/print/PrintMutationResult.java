package com.tce.smart.platform.service.print;

import lombok.Getter;
import java.util.Map;

/** 原响应数据与幂等重放标识分开返回，不改变业务快照。 */
@Getter
public class PrintMutationResult {
    private final Map<String, Object> data;
    private final boolean replayed;
    public PrintMutationResult(Map<String, Object> data, boolean replayed) { this.data = data; this.replayed = replayed; }
}
