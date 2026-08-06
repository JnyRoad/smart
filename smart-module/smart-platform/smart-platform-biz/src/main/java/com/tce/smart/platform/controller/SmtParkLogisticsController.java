package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.SmtParkLogisticsDTO;
import com.tce.smart.platform.core.entity.SmtParkLogistics;
import com.tce.smart.platform.service.SmtParkLogisticsService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 园区物流关系表
 *
 * @author mckaywu
 * @date 2019-11-20 10:36:33
 */
@RestController
@AllArgsConstructor
@RequestMapping("/parklogistics")
public class SmtParkLogisticsController extends BaseController {

  private final SmtParkLogisticsService smtParkLogisticsService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtParkLogistics 园区物流关系表
   * @return
   */
  @GetMapping("/page")
  public Result getsmtParkLogisticsPage(Page page, SmtParkLogistics smtParkLogistics) {
    return success(smtParkLogisticsService.page(page,Wrappers.query(smtParkLogistics)));
  }


  /**
   * 通过id查询园区物流关系表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return success(smtParkLogisticsService.getById(id));
  }

  /**
   * 新增园区物流关系表
   * @param smtParkLogistics 园区物流关系表
   * @return Result
   */
  @SysLog("新增园区物流关系表")
  @PostMapping("/save")
  public Result<Boolean> save(@RequestBody SmtParkLogistics smtParkLogistics){
    return success(smtParkLogisticsService.save(smtParkLogistics));
  }

  /**
   * 修改园区物流关系表
   * @param smtParkLogistics 园区物流关系表
   * @return Result
   */
  @SysLog("修改园区物流关系表")
  @PostMapping("/update")
  public Result<Boolean> updateById(@RequestBody SmtParkLogistics smtParkLogistics){
    return success(smtParkLogisticsService.updateById(smtParkLogistics));
  }

  /**
   * 通过id删除园区物流关系表
   * @param id id
   * @return Result
   */
  @SysLog("删除园区物流关系表")
  @PostMapping("/{id}")
  public Result<Boolean> removeById(@PathVariable Integer id){
    return success(smtParkLogisticsService.removeById(id));
  }

	/**
	 * 查询园区物流关系表
	 * @return Result
	 */
	@Inner
	@GetMapping("/list")
	public Result<List<SmtParkLogisticsDTO>> list(){
		return success(smtParkLogisticsService.list(), SmtParkLogisticsDTO.class);
	}

	@Inner
	@GetMapping("/companyId/{companyId}")
	public Result<SmtParkLogisticsDTO> getByCompanyId(@PathVariable("companyId") String companyId){
		return success(smtParkLogisticsService.getOne(Wrappers.<SmtParkLogistics>query().lambda().eq(SmtParkLogistics::getCompanyId, companyId)), SmtParkLogisticsDTO.class);
	}
}
