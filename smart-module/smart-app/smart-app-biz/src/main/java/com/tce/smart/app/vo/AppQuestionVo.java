package com.tce.smart.app.vo;

import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppQuestionVo extends BaseVO {
	private static final long serialVersionUID = 1L;
	/**
	* 问题的ID
	*/
	private Integer id;
	/**
	 * 添加问题时，问题文本id
	 */
	private Integer contentTextId;
	/**
	 * 问题的内容
	 */
	private String subjectName;
	/**
	 * 问题的文本
	 */
    private String textDesc;
	/**
	 * 问题的创建时间
	 */
	private LocalDateTime createTime;

}
