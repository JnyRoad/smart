package com.tce.smart.platform.api.dto.resp.admittance;

import com.tce.smart.platform.api.dto.resp.securityzone.OaFlowRespDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入厂申请预约表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:45
 */
@Data
public class AdmittanceApplyDetailRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@ApiModelProperty("id")
	private Integer id;

	@ApiModelProperty("smsCode")
	private String smsCode;
	/**
	 * 园区ID
	 */
	@ApiModelProperty("园区ID")
	private Integer parkId;
	/**
	 * 访客姓名
	 */
	@ApiModelProperty("访客姓名")
	private String visitorName;
	/**
	 * 来访状态 来访状态 0:已通过1:已驳回2:未处理3:已到达4超时未到
	 */
	@ApiModelProperty("来访状态")
	private Integer status;

	private String qrCode;

	private String visitorPhone;

	@ApiModelProperty("来访状态DESC")
	private String statusDesc;

	/**
	 * 开始时间
	 */
	@ApiModelProperty("创建时间")
	private LocalDateTime createTime;
	/**
	 * 开始时间
	 */
	@ApiModelProperty("开始时间")
	private LocalDateTime startTime;
	/**
	 * 结束时间
	 */
	@ApiModelProperty("结束时间")
	private LocalDateTime endTime;
	/**
	 * 被访人员工号
	 */
	@ApiModelProperty("被访人员工号")
	private String receptionistBadge;
	/**
	 * 被访人姓名
	 */
	@ApiModelProperty("被访人姓名")
	private String receptionistName;
	/**
	 * 被访人手机号
	 */
	@ApiModelProperty("被访人手机号")
	private String receptionistPhone;

	/**
	 * 参观单位
	 */
	@ApiModelProperty("参观单位")
	private String company;

	private Integer personType;

	private String personTypeDesc;

	/**
	 * 来访事由
	 */
	@ApiModelProperty("来访事由")
	private Integer cause;

	/**
	 * 事由
	 */
	@ApiModelProperty("来访事由DESC")
	private String causeDesc;
	/**
	 * 携带物品
	 */
	@ApiModelProperty("携带物品")
	private Integer thing;

	/**
	 * 携带物品
	 */
	@ApiModelProperty("携带物品DESC")
	private String thingDesc;
	/**
	 * 授权进入工厂类型
	 */
	private String permitFactoryType;
	/**
	 * 授权进入工厂类型名
	 */
	private String permitFactoryTypeDesc;
	/**
	 * 授权进入工厂区域
	 */
	private List<Integer> areaType;
	/**
	 * 区域详情
	 */
	private String permitArea;

	/**
	 * 旧厂区域详情
	 */
	private String permitOldArea;
	/**
	 * 申请部门
	 */
	@ApiModelProperty("申请部门")
	private String receptionistDept;
	/**
	 * 所属园区
	 */
	@ApiModelProperty("所属园区")
	private String parkName;
	/**
	 * 访客的跟随人员信息
	 */
	@ApiModelProperty("访客的跟随人员信息")
	private List<AdmittanceFellowRespDTO> fellowVisitorList;

	@ApiModelProperty("访客的车辆信息")
	private List<AdmittanceVehicleRespDTO> vehicleList;

	@ApiModelProperty("审批流程")
	private List<OaFlowRespDTO> processList;

	@ApiModelProperty("是否拍照")
	private String isPhoto;

	@ApiModelProperty("开始时间段（天）")
	private String startDays;

	@ApiModelProperty("开始时间段（时）")
	private String startTimes;

	@ApiModelProperty("结束时间段（天）")
	private String endDays;

	@ApiModelProperty("结束时间段（时）")
	private String endTimes;

	@ApiModelProperty("来访种类")
	private String visitTypeDesc;

	@ApiModelProperty("来访类别")
	private String visitCauseType;


	private Integer delFlag;


}
