package com.tce.smart.platform.api.dto.resp.securityzone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:37
 */
@Data
public class SecurityApplyPersonRespDTO implements Serializable {

private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	@ApiModelProperty("申请区域名")
	private Long id;
    /**
   * 员工工号
   */
	@ApiModelProperty("申请区域名")
    private String badge;
    /**
   * 员工id
   */
	@ApiModelProperty("申请区域名")
    private Long staffId;
    /**
   * 员工姓名
   */
	@ApiModelProperty("申请区域名")
    private String staffName;
    /**
   * 创建时间
   */
    @ApiModelProperty("申请区域名")
    private String createTime;
    /**
   * 申请区域id
   */
	@ApiModelProperty("申请区域名")
    private String areaId;
    /**
   * 申请区域名
   */
	@ApiModelProperty("申请区域名")
    private String areaName;
    /**
   * 申请区域明细
   */
	@ApiModelProperty("申请区域名")
    private String areaDetails;
    /**
   * 申请表ID
   */
	@ApiModelProperty("申请区域名")
    private Integer applyId;

}
