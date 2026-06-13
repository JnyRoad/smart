package com.tce.smart.platform.core.vo;

import java.util.List;

import lombok.Data;

@Data
public class SearchPaperDetailVO {


	private Integer id;

	/**
	 * 问卷标题
	 */
	private String title;



	/**
	 * 开始时间
	 */
	private String startTime;

	/**
	 * 结束时间
	 */
	private String endTime;

	/**
	 * 状态 0-未开始 1-进行中 2-已结束
	 */
	private Integer status;


	/**
	 * 所属园区id
	 */
	private Integer parkId;

	/**
	 * 发布范围
	 */
	private List<String> compIds;


	private List<SearchQuestionDetailVO> questions;




}
