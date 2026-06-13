package com.tce.smart.common.gateway.support;

import org.springframework.context.ApplicationEvent;

/**
 * 路由初始化事件
 */
public class DynamicRouteInitEvent extends ApplicationEvent {
	public DynamicRouteInitEvent(Object source) {
		super(source);
	}
}
