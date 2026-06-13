package com.tce.smart.platform.core.dto;

import java.util.List;

import lombok.Data;

@Data
public class AddOrUpdatePaperDTO {


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
	private List<Integer> compIds;


	private List<AddOrUpdateQuestionDTO> questions;




}
