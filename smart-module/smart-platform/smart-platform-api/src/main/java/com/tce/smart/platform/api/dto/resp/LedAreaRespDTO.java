package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 区域
 */
@Data
public class LedAreaRespDTO implements Serializable {

	/**
	 * 行号，1~4【必选】
	 */
	private Integer areaRow;

	/**
	 * 显示类型：0：文本；1：时间【必选】
	 */
	private Integer areaType;

	/**
	 * 显示内容，时间为显示的格式【必选】
	 */
	private String areaContent;

	/**
	 * 显示的动作：0-静止；1-向上移动；2-向下移动；3-向左移动；4-向右移动；5-闪烁【必选】
	 */
	private Integer areaAction;

	/**
	 * 显示颜色：0-红色；1-绿色；2-黄色；3-蓝色；4-紫色；5-青色；6-白色【必选】
	 */
	private Integer areaColor;

	public LedAreaRespDTO() {

	}

	public LedAreaRespDTO(Integer areaRow, Integer areaType, String areaContent, Integer areaAction, Integer areaColor) {
		this.areaRow = areaRow;
		this.areaType = areaType;
		this.areaContent = areaContent;
		this.areaAction = areaAction;
		this.areaColor = areaColor;
	}
}