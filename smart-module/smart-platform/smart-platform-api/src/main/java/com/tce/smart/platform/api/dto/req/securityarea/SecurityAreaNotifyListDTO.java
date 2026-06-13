package com.tce.smart.platform.api.dto.req.securityarea;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 查询待通知保密区供应商列表DTO
 * @date: 2020-07-30 9:29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SecurityAreaNotifyListDTO implements Serializable {
	private static final long serialVersionUID = -2485505768750401777L;

	@ApiModelProperty(value = "园区id",required = true)
	private Integer parkId;

	@ApiModelProperty(value = "剩余天数",required = true)
	private Integer days;
}
