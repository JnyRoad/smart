package com.tce.smart.platform.controller.securityzone;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.securityzone.OaAreaRelationEditReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.OaAreaAuthListRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.OaAreaRelationRespDTO;
import com.tce.smart.platform.service.securityzone.SmtOaAreaRelationService;
import com.tce.smart.tool.util.ToolUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OA同步区域与权限关联
 *
 * @author fushiping
 * @date 2021-07-29 11:13:44
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-保密区配置-OA区域关联配置")
@RequestMapping("/oa/area")
public class SmtOaAreaRelationController extends BaseController {

	private final SmtOaAreaRelationService smtOaAreaRelationService;

	/**
	 * 列表查询
	 *
	 * @param parkId
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperation("配置列表查询")
	public Result<List<OaAreaRelationRespDTO>> getAuthList(@RequestParam("parkId") Integer parkId) {
		return success(smtOaAreaRelationService.getList(parkId));
	}

	/**
	 * 列表查询
	 *
	 * @param parkId
	 * @return
	 */
	@GetMapping("/list/auth")
	@ApiOperation("权限列表查询")
	public Result getList(@RequestParam("parkId") Integer parkId, @RequestParam("areaId") String areaIds) {
		List<Integer> areaId = ToolUtils.splitInt(areaIds);
		return success(smtOaAreaRelationService.getListByAreaId(parkId, areaId), OaAreaAuthListRespDTO.class);
	}

	/**
	 * 编辑
	 *
	 * @param reqDTO
	 * @return Result
	 */
	@SysLog("编辑")
	@PostMapping
	@ApiOperation("编辑")
	public Result save(@RequestBody List<OaAreaRelationEditReqDTO> reqDTO) {
		return success(smtOaAreaRelationService.editRelation(reqDTO));
	}

	/**
	 * 根据区域id获得权限名
	 *
	 * @param ids
	 * @return Result
	 */
	@SysLog("根据区域id获得关联权限名")
	@GetMapping("/auth/name")
	@ApiOperation("根据区域id获得关联权限名")
	public Result getAuthName(@RequestParam("id") String ids, @RequestParam("parkId") Integer parkId) {
		List<Integer> id = ToolUtils.splitInt(ids);
		return success(smtOaAreaRelationService.getAuthNameByAreaId(parkId, id));
	}


}
