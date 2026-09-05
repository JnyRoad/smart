package com.tce.smart.platform.controller.admittance;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.admittance.VisitorManualAuthReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorManualAuthOptionsRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.VisitorManualAuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 入厂申请管理端操作。
 *
 * 访客 H5 的 /admittance/** 为历史放行路径，管理端敏感操作必须使用独立受保护路由。
 */
@RestController
@Api(tags = "platform-入厂申请管理")
@RequestMapping("/manage/admittance/apply")
public class SmtAdmittanceApplyManageController extends BaseController {

	private final SmtAdmittanceApplyService smtAdmittanceApplyService;
	private final VisitorManualAuthService visitorManualAuthService;

	/**
	 * 注入既有申请管理服务和手动授权服务，确保两个管理端入口使用同一受保护控制器。
	 */
	@Autowired
	public SmtAdmittanceApplyManageController(SmtAdmittanceApplyService smtAdmittanceApplyService,
			VisitorManualAuthService visitorManualAuthService) {
		this.smtAdmittanceApplyService = smtAdmittanceApplyService;
		this.visitorManualAuthService = visitorManualAuthService;
	}

	/**
	 * 作废入厂申请，并同步提交其人员、车辆的通行权限回收任务。
	 *
	 * @param id 入厂申请单 ID
	 * @return 是否成功作废
	 */
	@SysLog("管理端作废入厂申请")
	@ApiOperation("管理端作废入厂申请")
	@PreAuthorize("@pms.hasPermission('platform_visitor_incoming_revoke')")
	@PostMapping("/revoke")
	public Result<Boolean> revokeApply(@RequestParam("id") String id) {
		Long applyId = parseApplyId(id);
		SmtAdmittanceApply apply = smtAdmittanceApplyService.getById(applyId);
		return success(smtAdmittanceApplyService.revokeApply(apply));
	}

	/**
	 * 查询管理端手动授权选项；申请权限和园区范围由服务层再次校验。
	 */
	@ApiOperation("查询访客手动授权选项")
	@PreAuthorize("@pms.hasPermission('platform_visitor_incoming_auth')")
	@GetMapping("/device/auth/options")
	public Result<VisitorManualAuthOptionsRespDTO> getManualAuthOptions(@RequestParam("applyId") String applyId) {
		return success(visitorManualAuthService.getOptions(parseApplyId(applyId)));
	}

	/**
	 * 为当前申请中的人员创建一个手动下发批次，日期只取数据库申请单值。
	 */
	@SysLog("管理端手动下发访客人员权限")
	@ApiOperation("管理端手动下发访客人员权限")
	@PreAuthorize("@pms.hasPermission('platform_visitor_incoming_auth')")
	@PostMapping("/device/auth")
	public Result<String> manualAuth(@RequestBody VisitorManualAuthReqDTO request) {
		return success(visitorManualAuthService.submit(request));
	}

	/**
	 * 管理端路径参数必须是正整数，非法值按申请单不存在处理。
	 */
	private Long parseApplyId(String value) {
		try {
			long applyId = Long.parseLong(value);
			if (applyId <= 0) {
				throw new NumberFormatException("non-positive");
			}
			return applyId;
		} catch (NumberFormatException error) {
			throw new SmartException("申请单不存在");
		}
	}
}
