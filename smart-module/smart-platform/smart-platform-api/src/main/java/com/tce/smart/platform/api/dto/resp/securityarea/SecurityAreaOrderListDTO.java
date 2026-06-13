package com.tce.smart.platform.api.dto.resp.securityarea;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: SecurityAreaOrderListDTO
 * @date: 2020-07-30 9:39
 * @author: wuling
 * @version: 1.0
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SecurityAreaOrderListDTO implements Serializable {
	private static final long serialVersionUID = 1525020841426790019L;

	@ApiModelProperty("记录Id")
	private Long id;

	/**
	 * 到访区域
	 */
	@ApiModelProperty("到访区域")
	private String visitArea;

	/**
	 * 来访日期
	 */
	@ApiModelProperty("来访日期")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date comeTime;

	/**
	 * 离开日期
	 */
	@ApiModelProperty("离开日期")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date leaveTime;

	/**
	 * 状态描述
	 */
	@ApiModelProperty("状态描述")
	private String statusDesc;

	/**
	 * 申请人
	 */
	@ApiModelProperty("申请人")
	private String applicant;

	/**
	 * 申请日期
	 */
	@ApiModelProperty("申请日期")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createTime;
}
