package com.tce.smart.admin.api.vo;

import com.tce.smart.admin.api.entity.SysLog;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @date 2017/11/20
 */
@Data
public class LogVO implements Serializable {
	private static final long serialVersionUID = 1L;

	private SysLog sysLog;
	private String username;
}
