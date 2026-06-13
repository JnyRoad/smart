package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 离职原因字典
 * @author Lenovo
 *
 */
@Data
public class LeaveReasonDTO implements Serializable {

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
