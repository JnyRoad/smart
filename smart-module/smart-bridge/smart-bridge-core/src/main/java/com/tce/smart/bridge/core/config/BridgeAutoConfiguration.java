package com.tce.smart.bridge.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.tce.smart.bridge.core")
@MapperScan("com.tce.smart.bridge.core.mapper")
public class BridgeAutoConfiguration  {
}