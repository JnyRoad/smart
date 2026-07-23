package com.tce.smart.data.api.feign.msg;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.api.vo.msg.SendSmsVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * 短信服务内部调用契约。
 *
 * <p>每个默认门面均固定附加内部来源和服务令牌，确保 App、Platform 的受控业务流程无需自行拼装安全头。</p>
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteSmsManageService {

	@PostMapping("/smsmanage/internal/send/appointment")
	Result<SendSmsVo> sendAppointmentSms(@RequestBody AppointmentMsgReqDTO appointmentMsgAo,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result<SendSmsVo> sendAppointmentSms(AppointmentMsgReqDTO appointmentMsgAo) {
		return sendAppointmentSms(appointmentMsgAo, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/smsmanage/internal/send/visitor/proxy")
	Result<SendSmsVo> sendVisitorProxySms(@RequestBody AppointmentMsgReqDTO appointmentMsgAo,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result<SendSmsVo> sendVisitorProxySms(AppointmentMsgReqDTO appointmentMsgAo) {
		return sendVisitorProxySms(appointmentMsgAo, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/smsmanage/internal/send/recruit")
	Result<SendSmsVo> sendRecruitSms(@RequestBody RecruitMsgReqDTO recruitMsgAo,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result<SendSmsVo> sendRecruitSms(RecruitMsgReqDTO recruitMsgAo) {
		return sendRecruitSms(recruitMsgAo, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/smsmanage/internal/send/dimission")
	Result<SendSmsVo> sendDimissionSms(@RequestBody DimissionMsgReqDTO dimissionMsgAo,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result<SendSmsVo> sendDimissionSms(DimissionMsgReqDTO dimissionMsgAo) {
		return sendDimissionSms(dimissionMsgAo, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/smsmanage/internal/send/guard")
	Result<SendSmsVo> sendGuardSms(@RequestBody GuardMsgReqDTO guardMsgAo,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result<SendSmsVo> sendGuardSms(GuardMsgReqDTO guardMsgAo) {
		return sendGuardSms(guardMsgAo, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/smsmanage/internal/send/smsCode")
	Result<SendSmsVo> sendSmsCode(@RequestBody SendSmsCodeMsgReqDTO smsCodeMsgAo,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result<SendSmsVo> sendSmsCode(SendSmsCodeMsgReqDTO smsCodeMsgAo) {
		return sendSmsCode(smsCodeMsgAo, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/smsmanage/internal/send/smsError")
	Result<SendSmsVo> sendSmsError(@RequestBody SendSmsErrorReqDTO sendSmsErrorAo,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result<SendSmsVo> sendSmsError(SendSmsErrorReqDTO sendSmsErrorAo) {
		return sendSmsError(sendSmsErrorAo, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/smsmanage/internal/send/badge/agree")
	Result<SendSmsVo> sendBadgeAgree(@RequestBody BadgeAgreeMsgReqDTO req,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result<SendSmsVo> sendBadgeAgree(BadgeAgreeMsgReqDTO req) {
		return sendBadgeAgree(req, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/smsmanage/internal/send/badge/refuse")
	Result<SendSmsVo> sendBadgeRefuse(@RequestBody BadgeRefuseMsgReqDTO req,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result<SendSmsVo> sendBadgeRefuse(BadgeRefuseMsgReqDTO req) {
		return sendBadgeRefuse(req, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/smsmanage/internal/send/attendance/sign")
	Result sendAttendanceSign(@RequestBody List<SignMsgReqDTO> signMsgReqDTO,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result sendAttendanceSign(List<SignMsgReqDTO> signMsgReqDTO) {
		return sendAttendanceSign(signMsgReqDTO, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/smsmanage/internal/send/wage/sign")
	Result sendWageSign(@RequestBody List<SignMsgReqDTO> signMsgReqDTO,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result sendWageSign(List<SignMsgReqDTO> signMsgReqDTO) {
		return sendWageSign(signMsgReqDTO, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/smsmanage/internal/send/articlesrelease/smscode")
	Result sendArticlesRelease(@RequestBody ArticlesReleaseMsgReqDTO articlesReleaseMsgReqDTO,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result sendArticlesRelease(ArticlesReleaseMsgReqDTO articlesReleaseMsgReqDTO) {
		return sendArticlesRelease(articlesReleaseMsgReqDTO, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/smsmanage/internal/send/msg")
	Result sendMessage(@RequestBody SendMsgReqDTO reqDTO,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result sendMessage(SendMsgReqDTO reqDTO) {
		return sendMessage(reqDTO, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}
}
