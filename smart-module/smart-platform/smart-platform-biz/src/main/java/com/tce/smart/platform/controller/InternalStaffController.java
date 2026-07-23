package com.tce.smart.platform.controller;

import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.InternalStaffBindingRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffIdentityRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffModuleRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffPasswordRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffProvisioningRespDTO;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.service.SmtStaffService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仅供服务间调用的员工最小资料接口。
 *
 * 旧的员工实体查询端点已下线；此处按调用用途显式投影字段，避免新增实体字段被跨服务透传。
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/internal/staff")
public class InternalStaffController extends BaseController {

	private final SmtStaffService smtStaffService;

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
			@RequestHeader(SecurityConstants.FROM) String from) {
		assertInternalFrom(from);
		SmtStaff staff = findStaff(badge);
		InternalStaffPasswordRespDTO response = staff == null ? null : toPasswordResponse(staff);
		log.info("内部员工密码资料查询完成 callerService={} purpose=password success={}", callerService(), response != null);
		return success(response);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/ocr/{badge}")
	public Result<InternalStaffIdentityRespDTO> getIdentityStaff(@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from) {
		assertInternalFrom(from);
		SmtStaff staff = findStaff(badge);
		InternalStaffIdentityRespDTO response = staff == null ? null : toIdentityResponse(staff);
		// 身份资料仅供 OCR 与银行实名服务端流程使用；日志不得包含工号、姓名或证件号。
		log.info("内部员工身份资料查询完成 callerService={} purpose=ocr-or-bank success={}", callerService(), response != null);
		return success(response);
	}

	/**
	 * 仅供 UPMS 在创建或更新本地账号时取得必要资料，不能用于通用人员查询。
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/provisioning/{badge}")
	public Result<InternalStaffProvisioningRespDTO> getProvisioningStaff(@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from) {
		assertInternalFrom(from);
		SmtStaff staff = findStaff(badge);
		InternalStaffProvisioningRespDTO response = staff == null ? null : toProvisioningResponse(staff);
		log.info("内部员工账号开通资料查询完成 callerService={} purpose=upms-provisioning success={}", callerService(), response != null);
		return success(response);
	}

	private void assertInternalFrom(String from) {
		if (!SecurityConstants.FROM_IN.equals(from)) {
			throw new AccessDeniedException("仅允许内部服务调用");
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
}
