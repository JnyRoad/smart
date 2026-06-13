package com.tce.smart.platform.controller;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.DeviceAuthPersonReqDTO;
import com.tce.smart.platform.api.dto.resp.DeviceAuthPersonRespDTO;
import com.tce.smart.platform.core.dto.DeviceDataDTO;
import com.tce.smart.platform.core.entity.SmtDevicePerson;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.service.SmtDevicePersonService;
import com.tce.smart.platform.core.vo.DevicePersonVO;
import com.tce.smart.platform.service.IDevicePersonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 设备人员关联
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:38
 */
@RestController
@AllArgsConstructor
@RequestMapping("/device/person")
@Api(value = "device_person", tags = "设备人员授权")
public class SmtDevicePersonController extends BaseController {

	private final SmtDevicePersonService smtDevicePersonService;

	private final IDevicePersonService bizDevicePersonService;

	/**
	 * 分页查询
	 *
	 * @param page            分页对象
	 * @param devicePersonDTO 查询条件
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtDevicePersonPage(Page page, DeviceDataDTO devicePersonDTO) {
		return success(smtDevicePersonService.getDevicePerson(page, devicePersonDTO), DevicePersonVO.class);
	}

	/**
	 * 分页查询授权人员
	 *
	 * @param page
	 * @param reqDTO
	 * @return
	 */
	@GetMapping("/auth/page")
	@ApiOperation(value = "分页查询授权人员")
	public Result<IPage<DeviceAuthPersonRespDTO>> getSmtDeviceAuthPersonPage(Page page, DeviceAuthPersonReqDTO reqDTO, @RequestParam(value = "areaIdArray[]", required = false) Integer[] areaIdArray) {
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
		return success(smtDevicePersonService.getDeviceAuthPerson(page, reqDTO));
	}

	/**
	 * 授权人员导出
	 * @return
	 */
	@GetMapping("/auth/export")
	@ApiOperation(value = "授权人员导出")
	public ResponseEntity<byte[]> exportAuthPerson() {
		return smtDevicePersonService.exportAuthPerson();
	}

	/**
	 * 查询
	 *
	 * @param smtDeviceTask 设备任务信息表
	 * @return
	 */
	@PostMapping("/list")
	public Result list(@RequestBody SmtDeviceTask smtDeviceTask) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return success(smtDevicePersonService.listDownRecord(smtDeviceTask, parkIds));
	}

	@PostMapping("/image")
	public Result image(@RequestBody SmtDeviceTask smtDeviceTask) {
		return new Result(bizDevicePersonService.image(smtDeviceTask));
	}

	/**
	 * 通过id查询设备人员关联
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return new Result<>(smtDevicePersonService.getById(id));
	}

	/**
	 * 新增设备人员关联
	 *
	 * @param smtDevicePerson 设备人员关联
	 * @return Result
	 */
	@SysLog("新增设备人员关联")
	@PostMapping("/save")
	public Result save(@RequestBody SmtDevicePerson smtDevicePerson) {
		return new Result<>(smtDevicePersonService.save(smtDevicePerson));
	}

	/**
	 * 修改设备人员关联
	 *
	 * @param smtDevicePerson 设备人员关联
	 * @return Result
	 */
	@SysLog("修改设备人员关联")
	@PostMapping("/update")
	public Result updateById(@RequestBody SmtDevicePerson smtDevicePerson) {
		return new Result<>(smtDevicePersonService.updateById(smtDevicePerson));
	}

	/**
	 * 通过id删除设备人员关联
	 *
	 * @param id id
	 * @return Result
	 */
	@SysLog("删除设备人员关联")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable Integer id) {
		return new Result<>(smtDevicePersonService.removeById(id));
	}

}
