package com.tce.smart.platform.controller.admittance;

import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction;
import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceApplyReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.VisitorReceptionistSearchReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceApplyDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorReceptionistRespDTO;
import com.tce.smart.platform.api.dto.req.admittance.AdmittanceFellowReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.AdmittanceVehicleReqDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceAreaOptionsService;
import com.tce.smart.platform.service.admittance.VisitorFaceCropCapabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.tce.smart.tool.enums.AdmittanceCauseEnum;
import com.tce.smart.tool.enums.AdmittanceVehicleCertTypeEnum;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAreaOptionsRespDTO;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 微信访客匿名入口。
 *
 * 每次查询或提交均消费一张草稿会话派生的 capability，且 OAuth 身份只存在于 Redis 草稿，
 * 因此不能以裸接口、工号或浏览器提供的 unionId 执行员工查询和访客落库。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admittance/visitor-entry")
public class VisitorEntryController extends BaseController {
	private static final DateTimeFormatter CAPABILITY_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final String ACTION_CAPABILITY_HEADER = "X-Visitor-Action-Capability";
	private static final String DRAFT_TOKEN_HEADER = "X-Visitor-Draft-Token";
	private static final String DRAFT_ID_HEADER = "X-Visitor-Draft-Id";

	private final VisitorFaceCropCapabilityService capabilityService;
	private final SmtAdmittanceApplyService applyService;
	private final SmtAdmittanceAreaOptionsService areaOptionsService;

	@PostMapping("/receptionist")
	public Result<VisitorReceptionistRespDTO> searchReceptionist(
			@RequestHeader(value = ACTION_CAPABILITY_HEADER, required = false) String capability,
			@RequestHeader(value = DRAFT_ID_HEADER, required = false) String draftId,
			@RequestBody VisitorReceptionistSearchReqDTO request) {
		if (request == null || request.getParkId() == null || !StringUtils.hasText(request.getReceptionistName())
				|| !StringUtils.hasText(request.getReceptionistPhone())) {
			throw expired();
		}
		capabilityService.consumeActionCapability(capability, draftId, VisitorActionCapabilityAction.RECEPTIONIST_SEARCH,
				receptionistPayloadHash(request));
		SmtAdmittanceApply query = new SmtAdmittanceApply();
		query.setParkId(request.getParkId());
		query.setReceptionistName(request.getReceptionistName().replaceAll("\\s+", ""));
		query.setReceptionistPhone(request.getReceptionistPhone().replaceAll("\\s+", ""));
		SmtAdmittanceApply matched = applyService.searchReceptionist(query);
		if (matched == null || !StringUtils.hasText(matched.getReceptionistBadge()) || !StringUtils.hasText(matched.getReceptionistName())
				|| !StringUtils.hasText(matched.getReceptionistPhone())) {
			throw expired();
		}
		capabilityService.rememberReceptionistSelection(draftId, matched.getReceptionistBadge(), matched.getReceptionistName(),
				matched.getReceptionistPhone());
		VisitorReceptionistRespDTO response = new VisitorReceptionistRespDTO();
		response.setReceptionistBadge(matched.getReceptionistBadge());
		response.setReceptionistName(matched.getReceptionistName());
		response.setReceptionistPhone(maskPhone(matched.getReceptionistPhone()));
		return success(response);
	}

	@PostMapping("/apply")
	public Result<AdmittanceApplyDetailRespDTO> submitApply(
			@RequestHeader(value = ACTION_CAPABILITY_HEADER, required = false) String capability,
			@RequestHeader(value = DRAFT_TOKEN_HEADER, required = false) String draftToken,
			@RequestHeader(value = DRAFT_ID_HEADER, required = false) String draftId,
			@RequestBody SaveAdmittanceApplyReqDTO request) {
		if (request == null) {
			throw expired();
		}
		capabilityService.consumeActionCapability(capability, draftId, VisitorActionCapabilityAction.APPLY_SUBMIT,
				applyPayloadHash(request));
		VisitorFaceCropCapabilityService.VisitorReceptionistSelection selection = capabilityService
				.consumeReceptionistSelection(draftToken, draftId);
		// 忽略客户端给出的 unionId，只采用同一草稿会话中的服务端身份，避免伪造消息接收方。
		request.setUnionId(capabilityService.resolveUnionId(draftToken, draftId));
		request.setReceptionistBadge(selection.getReceptionistBadge());
		request.setReceptionistName(selection.getReceptionistName());
		request.setReceptionistPhone(selection.getReceptionistPhone());
		return success(applyService.saveAdmittanceApply(request), AdmittanceApplyDetailRespDTO.class);
	}

	/**
	 * 提交前重复/实名一致性校验也必须消费一次性草稿 capability，且接待人只取服务端选择。
	 */
	@PostMapping("/precheck")
	public Result<Boolean> precheckApply(
			@RequestHeader(value = ACTION_CAPABILITY_HEADER, required = false) String capability,
			@RequestHeader(value = DRAFT_TOKEN_HEADER, required = false) String draftToken,
			@RequestHeader(value = DRAFT_ID_HEADER, required = false) String draftId,
			@RequestBody SaveAdmittanceApplyReqDTO request) {
		if (request == null) {
			throw expired();
		}
		capabilityService.consumeActionCapability(capability, draftId, VisitorActionCapabilityAction.APPLY_PRECHECK,
				applyPayloadHash(request));
		VisitorFaceCropCapabilityService.VisitorReceptionistSelection selection = capabilityService
				.getReceptionistSelection(draftToken, draftId);
		request.setReceptionistBadge(selection.getReceptionistBadge());
		request.setReceptionistName(selection.getReceptionistName());
		request.setReceptionistPhone(selection.getReceptionistPhone());
		return success(applyService.visitorEqualCheck(request));
	}

	/** 只有短时 OAuth 草稿可读取无个人信息的表单枚举，避免恢复广泛匿名白名单。 */
	@GetMapping("/options/cause")
	public Result<List<Map<String, Object>>> getCauseOptions(
			@RequestHeader(value = DRAFT_TOKEN_HEADER, required = false) String draftToken,
			@RequestHeader(value = DRAFT_ID_HEADER, required = false) String draftId) {
		assertStaticOptionAccess(draftToken, draftId);
		return success(AdmittanceCauseEnum.getTypeList());
	}

	@GetMapping("/options/vehicle-cert")
	public Result<List<Map<String, Object>>> getVehicleCertOptions(
			@RequestHeader(value = DRAFT_TOKEN_HEADER, required = false) String draftToken,
			@RequestHeader(value = DRAFT_ID_HEADER, required = false) String draftId) {
		assertStaticOptionAccess(draftToken, draftId);
		return success(AdmittanceVehicleCertTypeEnum.getTypeList());
	}

	@GetMapping("/options/area-options")
	public Result<AdmittanceAreaOptionsRespDTO> getAreaOptions(
			@RequestHeader(value = DRAFT_TOKEN_HEADER, required = false) String draftToken,
			@RequestHeader(value = DRAFT_ID_HEADER, required = false) String draftId,
			@RequestParam("parkId") Integer parkId) {
		if (parkId == null) {
			throw expired();
		}
		assertStaticOptionAccess(draftToken, draftId);
		return success(areaOptionsService.getAreaOptions(parkId));
	}

	/** 将查询参数规范化后摘要化，使 capability 不能改用于另一位接待人。 */
	String receptionistPayloadHash(VisitorReceptionistSearchReqDTO request) {
		String payload = request.getParkId() + "|" + request.getReceptionistName().replaceAll("\\s+", "") + "|"
				+ request.getReceptionistPhone().replaceAll("\\s+", "");
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(payload.getBytes(StandardCharsets.UTF_8));
			StringBuilder result = new StringBuilder(digest.length * 2);
			for (byte item : digest) {
				result.append(String.format("%02x", item));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new SmartException("访客身份服务不可用");
		}
	}

	/** 提交 capability 绑定所有会影响入库、审批或设备权限的规范化申请字段。 */
	String applyPayloadHash(SaveAdmittanceApplyReqDTO request) {
		StringBuilder payload = new StringBuilder("visitor-apply-v1");
		appendFields(payload, request.getParkId(), request.getVisitorName(), request.getVisitorPhotoId(), request.getVisitorPhone(),
				request.getStartTime(), request.getEndTime(), request.getReceptionistBadge(), request.getReceptionistName(),
				request.getReceptionistPhone(), request.getRemark(), request.getCompany(), request.getPersonType(), request.getCause(),
				request.getThing(), request.getPermitFactoryType(), request.getPermitArea(), request.getPermitOldArea());
		appendList(payload, request.getAreaType());
		appendFellowList(payload, request.getFellowList());
		appendVehicleList(payload, request.getVehicleList());
		return sha256(payload.toString());
	}

	private void appendFellowList(StringBuilder payload, List<AdmittanceFellowReqDTO> fellows) {
		List<AdmittanceFellowReqDTO> values = fellows == null ? Collections.<AdmittanceFellowReqDTO>emptyList() : fellows;
		payload.append(values.size()).append(':');
		for (AdmittanceFellowReqDTO fellow : values) {
			appendFields(payload, fellow == null ? null : fellow.getFellowName(), fellow == null ? null : fellow.getFellowPhotoId(),
					fellow == null ? null : fellow.getCertNo(), fellow == null ? null : fellow.getCertType(),
					fellow == null ? null : fellow.getNativePlace(), fellow == null ? null : fellow.getFrontPhotoId(),
					fellow == null ? null : fellow.getIsMain());
		}
	}

	private void appendVehicleList(StringBuilder payload, List<AdmittanceVehicleReqDTO> vehicles) {
		List<AdmittanceVehicleReqDTO> values = vehicles == null ? Collections.<AdmittanceVehicleReqDTO>emptyList() : vehicles;
		payload.append(values.size()).append(':');
		for (AdmittanceVehicleReqDTO vehicle : values) {
			appendFields(payload, vehicle == null ? null : vehicle.getPlate(), vehicle == null ? null : vehicle.getName(),
					vehicle == null ? null : vehicle.getNativePlace(), vehicle == null ? null : vehicle.getLicenseNo(),
					vehicle == null ? null : vehicle.getEmergencyName(), vehicle == null ? null : vehicle.getEmergencyPhone(),
					vehicle == null ? null : vehicle.getModle(), vehicle == null ? null : vehicle.getVehicleType(),
					vehicle == null ? null : vehicle.getColour(), vehicle == null ? null : vehicle.getCertImg(),
					vehicle == null ? null : vehicle.getCertType());
		}
	}

	private void appendList(StringBuilder payload, List<?> values) {
		List<?> items = values == null ? Collections.emptyList() : values;
		payload.append(items.size()).append(':');
		for (Object item : items) {
			appendField(payload, item);
		}
	}

	private void appendFields(StringBuilder payload, Object... values) {
		for (Object value : values) {
			appendField(payload, value);
		}
	}

	private void appendField(StringBuilder payload, Object value) {
		String raw = value instanceof LocalDateTime ? CAPABILITY_DATE_TIME.format((LocalDateTime) value) : String.valueOf(value == null ? "" : value);
		String normalized = raw.trim().replaceAll("\\s+", " ");
		payload.append(normalized.length()).append(':').append(normalized);
	}

	private String sha256(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder result = new StringBuilder(digest.length * 2);
			for (byte item : digest) {
				result.append(String.format("%02x", item));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new SmartException("访客身份服务不可用");
		}
	}

	private String maskPhone(String phone) {
		String normalized = phone.replaceAll("\\s+", "");
		if (normalized.length() < 7) {
			return "***";
		}
		return normalized.substring(0, 3) + "****" + normalized.substring(normalized.length() - 4);
	}

	private SmartException expired() {
		return new SmartException("访客操作授权已失效，请重新进入申请流程");
	}

	private void assertStaticOptionAccess(String draftToken, String draftId) {
		try {
			capabilityService.assertStaticOptionAccess(draftToken, draftId);
		} catch (SmartException exception) {
			throw expired();
		}
	}
}
