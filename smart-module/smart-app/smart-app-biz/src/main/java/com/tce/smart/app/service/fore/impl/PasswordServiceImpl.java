package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.algorithm.api.dto.req.CompareDTO;
import com.tce.smart.algorithm.api.dto.req.CompareImageDTO;
import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.api.enums.FaceTypeEnum;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.app.api.entity.AppUserDevice;
import com.tce.smart.app.service.AppSmsService;
import com.tce.smart.app.service.fore.DeviceManageService;
import com.tce.smart.app.service.fore.PasswordService;
import com.tce.smart.app.vo.fore.ChackFacePwdVo;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.EncDecryUtils;
import com.tce.smart.common.core.util.UUIDUtils;
import com.tce.smart.platform.api.dto.resp.InternalStaffPasswordRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffPhoneRespDTO;
import com.tce.smart.platform.api.feign.RemoteSmtImageService;
import com.tce.smart.platform.api.feign.RemoteStaffInternalService;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.Collections;

/**
 * 密码服务实现类
 *
 * @author mckaywu
 * @date 2019-06-15 16:11:47
 */
@Service
@Slf4j
public class PasswordServiceImpl implements PasswordService {

	private static final String PASSWORD_RESET_PURPOSE = "password-reset";
	private static final String FACE_PASSWORD_RESET_PURPOSE = "face-password-reset";
	private static final String CHALLENGE_KEY_PREFIX = "smart_app:auth:password:challenge:";
	private static final String CHALLENGE_RATE_KEY_PREFIX = "smart_app:auth:password:challenge:rate:";
	private static final long CHALLENGE_TTL_SECONDS = 600;
	private static final long CHALLENGE_RATE_TTL_SECONDS = 60;
	private static final int MAX_CHALLENGE_REQUESTS_PER_MINUTE = 5;
	private static final int MAX_SMS_SEND_ATTEMPTS = 3;
	private static final int MAX_VERIFY_ATTEMPTS = 5;
	private static final DefaultRedisScript<Long> COMPARE_AND_DELETE = new DefaultRedisScript<>(
			"local value = redis.call('get', KEYS[1]); if value == ARGV[1] then redis.call('del', KEYS[1]); return 1; end; return 0;",
			Long.class);
	private static final DefaultRedisScript<String> RESERVE_SMS_SEND_ATTEMPT = new DefaultRedisScript<>(
			"local value = redis.call('get', KEYS[1]); "
					+ "if not value then return nil; end; "
					+ "local challenge = cjson.decode(value); "
					+ "if challenge['purpose'] ~= ARGV[1] or challenge['active'] ~= true "
					+ "or (tonumber(challenge['sendAttempts']) or 0) >= tonumber(ARGV[2]) then return nil; end; "
					+ "challenge['sendAttempts'] = (tonumber(challenge['sendAttempts']) or 0) + 1; "
					+ "local updated = cjson.encode(challenge); local ttl = redis.call('ttl', KEYS[1]); "
					+ "if ttl > 0 then redis.call('setex', KEYS[1], ttl, updated); else redis.call('set', KEYS[1], updated); end; "
					+ "return updated;",
			String.class);

	@Autowired
	private AppSmsService appSmsService;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private RemoteStaffInternalService remoteStaffInternalService;

	@Autowired
	private RemoteAlgorithmService remoteAlgorithmService;

	@Autowired
	private RemoteSmtImageService remoteSmtImageService;

	@Autowired
	private DeviceManageService deviceManageService;

	@Value("${spring.face.forget-password}")
	private Double compareValue;

	@Value("${security.auth-code.encode-key:}")
	private String authCodeEncodeKey;

	@Override
	public String createPasswordResetChallenge(String badge) {
		String challengeId = UUID.randomUUID().toString().replace("-", "");
		Map<String, Object> challenge = new HashMap<>();
		challenge.put("purpose", PASSWORD_RESET_PURPOSE);
		challenge.put("badge", StringUtils.defaultString(badge));
		challenge.put("sendAttempts", 0);
		challenge.put("verifyAttempts", 0);

		Long requests = stringRedisTemplate.opsForValue().increment(CHALLENGE_RATE_KEY_PREFIX
				+ DigestUtil.sha256Hex(StringUtils.defaultString(badge)), 1);
		if (Long.valueOf(1L).equals(requests)) {
			stringRedisTemplate.expire(CHALLENGE_RATE_KEY_PREFIX + DigestUtil.sha256Hex(StringUtils.defaultString(badge)),
					CHALLENGE_RATE_TTL_SECONDS, TimeUnit.SECONDS);
		}
		InternalStaffPhoneRespDTO phone = requests != null && requests <= MAX_CHALLENGE_REQUESTS_PER_MINUTE
				? findPasswordPhone(badge) : null;
		// active 只保存在服务端。对客户端而言，无论工号是否存在和是否触发限流，响应完全一致。
		challenge.put("active", phone != null && StringUtils.isNotBlank(phone.getPhone()));
		challenge.put("phone", phone == null ? "" : phone.getPhone());
		writeChallenge(challengeId, challenge);
		return challengeId;
	}

	@Override
	public Boolean sendSmsCode(String challengeId) {
		String reservedChallengeSource = reserveSmsSendAttempt(challengeId);
		Map<String, Object> challenge = parseChallenge(reservedChallengeSource);
		if (challenge == null) {
			return Boolean.TRUE;
		}
		try {
			// 手机号只在服务端 challenge 中使用，客户端既不能提交，也不会得到脱敏或完整值。
			appSmsService.sendSmsCode(String.valueOf(challenge.get("phone")));
		} catch (Exception e) {
			// 保持抗枚举响应；告警日志不包含工号、手机号或 challenge。
			log.warn("找回密码短信下发失败 scene=password-reset");
		}
		return Boolean.TRUE;
	}

	/**
	 * 通过 Lua 原子预占一次短信下发次数，避免多个并发请求同时读取旧计数后重复下发。
	 */
	private String reserveSmsSendAttempt(String challengeId) {
		if (StringUtils.isBlank(challengeId)) {
			return null;
		}
		try {
			return stringRedisTemplate.execute(RESERVE_SMS_SEND_ATTEMPT,
					Collections.singletonList(CHALLENGE_KEY_PREFIX + challengeId), PASSWORD_RESET_PURPOSE,
					String.valueOf(MAX_SMS_SEND_ATTEMPTS));
		} catch (Exception e) {
			// Redis 不可用时不下发短信，防止跳过次数控制而放大短信轰炸风险。
			log.warn("找回密码短信预占失败 scene=password-reset");
			return null;
		}
	}

	@Override
	public String verifySmsCode(String challengeId, String smsCode) {
		String challengeSource = readChallengeSource(challengeId);
		Map<String, Object> challenge = parseChallenge(challengeSource);
		if (!isActivePasswordChallenge(challenge) || number(challenge, "verifyAttempts") >= MAX_VERIFY_ATTEMPTS) {
			throw new TCEException("验证码校验失败");
		}
		boolean verified;
		try {
			verified = Boolean.TRUE.equals(appSmsService.verifySmsCode(String.valueOf(challenge.get("phone")), smsCode));
		} catch (Exception e) {
			verified = false;
		}
		if (!verified) {
			challenge.put("verifyAttempts", number(challenge, "verifyAttempts") + 1);
			writeChallenge(challengeId, challenge);
			throw new TCEException("验证码校验失败");
		}

		// 比较后删除：验证码错误不会消耗合法 challenge；并发成功请求中仅一个能兑换授权。
		Long consumed = stringRedisTemplate.execute(COMPARE_AND_DELETE, Collections.singletonList(CHALLENGE_KEY_PREFIX + challengeId),
				challengeSource);
		if (!Long.valueOf(1L).equals(consumed)) {
			throw new TCEException("验证码校验失败");
		}
		String verifySuccessCode = saveRedisCode(String.valueOf(challenge.get("badge")), PASSWORD_RESET_PURPOSE, challengeId);
		return URLEncoder.encode(EncDecryUtils.encryptByJasypt(verifySuccessCode, authCodeEncodeKey));
	}

	@Override
	public ChackFacePwdVo verifyFace(String facePhoto, String deviceNo) {
		if (StringUtil.isNullOrEmpty(facePhoto) || StringUtil.isNullOrEmpty(deviceNo)) {
			throw new TCEException("参数不全");
		}

		List<AppUserDevice> userDeviceList = deviceManageService.queryByDeviceNo(deviceNo);
		if (CollectionUtils.isEmpty(userDeviceList)) {
			throw new TCEException("获取设备信息异常");
		}
		//只取最近登录过的，优先已绑定的
		AppUserDevice appUserDevice = userDeviceList.get(0);

		Result<InternalStaffPasswordRespDTO> passwordStaffResponse;
		String badge;
		String staffFaceImgId;
		try {
			// 远程调用查询员工信息
			passwordStaffResponse = remoteStaffInternalService.getPasswordStaff(appUserDevice.getBadge(),
					SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, "password-face-verify");
			if (!passwordStaffResponse.isSuccess() || Objects.isNull(passwordStaffResponse.getData())) {
				throw new TCEException("查询员工信息异常");
			}

			badge = passwordStaffResponse.getData().getBadge();
			staffFaceImgId = passwordStaffResponse.getData().getFacePicId();

			//下载员工人脸图片
			Result<String> getImageBase64Rs = remoteSmtImageService.getImageBase64ByCode(staffFaceImgId, SecurityConstants.FROM_IN);
			log.info("找回密码人脸图片读取完成 scene=forget-password success={}", getImageBase64Rs.isSuccess());
			if (!getImageBase64Rs.isSuccess() || StringUtil.isNullOrEmpty(getImageBase64Rs.getData())) {
				throw new TCEException("下载员工人脸图异常");
			}

			CompareDTO compareDTO = new CompareDTO();
			CompareImageDTO compareImageA = new CompareImageDTO();
			compareImageA.setImageBase64(facePhoto);
			compareImageA.setFaceType(FaceTypeEnum.LIVE.getType());
			CompareImageDTO compareImageB = new CompareImageDTO();
			compareImageB.setImageBase64(getImageBase64Rs.getData());
			compareImageB.setFaceType(FaceTypeEnum.LIVE.getType());
			compareDTO.setCompareImageA(compareImageA);
			compareDTO.setCompareImageB(compareImageB);

			// 人脸照片1:1对比
			//log.info("verifyFace 对比请求参数:[{}]",JSONUtil.toJsonStr(compareDTO));
			Result<com.tce.smart.algorithm.api.dto.resp.CompareDTO> result = remoteAlgorithmService.compare(UUIDUtils.create(),
					AlgorithmTypeEnum.COMPARE_FACEALL.getType(), compareDTO, SecurityConstants.FROM_IN);
			log.info("找回密码人脸比对完成 scene=forget-password success={}", result.isSuccess());

			if (result.isSuccess()) {
				//小于阀值则认为不是本人
				if (-1 == (new BigDecimal(String.valueOf(result.getData()))
						.compareTo(new BigDecimal(compareValue)))) {
					throw new TCEException("人脸不匹配,请重新拍照");
				}
			}
		} catch (TCEException tce) {
			throw tce;
		} catch (Exception e) {
			log.error("获取员工信息异常", e);
			throw new TCEException("获取员工信息异常");
		}
		String verifySuccessCode = saveRedisCode(badge, FACE_PASSWORD_RESET_PURPOSE,
				"face-verified-" + UUID.randomUUID().toString().replace("-", ""));

		ChackFacePwdVo chackFacePwdVo = new ChackFacePwdVo();
		chackFacePwdVo.setUsername(badge);
		chackFacePwdVo.setPwdUpdateAuthCode(URLEncoder.encode(EncDecryUtils.encryptByJasypt(verifySuccessCode, authCodeEncodeKey)));
		return chackFacePwdVo;
	}

	/**
	 * 生成校验码，并存放redis
	 * @param badge 员工号
	 * @return 校验码
	 */
	private String saveRedisCode(String badge, String purpose, String verifiedChallengeId) {
		// 授权码
		String verifySuccessCode = RandomUtil.randomStringUpper(6);

		Map<String, Object> authCodeMap = new HashMap<>();
		authCodeMap.put(SecurityConstants.PWD_UPDATE_AUTHCODE_SUB_KEY, verifySuccessCode);
		authCodeMap.put("purpose", purpose);
		authCodeMap.put("verifiedChallengeId", verifiedChallengeId);
		String pwdUpdateAuthCodeKey = SecurityConstants.APP_PWD_UPDATE_AUTHCODE + badge;

		// 存放redis
		stringRedisTemplate.opsForValue().set(pwdUpdateAuthCodeKey, JSONUtil.toJsonStr(authCodeMap), 300,
				TimeUnit.SECONDS);// 5分钟分钟失效
		return verifySuccessCode;
	}

	/**
	 * 检查设备是否已绑定
	 *
	 * @param deviceNo 设备号
	 * @param badge    员工工号
	 * @return true-已绑定,false-未绑定
	 */
	private boolean checkDevcieBind(String deviceNo, String badge) {
		boolean isBind = false;
		List<AppUserDevice> bindDeviceList = deviceManageService.queryBindDevice(badge);
		if (CollectionUtils.isNotEmpty(bindDeviceList)) {
			for (AppUserDevice tempUserDevice : bindDeviceList) {
				if (deviceNo.equals(tempUserDevice.getDeviceNo())) {
					isBind = true;
					break;
				}
			}
		}

		return isBind;
	}

	private InternalStaffPhoneRespDTO findPasswordPhone(String badge) {
		try {
			Result<InternalStaffPhoneRespDTO> result = remoteStaffInternalService.getPasswordPhone(badge,
					SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, PASSWORD_RESET_PURPOSE);
			return result.isSuccess() && result.getData() != null && StringUtils.isNotBlank(result.getData().getPhone())
					? result.getData() : null;
		} catch (Exception e) {
			log.warn("找回密码员工资料查询失败 scene=password-reset");
			return null;
		}
	}

	private Map<String, Object> readChallenge(String challengeId) {
		return parseChallenge(readChallengeSource(challengeId));
	}

	private String readChallengeSource(String challengeId) {
		if (StringUtils.isBlank(challengeId)) {
			return null;
		}
		return stringRedisTemplate.opsForValue().get(CHALLENGE_KEY_PREFIX + challengeId);
	}

	private Map<String, Object> parseChallenge(String source) {
		if (StringUtils.isBlank(source)) {
			return null;
		}
		JSONObject object = JSONUtil.parseObj(source);
		Map<String, Object> challenge = new HashMap<>();
		for (String key : object.keySet()) {
			challenge.put(key, object.get(key));
		}
		return challenge;
	}

	private void writeChallenge(String challengeId, Map<String, Object> challenge) {
		stringRedisTemplate.opsForValue().set(CHALLENGE_KEY_PREFIX + challengeId, JSONUtil.toJsonStr(challenge),
				CHALLENGE_TTL_SECONDS, TimeUnit.SECONDS);
	}

	private boolean isActivePasswordChallenge(Map<String, Object> challenge) {
		return challenge != null && PASSWORD_RESET_PURPOSE.equals(challenge.get("purpose"))
				&& Boolean.TRUE.equals(challenge.get("active"));
	}

	private int number(Map<String, Object> challenge, String key) {
		Object value = challenge.get(key);
		return value instanceof Number ? ((Number) value).intValue() : 0;
	}

}
