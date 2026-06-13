package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 离职类型数据字典
 * @author Lenovo
 *
 */
@Data
public class LeaveTypeDTO implements Serializable {

    private static final long serialVersionUID = 6653074878790602643L;

    /**
	 * value
	 */
	private Integer typeCode;

	/**
	 * label
	 */
	private String typeName;
}
