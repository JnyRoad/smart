package com.tce.smart.platform.api.dto.req.admittance;

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
public class SaveAdmittanceApplyReqDTO implements Serializable {

private static final long serialVersionUID = 1L;

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
   * 访客图片id
   */
	@ApiModelProperty("访客图片id")
    private String visitorPhotoId;
    /**
   * 访客手机号
   */
	@ApiModelProperty("访客手机号")
    private String visitorPhone;
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
   * 备注说明
   */
	@ApiModelProperty("备注说明")
    private String remark;
    /**
   * 参观单位
   */
	@ApiModelProperty("参观单位")
    private String company;
    /**
   * 人员类型
   */
	@ApiModelProperty("人员类型")
	private Integer personType;
    /**
   * 来访事由
   */
	@ApiModelProperty("来访事由")
    private Integer cause;
    /**
   * 携带物品
   */
	@ApiModelProperty("携带物品")
    private Integer thing;

	@ApiModelProperty("随行人员")
	List<AdmittanceFellowReqDTO> fellowList;

	@ApiModelProperty("车辆信息")
	List<AdmittanceVehicleReqDTO> vehicleList;

	@ApiModelProperty("用户微信unionId")
	private String unionId;

	@ApiModelProperty("授权进入区域类型ID")
	private String permitFactoryType;

	@ApiModelProperty("授权进入区域选项")
	private List<Integer> areaType;

	@ApiModelProperty("授权进入新厂厂区域详情")
	private String permitArea;


	@ApiModelProperty("授权进入旧厂区域详情")
	private String permitOldArea;


}
