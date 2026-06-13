package com.tce.smart.platform.api.dto.resp.commonconfig;

import lombok.Data;

import java.io.Serializable;

/**
 * @author fushiping
 * @date 2021/8/13 0013 17:35
 **/
@Data
public class ConfigVisitorApprovalDTO implements Serializable {

	private static final long serialVersionUID = -1L;

	/**
	 * 是否需要上级审批 0 否 1 是
	 */
	private Integer needApproval;

}
