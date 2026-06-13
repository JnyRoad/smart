package com.tce.smart.platform.controller.manage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.manage.RechargePageReqDTO;
import com.tce.smart.platform.service.manage.SmtStaffRechargeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-17 16:51:38
 */
@RestController
@AllArgsConstructor
@RequestMapping("/recharge")
@Api(tags = "员工充值名单")
public class SmtStaffRechargeController extends BaseController {

  private final SmtStaffRechargeService smtStaffRechargeService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param reqDTO
   * @return
   */
  @PostMapping("/page")
  @ApiOperation("获取分页数据")
  public Result getPage(Page page, @RequestBody(required = false) RechargePageReqDTO reqDTO) {
    return success(smtStaffRechargeService.getPage(page,reqDTO));
  }

  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @GetMapping("/detail/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return success(smtStaffRechargeService.getById(id));
  }

  /**
	 * 新员工充值名单定时任务
	 * @param
	 * @return Result
	 */
	@SysLog("新员工充值名单定时任务")
	@GetMapping("/new/recharge")
	public Result insertNewStaffRecharge(){
		return success(smtStaffRechargeService.syncNewStaff());
	}

	/**
	 * 请求在职员工充值名单
	 * @param
	 * @return Result
	 */
	@SysLog("请求在职员工充值名单")
	@GetMapping("/senior/recharge")
	public Result insertSeniorStaffRecharge(){
		return success(smtStaffRechargeService.syncSeniorRecharge());
	}

	/**
	 * excel文件名流水号
	 * @return Result
	 */
	@SysLog("excel数据下载")
	@GetMapping("/excel/title")
	public Result excelTitle(){
		return success(smtStaffRechargeService.genSerialNumber());
	}

  /**
   * 同步名单到c6
   * @param
   * @return Result
   */
  @SysLog("同步名单到c6")
  @PostMapping("/toC6")
  public Result<String> toC6(@RequestBody(required = false) RechargePageReqDTO reqDTO){
    return success(smtStaffRechargeService.syncToC6(reqDTO));
  }

	/**
	 * 特殊名单充值
	 * @param badges id
	 * @return Result
	 */
	@SysLog("特殊名单充值")
	@GetMapping("/single/recharge")
	public Result getById(@RequestParam("badges") String badges, @RequestParam(value = "remark", required = false) String remark){
		return success(smtStaffRechargeService.saveSingleRecharge(badges, remark));
	}

	/**
	 * 删除充值名单
	 * @param reqDTO
	 * @return
	 */
	@PostMapping("/delete/recharge")
	@ApiOperation("删除充值名单")
	public Result deleteInfo(@RequestBody(required = false) RechargePageReqDTO reqDTO) {
		return success(smtStaffRechargeService.deleteInfo(reqDTO));
	}

	/**
	 * 修改餐补
	 * @param blank blank
	 * @return Result
	 */
	@SysLog("修改餐补")
	@GetMapping("/update/recharge")
	public Result getById(@RequestParam("account") BigDecimal account, @RequestParam("blank") String blank,
						  @RequestParam("id") String id){
		return success(smtStaffRechargeService.updateRecharge(account, blank, id));
	}
}
