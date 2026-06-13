package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 常见问题详情Vo
 *
 * @author mckaywu
 * @date 2019-06-10 20:00:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionDetailVo extends BaseVO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -5738036064072629898L;

	/**
	 * 问题ID
	 */
	private Integer questionId;

	/**
	 * 问题
	 */
	private String questionTitle;

	/**
	 * 问题解答
	 */
	private String answerContent;
}
