package com.tce.smart.platform.service.admittance.impl;

import com.tce.smart.app.api.dto.InternalSmsVerifyReqDTO;
import com.tce.smart.app.api.feign.RemoteAppSmsService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceCarApplyReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.VisitorTruckSmsVerifyReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorTruckProofRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.VisitorTruckProofService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 货车预约短时 proof 的 Redis 实现。
 *
 * proof 只能在短信本人核验成功后签发，选项查询只验证不消费；提交通过 Lua 在比较
 * 已规范化手机号成功后删除，防止并发重放和跨手机号提交。
 */
@Service
public class VisitorTruckProofServiceImpl implements VisitorTruckProofService {
	private static final String PROOF_KEY_PREFIX = "smart:admittance:visitor-truck:proof:";
	private static final long PROOF_TTL_MINUTES = 5L;
	private static final int PROOF_TOKEN_BYTES = 32;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final DefaultRedisScript<String> CONSUME_OWNER_PROOF_SCRIPT = new DefaultRedisScript<>(
			"local value = redis.call('get', KEYS[1]); "
					+ "if value and value == ARGV[1] then redis.call('del', KEYS[1]); return value; end; return nil;",
			String.class);

	private final RemoteAppSmsService remoteAppSmsService;
	private final StringRedisTemplate redisTemplate;
	private final SmtAdmittanceApplyService smtAdmittanceApplyService;
	private final Supplier<String> proofTokenSupplier;

	public VisitorTruckProofServiceImpl(RemoteAppSmsService remoteAppSmsService, StringRedisTemplate redisTemplate,
			SmtAdmittanceApplyService smtAdmittanceApplyService) {
		this(remoteAppSmsService, redisTemplate, smtAdmittanceApplyService, VisitorTruckProofServiceImpl::newProofToken);
	}

	VisitorTruckProofServiceImpl(RemoteAppSmsService remoteAppSmsService, StringRedisTemplate redisTemplate,
			SmtAdmittanceApplyService smtAdmittanceApplyService, Supplier<String> proofTokenSupplier) {
		this.remoteAppSmsService = remoteAppSmsService;
		this.redisTemplate = redisTemplate;
		this.smtAdmittanceApplyService = smtAdmittanceApplyService;
		this.proofTokenSupplier = proofTokenSupplier;
	}

	@Override
	public VisitorTruckProofRespDTO verifySms(VisitorTruckSmsVerifyReqDTO request) {
		String mobile = request == null ? null : normalizeMobile(request.getMobile());
		String smsCode = request == null ? null : trim(request.getSmsCode());
		if (!StringUtils.hasText(mobile) || !StringUtils.hasText(smsCode)) {
			throw proofInvalid();
		}
		InternalSmsVerifyReqDTO verifyRequest = new InternalSmsVerifyReqDTO();
		verifyRequest.setMobile(mobile);
		verifyRequest.setSmsCode(smsCode);
		Result<Boolean> result = remoteAppSmsService.verifySmsCode(verifyRequest, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (result == null || !result.isSuccess() || !Boolean.TRUE.equals(result.getData())) {
			throw proofInvalid();
		}
		String proofToken = trim(proofTokenSupplier.get());
		if (!StringUtils.hasText(proofToken)) {
			throw new SmartException("货车预约身份服务不可用");
		}
		redisTemplate.opsForValue().set(proofKey(proofToken), mobile, PROOF_TTL_MINUTES, TimeUnit.MINUTES);
		VisitorTruckProofRespDTO response = new VisitorTruckProofRespDTO();
		response.setProof(proofToken);
		return response;
	}

	@Override
	public void assertActiveProof(String proofToken) {
		String proofMobile = redisTemplate.opsForValue().get(proofKey(requireProofToken(proofToken)));
		if (!StringUtils.hasText(proofMobile)) {
			throw proofInvalid();
		}
	}

	@Override
	public SmtAdmittanceApply apply(String proofToken, SaveAdmittanceCarApplyReqDTO request) {
		if (request == null) {
			throw proofInvalid();
		}
		String mobile = normalizeMobile(request.getVisitorPhone());
		if (!StringUtils.hasText(mobile)) {
			throw proofInvalid();
		}
		String consumedMobile = redisTemplate.execute(CONSUME_OWNER_PROOF_SCRIPT,
				Collections.singletonList(proofKey(requireProofToken(proofToken))), mobile);
		if (!StringUtils.hasText(consumedMobile)) {
			throw proofInvalid();
		}
		// 持久化使用与 proof 一致的规范化手机号，避免空白字符造成所有权绕过。
		request.setVisitorPhone(mobile);
		return smtAdmittanceApplyService.saveAdmittanceCarApply(request);
	}

	private static String newProofToken() {
		byte[] bytes = new byte[PROOF_TOKEN_BYTES];
		SECURE_RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String requireProofToken(String proofToken) {
		String normalized = trim(proofToken);
		if (!StringUtils.hasText(normalized)) {
			throw proofInvalid();
		}
		return normalized;
	}

	private String proofKey(String proofToken) {
		return PROOF_KEY_PREFIX + proofToken;
	}

	private String normalizeMobile(String mobile) {
		return trim(mobile).replaceAll("\\s+", "");
	}

	private String trim(String value) {
		return value == null ? "" : value.trim();
	}

	private SmartException proofInvalid() {
		return new SmartException("货车预约身份验证已失效，请重新验证");
	}
}
