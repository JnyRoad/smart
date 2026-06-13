package com.tce.smart.platform.core.ao;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * App权限表新增修改AO
 *
 * @author mckaywu
 * @date 2019-06-12 11:03:21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmtAppAuthSaveAO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -7605582002129202885L;

	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 权限名称
	 */
	@NotBlank(message="权限名称不能为空")
	@NotNull(message="权限名称不能为空")
	private String authName;

	/**
	 * 模块ID,多个用逗号","分隔'
	 */
	@NotEmpty(message="模块ID不能为空")
	@NotNull(message="模块ID不能为空")
	private Integer[] moduleId;

	/**
	 * HR权限ID,多个用逗号","分隔'
	 */
	private Integer[] hrAuthId;

	/**
	 * 权限描述
	 */
//	@NotBlank(message="权限描述不能为空")
//	@NotNull(message="权限描述不能为空")
	private String authDesc;

	private Integer parkId;

	private Integer[] jcheId;

	private Integer initFlag;
}
