package com.tce.smart.app.vo.fore;

import java.util.Date;

import lombok.Data;
/**
 * 补卡记录列表
 * @author ly
 *
 */
@Data
public class PatchApplicationVo {

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
	 * 补卡时间
	 */
	private String patchDate;
	/**
	 * 补卡原因
	 */
	private String patchReasonDesc;

	/**
	 * bu名称
	 */
	private String buName;
	/**
	 * 班次描述
	 */
	private String classDesc;
	/**
	 * 缺卡次数
	 */
	private String missPatchCount;
	/**
	 * 申请记录时间
	 */
	private Date recordDate;
}
