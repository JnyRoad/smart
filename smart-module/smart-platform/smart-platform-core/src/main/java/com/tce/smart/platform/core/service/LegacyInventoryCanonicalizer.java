package com.tce.smart.platform.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tce.smart.platform.core.dto.legacyinventory.LegacyInventoryData.CanonicalReview;
import com.tce.smart.platform.core.dto.legacyinventory.LegacyInventoryData.FlowKind;
import com.tce.smart.platform.core.dto.legacyinventory.LegacyInventoryData.RawCandidate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** 将已知旧列和查找证据编码为稳定、可复算的 canonical JSON。 */
@Component
public class LegacyInventoryCanonicalizer {

	public static final String RAW_FORMAT = "CANONICAL_JSON_V1";
	public static final String EVIDENCE_FORMAT = "LEGACY_EVIDENCE_V1";
	public static final String QUERY_VERSION = "LEGACY_EMPLOYEE_LOOKUP_V1";
	private static final ObjectMapper JSON = new ObjectMapper();

	public CanonicalReview canonicalize(FlowKind flow, RawCandidate row, LocalDateTime sourceReadAt) {
		if (flow == null || row == null || row.getId() == null || sourceReadAt == null) {
			throw new IllegalArgumentException("盘点 canonical 输入不完整");
		}
		String raw = json(raw(flow, row));
		Projection projection = projection(flow, row);
		ObjectNode stableEvidence = stableEvidence(flow, row, projection);
		ObjectNode completeEvidence = stableEvidence.deepCopy();
		completeEvidence.put("sourceReadAt", timestamp(sourceReadAt));
		String stable = json(stableEvidence);
		String evidence = json(completeEvidence);
		String rawHash = sha256(raw);
		String evidenceHash = sha256(evidence);
		String fingerprint = sha256(flow.rawColumnSetVersion() + "\n" + RAW_FORMAT + "\n"
				+ raw + "\n" + QUERY_VERSION + "\n" + stable);
		return CanonicalReview.builder()
				.legacyRef(flow.name() + ":" + row.getId()).rowKind(flow.rowKind())
				.sourceTable(flow.sourceTable()).sourceRowId(String.valueOf(row.getId()))
				.rawColumnSetVersion(flow.rawColumnSetVersion()).rawPayload(raw).rawSha256(rawHash)
				.evidencePayload(evidence).stableEvidencePayload(stable).evidenceSha256(evidenceHash)
				.revisionFingerprint(fingerprint).capturedAt(sourceReadAt)
				.parkId(projection.parkId).parkState(projection.parkState)
				.deviceCode(row.getDeviceCode()).deviceType(row.getDeviceType()).accessType(projection.accessType)
				.serviceType(row.getServiceType()).serviceFamily(projection.serviceFamily).cardNo(row.getCardNo())
				.staffId(projection.staffId).iscPersonId(row.getPersonId()).badge(row.getBadge())
				.imageId(row.getImageId()).action(row.getAction()).status(row.getStatus())
				.taskType(row.getTaskType()).code(row.getCode()).relatedTaskRef(projection.relatedTaskRef)
				.externalTaskId(projection.externalTaskId).identityState(projection.identityState)
				.residueKind(projection.residueKind).reviewState(projection.reviewState)
				.reviewReason(projection.reviewReason).physicalState("UNKNOWN").build();
	}

	public String stableEvidence(String evidencePayload) {
		try {
			JsonNode parsed = JSON.readTree(evidencePayload);
			if (parsed == null || !parsed.isObject()) throw new IllegalArgumentException("历史 evidence 不是JSON对象");
			ObjectNode copy = ((ObjectNode) parsed).deepCopy();
			removeVolatile(copy);
			return json(copy);
		} catch (Exception invalid) {
			throw new IllegalArgumentException("历史 evidence 无法规范化", invalid);
		}
	}

	public String sha256(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder result = new StringBuilder(64);
			for (byte item : digest) result.append(String.format("%02x", item & 255));
			return result.toString();
		} catch (Exception impossible) {
			throw new IllegalStateException("JVM缺少SHA-256", impossible);
		}
	}

	private ObjectNode raw(FlowKind flow, RawCandidate row) {
		ObjectNode result = JSON.createObjectNode();
		switch (flow) {
			case CURRENT_SOURCE:
				put(result, "ID", row.getId()); put(result, "STAFF_ID", row.getStaffId());
				put(result, "AUTH_ID", row.getAuthId()); put(result, "CREATE_TIME", row.getCreateTime());
				put(result, "START_TIME", row.getStartAt()); put(result, "END_TIME", row.getOverAt());
				put(result, "AUTH_TYPE", row.getAuthType());
				break;
			case ISC_TASK:
				put(result, "ID", row.getId()); put(result, "ACTION", row.getAction());
				put(result, "STATUS", row.getStatus()); put(result, "DEVICE_TYPE", row.getDeviceType());
				put(result, "START_TIME", row.getStartEpoch()); put(result, "OVER_TIME", row.getOverEpoch());
				put(result, "DEVICE_CODE", row.getDeviceCode()); put(result, "CARD_NO", row.getCardNo());
				put(result, "CODE", row.getCode()); put(result, "CONSUME", row.getConsume());
				put(result, "TIMES", row.getTimes()); put(result, "GENERAL", row.getGeneral());
				put(result, "IMAGE_ID", row.getImageId()); put(result, "SERVICE_TYPE", row.getServiceType());
				put(result, "REMARK", row.getRemark()); put(result, "ISC_TASK_ID", row.getIscTaskId());
				put(result, "CREATE_TIME", row.getCreateTime()); put(result, "UPDATE_TIME", row.getUpdateTime());
				put(result, "OPT_USER", row.getOptUser()); put(result, "BADGE", row.getBadge());
				put(result, "PERSON_ID", row.getPersonId()); put(result, "APPLY_ID", row.getApplyId());
				put(result, "BATCH_ID", row.getBatchId());
				break;
			case ISC_DOWN:
				put(result, "ID", row.getId()); put(result, "PARK_ID", row.getParkId());
				put(result, "ACTION", row.getAction()); put(result, "DEVICE_TYPE", row.getDeviceType());
				put(result, "START_TIME", row.getStartAt()); put(result, "OVER_TIME", row.getOverAt());
				put(result, "DEVICE_CODE", row.getDeviceCode()); put(result, "CARD_NO", row.getCardNo());
				put(result, "GENERAL", row.getGeneral()); put(result, "IMAGE_ID", row.getImageId());
				put(result, "SERVICE_TYPE", row.getServiceType()); put(result, "REMARK", row.getRemark());
				put(result, "TASK_ID", row.getTaskId()); put(result, "TASK_TYPE", row.getTaskType());
				put(result, "CREATE_TIME", row.getCreateTime()); put(result, "OPT_USER", row.getOptUser());
				put(result, "BADGE", row.getBadge()); put(result, "PERSON_ID", row.getPersonId());
				break;
			case DIRECT_TASK:
				put(result, "ID", row.getId()); put(result, "IMAGE_ID", row.getImageId());
				put(result, "CARD_TYPE", row.getCardType()); put(result, "DEVICE_CODE", row.getDeviceCode());
				put(result, "CARD_NO", row.getCardNo()); put(result, "ACTION", row.getAction());
				put(result, "STATUS", row.getStatus()); put(result, "DEVICE_TYPE", row.getDeviceType());
				put(result, "START_TIME", row.getStartEpoch()); put(result, "OVER_TIME", row.getOverEpoch());
				put(result, "CREATE_TIME", row.getCreateTime()); put(result, "REMARK", row.getRemark());
				put(result, "CODE", row.getCode()); put(result, "CONSUME", row.getConsume());
				put(result, "TIMES", row.getTimes()); put(result, "UPDATE_TIME", row.getUpdateTime());
				put(result, "GENERAL", row.getGeneral()); put(result, "SERIAL_NO", row.getSerialNo());
				put(result, "SERVICE_TYPE", row.getServiceType());
				break;
			case DIRECT_DOWN:
				put(result, "ID", row.getId()); put(result, "PARK_ID", row.getParkId());
				put(result, "DEVICE_TYPE", row.getDeviceType()); put(result, "ACTION", row.getAction());
				put(result, "GENERAL", row.getGeneral()); put(result, "DEVICE_CODE", row.getDeviceCode());
				put(result, "CARD_NO", row.getCardNo()); put(result, "IMAGE_ID", row.getImageId());
				put(result, "START_TIME", row.getStartAt()); put(result, "OVER_TIME", row.getOverAt());
				put(result, "CREATE_TIME", row.getCreateTime()); put(result, "TASK_ID", row.getTaskId());
				put(result, "SERVICE_TYPE", row.getServiceType()); put(result, "TASK_TYPE", row.getTaskType());
				put(result, "REMARK", row.getRemark());
				break;
			default: throw new IllegalArgumentException("未知历史流");
		}
		return result;
	}

	private ObjectNode stableEvidence(FlowKind flow, RawCandidate row, Projection projection) {
		ObjectNode result = JSON.createObjectNode();
		result.put("sourceTable", flow.sourceTable());
		result.put("sourceRowId", String.valueOf(row.getId()));
		result.put("queryVersion", QUERY_VERSION);
		put(result, "rawParkId", row.getParkId());
		put(result, "evidenceParkId", row.getEvidenceParkId());
		put(result, "deviceParkId", row.getDeviceParkMin());
		put(result, "deviceParkCount", row.getDeviceParkCount());
		put(result, "staffCandidateId", row.getStaffCandidateId());
		put(result, "staffCandidateCount", row.getStaffCandidateCount());
		put(result, "relatedTaskId", row.getRelatedTaskId());
		put(result, "relatedTaskCount", row.getRelatedTaskCount());
		result.put("parkState", projection.parkState);
		result.put("identityState", projection.identityState);
		result.put("residueKind", projection.residueKind);
		result.put("reviewReason", projection.reviewReason);
		result.put("physicalState", "UNKNOWN");
		return result;
	}

	private Projection projection(FlowKind flow, RawCandidate row) {
		Projection result = new Projection();
		int parkCount = number(row.getDeviceParkCount());
		Integer persistentPark = flow == FlowKind.CURRENT_SOURCE ? row.getEvidenceParkId()
				: flow == FlowKind.ISC_DOWN || flow == FlowKind.DIRECT_DOWN ? row.getParkId() : null;
		if (parkCount > 1) {
			result.parkState = "CONFLICT"; result.parkId = null;
		} else if (persistentPark != null && parkCount > 0 && row.getDeviceParkMin() != null
				&& !persistentPark.equals(row.getDeviceParkMin())) {
			result.parkState = "CONFLICT"; result.parkId = persistentPark;
		} else if (persistentPark != null) {
			result.parkState = "KNOWN"; result.parkId = persistentPark;
		} else if (parkCount == 1 && row.getDeviceParkMin() != null) {
			result.parkState = "KNOWN"; result.parkId = row.getDeviceParkMin();
		} else {
			result.parkState = "UNKNOWN"; result.parkId = null;
		}

		int candidates = number(row.getStaffCandidateCount());
		result.identityState = candidates == 1 ? "UNIQUE" : candidates == 0 ? "UNKNOWN" : "AMBIGUOUS";
		result.staffId = candidates == 1 ? row.getStaffCandidateId() : null;
		result.accessType = Integer.valueOf(7).equals(row.getServiceType()) ? "FACE" : "PERSON";
		result.serviceFamily = Integer.valueOf(2).equals(row.getServiceType())
				? "APP_PERFECT_REVIEW" : "EMPLOYEE_ACCESS_REVIEW";
		result.externalTaskId = flow == FlowKind.ISC_TASK ? row.getIscTaskId() : null;
		int related = number(row.getRelatedTaskCount());
		if (flow == FlowKind.CURRENT_SOURCE) {
			result.residueKind = "SOURCE_BASELINE";
		} else if (flow == FlowKind.ISC_TASK || flow == FlowKind.DIRECT_TASK) {
			result.residueKind = related > 0 ? "CORRELATED_TASK" : "TASK_ONLY";
			result.relatedTaskRef = related > 0 ? flow.name().replace("TASK", "DOWN") + ":" + row.getRelatedTaskId() : null;
		} else {
			result.residueKind = related > 0 ? "CORRELATED_DOWN" : "DOWN_ONLY";
			result.relatedTaskRef = related > 0 ? flow.name().replace("DOWN", "TASK") + ":" + row.getRelatedTaskId() : null;
		}

		List<String> reasons = new ArrayList<>();
		if (!"KNOWN".equals(result.parkState)) reasons.add("PARK_" + result.parkState);
		if (!"UNIQUE".equals(result.identityState)) reasons.add("IDENTITY_" + result.identityState);
		if (Integer.valueOf(2).equals(row.getServiceType())) reasons.add("APP_PERFECT_REVIEW");
		if (flow == FlowKind.ISC_DOWN && Integer.valueOf(1).equals(row.getServiceType()) && related == 0) {
			reasons.add("SERVICE_AMBIGUOUS_NORMALIZED");
		}
		if (result.residueKind.endsWith("_ONLY")) reasons.add(result.residueKind);
		result.reviewReason = reasons.isEmpty() ? "LEGACY_ROW_DISCOVERED" : String.join(",", reasons);
		result.reviewState = reasons.isEmpty() ? "DISCOVERED" : "REVIEW_REQUIRED";
		return result;
	}

	private static int number(Integer value) { return value == null ? 0 : value; }

	private static void put(ObjectNode node, String name, Object value) {
		if (value == null) node.putNull(name);
		else if (value instanceof Integer) node.put(name, (Integer) value);
		else if (value instanceof Long) node.put(name, (Long) value);
		else if (value instanceof LocalDateTime) node.put(name, timestamp((LocalDateTime) value));
		else node.put(name, String.valueOf(value));
	}

	private static String timestamp(LocalDateTime value) {
		return value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	private static String json(JsonNode value) {
		try { return JSON.writeValueAsString(value); }
		catch (Exception impossible) { throw new IllegalStateException("历史JSON编码失败", impossible); }
	}

	private static void removeVolatile(JsonNode node) {
		if (node.isObject()) {
			ObjectNode object = (ObjectNode) node;
			object.remove("capturedAt"); object.remove("sourceReadAt");
			Iterator<JsonNode> children = object.elements();
			while (children.hasNext()) removeVolatile(children.next());
		} else if (node.isArray()) {
			for (JsonNode child : node) removeVolatile(child);
		}
	}

	private static final class Projection {
		private Integer parkId;
		private String parkState;
		private String accessType;
		private String serviceFamily;
		private Long staffId;
		private String identityState;
		private String residueKind;
		private String relatedTaskRef;
		private String externalTaskId;
		private String reviewState;
		private String reviewReason;
	}
}
