package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:24
 */
@Data
public class SecurityAuthDeleteReqDTO implements Serializable {

private static final long serialVersionUID = 1L;

	@ApiModelProperty("ID")
	private Long id;

	@ApiModelProperty("超过多少天后删除权限")
    private Integer deleteDay;

	@ApiModelProperty("是否计算假期")
    private Integer isHoliday;

	@ApiModelProperty("是否计算出差")
    private Integer isBusiness;

	@ApiModelProperty("是否计算请假")
    private Integer isLeave;

	@ApiModelProperty("是否计算调休")
    private Integer isCompensatory;

	@ApiModelProperty("是否启用白名单")
    private Integer isWhiteList;

	@ApiModelProperty("是否演练模式：0-正式删除，1-只记录判定")
	private Integer dryRun;

	@ApiModelProperty("园区id")
	private Integer parkId;

	@ApiModelProperty("白名单列表")
	private List<SecurityWhiteReqDTO> whiteList;
}
