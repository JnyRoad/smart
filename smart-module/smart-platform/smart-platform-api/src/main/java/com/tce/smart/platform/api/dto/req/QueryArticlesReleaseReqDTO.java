package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-23 17:02
 */
@Data
public class QueryArticlesReleaseReqDTO implements Serializable {
	private static final long serialVersionUID = -1;
	private Long id;
	private Integer parkId;
	private String badge;
	private String name;
	@ApiModelProperty(value = "物品放行类型")
	private Integer type;
	private String licensePlate;
	private Integer status;
	private String startTime;
	private String endTime;
}