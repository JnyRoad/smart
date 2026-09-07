package com.tce.smart.platform.core.client.release;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 放行快照、事件和命令请求的显式 JSON 编解码器。
 *
 * 领域对象保持不可变；恢复时逐字段调用同包构造器，不依赖反射写入 final 字段，
 * 也不使用 Java 对象序列化。
 */
final class ReleasePersistenceCodec {

	private final ObjectMapper mapper = new ObjectMapper();

	String encodeRelease(ConfidentialRelease release) throws JsonProcessingException {
		ObjectNode root = mapper.createObjectNode();
		root.put("releaseId", release.getReleaseId());
		root.put("applicantId", release.getApplicantId());
		root.put("assignedApproverId", release.getAssignedApproverId());
		root.put("title", release.getTitle());
		root.put("reason", release.getReason());
		putStrings(root.putArray("materials"), release.getMaterials());
		putStrings(root.putArray("sealCodes"), release.getSealCodes());
		root.put("originPostId", release.getOriginPostId());
		root.put("destinationPostId", release.getDestinationPostId());
		root.put("status", release.getStatus().name());
		putEnum(root, "escortMode", release.getEscortMode());
		putText(root, "positioningLockId", release.getPositioningLockId());
		root.put("version", release.getVersion());
		ArrayNode events = root.putArray("auditTrail");
		for (ReleaseAuditEvent event : release.getAuditTrail()) {
			events.add(eventNode(event));
		}
		return mapper.writeValueAsString(root);
	}

	ConfidentialRelease decodeRelease(String json) throws IOException {
		JsonNode root = mapper.readTree(json);
		List<ReleaseAuditEvent> events = new ArrayList<>();
		for (JsonNode event : required(root, "auditTrail")) {
			events.add(decodeEvent(event));
		}
		return new ConfidentialRelease(text(root, "releaseId"), text(root, "applicantId"),
				text(root, "assignedApproverId"), text(root, "title"), text(root, "reason"),
				strings(root, "materials"), strings(root, "sealCodes"), text(root, "originPostId"),
				text(root, "destinationPostId"), ReleaseStatus.valueOf(text(root, "status")),
				enumValue(root, "escortMode", EscortMode.class), nullableText(root, "positioningLockId"),
				required(root, "version").asLong(), events);
	}

	String encodeEvent(ReleaseAuditEvent event) throws JsonProcessingException {
		return mapper.writeValueAsString(eventNode(event));
	}

	String digestCreate(ReleaseApplicationRequest request) {
		ObjectNode root = actionNode(ReleaseAction.CREATE, null, null);
		putText(root, "title", request == null ? null : request.getTitle());
		putText(root, "reason", request == null ? null : request.getReason());
		putStrings(root.putArray("materials"), request == null ? Collections.<String>emptyList() : request.getMaterials());
		putStrings(root.putArray("sealCodes"), request == null ? Collections.<String>emptyList() : request.getSealCodes());
		putText(root, "originPostId", request == null ? null : request.getOriginPostId());
		putText(root, "destinationPostId", request == null ? null : request.getDestinationPostId());
		return sha256(root);
	}

	String digestApproval(ReleaseAction action, String releaseId, long expectedVersion, String rejectionReason) {
		ObjectNode root = actionNode(action, releaseId, expectedVersion);
		putText(root, "rejectionReason", rejectionReason);
		return sha256(root);
	}

	String digestTransfer(ReleaseAction action, String releaseId, long expectedVersion, EscortMode escortMode,
			String positioningLockId, CardEvidence securityEvidence, CardEvidence escortEvidence) {
		ObjectNode root = actionNode(action, releaseId, expectedVersion);
		putEnum(root, "escortMode", escortMode);
		putText(root, "positioningLockId", positioningLockId);
		root.set("securityEvidence", evidenceNode(securityEvidence));
		root.set("escortEvidence", evidenceNode(escortEvidence));
		return sha256(root);
	}

	private ObjectNode actionNode(ReleaseAction action, String releaseId, Long expectedVersion) {
		ObjectNode root = mapper.createObjectNode();
		root.put("action", action.name());
		putText(root, "releaseId", releaseId);
		if (expectedVersion == null) {
			root.putNull("expectedVersion");
		} else {
			root.put("expectedVersion", expectedVersion.longValue());
		}
		return root;
	}

	private ObjectNode eventNode(ReleaseAuditEvent event) {
		ObjectNode node = mapper.createObjectNode();
		node.put("eventId", event.getEventId());
		node.put("releaseId", event.getReleaseId());
		node.put("action", event.getAction().name());
		node.put("actorId", event.getActorId());
		putText(node, "postId", event.getPostId());
		putEnum(node, "fromStatus", event.getFromStatus());
		node.put("toStatus", event.getToStatus().name());
		node.put("occurredAt", event.getOccurredAt().toString());
		putText(node, "reason", event.getReason());
		putEnum(node, "escortMode", event.getEscortMode());
		putText(node, "positioningLockId", event.getPositioningLockId());
		node.set("securityEvidence", evidenceNode(event.getSecurityEvidence()));
		node.set("escortEvidence", evidenceNode(event.getEscortEvidence()));
		node.put("version", event.getVersion());
		return node;
	}

	private ReleaseAuditEvent decodeEvent(JsonNode node) throws IOException {
		return new ReleaseAuditEvent(text(node, "eventId"), text(node, "releaseId"),
				ReleaseAction.valueOf(text(node, "action")), text(node, "actorId"), nullableText(node, "postId"),
				enumValue(node, "fromStatus", ReleaseStatus.class), ReleaseStatus.valueOf(text(node, "toStatus")),
				Instant.parse(text(node, "occurredAt")), nullableText(node, "reason"),
				enumValue(node, "escortMode", EscortMode.class), nullableText(node, "positioningLockId"),
				decodeEvidence(node.get("securityEvidence")), decodeEvidence(node.get("escortEvidence")),
				required(node, "version").asLong());
	}

	private JsonNode evidenceNode(CardEvidence evidence) {
		if (evidence == null) {
			return mapper.getNodeFactory().nullNode();
		}
		ObjectNode node = mapper.createObjectNode();
		putText(node, "evidenceId", evidence.getEvidenceId());
		putEnum(node, "role", evidence.getRole());
		putText(node, "holderId", evidence.getHolderId());
		putText(node, "releaseId", evidence.getReleaseId());
		putText(node, "postId", evidence.getPostId());
		putEnum(node, "action", evidence.getAction());
		putText(node, "operatorId", evidence.getOperatorId());
		putInstant(node, "verifiedAt", evidence.getVerifiedAt());
		putInstant(node, "validUntil", evidence.getValidUntil());
		return node;
	}

	private CardEvidence decodeEvidence(JsonNode node) throws IOException {
		if (node == null || node.isNull()) {
			return null;
		}
		return CardEvidence.verified(nullableText(node, "evidenceId"),
				enumValue(node, "role", CardRole.class), nullableText(node, "holderId"),
				nullableText(node, "releaseId"), nullableText(node, "postId"),
				enumValue(node, "action", ReleaseAction.class), nullableText(node, "operatorId"),
				instant(node, "verifiedAt"), instant(node, "validUntil"));
	}

	private void putStrings(ArrayNode node, List<String> values) {
		if (values == null) {
			return;
		}
		for (String value : values) {
			if (value == null) {
				node.addNull();
			} else {
				node.add(value);
			}
		}
	}

	private List<String> strings(JsonNode root, String field) throws IOException {
		List<String> result = new ArrayList<>();
		for (JsonNode value : required(root, field)) {
			result.add(value.isNull() ? null : value.asText());
		}
		return result;
	}

	private void putText(ObjectNode node, String field, String value) {
		if (value == null) {
			node.putNull(field);
		} else {
			node.put(field, value);
		}
	}

	private void putInstant(ObjectNode node, String field, Instant value) {
		putText(node, field, value == null ? null : value.toString());
	}

	private void putEnum(ObjectNode node, String field, Enum<?> value) {
		putText(node, field, value == null ? null : value.name());
	}

	private String text(JsonNode node, String field) throws IOException {
		JsonNode value = required(node, field);
		if (!value.isTextual()) {
			throw new IOException("JSON 字段不是文本：" + field);
		}
		return value.asText();
	}

	private String nullableText(JsonNode node, String field) throws IOException {
		JsonNode value = node == null ? null : node.get(field);
		if (value == null || value.isNull()) {
			return null;
		}
		if (!value.isTextual()) {
			throw new IOException("JSON 字段不是文本：" + field);
		}
		return value.asText();
	}

	private Instant instant(JsonNode node, String field) throws IOException {
		String value = nullableText(node, field);
		return value == null ? null : Instant.parse(value);
	}

	private <T extends Enum<T>> T enumValue(JsonNode node, String field, Class<T> type) throws IOException {
		String value = nullableText(node, field);
		return value == null ? null : Enum.valueOf(type, value);
	}

	private JsonNode required(JsonNode node, String field) throws IOException {
		JsonNode value = node == null ? null : node.get(field);
		if (value == null || value.isNull()) {
			throw new IOException("JSON 缺少字段：" + field);
		}
		return value;
	}

	private String sha256(JsonNode node) {
		try {
			byte[] bytes = mapper.writeValueAsString(node).getBytes(StandardCharsets.UTF_8);
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
			StringBuilder result = new StringBuilder(digest.length * 2);
			for (byte value : digest) {
				result.append(String.format("%02x", value & 0xff));
			}
			return result.toString();
		} catch (JsonProcessingException | NoSuchAlgorithmException error) {
			throw new IllegalStateException("无法计算放行命令摘要", error);
		}
	}
}
