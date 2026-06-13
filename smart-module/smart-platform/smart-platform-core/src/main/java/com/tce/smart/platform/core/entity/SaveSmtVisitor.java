package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import com.tce.smart.platform.core.dto.SaveFellowVisitorDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 添加访客表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SaveSmtVisitor extends Model<SaveSmtVisitor> {
private static final long serialVersionUID = 1L;
    /**
   *  访客姓名
   */
    private String visitorName;
    /**
   * 访客照片
   */
    private String visitorPhoto;
    /**
   * 访客电话
   */
    private String visitorPhone;
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
   *  0:没有,1:有车
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
	 * 所属园区id
	 */
	private Integer parkId;


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


    private List<SaveFellowVisitorDTO> fellowVisitorList;


	private String processId;

	private String createTime;



}
