package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 园区表
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:35
 */
@Data
public class SmtParkRespDTO extends BaseVO {
	private static final long serialVersionUID = -2947421344144981911L;

	/**
	 * 园区ID
	 */
	@ApiModelProperty("园区ID")
	private Integer id;
	/**
	 * 园区名称
	 */

	@ApiModelProperty("园区名称")
	@NotBlank(message = "园区名称不能为空")
	private String parkName;

	/**
	 * 园区经度
	 */
	@ApiModelProperty("园区经度")
	@NotBlank(message = "园区经度不能为空")
	private BigDecimal parkLongitude;
	/**
	 * 园区纬度
	 */
	@ApiModelProperty("园区纬度")
	@NotBlank(message = "园区维度不能为空")
	private BigDecimal parkLatitude;
	/**
	 * 园区地址
	 */
	@ApiModelProperty("园区地址")
	private String parkAddress;
	/**
	 * 识别半径,单位是米
	 */
	@ApiModelProperty("识别半径,单位是米")
	private Integer radius;

	/**
	 * 定位计算距离，单位:米
	 */
	@ApiModelProperty("定位计算距离，单位:米")
	private BigDecimal distance;
//
//	/**
//	 * 咨询电话
//	 */
//	@ApiModelProperty("咨询电话")
//	private String parkPhone;

	/**
	 * 转发服务器URL
	 */
	@ApiModelProperty("转发服务器URL")
	private String bridgeUrl;
}
