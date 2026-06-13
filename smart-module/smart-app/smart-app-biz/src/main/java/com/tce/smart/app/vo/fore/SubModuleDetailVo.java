package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App服务模块-子模块信息
 *
 * @author mckaywu
 * @date 2019-06-11 14:06:47
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubModuleDetailVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 2389080007576276253L;

	/**
	 * 模块名称
	 */
	private String moduleName;

	/**
	 * 模块图标
	 */
	private String moduleIcon;

	/**
	 * 模块链接
	 */
	private String moduleUrl;

	/**
	 * 链接跳转类型
	 */
	private Integer contentLinkType;

}
