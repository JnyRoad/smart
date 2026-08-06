package com.tce.smart.data.api.feign.xcvehicle;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.consume.req.RsEmpSaveReqDto;
import com.tce.smart.data.api.dto.xcvehicle.req.XCVehicleAddDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author wuling
 * @date 2020-7-9
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteXCVehicleService {

	@PostMapping("/xc-vehicle/inner/saveVehicle")
	Result<Boolean> saveVehicle(@RequestBody XCVehicleAddDTO xcVehicleAddDTO,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@PostMapping("/xc-vehicle/inner/deleteVehicle/{cardNo}")
	Result<Boolean> deleteVehicle(@PathVariable("cardNo")String cardNo,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
