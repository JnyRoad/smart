package com.tce.smart.platform.service.print;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
/** 调度由render-worker-enabled门禁控制，默认不查询待渲染表。 */
@Configuration @EnableScheduling public class PrintJobScheduling { }
