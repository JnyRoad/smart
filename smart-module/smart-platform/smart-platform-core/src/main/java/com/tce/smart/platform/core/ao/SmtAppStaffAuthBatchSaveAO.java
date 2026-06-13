package com.tce.smart.platform.core.ao;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App员工权限表批量新增修改AO
 *
 * @author mckaywu
 * @date 2019-06-12 11:03:21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmtAppStaffAuthBatchSaveAO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 9009027460011032103L;

	/**
	 * 员工ID
	 */
	@NotEmpty(message = "员工ID不能为空")
	@NotNull(message = "员工ID不能为空")
	private String[] staffId;

	/**
	 * 权限ID数组
	 */
	@NotEmpty(message = "权限ID不能为空")
	@NotNull(message = "权限ID不能为空")
	private Integer[] authId;

}
