package com.tce.smart.app.vo.fore;

import java.util.Date;

import lombok.Data;
/**
 * 调休列表
 * @author ly
 *
 */
@Data
public class RestApplicationVo {

	/**
	 * 申请id
	 */
	private String recordId;
	/**
	 * 员工记录标题
	 */
	private String recordTitle;

	/**
	 * 调休类型
	 */
	private String restTypeDesc;
	/**
	 * 记录类型备注
	 */
	private String recordDesc;

	/**
	 * 调休日期
	 */
	private String restDate;

	/**
	 * 申请记录时间
	 */
	private Date recordDate;


}
