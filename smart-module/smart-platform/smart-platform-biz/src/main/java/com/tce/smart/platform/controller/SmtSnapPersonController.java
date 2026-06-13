package com.tce.smart.platform.controller;

import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.IscTemperatureDTO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.SaveSnapPersonDTO;
import com.tce.smart.platform.core.dto.SearchSnapPersonAccessDTO;
import com.tce.smart.platform.core.entity.SmtSnapPerson;
import com.tce.smart.platform.service.SmtSnapPersonService;

import lombok.AllArgsConstructor;

import java.util.List;


/**
 * 人员抓拍记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:30
 */
@RestController
@AllArgsConstructor
@RequestMapping("/snap/person")
public class SmtSnapPersonController {

  private final  SmtSnapPersonService smtSnapPersonService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtSnapPerson 人员抓拍记录表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtSnapPersonPage(Page page, SearchSnapPersonAccessDTO searchSnapPersonAccessDto,@RequestParam(value = "snapTime",required=false) String snapTime) {
	  return  new Result<>(smtSnapPersonService.getSmtSnapPersonPage(page,searchSnapPersonAccessDto,snapTime));
  }


  /**
   * 通过id查询人员抓拍记录表
   * @param id id
   * @return Result
   */
  @GetMapping("/searchSnapPersonDetail/{id}")
  public Result getSnapPersonById(@PathVariable("id") Integer id){
    return new Result<>(smtSnapPersonService.getSnapPersonById(id));
  }


  /**
   * 新增人员抓拍记录表
   * @param smtSnapPerson 人员抓拍记录表
   * @return Result
   */
  @SysLog("添加人员抓拍记录表")
  @PostMapping("/addSnapPerson")
  public Result<Boolean> addSnapPerson(@RequestBody SaveSnapPersonDTO saveSnapPersonDTO){
    return smtSnapPersonService.addSnapPerson(saveSnapPersonDTO);
  }

  /**
   * 新增人员抓拍记录表
   * @param smtSnapPerson 人员抓拍记录表
   * @return Result
   */
  @SysLog("新增人员抓拍记录表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtSnapPerson smtSnapPerson){
    return new Result<>(smtSnapPersonService.save(smtSnapPerson));
  }

  /**
   * 修改人员抓拍记录表
   * @param smtSnapPerson 人员抓拍记录表
   * @return Result
   */
  @SysLog("修改人员抓拍记录表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtSnapPerson smtSnapPerson){
    return new Result<>(smtSnapPersonService.updateById(smtSnapPerson));
  }

  /**
   * 通过id删除人员抓拍记录表
   * @param id id
   * @return Result
   */
  @SysLog("删除人员抓拍记录表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(smtSnapPersonService.removeById(id));
  }

	/**
	 * 合肥温度检查
	 * @param dto
	 */
	@PostMapping("/check/temp")
	@Inner
	public Result<Boolean> checkTemp(@RequestBody List<IscTemperatureDTO> dto){
		return new Result<>(smtSnapPersonService.checkTemperature(dto));
	}

}
