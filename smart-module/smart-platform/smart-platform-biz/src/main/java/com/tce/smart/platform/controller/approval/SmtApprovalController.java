package com.tce.smart.platform.controller.approval;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.approval.EditApprovalReqDTO;
import com.tce.smart.platform.core.entity.SmtApproval;
import com.tce.smart.platform.core.service.SmtApprovalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import java.util.Objects;

/**
 * 审批事务表
 *
 * @author fushiping
 * @date 2021-04-08 16:25:32
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-审批列表")
@RequestMapping("/approval/list")
public class SmtApprovalController extends BaseController {

	private final SmtApprovalService smtApprovalService;


	/**
	 * 分页查询
	 *
	 * @param page        分页对象
	 * @param
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperation("分页对象")
	public Result getSmtApprovalPage(Page page, @RequestParam("eventCode") Integer eventCode) {
		return success(smtApprovalService.page(page, Wrappers.<SmtApproval>query().lambda()
				.eq(Objects.nonNull(eventCode), SmtApproval::getEventCode, eventCode)
				.in(SmtApproval::getParkId, SecurityUtils.getUser().getParkIdList())));
	}


	/**
	 * 通过id查询
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/{id}")
	@ApiOperation("通过id查询")
	public Result getById(@PathVariable("id") Integer id) {
		return success(smtApprovalService.getById(id));
	}

	/**
	 * 新增
	 *
	 * @param smtApproval
	 * @return Result
	 */
	@SysLog("新增")
	@PostMapping
	@ApiOperation("新增")
	public Result save(@RequestBody EditApprovalReqDTO smtApproval) {
		SmtApproval approval = BeanUtils.transform(SmtApproval.class, smtApproval);
		return success(smtApprovalService.saveApproval(approval));
	}


	/**
	 * 通过id删除
	 *
	 * @param id id
	 * @return Result
	 */
	@SysLog("删除")
	@PostMapping("/{id}")
	@ApiOperation("删除")
	public Result removeById(@PathVariable Integer id) {
		return success(smtApprovalService.removeById(id));
	}

}
