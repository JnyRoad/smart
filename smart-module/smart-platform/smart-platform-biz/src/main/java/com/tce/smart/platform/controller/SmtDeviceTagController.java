package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.resp.DeviceTagListDTO;
import com.tce.smart.platform.service.SmtDeviceTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author sunfujian
 * @date 2021/7/29 11:29
 */
@RestController
@AllArgsConstructor
@RequestMapping("/device/tag")
@Api(value = "device_tag", tags = "设备标签")
public class SmtDeviceTagController extends BaseController {

	private final SmtDeviceTagService deviceTagService;

	/**
	 * 分页查询设备标签
	 * @param page
	 * @param tagName
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperation(value = "分页查询设备标签")
	public Result<IPage<DeviceTagListDTO>> getPage(Page page, @RequestParam(value = "tagName", required = false) String tagName) {
		return success(deviceTagService.getPage(page, tagName), DeviceTagListDTO.class);
	}

	/**
	 * 获取设备标签列表
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperation(value = "获取设备标签列表")
	public Result<List<DeviceTagListDTO>> getList() {
		return success(deviceTagService.list(), DeviceTagListDTO.class);
	}

	/**
	 * 添加设备标签
	 * @param tagName
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperation(value = "添加设备标签")
	public Result<Boolean> save(@RequestParam("tagName") String tagName) {
		return success(deviceTagService.save(tagName));
	}

	/**
	 * 更新设备标签
	 * @param id
	 * @param tagName
	 * @return
	 */
	@PostMapping("/update/{id}")
	@ApiOperation(value = "更新设备标签")
	public Result<Boolean> update(@PathVariable("id") Long id, @RequestParam("tagName") String tagName) {
		return success(deviceTagService.update(id, tagName));
	}

	/**
	 * 删除设备标签
	 * @param id
	 * @return
	 */
	@PostMapping("/{id}")
	@ApiOperation(value = "删除设备标签")
	public Result<Boolean> delete(@PathVariable("id") Long id) {
		return success(deviceTagService.removeById(id));
	}

}
