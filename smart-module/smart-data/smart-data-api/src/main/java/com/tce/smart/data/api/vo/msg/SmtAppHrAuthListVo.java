package com.tce.smart.data.api.vo.msg;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * App HR招聘数据权限列表
 *
 * @author mckaywu
 * @date 2019-06-13 15:06:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmtAppHrAuthListVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -4704561498230996269L;

	/**
	 * 权限ID
	 */
	private String id;

	/**
	 * 权限名称
	 */
	private String authName;

	/**
	 * 创建时间
	 */
	private Date createTime;

}
