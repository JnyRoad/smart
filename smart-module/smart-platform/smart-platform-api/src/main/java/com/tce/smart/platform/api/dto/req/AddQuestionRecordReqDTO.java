package com.tce.smart.platform.api.dto.req;

import lombok.Data;

@Data
public class AddQuestionRecordReqDTO {


	private Integer id;

	/**
	 * 问卷id
	 */
	private Integer paperId;

	/**
	 * 问题id
	 */
	private Integer questionId;

	/**
	 * 答案
	 */
	private String answer;



	/**
	 * 员工号
	 */
	private String badge;
}
