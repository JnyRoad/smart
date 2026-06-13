package com.tce.smart.data.service.msg;

import com.tce.smart.data.api.dto.msg.req.EmailReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendEmailReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendEmailsReqDTO;

import java.util.List;

public interface IEmailManagerService {

	/**
	 * 发送邮件
	 * @param sendEmail
	 * @return
	 */
	boolean sendHtmlEmail(SendEmailReqDTO sendEmail);

	/**
	 * 批量发送邮件
	 * @param sendEmailsAo
	 */
	void sendEmails(SendEmailsReqDTO sendEmailsAo);

	/**
	 * 构造数据直接发送邮件
	 * @param emailAo
	 */
	boolean sendEmailWithContent(EmailReqDTO emailAo);

	/**
	 * 根据模板号获取模板里面的变量key
	 * @param tempCode
	 * @return
	 */
	List<String> getTemplateKey(String tempCode);
}
