package com.tce.smart.platform.api.dto.resp.securityzone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限申请表详情
 *
 * @author
 * @date
 */
@Data
public class SecurityAuthApplyDetailRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("ID")
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	private Long id;

	@ApiModelProperty("OA单号")
	private String processId;

	@ApiModelProperty("流水号")
	private String serialNum;

	@ApiModelProperty("员工姓名")
	private String name;

	@ApiModelProperty("申请区域")
	private String areaId;

	@ApiModelProperty("申请区域")
	private String areaName;

	@ApiModelProperty("申请时间")
	private LocalDateTime createTime;

	@ApiModelProperty("OA状态描述")
	private String oaStatusDesc;

	@ApiModelProperty("下发状态描述")
	private String deviceStatusDesc;

	@ApiModelProperty("下发任务总数")
	private Integer totalNum;

	@ApiModelProperty("成功数量")
	private Integer successNum;

	@ApiModelProperty("失败数量")
	private Integer failNum;

	@ApiModelProperty("审批流程")
	private List<OaFlowRespDTO> oaFlow;

	/**
	 * 授权进入工厂区域
	 */
	private List<Integer> areaType;

	/**
	 * 授权进入工厂区域
	 */
	private String areaTypeName;
	/**
	 * 区域详情
	 */
	private String permitArea;

	/**
	 * 旧厂区域详情
	 */
	private String permitOldArea;
}
