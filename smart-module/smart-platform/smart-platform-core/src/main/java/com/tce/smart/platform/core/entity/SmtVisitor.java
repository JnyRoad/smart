package com.tce.smart.platform.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 访客表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
@TableName("smt_visitor")
@EqualsAndHashCode(callSuper = true)
public class SmtVisitor extends Model<SmtVisitor> {
	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	private Integer parkId;
	/**
	 * 访客姓名
	 */
	private String visitorName;
	/**
	 * 图片id
	 */
	private String visitorPhotoId;
	/**
	 * 访客手机号
	 */
	private String visitorPhone;
	/**
	 * 车牌
	 */
	private String vehiclePlate;
	/**
	 * 公司
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
	 * 访客来访类型
	 */
	private Integer cause;
	/**
	 * 开始时间
	 */
	private Date startTime;
	/**
	 * 结束时间
	 */
	private Date endTime;
	/**
	 * 被访人员工号
	 */
	private String receptionistBadge;
	/**
	 * 发起人的员工号
	 */
	private String promoterBadge;
	/**
	 * 被访人姓名
	 */
	private String receptionistName;
	/**
	 * 被访人手机号
	 */
	private String receptionistPhone;

	/**
	 * 被防人级别（福利层次）
	 */
	private String receptionistLevel;
	/**
	 * 是否发送提醒短信0是1否
	 */
	private Integer isSend;

    /**
   * 创建时间
   */
    private Date createTime;
	/**
	 * 身份证号
	 */
	private String certNo;

	/**
	 * 证件类型
	 */
	private Integer certType;

	/**
	 * 访客身份证正面照片
	 */
	private String visitorFrontPhotoId;

	/**
	 * 访客身份证反面照
	 */
	private String visitorBackPhotoId;

	/**
	 * 说明
	 */
	private String remark;

	/**
	 *  预约成功的验证码
	 */
	private String smsCode;

	/**
	 *  删除状态
	 */
	private Integer delFlag;

	/**
	 *携带物品
	 */
	private Integer carryThing;

	/**
	 * 行程二维码
	 */
	private String tripCode;

	/**
	 * 健康二维码
	 */
	private String healthcode;

	private String processId;

}
