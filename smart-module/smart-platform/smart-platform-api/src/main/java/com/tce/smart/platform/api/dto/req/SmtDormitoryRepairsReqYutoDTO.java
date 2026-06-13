package com.tce.smart.platform.api.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: SmtDormitoryRepairsReqDTO
 * @date: 2020-07-20 14:13
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SmtDormitoryRepairsReqYutoDTO implements Serializable {
	private static final long serialVersionUID = -7289022427725195102L;
	/**
	 * 维修区域
	 */
	@ApiModelProperty(value = "维修区域")
	private Integer rangeType;

	/**
	 * 维修类别
	 */
	@ApiModelProperty(value = "维修类别")
	private Integer repairType;

	/**
	 * 申请开始时间
	 */
	@JsonIgnore
	@ApiModelProperty(value = "申请开始时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date beginTime;

	/**
	 * 申请结束时间
	 */
	@JsonIgnore
	@ApiModelProperty(value = "申请结束时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date endTime;

	/**
	 * 状态
	 */
	@JsonIgnore
	@ApiModelProperty(value = "状态")
	private Integer status;

}
