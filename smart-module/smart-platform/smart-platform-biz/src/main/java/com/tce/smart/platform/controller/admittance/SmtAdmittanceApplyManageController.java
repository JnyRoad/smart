package com.tce.smart.platform.controller.admittance;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 入厂申请管理端操作。
 *
 * 访客 H5 的 /admittance/** 为历史放行路径，管理端敏感操作必须使用独立受保护路由。
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-入厂申请管理")
@RequestMapping("/manage/admittance/apply")
public class SmtAdmittanceApplyManageController extends BaseController {

	private final SmtAdmittanceApplyService smtAdmittanceApplyService;

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
		Long applyId;
		try {
			applyId = Long.parseLong(id);
		} catch (NumberFormatException error) {
			throw new SmartException("申请单不存在");
		}
		SmtAdmittanceApply apply = smtAdmittanceApplyService.getById(applyId);
		return success(smtAdmittanceApplyService.revokeApply(apply));
	}
}
