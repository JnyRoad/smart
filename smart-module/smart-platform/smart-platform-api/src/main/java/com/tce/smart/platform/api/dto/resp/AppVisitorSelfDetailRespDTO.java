package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 发起人或被访人查看本人关联预约时的最小响应。
 *
 * 此契约刻意不定义身份证、人脸/头像或证件照片、健康/行程码、审批流和流程编号等字段，防止后续
 * BeanUtils 复制或新增字段时意外向 App 浏览器透传敏感资料。
 */
@Data
public class AppVisitorSelfDetailRespDTO {
	private Integer parkId;
	private String parkName;
	private Long visitorId;
	private String visitorName;
	private String visitorPhone;
	private String vehiclePlate;
	private String company;
	private Integer cause;
	private String causeDesc;
	private Integer status;
	private String statusDesc;
	private Date startTime;
	private Date endTime;
	private String receptionistName;
	private String receptionistPhone;
	private Integer carryThing;
	private String carryThingDesc;
	private List<AppVisitorFellowRespDTO> fellowVisitorList;
}
