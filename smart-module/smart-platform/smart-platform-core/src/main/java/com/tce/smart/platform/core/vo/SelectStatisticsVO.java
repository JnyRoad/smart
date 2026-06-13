package com.tce.smart.platform.core.vo;

import lombok.Data;

/**
 * 调查问卷-选项统计结果
 * @author 齐佩
 *
 */
@Data
public class SelectStatisticsVO {

	/**
	 * 答案
	 */
	private String answer;

	/**
	 * 人数
	 */
	private Integer num;

}
