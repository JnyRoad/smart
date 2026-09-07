package com.tce.smart.platform.core.client.supplier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** 供应商资格、通行回复和命令摘要的显式 JSON 编解码。 */
final class SupplierPersistenceCodec {

	private static final int FORMAT_VERSION = 1;

	private final ObjectMapper mapper = new ObjectMapper();

	String encodeQualification(SupplierQualificationSnapshot qualification)
			throws JsonProcessingException {
		return mapper.writeValueAsString(qualificationNode(qualification));
	}

	SupplierQualificationSnapshot decodeQualification(String json) throws IOException {
		return decodeQualificationNode(mapper.readTree(json));
	}

	String encodeResult(SupplierPassageResult result) throws JsonProcessingException {
		ObjectNode root = mapper.createObjectNode();
		root.put("formatVersion", FORMAT_VERSION);
		SupplierPresenceSnapshot presence = result.getPresence();
		ObjectNode presenceNode = root.putObject("presence");
		presenceNode.put("personId", presence.getPersonId());
		presenceNode.put("areaId", presence.getAreaId());
		presenceNode.put("state", presence.getPresence().name());
		presenceNode.put("version", presence.getVersion());

		SupplierPassageEvent event = result.getEvent();
		ObjectNode eventNode = root.putObject("event");
		eventNode.put("eventId", event.getEventId());
		eventNode.put("verificationId", event.getVerificationId());
		eventNode.put("operatorId", event.getOperatorId());
		eventNode.put("postId", event.getPostId());
		eventNode.put("areaId", event.getAreaId());
		eventNode.put("direction", event.getDirection().name());
		eventNode.put("occurredAt", event.getOccurredAt().toString());
		eventNode.put("version", event.getVersion());
		eventNode.set("qualification", qualificationNode(event.getQualificationSnapshot()));
		return mapper.writeValueAsString(root);
	}

	SupplierPassageResult decodeResult(String json) throws IOException {
		JsonNode root = mapper.readTree(json);
		requireFormat(root);
		JsonNode presenceNode = object(root, "presence");
		SupplierPresenceSnapshot presence = SupplierPresenceSnapshot.current(
				text(presenceNode, "personId"), text(presenceNode, "areaId"),
				SupplierPresence.valueOf(text(presenceNode, "state")), number(presenceNode, "version"));

		JsonNode eventNode = object(root, "event");
		SupplierPassageEvent event = new SupplierPassageEvent(text(eventNode, "eventId"),
				text(eventNode, "verificationId"), text(eventNode, "operatorId"),
				text(eventNode, "postId"), text(eventNode, "areaId"),
				SupplierDirection.valueOf(text(eventNode, "direction")),
				Instant.parse(text(eventNode, "occurredAt")), number(eventNode, "version"),
				decodeQualificationNode(object(eventNode, "qualification")));
		return new SupplierPassageResult(presence, event);
	}

	String digestRecord(String verificationId, SupplierDirection direction,
			SupplierQualificationSnapshot qualification, SupplierPostAreaMapping postArea) {
		ObjectNode root = mapper.createObjectNode();
		root.put("operation", "supplier-passage-record");
		putText(root, "verificationId", verificationId);
		putText(root, "direction", direction == null ? null : direction.name());
		putText(root, "badgeId", qualification == null ? null : qualification.getBadgeId());
		putText(root, "personId", qualification == null ? null : qualification.getPersonId());
		putText(root, "companyId", qualification == null ? null : qualification.getCompanyId());
		putText(root, "admissionId", qualification == null ? null : qualification.getAdmissionId());
		putText(root, "postId", postArea == null ? null : postArea.getPostId());
		putText(root, "areaId", postArea == null ? null : postArea.getAreaId());
		return sha256(root);
	}

	private ObjectNode qualificationNode(SupplierQualificationSnapshot qualification) {
		ObjectNode node = mapper.createObjectNode();
		node.put("formatVersion", FORMAT_VERSION);
		node.put("badgeId", qualification.getBadgeId());
		node.put("personId", qualification.getPersonId());
		node.put("companyId", qualification.getCompanyId());
		node.put("admissionId", qualification.getAdmissionId());
		node.put("badgeActive", qualification.isBadgeActive());
		node.put("personActive", qualification.isPersonActive());
		node.put("companyActive", qualification.isCompanyActive());
		node.put("admissionActive", qualification.isAdmissionActive());
		node.put("admissionApproved", qualification.isAdmissionApproved());
		putInstant(node, "validFrom", qualification.getValidFrom());
		putInstant(node, "validUntil", qualification.getValidUntil());
		ArrayNode areas = node.putArray("authorizedAreaIds");
		List<String> sortedAreas = new ArrayList<>(qualification.getAuthorizedAreaIds());
		Collections.sort(sortedAreas);
		for (String area : sortedAreas) {
			areas.add(area);
		}
		putText(node, "personName", qualification.getPersonName());
		putText(node, "companyName", qualification.getCompanyName());
		putText(node, "photoUrl", qualification.getPhotoUrl());
		putText(node, "personPhone", qualification.getPersonPhone());
		putText(node, "hostName", qualification.getHostName());
		putText(node, "hostPhone", qualification.getHostPhone());
		return node;
	}

	private SupplierQualificationSnapshot decodeQualificationNode(JsonNode node) throws IOException {
		requireFormat(node);
		Set<String> areas = new LinkedHashSet<>();
		JsonNode areaNodes = required(node, "authorizedAreaIds");
		if (!areaNodes.isArray()) {
			throw new IOException("JSON 字段不是数组：authorizedAreaIds");
		}
		for (JsonNode area : areaNodes) {
			if (!area.isTextual()) {
				throw new IOException("JSON 区域标识不是文本");
			}
			areas.add(area.asText());
		}
		return SupplierQualificationSnapshot.fromTrustedSource(text(node, "badgeId"),
				text(node, "personId"), text(node, "companyId"), text(node, "admissionId"),
				bool(node, "badgeActive"), bool(node, "personActive"), bool(node, "companyActive"),
				bool(node, "admissionActive"), bool(node, "admissionApproved"),
				Instant.parse(text(node, "validFrom")), Instant.parse(text(node, "validUntil")), areas,
				nullableText(node, "personName"), nullableText(node, "companyName"),
				nullableText(node, "photoUrl"), nullableText(node, "personPhone"),
				nullableText(node, "hostName"), nullableText(node, "hostPhone"));
	}

	private void requireFormat(JsonNode node) throws IOException {
		if (number(node, "formatVersion") != FORMAT_VERSION) {
			throw new IOException("不支持的供应商持久化 JSON 版本");
		}
	}

	private JsonNode object(JsonNode node, String field) throws IOException {
		JsonNode value = required(node, field);
		if (!value.isObject()) {
			throw new IOException("JSON 字段不是对象：" + field);
		}
		return value;
	}

	private JsonNode required(JsonNode node, String field) throws IOException {
		JsonNode value = node == null ? null : node.get(field);
		if (value == null || value.isNull()) {
			throw new IOException("JSON 缺少字段：" + field);
		}
		return value;
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

	private boolean bool(JsonNode node, String field) throws IOException {
		JsonNode value = required(node, field);
		if (!value.isBoolean()) {
			throw new IOException("JSON 字段不是布尔值：" + field);
		}
		return value.asBoolean();
	}

	private long number(JsonNode node, String field) throws IOException {
		JsonNode value = required(node, field);
		if (!value.isIntegralNumber()) {
			throw new IOException("JSON 字段不是整数：" + field);
		}
		return value.asLong();
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

	private String sha256(JsonNode node) {
		try {
			byte[] source = mapper.writeValueAsString(node).getBytes(StandardCharsets.UTF_8);
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(source);
			StringBuilder result = new StringBuilder(digest.length * 2);
			for (byte value : digest) {
				result.append(String.format("%02x", value & 0xff));
			}
			return result.toString();
		} catch (JsonProcessingException | NoSuchAlgorithmException error) {
			throw new IllegalStateException("无法计算供应商通行请求摘要", error);
		}
	}
}
