package com.tce.smart.platform.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsdepRespDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.SmtVehicle;
import com.tce.smart.platform.core.entity.SmtXcVehicle;
import com.tce.smart.platform.core.model.*;
import com.tce.smart.platform.core.vo.NotStaffVehicleVO;
import com.tce.smart.platform.service.SmtVehicleService;
import com.tce.smart.platform.service.SmtXcVehicleService;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import com.tce.smart.tool.constant.VehicleConstants;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;


/**
 * 车辆信息表
 *
 * @author 王艳勇
 */
@RestController
@AllArgsConstructor
@RequestMapping("/vehicle")
public class SmtVehicleController extends BaseController{

  private final SmtVehicleService smtVehicleService;

  private final SmtXcVehicleService smtXcVehicleService;

  private final SmtDormitoryPersonService smtDormitoryPersonService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtVehicle 车辆信息表
   * @return Result
   */
  @GetMapping("/page")
  public Result getSmtVehiclePage(Page page, VehicleDTO smtVehicle) {
	IPage pageList = smtVehicleService.getVehicle(page,smtVehicle);
    return success(pageList, VehicleList.class);
  }

	/**
	 * 许昌车辆分页查询
	 * @param page 分页对象
	 * @param xcVehicleDTO 车辆信息表
	 * @return Result
	 */
	@GetMapping("/xc-page")
	public Result getXCSmtVehiclePage(Page page, XcVehicleDTO xcVehicleDTO) {
		IPage pageList = smtXcVehicleService.getXcVehicle(page,xcVehicleDTO);
		return success(pageList, XcVehicleList.class);
	}

  /**
   * 通过id查询车辆信息表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Long id){
	  VehicleDetail vehicleVO = smtVehicleService.getVehicleDetail(id);
      return success(vehicleVO);
  }

	/**
	 * 通过id查询许昌园区车辆信息表
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/xc/{id}")
	public Result getXcById(@PathVariable("id") Long id){
		SmtXcVehicle vehicleVO = smtXcVehicleService.getById(id);
		return success(vehicleVO,XcVehicleList.class);
	}

  /**
   * 新增车辆信息表
   * @param saveVehicleDTO 车辆信息表
   * @return Result
   */
  @SysLog("新增车辆信息表")
  @PostMapping("/save")
  public Result save(@Valid @RequestBody SaveVehicleDTO saveVehicleDTO){
    return smtVehicleService.saveSmtVehicle(saveVehicleDTO);
  }

	/**
	 * 新增许昌车辆信息表
	 * @param saveVehicleDTO 车辆信息表
	 * @return Result
	 */
	@SysLog("新增许昌车辆信息表")
	@PostMapping("/xc-save")
	public Result<Boolean> xcSave(@Valid @RequestBody SaveXCVehicleDTO saveVehicleDTO){
		return success(smtXcVehicleService.saveXCSmtVehicle(saveVehicleDTO));
	}

  /**
   * 判断车辆是否已经添加
   * @param saveVehicleDTO 车辆信息表
   * @return Result
   */
  @SysLog("判断车辆是否已经添加")
  @PostMapping("/plate")
  public Result plate(@RequestBody SaveVehicleDTO saveVehicleDTO){
	  List<SmtVehicle> smtVehicleList = smtVehicleService.list(Wrappers.<SmtVehicle>query().lambda()
				.eq(SmtVehicle::getVehiclePlate, StrUtil.removeAll(saveVehicleDTO.getVehiclePlate(), " ").toUpperCase())
				.eq(SmtVehicle::getIsDelete, VehicleConstants.UNDELETED)
			  .eq(Objects.nonNull(saveVehicleDTO.getParkId()), SmtVehicle::getParkId, saveVehicleDTO.getParkId())
	  );
	  return new Result<>(CollectionUtil.isNotEmpty(smtVehicleList));
  }

  /**
   * 修改车辆信息表
   * @param smtVehicle 车辆信息表
   * @return Result
   */
  @SysLog("修改车辆信息表")
  @PostMapping("/update")
  public Result updateById(@RequestBody VehicleDTO smtVehicle){
    return smtVehicleService.updateVehicle(smtVehicle);
  }

	/**
	 * 许昌修改车辆信息表
	 * @param updateXCVehicleDTO 车辆信息表
	 * @return Result
	 */
	@SysLog("修改车辆信息表")
	@PostMapping("/xc-update")
	public Result xcUpdateById(@RequestBody UpdateXCVehicleDTO updateXCVehicleDTO){
		return success(smtXcVehicleService.xcUpdateById(updateXCVehicleDTO));
	}

  /**
   * 通过id删除车辆信息表
   * @param id id
   * @return Result
   */
  @SysLog("删除车辆信息表")
  @GetMapping("/delete/{id}")
  public Result removeById(@PathVariable("id") Long id){
	  List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
	  return new Result <>(smtVehicleService.deleteVehicle(id, parkIds));
  }

	/**
	 * 删除许昌车辆
	 * @param id
	 * @return
	 */
	@GetMapping("/xc-delete/{id}")
	public Result<Boolean> deleteXcVehicle(@PathVariable("id") Long id){
		return new Result <>(smtXcVehicleService.deleteVehicle(id));
	}

  /**
   * 获取BU信息
   * @return Result
   */
  @GetMapping("/comp")
  public Result getComp(){
	List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
    List<OvwYscompRespDTO> list = smtVehicleService.getComp(parkIds);
    return success(list);
  }
  /**
   * 通过BUID查询部门信息
   * @param id id
   * @return Result
   */
  @GetMapping("/dep/{id}")
  public Result getDepById(@PathVariable("id") Integer id){
    List<OvwYsdepRespDTO> list = smtVehicleService.getDep(id);
    return success(list);
  }

  /**
   * 通过部门ID查询员工信息
   * @param id id
   * @return Result
   */
  @GetMapping("/staff/{id}")
  public Result getStaff(@PathVariable("id") Integer id){
    List<VehicleStaff> list = smtVehicleService.getStaff(id);
    return success(list);
  }

	/**
	 * 获取员工信息
	 * @param staffId 工号
	 * @return
	 */
  @GetMapping("/staff/detail/{badge}")
  public Result<VehicleStaff> getStaffDetail(@PathVariable("badge") String staffId) {
	  List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
	  VehicleStaff staff = smtVehicleService.getStaffDetail(staffId,parkIds);
	  return success(staff);
  }

  /**
   * bu 部门级联
   * @return Result
   */
  @GetMapping("/comp/tree")
  public Result getCompTree(){
	  List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
	  List<DepTree> list = smtVehicleService.getCompTree(parkIds);
	  return success(list);
  }

	/**
	 * bu 部门级联
	 * @return Result
	 */
	@GetMapping("/dormitory/comp/tree")
	public Result getDorCompTree(){
		List<Integer> parkIds = smtDormitoryPersonService.getParkId(SecurityUtils.getUser().getUsername());
		if(CollUtil.isEmpty(parkIds)) {
			parkIds = SecurityUtils.getUser().getParkIdList();
		}
		List<DepTree> list = smtVehicleService.getCompTree(parkIds);
		return success(list);
	}

  /**
   * 车辆统计信息
   * @return Result
   */
  @GetMapping("/count")
  public Result getVehicleCountInfo() {
	  return new Result <>(smtVehicleService.getVehicleCountInfo());
  }


  /**
   * 非车辆分页查询
   * @param page 分页对象
   * @param smtVehicle 车辆信息表
   * @return Result
   */
  @GetMapping("/not/staff/page")
  public Result getNotStaffVehiclePage(Page page, VehicleDTO smtVehicle) {
	  List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
	  smtVehicle.setParkIds(parkIds);
	  IPage pageList = smtVehicleService.getNotStaffVehiclePage(page,smtVehicle);
	  return success(pageList, NotStaffVehicle.class);
  }

  /**
   * 通过id查询非车辆信息表
   * @param id id
   * @return Result
   */
  @GetMapping("/not/staff/{id}")
  public Result getNotStaffById(@PathVariable("id") Long id){
	NotStaffVehicleVO vehicle = this.smtVehicleService.getNotStaffVehicle(id);
	if(ObjectUtil.isNotNull(vehicle)){
		return success(vehicle,NotStaffVehicle.class);
	}
	return fail("数据异常");
  }

  /**
   * 新增非员工车辆信息表
   * @param notStaffVehicleDTO 车辆信息表
   * @return Result
   */
  @SysLog("新增车辆信息表")
  @PostMapping("/not/staff/save")
  public Result saveNotStaffVehicle(@Valid @RequestBody NotStaffVehicleDTO notStaffVehicleDTO){
    return smtVehicleService.saveNotStaffVehicle(notStaffVehicleDTO);
  }

  /**
   * 修改非车辆信息表
   * @param notStaffVehicleDTO 车辆信息表
   * @return Result
   */
  @SysLog("修改车辆信息表")
  @PostMapping("/not/staff/update")
  public Result updateNotStaffVehicle(@RequestBody NotStaffVehicleDTO notStaffVehicleDTO){
    return new Result <>(smtVehicleService.updateNotStaffVehicle(notStaffVehicleDTO));
  }

  /**
   * 通过id删除车辆信息表
   * @param id id
   * @return Result
   */
  @SysLog("删除车辆信息表")
  @GetMapping("/not/staff/delete/{id}")
  public Result deleteNotStaffVehicle(@PathVariable Long id){
	  return new Result <>(smtVehicleService.deleteNotStaffVehicle(id));
  }

	/**
	 * 获取福利信息
	 * @return Result
	 */
  @SysLog("获取福利信息")
  @GetMapping("/welfare/level")
  public List<String> getWelfareLevel() {
	return smtVehicleService.getWelfareLevel();
  }
}
