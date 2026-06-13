package com.tce.smart.platform.api.dto.resp;

import java.util.List;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class SearchQuestionListRespDTO  extends BaseVO {


private Integer questionId;

	/**
	 * 问卷id
	 */
	private Integer paperId;

	/**
	 * 问题标题
	 */
	private String title;

	/**
	 * 问题类型 0-单选 1-多选 2-问答题
	 */
	private Integer type;


	/**
	 * 答案
	 */
	private String answer;

	/**
	 * 选项
	 */
	private List<SearchSelectRespDTO> selectList;
}
