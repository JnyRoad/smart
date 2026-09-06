package com.tce.smart.platform.service.print;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/** 执行与异步处理分别显式启用，关闭执行不阻断已占用任务恢复。 */
@Data @Component @ConfigurationProperties(prefix="smart.print.execution")
public class PrintExecutionProperties {
 private boolean enabled;
 private boolean renderWorkerEnabled;
 private int renderLeaseSeconds=120;
 /** 渲染服务默认读超时为30秒；小于60秒按下限处理，避免健康请求误回收正在生成的文件。 */
 public int renderLeaseSeconds(){return Math.max(60,renderLeaseSeconds);}
}
