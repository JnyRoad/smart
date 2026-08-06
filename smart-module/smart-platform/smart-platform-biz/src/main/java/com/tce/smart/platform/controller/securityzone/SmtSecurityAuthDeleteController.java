package com.tce.smart.platform.controller.securityzone;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthDeleteReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthDeleteRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityConfigParkListRespDTO;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthDeleteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author fushiping
 * @date 2021-07-29 11:13:24
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-保密区配置-权限删除配置")
@RequestMapping("/security/auth/delete")
public class SmtSecurityAuthDeleteController extends BaseController {

	private final SmtSecurityAuthDeleteService smtSecurityAuthDeleteService;

	/**
	 * 配置查询
	 *
	 * @param parkId
	 * @return
	 */
	@ApiOperation("配置查询")
	@GetMapping("/get")
	public Result getConfig(@RequestParam("parkId") Integer parkId) {
		return success(smtSecurityAuthDeleteService.getConfig(parkId), SecurityAuthDeleteRespDTO.class);
	}

	/**
	 * 配置分页查询
	 *
	 * @return
	 */
	@ApiOperation("配置分页查询")
	@GetMapping("/getPage")
	public Result getConfigList(Page page) {
		return success(smtSecurityAuthDeleteService.getList(page), SecurityConfigParkListRespDTO.class);
	}

	/**
	 * 自动删除任务
	 *
	 * @param
	 * @return
	 */
	@ApiOperation("自动删除任务")
	@GetMapping("/task")
	public Result getConfig() {
		smtSecurityAuthDeleteService.deleteAuthTask();
		return success();
	}

	/**
	 * 编辑配置
	 *
	 * @param reqDTO
	 * @return Result
	 */
	@ApiOperation("编辑配置")
	@SysLog("编辑配置")
	@PostMapping
	public Result editConfig(@RequestBody(required = false) SecurityAuthDeleteReqDTO reqDTO) {
		return success(smtSecurityAuthDeleteService.editConfig(reqDTO));
	}

}
