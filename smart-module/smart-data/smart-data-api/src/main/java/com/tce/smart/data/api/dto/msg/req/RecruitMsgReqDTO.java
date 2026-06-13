package com.tce.smart.data.api.dto.msg.req;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发送招聘短信Ao
 *
 * @author mingkai.wu
 * @date 2019-05-15 10:33:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecruitMsgReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 1746244629039720561L;

	/**
	 * 手机号码
	 */
	private String number;

	/**
	 * 模板编码
	 */
	private String tempCode;

	/**
	 * 应聘者姓名
	 */
	private String applicantName;

	/**
	 * 面试时间(面试通知)
	 */
	private String faceTime;

	/**
	 * 复试时间(复试通知)
	 */
	private String faceAgainTime;

	/**
	 * bu名称(录取通知)
	 */
	private String buName;

	/**
	 * 部门名称(录取通知)
	 */
	private String deptName;

	/**
	 * 岗位名称(录取通知)
	 */
	private String jobName;

	/**
	 * 入职时间(录取通知)
	 */
	private String entryDate;

	/**
	 * 星期(录取通知)
	 */
	private String entryWeek;

	/**
	 * 时(录取通知)
	 */
	private String entryTime;

	/**
	 * 链接URL
	 */
	private String linkUrl;

	/**
	 * 园区地址
	 */
	private String parkAddress;

	/**
	 * 园区咨询电话
	 */
	private String parkPhone;

}
