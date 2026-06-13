package com.tce.smart.platform.api.dto.resp.securityzone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:31
 */
@Data
public class SecurityAuthApplyRespDTO implements Serializable {

private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	@ApiModelProperty("ID")
	private Long id;
    /**
   * 流水号
   */
	@ApiModelProperty("流水号")
    private String serialNum;
    /**
   * oa单号
   */
	@ApiModelProperty("oa单号")
    private String processId;
    /**
   * 创建时间
   */
	@ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    /**
   * oa状态
   */
	@ApiModelProperty("oa状态")
    private Integer oaStatus;
    /**
   * 下发状态
   */
	@ApiModelProperty("下发状态")
    private Integer deviceStatus;
    /**
   * 下发总数
   */
	@ApiModelProperty("下发总数")
    private Integer totalNum;
    /**
   * 成功总数
   */
	@ApiModelProperty("成功总数")
    private Integer successNum;
    /**
   * 申请区域id
   */
	@ApiModelProperty("申请区域id")
    private String areaId;
    /**
   * 申请区域名
   */
	@ApiModelProperty("申请区域名")
    private String areaName;
    /**
   * 备注
   */
	@ApiModelProperty("备注")
    private String remark;
    /**
   * 园区id
   */
	@ApiModelProperty("园区id")
    private Integer parkId;

}
