package com.tce.smart.data.api.feign.msg;


import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.msg.req.EmailReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendEmailReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendEmailsReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEmailManagerService {

	/**
	 * 发送邮件
	 *
	 * @param sendEmailAo
	 * @return
	 */
	@PostMapping("/emailmanager/send/email")
	Result<?> sendEmail(@RequestBody SendEmailReqDTO sendEmailAo);

	/**
	 * 批量发送邮件
	 *
	 * @param sendEmailsAo
	 * @return
	 */
	@PostMapping("/emailmanager/send/emails")
	Result<?> sendEmails(@RequestBody SendEmailsReqDTO sendEmailsAo);

	/**
	 * 传入邮件内容直接发送邮件
	 *
	 * @param emailAo
	 * @return
	 */
	@PostMapping("/emailmanager/send/emailwithcontent")
	Result<?> sendEmailsWithContent(@RequestBody EmailReqDTO emailAo);


	@PostMapping("/emailmanager/template/{tempCode}")
	Result<List<String>> getTemplateKey(@PathVariable("tempCode") String tempCode);
}
