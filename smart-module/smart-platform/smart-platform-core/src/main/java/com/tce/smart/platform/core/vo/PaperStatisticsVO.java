package com.tce.smart.platform.core.vo;


import java.util.List;

import lombok.Data;

/**
 * 问卷调查统计结果表
 * @author 齐佩
 *
 */
@Data
public class PaperStatisticsVO  {

	/**
	 * 调查问卷标题
	 */
	private String title;

	/**
	 * 提交总数
	 */
	private Integer totalCount;


	private List<QuestionStatisticsVO> questions;

}
