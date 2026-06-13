package com.tce.smart.app.controller.fore;

import java.util.Map;

import javax.validation.Valid;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.feign.RemoteParkService;
import org.springframework.web.bind.annotation.*;

import com.tce.smart.app.ao.fore.LocationAo;
import com.tce.smart.app.service.fore.ParkService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import lombok.AllArgsConstructor;

/**
 * 园区信息Controller
 *
 * @author mingkai.wu
 * @date 2019-05-09 14:46:11
 */
@RestController
@AllArgsConstructor
@RequestMapping("/park")
public class ParkController extends BaseController {

	private ParkService parkService;

	private RemoteParkService remoteParkService;

	/**
	 * 经纬度定位
	 *
	 * @param LocationAo 定位ao
	 * @return Result
	 */
	@PostMapping("/location/auto")
	public Result<?> autoLocation(@Valid @RequestBody LocationAo LocationAo) {
		return success(parkService.processlocation(LocationAo));
	}

	/**
	 * 通过id查询园区表
	 * @param id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getParkById(@PathVariable("id") Integer id) {
		return success(remoteParkService.getPakrById(id, SecurityConstants.FROM_IN));
	}

	/**
	 * 分页查询园区信息
	 *
	 * @param params     分页参数
	 * @param locationAo 查询条件
	 * @return
	 */
	@PostMapping("/list")
	public Result<?> getParkList(@RequestParam Map<String, Object> params, @RequestBody LocationAo locationAo) {
		return new Result<>(parkService.getParkList(params, locationAo));
	}

	/**
	 * 分页查询园区信息
	 *
	 * @return
	 */
	@GetMapping("user/list")
	public Result<?> getUserPark() {
		return new Result<>(parkService.getUserPark());
	}
}
