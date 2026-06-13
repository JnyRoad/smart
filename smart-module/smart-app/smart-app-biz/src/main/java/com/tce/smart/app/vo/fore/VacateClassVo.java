package com.tce.smart.app.vo.fore;

import lombok.Data;

/**
 *员工班次
 * @author ly
 *
 */
@Data
public class VacateClassVo {

	/**
	 * 班次描述
	 */
	private String classDesc;
	/**
	 * 2入（格式: HH:mm）
	 */
	private String secondEnter;
	/**
	 * 2出（格式: HH:mm）
	 */
	private String secondOut;
	/**
	 * 4入（格式: HH:mm）
	 */
	private String fourthEnter;
	/**
	 * 4出（格式: HH:mm）
	 */
	private String fourthOut;
	/**
	 * 5入（格式: HH:mm）
	 */
	private String fifthEnter;
	/**
	 * 5出（格式: HH:mm）
	 */
	private String fifthOut;
}
