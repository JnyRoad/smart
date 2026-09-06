package com.tce.smart.platform.client.release;

import com.tce.smart.platform.client.identity.ClientApiException;
import com.tce.smart.platform.client.identity.ClientAuthenticatedPrincipal;
import com.tce.smart.platform.client.identity.ClientIdentityService;
import com.tce.smart.platform.client.identity.ClientPersonnelDirectory;
import com.tce.smart.platform.core.client.release.CardEvidence;
import com.tce.smart.platform.core.client.release.ConfidentialRelease;
import com.tce.smart.platform.core.client.release.EscortMode;
import com.tce.smart.platform.core.client.release.JdbcConfidentialReleaseStore;
import com.tce.smart.platform.core.client.release.ReleaseAction;
import com.tce.smart.platform.core.client.release.ReleaseApplicationRequest;
import com.tce.smart.platform.core.client.release.ReleaseCreationContext;
import com.tce.smart.platform.core.client.release.ReleasePrincipal;
import com.tce.smart.platform.core.client.release.ReleaseStatus;
import org.springframework.http.HttpStatus;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** App 放行 HTTP 适配的服务端授权层：所有身份、审批人、岗位和卡证都在此收敛。 */
public class ClientReleaseService {
	private static final String SCOPE = "client008:item-pass";
	private final ReleaseAccessProperties properties;
	private final JdbcConfidentialReleaseStore store;
	private final ClientIdentityService identities;
	private final ClientPersonnelDirectory personnel;
	private final ReleaseCardEvidenceVerifier cards;
	private final Clock clock;

	ClientReleaseService(ReleaseAccessProperties properties, JdbcConfidentialReleaseStore store,
			ClientIdentityService identities, ClientPersonnelDirectory personnel,
			ReleaseCardEvidenceVerifier cards, Clock clock) {
		this.properties = properties; this.store = store; this.identities = identities; this.personnel = personnel;
		this.cards = cards; this.clock = clock;
	}

	public Map<String, Object> create(ClientReleaseRequests.Application body, String idempotencyKey) {
		properties.validate();
		if (body == null || !idempotency(idempotencyKey)) throw new ClientApiException(400);
		ClientAuthenticatedPrincipal subject = require("item-pass:apply");
		Set<String> candidates = candidatePostIds(subject);
		String approver = properties.approverFor(subject.getPerson().getStaffNo());
		if (!ReleaseAccessProperties.identifier(approver)) throw new ClientApiException(403);
		try {
			ConfidentialRelease release = store.create(SCOPE, idempotencyKey,
					new ReleaseApplicationRequest(body.title, body.reason, Collections.singletonList(body.materials), body.seals,
							body.fromPostId, body.toPostId),
					ReleaseCreationContext.verified(principal(subject), candidates, approver), now(), releaseId(), UUID.randomUUID().toString());
			return response(release);
		} catch (SQLException failure) { throw new ClientApiException(503); }
	}

	public List<Map<String, Object>> list(String scope, String postId) {
		properties.validate();
		ClientAuthenticatedPrincipal subject = identities.current();
		try {
			List<Map<String, Object>> result = new ArrayList<>();
			if ("execute".equals(scope)) {
				require(subject, "item-pass:execute");
				requireExecutionPost(subject, postId);
				for (ConfidentialRelease release : store.listRecent(200)) if (isExecutableAt(release, postId)) result.add(response(release));
				return result;
			}
			if (scope != null && !scope.trim().isEmpty()) throw new ClientApiException(400);
			if (!subject.has("item-pass:apply") && !subject.has("item-pass:approve") && !subject.has("item-pass:read")) throw new ClientApiException(403);
			for (ConfidentialRelease release : store.listRecent(200)) if (mayRead(subject, release)) result.add(response(release));
			return result;
		} catch (SQLException failure) { throw new ClientApiException(503); }
	}

	/** 详情沿用列表的服务端可见范围，执行人只能读取当前可办理的单据。 */
	public Map<String, Object> detail(String releaseId) {
		properties.validate();
		ClientAuthenticatedPrincipal subject = identities.current();
		ConfidentialRelease release = find(releaseId);
		if (!mayRead(subject, release)) throw new ClientApiException(403);
		return response(release);
	}

	public Map<String, Object> options() {
		properties.validate();
		ClientAuthenticatedPrincipal subject = require("item-pass:apply");
		List<Map<String, Object>> posts = new ArrayList<>();
		for (ReleaseAccessProperties.Post post : properties.getPosts()) if (subject.getParkIds().contains(post.getParkId())) posts.add(post(post));
		if (posts.size() < 2) throw new ClientApiException(403);
		Map<String, Object> response = new LinkedHashMap<>(); response.put("posts", posts); return response;
	}

	public Map<String, Object> action(String releaseId, ClientReleaseRequests.Action body, String idempotencyKey) {
		properties.validate();
		if (!ReleaseAccessProperties.identifier(releaseId) || body == null || !idempotency(idempotencyKey)) throw new ClientApiException(400);
		ConfidentialRelease current = find(releaseId);
		Instant now = now();
		try {
			if ("approve".equals(body.action)) {
				ClientAuthenticatedPrincipal subject = require("item-pass:approve");
				return response(store.approve(SCOPE, idempotencyKey, releaseId, principal(subject), current.getVersion(), now, UUID.randomUUID().toString()));
			}
			if ("reject".equals(body.action)) {
				ClientAuthenticatedPrincipal subject = require("item-pass:approve");
				return response(store.reject(SCOPE, idempotencyKey, releaseId, principal(subject), current.getVersion(), body.comment, now, UUID.randomUUID().toString()));
			}
			if ("depart".equals(body.action) || "arrive".equals(body.action)) return execute(releaseId, current, body, idempotencyKey, now);
			throw new ClientApiException(400);
		} catch (SQLException failure) { throw new ClientApiException(503); }
	}

	private Map<String, Object> execute(String releaseId, ConfidentialRelease current, ClientReleaseRequests.Action body,
			String idempotencyKey, Instant now) throws SQLException {
		ClientAuthenticatedPrincipal subject = require("item-pass:execute");
		String requiredPost = "depart".equals(body.action) ? current.getOriginPostId() : current.getDestinationPostId();
		if (!requiredPost.equals(body.postId)) throw new ClientApiException(403);
		requireExecutionPost(subject, requiredPost);
		if (body.execution == null) throw new ClientApiException(400);
		ReleaseAction action = "depart".equals(body.action) ? ReleaseAction.DEPART : ReleaseAction.ARRIVE;
		EscortMode mode = escortMode(body.execution.mode);
		CardEvidence security = cards.security(current, requiredPost, action, subject.getPerson().getStaffNo(), now);
		CardEvidence escort = mode == EscortMode.ESCORT_CARD
				? cards.escort(current, requiredPost, action, subject.getPerson().getStaffNo(), body.execution.escortProof, now) : null;
		if (mode == EscortMode.ESCORT_CARD && !blank(body.execution.lockNo)) throw new ClientApiException(400);
		if (mode == EscortMode.POSITIONING_LOCK && (!blank(body.execution.escortProof) || !ReleaseAccessProperties.identifier(body.execution.lockNo))) throw new ClientApiException(400);
		ReleasePrincipal principal = principal(subject);
		ConfidentialRelease result = action == ReleaseAction.DEPART
				? store.depart(SCOPE, idempotencyKey, releaseId, principal, current.getVersion(), mode, body.execution.lockNo,
						security, escort, now, UUID.randomUUID().toString())
				: store.arrive(SCOPE, idempotencyKey, releaseId, principal, current.getVersion(), mode, body.execution.lockNo,
						security, escort, now, UUID.randomUUID().toString());
		return response(result);
	}

	private ConfidentialRelease find(String releaseId) {
		try { ConfidentialRelease result = store.find(releaseId); if (result == null) throw new ClientApiException(404); return result; }
		catch (ClientApiException failure) { throw failure; }
		catch (SQLException failure) { throw new ClientApiException(503); }
	}

	private boolean mayRead(ClientAuthenticatedPrincipal subject, ConfidentialRelease release) {
		String actor = subject.getPerson().getStaffNo();
		if (actor.equals(release.getApplicantId()) && (subject.has("item-pass:apply") || subject.has("item-pass:read"))) return true;
		if (actor.equals(release.getAssignedApproverId()) && (subject.has("item-pass:approve") || subject.has("item-pass:read"))) return true;
		if (subject.has("item-pass:execute")) for (ReleaseAccessProperties.Post post : properties.getPosts())
			if (subject.getParkIds().contains(post.getParkId()) && subject.has("item-pass:post:" + post.getId())
					&& isExecutableAt(release, post.getId())) return true;
		return subject.has("item-pass:read") && (inPark(subject, release.getOriginPostId()) || inPark(subject, release.getDestinationPostId()));
	}

	private boolean isExecutableAt(ConfidentialRelease release, String postId) {
		return (release.getStatus() == ReleaseStatus.APPROVED && postId.equals(release.getOriginPostId()))
				|| (release.getStatus() == ReleaseStatus.TRANSPORTING && postId.equals(release.getDestinationPostId()));
	}

	private ClientAuthenticatedPrincipal require(String permission) { ClientAuthenticatedPrincipal subject = identities.current(); require(subject, permission); return subject; }
	private void require(ClientAuthenticatedPrincipal subject, String permission) { if (!subject.has(permission)) throw new ClientApiException(403); }
	private void requireExecutionPost(ClientAuthenticatedPrincipal subject, String postId) {
		ReleaseAccessProperties.Post post = properties.post(postId);
		if (post == null || !subject.getParkIds().contains(post.getParkId()) || !subject.has("item-pass:post:" + postId)) throw new ClientApiException(403);
	}
	private Set<String> candidatePostIds(ClientAuthenticatedPrincipal subject) {
		Set<String> ids = new LinkedHashSet<>();
		for (ReleaseAccessProperties.Post post : properties.getPosts()) if (subject.getParkIds().contains(post.getParkId())) ids.add(post.getId());
		if (ids.size() < 2) throw new ClientApiException(403); return ids;
	}
	private boolean inPark(ClientAuthenticatedPrincipal subject, String postId) {
		ReleaseAccessProperties.Post post = properties.post(postId); return post != null && subject.getParkIds().contains(post.getParkId());
	}
	private ReleasePrincipal principal(ClientAuthenticatedPrincipal subject) {
		Set<String> posts = new LinkedHashSet<>();
		for (ReleaseAccessProperties.Post post : properties.getPosts())
			if (subject.getParkIds().contains(post.getParkId()) && subject.has("item-pass:post:" + post.getId())) posts.add(post.getId());
		return ReleasePrincipal.authenticated(subject.getPerson().getStaffNo(), subject.getPermissions(), posts);
	}
	private Map<String, Object> response(ConfidentialRelease release) {
		ReleaseAccessProperties.Post origin = properties.post(release.getOriginPostId()), destination = properties.post(release.getDestinationPostId());
		if (origin == null || destination == null) throw new ClientApiException(503);
		Map<String, Object> value = new LinkedHashMap<>();
		value.put("id", release.getReleaseId()); value.put("kind", "item-pass"); value.put("title", release.getTitle()); value.put("reason", release.getReason());
		value.put("fromPostId", origin.getId()); value.put("fromPostName", origin.getName()); value.put("toPostId", destination.getId()); value.put("toPostName", destination.getName());
		value.put("supplierName", ""); value.put("visitorName", ""); value.put("materials", String.join("、", release.getMaterials()));
		value.put("seals", new ArrayList<>(release.getSealCodes())); value.put("applicantId", release.getApplicantId());
		value.put("applicantName", personnel.displayNameOrStaffNo(release.getApplicantId())); value.put("status", status(release.getStatus())); value.put("version", release.getVersion());
		if (release.getEscortMode() == null) value.put("transport", null); else {
			Map<String, Object> transport = new LinkedHashMap<>(); transport.put("mode", release.getEscortMode() == EscortMode.ESCORT_CARD ? "escort" : "lock");
			transport.put("lockNo", release.getPositioningLockId() == null ? "" : release.getPositioningLockId()); value.put("transport", transport);
		}
		List<Map<String, Object>> timeline = new ArrayList<>();
		for (com.tce.smart.platform.core.client.release.ReleaseAuditEvent event : release.getAuditTrail()) {
			Map<String, Object> item = new LinkedHashMap<>(); item.put("id", event.getEventId()); item.put("title", title(event.getAction()));
			item.put("actor", personnel.displayNameOrStaffNo(event.getActorId())); item.put("at", event.getOccurredAt().toString()); timeline.add(item);
		}
		value.put("timeline", timeline); return value;
	}
	private Map<String, Object> post(ReleaseAccessProperties.Post post) { Map<String, Object> item = new LinkedHashMap<>(); item.put("id", post.getId()); item.put("name", post.getName()); item.put("parkId", String.valueOf(post.getParkId())); item.put("parkName", post.getParkName()); return item; }
	private EscortMode escortMode(String value) { if ("escort".equals(value)) return EscortMode.ESCORT_CARD; if ("lock".equals(value)) return EscortMode.POSITIONING_LOCK; throw new ClientApiException(400); }
	private String status(ReleaseStatus value) { return value.name().toLowerCase(java.util.Locale.ROOT); }
	private String title(ReleaseAction action) { switch (action) { case CREATE: return "提交申请"; case APPROVE: return "审批通过"; case REJECT: return "驳回申请"; case DEPART: return "安检放行"; default: return "到达确认"; } }
	private boolean idempotency(String value) { return value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,127}"); }
	private boolean blank(String value) { return value == null || value.trim().isEmpty(); }
	private Instant now() { return clock.instant(); }
	private String releaseId() { return "REL-" + UUID.randomUUID().toString(); }
}
