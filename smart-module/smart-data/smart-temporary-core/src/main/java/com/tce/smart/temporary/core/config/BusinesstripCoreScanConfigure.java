package com.tce.smart.temporary.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = {"com.tce.smart.temporary.core"})
@MapperScan("com.tce.smart.temporary.core.mapper")
public class BusinesstripCoreScanConfigure {
}
