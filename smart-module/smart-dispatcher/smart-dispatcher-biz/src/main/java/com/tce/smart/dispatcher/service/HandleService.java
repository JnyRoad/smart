package com.tce.smart.dispatcher.service;

import com.tce.smart.dispatcher.api.dto.resp.BridgeDTO;

/**
 * @Description: TODO
 * @InterfaceName HandleService
 * @Author jinbo
 * @Date 2019/11/7
 */
public interface HandleService {
	boolean handle(BridgeDTO<String> bridgeDTO);
}
