package com.tce.smart.dispatcher.service;

import com.tce.smart.bridge.api.feign.RemoteBridgeService;

/**
 * @Description: TODO
 * @InterfaceName RemoteFeignService
 * @Author jinbo
 * @Date 2019/11/6
 */
public interface RemoteFeignService {
	RemoteBridgeService getBridge(Integer parkId);
}
