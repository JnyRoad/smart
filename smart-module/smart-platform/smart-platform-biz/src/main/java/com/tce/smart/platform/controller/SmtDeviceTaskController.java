package com.tce.smart.platform.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Api(tags = "设备任务管理")
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/device/task")
public class SmtDeviceTaskController extends BaseController {

  private final SmtDeviceTaskService smtDeviceTaskService;

  @PostMapping("/update")
  public Result<Boolean> updateStatus(@Valid  @RequestBody DeviceTaskDTO deviceTaskDTO){
	  boolean flag = smtDeviceTaskService.updateStatus(deviceTaskDTO);
	  log.info("卡片状态变更通知,请求参数： data:{},code:{},message:{},处理结果:{}",deviceTaskDTO.getData(),deviceTaskDTO.getCode(),deviceTaskDTO.getMessage(),flag);
	  return new Result<Boolean>(flag);
  }

	/**
	 * 分页查询
	 *
	 * @param page              分页对象
	 * @param taskDownRecordDTO 车辆参数查询
	 * @return return
	 */
	@GetMapping("/person/page")
	public Result getPerson(Page page, TaskDownRecordDTO taskDownRecordDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		taskDownRecordDTO.setParkIds(parkIds);
		return new Result<>(smtDeviceTaskService.getPerson(page, taskDownRecordDTO));
	}

	/**
	 * 分页查询
	 *
	 * @param page              分页对象
	 * @param taskDownRecordDTO 车辆参数查询
	 * @return return
	 */
	@GetMapping("/isc/person/page")
	public Result getPersonForISC(Page page, TaskDownRecordDTO taskDownRecordDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		taskDownRecordDTO.setParkIds(parkIds);
		return new Result<>(smtDeviceTaskService.getPersonForISC(page, taskDownRecordDTO));
	}

//  @PostMapping("/update")
//  public Result<Boolean> updateStatus(@RequestBody BridgeDTO<String> bridgeDTO){
//	  if(StrUtil.isNotBlank(bridgeDTO.getData())){
//		  DeviceTaskDTO deviceTaskDTO = JSONUtil.toBean(bridgeDTO.getData(),DeviceTaskDTO.class);
//		  boolean flag = smtDeviceTaskService.updateStatus(deviceTaskDTO);
//		  log.info("卡片状态变更通知,请求参数： data:{},code:{},message:{},处理结果:{}",deviceTaskDTO.getData(),deviceTaskDTO.getCode(),deviceTaskDTO.getMessage(),flag);
//		  return new Result<Boolean>(flag);
//	  }
//	  return new Result<Boolean>(false);
//  }
	/**
	 * 重发
	 * @return Result
	 */
	@GetMapping("/repeat")
	public void repeat(){
		log.info("卡片重发定时任务开始执行，开始时间：{}", DateUtil.formatDateTime(DateUtil.date()));
		smtDeviceTaskService.repeat();
	}

	@PostMapping("/delete")
	public Result<Boolean> delete(@Valid  @RequestBody DeviceTaskDeleteDTO deviceTaskDeleteDTO){
		return new Result<Boolean>(smtDeviceTaskService.deleteTask(deviceTaskDeleteDTO));
	}

//	@PostMapping("/code")
//	public Result<List<String>> getDeviceCode(@Valid  @RequestBody  DeviceTaskQueryDTO deviceTaskQueryDTO){
//		return new Result<List<String>>(smtDeviceTaskService.getDeviceCode(deviceTaskQueryDTO));
//	}

	@PostMapping("/code")
	public Result<List<String>> getDeviceCode(@Valid  @RequestBody  DeviceTaskQueryDTO deviceTaskQueryDTO){
		return new Result<>(smtDeviceTaskService.getDownDeviceCode(deviceTaskQueryDTO));
	}

	@PostMapping("/save")
	public Result saveTask(@RequestBody  DeviceTaskVO deviceTaskVO){
		smtDeviceTaskService.saveTask(deviceTaskVO);
		return new Result<>(Boolean.TRUE);
	}


	/**
	 * 人脸下发日志--重新发下
	 * @param id 任务id
	 */
	@GetMapping("/updateStatus")
	public Result<Boolean> updateStatusById(Integer taskId,Integer id){
		return new Result<>(smtDeviceTaskService.updateStatusById(taskId,id));
	}

	/**
	 * 删除访客设备通行权限
	 * @param id 记录id
	 */
	@ApiOperation("删除访客设备通行权限")
	@PostMapping("/del/visitor/auth")
	public Result<Boolean> delVisitorDeviceAuth(@ApiParam(name = "id",value = "访客预约记录Id",required = true) @RequestParam String id){
		return new Result<>(smtDeviceTaskService.delVisitorDeviceAuth(Long.parseLong(id)));
	}
}
