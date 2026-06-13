package com.tce.smart.bridge.isc.service;


import com.tce.smart.bridge.isc.api.dto.req.BridgeDTO;
import com.tce.smart.dispatcher.api.dto.resp.ISCResponse;
import com.tce.smart.dispatcher.api.enums.EventEnum;

import java.util.Map;

/**
 * @Description: TODO
 * @InterfaceName BridgeService
 * @Author jinbo
 * @Date 2019/11/6
 */
public interface BridgeISCService {
	<T> String dispatch(BridgeDTO<T> bridgeDTO);

	byte[] downISCImage(EventEnum eventEnum, String picUrl, String serverCode);

	ISCResponse post(EventEnum eventEnum, String data);
}
