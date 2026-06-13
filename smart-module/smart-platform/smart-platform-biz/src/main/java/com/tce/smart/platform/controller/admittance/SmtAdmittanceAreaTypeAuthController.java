package com.tce.smart.platform.controller.admittance;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.admittance.AdmittanceAuthEditReqDTO;
import com.tce.smart.platform.service.admittance.SmtAdmittanceAreaTypeAuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 *
 * @author fushiping
 * @date 2021-08-17 17:45:23
 */
@RestController
@Api(tags = "platform-区域类型权限关联")
@AllArgsConstructor
@RequestMapping("/admittance/auth")
public class SmtAdmittanceAreaTypeAuthController extends BaseController {

  private final SmtAdmittanceAreaTypeAuthService smtAdmittanceAreaTypeAuthService;

  /**
   * 分页查询
   * @param parkId
   * @return
   */
  @ApiOperation("分页查询")
  @GetMapping("/list")
  public Result getList(@RequestParam("parkId") Integer parkId) {
    return success(smtAdmittanceAreaTypeAuthService.getList(parkId));
  }

  /**
   * 修改
   * @param reqDTO
   * @return Result
   */
  @ApiOperation("修改")
  @SysLog("修改")
  @PostMapping("/edit")
  public Result edit(@RequestBody List<AdmittanceAuthEditReqDTO> reqDTO){
    return success(smtAdmittanceAreaTypeAuthService.editAuth(reqDTO));
  }


	/**
	 * 根据区域id获得权限名
	 * @param id
	 * @return Result
	 */
	@SysLog("根据区域id获得关联权限名")
	@GetMapping("/auth/name")
	@ApiOperation("根据区域id获得关联权限名")
	public Result getAuthName(@RequestParam("id") Integer id, @RequestParam("parkId") Integer parkId){
		return success(smtAdmittanceAreaTypeAuthService.getAuthNameByAreaId(parkId, id.toString()));
	}
}
