package com.tce.smart.app.vo.fore;

import java.util.List;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App服务模块-附加模块(自定义模块)
 *
 * @author mckaywu
 * @date 2019-06-11 14:12:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubModuleInfoVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 6208489725208374819L;

	/**
	 * 附加子模块集合名称
	 */
	private String hubModuleName;

	/**
	 * 附加子模块列表（自定义子模块）
	 */
	private List<SubModuleDetailVo> subModule;
}
