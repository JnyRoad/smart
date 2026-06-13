package com.tce.smart.app.ao.wechat;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 被访人信息查询Ao
 *
 * @author mingkai.wu
 * @date 2019-05-13 08:28:54
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AddVisitorAo extends BaseAO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 8687754044704419673L;

	/**
	 * 被访园区
	 */
	private Integer parkId;

	/**
	 * 被访人员工号
	 */
	private String employeeId;
	/**
	 * 被访人姓名
	 */
	private String employeeName;
	/**
	 * 被访人手机号
	 */
	private String employeeMobile;

	/**
	 * 访客姓名
	 */
	private String visitorName;

	/**
	 * 访客照片
	 */
	private String visitorPhoto;
	/**
	 * 访客照片ID
	 */
	private String visitorPhotoId;

	/**
	 * 访客电话
	 */
	private String visitorMobile;

	/**
	 * 短信验证码
	 */
	private String smsCode;

	/**
	 * 访客单位
	 */
	private String visitorCompany;

	/**
	 * 事由编码
	 */
	private String visitReasonCode;

	/**
	 * 访客身份证正面照
	 */
	private String visitorFrontPhoto;

	/**
	 * 身份证背面照
	 */
	private String visitorBackPhoto;

	/**
	 * 车牌号
	 */
	private String plateNumber;

	/**
	 * 开始时间
	 */
	private String startTime;

	/**
	 * 结束时间
	 */
	private String endTime;
	/**
	 * 身份证号
	 */
	private String certNo;

	/**
	 * 证件类型
	 */
	private Integer certType;

	/**
	 * 说明
	 */
	private String remark;
}
