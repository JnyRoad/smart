package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.commonconfig.CommonConfigEditReqDTO;
import com.tce.smart.platform.api.dto.req.commonconfig.CommonConfigQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.CommonConfigRespDTO;
import com.tce.smart.platform.core.service.SmtCommonConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预约配置表
 *
 * @author fushiping
 * @date 2021-08-13 16:08:16
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-公共配置")
@RequestMapping("/common/config")
public class SmtCommonConfigController extends BaseController {

  private final SmtCommonConfigService commonConfigService;

  /**
   * 查询
   * @param queryDTO 预约配置表
   * @return
   */
  @ApiOperation("根据类型获得配置")
  @PostMapping("/getByType")
  public Result getByType(@RequestBody(required = false) CommonConfigQueryReqDTO queryDTO) {
    return success(commonConfigService.getList(queryDTO), CommonConfigRespDTO.class);
  }

  /**
   * 通过id查询预约配置表
   * @param id id
   * @return Result
   */
  @ApiOperation("通过id查询预约配置表")
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") String id){
    return success(commonConfigService.getById(Long.parseLong(id)), CommonConfigRespDTO.class);
  }

  /**
   * 编辑配置项
   * @param editReqDTO 预约配置表
   * @return Result
   */
  @SysLog("编辑配置项")
  @ApiOperation("编辑配置项")
  @PostMapping("/edit/config")
  public Result editConfig(@RequestBody CommonConfigEditReqDTO editReqDTO){
    return success(commonConfigService.editConfig(editReqDTO));
  }

	/**
	 * 批量编辑配置项
	 * @param editReqDTO 预约配置表
	 * @return Result
	 */
	@SysLog("批量编辑配置项")
	@ApiOperation("批量编辑配置项")
	@PostMapping("/batch/edit/config")
	public Result batchEditConfig(@RequestBody List<CommonConfigEditReqDTO> editReqDTO){
		return success(commonConfigService.batchEditConfig(editReqDTO));
	}

	/**
	 * 获得访客预约温馨提示
	 * @param parkId parkId
	 * @return Result
	 */
	@ApiOperation("获得访客预约温馨提示")
	@GetMapping("/visitor/notice")
	public Result getVisitorNotice(@RequestParam("parkId") Integer parkId) {
		return success(commonConfigService.getVisitorNotice(parkId));
	}

	/**
	 * 获得入厂申请预约温馨提示
	 * @param parkId parkId
	 * @return Result
	 */
	@ApiOperation("获得入厂申请预约温馨提示")
	@GetMapping("/admittance/notice")
	public Result getAdmittanceNotice(@RequestParam("parkId") Integer parkId) {
		return success(commonConfigService.getAdmittanceNotice(parkId));
	}

	/**
	 * 是否开启健康码
	 * @param parkId parkId
	 * @return Result
	 */
	@ApiOperation("是否开启健康码")
	@GetMapping("/visitor/health")
	public Result getVisitorHealth(@RequestParam("parkId") Integer parkId) {
		return success(commonConfigService.getVisitorHealth(parkId));
	}

	/**
	 * 离职结算是否计算最后一天
	 * @param parkId parkId
	 * @return Result
	 */
	@ApiOperation("离职结算是否计算最后一天")
	@GetMapping("/leave/settlement")
	public Result getLeaveSettlementApprove(@RequestParam("parkId") Integer parkId) {
		return success(commonConfigService.getLeaveSettlementApprove(parkId));
	}

	/**
	 * 离职结算日志保留天数
	 * @return Result
	 */
	@ApiOperation("离职结算日志保留天数")
	@GetMapping("/leave/settlement/log")
	public Result getSettlementDeleteDay() {
		return success(commonConfigService.getSettlementDeleteDay());
	}

}
