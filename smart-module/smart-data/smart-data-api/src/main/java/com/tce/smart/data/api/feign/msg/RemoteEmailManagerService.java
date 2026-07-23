package com.tce.smart.data.api.feign.msg;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.msg.req.EmailReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendEmailReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendEmailsReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * 邮件服务内部调用契约。
 *
 * <p>默认门面统一附加服务身份，避免业务调用方直接退化为可被外部请求复用的邮件发送端点。</p>
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEmailManagerService {

	@PostMapping("/emailmanager/internal/send/email")
	Result<?> sendEmail(@RequestBody SendEmailReqDTO sendEmailAo,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result<?> sendEmail(SendEmailReqDTO sendEmailAo) {
		return sendEmail(sendEmailAo, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/emailmanager/internal/send/emails")
	Result<?> sendEmails(@RequestBody SendEmailsReqDTO sendEmailsAo,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result<?> sendEmails(SendEmailsReqDTO sendEmailsAo) {
		return sendEmails(sendEmailsAo, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/emailmanager/internal/send/emailwithcontent")
	Result<?> sendEmailsWithContent(@RequestBody EmailReqDTO emailAo,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result<?> sendEmailsWithContent(EmailReqDTO emailAo) {
		return sendEmailsWithContent(emailAo, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@PostMapping("/emailmanager/internal/template/{tempCode}")
	Result<List<String>> getTemplateKey(@PathVariable("tempCode") String tempCode,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	default Result<List<String>> getTemplateKey(String tempCode) {
		return getTemplateKey(tempCode, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}
}
