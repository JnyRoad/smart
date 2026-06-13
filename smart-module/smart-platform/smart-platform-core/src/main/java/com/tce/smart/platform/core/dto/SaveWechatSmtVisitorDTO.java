package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 添加访客表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SaveWechatSmtVisitorDTO extends Model<SaveWechatSmtVisitorDTO> {
private static final long serialVersionUID = 1L;


	private Integer parkId;
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
	 * 证件类型
	 */
	private Integer certType;

    /**
     * 身份证号
     */
    private String certNo;
    /**
   * 是否发送提醒短信0是1否
   */
    private Integer isSend;


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


}
