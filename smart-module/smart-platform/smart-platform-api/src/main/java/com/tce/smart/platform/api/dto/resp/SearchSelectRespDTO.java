package com.tce.smart.platform.api.dto.resp;


import lombok.Data;

@Data
public class SearchSelectRespDTO {

private Integer selectId;

	/**
	 * 问题id
	 */
	private Integer questionId;


	/**
	 * 选项内容
	 */
	private String answer;


}
