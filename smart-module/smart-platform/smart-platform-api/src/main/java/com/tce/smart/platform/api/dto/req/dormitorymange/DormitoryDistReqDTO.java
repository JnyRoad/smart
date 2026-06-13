package com.tce.smart.platform.api.dto.req.dormitorymange;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 内宿自动分配条件DTO
 * @date: 2020/12/29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DormitoryDistReqDTO implements Serializable {
	private static final long serialVersionUID = -9134495839684689078L;

	@ApiModelProperty(value = "园区Id")
	private Integer parkId;

	@ApiModelProperty(value = "员工工号")
	private String staffBadge;

	@ApiModelProperty(value = "床位类型")
	private Integer bedType;
}
