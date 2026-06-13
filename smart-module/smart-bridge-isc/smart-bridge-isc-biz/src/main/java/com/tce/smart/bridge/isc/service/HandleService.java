package com.tce.smart.bridge.isc.service;

/**
 * @author sunfujian
 * @date 2021/9/1 11:19
 */
public interface HandleService {

	boolean eventHandle(String eventData);

	boolean callbackHandle(String callbackData);
}
