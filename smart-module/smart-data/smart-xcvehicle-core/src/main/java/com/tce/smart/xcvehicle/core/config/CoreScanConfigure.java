package com.tce.smart.xcvehicle.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = {"com.tce.smart.xcvehicle.core"})
@MapperScan("com.tce.smart.xcvehicle.core.mapper")
public class CoreScanConfigure {
}
