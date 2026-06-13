package com.tce.smart.platform.core.entity;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.platform.core.vo.FlowVO;
import com.tce.smart.platform.core.vo.GetSmtFellowVisitorVO;

import lombok.Data;

/**
 * 访客表
 *
 * @author liangyuan
 * @date 2019-04-11 15:57:18
 */
@SuppressWarnings("serial")
@Data
public class SearchVisitorDetail extends Model<SearchVisitorDetail> {

/**
 * 访客的信息
 */
   private Long visitorId;

   private String visitorName;

   private String visitorPhotoId;
   private String visitorPhoto;

   private String visitorPhone;

   private String vehiclePlate;

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
     * 事由
     */
    private Integer cause;

	/**
	 * 证件类型
	 */
	private Integer certType;

	/**
	 * 证件类型
	 */
	private String certTypeDesc;

    /**
     * 身份证号
     */
    private String certNo;
    /**
     * 事由
     */
    private String causeDesc;

    /**
   *
   */
    private Date startTime;
    /**
   *
   */
    private Date endTime;

	/**
	 * 创建时间
	 */
	private Date createTime;


	/**
	 * 访客身份证正面照片
	 */
	private String visitorFrontPhotoId;

	/**
	 * 访客身份证反面照
	 */
	private String visitorBackPhotoId;

	/**
	 * 访客身份证正面照片
	 */
	private String visitorFrontPhoto;

	/**
	 * 访客身份证反面照
	 */
	private String visitorBackPhoto;

	/**
	 * 说明
	 */
	private String remark;

	/**
	 * 所属园区
	 */
	private String parkName;

	/**
	 * 所属园区
	 */
	private String parkId;


    /**
     * 被访人的信息
     */

    private Long receptionistId;
    private String receptionistName;
    private String receptionistNumber;
    private String receptionistBU;
    private String receptionistDept;
    private String receptionistJob;
    private String receptionistJcheName;
    private String receptionistPhone;



    /**
     * 访客的跟随人员信息
     */
    private List<GetSmtFellowVisitorVO> fellowVisitorList;
    /**
     * 抓拍图片信息
     */
    private List<SnapVisitor> snapVisitorList;

    /**
     * 访客审批流程
     */
    private List<SmtVisitorProcessRecord> processList;

    private Integer isVip;

	/**
	 * 行程二维码
	 */
	private String tripCode;

	/**
	 * 健康二维码
	 */
	private String healthcode;

	/**
	 *携带物品
	 */
	private Integer carryThing;

	/**
	 *携带物品
	 */
	private String carryThingDesc;

	/**
	 * 流程编号
	 */
	private String processId;
}
