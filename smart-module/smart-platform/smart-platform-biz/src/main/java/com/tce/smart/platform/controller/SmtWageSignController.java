package com.tce.smart.platform.controller;

import javax.validation.Valid;

import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.platform.api.dto.req.manage.QueryAttendanceSignReqDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.WageSignDTO;
import com.tce.smart.platform.core.entity.SmtWageSign;
import com.tce.smart.platform.core.model.WageSignDetail;
import com.tce.smart.platform.service.SmtWageSignService;
import com.tce.smart.tool.constant.WageSignConstants;

import lombok.AllArgsConstructor;

import java.util.List;


/**
 * 工资签单
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@RestController
@AllArgsConstructor
@RequestMapping("/wage/sign")
public class SmtWageSignController extends BaseController{

  private final  SmtWageSignService smtWageSignService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param wageSignDTO 工资签单
   * @return
   */
  @GetMapping("/page")
  public Result getSmtWageSignPage(Page page, WageSignDTO wageSignDTO) {
    return  new Result <>(smtWageSignService.getPage(page, wageSignDTO));
  }

  /**
   * 裕同查询
   * @param smtWageSign 工资签单
   * @return
   */
  @PostMapping("/get")
  public Result getWageSign(@Valid @RequestBody SmtWageSign smtWageSign) {
    return  new Result <>(smtWageSignService.getWageSign(smtWageSign) ? WageSignConstants.AUDITED : WageSignConstants.NOT_AUDITED);
  }


  /**
   * 通过id查询工资签单
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
	SmtWageSign smtWageSign = smtWageSignService.getById(id);
    return success(smtWageSign, WageSignDetail.class);
  }

  /**
   * 新增工资签单记录
   * @param smtWageSign 工资签单
   * @return Result
   */
  @SysLog("新工资签单记录")
  @PostMapping("/save")
  public Result updateToSign(@RequestBody SmtWageSign smtWageSign){
    return new Result <>(smtWageSignService.updateToSign(smtWageSign));
  }

  /**
   * 修改工资签单信息记录
   * @param smtWageSign 工资签单记录
   * @return Result
   */
  @SysLog("修改工资签单记录")
  @PostMapping("/app/update")
  public Result updateAppById(@RequestBody SmtWageSign smtWageSign){
    return new Result <>(smtWageSignService.updateById(smtWageSign));
  }

	@SysLog("每月同步员工定时任务")
	@Inner
	@OpenApi("server")
	@GetMapping("/sync/task")
	public Result syncStaff(){
		return new Result <>(smtWageSignService.syncStaff());
	}


	@SysLog("短信提醒发送")
	@PostMapping("/msg")
	public Result msg(@RequestBody(required = false) WageSignDTO wageSignDTO){
		return new Result <>(smtWageSignService.sendMessage(wageSignDTO));
	}

	@SysLog("短信提醒条数")
	@ApiOperation("短信提醒条数")
	@PostMapping("/msg/count")
	public Result msgCount(@RequestBody(required = false) WageSignDTO reqDTO) {
		return success(smtWageSignService.countMessage(reqDTO));
	}

}
