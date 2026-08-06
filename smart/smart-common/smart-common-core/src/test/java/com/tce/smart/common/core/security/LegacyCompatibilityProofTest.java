package com.tce.smart.common.core.security;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Gateway 与 Platform 之间的遗留兼容证明必须同时绑定请求方法、路径和查询参数，
 * 否则攻击者可以把合法签名转用到其他人员或宿舍查询上。
 */
public class LegacyCompatibilityProofTest {

	private static final String SIGNATURE_KEY = "legacy-test-key";
	private static final long NOW = 1_753_800_000L;

	@Test
	public void verificationRejectsAProofWhenTheRawQueryParameterOrderChanges() {
		LegacyCompatibilityProof.Claims claims = claims("/dormitory/staff/remote/to/lock",
				"badge=A001&badge=B002");
		String signature = LegacyCompatibilityProof.sign(claims, SIGNATURE_KEY);

		LegacyCompatibilityProof.Claims reordered = claims("/dormitory/staff/remote/to/lock",
				"badge=B002&badge=A001");

		assertFalse(LegacyCompatibilityProof.verify(reordered, signature, SIGNATURE_KEY, NOW, 5, 30));
	}

	@Test
	public void verificationRejectsAProofReusedForAnotherLegacyPath() {
		LegacyCompatibilityProof.Claims claims = claims("/dormitory/staff/remote/to/lock", "parkId=7");
		String signature = LegacyCompatibilityProof.sign(claims, SIGNATURE_KEY);

		LegacyCompatibilityProof.Claims changedPath = claims("/staff/define/badge", "badge=A001");

		assertFalse(LegacyCompatibilityProof.verify(changedPath, signature, SIGNATURE_KEY, NOW, 5, 30));
	}

	@Test
	public void verificationRejectsAProofWhenTheRequestedParkChanges() {
		LegacyCompatibilityProof.Claims claims = claims("/dormitory/staff/remote/to/lock", "parkId=7");
		String signature = LegacyCompatibilityProof.sign(claims, SIGNATURE_KEY);

		LegacyCompatibilityProof.Claims changedPark = claims("/dormitory/staff/remote/to/lock", "parkId=8");

		assertFalse(LegacyCompatibilityProof.verify(changedPark, signature, SIGNATURE_KEY, NOW, 5, 30));
	}

	@Test
	public void verificationRejectsAnExpiredProof() {
		LegacyCompatibilityProof.Claims expired = claims("/dormitory/staff/remote/to/lock", "parkId=7",
				NOW - 100, NOW - 70);

		assertFalse(LegacyCompatibilityProof.verify(expired, LegacyCompatibilityProof.sign(expired, SIGNATURE_KEY),
				SIGNATURE_KEY, NOW, 5, 30));
	}

	@Test
	public void verificationRejectsAProofWhoseLifetimeExceedsTheConfiguredMaximum() {
		LegacyCompatibilityProof.Claims longLived = claims("/dormitory/staff/remote/to/lock", "parkId=7",
				NOW, NOW + 31);

		assertFalse(LegacyCompatibilityProof.verify(longLived, LegacyCompatibilityProof.sign(longLived, SIGNATURE_KEY),
				SIGNATURE_KEY, NOW, 5, 30));
	}

	@Test
	public void verificationRejectsAProofIssuedTooFarInTheFuture() {
		LegacyCompatibilityProof.Claims future = claims("/dormitory/staff/remote/to/lock", "parkId=7",
				NOW + 6, NOW + 30);

		assertFalse(LegacyCompatibilityProof.verify(future, LegacyCompatibilityProof.sign(future, SIGNATURE_KEY),
				SIGNATURE_KEY, NOW, 5, 30));
	}

	@Test
	public void verificationRejectsACallerOrSourceIpThatDiffersFromTheSignedRequest() {
		LegacyCompatibilityProof.Claims original = claims("/dormitory/staff/remote/to/lock", "parkId=7");
		String signature = LegacyCompatibilityProof.sign(original, SIGNATURE_KEY);
		LegacyCompatibilityProof.Claims differentSource = LegacyCompatibilityProof.Claims.of("v1", "key-v1", "lock-a",
				"10.13.21.31", "GET", "/dormitory/staff/remote/to/lock", "parkId=7", NOW, NOW + 30,
				"nonce-0123456789");

		assertFalse(LegacyCompatibilityProof.verify(differentSource, signature, SIGNATURE_KEY, NOW, 5, 30));
	}

	@Test
	public void verificationRejectsMalformedBase64Signature() {
		LegacyCompatibilityProof.Claims proof = claims("/dormitory/staff/remote/to/lock", "parkId=7");

		assertFalse(LegacyCompatibilityProof.verify(proof, "not-valid-base64!", SIGNATURE_KEY, NOW, 5, 30));
	}

	private LegacyCompatibilityProof.Claims claims(String path, String query) {
		return claims(path, query, NOW, NOW + 30);
	}

	private LegacyCompatibilityProof.Claims claims(String path, String query, long issuedAt, long expiresAt) {
		return LegacyCompatibilityProof.Claims.of("v1", "key-v1", "lock-a", "10.13.21.30", "GET", path, query,
				issuedAt, expiresAt, "nonce-0123456789");
	}
}
