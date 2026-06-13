package com.tce.smart.platform.api.dto.req.dormitorymange;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
public class BedReqDTO implements Serializable {
	private static final long serialVersionUID = 5169986485299868256L;

	@ApiModelProperty(value = "床位标识")
	private Integer bedId;

	@ApiModelProperty(value = "床位名称")
	private String bedName;

	@ApiModelProperty(value = "是否删除 1.删除 0.不删除")
	private Integer delFlag;
}
