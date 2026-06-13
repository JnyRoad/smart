package com.tce.smart.app.vo.fore;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区信息VO
 *
 * @author mingkai.wu
 * @date 2019-05-10 16:11:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ParkVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 5362758608679031031L;

	/**
	 * 园区编号
	 */
	private String parkId;

	/**
	 * 园区名称
	 */
	private String parkName;



	private BigDecimal parkLongitude;
	/**
	 * 园区纬度
	 */
	private BigDecimal parkLatitude;

}
