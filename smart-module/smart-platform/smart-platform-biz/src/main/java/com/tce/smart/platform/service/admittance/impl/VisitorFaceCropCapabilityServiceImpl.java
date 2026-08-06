package com.tce.smart.platform.service.admittance.impl;

import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction;
import com.tce.smart.platform.service.admittance.VisitorFaceCropCapabilityService;
import com.tce.smart.platform.service.admittance.VisitorFaceDraftCredential;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 访客人脸裁剪能力的 Redis 实现。
 *
 * 草稿会话只能由成功校验的微信 code 签发，Redis 只保存 openId 摘要；裁剪能力
 * 通过 Lua 比对草稿后删除，避免并发重放同一能力。
 */
@Service
public class VisitorFaceCropCapabilityServiceImpl implements VisitorFaceCropCapabilityService {
	private static final String DRAFT_KEY_PREFIX = "smart:admittance:visitor-face:draft:";
	private static final String DRAFT_UNION_KEY_PREFIX = "smart:admittance:visitor-face:union:";
	private static final String DRAFT_RECEPTIONIST_KEY_PREFIX = "smart:admittance:visitor-face:receptionist:";
	private static final String ACTION_KEY_PREFIX = "smart:admittance:visitor-action:";
	private static final String ACTION_RATE_KEY_PREFIX = "smart:admittance:visitor-action-rate:";
	private static final long DRAFT_TTL_SECONDS = 30L * 60L;
	private static final long ACTION_TTL_SECONDS = 2L * 60L;
	private static final long ACTION_RATE_TTL_SECONDS = 60L;
	private static final long RECEPTIONIST_SELECTION_TTL_SECONDS = DRAFT_TTL_SECONDS;
	private static final long MAX_ACTION_ISSUES_PER_MINUTE = 12L;
	private static final DefaultRedisScript<Long> CONSUME_ACTION_SCRIPT = new DefaultRedisScript<>(
			"if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
			Long.class);
	private static final DefaultRedisScript<String> CONSUME_RECEPTIONIST_SELECTION_SCRIPT = new DefaultRedisScript<>(
			"local value = redis.call('get', KEYS[1]); if value then redis.call('del', KEYS[1]); end; return value",
			String.class);

	private final StringRedisTemplate redisTemplate;
	private final Supplier<String> tokenSupplier;

	public VisitorFaceCropCapabilityServiceImpl(StringRedisTemplate redisTemplate) {
		this(redisTemplate, () -> UUID.randomUUID().toString().replace("-", ""));
	}

	VisitorFaceCropCapabilityServiceImpl(StringRedisTemplate redisTemplate, Supplier<String> tokenSupplier) {
		this.redisTemplate = redisTemplate;
		this.tokenSupplier = tokenSupplier;
	}

	@Override
	public VisitorFaceDraftCredential issueDraft(String openId) {
		return issueDraft(openId, null);
	}

	@Override
	public VisitorFaceDraftCredential issueDraft(String openId, String unionId) {
		if (!StringUtils.hasText(openId)) {
			throw forbidden();
		}
		String draftToken = nextToken();
		String draftId = nextToken();
		redisTemplate.opsForValue().set(draftKey(draftToken), hashOpenId(openId) + "|" + draftId,
				DRAFT_TTL_SECONDS, TimeUnit.SECONDS);
		if (StringUtils.hasText(unionId)) {
			redisTemplate.opsForValue().set(draftUnionKey(draftToken), unionId, DRAFT_TTL_SECONDS, TimeUnit.SECONDS);
		}
		return new VisitorFaceDraftCredential(draftToken, draftId);
	}

	@Override
	public String resolveUnionId(String draftToken, String draftId) {
		String expectedDraftId = draftIdFromSession(draftToken);
		if (!StringUtils.hasText(draftId) || !draftId.equals(expectedDraftId)) {
			throw forbidden();
		}
		String unionId = redisTemplate.opsForValue().get(draftUnionKey(draftToken));
		if (!StringUtils.hasText(unionId)) {
			throw forbidden();
		}
		return unionId;
	}

	@Override
	public void assertStaticOptionAccess(String draftToken, String draftId) {
		String expectedDraftId = draftIdFromSession(draftToken);
		if (!StringUtils.hasText(draftId) || !draftId.equals(expectedDraftId)) {
			throw forbidden();
		}
		// 选项是无敏感数据的只读内容，但仍要限制草稿令牌被脚本反复调用造成的放大流量。
		assertIssueRate(draftId, VisitorActionCapabilityAction.STATIC_OPTIONS);
	}

	@Override
	public void rememberReceptionistSelection(String draftId, String receptionistBadge, String receptionistName,
			String receptionistPhone) {
		if (!StringUtils.hasText(draftId) || !StringUtils.hasText(receptionistBadge) || !StringUtils.hasText(receptionistName)
				|| !StringUtils.hasText(receptionistPhone)) {
			throw forbidden();
		}
		redisTemplate.opsForValue().set(receptionistKey(draftId), selectionValue(receptionistBadge, receptionistName,
				receptionistPhone), RECEPTIONIST_SELECTION_TTL_SECONDS, TimeUnit.SECONDS);
	}

	@Override
	public VisitorReceptionistSelection getReceptionistSelection(String draftToken, String draftId) {
		String expectedDraftId = draftIdFromSession(draftToken);
		if (!StringUtils.hasText(draftId) || !draftId.equals(expectedDraftId)) {
			throw forbidden();
		}
		return parseReceptionistSelection(redisTemplate.opsForValue().get(receptionistKey(draftId)));
	}

	@Override
	public VisitorReceptionistSelection consumeReceptionistSelection(String draftToken, String draftId) {
		String expectedDraftId = draftIdFromSession(draftToken);
		if (!StringUtils.hasText(draftId) || !draftId.equals(expectedDraftId)) {
			throw forbidden();
		}
		String selection = redisTemplate.execute(CONSUME_RECEPTIONIST_SELECTION_SCRIPT,
				Collections.singletonList(receptionistKey(draftId)), "");
		return parseReceptionistSelection(selection);
	}

	private VisitorReceptionistSelection parseReceptionistSelection(String selection) {
		if (!StringUtils.hasText(selection)) {
			throw forbidden();
		}
		String[] fields = selection.split("\\u001f", -1);
		if (fields.length != 3 || !StringUtils.hasText(fields[0]) || !StringUtils.hasText(fields[1])
				|| !StringUtils.hasText(fields[2])) {
			throw forbidden();
		}
		return new VisitorReceptionistSelection(fields[0], fields[1], fields[2]);
	}

	@Override
	public String issueCropCapability(String draftToken, String draftId) {
		return issueActionCapability(draftToken, draftId, VisitorActionCapabilityAction.FACE_CROP);
	}

	@Override
	public String issueActionCapability(String draftToken, String draftId, VisitorActionCapabilityAction action) {
		return issueActionCapability(draftToken, draftId, action, null);
	}

	@Override
	public String issueActionCapability(String draftToken, String draftId, VisitorActionCapabilityAction action,
			String payloadHash) {
		String expectedDraftId = draftIdFromSession(draftToken);
		if (!StringUtils.hasText(draftId) || !draftId.equals(expectedDraftId)) {
			throw forbidden();
		}
		return issueActionCapabilityForVerifiedDraft(draftId, action, payloadHash, true);
	}

	@Override
	public String issueActionCapabilityForVerifiedDraft(String draftId, VisitorActionCapabilityAction action, String payloadHash) {
		return issueActionCapabilityForVerifiedDraft(draftId, action, payloadHash, false);
	}

	private String issueActionCapabilityForVerifiedDraft(String draftId, VisitorActionCapabilityAction action, String payloadHash,
			boolean applyRateLimit) {
		if (!StringUtils.hasText(draftId) || action == null || !validPayloadHash(action, payloadHash)) {
			throw forbidden();
		}
		if (applyRateLimit) {
			assertIssueRate(draftId, action);
		}
		String capability = nextToken();
		redisTemplate.opsForValue().set(actionKey(capability), actionValue(draftId, action, payloadHash), ACTION_TTL_SECONDS,
				TimeUnit.SECONDS);
		return capability;
	}

	@Override
	public void consumeCropCapability(String capability, String draftId) {
		consumeActionCapability(capability, draftId, VisitorActionCapabilityAction.FACE_CROP);
	}

	@Override
	public void consumeActionCapability(String capability, String draftId, VisitorActionCapabilityAction action) {
		consumeActionCapability(capability, draftId, action, null);
	}

	@Override
	public void consumeActionCapability(String capability, String draftId, VisitorActionCapabilityAction action,
			String payloadHash) {
		if (!StringUtils.hasText(capability) || !StringUtils.hasText(draftId) || action == null
				|| !validPayloadHash(action, payloadHash)) {
			throw forbidden();
		}
		Long consumed = redisTemplate.execute(CONSUME_ACTION_SCRIPT, Collections.singletonList(actionKey(capability)),
				actionValue(draftId, action, payloadHash));
		if (!Long.valueOf(1L).equals(consumed)) {
			throw forbidden();
		}
	}

	/** 限制每个短时草稿的动作签发频率，避免持有草稿票据的一方无限放大存图或查询压力。 */
	private void assertIssueRate(String draftId, VisitorActionCapabilityAction action) {
		String key = actionRateKey(draftId, action);
		Long count = redisTemplate.opsForValue().increment(key);
		if (Long.valueOf(1L).equals(count)) {
			redisTemplate.expire(key, ACTION_RATE_TTL_SECONDS, TimeUnit.SECONDS);
		}
		if (count == null || count > MAX_ACTION_ISSUES_PER_MINUTE) {
			throw forbidden();
		}
	}

	private String draftIdFromSession(String draftToken) {
		if (!StringUtils.hasText(draftToken)) {
			throw forbidden();
		}
		String record = redisTemplate.opsForValue().get(draftKey(draftToken));
		if (!StringUtils.hasText(record)) {
			throw forbidden();
		}
		int separator = record.lastIndexOf('|');
		if (separator <= 0 || separator == record.length() - 1) {
			throw forbidden();
		}
		return record.substring(separator + 1);
	}

	private String nextToken() {
		String token = tokenSupplier.get();
		if (!StringUtils.hasText(token)) {
			throw new SmartException("访客身份服务不可用");
		}
		return token;
	}

	private String hashOpenId(String openId) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(openId.getBytes(StandardCharsets.UTF_8));
			StringBuilder result = new StringBuilder(digest.length * 2);
			for (byte current : digest) {
				result.append(String.format("%02x", current));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new SmartException("访客身份服务不可用");
		}
	}

	private String draftKey(String token) {
		return DRAFT_KEY_PREFIX + token;
	}

	private String draftUnionKey(String token) {
		return DRAFT_UNION_KEY_PREFIX + token;
	}

	private String receptionistKey(String draftId) {
		return DRAFT_RECEPTIONIST_KEY_PREFIX + draftId;
	}

	private String selectionValue(String receptionistBadge, String receptionistName, String receptionistPhone) {
		return receptionistBadge + "\u001f" + receptionistName + "\u001f" + receptionistPhone;
	}

	private String actionKey(String capability) {
		return ACTION_KEY_PREFIX + capability;
	}

	private String actionRateKey(String draftId, VisitorActionCapabilityAction action) {
		return ACTION_RATE_KEY_PREFIX + action.name() + ":" + draftId;
	}

	private boolean validPayloadHash(VisitorActionCapabilityAction action, String payloadHash) {
		if (action != VisitorActionCapabilityAction.FACE_UPLOAD && action != VisitorActionCapabilityAction.DOCUMENT_UPLOAD
				&& action != VisitorActionCapabilityAction.BLACKLIST_CHECK && action != VisitorActionCapabilityAction.RECEPTIONIST_SEARCH
				&& action != VisitorActionCapabilityAction.APPLY_PRECHECK && action != VisitorActionCapabilityAction.APPLY_SUBMIT) {
			return !StringUtils.hasText(payloadHash);
		}
		return payloadHash != null && payloadHash.matches("[0-9a-f]{64}");
	}

	private String actionValue(String draftId, VisitorActionCapabilityAction action, String payloadHash) {
		return draftId + "|" + action.name() + "|" + (payloadHash == null ? "" : payloadHash);
	}

	private SmartException forbidden() {
		return new SmartException("访客人脸授权已失效，请重新进入申请流程");
	}
}
