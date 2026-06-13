package com.tce.smart.platform.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.tce.smart.platform.core")
@MapperScan("com.tce.smart.platform.core.mapper")
public class PlatformCoreScanConfigure {
}
