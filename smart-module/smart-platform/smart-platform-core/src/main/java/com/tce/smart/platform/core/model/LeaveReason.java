package com.tce.smart.platform.core.model;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

/**
 * 离职原因字典
 * @author Lenovo
 *
 */
@Data
public class LeaveReason extends BaseVO {

    private static final long serialVersionUID = 1L;

    /**
	 * value
	 */
	private Integer reasonCode;

	/**
	 * label
	 */
	private String reasonName;
}
