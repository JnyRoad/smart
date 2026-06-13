package com.tce.smart.platform.controller;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.DeviceAuthVehicleReqDTO;
import com.tce.smart.platform.api.dto.resp.DeviceAuthVehicleRespDTO;
import com.tce.smart.platform.core.dto.DeviceDataDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtDeviceVehicle;
import com.tce.smart.platform.core.service.SmtDeviceVehicleService;
import com.tce.smart.platform.core.vo.DeviceTaskVehicleVO;
import com.tce.smart.platform.core.vo.DeviceVehicleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * 设备车辆关联
 *
 * @author 王艳勇
 * @date 2019-04-16 16:06:14
 */
@RestController
@AllArgsConstructor
@RequestMapping("/device/vehicle")
@Api(value = "device_vehicle", tags = "设备车辆授权")
public class SmtDeviceVehicleController extends BaseController {

	private final SmtDeviceVehicleService smtDeviceVehicleService;


	/**
	 * 分页查询
	 *
	 * @param page          分页对象
	 * @param deviceDataDTO 查询条件
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtDeviceVehiclePage(Page page, DeviceDataDTO deviceDataDTO) {
		return success(smtDeviceVehicleService.getDeviceVehicle(page, deviceDataDTO), DeviceVehicleVO.class);
	}

	/**
	 * 分页查询授权车辆
	 *
	 * @param page
	 * @param reqDTO
	 * @return
	 */
	@GetMapping("/auth/page")
	@ApiOperation(value = "分页查询授权车辆")
	public Result<IPage<DeviceAuthVehicleRespDTO>> getSmtDeviceAuthVehiclePage(Page page, DeviceAuthVehicleReqDTO reqDTO, @RequestParam(value = "areaIdArray[]", required = false) Integer[] areaIdArray) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		reqDTO.setParkIds(parkIds);
		if (ArrayUtil.isNotEmpty(areaIdArray) && areaIdArray.length > 0) {
			switch (areaIdArray.length) {
				case 1:
					reqDTO.setParkId(areaIdArray[0]);
					break;
				case 2:
					reqDTO.setParkId(areaIdArray[0]);
					reqDTO.setParAreaId(areaIdArray[1]);
					break;
				case 3:
					reqDTO.setParkId(areaIdArray[0]);
					reqDTO.setParAreaId(areaIdArray[1]);
					reqDTO.setAreaId(areaIdArray[2]);
					break;
				default:
					break;
			}
		}
		return success(smtDeviceVehicleService.getDeviceAuthVehicle(page, reqDTO));
	}

	/**
	 * 授权车辆导出
	 * @return
	 */
	@GetMapping("/auth/export")
	@ApiOperation(value = "授权车辆导出")
	public ResponseEntity<byte[]> exportAuthPerson() {
		return smtDeviceVehicleService.exportAuthPerson();
	}

	/**
	 * 查询
	 *
	 * @param smtDeviceTask 设备任务信息表
	 * @return
	 */
	@PostMapping("/list")
	public Result list(@Valid @RequestBody SmtDeviceTask smtDeviceTask) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return success(smtDeviceVehicleService.listSmtDeviceTask(smtDeviceTask, parkIds), DeviceTaskVehicleVO.class);
	}


	/**
	 * 通过id查询设备车辆关联
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return new Result<>(smtDeviceVehicleService.getById(id));
	}

	/**
	 * 新增设备车辆关联
	 *
	 * @param smtDeviceVehicle 设备车辆关联
	 * @return Result
	 */
	@SysLog("新增设备车辆关联")
	@PostMapping("/save")
	public Result save(@RequestBody SmtDeviceVehicle smtDeviceVehicle) {
		return new Result<>(smtDeviceVehicleService.save(smtDeviceVehicle));
	}

	/**
	 * 修改设备车辆关联
	 *
	 * @param smtDeviceVehicle 设备车辆关联
	 * @return Result
	 */
	@SysLog("修改设备车辆关联")
	@PostMapping("/update")
	public Result updateById(@RequestBody SmtDeviceVehicle smtDeviceVehicle) {
		return new Result<>(smtDeviceVehicleService.updateById(smtDeviceVehicle));
	}

	/**
	 * 通过id删除设备车辆关联
	 *
	 * @param id id
	 * @return Result
	 */
	@SysLog("删除设备车辆关联")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable Integer id) {
		return new Result<>(smtDeviceVehicleService.removeById(id));
	}

}
