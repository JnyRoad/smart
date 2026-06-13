package com.tce.smart.common.gateway.vo
	;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.io.Serializable;

/**
 * 扩展此类支持序列化a
 * See RouteDefinition.class
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RouteDefinitionVO extends RouteDefinition implements Serializable {
	/**
	 * 路由名称
	 */
	private String routeName;
}
