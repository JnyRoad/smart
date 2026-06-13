package com.tce.smart.platform.core.vo;

import java.util.List;

import lombok.Data;

@Data
public class SearchQuestionDetailVO {

	private Integer id;

	private String title;


	/**
	 * 问题类型 0-单选 1-多选 2-问答题
	 */
	private Integer type;


	/**
	 * 问题答案列表
	 */
	private List<String> answers;
}
