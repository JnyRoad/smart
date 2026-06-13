package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 保密区权限申请
 *
 * @author fushiping
 * @date 2021-07-29 11:13:31
 */
@Data
public class SecurityAuthApplyReqDTO implements Serializable {

private static final long serialVersionUID = 1L;

	@ApiModelProperty("申请区域id")
    private String areaId;

	@ApiModelProperty("申请区域名")
    private String areaName;

	@ApiModelProperty("园区id")
    private Integer parkId;

	@ApiModelProperty("申请人工号")
	private String applyBadge;

	@ApiModelProperty("申请人部门ID")
	private String applyDep;

	@ApiModelProperty("权限申请人")
	private List<SecurityApplyPersonReqDTO> personList;

	@ApiModelProperty("授权进入区域选项")
	private List<Integer> areaType;

	@ApiModelProperty("授权进入新厂厂区域详情")
	private String permitArea;

	@ApiModelProperty("授权进入旧厂区域详情")
	private String permitOldArea;


}
