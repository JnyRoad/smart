package com.tce.smart.app.vo.fore;

import java.util.Date;

import lombok.Data;
/**
 * 加班列表
 * @author ly
 *
 */
@Data
public class ExtraWorkApplicationVo {

	/**
	 * 申请id
	 */
	private String recordId;
	/**
	 * 记录标题
	 */
	private String recordTitle;

	/**
	 * 记录类型备注
	 */
	private String recordDesc;

	/**
	 * 申请加班时间
	 */
	private String extraworkDate;

	/**
	 * 申请记录时间
	 */
	private Date recordDate;
	/**
	 * 加班时长
	 */
	private String extraworkCount;
	/**
	 * 加班类型
	 */
	private String extraworkTypeName;
}
