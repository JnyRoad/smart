package com.tce.smart.platform.core.vo;
import java.util.Date;

import lombok.Data;

/**
 * 查询访客记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:20
 */
@Data
public class SearchSmtVisitorVO{
    /**
   *
   */
    private String id;

    /**
   *
   */
    private String visitorName;
    /**
   *
   */
    private String visitorPhotoId;
    /**
   *
   */
    private String visitorPhone;


    private String visitorCertNo;
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
	 * 来访状态 0:已通过1:已驳回2:未处理3:已到达4超时未到
	 */
	private Integer deviceStatus;

	private String deviceStatusDesc;
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
    private Date startTime;
    /**
   *
   */
    private Date endTime;

    private String visitorPhoto;

    /**
     * 被访人员工号
     */
    private String receptionistBadge;


    /**
     * 被访人姓名
     */
    private String receptionistName;

    /**
     * 被访人电话
     */
    private String receptionistPhone;

    /**
     * 被访人bu
     */
    private String compName;

    /**
     * 被访人部门
     */
    private String depName;

    /**
     * 被访人岗位
     */
    private String jobName;

    /**
     * 所属园区
     */
    private String parkName;

    /**
     * 所属园区id
     */
    private Integer parkId;

	/**
	 * 是否有通行权限 0.没有 1.有
	 */
	private Integer hasAuth;

	private Integer personType;

	private String personTypeDesc;

	/**
	 * 行程二维码
	 */
	private String tripCode;

	/**
	 * 健康二维码
	 */
	private String healthcode;

	private Integer applyType;

	private String applyTypeDesc;


}
