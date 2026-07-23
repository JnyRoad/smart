package com.tce.smart.platform.controller;

import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.InternalStaffPhoneUpdateReqDTO;
import com.tce.smart.platform.api.dto.req.InternalStaffFaceLoginReqDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffBindingRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffIdentityRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffLoginRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffModuleRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffPasswordRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffPhoneRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffProvisioningRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffSelfProfileRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffFaceLoginRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalScheduleIscPersonRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalScheduleStaffIdentityRespDTO;
import com.tce.smart.platform.api.dto.resp.MyDormitoryRespDTO;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffEmergency;
import com.tce.smart.platform.core.vo.StaffInfoVO;
import com.tce.smart.platform.service.SmtStaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 仅供服务间调用的员工最小资料接口。
 *
 * 旧的员工实体查询端点已下线；此处按调用用途显式投影字段，避免新增实体字段被跨服务透传。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/staff")
public class InternalStaffController extends BaseController {
	private static final String APP_SERVICE_CLIENT_ID = "app";
	private static final List<String> IDENTITY_PURPOSES = Arrays.asList("ocr-compare", "icbc-eaccount");
	private static final List<String> PASSWORD_STAFF_PURPOSES = Collections.singletonList("password-face-verify");
	private static final List<String> PASSWORD_PHONE_PURPOSES = Arrays.asList("password-reset", "self-phone-verify");
	private static final List<String> SELF_PROFILE_PURPOSES = Collections.singletonList("self-profile");
	private static final List<String> PHONE_UPDATE_PURPOSES = Collections.singletonList("phone-update");
	private static final List<String> PROVISIONING_PURPOSES = Collections.singletonList("upms-provisioning");
	private static final List<String> MOBILE_LOGIN_PURPOSES = Collections.singletonList("upms-mobile-login");
	private static final List<String> FACE_LOGIN_PURPOSES = Collections.singletonList("face-login");
	private static final List<String> DORMITORY_PURPOSES = Collections.singletonList("my-dormitory");

	private final SmtStaffService smtStaffService;
	private final OpenApiAuthenticationAdapter openApiAuthenticationAdapter;

	/**
	 * UPMS 的服务客户端由受管配置提供。Nacos 未配置时拒绝账号开通和手机号登录，
	 * 不能用猜测的 client_id 放宽权限。
	 */
	@Value("${security.inner.staff.upms-client-id:}")
	private String upmsServiceClientId;

	/** Smart Schedule 的服务客户端由受管配置提供；未配置时拒绝敏感 ISC 员工资料查询。 */
	@Value("${security.inner.staff.schedule-client-id:}")
	private String scheduleServiceClientId;

	@Inner
	@OpenApi("server")
	@GetMapping("/binding/{badge}")
	public Result<InternalStaffBindingRespDTO> getBindingStaff(@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from) {
		assertInternalFrom(from);
		SmtStaff staff = findStaff(badge);
		InternalStaffBindingRespDTO response = staff == null ? null : toBindingResponse(staff);
		log.info("内部员工绑定资料查询完成 callerService={} purpose=binding success={}", callerService(), response != null);
		return success(response);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/module/{badge}")
	public Result<InternalStaffModuleRespDTO> getModuleStaff(@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from) {
		assertInternalFrom(from);
		SmtStaff staff = findStaff(badge);
		InternalStaffModuleRespDTO response = staff == null ? null : toModuleResponse(staff);
		log.info("内部员工模块资料查询完成 callerService={} purpose=module success={}", callerService(), response != null);
		return success(response);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/password/{badge}")
	public Result<InternalStaffPasswordRespDTO> getPasswordStaff(@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose) {
		assertInternalFrom(from);
		assertCallerAndPurpose(purpose, PASSWORD_STAFF_PURPOSES);
		SmtStaff staff = findStaff(badge);
		InternalStaffPasswordRespDTO response = staff == null ? null : toPasswordResponse(staff);
		log.info("内部员工密码资料查询完成 callerService={} purpose=password success={}", callerService(), response != null);
		return success(response);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/ocr/{badge}")
	public Result<InternalStaffIdentityRespDTO> getIdentityStaff(@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose) {
		assertInternalFrom(from);
		assertIdentityCallerAndPurpose(purpose);
		SmtStaff staff = findStaff(badge);
		InternalStaffIdentityRespDTO response = staff == null ? null : toIdentityResponse(staff);
		// 身份资料仅供 OCR 与银行实名服务端流程使用；日志不得包含工号、姓名或证件号。
		log.info("内部员工身份资料查询完成 callerService={} purpose={} success={}", callerService(), purpose, response != null);
		return success(response);
	}

	/** UPMS 手机号登录补建账号时按手机号查找可用员工，仅返回初始化密码所需字段。 */
	@Inner
	@OpenApi("server")
	@GetMapping("/login/mobile/{mobile}")
	public Result<List<InternalStaffLoginRespDTO>> getLoginStaffByMobile(@PathVariable("mobile") String mobile,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose) {
		assertInternalFrom(from);
		assertUpmsCallerAndPurpose(purpose, MOBILE_LOGIN_PURPOSES);
		List<SmtStaff> staffs = smtStaffService.findStaffByMobileForLogin(mobile);
		if (staffs == null || staffs.isEmpty()) {
			return success(Collections.emptyList());
		}
		List<InternalStaffLoginRespDTO> response = new ArrayList<>();
		for (SmtStaff staff : staffs) {
			InternalStaffLoginRespDTO item = new InternalStaffLoginRespDTO();
			item.setBadge(staff.getBadge());
			item.setCertNoLast6(lastSix(staff.getCertno()));
			response.add(item);
		}
		log.info("内部手机号登录员工查询完成 callerService={} purpose=upms-mobile-login resultSize={}", callerService(), response.size());
		return success(response);
	}

	/** 密码找回仅供 App 服务端取得手机号；客户端只能收到脱敏值。 */
	@Inner
	@OpenApi("server")
	@GetMapping("/password-phone/{badge}")
	public Result<InternalStaffPhoneRespDTO> getPasswordPhone(@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose) {
		assertInternalFrom(from);
		assertCallerAndPurpose(purpose, PASSWORD_PHONE_PURPOSES);
		SmtStaff staff = findStaff(badge);
		InternalStaffPhoneRespDTO response = null;
		if (staff != null) {
			response = new InternalStaffPhoneRespDTO();
			response.setPhone(staff.getPhone());
			response.setMaskedPhone(maskPhone(staff.getPhone()));
		}
		log.info("内部密码找回手机号查询完成 callerService={} purpose=password-sms success={}", callerService(), response != null);
		return success(response);
	}

	/** 员工本人资料页的内部投影，调用方必须先把外部员工号约束为认证主体。 */
	@Inner
	@OpenApi("server")
	@GetMapping("/self-profile/{badge}")
	public Result<InternalStaffSelfProfileRespDTO> getSelfProfile(@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose) {
		assertInternalFrom(from);
		assertCallerAndPurpose(purpose, SELF_PROFILE_PURPOSES);
		StaffInfoVO baseInfo = smtStaffService.getBaseinfoById(badge);
		StaffInfoVO fullInfo = smtStaffService.getSmtStaffInfoByBadge(badge);
		InternalStaffSelfProfileRespDTO response = toSelfProfileResponse(baseInfo, fullInfo);
		log.info("内部员工本人资料查询完成 callerService={} purpose=self-profile success={}", callerService(), response != null);
		return success(response);
	}

	/** 服务端在外部人资系统更新成功后回写员工手机号。 */
	@Inner
	@OpenApi("server")
	@PostMapping("/phone")
	public Result<Boolean> updatePhone(@RequestBody InternalStaffPhoneUpdateReqDTO request,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose) {
		assertInternalFrom(from);
		assertCallerAndPurpose(purpose, PHONE_UPDATE_PURPOSES);
		if (request == null || StrUtil.isBlank(request.getBadge()) || StrUtil.isBlank(request.getPhone())) {
			throw new AccessDeniedException("手机号更新参数不完整");
		}
		SmtStaff staff = new SmtStaff();
		staff.setBadge(request.getBadge());
		staff.setPhone(request.getPhone());
		Result result = smtStaffService.updatePhone(staff);
		log.info("内部员工手机号更新完成 callerService={} purpose=phone-update success={}", callerService(), result.isSuccess());
		return success(result.isSuccess());
	}

	/** 人脸登录认证过滤器专用端点，认证链路只需要工号且不得返回员工实体。 */
	@Inner
	@OpenApi("server")
	@PostMapping("/face-login")
	public Result<InternalStaffFaceLoginRespDTO> faceLogin(@RequestBody InternalStaffFaceLoginReqDTO request,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose) {
		assertInternalFrom(from);
		assertCallerAndPurpose(purpose, FACE_LOGIN_PURPOSES);
		if (request == null || StrUtil.isBlank(request.getFacePic()) || StrUtil.isBlank(request.getDeviceNo())
				|| !FACE_LOGIN_PURPOSES.contains(purpose)) {
			throw new AccessDeniedException("人脸登录调用未获授权");
		}
		SmtStaff staff = smtStaffService.faceSearchForLogin(request.getFacePic().replaceAll("[\\t\\n\\r]", ""), request.getDeviceNo());
		InternalStaffFaceLoginRespDTO response = null;
		if (staff != null) {
			response = new InternalStaffFaceLoginRespDTO();
			response.setBadge(staff.getBadge());
		}
		log.info("内部人脸登录查询完成 callerService={} purpose={} success={}", callerService(), purpose, response != null);
		return success(response);
	}

	/** 员工本人宿舍查询的内部投影，调用方不得再构造员工实体。 */
	@Inner
	@OpenApi("server")
	@GetMapping("/dormitory/{badge}")
	public Result<MyDormitoryRespDTO> getMyDormitory(@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose) {
		assertInternalFrom(from);
		assertCallerAndPurpose(purpose, DORMITORY_PURPOSES);
		SmtStaff staff = new SmtStaff();
		staff.setBadge(badge);
		log.info("内部员工宿舍查询完成 callerService={} purpose=my-dormitory", callerService());
		return success(smtStaffService.myDormitory(staff), MyDormitoryRespDTO.class);
	}

	/**
	 * 仅供 UPMS 在创建或更新本地账号时取得必要资料，不能用于通用人员查询。
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/provisioning/{badge}")
	public Result<InternalStaffProvisioningRespDTO> getProvisioningStaff(@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose) {
		assertInternalFrom(from);
		assertUpmsCallerAndPurpose(purpose, PROVISIONING_PURPOSES);
		SmtStaff staff = findStaff(badge);
		InternalStaffProvisioningRespDTO response = staff == null ? null : toProvisioningResponse(staff);
		log.info("内部员工账号开通资料查询完成 callerService={} purpose=upms-provisioning success={}", callerService(), response != null);
		return success(response);
	}

	/** ISC 人员创建专用资料：完整证件号只能由 Smart Schedule 服务端下发至 ISC。 */
	@Inner
	@OpenApi("server")
	@GetMapping("/schedule/isc-person/{staffId}")
	public Result<InternalScheduleIscPersonRespDTO> getScheduleIscPersonStaff(@PathVariable("staffId") Long staffId,
			@RequestHeader(SecurityConstants.FROM) String from) {
		assertInternalFrom(from);
		assertScheduleCaller();
		SmtStaff staff = findStaffById(staffId);
		InternalScheduleIscPersonRespDTO response = staff == null ? null : toScheduleIscPersonResponse(staff);
		log.info("Schedule ISC 人员创建资料查询完成 callerService={} purpose=isc-person success={}", callerService(), response != null);
		return success(response);
	}

	/** ISC 人员查询、删除和卡片下发专用的身份投影，禁止回传完整员工实体。 */
	@Inner
	@OpenApi("server")
	@GetMapping("/schedule/identity/{staffId}")
	public Result<InternalScheduleStaffIdentityRespDTO> getScheduleIdentityStaff(@PathVariable("staffId") Long staffId,
			@RequestHeader(SecurityConstants.FROM) String from) {
		assertInternalFrom(from);
		assertScheduleCaller();
		SmtStaff staff = findStaffById(staffId);
		InternalScheduleStaffIdentityRespDTO response = staff == null ? null : toScheduleIdentityResponse(staff);
		log.info("Schedule ISC 身份资料查询完成 callerService={} purpose=isc-identity success={}", callerService(), response != null);
		return success(response);
	}

	private void assertInternalFrom(String from) {
		if (!SecurityConstants.FROM_IN.equals(from)) {
			throw new AccessDeniedException("仅允许内部服务调用");
		}
	}

	/**
	 * 身份证号属于高敏感资料：除服务令牌外，还必须限定为 App 服务客户端并声明已审核用途。
	 */
	private void assertIdentityCallerAndPurpose(String purpose) {
		assertCallerAndPurpose(purpose, IDENTITY_PURPOSES);
	}

	/**
	 * 读取身份证、手机号和本人资料等敏感投影时，服务令牌还必须是受审核的纯 App 客户端，
	 * 并带上端点级用途。目的在于阻止任意 server scope 客户端按工号横向查询。
	 */
	private void assertCallerAndPurpose(String purpose, List<String> allowedPurposes) {
		assertCallerAndPurpose(purpose, allowedPurposes, APP_SERVICE_CLIENT_ID);
	}

	private void assertUpmsCallerAndPurpose(String purpose, List<String> allowedPurposes) {
		if (StrUtil.isBlank(upmsServiceClientId)) {
			throw new AccessDeniedException("UPMS 内部客户端尚未受管配置");
		}
		assertCallerAndPurpose(purpose, allowedPurposes, upmsServiceClientId);
	}

	/**
	 * Schedule 身份资料接口不接收可伪造的用途参数，只接受受管 Schedule client_credentials 主体。
	 * {@link OpenApi} 负责 scope=server 校验，此处把 client_id 再约束为 Schedule 专用配置。
	 */
	private void assertScheduleCaller() {
		if (StrUtil.isBlank(scheduleServiceClientId)) {
			throw new AccessDeniedException("Schedule 内部客户端尚未受管配置");
		}
		Authentication authentication = SecurityUtils.getAuthentication();
		if (authentication == null || !openApiAuthenticationAdapter.isClientOnly(authentication)
				|| !scheduleServiceClientId.equals(openApiAuthenticationAdapter.clientId(authentication))) {
			throw new AccessDeniedException("Schedule 身份资料调用未获授权");
		}
	}

	private void assertCallerAndPurpose(String purpose, List<String> allowedPurposes, String allowedClientId) {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (authentication == null || !openApiAuthenticationAdapter.isClientOnly(authentication)
				|| !allowedClientId.equals(openApiAuthenticationAdapter.clientId(authentication))
				|| !allowedPurposes.contains(purpose)) {
			throw new AccessDeniedException("内部敏感资料调用未获授权");
		}
	}

	/**
	 * 服务令牌认证后的主体名用于审计调用服务；不可用时使用固定占位符，避免写入员工身份资料。
	 */
	private String callerService() {
		Authentication authentication = SecurityUtils.getAuthentication();
		return authentication == null || StrUtil.isBlank(authentication.getName()) ? "unknown" : authentication.getName();
	}

	private SmtStaff findStaff(String badge) {
		return StrUtil.isBlank(badge) ? null : smtStaffService.getSimpleSttaffByBadge(badge);
	}

	private SmtStaff findStaffById(Long staffId) {
		return staffId == null ? null : smtStaffService.getById(staffId);
	}

	private InternalScheduleIscPersonRespDTO toScheduleIscPersonResponse(SmtStaff staff) {
		InternalScheduleIscPersonRespDTO response = new InternalScheduleIscPersonRespDTO();
		response.setBadge(staff.getBadge());
		response.setName(staff.getName());
		response.setSex(staff.getSex());
		response.setBirth(staff.getBirth());
		response.setCertno(staff.getCertno());
		return response;
	}

	private InternalScheduleStaffIdentityRespDTO toScheduleIdentityResponse(SmtStaff staff) {
		InternalScheduleStaffIdentityRespDTO response = new InternalScheduleStaffIdentityRespDTO();
		response.setBadge(staff.getBadge());
		response.setCertno(staff.getCertno());
		response.setStatus(staff.getStatus());
		return response;
	}

	private InternalStaffBindingRespDTO toBindingResponse(SmtStaff staff) {
		InternalStaffBindingRespDTO response = new InternalStaffBindingRespDTO();
		response.setStaffId(staff.getId());
		response.setBadge(staff.getBadge());
		response.setName(staff.getName());
		response.setStatus(staff.getStatus());
		response.setCertNoLast6(lastSix(staff.getCertno()));
		return response;
	}

	private InternalStaffModuleRespDTO toModuleResponse(SmtStaff staff) {
		InternalStaffModuleRespDTO response = new InternalStaffModuleRespDTO();
		response.setBadge(staff.getBadge());
		response.setCompId(staff.getCompId());
		return response;
	}

	private InternalStaffPasswordRespDTO toPasswordResponse(SmtStaff staff) {
		InternalStaffPasswordRespDTO response = new InternalStaffPasswordRespDTO();
		response.setStaffId(staff.getId());
		response.setBadge(staff.getBadge());
		response.setFacePicId(staff.getFacePicId());
		return response;
	}

	private InternalStaffIdentityRespDTO toIdentityResponse(SmtStaff staff) {
		InternalStaffIdentityRespDTO response = new InternalStaffIdentityRespDTO();
		response.setStaffId(staff.getId());
		response.setBadge(staff.getBadge());
		response.setName(staff.getName());
		response.setCertno(staff.getCertno());
		return response;
	}

	private InternalStaffProvisioningRespDTO toProvisioningResponse(SmtStaff staff) {
		InternalStaffProvisioningRespDTO response = new InternalStaffProvisioningRespDTO();
		response.setStatus(staff.getStatus());
		response.setCertNoLast6(lastSix(staff.getCertno()));
		response.setPhone(staff.getPhone());
		return response;
	}


	private String lastSix(String certNo) {
		return StrUtil.isBlank(certNo) || certNo.length() < 6 ? null : certNo.substring(certNo.length() - 6);
	}

	private String maskPhone(String phone) {
		return StrUtil.isBlank(phone) || phone.length() < 7 ? null
				: phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
	}

	private InternalStaffSelfProfileRespDTO toSelfProfileResponse(StaffInfoVO baseInfo, StaffInfoVO fullInfo) {
		if (baseInfo == null || baseInfo.getSmtStaff() == null) {
			return null;
		}
		SmtStaff staff = baseInfo.getSmtStaff();
		InternalStaffSelfProfileRespDTO response = new InternalStaffSelfProfileRespDTO();
		response.setBadge(staff.getBadge());
		response.setName(staff.getName());
		response.setPhone(staff.getPhone());
		response.setSex(staff.getSex());
		response.setCompName(staff.getCompName());
		response.setDepName(staff.getDepName());
		response.setJobName(staff.getJobName());
		response.setJcheName(staff.getJcheName());
		response.setWelfareLevel(staff.getWelfareLevel());
		response.setFacePicId(staff.getFacePicId());
		response.setCreateTime(staff.getCreateTime());
		response.setCertno(staff.getCertno());
		response.setEmail(staff.getEmail());
		response.setEmpType(staff.getEmpType());
		response.setDormitoryState(baseInfo.getDormitoryState());
		response.setDormitoryStateDesc(baseInfo.getDormitoryStateDesc());
		response.setApplyState(baseInfo.getApplyState());
		response.setApplyStateDesc(baseInfo.getApplyStateDesc());
		response.setVehicleState(baseInfo.getVehicleState());
		response.setVehicleStateDesc(baseInfo.getVehicleStateDesc());
		response.setStatus(baseInfo.getStatus());
		response.setStatusDes(baseInfo.getStatusDes());
		response.setEmpTypeDes(baseInfo.getEmpTypeDes());
		response.setParkName(fullInfo == null ? null : fullInfo.getParkName());
		response.setFacePic(fullInfo == null ? null : fullInfo.getFacePic());
		if (fullInfo != null && fullInfo.getSmtStaffEmergency() != null && !fullInfo.getSmtStaffEmergency().isEmpty()) {
			SmtStaffEmergency emergency = fullInfo.getSmtStaffEmergency().get(0);
			response.setEmergencyRelation(emergency.getRelation());
			response.setEmergencyName(emergency.getEmergencyName());
			response.setEmergencyPhone(emergency.getTelephont());
		}
		return response;
	}
}
