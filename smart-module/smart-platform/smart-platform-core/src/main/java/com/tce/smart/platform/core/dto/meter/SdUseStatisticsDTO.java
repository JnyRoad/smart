package com.tce.smart.platform.core.dto.meter;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

@Data
public class SdUseStatisticsDTO extends BaseDTO {

	/**
	 * 设备记录Id
	 */
	private Long id;

	/**
     * 查询开始时间
	 */
	private String startDate;

	/**
	 * 查询结束时间
	 */
	private String endDate;

	/**
	 * 查询段起数
	 */
	private String startNum;

	/**
	 * 查询段止数
	 */
	private String endNum;

	/**
	 * 月用量
	 */
	private String monthUse;
}
