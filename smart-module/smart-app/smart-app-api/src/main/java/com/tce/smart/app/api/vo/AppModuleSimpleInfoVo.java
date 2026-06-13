package com.tce.smart.app.api.vo;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App模块简短信息Vo
 *
 * @author mckaywu
 * @date 2019-06-13 18:01:37
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppModuleSimpleInfoVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -3439283411114289370L;

	/**
	 * 模块ID
	 */
	private Integer id;

	/**
	 * 模块名称
	 */
	private String moduleName;
}
