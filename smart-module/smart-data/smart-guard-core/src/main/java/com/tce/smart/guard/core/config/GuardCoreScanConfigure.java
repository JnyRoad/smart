package com.tce.smart.guard.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = {"com.tce.smart.guard.core"})
@MapperScan("com.tce.smart.guard.core.mapper")
public class GuardCoreScanConfigure {
}
