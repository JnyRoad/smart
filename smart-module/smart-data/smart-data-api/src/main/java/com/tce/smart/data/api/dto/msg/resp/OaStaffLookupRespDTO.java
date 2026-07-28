package com.tce.smart.data.api.dto.msg.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * OA 员工最小查询结果。
 *
 * 物品放行人员选择只需要 OA 员工标识与姓名，不能透传 OA 原始员工档案。
 */
@Data
public class OaStaffLookupRespDTO implements Serializable {

	private static final long serialVersionUID = 7843710529030427670L;

	/**
	 * OA 员工标识。
	 */
	private Integer id;

	/**
	 * 员工姓名。
	 */
	private String name;
}
