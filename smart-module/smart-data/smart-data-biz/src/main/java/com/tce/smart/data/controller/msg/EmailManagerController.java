package com.tce.smart.data.controller.msg;

import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.enums.ExceptionType;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.msg.req.EmailReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendEmailReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendEmailsReqDTO;
import com.tce.smart.data.service.msg.IEmailManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/emailmanager")
public class EmailManagerController {

	@Autowired
	private IEmailManagerService emailManagerService;

	@PostMapping("/send/email")
	public Result<?> sendEmail(@RequestBody SendEmailReqDTO sendEmailAo) {
		if (emailManagerService.sendHtmlEmail(sendEmailAo)) {
			return Result.success(null);
		}
		return Result.fail(CommonConstants.FAIL, "操作失败");

	}

	/**
	 * 批量发送邮件
	 *
	 * @param sendEmailsAo
	 * @return
	 */
	@PostMapping("/send/emails")
	Result<?> sendEmails(@RequestBody SendEmailsReqDTO sendEmailsAo) {
		emailManagerService.sendEmails(sendEmailsAo);
		return Result.success(null);
	}

	/**
	 * 传入邮件内容 直接发送邮件
	 *
	 * @param emailAo
	 * @return
	 */
	@PostMapping("/send/emailwithcontent")
	Result<?> sendEmailsWithContent(@RequestBody EmailReqDTO emailAo) {
		if (emailManagerService.sendEmailWithContent(emailAo)) {
			return Result.success(null);
		}
		return Result.fail(ExceptionType.SERVER_ERROR);
	}

	@PostMapping("/template/{tempCode}")
	Result<List<String>> getTemplateKey(@PathVariable("tempCode") String tempCode) {
		if (StringUtils.isBlank(tempCode)) {
			return Result.fail(CommonConstants.FAIL, "传入数据为空");
		}
		return Result.success(emailManagerService.getTemplateKey(tempCode));
	}
}
