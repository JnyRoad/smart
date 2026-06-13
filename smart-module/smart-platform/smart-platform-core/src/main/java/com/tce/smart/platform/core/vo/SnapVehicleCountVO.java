package com.tce.smart.platform.core.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 车辆记录统计
 *
 * @author 王艳勇
 * @date 2019-04-13 18:19:30
 */
@Data
public class SnapVehicleCountVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 进
	 */
	private Integer[] indoorNums;

	/**
	 * 出
	 */
	private Integer[] outdoorNums;

}
