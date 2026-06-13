package com.tce.smart.app.controller.fore;

import com.tce.smart.app.ao.fore.AuthParkAo;
import com.tce.smart.app.service.fore.VehicleService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.AddVehicleReqDTO;
import com.tce.smart.platform.api.dto.req.ApplyAuthReqDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 员工车辆信息 控制器
 * @author qipei
 *
 */

@RestController
@AllArgsConstructor
@RequestMapping("/employee/vehicle")
public class VehicleController extends BaseController{

	private  VehicleService vehicleService;

	/**
	 * 获取员工车辆信息
	 * @param params
	 * @return
	 */
	@GetMapping("/baseinfo")
	public Result<?> getVehicleList(@RequestParam Map<String, Object> params) {
		return new Result<>(vehicleService.getVehicleList(params));
	}

	/**
	 * 获取车辆通行权限列表
	 * @param ao 车牌
	 * @return
	 */
	@PostMapping("/auth/park")
	public Result<?> getAuthPark(@RequestBody AuthParkAo ao){
		return new Result<>(vehicleService.getAuthPark(ao));
	}


	/**
	 * 查看车辆通行权限详情
	 * @param ao 车辆园区权限编号
	 * @return
	 */
	@PostMapping("/auth/detail")
	public Result<?> getAuthDetail(@RequestBody AuthParkAo ao ) {
		return new Result<>(vehicleService.getAuthDetail(ao ));
	}

	/**
	 * 申请车辆通行权限
	 * @param applyAuthDTO
	 * @return
	 */
	@PostMapping("/auth/apply")
	public Result<?> addAuthApply(@RequestBody ApplyAuthReqDTO applyAuthDTO ) {
		return vehicleService.addAuthApply(applyAuthDTO);
	}


	/**
	 * 员工车辆添加
	 * @param addVehicleDTO
	 * @return
	 */
	@PostMapping("/add")
	public Result<?> addVehicle(@RequestBody AddVehicleReqDTO addVehicleDTO ) {
		return vehicleService.addVehicle(addVehicleDTO);
	}

	/**
	 * 获取颜色字典列表
	 * @return
	 */
	@GetMapping("/color/type")
	public Result<?> getColorType() {
		return new Result<>(vehicleService.getColorType());
	}

	/**
	 * 获取车辆类型
	 * @return
	 */
	@GetMapping("/type")
	public Result<?> getVehicleType() {
		return new Result<>(vehicleService.getVehicleType());
	}

	/**
	 * 移除车辆和通行权限
	 * @return
	 */
	@PostMapping("/delete")
	public Result<?> delete(@RequestBody AuthParkAo ao){
		return vehicleService.delete(ao);
	}

}
