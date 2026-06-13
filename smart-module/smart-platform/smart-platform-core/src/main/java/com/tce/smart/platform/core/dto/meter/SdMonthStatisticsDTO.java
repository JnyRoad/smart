package com.tce.smart.platform.core.dto.meter;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

@Data
public class SdMonthStatisticsDTO extends BaseDTO {

	/**
	 * 设备记录Id
	 */
	private Long id;

	/**
     * 月份
	 */
	private String month;

	/**
	 * 月用量
	 */
	private String monthUse;
}
