package com.tce.smart.platform.controller.bigdatapanel;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.resp.SmtDormitoryRepairsRespVO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkDataRespDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 大数据面板-园区总览（平台级） 控制器
 * @date: 2020-08-04 11:20
 * @author: wuling
 * @version: 1.0
 */
//@Api(tags = "大数据面板-园区总览")
@RestController
@AllArgsConstructor
@RequestMapping("/bigdata")
public class ParkOverviewController {

	/**
	 * 获取园区数据
	 * @return
	 */
	@ApiOperation("获取园区数据")
	@GetMapping("/park/data")
	public Result<ParkDataRespDTO> getParkData(){
		return null;
	}

	/**
	 * 获取APP动态
	 * @return
	 */
	@ApiOperation("获取APP动态")
	@GetMapping("/app/data")
	public Result<IPage<SmtDormitoryRepairsRespVO>> getAPPData(){
		return null;
	}

	/**
	 * 设备安装
	 * @return
	 */
	@ApiOperation("设备安装")
	@GetMapping("/device/data")
	public Result<IPage<SmtDormitoryRepairsRespVO>> getDeviceData(){
		return null;
	}

	/**
	 * 宿舍动态
	 * @return
	 */
	@ApiOperation("宿舍动态")
	@GetMapping("/dormitory/data")
	public Result<IPage<SmtDormitoryRepairsRespVO>> getDormitoryData(){
		return null;
	}

	/**
	 * 车位动态
	 * @return
	 */
	@ApiOperation("车位动态")
	@GetMapping("/parking/data")
	public Result<IPage<SmtDormitoryRepairsRespVO>> getParkingData(){
		return null;
	}
}
