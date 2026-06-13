package com.tce.smart.businesstrip.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = {"com.tce.smart.businesstrip.core"})
@MapperScan("com.tce.smart.businesstrip.core.mapper")
public class BusinesstripCoreScanConfigure {
}
