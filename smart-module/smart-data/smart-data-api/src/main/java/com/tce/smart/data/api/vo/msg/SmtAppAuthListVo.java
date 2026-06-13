package com.tce.smart.data.api.vo.msg;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * App权限列表Vo
 *
 * @author mckaywu
 * @date 2019-06-13 15:06:29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmtAppAuthListVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -4704561498230996269L;

	/**
	 * 权限ID
	 */
	private Integer id;

	/**
	 * 权限名称
	 */
	private String authName;

	/**
	 * 是否固定
	 */
	private Boolean isFix;

	/**
	 * 权限描述
	 */
	private String authDesc;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 园区名称
	 */
	private String parkName;
}
