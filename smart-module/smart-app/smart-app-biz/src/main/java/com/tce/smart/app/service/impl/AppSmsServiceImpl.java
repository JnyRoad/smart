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
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
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
	/** 访客 OTP 与通用、登录 OTP 分开存储，避免跨匿名场景重放。 */
	private static final String VISITOR_SMS_CODE_KEY = "smart_app:visitor:sms:code:";
	private static final String VISITOR_SMS_VERIFY_FAILURE_KEY = "smart_app:visitor:sms:verify-failure:";
	private static final String PHONE_CHANGE_SMS_KEY = "smart_app:phone-change:sms:";
	private static final String PHONE_CHANGE_OLD_STAGE = "old";
	private static final String PHONE_CHANGE_NEW_STAGE = "new";
	private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
	/** 专用换绑 OTP 必须 compare-and-delete；任何重放或跨账号/阶段使用均返回失败。 */
	private static final DefaultRedisScript<Long> CONSUME_PHONE_CHANGE_SMS = new DefaultRedisScript<>(
			"local value = redis.call('get', KEYS[1]); if not value then return 0; end; "
					+ "local state = cjson.decode(value); if state['smsCode'] ~= ARGV[1] then return 0; end; "
					+ "redis.call('del', KEYS[1]); return 1;", Long.class);
	/**
	 * 在单个 Redis 原子操作中检查失败阈值、比对 OTP、消费成功 OTP 并记录失败。
	 * 先检查计数可防止攻击者在第六次尝试使用正确验证码绕过上限；成功时同时清理失败计数，
	 * 新的验证码生命周期不会继承旧验证码的错误尝试。
	 */
	private static final DefaultRedisScript<Long> VERIFY_AND_CONSUME_VISITOR_SMS = new DefaultRedisScript<>(
			"local failures = tonumber(redis.call('get', KEYS[2]) or '0'); "
					+ "if failures >= tonumber(ARGV[2]) then return -1; end; "
					+ "local value = redis.call('get', KEYS[1]); "
					+ "if value then local parsed, payload = pcall(cjson.decode, value); "
					+ "if parsed and payload['smsCode'] == ARGV[1] then redis.call('del', KEYS[1]); "
					+ "redis.call('del', KEYS[2]); return 1; end; end; "
					+ "local next = redis.call('incr', KEYS[2]); "
					+ "if next == 1 then redis.call('expire', KEYS[2], tonumber(ARGV[3])); end; return 0;", Long.class);

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private RemoteSmsManageService remoteSmsManageService;

	@SuppressWarnings({"rawtypes" })
	@Override
	public Boolean sendSmsCode(String mobile) {
		return sendSmsCodeWithKey(mobile, RedisKeyConstants.SMAT_APP_WECHAT_SMSCODE + mobile);
	}

	/** 通用及换绑专用短信共享供应商调用，但各自传入隔离的验证码 Redis 键。 */
	@SuppressWarnings({"rawtypes" })
	private Boolean sendSmsCodeWithKey(String mobile, String redisKey) {
		String smsCode = RandomUtil.randomNumbers(MESSAGE_LENGTH);// 短信验证码
		Map<String, Object> smsCodeMap = new HashMap<>();
		smsCodeMap.put("smsCode", smsCode);
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

	@Override
	public Boolean sendPhoneChangeSmsCode(Integer userId, String stage, String mobile) {
		String normalizedMobile = requireMobile(mobile);
		String redisKey = phoneChangeSmsKey(requirePhoneChangeUserId(userId), requirePhoneChangeStage(stage), normalizedMobile);
		return sendSmsCodeWithKey(normalizedMobile, redisKey);
	}

	@Override
	public Boolean consumePhoneChangeSmsCode(Integer userId, String stage, String mobile, String smsCode) {
		if (StringUtils.isEmpty(smsCode) || !smsCode.matches("^\\d{6}$")) {
			throw new TCEException("验证码错误或已过期");
		}
		String redisKey = phoneChangeSmsKey(requirePhoneChangeUserId(userId), requirePhoneChangeStage(stage),
				requireMobile(mobile));
		Long consumed = stringRedisTemplate.execute(CONSUME_PHONE_CHANGE_SMS, Collections.singletonList(redisKey), smsCode);
		if (!Long.valueOf(1L).equals(consumed)) {
			throw new TCEException("验证码错误或已过期");
		}
		return Boolean.TRUE;
	}

	/** 换绑 OTP 的 Redis 键仅保存手机号摘要，避免手机号出现在键空间或运维扫描结果中。 */
	private String phoneChangeSmsKey(Integer userId, String stage, String mobile) {
		return PHONE_CHANGE_SMS_KEY + userId + ":" + stage + ":" + phoneFingerprint(mobile);
	}

	private Integer requirePhoneChangeUserId(Integer userId) {
		if (userId == null || userId <= 0) {
			throw new TCEException("当前登录员工信息缺失");
		}
		return userId;
	}

	private String requirePhoneChangeStage(String stage) {
		if (!PHONE_CHANGE_OLD_STAGE.equals(stage) && !PHONE_CHANGE_NEW_STAGE.equals(stage)) {
			throw new TCEException("短信验证阶段无效");
		}
		return stage;
	}

	private String phoneFingerprint(String mobile) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(mobile.getBytes(StandardCharsets.UTF_8));
			StringBuilder result = new StringBuilder(digest.length * 2);
			for (byte value : digest) {
				result.append(String.format("%02x", value));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new TCEException("验证码状态初始化失败");
		}
	}

	/**
	 * 匿名访客短信仅允许该显式场景使用。手机号冷却期内返回同一受理结果，
	 * 既避免重复计费/轰炸，也不把限流状态暴露给攻击者。
	 */
	@Override
	public Boolean sendVisitorSmsCode(String mobile) {
		return sendPublicSmsCode(mobile, VISITOR_SMS_SEND_RATE_KEY, VISITOR_SMS_CODE_KEY,
				VISITOR_SMS_VERIFY_FAILURE_KEY);
	}

	/**
	 * 手机号登录保留独立限流命名空间，防止某一访客流程消耗或重置登录流程的冷却状态。
	 */
	@Override
	public Boolean sendLoginSmsCode(String mobile) {
		return sendPublicSmsCode(mobile, LOGIN_SMS_SEND_RATE_KEY, RedisKeyConstants.SMAT_APP_WECHAT_SMSCODE, null);
	}

	/**
	 * 所有匿名发送场景共用的受理逻辑。调用方只能传入代码常量，不能由外部请求指定场景或 Redis 键。
	 */
	private Boolean sendPublicSmsCode(String mobile, String rateKeyPrefix, String smsCodeKeyPrefix,
			String verifyFailureKeyPrefix) {
		String normalizedMobile = requireMobile(mobile);
		String rateKey = rateKeyPrefix + normalizedMobile;
		Boolean accepted = stringRedisTemplate.opsForValue().setIfAbsent(
				rateKey, "1", VISITOR_SMS_SEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);
		if (!Boolean.TRUE.equals(accepted)) {
			return Boolean.TRUE;
		}
		try {
			Boolean sent = sendSmsCodeWithKey(normalizedMobile, smsCodeKeyPrefix + normalizedMobile);
			if (verifyFailureKeyPrefix != null) {
				// 仅在实际下发新 OTP 后清除旧 OTP 的失败计数，冷却期内请求不能借机重置计数。
				stringRedisTemplate.delete(verifyFailureKeyPrefix + normalizedMobile);
			}
			return sent;
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
		Long verified = stringRedisTemplate.execute(VERIFY_AND_CONSUME_VISITOR_SMS,
				Arrays.asList(VISITOR_SMS_CODE_KEY + normalizedMobile, VISITOR_SMS_VERIFY_FAILURE_KEY + normalizedMobile),
				smsCode, VISITOR_SMS_MAX_VERIFY_ATTEMPTS, VISITOR_SMS_VERIFY_WINDOW_SECONDS);
		if (!Long.valueOf(1L).equals(verified)) {
			// 对匿名调用统一错误，不能区分手机号、验证码、有效期和失败次数状态。
			throw new TCEException("验证码错误或已过期");
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
