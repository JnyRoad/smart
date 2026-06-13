package com.tce.smart.platform.api.dto.req.dormitorymange;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 床位修改DTO
 * @date: 2020/12/14 8:48
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FloorCountQueryReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "床位标识")
	private Integer parkId;

	@ApiModelProperty(value = "床位名称")
	private List<Integer> dormitoryIds;
}
