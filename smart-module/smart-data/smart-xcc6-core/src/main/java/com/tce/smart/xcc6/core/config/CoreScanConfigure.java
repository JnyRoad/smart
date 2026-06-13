package com.tce.smart.xcc6.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = {"com.tce.smart.xcc6.core"})
@MapperScan("com.tce.smart.xcc6.core.mapper")
public class CoreScanConfigure {
}
