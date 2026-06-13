package com.tce.smart.platform.api.dto.req.dormitorymange;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 内宿申请退回DTO
 * @date: 2020/12/29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DormitoryApplyFailBackDTO implements Serializable {
	private static final long serialVersionUID = -3294761334066770999L;

	@ApiModelProperty(value = "记录Id")
	private Long id;

	@ApiModelProperty(value = "原因")
	private String resultRemark;
}
