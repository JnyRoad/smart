package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtDormitoryLevel;
import com.tce.smart.platform.service.SmtDormitoryLevelService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 宿舍职层关联表
 *
 * @author 齐佩
 * @date 2019-04-18 14:47:57
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/level")
public class SmtDormitoryLevelController {

  private final  SmtDormitoryLevelService smtDormitoryLevelService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtDormitoryLevel 宿舍职层关联表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtDormitoryLevelPage(Page page, SmtDormitoryLevel smtDormitoryLevel) {
    return  new Result<>(smtDormitoryLevelService.page(page,Wrappers.query(smtDormitoryLevel)));
  }


  /**
   * 通过id查询宿舍职层关联表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(smtDormitoryLevelService.getById(id));
  }

  /**
   * 新增宿舍职层关联表
   * @param smtDormitoryLevel 宿舍职层关联表
   * @return Result
   */
  @SysLog("新增宿舍职层关联表 ")
  @PostMapping("addDormitoryLevel")
  public Result save(@RequestBody SmtDormitoryLevel smtDormitoryLevel){
    return new Result<>(smtDormitoryLevelService.save(smtDormitoryLevel));
  }

  /**
   * 修改宿舍职层关联表
   * @param smtDormitoryLevel 宿舍职层关联表
   * @return Result
   */
  @SysLog("修改宿舍职层关联表 ")
  @PostMapping("updateDormitoryLevel")
  public Result updateById(@RequestBody SmtDormitoryLevel smtDormitoryLevel){
    return new Result<>(smtDormitoryLevelService.updateById(smtDormitoryLevel));
  }

  /**
   * 通过id删除宿舍职层关联表
   * @param id id
   * @return Result
   */
  @SysLog("删除宿舍职层关联表 ")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(smtDormitoryLevelService.removeById(id));
  }

}
