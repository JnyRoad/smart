package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 补卡申请列表返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
public class SearchReplaceApplicationRespDTO implements Serializable {
	private static final long serialVersionUID = 6284780804909989290L;

	/**
	 * id
	 */
	private Integer recordId;
	/**
	 * 员工姓名
	 */
	private String staffName;
	/**
	 * 流程id
	 */
	private String processId;
	/**
	 * 记录备注
	 */
	private String recordDesc;
	/**
	 * 补卡时间
	 */
	private String patchDate;
	/**
	 * 补卡原因ID
	 */
	private Integer cause;
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
	private Integer missPatchCount;

	/**
	 * 记录时间
	 */
	private Date recordDate;

}
