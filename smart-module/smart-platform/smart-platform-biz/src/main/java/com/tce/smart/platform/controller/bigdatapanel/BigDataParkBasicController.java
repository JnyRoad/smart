package com.tce.smart.platform.controller.bigdatapanel;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.*;
import com.tce.smart.platform.service.bigdatapanel.BigDataParkBasicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description: 大数据面板-园区概况（园区级） 控制器
 * @date: 2020-08-04 15:02
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "大数据面板-园区概况（园区级）")
@RestController
@AllArgsConstructor
@RequestMapping("/bigdata")
public class BigDataParkBasicController {

	private final BigDataParkBasicService bigDataParkBasicService;



	/**
	 * 宿舍动态
	 * @return
	 */
	@ApiOperation("宿舍动态")
	@GetMapping("/park/dormitory/{parkId}")
	public Result<ParkDormitoryRespDTO> getParkDormitoryInfo(@PathVariable("parkId") Integer parkId){
		return new Result<>(bigDataParkBasicService.getParkDormitoryInfo(parkId));
	}

	/**
	 * 车位动态
	 * @return
	 */
	@ApiOperation("车位动态")
	@GetMapping("/park/parking/{parkId}")
	public Result<ParkParkingRespDTO> getParkParkingInfo(@ApiParam(name = "parkId",value = "园区id",required = true) @PathVariable("parkId") Integer parkId){
		return new Result<>(bigDataParkBasicService.getParkParkingInfo(parkId));
	}

	/**
	 * 访客实时
	 * @return
	 */
	@ApiOperation("访客实时")
	@GetMapping("/park/visitor/{parkId}")
	public Result<ParkVisitorRespDTO> getParkVisitorInfo(@PathVariable("parkId") Integer parkId){
		return new Result<>(bigDataParkBasicService.getParkVisitorInfo(parkId));
	}

	/**
	 * 区域设备抓拍
	 * @return
	 */
	@ApiOperation("区域设备抓拍")
	@GetMapping("/park/areadevice/snap/{parkId}")
	public Result<List<AreaDeviceSnapRespDTO>> getAreaDeviceSnapInfo(@PathVariable("parkId") Integer parkId){
		return new Result<>(bigDataParkBasicService.getAreaDeviceSnapData(parkId));
	}

}
