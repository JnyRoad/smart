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
import java.util.regex.Pattern;

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
	/** 匿名访客同一手机号的最短重发间隔，和 H5 倒计时保持一致。 */
	private static final long VISITOR_SMS_SEND_COOLDOWN_SECONDS = 60L;
	/** 单个验证码有效期内最多允许的错误校验次数，避免六位码在线撞库。 */
	private static final long VISITOR_SMS_MAX_VERIFY_ATTEMPTS = 5L;
	/** 验证失败计数窗口与验证码有效期一致。 */
	private static final long VISITOR_SMS_VERIFY_WINDOW_SECONDS = 600L;
	private static final String VISITOR_SMS_SEND_RATE_KEY = "smart_app:visitor:sms:send:";
	private static final String LOGIN_SMS_SEND_RATE_KEY = "smart_app:login:sms:send:";
	private static final String VISITOR_SMS_VERIFY_RATE_KEY = "smart_app:visitor:sms:verify:";
	private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

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
			Result sendSmsError = remoteSmsManageService.sendSmsError(sendSmsErrorAo);
			log.info("短信验证码供应商失败已上报 scene=app-sms success={}", sendSmsError != null && sendSmsError.isSuccess());

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

	/**
	 * 匿名访客短信仅允许该显式场景使用。手机号冷却期内返回同一受理结果，
	 * 既避免重复计费/轰炸，也不把限流状态暴露给攻击者。
	 */
	@Override
	public Boolean sendVisitorSmsCode(String mobile) {
		return sendPublicSmsCode(mobile, VISITOR_SMS_SEND_RATE_KEY);
	}

	/**
	 * 手机号登录保留独立限流命名空间，防止某一访客流程消耗或重置登录流程的冷却状态。
	 */
	@Override
	public Boolean sendLoginSmsCode(String mobile) {
		return sendPublicSmsCode(mobile, LOGIN_SMS_SEND_RATE_KEY);
	}

	/**
	 * 所有匿名发送场景共用的受理逻辑。调用方只能传入代码常量，不能由外部请求指定场景或 Redis 键。
	 */
	private Boolean sendPublicSmsCode(String mobile, String rateKeyPrefix) {
		String normalizedMobile = requireMobile(mobile);
		String rateKey = rateKeyPrefix + normalizedMobile;
		Boolean accepted = stringRedisTemplate.opsForValue().setIfAbsent(
				rateKey, "1", VISITOR_SMS_SEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);
		if (!Boolean.TRUE.equals(accepted)) {
			return Boolean.TRUE;
		}
		try {
			return sendSmsCode(normalizedMobile);
		} catch (RuntimeException e) {
			// 供应商同步失败时释放本次预约，合法访客可重试；成功或超时不释放，不能放大下发量。
			stringRedisTemplate.delete(rateKey);
			throw new TCEException("短信发送失败，请稍后重试");
		}
	}

	/**
	 * 访客验证码错误计数独立于通用内部校验，避免匿名入口无限尝试。
	 */
	@Override
	public Boolean verifyVisitorSmsCode(String mobile, String smsCode) {
		String normalizedMobile = requireMobile(mobile);
		if (StringUtils.isEmpty(smsCode) || !smsCode.matches("^\\d{6}$")) {
			throw new TCEException("验证码错误或已过期");
		}
		Long attempts = stringRedisTemplate.opsForValue().increment(VISITOR_SMS_VERIFY_RATE_KEY + normalizedMobile, 1L);
		if (attempts != null && attempts == 1L) {
			stringRedisTemplate.expire(VISITOR_SMS_VERIFY_RATE_KEY + normalizedMobile,
					VISITOR_SMS_VERIFY_WINDOW_SECONDS, TimeUnit.SECONDS);
		}
		if (attempts != null && attempts > VISITOR_SMS_MAX_VERIFY_ATTEMPTS) {
			throw new TCEException("验证码错误或已过期");
		}
		try {
			return verifySmsCode(normalizedMobile, smsCode);
		} catch (TCEException e) {
			// 对匿名调用统一错误，不能区分手机号、验证码和有效期的具体状态。
			throw new TCEException("验证码错误或已过期");
		}
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
			Result sendSmsError = remoteSmsManageService.sendSmsError(sendSmsErrorAo);
			log.info("短信验证码供应商失败已上报 scene=app-sms success={}", sendSmsError != null && sendSmsError.isSuccess());

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

	/**
	 * 入口手机号只接受中国大陆 11 位移动号码；拒绝而非纠正输入，避免号码混淆进入 Redis 键或短信供应商。
	 */
	private String requireMobile(String mobile) {
		String normalizedMobile = mobile == null ? null : mobile.trim();
		if (normalizedMobile == null || !MOBILE_PATTERN.matcher(normalizedMobile).matches()) {
			throw new TCEException("短信请求无效");
		}
		return normalizedMobile;
	}

}
