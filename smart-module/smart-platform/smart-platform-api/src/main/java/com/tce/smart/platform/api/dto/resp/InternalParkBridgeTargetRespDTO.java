package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Dispatcher 创建动态 Bridge 客户端所需的最小园区目标。
 *
 * 该对象仅允许受控服务令牌读取，禁止复用包含园区地址、电话等字段的客户端园区响应。
 */
@Data
@ApiModel("内部Bridge目标响应")
public class InternalParkBridgeTargetRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("园区ID")
	private Integer id;

	@ApiModelProperty("受控服务调用的Bridge基础地址")
	private String bridgeUrl;
}
