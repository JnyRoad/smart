package com.tce.smart.platform.client.identity;

import com.tce.smart.platform.client.release.ReleaseAccessProperties;
import com.tce.smart.platform.client.supplier.SupplierAccessProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** 让身份端点在单项业务尚未启用时仍能安全读取空的岗位配置。 */
@Configuration
@EnableConfigurationProperties({ReleaseAccessProperties.class, SupplierAccessProperties.class})
public class ClientIdentityConfiguration { }
