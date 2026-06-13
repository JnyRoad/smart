package com.tce.smart.platform.api.dto.resp.commonconfig;

import lombok.Data;

import java.io.Serializable;

/**
 * @author fushiping
 * @date 2021/8/13 0013 17:35
 **/
@Data
public class ConfigDeviceAdminDTO implements Serializable {

	private static final long serialVersionUID = -1L;

	/**
	 * 管理员工号
	 */
	private String adminBadge;

}
