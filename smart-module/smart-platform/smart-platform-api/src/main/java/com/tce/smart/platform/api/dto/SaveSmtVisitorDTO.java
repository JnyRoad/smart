package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加访客表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
public class SaveSmtVisitorDTO implements Serializable {
private static final long serialVersionUID = 2480042680577535430L;
    /**
   *
   */
    private String visitorName;
    /**
   *
   */
    private String visitorPhoto;
    /**
   *
   */
    private String visitorPhone;
    /**
   *
   */
    private String vehiclePlate;
    /**
   *
   */
    private String company;
    /**
   * 来访状态 0:已通过1:已驳回2:未处理3:已到达4超时未到
   */
    private Integer status;
    /**
   *  0:没有,1:有车
   */
    private Integer isVehicle;
    /**
   *
   */
    private Integer cause;
    /**
   *
   */
    private String startTime;
    /**
   *
   */
    private String endTime;
    /**
     * 预约发起人
     */
    private String promoterBadge;
    /**
     *
     */
    private String receptionistBadge;
    /**
   *
   */
    private String receptionistName;
    /**
   *
   */
    private String receptionistPhone;
    /**
   * 是否发送提醒短信0是1否
   */
    private Integer isSend;

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
	 * 园区id
	 */
	private Integer parkId;
  /*  //随行人员信息
    private List<SaveFellowVisitorDTO> fellowVisitorList;*/

}
