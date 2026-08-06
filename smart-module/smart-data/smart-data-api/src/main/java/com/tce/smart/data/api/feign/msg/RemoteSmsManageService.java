package com.tce.smart.data.api.feign.msg;

import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.api.vo.msg.SendSmsVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 短信服务
 *
 * @author mingkai.wu
 * @date 2019-05-15 18:54:18
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteSmsManageService {

	/**
	 * 发送预约通知短信
	 *
	 * @param appointmentMsgAo 预约通知
	 * @return
	 */
	@PostMapping("/smsmanage/send/appointment")
	Result<SendSmsVo> sendAppointmentSms(@RequestBody AppointmentMsgReqDTO appointmentMsgAo);

	/**
	 * 发送预约通知短信
	 *
	 * @param appointmentMsgAo 预约通知
	 * @return
	 */
	@PostMapping("/smsmanage/send/visitor/proxy")
	Result<SendSmsVo> sendVisitorProxySms(@RequestBody AppointmentMsgReqDTO appointmentMsgAo);
	/**
	 * 发送招聘通知短信
	 *
	 * @param recruitMsgAo 招聘通知短信Ao
	 * @return
	 */
	@PostMapping("/smsmanage/send/recruit")
	Result<SendSmsVo> sendRecruitSms(@RequestBody RecruitMsgReqDTO recruitMsgAo);

	/**
	 * 发送离职通知短信
	 *
	 * @param dimissionMsgAo 离职通知短信Ao
	 * @return
	 */
	@PostMapping("/smsmanage/send/dimission")
	Result<SendSmsVo> sendDimissionSms(@RequestBody DimissionMsgReqDTO dimissionMsgAo);

	/**
	 * 物流车预约通知
	 * @param guardMsgAo 物流车预约通知信息
	 * @return
	 */
	@PostMapping("/smsmanage/send/guard")
	Result<SendSmsVo> sendGuardSms(@RequestBody GuardMsgReqDTO guardMsgAo);

	/**
	 * 发送短信验证码
	 *
	 * @param smsCodeMsgAo 短信发送Ao
	 * @return
	 */
	@PostMapping("/smsmanage/send/smsCode")
	Result<SendSmsVo> sendSmsCode(@RequestBody SendSmsCodeMsgReqDTO smsCodeMsgAo);


	/**
	 * 发送短信验证码
	 *
	 * @param sendSmsErrorAo 短信发送Ao
	 * @return
	 */
	@PostMapping("/smsmanage/send/smsError")
	Result<SendSmsVo> sendSmsError(@RequestBody SendSmsErrorReqDTO sendSmsErrorAo);

	/**
	 * 厂牌补领申请同意短信发送
	 * @param req
	 * @return
	 */
	@PostMapping("/smsmanage/send/badge/agree")
	Result<SendSmsVo> sendBadgeAgree(@RequestBody BadgeAgreeMsgReqDTO req);

	/**
	 * 厂牌补领申请拒绝发送
	 * @param req
	 * @return
	 */
	@PostMapping("/smsmanage/send/badge/refuse")
	Result<SendSmsVo> sendBadgeRefuse(@RequestBody BadgeRefuseMsgReqDTO req);

	/**
	 * 考勤汇总确认提醒
	 * @param signMsgReqDTO
	 * @return
	 */
	@GetMapping("/smsmanage/send/attendance/sign")
	Result sendAttendanceSign(@RequestBody List<SignMsgReqDTO> signMsgReqDTO);

	/**
	 * 工资签收提醒
	 * @param signMsgReqDTO
	 * @return
	 */
	@GetMapping("/smsmanage/send/wage/sign")
	Result sendWageSign(@RequestBody List<SignMsgReqDTO> signMsgReqDTO);

	/**
	 *  临时人员物品放行
	 * @param articlesReleaseMsgReqDTO
	 * @return
	 */
	@GetMapping("/smsmanage/send/articlesrelease/smscode")
	Result sendArticlesRelease(@RequestBody ArticlesReleaseMsgReqDTO articlesReleaseMsgReqDTO);

	@GetMapping("/smsmanage/send/msg")
	Result sendMessage(@RequestBody  SendMsgReqDTO reqDTO);
}
