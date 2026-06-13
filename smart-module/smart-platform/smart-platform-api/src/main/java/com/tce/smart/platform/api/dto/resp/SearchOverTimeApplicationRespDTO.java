package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 加班申请列表返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
public class SearchOverTimeApplicationRespDTO implements Serializable {
	private static final long serialVersionUID = 4747113681490718818L;

	/**
	 * 申请id
	 */
	private Integer recordId;
	/**
	 * 员工姓名
	 */
	private String staffName;

	/**
	 * 记录类型备注
	 */
	private String recordDesc;

	/**
	 * 流程id
	 */

	private String processId;

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
	 * 加班类型说明
	 */
	private String extraworkTypeName;
	/**
	 * 加班类型
	 */
	private Integer workType;

}
