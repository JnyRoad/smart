package com.tce.smart.platform.core.dto;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 14:26
 */
@Data
public class OperateLogDTO extends BaseDTO {

	/**
	 * 功能类型
	 */
	private Integer code;
	/**
	 * 功能描述
	 */
	private String codeDesc;
	/**
	 * 操作目标
	 */
	private Long targetId;
	/**
	 * 操作动作
	 */
	private Integer action;
}
