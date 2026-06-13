package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.service.SmtParkBuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.tce.smart.common.core.model.Result.success;

import java.util.List;


/**
 * 园区BU关系控制器
 *
 * @author mckaywu
 * @date 2019-11-20 10:35:16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/parkbu")
@Api(tags = "园区BU关系")
public class SmtParkBuController {

  private final  SmtParkBuService smtParkBuService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtParkBu 园区BU关系表
   * @return
   */
  @GetMapping("/page")
  @ApiOperation("获取园区BU关系列表")
  public Result<Page<SmtParkBu>> getSmtParkBuPage(Page page, SmtParkBu smtParkBu) {
    return success(smtParkBuService.page(page,Wrappers.query(smtParkBu)));
  }

	/**
	 * 分页查询
	 * @param parkId 园区id
	 * @return
	 */
	@GetMapping("/getByPark/{parkId}")
	@ApiOperation("根据园区id获取bu")
	public Result<Page<SmtParkBu>> getBuByPark(@PathVariable("parkId") Integer parkId) {
		return success(smtParkBuService.getAllByParkId(parkId));
	}


  /**
   * 通过id查询园区BU关系
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  @ApiOperation("通过id查询园区BU关系")
  public Result<SmtParkBu> getById(@PathVariable("id") Integer id){
    return success(smtParkBuService.getById(id));
  }

  /**
   * 新增园区BU关系
   * @return Result
   */
  @SysLog("新增园区BU关系")
  @PostMapping("/save")
  @ApiOperation("新增园区BU关系")
  public Result<Boolean> save(@RequestBody SmtParkBu smtParkBu){
    return success(smtParkBuService.save(smtParkBu));
  }

  /**
   * 修改园区BU关系表
   * @param smtParkBu 园区BU关系表
   * @return Result
   */
  @SysLog("修改园区BU关系")
  @PostMapping("/update")
  @ApiOperation("修改园区BU关系")
  public Result<Boolean> updateById(@RequestBody SmtParkBu smtParkBu){
    return success(smtParkBuService.updateById(smtParkBu));
  }

  /**
   * 通过id删除园区BU关系
   * @param id id
   * @return Result
   */
  @SysLog("删除园区BU关系")
  @PostMapping("/{id}")
  @ApiOperation("删除园区BU关系")
  public Result<Boolean>  removeById(@PathVariable Integer id){
    return success(smtParkBuService.removeById(id));
  }


  @SysLog("根据bu查询园区")
  @GetMapping("getParkList/{compId}")
  @ApiOperation("根据bu查询园区")
  public Result<List<SmtPark>>  getParkListByBu(@PathVariable Long compId){
    return success(smtParkBuService.getParkListByBu(compId));
  }

}
