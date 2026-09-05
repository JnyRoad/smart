package com.tce.smart.platform.api.dto.resp.securityzone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 保密区权限自动删除审计报表行。
 *
 * <p>Long 主键统一以字符串输出，避免前端 JavaScript 数字精度损失。</p>
 */
@Data
public class SecurityAuthDeleteLogRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("审计记录ID")
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private String id;

	private Integer parkId;
	private LocalDateTime execTime;

	@ApiModelProperty("员工ID")
	private String staffId;

	private String staffBadge;
	private String staffName;
	private String department;
	private Integer authId;
	private String authName;
	private LocalDateTime lastSnapTime;
	private String triggerReason;
	private String result;
	private String remark;
	private Integer taskCount;
	private Integer successCount;
	private Integer failCount;
	private Integer pendingCount;
	private Integer unknownCount;
}
