package com.tce.smart.platform.core.dto.securityzone;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 自动删除审计任务明细的数据库投影。
 *
 * <p>状态从当前任务表实时读取；任务不存在时 status 保持为空，由服务转成未知状态。</p>
 */
@Data
public class SecurityAuthDeleteLogTaskDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String taskSource;
	private String taskId;
	private String deviceCode;
	private Integer action;
	private Integer status;
	private Integer code;
	private String remark;
	private LocalDateTime createTime;
	private LocalDateTime updateTime;
}
