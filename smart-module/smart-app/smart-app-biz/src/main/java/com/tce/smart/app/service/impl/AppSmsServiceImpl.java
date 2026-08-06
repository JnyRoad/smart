package com.tce.smart.app.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.app.service.AppSmsService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.data.api.dto.msg.req.SendSmsCodeMsgReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendSmsErrorReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.tool.constant.RedisKeyConstants;
import com.tce.smart.tool.enums.SmsTemplateEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务接口
 *
 * @author mkwu
 * @date 2019-05-20
 */
@Service
@Slf4j
public class AppSmsServiceImpl implements AppSmsService {
	private static final Integer MESSAGE_LENGTH = 6;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private RemoteSmsManageService remoteSmsManageService;

	@SuppressWarnings({"rawtypes" })
	@Override
	public Boolean sendSmsCode(String mobile) {

		String smsCode = RandomUtil.randomNumbers(MESSAGE_LENGTH);// 短信验证码
		Map<String, Object> smsCodeMap = new HashMap<>();
		smsCodeMap.put("smsCode", smsCode);
		String redisKey = RedisKeyConstants.SMAT_APP_WECHAT_SMSCODE + mobile;
		stringRedisTemplate.opsForValue().set(redisKey, JSONUtil.toJsonStr(smsCodeMap), 600, TimeUnit.SECONDS);// 10分钟失效
		SendSmsCodeMsgReqDTO codeMsg = new SendSmsCodeMsgReqDTO();
		codeMsg.setName(mobile);
		codeMsg.setNumber(mobile);
		codeMsg.setSmsCode(smsCode);
		codeMsg.setTempCode(SmsTemplateEnum.SMSCODE_4001.getCode());
		// 安全：日志只记录脱敏手机号，绝不打印验证码明文（CWE-532，避免 debug 级日志泄露验证码）
		log.debug("发送短信验证码，手机号：{}", maskMobile(mobile));
		// 调用feign接口发送短信验证码
		Result result = remoteSmsManageService.sendSmsCode(codeMsg);
		if (!result.isSuccess()) {
			String errorMsg = result.getMsg();

			log.debug("发送短信验证码失败，手机号：{},", maskMobile(mobile));

			SendSmsErrorReqDTO sendSmsErrorAo=new SendSmsErrorReqDTO();
			sendSmsErrorAo.setPhoneNumber(mobile);
			sendSmsErrorAo.setTempCode(SmsTemplateEnum.SMS_12001.getCode());
			sendSmsErrorAo.setTempNameError(SmsTemplateEnum.SMSCODE_4001.getDesc());
			sendSmsErrorAo.setRemark(errorMsg);
			log.info("remoteSmsManageService:"+remoteSmsManageService);
			Result sendSmsError = remoteSmsManageService.sendSmsError(sendSmsErrorAo);
			log.info("remoteSmsManageService.sendSmsError Result={} :"+sendSmsError);

			throw new TCEException(result.getCode(), errorMsg);
		}

		// 安全：成功日志同样只记录脱敏手机号，不打印验证码
		log.debug("发送短信验证码成功，手机号：{}", maskMobile(mobile));
		return Boolean.TRUE;

	}

	@Override
	public Boolean verifySmsCode(String mobile, String smsCode) {
		String redisKey = RedisKeyConstants.SMAT_APP_WECHAT_SMSCODE + mobile;
		String smsCodeObject = stringRedisTemplate.opsForValue().get(redisKey);
		if (StringUtils.isEmpty(smsCodeObject)) {
			log.error("获取短信验证码异常");
			throw new TCEException("验证码已过期");
		}

		JSONObject smsCodeJson = (JSONObject) JSONUtil.parse(smsCodeObject);
		if (!smsCodeJson.get("smsCode").equals(smsCode)) {
			throw new TCEException("验证码已过期");
		}

		return Boolean.TRUE;
	}

	@Override
	public String sendAndGetSmsCode(String mobile) {
	// TODO Auto-generated method stub
		String smsCode = RandomUtil.randomNumbers(MESSAGE_LENGTH);// 短信验证码
		Map<String, Object> smsCodeMap = new HashMap<>();
		smsCodeMap.put("smsCode", smsCode);
		String redisKey = RedisKeyConstants.SMAT_APP_WECHAT_SMSCODE + mobile;
		stringRedisTemplate.opsForValue().set(redisKey, JSONUtil.toJsonStr(smsCodeMap), 600, TimeUnit.SECONDS);// 10分钟失效
		SendSmsCodeMsgReqDTO codeMsg = new SendSmsCodeMsgReqDTO();
		codeMsg.setName(mobile);
		codeMsg.setNumber(mobile);
		codeMsg.setSmsCode(smsCode);
		codeMsg.setTempCode(SmsTemplateEnum.SMSCODE_4001.getCode());
		// 安全：日志只记录脱敏手机号，绝不打印验证码明文（CWE-532，避免 debug 级日志泄露验证码）
		log.debug("发送短信验证码，手机号：{}", maskMobile(mobile));
		// 调用feign接口发送短信验证码
		Result result = remoteSmsManageService.sendSmsCode(codeMsg);
		if (!result.isSuccess()) {
			String errorMsg = result.getMsg();

			log.debug("发送短信验证码失败，手机号：{},", maskMobile(mobile));

			SendSmsErrorReqDTO sendSmsErrorAo=new SendSmsErrorReqDTO();
			sendSmsErrorAo.setPhoneNumber(mobile);
			sendSmsErrorAo.setTempCode(SmsTemplateEnum.SMS_12001.getCode());
			sendSmsErrorAo.setTempNameError(SmsTemplateEnum.SMSCODE_4001.getDesc());
			sendSmsErrorAo.setRemark(errorMsg);
			log.info("remoteSmsManageService:"+remoteSmsManageService);
			Result sendSmsError = remoteSmsManageService.sendSmsError(sendSmsErrorAo);
			log.info("remoteSmsManageService.sendSmsError Result={} :"+sendSmsError);

			throw new TCEException(result.getCode(), errorMsg);
		}

		// 安全：成功日志同样只记录脱敏手机号，不打印验证码
		log.debug("发送短信验证码成功，手机号：{}", maskMobile(mobile));
		return null;
	}

	/**
	 * 手机号脱敏：保留前 3 位和后 4 位，中间 4 位用 * 掩码（如 138****8888）。
	 *
	 * <p>用于日志输出，避免完整手机号落盘（个人信息保护）。仅做展示脱敏，
	 * 不改变业务侧实际使用的 mobile 值。</p>
	 *
	 * @param mobile 原始手机号；为空或长度不足 7 位时按掩码常量处理，避免越界
	 * @return 脱敏后的手机号字符串
	 */
	static String maskMobile(String mobile) {
		if (mobile == null || mobile.length() < 7) {
			// 异常/短号场景统一返回固定掩码，既不泄露也不抛错（日志路径需保证不影响主流程）
			return "***";
		}
		return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
	}

}
