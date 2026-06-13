package com.tce.smart.ehrview.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = {"com.tce.smart.ehrview.core"})
@MapperScan("com.tce.smart.ehrview.core.mapper")
public class EHRViewCoreScanConfigure {
}
