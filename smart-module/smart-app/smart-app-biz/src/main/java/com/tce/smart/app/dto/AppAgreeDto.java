package com.tce.smart.app.dto;

import com.tce.smart.platform.api.dto.SmtParkDTO;
import lombok.Data;

import java.util.List;

@Data
public class AppAgreeDto {
	/**
	 * 协议主题的ID
	 */
	private Integer id;
	/**
	 * 输入的协议标题
	 */
	private String subjectName;
	/**
	 *选择的协议适配的园区id
	 */
	private int[]  parkId;
	/**
	 * 对应的模块ID
	 */
	private Integer moduleId;
	/**
	 *协议的内容
	 */
	private String textDesc;
	/**
	 * 对应的模块
	 */
	private String  module;
	/**
	 * 对应的园区集合
	 */
	private List<SmtParkDTO> park;
}
