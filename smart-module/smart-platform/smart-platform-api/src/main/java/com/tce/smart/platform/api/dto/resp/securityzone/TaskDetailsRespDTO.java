package com.tce.smart.platform.api.dto.resp.securityzone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author fushiping
 * @date 2021-07-29 11:13:17
 */
@Data
public class TaskDetailsRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@JsonFormat(shape=JsonFormat.Shape.STRING)
	private Long id;

	@ApiModelProperty("员工工号")
    private String staffBadge;

	@ApiModelProperty("员工姓名")
    private String staffName;

	@ApiModelProperty("申请区域")
    private String areaName;

	@ApiModelProperty("下发权限")
	private String auth;

	@ApiModelProperty("下发状态")
    private String statusDesc;

	@ApiModelProperty("下发状态code")
	private Integer status;

	@ApiModelProperty("失败备注")
    private String remark;

	@ApiModelProperty("创建时间")
    private LocalDateTime createTime;

}
