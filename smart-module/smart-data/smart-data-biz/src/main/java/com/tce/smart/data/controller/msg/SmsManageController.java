package com.tce.smart.data.controller.msg;

import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.api.vo.msg.SendSmsVo;
import org.springframework.web.bind.annotation.*;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.service.msg.ISmsManageService;

import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 短信服务控制器
 *
 * @author mingkai.wu
 * @date 2019-05-15 10:20:33
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smsmanage")
public class SmsManageController extends BaseController {

	private ISmsManageService smsManageService;

	/**
	 * 发送访客预约短信
	 *
	 * @param appointmentMsgAo 访客预约短信Ao
	 * @return Result
	 */
	@Inner
	@OpenApi("server")
	@PostMapping("/internal/send/appointment")
	public Result<SendSmsVo> sendAppointmentSms(@RequestBody AppointmentMsgReqDTO appointmentMsgAo) {
		return success(smsManageService.sendAppointmentSms(appointmentMsgAo));
	}

	/**
	 * 发送访客审批代理人短信
	 *
	 * @param appointmentMsgAo 访客预约短信Ao
	 * @return Result
	 */
	@Inner
	@OpenApi("server")
	@PostMapping("/internal/send/visitor/proxy")
	public Result<SendSmsVo> sendVisitorProxySms(@RequestBody AppointmentMsgReqDTO appointmentMsgAo) {
		return success(smsManageService.sendVisitorProxySms(appointmentMsgAo));
	}

	/**
	 * 发送招聘通知短信
	 *
	 * @param recruitMsgAo 招聘通知短信Ao
	 * @return Result
	 */
	@Inner
	@OpenApi("server")
	@PostMapping("/internal/send/recruit")
	public Result<SendSmsVo> sendRecruitSms(@RequestBody RecruitMsgReqDTO recruitMsgAo) {
		return success(smsManageService.sendRecruitSms(recruitMsgAo));
	}

	/**
	 * 发送离职通知短信
	 *
	 * @param dimissionMsgAo 离职通知短信Ao
	 * @return Result
	 */
	@Inner
	@OpenApi("server")
	@PostMapping("/internal/send/dimission")
	public Result<SendSmsVo> sendDimissionSms(@RequestBody DimissionMsgReqDTO dimissionMsgAo) {
		return success(smsManageService.sendDimissionSms(dimissionMsgAo));
	}

	/**
	 * 发送物流车预约通知短信
	 *
	 * @param guardMsgAo 物流车预约通知短信Ao
	 * @return Result
	 */
	@Inner
	@OpenApi("server")
	@PostMapping("/internal/send/guard")
	public Result<SendSmsVo> sendGuardSms(@RequestBody GuardMsgReqDTO guardMsgAo) {
		return success(smsManageService.sendGuardSms(guardMsgAo));
	}

	/**
	 * 发送离职通知短信
	 *
	 * @param smsCodeMsgAo 离职通知短信Ao
	 * @return Result
	 */
	@Inner
	@OpenApi("server")
	@PostMapping("/internal/send/smsCode")
	public Result<SendSmsVo> sendSmsCode(@RequestBody SendSmsCodeMsgReqDTO smsCodeMsgAo) {
		return success(smsManageService.sendSmsCode(smsCodeMsgAo));
	}

	/**
	 * 发送失败后，短息提醒
	 * @param sendSmsErrorAo 错误内容
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@PostMapping("/internal/send/smsError")
	public Result<SendSmsVo> sendSmsError(@RequestBody SendSmsErrorReqDTO sendSmsErrorAo) {
		return success(smsManageService.sendSmsError(sendSmsErrorAo));
	}

	/**
	 * 厂牌补领申请同意短信发送
	 * @param req
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@PostMapping("/internal/send/badge/agree")
	public Result<SendSmsVo> sendBadgeAgree(@RequestBody BadgeAgreeMsgReqDTO req) {
		return success(smsManageService.sendBadgeAgree(req));
	}

	/**
	 * 厂牌补领申请拒绝发送
	 * @param req
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@PostMapping("/internal/send/badge/refuse")
	public Result<SendSmsVo> sendBadgeRefuse(@RequestBody BadgeRefuseMsgReqDTO req) {
		return success(smsManageService.sendBadgeRefuse(req));
	}

	/**
	 * 考勤汇总确认提醒
	 * @param signMsgReqDTO
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/internal/send/attendance/sign")
	public Result sendAttendanceSign(@RequestBody List<SignMsgReqDTO> signMsgReqDTO) {
		return success(smsManageService.sendAttendanceSign(signMsgReqDTO));
	}

	/**
	 * 工资签收提醒
	 * @param signMsgReqDTO
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/internal/send/wage/sign")
	public Result sendWageSign(@RequestBody List<SignMsgReqDTO> signMsgReqDTO) {
		return success(smsManageService.sendWageSign(signMsgReqDTO));
	}

	/**
	 *  临时人员物品放行
	 * @param articlesReleaseMsgReqDTO
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/internal/send/articlesrelease/smscode")
	public Result sendArticlesRelease(@RequestBody ArticlesReleaseMsgReqDTO articlesReleaseMsgReqDTO) {
		return success(smsManageService.sendArticlesRelease(articlesReleaseMsgReqDTO));
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/internal/send/msg")
	public Result sendMessage(@RequestBody SendMsgReqDTO reqDTO) {
		return success(smsManageService.sendMessage(reqDTO));
	}


}
