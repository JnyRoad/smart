package com.tce.smart.bridge.service;

import com.tce.smart.bridge.api.dto.req.BridgeDTO;

/**
 * @Description: TODO
 * @InterfaceName BridgeService
 * @Author jinbo
 * @Date 2019/11/6
 */
public interface BridgeService {
	<T> Object dispatch(BridgeDTO<T> bridgeDTO);
}
