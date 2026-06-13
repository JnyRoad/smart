package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.entity.SmtParking;
import com.tce.smart.platform.core.model.ParkingVO;
import com.tce.smart.platform.service.SmtParkingService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 停车场表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@RestController
@AllArgsConstructor
@RequestMapping("/parking")
public class SmtParkingController extends BaseController {

  private final  SmtParkingService smtParkingService;
//  private final  SmtParkService smtParkService;


  /**
   * 通过id查询停车场表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") String id){
    return new Result <>(smtParkingService.getById(id));
  }

  /**
   * 新增停车场表
   * @param entity 停车场表
   * @return Result
   */
  @SysLog("新增停车场信息表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtParking entity){
    return new Result <>(smtParkingService.saveParking(entity));
  }

  /**
   * 修改停车场表
   * @param entity 设备信息表
   * @return Result
   */
  @SysLog("修改停车场表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtParking entity){
    return new Result <>(smtParkingService.updateParking(entity));
  }

  /**
   * 通过id删除停车场表
   * @param id id
   * @return Result
   */
  @SysLog("删除停车场表")
  @GetMapping("/delete/{id}")
  public Result removeById(@PathVariable String id){
    return new Result <>(smtParkingService.removeParking(id));
  }

  /**
   * 查询停车场表
   * @return Result
   */
  @GetMapping("/page")
  public Result page(Page page, @RequestParam(value = "parkId", required = false) Integer parkId){
	  List<Integer> parkIds = new ArrayList<>();
	if(Objects.nonNull(parkId)) {
		parkIds.add(parkId);
	}else {
		parkIds = SecurityUtils.getUser().getParkIdList();
	}
	IPage parkingList = smtParkingService.page(page,parkIds);
	return success(parkingList, ParkingVO.class);
  }


	/**
	 * 查询停车场表
	 * @return Result
	 */
	@GetMapping("/getParking")
	public Result getParking(Page page){
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		List<SmtParking> parkingList = smtParkingService.getParking(parkIds);
		return success(parkingList, ParkingVO.class);
	}

//  /**
//   * 获取园区列表
//   * @return Result
//   */
//  @GetMapping("/getPark")
//  public Result getPark(){
//	return new Result <>(smtParkService.getParkList());
//  }
}
