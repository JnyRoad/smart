package com.tce.smart.app.vo;

import com.tce.smart.app.dto.AppModuleInfoDto;
import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.List;
@Data
public class AppServeVo extends BaseVO {
	private static final long serialVersionUID = 1L;
	/**
	 * 模块ID
	 */
	private Integer id;
	/**
	 * 模块名称
	 */
	private String moduleName;
	/**
	 * 状态判断
	 */
	private boolean moduleStatus;
	/**
	 * 状态判断
	 */
	private boolean editStatus;
	/**
	 * 所有子模块
	 */
	private List<AppModuleInfoDto> module;
}
