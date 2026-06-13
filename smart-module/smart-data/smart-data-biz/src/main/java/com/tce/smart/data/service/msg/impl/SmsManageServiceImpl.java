package com.tce.smart.data.service.msg.impl;

import cn.hutool.json.JSONUtil;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.enums.ExceptionType;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.api.vo.msg.SendSmsVo;
import com.tce.smart.data.service.msg.ISmsManageService;
import com.tce.smart.data.util.BaiduDwz;
import com.tce.smart.platform.core.entity.SmtMsgRecord;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;
import com.tce.smart.platform.core.service.SmtMsgRecordService;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.tool.enums.SmsRecordSateEnum;
import com.tce.smart.tool.enums.SmsTemplateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 短信服务实现类
 *
 * @author mingkai.wu
 * @date 2019-05-15 10:23:48
 */
@Service
@Slf4j
public class SmsManageServiceImpl implements ISmsManageService {

	@Value("${spring.sms.url}")
	private String smsUrl;
	@Value("${spring.sms.token}")
	private String smsToken;
	@Value("${spring.sms.systemName}")
	private String systemName;
	@Value("${spring.sms.remarks}")
	private String remarks;
	@Value("${spring.baidu.dwz.token:}")
	private String baiduDwzToken;
	@Autowired
	private SmtMsgTemplateService msgTemplateService;

	@Autowired
	private SmtMsgRecordService msgRecordService;


	@Override
	public SendSmsVo sendAppointmentSms(AppointmentMsgReqDTO appointmentMsgAo) {
		log.info("发送访客短信通知 appointmentMsgAo---->{}", appointmentMsgAo);
		SendSmsVo sendSmsVo = null;

		if (Objects.nonNull(appointmentMsgAo)) {

			SmtMsgTemplate smtMsgTemplate = msgTemplateService.selectByTempCode(appointmentMsgAo.getTempCode());
			if (Objects.isNull(smtMsgTemplate)) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
			}

			SendMsgReqDTO sendMsgAo = new SendMsgReqDTO();
			sendMsgAo.setNumber(appointmentMsgAo.getNumber());
			// 字段检查
			String smsContent = creatAppointContent(appointmentMsgAo, smtMsgTemplate);
			// 消息内容
			sendMsgAo.setContents(smsContent);

			Integer sendState = SmsRecordSateEnum.INIT.getCode();

			SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
			smtMsgRecord.setTempId(smtMsgTemplate.getId());
			smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
			smtMsgRecord.setMsgObject(appointmentMsgAo.getNumber());
			smtMsgRecord.setMsgContent(sendMsgAo.getContents());
			smtMsgRecord.setMsgState(sendState);

			// 发送短信短信，记录短信发送记录
			sendSmsVo = processSendMsg(sendSmsVo, sendMsgAo, smtMsgRecord);
		}

		return sendSmsVo;
	}

	@Override
	public SendSmsVo sendVisitorProxySms(AppointmentMsgReqDTO appointmentMsgAo) {
		log.info("发送访客审批代理人短信通知 appointmentMsgAo---->{}", appointmentMsgAo);
		SendSmsVo sendSmsVo = null;

		if (Objects.nonNull(appointmentMsgAo)) {

			SmtMsgTemplate smtMsgTemplate = msgTemplateService.selectByTempCode(appointmentMsgAo.getTempCode());
			if (Objects.isNull(smtMsgTemplate)) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
			}

			SendMsgReqDTO sendMsgAo = new SendMsgReqDTO();
			sendMsgAo.setNumber(appointmentMsgAo.getNumber());
			// 字段检查
			String smsContent = creatAppointContent(appointmentMsgAo, smtMsgTemplate);
			// 消息内容
			sendMsgAo.setContents(smsContent);

			Integer sendState = SmsRecordSateEnum.INIT.getCode();

			SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
			smtMsgRecord.setTempId(smtMsgTemplate.getId());
			smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
			smtMsgRecord.setMsgObject(appointmentMsgAo.getNumber());
			smtMsgRecord.setMsgContent(sendMsgAo.getContents());
			smtMsgRecord.setMsgState(sendState);

			// 发送短信短信，记录短信发送记录
			sendSmsVo = processSendMsg(sendSmsVo, sendMsgAo, smtMsgRecord);
		}

		return sendSmsVo;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.tce.smart.data.service.impl.processSendMsg#sendDimissionSms(com.tce.smart
	 * .data.api.ao.DimissionMsgAo)
	 */
	@Override
	public SendSmsVo sendDimissionSms(DimissionMsgReqDTO dimissionMsgAo) {
		SendSmsVo sendSmsVo = null;

		if (Objects.nonNull(dimissionMsgAo)) {

			SmtMsgTemplate smtMsgTemplate = msgTemplateService.selectByTempCode(dimissionMsgAo.getTempCode());
			if (Objects.isNull(smtMsgTemplate)) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
			}

			SendMsgReqDTO sendMsgAo = new SendMsgReqDTO();
			sendMsgAo.setNumber(dimissionMsgAo.getNumber());
			// 字段检查
			String smsContent = creatDimissionContent(dimissionMsgAo, smtMsgTemplate);
			// 消息内容
			sendMsgAo.setContents(smsContent);

			Integer sendState = SmsRecordSateEnum.INIT.getCode();

			SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
			smtMsgRecord.setTempId(smtMsgTemplate.getId());
			smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
			smtMsgRecord.setMsgObject(dimissionMsgAo.getNumber());
			smtMsgRecord.setMsgContent(sendMsgAo.getContents());
			smtMsgRecord.setMsgState(sendState);

			// 发送短信短信，记录短信发送记录
			sendSmsVo = processSendMsg(sendSmsVo, sendMsgAo, smtMsgRecord);
		}

		return sendSmsVo;
	}

	@Override
	public SendSmsVo sendGuardSms(GuardMsgReqDTO guardMsgAo) {
		SendSmsVo sendSmsVo = null;

		if (Objects.nonNull(guardMsgAo)) {

			SmtMsgTemplate smtMsgTemplate = msgTemplateService.selectByTempCode(guardMsgAo.getTempCode());
			if (Objects.isNull(smtMsgTemplate)) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
			}

			SendMsgReqDTO sendMsgAo = new SendMsgReqDTO();
			sendMsgAo.setNumber(guardMsgAo.getNumber());
			// 字段检查
			String smsContent = creatGuardContent(guardMsgAo, smtMsgTemplate);
			// 消息内容
			sendMsgAo.setContents(smsContent);

			Integer sendState = SmsRecordSateEnum.INIT.getCode();

			SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
			smtMsgRecord.setTempId(smtMsgTemplate.getId());
			smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
			smtMsgRecord.setMsgObject(guardMsgAo.getNumber());
			smtMsgRecord.setMsgContent(sendMsgAo.getContents());
			smtMsgRecord.setMsgState(sendState);

			// 发送短信短信，记录短信发送记录
			sendSmsVo = processSendMsg(sendSmsVo, sendMsgAo, smtMsgRecord);
		}

		return sendSmsVo;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.tce.smart.data.service.impl.processSendMsg#sendRecruitSms(com.tce.smart.
	 * data.api.ao.RecruitMsgAo)
	 */
	@Override
	public SendSmsVo sendRecruitSms(RecruitMsgReqDTO recruitMsgAo) {
		SendSmsVo sendSmsVo = null;

		if (Objects.nonNull(recruitMsgAo)) {
			SmtMsgTemplate smtMsgTemplate = msgTemplateService.selectByTempCode(recruitMsgAo.getTempCode());
			if (Objects.isNull(smtMsgTemplate)) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
			}

			SendMsgReqDTO sendMsgAo = new SendMsgReqDTO();
			sendMsgAo.setNumber(recruitMsgAo.getNumber());
			// 字段检查
			String smsContent = creatRecruitContent(recruitMsgAo, smtMsgTemplate);
			// 消息内容
			sendMsgAo.setContents(smsContent);

			Integer sendState = SmsRecordSateEnum.INIT.getCode();

			SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
			smtMsgRecord.setTempId(smtMsgTemplate.getId());
			smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
			smtMsgRecord.setMsgObject(recruitMsgAo.getNumber());
			smtMsgRecord.setMsgContent(sendMsgAo.getContents());
			smtMsgRecord.setMsgState(sendState);
			// 发送短信短信，记录短信发送记录
			sendSmsVo = processSendMsg(sendSmsVo, sendMsgAo, smtMsgRecord);
		}

		return sendSmsVo;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.tce.smart.data.service.impl.processSendMsg#sendSmsCode(com.tce.smart.data
	 * .api.ao.SendSmsCodeMsgAo)
	 */
	@Override
	public SendSmsVo sendSmsCode(SendSmsCodeMsgReqDTO smsCodeMsgAo) {

		SendSmsVo sendSmsVo = null;

		if (Objects.nonNull(smsCodeMsgAo)) {

			SmtMsgTemplate smtMsgTemplate = msgTemplateService.selectByTempCode(smsCodeMsgAo.getTempCode());
			if (Objects.isNull(smtMsgTemplate)) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
			}

			SendMsgReqDTO sendMsgAo = new SendMsgReqDTO();
			sendMsgAo.setNumber(smsCodeMsgAo.getNumber());
			// 字段检查
			String smsContent = creatSmsCodeContent(smsCodeMsgAo, smtMsgTemplate);
			// 消息内容
			sendMsgAo.setContents(smsContent);

			SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
			smtMsgRecord.setTempId(smtMsgTemplate.getId());
			smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
			smtMsgRecord.setMsgObject(smsCodeMsgAo.getNumber());
			smtMsgRecord.setMsgContent(sendMsgAo.getContents());
			smtMsgRecord.setMsgState(SmsRecordSateEnum.INIT.getCode());

			// 发送短信短信，记录短信发送记录
			sendSmsVo = processSendMsg(sendSmsVo, sendMsgAo, smtMsgRecord);
		}

		return sendSmsVo;
	}

	/**
	 * 构造短信验证码参数
	 *
	 * @param smsCodeMsgAo
	 * @param smtMsgTemplate
	 * @return
	 */
	private String creatSmsCodeContent(SendSmsCodeMsgReqDTO smsCodeMsgAo, SmtMsgTemplate smtMsgTemplate) {
		String smsContent = null;
		if (StringUtils.isBlank(smsCodeMsgAo.getName())) {
			throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "name(姓名)为空");
		}
		smsContent = smtMsgTemplate.getTempContent().replace("{姓名}", smsCodeMsgAo.getName());

		if (StringUtils.isBlank(smsCodeMsgAo.getSmsCode())) {
			throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "name(短信码)为空");
		}
		smsContent = smsContent.replace("{验证码}", smsCodeMsgAo.getSmsCode());

		return smsContent;
	}

	/**
	 * 检查招聘通知通知必须字段
	 *
	 * @param recruitMsgAo   招聘通知消息Ao
	 * @param smtMsgTemplate 消息模板编码
	 * @return String 转换后的消息
	 */
	private String creatRecruitContent(RecruitMsgReqDTO recruitMsgAo, SmtMsgTemplate smtMsgTemplate) {

		String smsContent = null;
		if (StringUtils.isBlank(recruitMsgAo.getApplicantName())) {
			throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "applicantName(姓名)为空");
		}

		// 拼接"姓名"
		String tempCode = smtMsgTemplate.getTempCode();
		smsContent = smtMsgTemplate.getTempContent().replace("{姓名}", recruitMsgAo.getApplicantName());

		smsContent = smsContent.replace("{电话}", recruitMsgAo.getParkPhone());
		smsContent = smsContent.replace("{园区详细地址}", recruitMsgAo.getParkAddress());

		// 检查"面试通知"模板字段
		if (SmsTemplateEnum.RECRUIT_2001.getCode().equals(tempCode)) {
			// 检查拼接"面试时间"
			if (StringUtils.isBlank(recruitMsgAo.getFaceTime())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "faceTime(面试时间)为空");
			} else {
				smsContent = smsContent.replace("{面试时间}", recruitMsgAo.getFaceTime());
			}
		} else if (SmsTemplateEnum.RECRUIT_2002.getCode().equals(tempCode)) {// 检查"复试通知"模板字段
			// 检查拼接"复试时间"
			if (StringUtils.isBlank(recruitMsgAo.getFaceAgainTime())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "faceAgainTime(复试时间)为空");
			} else {
				smsContent = smsContent.replace("{复试时间}", recruitMsgAo.getFaceAgainTime());
			}
		} else if (SmsTemplateEnum.RECRUIT_2003.getCode().equals(tempCode)) {// 检查"录取通知"模板字段
			// 检查拼接"U名称"
			if (StringUtils.isBlank(recruitMsgAo.getBuName())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "buName(BU名称)为空");
			} else {
				smsContent = smsContent.replace("{BU}", recruitMsgAo.getBuName());
			}

			// 检查拼接"部门名称"
			if (StringUtils.isBlank(recruitMsgAo.getDeptName())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "deptName(部门名称)为空");
			} else {
				smsContent = smsContent.replace("{DEPT}", recruitMsgAo.getDeptName());
			}

			// 检查拼接"岗位"
			if (StringUtils.isBlank(recruitMsgAo.getJobName())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "jobName(岗位)为空");
			} else {
				smsContent = smsContent.replace("{岗位}", recruitMsgAo.getJobName());
			}

			// 检查拼接"入职时间"
			if (StringUtils.isBlank(recruitMsgAo.getEntryDate())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "entryDate(入职时间)为空");
			} else {
				smsContent = smsContent.replace("{入职时间}", recruitMsgAo.getEntryDate());
			}

			// 检查拼接"星期"
			if (StringUtils.isBlank(recruitMsgAo.getEntryWeek())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "entryWeek(星期)为空");
			} else {
				smsContent = smsContent.replace("{星期几}", recruitMsgAo.getEntryWeek());
			}

			// 检查拼接"时"
			if (StringUtils.isBlank(recruitMsgAo.getEntryTime())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "entryTime(时)为空");
			} else {
				smsContent = smsContent.replace("{时}", recruitMsgAo.getEntryTime());
			}

			// 检查拼接"链接URL"
			if (StringUtils.isBlank(recruitMsgAo.getLinkUrl())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "linkUrl(链接URL)为空");
			} else {
				smsContent = smsContent.replace("{LINK_URL}", recruitMsgAo.getLinkUrl());
			}
		}

		return smsContent;
	}

	/**
	 * 检查访客预约通知必须字段
	 *
	 * @param appointmentMsgAo 客预约通知消息Ao
	 * @param smtMsgTemplate   消息模板编码
	 * @return String 转换后的消息
	 */
	private String creatAppointContent(AppointmentMsgReqDTO appointmentMsgAo, SmtMsgTemplate smtMsgTemplate) {

		String smsContent = null;
		if (StringUtils.isBlank(appointmentMsgAo.getHostName())) {
			throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "hostName(被访对象)为空");
		}

		if (StringUtils.isBlank(appointmentMsgAo.getVisitorName())) {
			throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "visitorName(访客姓名)为空");
		}
		if (StringUtils.isBlank(appointmentMsgAo.getCompany())) {
			appointmentMsgAo.setCompany("");
		}

		// 检查拼接"访客姓名"、"被访对象预约的访客申请已通过"
		String tempCode = smtMsgTemplate.getTempCode();
		smsContent = smtMsgTemplate.getTempContent().replace("{访客姓名}", appointmentMsgAo.getVisitorName())
				.replace("{被访对象预约的访客申请已通过}", appointmentMsgAo.getHostName() + "预约的访客申请已通过")
				.replace("{被访对象}", appointmentMsgAo.getHostName())
				.replace("{来访单位}", appointmentMsgAo.getCompany());

		// 检查"预约码"模板字段
		if (SmsTemplateEnum.VISIT_1001.getCode().equals(tempCode)) {
			// 检查拼接"预约码"
			if (StringUtils.isBlank(appointmentMsgAo.getSmsCode())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "smsCode(预约码)为空");
			} else {
				smsContent = smsContent.replace("{url}", BaiduDwz.createShortUrl(appointmentMsgAo.getCodeUrl(), baiduDwzToken));
			}

			if(StringUtils.isBlank(appointmentMsgAo.getParkName())){
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "parkName(预约园区)为空");
			}else{
				smsContent = smsContent.replace("{预约园区}", appointmentMsgAo.getParkName());
			}
		}

		// 检查"通知时间"模板字段
		if (SmsTemplateEnum.VISIT_1003.getCode().equals(tempCode)) {
			// 检查拼接"通知时间"
			if (StringUtils.isBlank(appointmentMsgAo.getNoticeTime())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "noticeTime(通知时间)为空");
			} else {
				smsContent = smsContent.replace("{通知时间}", appointmentMsgAo.getNoticeTime());
			}
		}

		if (SmsTemplateEnum.VISIT_1004.getCode().equals(tempCode) || SmsTemplateEnum.VISIT_1007.getCode().equals(tempCode)) {
			// 检查拼接"刷脸的门"
			if (StringUtils.isBlank(appointmentMsgAo.getDeviceName())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "deviceName(刷脸的门})为空");
			} else {
				smsContent = smsContent.replace("{刷脸的门}", appointmentMsgAo.getDeviceName());
			}
		}

		if (SmsTemplateEnum.VISIT_10011.getCode().equals(tempCode) || SmsTemplateEnum.VISIT_1002.getCode().equals(tempCode)) {
			// 检查"拒绝原因"模板字段
			if (StringUtils.isBlank(appointmentMsgAo.getRefuseDes())) {
				smsContent = smsContent.replace("{拒绝原因}", "未知");
			} else {
				smsContent = smsContent.replace("{拒绝原因}", appointmentMsgAo.getRefuseDes());
			}
		}

		// 检查"实访客到访通知"模板字段
		if (SmsTemplateEnum.VISIT_1004.getCode().equals(tempCode)) {
			// 检查拼接"实际来访时间"
			if (StringUtils.isBlank(appointmentMsgAo.getRealityDate())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "realityDate(实际来访时间)为空");
			} else {
				smsContent = smsContent.replace("{实际来访时间}", appointmentMsgAo.getRealityDate());
			}
		} else if (SmsTemplateEnum.VISIT_1007.getCode().equals(tempCode)) {
			if (StringUtils.isBlank(appointmentMsgAo.getRealityDate())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "realityDate(实际离开时间)为空");
			} else {
				smsContent = smsContent.replace("{实际离开时间}", appointmentMsgAo.getRealityDate());
			}
		} else if (SmsTemplateEnum.VISIT_1008.getCode().equals(tempCode)) {
			if (StringUtils.isBlank(appointmentMsgAo.getReportToName())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "reportToName(主管领导名字)为空");
			} else {
				smsContent = smsContent.replace("{主管领导名字}", appointmentMsgAo.getReportToName()).replace("{预计来访时间}", appointmentMsgAo.getAppointmentDate());
			}
		}else {
			// 检查拼接"预计来访时间"
			if (StringUtils.isBlank(appointmentMsgAo.getAppointmentDate())) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "appointmentDate(预计来访时间)为空");
			} else {
				smsContent = smsContent.replace("{预计来访时间}", appointmentMsgAo.getAppointmentDate());
			}
		}
		return smsContent;
	}

	/**
	 * 检查离职完成通知必须字段
	 *
	 * @param dimissionMsgAo 离职通知消息Ao
	 * @param smtMsgTemplate 消息模板
	 * @return String 转换后的消息
	 */
	private String creatDimissionContent(DimissionMsgReqDTO dimissionMsgAo, SmtMsgTemplate smtMsgTemplate) {

		String smsContent = null;
		if (StringUtils.isBlank(dimissionMsgAo.getDimissioName())) {
			throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "dimissioName(姓名)为空");
		}

		// 拼接"姓名"
//		String tempCode = smtMsgTemplate.getTempCode();
		smsContent = smtMsgTemplate.getTempContent().replace("{姓名}", dimissionMsgAo.getDimissioName());

		return smsContent;
	}

	/**
	 * 检查物流车预约通知必须字段
	 *
	 * @param guardMsgAo     招聘通知消息Ao
	 * @param smtMsgTemplate 消息模板
	 * @return String 转换后的消息
	 */
	private String creatGuardContent(GuardMsgReqDTO guardMsgAo, SmtMsgTemplate smtMsgTemplate) {

		String smsContent = null;
		if (StringUtils.isBlank(guardMsgAo.getVisitorName())) {
			throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "VisitorName(姓名)为空");
		}

		// 拼接"姓名"
//		String tempCode = smtMsgTemplate.getTempCode();
		smsContent = smtMsgTemplate.getTempContent()
				.replace("{姓名}", guardMsgAo.getVisitorName())
				.replace("{车牌号}", guardMsgAo.getPlat())
				.replace("{预约园区}", guardMsgAo.getParkName())
				.replace("{来访时间}", guardMsgAo.getAppointmentDate());

		return smsContent;
	}

	private SendSmsVo sendSms(SendMsgReqDTO sendMsgAo) {

		SendSmsVo sendSmsVo = null;

		String phoneNumber = sendMsgAo.getNumber();
		String smsContent = sendMsgAo.getContents();
		if (StringUtils.isBlank(phoneNumber) || StringUtils.isBlank(smsContent)) {
			return null;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		//headers.add("TokenID", smsToken);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("Number", phoneNumber);
		map.add("Contents", smsContent);
		map.add("SystemName", systemName);
		map.add("Remarks", remarks);
		map.add("TokenID", smsToken);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		String smsSendResp = new RestTemplate().postForObject(smsUrl, request, String.class);
		log.info("发送短信到{},resp={}", phoneNumber, smsSendResp);

		sendSmsVo = parseResp(smsSendResp);

		if (Objects.nonNull(sendSmsVo) && !String.valueOf(CommonConstants.SUCCESS).equals(sendSmsVo.getResult())) {
			throw new TCEException(sendSmsVo.getDescription());
		}

		return sendSmsVo;
	}

	/**
	 * 解析即接口响应信息
	 *
	 * @param smsSendResp smsSendResp
	 * @return
	 */
	private SendSmsVo parseResp(String smsSendResp) {
		SendSmsVo sendSmsVo = null;
		if (StringUtils.isNotBlank(smsSendResp)) {
			Map<String, Object> hashMap = new HashMap<String, Object>();
			String[] smsSendRespArray = smsSendResp.split("&");
			String[] paramTempArray = null;
			for (String parmsElement : smsSendRespArray) {
				paramTempArray = parmsElement.split("=");
				if (Objects.nonNull(paramTempArray) && paramTempArray.length == 2) {
					hashMap.put(paramTempArray[0], paramTempArray[1]);
				}
			}

			sendSmsVo = JSONUtil.toBean(JSONUtil.parseFromMap(hashMap), SendSmsVo.class);
		}

		return sendSmsVo;
	}

	/**
	 * 发送短信短信，记录短信发送记录
	 *
	 * @param sendSmsVo
	 * @param sendMsgAo
	 * @param smtMsgRecord
	 * @return
	 */
	private SendSmsVo processSendMsg(SendSmsVo sendSmsVo, SendMsgReqDTO sendMsgAo, SmtMsgRecord smtMsgRecord) {
		// 添加记录
		Integer pk = msgRecordService.addRecord(smtMsgRecord);

		String sendDesc = null;
		Integer sendState = SmsRecordSateEnum.INIT.getCode();
		try {
			// 发送短信
			sendSmsVo = sendSms(sendMsgAo);
			sendDesc = sendSmsVo.getTaskid();

			if (Objects.nonNull(sendSmsVo) && String.valueOf(CommonConstants.SUCCESS).equals(sendSmsVo.getResult())) {
				sendState = SmsRecordSateEnum.SUCCESS.getCode();
			}
		} catch (TCEException tce) {
			sendState = SmsRecordSateEnum.FAILD.getCode();
			sendDesc = tce.getMessage();
			throw tce;
		} catch (Exception e) {
			sendState = SmsRecordSateEnum.FAILD.getCode();
			sendDesc = "内部错误";
			log.error("短信调用失败,",e);
			throw new TCEException(ExceptionType.SERVER_ERROR);
		} finally {
			// 更新推送状态
			msgRecordService.updateRecordState(pk, sendState, sendDesc);
		}

		return sendSmsVo;
	}

	@Override
	public SendSmsVo sendSmsError(SendSmsErrorReqDTO sendSmsErrorAo) {
		// TODO Auto-generated method stub
		SendSmsVo sendSmsVo = null;

		if (Objects.nonNull(sendSmsErrorAo)) {

			SmtMsgTemplate smtMsgTemplate = msgTemplateService.selectByTempCode(sendSmsErrorAo.getTempCode());
			if (Objects.isNull(smtMsgTemplate)) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
			}

			SendMsgReqDTO sendMsgAo = new SendMsgReqDTO();
			sendMsgAo.setNumber(sendSmsErrorAo.getPhoneNumber());
			// 字段检查
			String smsContent = creatSmsErrorContent(sendSmsErrorAo, smtMsgTemplate);
			// 消息内容
			sendMsgAo.setContents(smsContent);

			SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
			smtMsgRecord.setTempId(smtMsgTemplate.getId());
			smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
			smtMsgRecord.setMsgObject(sendSmsErrorAo.getPhoneNumber());
			smtMsgRecord.setMsgContent(sendMsgAo.getContents());
			smtMsgRecord.setMsgState(SmsRecordSateEnum.INIT.getCode());

			// 发送短信短信，记录短信发送记录
			sendSmsVo = processSendMsg(sendSmsVo, sendMsgAo, smtMsgRecord);
		}
		return sendSmsVo;
	}

	@Override
	public SendSmsVo sendBadgeRefuse(BadgeRefuseMsgReqDTO badgeRefuseMsgReqDTO) {
		SendSmsVo sendSmsVo = null;

		if (Objects.nonNull(badgeRefuseMsgReqDTO)) {

			SmtMsgTemplate smtMsgTemplate = msgTemplateService.selectByTempCode(badgeRefuseMsgReqDTO.getTempCode());
			if (Objects.isNull(smtMsgTemplate)) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
			}
			SendMsgReqDTO sendMsgAo = new SendMsgReqDTO();
			//员工在填写个人电话时可能通过填写通过“/”分隔符分隔多个电话
			String phone = badgeRefuseMsgReqDTO.getNumber().substring(0, 10);
			sendMsgAo.setNumber(phone);
			// 字段检查
			String smsContent = "-";
			if (StringUtils.isNotBlank(badgeRefuseMsgReqDTO.getRefuseReason())) {
				smsContent = smtMsgTemplate.getTempContent().replace("{拒绝理由}", badgeRefuseMsgReqDTO.getRefuseReason());
			}
			// 消息内容
			sendMsgAo.setContents(smsContent);
			Integer sendState = SmsRecordSateEnum.INIT.getCode();
			SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
			smtMsgRecord.setTempId(smtMsgTemplate.getId());
			smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
			smtMsgRecord.setMsgObject(phone);
			smtMsgRecord.setMsgContent(sendMsgAo.getContents());
			smtMsgRecord.setMsgState(sendState);

			// 发送短信短信，记录短信发送记录
			sendSmsVo = processSendMsg(sendSmsVo, sendMsgAo, smtMsgRecord);
		}

		return sendSmsVo;
	}

	@Override
	public SendSmsVo sendBadgeAgree(BadgeAgreeMsgReqDTO badgeAgreeMsgReqDTO) {
		SendSmsVo sendSmsVo = null;

		if (Objects.nonNull(badgeAgreeMsgReqDTO)) {

			SmtMsgTemplate smtMsgTemplate = msgTemplateService.selectByTempCode(badgeAgreeMsgReqDTO.getTempCode());
			if (Objects.isNull(smtMsgTemplate)) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
			}
			SendMsgReqDTO sendMsgAo = new SendMsgReqDTO();
			String phone = badgeAgreeMsgReqDTO.getNumber();
			sendMsgAo.setNumber(phone);
			// 字段检查
			String smsContent = "-";
			if (StringUtils.isNotBlank(badgeAgreeMsgReqDTO.getAdress())) {
				smsContent = smtMsgTemplate.getTempContent().replace("{补领地址}", badgeAgreeMsgReqDTO.getAdress());
			}
			// 消息内容
			sendMsgAo.setContents(smsContent);
			Integer sendState = SmsRecordSateEnum.INIT.getCode();
			SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
			smtMsgRecord.setTempId(smtMsgTemplate.getId());
			smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
			smtMsgRecord.setMsgObject(phone);
			smtMsgRecord.setMsgContent(sendMsgAo.getContents());
			smtMsgRecord.setMsgState(sendState);

			// 发送短信短信，记录短信发送记录
			sendSmsVo = processSendMsg(sendSmsVo, sendMsgAo, smtMsgRecord);
		}

		return sendSmsVo;
	}

	@Override
	public Boolean sendWageSign(List<SignMsgReqDTO> signMsgReqDTO) {
		SendSmsVo sendSmsVo = null;
		if (CollectionUtils.isNotEmpty(signMsgReqDTO)) {
			SmtMsgTemplate smtMsgTemplate = msgTemplateService.selectByTempCode(signMsgReqDTO.get(0).getTempCode());
			if (Objects.isNull(smtMsgTemplate)) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
			}
			signMsgReqDTO.forEach(signMsgReq -> {
				if (StringUtils.isNotBlank(signMsgReq.getNumbers())) {
					SendMsgReqDTO sendMsgAo = new SendMsgReqDTO();
					//员工在填写个人电话时可能通过填写通过“/”分隔符分隔多个电话
					String phone = signMsgReq.getNumbers().substring(0, 11);
					sendMsgAo.setNumber(phone);
					// 消息内容
					String smsContent = "-";
					if (StringUtils.isNotBlank(signMsgReq.getPersonName())) {
						smsContent = smtMsgTemplate.getTempContent().replace("{员工姓名}", signMsgReq.getPersonName());
					}
					sendMsgAo.setContents(smsContent);
					Integer sendState = SmsRecordSateEnum.INIT.getCode();
					SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
					smtMsgRecord.setTempId(smtMsgTemplate.getId());
					smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
					smtMsgRecord.setMsgObject(phone);
					smtMsgRecord.setMsgContent(sendMsgAo.getContents());
					smtMsgRecord.setMsgState(sendState);
					// 发送短信短信，记录短信发送记录
					processSendMsg(sendSmsVo, sendMsgAo, smtMsgRecord);
				}
			});
		}
		return true;
	}

	@Override
	public Boolean sendAttendanceSign(List<SignMsgReqDTO> signMsgReqDTO) {
		SendSmsVo sendSmsVo = null;
		if (CollectionUtils.isNotEmpty(signMsgReqDTO)) {
			SmtMsgTemplate smtMsgTemplate = msgTemplateService.selectByTempCode(signMsgReqDTO.get(0).getTempCode());
			if (Objects.isNull(smtMsgTemplate)) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
			}
			signMsgReqDTO.forEach(signMsgReq -> {
				if (StringUtils.isNotBlank(signMsgReq.getNumbers())) {
					SendMsgReqDTO sendMsgAo = new SendMsgReqDTO();
					//员工在填写个人电话时可能通过填写通过“/”分隔符分隔多个电话
					String phone = signMsgReq.getNumbers().substring(0, 11);
					sendMsgAo.setNumber(phone);
					// 消息内容
					String smsContent = "-";
					if (StringUtils.isNotBlank(signMsgReq.getPersonName())) {
						smsContent = smtMsgTemplate.getTempContent().replace("{员工姓名}", signMsgReq.getPersonName());
					}
					sendMsgAo.setContents(smsContent);
					Integer sendState = SmsRecordSateEnum.INIT.getCode();
					SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
					smtMsgRecord.setTempId(smtMsgTemplate.getId());
					smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
					smtMsgRecord.setMsgObject(phone);
					smtMsgRecord.setMsgContent(sendMsgAo.getContents());
					smtMsgRecord.setMsgState(sendState);
					// 发送短信短信，记录短信发送记录
					processSendMsg(sendSmsVo, sendMsgAo, smtMsgRecord);
				}
			});
		}
		return true;
	}

	@Override
	public Boolean sendArticlesRelease(ArticlesReleaseMsgReqDTO articlesReleaseMsgReqDTO) {
		SendSmsVo sendSmsVo = null;
		SmtMsgTemplate smtMsgTemplate = msgTemplateService.selectByTempCode(articlesReleaseMsgReqDTO.getTempCode());
		if (Objects.isNull(smtMsgTemplate)) {
			throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
		}

		SendMsgReqDTO sendMsgReqDTO = new SendMsgReqDTO();
		sendMsgReqDTO.setNumber(articlesReleaseMsgReqDTO.getPhone());
		String smsContent = "-";
		if (StringUtils.isNotBlank(articlesReleaseMsgReqDTO.getUrl())) {
			smsContent = smtMsgTemplate.getTempContent().replace("{url}", BaiduDwz.createShortUrl(articlesReleaseMsgReqDTO.getUrl(), baiduDwzToken));
		}
		sendMsgReqDTO.setContents(smsContent);
		Integer sendState = SmsRecordSateEnum.INIT.getCode();
		SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
		smtMsgRecord.setTempId(smtMsgTemplate.getId());
		smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
		smtMsgRecord.setMsgObject(articlesReleaseMsgReqDTO.getPhone());
		smtMsgRecord.setMsgContent(smsContent);
		smtMsgRecord.setMsgState(sendState);
		// 发送短信短信，记录短信发送记录
		processSendMsg(sendSmsVo, sendMsgReqDTO, smtMsgRecord);
		return true;
	}

	@Override
	public Boolean sendMessage(SendMsgReqDTO reqDTO) {
		SendSmsVo sendSmsVo = null;
		SmtMsgTemplate smtMsgTemplate = msgTemplateService.selectByTempCode(reqDTO.getTempCode());
		if (Objects.isNull(smtMsgTemplate)) {
			throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
		}
		Integer sendState = SmsRecordSateEnum.INIT.getCode();
		SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
		smtMsgRecord.setTempId(smtMsgTemplate.getId());
		smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
		smtMsgRecord.setMsgObject(reqDTO.getNumber());
		smtMsgRecord.setMsgContent(reqDTO.getContents());
		smtMsgRecord.setMsgState(sendState);
		// 发送短信短信，记录短信发送记录
		processSendMsg(sendSmsVo, reqDTO, smtMsgRecord);
		return true;
	}

	private String creatSmsErrorContent(SendSmsErrorReqDTO sendSmsErrorAo, SmtMsgTemplate smtMsgTemplate) {
		// TODO Auto-generated method stub
		String smsContent = null;
		smsContent = smtMsgTemplate.getTempContent().replace("{手机号码}", sendSmsErrorAo.getPhoneNumber()).replace("{短信模板名称}", sendSmsErrorAo.getTempNameError()).replace("{备注}", sendSmsErrorAo.getRemark());
		return smsContent;
	}

}
