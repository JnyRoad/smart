package com.tce.smart.platform.api.dto.resp.admittance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tce.smart.common.core.vo.BaseVO;
import com.tce.smart.platform.api.dto.resp.GetSmtFellowVisitorRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.OaFlowRespDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @Title:
 * @Auther: fushiping
 * @Date: 2020-10-21 20:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdmittanceApplyCodeDetailRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String remotePath;
	private String visitorName;
	private String visitorPhoto;
	private String visitorPhone;
	private String company;
	private String causeDesc;
	private String startTime;
	private String endTime;
	private String parkName;
	private String receptionistName;
	private String receptionistPhone;
	private List<AdmittanceVehicleRespDTO> vehicleList;
	private String smsCode;
	private String qrCode;
	private Integer delFlag;
	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 访客图片id
	 */
	private String visitorPhotoId;

	/**
	 * 访客图片id
	 */
	private String visitorPhotoIdUrl;
	/**
	 * 来访状态 来访状态 0:已通过 1:已驳回 2:未处理 3:已到达 4超时未到
	 */
	private Integer status;
	/**
	 * 被访人员工号
	 */
	private String receptionistBadge;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 备注说明
	 */
	private String remark;
	/**
	 * 参观机台
	 */
	private Integer personType;

	private String personTypeDesc;

	/**
	 * 来访事由
	 */
	private Integer cause;
	/**
	 * 携带物品
	 */
	private Integer thing;

	/**
	 * 携带物品
	 */
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
	 * 授权进入旧厂区域详情
	 */
	private String permitOldArea;

	/**
	 * 申请部门
	 */
	private String receptionistDept;
	/**
	 * 访客的跟随人员信息
	 */

	private List<AdmittanceFellowRespDTO> fellowVisitorList;

	@ApiModelProperty("来访种类")
	private String visitTypeDesc;

	@ApiModelProperty("来访类别")
	private String visitCauseType;

	@ApiModelProperty("是否拍照")
	private String isPhoto;

	@ApiModelProperty("来访状态DESC")
	private String statusDesc;


}
