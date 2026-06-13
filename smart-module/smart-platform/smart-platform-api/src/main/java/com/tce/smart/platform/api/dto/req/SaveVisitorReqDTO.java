package com.tce.smart.platform.api.dto.req;


import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class SaveVisitorReqDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 车牌号
	 */
	private String vehiclePlate;
	/**
	 * 公司姓名
	 */
	private String company;
	/**
	 * 来访状态 0:已通过1:已驳回2:未处理3:已到达4超时未到
	 */
	private Integer status;
	/**
	 * 0:没有,1:有车
	 */
	private Integer isVehicle;
	/**
	 * 来访事由
	 */
	private Integer cause;
	/**
	 * 开始时间
	 */
	private String startTime;
	/**
	 * 结束时间
	 */
	private String endTime;
	/**
	 * 预约发起人
	 */
	private String promoterBadge;
	/**
	 * 被访人工号
	 */
	private String receptionistBadge;
	/**
	 * 被访人姓名
	 */
	private String receptionistName;
	/**
	 * 被访人手机号
	 */
	private String receptionistPhone;

	/**
	 * 身份证号
	 */
	private String certNo;

	/**
	 * 证件类型
	 */
	private Integer certType;

	/**
	 * 访客身份证正面照
	 */
	private String visitorFrontPhoto;

	/**
	 * 身份证背面照
	 */
	private String visitorBackPhoto;

	/**
	 * 说明
	 */
	private String remark;

	/**
	 * 行程二维码
	 */
	private String tripCode;

	/**
	 * 健康二维码
	 */
	private String healthcode;

	/**
	 * 携带物品
	 */
	private Integer carryThing;


	private List<SaveFellowVisitorReqDTO> visitorlist;


	private String processId;

	private String createTime;


}
