package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 保密区权限申请查询
 *
 * @author fushiping
 * @date 2021-07-29 11:13:31
 */
@Data
public class SecurityAuthApplyPageQueryReqDTO implements Serializable {

private static final long serialVersionUID = 1L;

	@ApiModelProperty("OA单号")
    private String processId;

	@ApiModelProperty("buID")
    private String buId;

	@ApiModelProperty("parkId")
	private Integer parkId;

	@ApiModelProperty("部门ID")
    private String depId;

	@ApiModelProperty("申请时间")
	private String startDate;

	@ApiModelProperty("申请时间")
	private String endDate;

	@ApiModelProperty("下发状态")
	private Integer downStatus;

}
