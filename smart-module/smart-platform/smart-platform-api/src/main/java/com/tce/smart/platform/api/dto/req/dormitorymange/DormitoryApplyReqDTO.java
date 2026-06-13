package com.tce.smart.platform.api.dto.req.dormitorymange;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 内宿申请DTO
 * @date: 2020/12/29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DormitoryApplyReqDTO implements Serializable {
	private static final long serialVersionUID = -5283848002936604084L;

	@ApiModelProperty(value = "园区Id")
	private Integer parkId;

	@ApiModelProperty(value = "床位类型 1.上铺铺 2.下铺")
	private Integer bedType ;

	@ApiModelProperty(value = "申请备注")
	private String applyRemark;
}
