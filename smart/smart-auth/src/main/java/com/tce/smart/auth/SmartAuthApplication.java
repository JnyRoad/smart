

package com.tce.smart.auth;


import com.tce.smart.common.security.annotation.EnableSmartFeignClients;
import com.tce.smart.common.security.component.SmartInnerSecurityProperties;
import com.tce.smart.common.security.component.SmartSecurityInnerAspect;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.Import;

/**
 * 认证授权中心
 */
@SpringCloudApplication
@EnableSmartFeignClients
// 本服务是授权服务器而非资源服务器，没有 @EnableSmartResourceServer 帮忙扫描注册安全组件；
// 需显式注册 @Inner 校验切面，否则本服务的 @Inner 端点（token 删除/分页查询）既不产生审计日志也不会被拦截
@Import({SmartInnerSecurityProperties.class, SmartSecurityInnerAspect.class})
public class SmartAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartAuthApplication.class, args);
	}
}
