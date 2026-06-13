package com.tce.smart.data.service.msg;

import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.api.vo.msg.SendSmsVo;

import java.util.List;

/**
 * 短信服务接口
 *
 * @author mingkai.wu
 * @date 2019-05-15 10:23:09
 */
public interface ISmsManageService {

	/**
	 * 发送预约短信通知
	 *
	 * @param appointmentMsgAo 访客预约Ao
	 * @return
	 */
	SendSmsVo sendAppointmentSms(AppointmentMsgReqDTO appointmentMsgAo);

	/**
	 * 发送预约短信通知
	 *
	 * @param appointmentMsgAo 访客预约Ao
	 * @return
	 */
	SendSmsVo sendVisitorProxySms(AppointmentMsgReqDTO appointmentMsgAo);

	/**
	 * 发送招聘通知
	 *
	 * @param recruitMsgAo 招聘通知Ao
	 * @return
	 */
	SendSmsVo sendRecruitSms(RecruitMsgReqDTO recruitMsgAo);

	/**
	 * 发送离职通知
	 *
	 * @param dimissionMsgAo dimissionMsgAo
	 * @return
	 */
	SendSmsVo sendDimissionSms(DimissionMsgReqDTO dimissionMsgAo);

	/**
	 * 发送物流车预约短信通知
	 *
	 * @param guardMsgAo 物流车预约Ao
	 * @return
	 */
	SendSmsVo sendGuardSms(GuardMsgReqDTO guardMsgAo);

	/**
	 * 发送短信验证码
	 *
	 * @param smsCodeMsgAo smsCodeMsgAo
	 * @return
	 */
	SendSmsVo sendSmsCode(SendSmsCodeMsgReqDTO smsCodeMsgAo);

	/**
	 * 发送失败后，短息提醒
	 * @param sendSmsErrorAo
	 * @return
	 */
	SendSmsVo sendSmsError(SendSmsErrorReqDTO sendSmsErrorAo);

	/**
	 * 厂牌补领申请拒绝
	 * @param badgeRefuseMsgReqDTO
	 * @return
	 */
	SendSmsVo sendBadgeRefuse(BadgeRefuseMsgReqDTO badgeRefuseMsgReqDTO);

	/**
	 * 厂牌补领申请同意
	 * @param badgeAgreeMsgReqDTO
	 * @return
	 */
	SendSmsVo sendBadgeAgree(BadgeAgreeMsgReqDTO badgeAgreeMsgReqDTO);

	/**
	 * 工资签收提醒
	 * @param signMsgReqDTO
	 * @return
	 */
	Boolean sendWageSign(List<SignMsgReqDTO> signMsgReqDTO);

	/**
	 * 考勤汇总确认提醒
	 * @param signMsgReqDTO
	 * @return
	 */
	Boolean sendAttendanceSign(List<SignMsgReqDTO> signMsgReqDTO);

	/**
	 *  临时人员物品放行
	 * @param articlesReleaseMsgReqDTO
	 * @return
	 */
	Boolean sendArticlesRelease(ArticlesReleaseMsgReqDTO articlesReleaseMsgReqDTO);

	/**
	 * 短信发送
	 * @param reqDTO
	 * @param
	 * @return
	 */
	Boolean sendMessage(SendMsgReqDTO reqDTO);
}
