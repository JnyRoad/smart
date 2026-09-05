package com.tce.smart.platform.api.dto.resp.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 保密区权限自动删除审计的设备任务明细。
 *
 * <p>taskId 保持字符串形态，兼容标准任务表 Integer 与 ISC 任务表 Long 主键。</p>
 */
@Data
public class SecurityAuthDeleteLogTaskRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String taskSource;

	@ApiModelProperty("设备任务ID")
	private String taskId;

	private String deviceCode;
	private Integer action;
	@ApiModelProperty("设备任务原始状态；任务缺失或状态为空时为null，展示为未知")
	private Integer status;
	private Integer code;
	private String remark;
	private LocalDateTime createTime;
	private LocalDateTime updateTime;
}
