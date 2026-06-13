package com.tce.smart.admin.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @program: smart
 * @description:
 * @author: Wuling
 * @create: 2021-07-27 17:46
 **/

@Data
public class SmtParkDTO implements Serializable {
	private static final long serialVersionUID = -1650717977192704794L;

	/**
	 * 园区ID
	 */
	private Integer id;
	/**
	 * 园区名称
	 */
	@NotBlank(message = "园区名称不能为空")
	private String parkName;

	/**
	 * 园区经度
	 */

	@NotBlank(message = "园区经度不能为空")
	private BigDecimal parkLongitude;
	/**
	 * 园区纬度
	 */
	@NotBlank(message = "园区维度不能为空")
	private BigDecimal parkLatitude;
	/**
	 * 园区地址
	 */
	private String parkAddress;
	/**
	 * 识别半径,单位是米
	 */
	private Integer radius;

	/**
	 * 定位计算距离，单位:米
	 */
	private BigDecimal distance;

	/**
	 * 咨询电话
	 */
	private String parkPhone;

	/**
	 * 转发服务器URL
	 */
	private String bridgeUrl;
}
