package com.tce.smart.platform.controller.securityzone;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityApplyPerson;
import com.tce.smart.platform.service.securityzone.SmtSecurityApplyPersonService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:37
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-保密区门禁申请-申请人员详情")
@RequestMapping("/security/apply/person")
public class SmtSecurityApplyPersonController extends BaseController {

  private final SmtSecurityApplyPersonService smtSecurityApplyPersonService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtSecurityApplyPerson
   * @return
   */
  @GetMapping("/page")
  public Result getSmtSecurityApplyPersonPage(Page page, SmtSecurityApplyPerson smtSecurityApplyPerson) {
    return success(smtSecurityApplyPersonService.page(page, Wrappers.query(smtSecurityApplyPerson)));
  }


  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") String id){
    return success(smtSecurityApplyPersonService.getById(Long.parseLong(id)));
  }

  /**
   * 新增
   * @param smtSecurityApplyPerson
   * @return Result
   */
  @SysLog("新增")
  @PostMapping("/save")
  public Result save(@RequestBody SmtSecurityApplyPerson smtSecurityApplyPerson){
    return success(smtSecurityApplyPersonService.save(smtSecurityApplyPerson));
  }

  /**
   * 修改
   * @param smtSecurityApplyPerson
   * @return Result
   */
  @SysLog("修改")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtSecurityApplyPerson smtSecurityApplyPerson){
    return success(smtSecurityApplyPersonService.updateById(smtSecurityApplyPerson));
  }

  /**
   * 通过id删除
   * @param id id
   * @return Result
   */
  @SysLog("删除")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return success(smtSecurityApplyPersonService.removeById(id));
  }

}
