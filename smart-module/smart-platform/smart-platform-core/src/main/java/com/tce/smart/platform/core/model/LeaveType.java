package com.tce.smart.platform.core.model;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

/**
 * 离职类型数据字典
 * @author Lenovo
 *
 */
@Data
public class LeaveType extends BaseVO {

    private static final long serialVersionUID = 1L;

    /**
	 * value
	 */
	private Integer typeCode;

	/**
	 * label
	 */
	private String typeName;
}
