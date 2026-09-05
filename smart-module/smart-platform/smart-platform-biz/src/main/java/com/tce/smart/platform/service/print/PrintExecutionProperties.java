package com.tce.smart.platform.service.print;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/** 执行与异步处理分别显式启用，关闭执行不阻断已占用任务恢复。 */
@Data @Component @ConfigurationProperties(prefix="smart.print.execution")
public class PrintExecutionProperties { private boolean enabled; private boolean renderWorkerEnabled; }
