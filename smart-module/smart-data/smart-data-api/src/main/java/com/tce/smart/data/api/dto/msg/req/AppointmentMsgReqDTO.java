package com.tce.smart.data.api.dto.msg.req;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发送预约短信Ao
 *
 * @author mingkai.wu
 * @date 2019-05-15 10:33:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppointmentMsgReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -1639947169468280029L;

	/**
	 * 手机号码
	 */
	private String number;

	/**
	 * 模板编码
	 */
	private String tempCode;

	/**
	 * 访客姓名
	 */
	private String visitorName;

	/**
	 * 访客单位
	 */
	private String company;

	/**
	 * 被访对象姓名
	 */
	private String hostName;

	/**
	 * 预计来访时间
	 */
	private String appointmentDate;

	/**
	 * 通知时间
	 */
	private String noticeTime;

	/**
	 * 实际来访时间（访客到访通知）
	 */
	private String realityDate;

	/**
	 * 刷脸的门（访客到访通知）
	 */
	private String deviceName;

	/**
	 * 主管领导名字
	 */
	private String reportToName;

	/**
	 * 拒绝原因
	 */
	private String refuseDes;
	/**
	 * 验证码
	 */
	private String smsCode;

	/**
	 * 二维码url
	 */
	private String codeUrl;

	/**
	 * 访客码
	 */
	private String visitorCode;

	/**
	 *  园区名称
	 */
	private String parkName;
}
