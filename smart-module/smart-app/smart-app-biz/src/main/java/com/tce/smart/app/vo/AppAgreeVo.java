package com.tce.smart.app.vo;

import com.tce.smart.app.entity.AppParkSubject;
import com.tce.smart.app.entity.AppSubjectModule;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AppAgreeVo extends BaseVO {
	private static final long serialVersionUID = 1L;
	/**
	 * 协议主题的ID
	 */
	private Integer id;
	/**
	 * 协议主题的名称
	 */
	private String subjectName;
	/**
	 * 协议主题的内容
	 */
	private String textDesc;
	/**
	 * 协议主题的文本ID
	 */
	private Integer contentTextId;
/*	*//**
	 * 协议主题的厂区范围ID
	 *//*
	private List<Integer> park;
	*//**
	 * 协议主题的厂区范围名
	 *//*
	private String parkId;
	*//**
	 * 协议主题适应的模块范围
	 *//*
	private String module;*/
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;


}
