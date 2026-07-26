package com.tce.smart.platform.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtVehicleRespDTO;
import com.tce.smart.platform.api.dto.req.AddVehicleReqDTO;
import com.tce.smart.platform.api.dto.req.ApplyAuthReqDTO;
import com.tce.smart.platform.api.dto.resp.VehicleApplyRespDTO;
import com.tce.smart.platform.api.dto.resp.VehicleAuthDetailRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 我的车辆
 * @author 齐佩
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteVehicleService {

	/**
	 * 查询我的车辆列表
	 * @param current 页号
	 * @param size 返回数量
	 * @param badge 员工号
	 * @return Result
	 */
	@GetMapping("/staff/myVehicle")
	Result<Page<SmtVehicleRespDTO>> getMyVehicle(@RequestParam("current") final long current, @RequestParam("size") final long size , @RequestParam("badge") String badge, @RequestHeader(SecurityConstants.FROM) String from, @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth, @RequestHeader("X-Smart-Internal-Purpose") String purpose);

	/**
	 * 获取车辆入园列表
	 * @param plateNumber 车牌号
	 * @return Result
	 */
	@GetMapping("/staff/getVehiclePark")
	Result<List<VehicleApplyRespDTO>> getVehiclePark(@RequestParam("plateNumber") String plateNumber, @RequestParam("badge") String badge, @RequestHeader(SecurityConstants.FROM) String from, @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth, @RequestHeader("X-Smart-Internal-Purpose") String purpose);


	/**
	 * 查看车辆入园权限的详情
	 * @param id 车辆入园权限id
	 * @return Result
	 */
	@GetMapping("/staff/getVehicleParkById/{id}")
	Result<VehicleAuthDetailRespDTO> getVehicleParkById(@PathVariable("id") Integer id, @RequestParam("badge") String badge, @RequestHeader(SecurityConstants.FROM) String from, @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth, @RequestHeader("X-Smart-Internal-Purpose") String purpose);

	/**
	 * 添加车辆
	 * @param addVehicleDTO 车辆信息
	 * @return Result
	 */
	@PostMapping("/staff/addVehicle")
	Result addVehicle(@RequestBody AddVehicleReqDTO addVehicleDTO, @RequestHeader(SecurityConstants.FROM) String from, @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth, @RequestHeader("X-Smart-Internal-Purpose") String purpose);


	/**
	 * 车辆入园申请
	 * @param applyAuthReqDTO 车辆申请园区信息
	 * @return Result
	 */
	@PostMapping("/staff/addVehiclePark")
	Result addVehiclePark(@RequestBody ApplyAuthReqDTO applyAuthReqDTO, @RequestHeader(SecurityConstants.FROM) String from, @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth, @RequestHeader("X-Smart-Internal-Purpose") String purpose);

	/**
	 * 删除车辆信息
	 * @param plateNumber 车牌号
	 * @return Result
	 */
	@GetMapping("/staff/delVehicle")
	Result delVehicle(@RequestParam("plateNumber") String plateNumber, @RequestParam("badge") String badge, @RequestHeader(SecurityConstants.FROM) String from, @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth, @RequestHeader("X-Smart-Internal-Purpose") String purpose);

}
