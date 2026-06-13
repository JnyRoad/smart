package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.ApplicationEmergencyDTO;
import com.tce.smart.platform.core.entity.SmtApplicationEmergency;
import com.tce.smart.platform.service.SmtApplicationEmergencyService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 应聘者紧急联系人
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:30
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/application/emergency")
public class SmtApplicationEmergencyController extends BaseController {

  private final  SmtApplicationEmergencyService smtApplicationEmergencyService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtapplicationEmergency 应聘者紧急联系人
   * @return
   */
  @GetMapping("/page")
  public Result getSmtapplicationEmergencyPage(Page page, SmtApplicationEmergency smtapplicationEmergency) {
    return  new Result<>(smtApplicationEmergencyService.page(page,Wrappers.query(smtapplicationEmergency)));
  }


  /**
   * 通过id查询应聘者紧急联系人
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(smtApplicationEmergencyService.getById(id));
  }

  /**
   * 新增应聘者紧急联系人
   * @param smtapplicationEmergency 应聘者紧急联系人
   * @return Result
   */
  @SysLog("新增应聘者紧急联系人")
  @PostMapping("addApplicationEmergency")
  public Result<Boolean> addApplicationEmergency(@RequestBody SmtApplicationEmergency smtapplicationEmergency){
    return success(smtApplicationEmergencyService.saveApplicationEmergency(smtapplicationEmergency));
  }

	 /**
	* 修改应聘者紧急联系人
	* @param emergencyDTO 应聘者紧急联系人
	* @return Result
	*/
	@SysLog("修改应聘者紧急联系人")
	@PostMapping("updateByApplication")
	public Result<Integer> updateByBadge(@RequestBody ApplicationEmergencyDTO emergencyDTO){
	 return success(smtApplicationEmergencyService.updateByIdApplicationEmergency(emergencyDTO));
	}

  /**
   * 通过id删除应聘者紧急联系人
   * @param id id
   * @return Result
   */
  @SysLog("删除应聘者紧急联系人")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(smtApplicationEmergencyService.removeById(id));
  }

  /**
   * 通过id查询应聘者紧急联系人
   * @param applicationId
   * @return Result
   */
  @GetMapping("/getByApplicationId/{applicationId}")
  public Result getByEmployeeId(@PathVariable String applicationId){
    Result result = Result.builder().build();
    try {
      result =  smtApplicationEmergencyService.getByApplicationId(applicationId);
    } catch (Exception e) {
      result.setMsg("通过应聘者id查询紧急联系人出错");
      log.warn("通过应聘者id查询紧急联系人出错",e);
    }
    return result;
  }






}
