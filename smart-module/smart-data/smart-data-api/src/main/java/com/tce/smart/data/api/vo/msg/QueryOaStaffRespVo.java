package com.tce.smart.data.api.vo.msg;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询OA系员工信息Vo
 *
 * @author mckaywu
 * @date 2019-06-19 22:34:46
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryOaStaffRespVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -8183546165770240794L;

	/**
	 * ID
	 */
	private Integer ID;

	/**
	 * 员工名称
	 */
	private String LASTNAME;

	/**
	 * OA系统-部门编号
	 */
	private Integer DEPARTMENTID;

	/**
	 * OA系统-公司编号
	 */
	private Integer SUBCOMPANYID1;

	/**
	 * OA系统-岗位编号
	 */
	private Integer JOBTITLE;

}
