package com.tce.smart.bridge.isc.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.tce.smart.bridge.isc.core")
@MapperScan("com.tce.smart.bridge.isc.core.mapper")
public class BridgeAutoConfiguration  {
}