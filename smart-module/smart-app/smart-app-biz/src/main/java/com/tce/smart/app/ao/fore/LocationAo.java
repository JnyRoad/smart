package com.tce.smart.app.ao.fore;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 经纬度定位Ao
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:13:11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LocationAo extends BaseAO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 2872881423154160334L;

	/**
	 * 经度值
	 */
	private String longitude;

	/**
	 * 纬度值
	 */
	private String latitude;

	/**
	 * 园区名称
	 */
	private String parkName;
}
