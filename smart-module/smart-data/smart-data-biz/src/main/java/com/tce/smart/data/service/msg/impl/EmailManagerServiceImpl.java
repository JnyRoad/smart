package com.tce.smart.data.service.msg.impl;

import cn.hutool.core.util.ReUtil;
import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.service.msg.IEmailManagerService;
import com.tce.smart.platform.core.entity.SmtMsgRecord;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;
import com.tce.smart.platform.core.service.SmtMsgRecordService;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.tool.enums.SmsRecordSateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class EmailManagerServiceImpl implements IEmailManagerService {

	/**
	 * 正则 匹配 {xxxx} 只匹配内容不匹配括号
	 */
	private final static String REPLACE_FLAG_PATTERN_IGN = "(?<=\\{)[^}]*(?=\\})";

	/**
	 * 正则 匹配 {xxx} 完全匹配
	 */
	private final static String REPLACE_FLAG_PATTERN_INC = "\\{([^}]*)\\}";

	@Value("${spring.mail.username}")
	private String senderEmail;

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private SmtMsgTemplateService smtMsgTemplateService;

	@Autowired
	private SmtMsgRecordService smtMsgRecordService;


	@Override
	public boolean sendHtmlEmail(SendEmailReqDTO sendEmail) {
		if (Objects.isNull(sendEmail)) {
			return false;
		}
		SmtMsgTemplate smtMsgTemplate = smtMsgTemplateService.selectByTempCode(sendEmail.getTempCode());
		String content = messageReplace(smtMsgTemplate.getTempContent(), sendEmail.getParam());
		String[] inbox = {sendEmail.getInbox()};
		return send(content, smtMsgTemplate, inbox, sendEmail.getFileData(),3);
	}


	@Override
	public void sendEmails(SendEmailsReqDTO sendEmailsAo) {
		if (Objects.isNull(sendEmailsAo)) {
			return;
		}
		List<AddresseeReqDTO> addressees = sendEmailsAo.getAddressee();
		if (CollectionUtils.isEmpty(addressees)) {
			return;
		}
		log.info("[批量发送邮件]：传入参数{}", sendEmailsAo.getParam().toString());
		SmtMsgTemplate smtMsgTemplate = smtMsgTemplateService.selectByTempCode(sendEmailsAo.getTempCode());
		String content = messageReplace(smtMsgTemplate.getTempContent(), sendEmailsAo.getParam());

		String[] inboxs = new String[addressees.size()];

		for (int i = 0; i < addressees.size(); i++) {
			inboxs[i] = addressees.get(i).getInbox();
		}
		send(content, smtMsgTemplate, inboxs,sendEmailsAo.getFileData(), 3);
		log.info("[批量发送邮件]:发送完成");
	}


	@Override
	public boolean sendEmailWithContent(EmailReqDTO emailAo) {
		if (Objects.isNull(emailAo)) {
			return false;
		}
		if (CollectionUtils.isEmpty(emailAo.getInboxs())) {
			return false;
		}
		List<String> boxs = emailAo.getInboxs();
		String[] inboxs = new String[boxs.size()];

		for (int i = 0; i < boxs.size(); i++) {
			inboxs[i] = boxs.get(i);
		}
		log.info("[发送邮件]：收件箱:{}", inboxs);
		//添加发送记录
		SmtMsgRecord record = new SmtMsgRecord();
		record.setMsgContent(emailAo.getContent());
		record.setTempName(emailAo.getTitle());
		//没有模板默认 为0
		record.setTempId(0);
		record.setMsgObject(Arrays.toString(inboxs).replace("[", "").replace("]", ""));
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			// 设置utf-8或GBK编码，否则邮件会有乱码，true表示为multipart邮件
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
			helper.setFrom(senderEmail);
			//不设置收件人，在抄送统一设置
			helper.setCc(inboxs);
			helper.setSubject(emailAo.getTitle());
			helper.setText(emailAo.getContent(), true);

			if(CollectionUtils.isNotEmpty(emailAo.getFileData())){
				for(EmailFileReqDTO file : emailAo.getFileData()){
					InputStreamSource source = new ByteArrayResource(file.getFileBytes());
					helper.addAttachment(MimeUtility.encodeWord(file.getFileName(),"utf-8","B"),source );
				}
			}

			javaMailSender.send(message);
			log.info("[发送邮件]:成功,收件箱:{}", inboxs);

		} catch (Exception e) {
			log.error("[发送邮件]:失败", e);
			record.setMsgState(SmsRecordSateEnum.FAILD.getCode());
			smtMsgRecordService.addRecord(record);
			return false;
		}
		record.setMsgState(SmsRecordSateEnum.SUCCESS.getCode());
		smtMsgRecordService.addRecord(record);
		return true;
	}

	/**
	 * 发送
	 *
	 * @param content        发送内容
	 * @param smtMsgTemplate 发送模板
	 * @param inbox          收件人
	 * @param fileData         附件
	 * @param retry          重试次数
	 * @return
	 */
	private boolean send(String content, SmtMsgTemplate smtMsgTemplate, String[] inbox, List<EmailFileReqDTO> fileData, Integer retry) {
		log.info("[发送邮件]：模板号-{},收件箱:{}", smtMsgTemplate.getTempCode(), inbox);
		//添加发送记录
		SmtMsgRecord record = new SmtMsgRecord();
		record.setMsgContent(content);
		record.setTempName(smtMsgTemplate.getTempName());
		record.setTempId(smtMsgTemplate.getId());
		record.setMsgObject(Arrays.toString(inbox).replace("[", "").replace("]", ""));
		try {

			MimeMessage message = javaMailSender.createMimeMessage();
			// 设置utf-8或GBK编码，否则邮件会有乱码，true表示为multipart邮件

			MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
			helper.setFrom(senderEmail);
			//不设置收件人，在抄送统一设置
			helper.setCc(inbox);
			helper.setSubject(smtMsgTemplate.getTempName());
			helper.setText(content, true);

			if(CollectionUtils.isNotEmpty(fileData)){
				for(EmailFileReqDTO file : fileData){
					InputStreamSource source = new ByteArrayResource(file.getFileBytes());
					helper.addAttachment(MimeUtility.encodeWord(file.getFileName(),"utf-8","B"), source);
				}
			}

			javaMailSender.send(message);
			log.info("[发送邮件]:成功，模板号-{},收件箱:{}", smtMsgTemplate.getTempCode(), inbox);

		} catch (Exception e) {
			log.error("[发送邮件]:失败，尝试重发", e);
			if (retry > 0) {
				log.info("[发送邮件]:失败，尝试重发,该次重发为倒数第{}次", retry);
				retry--;
				send(content, smtMsgTemplate, inbox,fileData, retry);
			} else {
				log.error("[发送邮件]:失败", e);
				record.setMsgState(SmsRecordSateEnum.FAILD.getCode());
				smtMsgRecordService.addRecord(record);
				return false;
			}
		}
		record.setMsgState(SmsRecordSateEnum.SUCCESS.getCode());
		smtMsgRecordService.addRecord(record);
		return true;

	}

	@Override
	public List<String> getTemplateKey(String tempCode) {
		SmtMsgTemplate smtMsgTemplate = smtMsgTemplateService.selectByTempCode(tempCode);
		if (Objects.isNull(smtMsgTemplate)) {
			return null;
		}
		return getInputParam(smtMsgTemplate.getTempContent());
	}

	/**
	 * 替换花括号内容
	 *
	 * @param msg
	 * @param map 替换参数和内容
	 * @return
	 */
	private String messageReplace(String msg, Map<String, String> map) {
		Pattern pattern = Pattern.compile(REPLACE_FLAG_PATTERN_INC);
		Matcher matcher = pattern.matcher(msg);
		//重写 Matcher.replaceAll
		boolean result = matcher.find();
		StringBuffer sb = new StringBuffer();
		if (result) {
			do {
				String matcherStr = matcher.group();
				String temp = matcherStr.substring(matcherStr.indexOf("{") + 1, matcherStr.lastIndexOf("}"));
				String replace = Objects.isNull(map.get(temp)) ? "-" : map.get(temp).trim();
				matcher.appendReplacement(sb, replace);
				result = matcher.find();
			} while (result);
			matcher.appendTail(sb);
		}else{
			return msg;
		}
		return sb.toString();
	}

	/**
	 * 获取邮件模板入参
	 *
	 * @param msg
	 * @return
	 */
	private List<String> getInputParam(String msg) {

		if (ReUtil.contains(REPLACE_FLAG_PATTERN_IGN, msg)) {
			List<String> list = ReUtil.findAllGroup0(REPLACE_FLAG_PATTERN_IGN, msg);
			if (CollectionUtils.isNotEmpty(list)) {
				Set<String> set = new HashSet<>();
				Iterator<String> it = list.iterator();
				while (it.hasNext()) {
					//去除重复项
					if (!set.add(it.next())) {
						it.remove();
					}
				}
				return list;
			}
		}
		return null;
	}


}
