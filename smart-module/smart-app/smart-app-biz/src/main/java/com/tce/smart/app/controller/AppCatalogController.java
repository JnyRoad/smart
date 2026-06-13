package com.tce.smart.app.controller;

import com.tce.smart.common.core.wrapper.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.app.entity.AppCatalog;
import com.tce.smart.app.service.AppCatalogService;

import lombok.AllArgsConstructor;


/**
 * 主题分类
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:12
 */
@RestController
@AllArgsConstructor
@RequestMapping("/appcatalog")
public class AppCatalogController extends BaseController {

  private final  AppCatalogService appCatalogService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param appCatalog 主题分类
   * @return
   */
  @GetMapping("/page")
  public Result getAppCatalogPage(Page page, AppCatalog appCatalog) {
    return  new Result<>(appCatalogService.page(page,Wrappers.query(appCatalog)));
  }


  /**
   * 通过id查询主题分类
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(appCatalogService.getById(id));
  }

  /**
   * 新增主题分类
   * @param appCatalog 主题分类
   * @return Result
   */
  @SysLog("新增主题分类")
  @PostMapping("/save")
  public Result save(@RequestBody AppCatalog appCatalog){
    return new Result<>(appCatalogService.save(appCatalog));
  }

  /**
   * 修改主题分类
   * @param appCatalog 主题分类
   * @return Result
   */
  @SysLog("修改主题分类")
  @PostMapping("/update")
  public Result updateById(@RequestBody AppCatalog appCatalog){
    return new Result<>(appCatalogService.updateById(appCatalog));
  }

  /**
   * 通过id删除主题分类
   * @param id id
   * @return Result
   */
  @SysLog("删除主题分类")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(appCatalogService.removeById(id));
  }

}
