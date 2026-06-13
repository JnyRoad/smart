package com.tce.smart.platform.api.dto.req.commonconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约配置表
 *
 * @author fushiping
 * @date 2021-08-13 16:08:16
 */
@Data
public class CommonConfigQueryReqDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "园区id")
    private Integer parkId;

	@ApiModelProperty(value = "配置类型")
    private List<Integer> configTypes;

	@ApiModelProperty(value = "预约类型")
	private List<Integer> businessTypes;

}
