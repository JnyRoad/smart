package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 保密区权限自动删除审计分页、导出筛选条件。
 *
 * <p>园区范围由服务端从登录令牌取得，客户端提供的 parkId 只允许在该范围内筛选。</p>
 */
@Data
public class SecurityAuthDeleteLogPageQueryReqDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("园区ID")
	private Integer parkId;

	@ApiModelProperty("执行开始时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startTime;

	@ApiModelProperty("执行结束时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endTime;

	@ApiModelProperty("员工工号")
	private String staffBadge;

	@ApiModelProperty("员工姓名")
	private String staffName;

	@ApiModelProperty("部门名称")
	private String department;

	@ApiModelProperty("权限组名称")
	private String authName;

	@ApiModelProperty("结果代码")
	private String result;
}
