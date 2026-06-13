package com.tce.smart.platform.core.vo;

import java.util.List;

import lombok.Data;

/**
 * 调查问卷-问题统计
 * @author 齐佩
 *
 */
@Data
public class QuestionStatisticsVO {

	private Integer id;
	/**
	 * 问题
	 */
	private String title;

	private Integer type;

	private Integer totalNum;

	private List<SelectStatisticsVO> selects;

}
