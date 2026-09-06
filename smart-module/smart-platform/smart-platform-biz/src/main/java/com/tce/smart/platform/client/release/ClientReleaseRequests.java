package com.tce.smart.platform.client.release;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tce.smart.platform.client.identity.ClientApiException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/** 严格的 App 物品放行请求解码器：资源路径已经表达业务类型，拒绝额外身份字段、数字条码和含糊的执行载荷。 */
final class ClientReleaseRequests {
	private ClientReleaseRequests() { }
	@JsonDeserialize(using = ApplicationDeserializer.class)
	static final class Application {
		final String title, reason, fromPostId, toPostId, supplierName, visitorName, materials;
		final List<String> seals;
		Application(String title, String reason, String fromPostId, String toPostId, String supplierName,
				String visitorName, String materials, List<String> seals) {
			this.title = title; this.reason = reason; this.fromPostId = fromPostId; this.toPostId = toPostId;
			this.supplierName = supplierName; this.visitorName = visitorName; this.materials = materials; this.seals = seals;
		}
	}
	@JsonDeserialize(using = ActionDeserializer.class)
	static final class Action {
		final String action, postId, comment;
		final Execution execution;
		Action(String action, String postId, String comment, Execution execution) { this.action = action; this.postId = postId; this.comment = comment; this.execution = execution; }
	}
	static final class Execution {
		final String mode, escortProof, lockNo;
		Execution(String mode, String escortProof, String lockNo) { this.mode = mode; this.escortProof = escortProof; this.lockNo = lockNo; }
	}

	static final class ApplicationDeserializer extends JsonDeserializer<Application> {
		@Override public Application deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			JsonNode body = object(parser, 8, "title", "reason", "fromPostId", "toPostId", "supplierName", "visitorName", "materials", "seals");
			List<String> seals = strings(body.get("seals"), 100);
			return new Application(text(body, "title"), text(body, "reason", 500), text(body, "fromPostId"),
					text(body, "toPostId"), text(body, "supplierName"), text(body, "visitorName"), text(body, "materials", 1000), seals);
		}
	}
	static final class ActionDeserializer extends JsonDeserializer<Action> {
		@Override public Action deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			JsonNode body = read(parser);
			if (body.size() != 3 && body.size() != 4) invalid();
			String action = text(body, "action"), postId = text(body, "postId"), comment = text(body, "comment");
			Execution execution = null;
			if (body.has("execution")) {
				JsonNode node = body.get("execution");
				if (node == null || !node.isObject() || node.size() != 3) invalid();
				execution = new Execution(text(node, "mode"), text(node, "escortProof"), text(node, "lockNo"));
			}
			return new Action(action, postId, comment, execution);
		}
	}

	private static JsonNode object(JsonParser parser, int size, String... fields) throws IOException {
		JsonNode body = read(parser); if (body.size() != size) invalid();
		for (String field : fields) if (!body.has(field)) invalid(); return body;
	}
	private static JsonNode read(JsonParser parser) throws IOException {
		parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
		try { JsonNode body = parser.readValueAsTree(); if (body == null || !body.isObject()) invalid(); return body; }
		catch (ClientApiException failure) { throw failure; }
		catch (IOException failure) { throw new ClientApiException(400); }
	}
	private static String text(JsonNode body, String field) {
		return text(body, field, 128);
	}
	private static String text(JsonNode body, String field, int maximumLength) {
		JsonNode value = body.get(field);
		if (value == null || !value.isTextual() || value.textValue().length() > maximumLength) invalid();
		return value.textValue();
	}
	private static List<String> strings(JsonNode node, int max) {
		if (node == null || !node.isArray() || node.size() > max) invalid();
		List<String> result = new ArrayList<>();
		for (JsonNode value : node) { if (!value.isTextual() || value.textValue().isEmpty() || value.textValue().length() > 128) invalid(); result.add(value.textValue()); }
		return result;
	}
	private static void invalid() { throw new ClientApiException(400); }
}
