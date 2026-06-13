package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.StaffEmergencyDTO;
import com.tce.smart.platform.core.entity.SmtStaffEmergency;
import com.tce.smart.platform.service.SmtStaffEmergencyService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 员工紧急联系人
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:30
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/staff/emergency")
public class SmtStaffEmergencyController {

  private final  SmtStaffEmergencyService smtStaffEmergencyService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtStaffEmergency 员工紧急联系人
   * @return
   */
  @GetMapping("/page")
  public Result getSmtStaffEmergencyPage(Page page, SmtStaffEmergency smtStaffEmergency) {
    return  new Result<>(smtStaffEmergencyService.page(page,Wrappers.query(smtStaffEmergency)));
  }


  /**
   * 通过id查询员工紧急联系人
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(smtStaffEmergencyService.getById(id));
  }

  /**
   * 新增员工紧急联系人
   * @param smtStaffEmergency 员工紧急联系人
   * @return Result
   */
  @SysLog("新增员工紧急联系人")
  @PostMapping("addStaffEmergency")
  public Result save(@RequestBody SmtStaffEmergency smtStaffEmergency){
    return new Result<>(smtStaffEmergencyService.saveStaffEmergency(smtStaffEmergency));
  }

	 /**
	* 修改员工紧急联系人
	* @param emergencyDTO 员工紧急联系人
	* @return Result
	*/
	@SysLog("修改员工紧急联系人")
	@PostMapping("updateByBadge")
	public Result updateByBadge(@RequestBody StaffEmergencyDTO emergencyDTO){
	 return smtStaffEmergencyService.updateByIdStaffEmergency(emergencyDTO);
	}

  /**
   * 通过id删除员工紧急联系人
   * @param id id
   * @return Result
   */
  @SysLog("删除员工紧急联系人")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(smtStaffEmergencyService.removeById(id));
  }

  /**
   * 通过id查询员工紧急联系人
   * @param staffId
   * @return Result
   */
  @GetMapping("/getByStaffId/{staffId}")
  public Result getByEmployeeId(@PathVariable String staffId){
    Result result = Result.builder().build();
    try {
      result =  smtStaffEmergencyService.getByStaffId(staffId);
    } catch (Exception e) {
      result.setMsg("通过员工id查询紧急联系人出错");
      log.warn("通过员工id查询紧急联系人出错",e);
    }
    return result;
  }

}
