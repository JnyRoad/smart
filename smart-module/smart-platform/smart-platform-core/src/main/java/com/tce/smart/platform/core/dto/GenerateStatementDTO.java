package com.tce.smart.platform.core.dto;

import lombok.Data;

import java.util.Date;

/**
 * @description: GenerateStatementDTO
 * @date: 2020-07-15 14:58
 * @author: wuling
 * @version: 1.0
 */
@Data
public class GenerateStatementDTO {
	/**
	 * SmtSdMeterread表标识ID
	 */
	private Long id;

	/**
	 * 水电模板ID
	 */
	private Long tempId;

	/**
	 * 房间号标识
	 */
	private Integer roomId;

	/**
	 * 规则月份
	 */
	private Integer monthNum;

	/**
	 * 抄表月份
	 */
	private Date meterMonth;


}
