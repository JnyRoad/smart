package com.tce.smart.app.vo.fore;

import java.util.List;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App服务模块
 *
 * @author mckaywu
 * @date 2019-06-11 15:39:55
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModuleListVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -2822444197751452728L;

	/**
	 * 业务模块
	 */
	private List<SubModuleDetailVo> serviceModule;

	/**
	 * 附加模块列表
	 */
	private List<SubModuleInfoVo> extraModule;

}