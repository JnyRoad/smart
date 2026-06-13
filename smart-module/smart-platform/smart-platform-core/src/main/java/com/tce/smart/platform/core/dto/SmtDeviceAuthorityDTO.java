package com.tce.smart.platform.core.dto;

import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备权限
 *
 * @author 王艳勇
 * @date 2019-04-13 18:19:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmtDeviceAuthorityDTO extends SmtDeviceAuthority {

	private static final long serialVersionUID = 1L;
	/**
	 * 权限关联设备ID集合
	 */
	private String[] checkedlimits;
}
