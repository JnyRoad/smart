package com.tce.smart.app.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class AppQuestionDto {
	/**
	 * 添加问题时，问题主题id
	 */
	private Integer id;
	/**
	 * 添加问题时，问题文本id
	 */
	private Integer contentTextId;
	/**
	 * 添加问题时，问题信息
	 */
	private String subjectName;
	/**
	 * 添加问题时，答案信息
	 */
	private String textDesc;
	/**
	 * 查询条件，开始时间
	 */
	private String startTime;
	/**
	 * 查询条件，结束时间
	 */
	private String endTime;
	/**
	 * 查询条件，分类编码
	 */
	private String catalogCode;
	/**
	 * 创建时间
	 */
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;

}
