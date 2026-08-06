package com.tce.smart.data.controller.xcvehicle;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.consume.req.RsEmpSaveReqDto;
import com.tce.smart.data.api.dto.xcvehicle.req.XCVehicleAddDTO;
import com.tce.smart.xcc6.core.service.IRsXCEmpService;
import com.tce.smart.xcvehicle.core.dto.TParkCardAddDTO;
import com.tce.smart.xcvehicle.core.service.TParkCardService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 *
 * @author wuling
 * @date 2020-7-9
 */
@ConditionalOnProperty(name = "xc-vehicle.datasource.type",havingValue = "com.alibaba.druid.pool.DruidDataSource")
@RestController
@RequestMapping("/xc-vehicle")
public class TParkCardController extends BaseController {

	@Autowired
	private TParkCardService tParkCardService;

	/**
	 * 保存车辆信息
	 *
	 * @return
	 */
	@PostMapping("/inner/saveVehicle")
	@Inner
	@OpenApi("server")
	public Result<Boolean> saveVehicle(@RequestBody XCVehicleAddDTO xcVehicleAddDTO) {
		TParkCardAddDTO tParkCardAddDTO = new TParkCardAddDTO();
		BeanUtils.copyProperties(xcVehicleAddDTO,tParkCardAddDTO);
		tParkCardAddDTO.setRemark(xcVehicleAddDTO.getBadge());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");
		String dateTime = LocalDateTime.now(ZoneOffset.of("+8")).format(formatter);
		tParkCardAddDTO.setHCardNo(dateTime);
		return success(tParkCardService.addParkCard(tParkCardAddDTO));
	}

	/**
	 * 删除车辆信息
	 *
	 * @return
	 */
	@PostMapping("/inner/deleteVehicle/{cardNo}")
	@Inner
	@OpenApi("server")
	public Result<Boolean> deleteVehicle(@PathVariable("cardNo")String cardNo) {
		return success(tParkCardService.deleteParkCard(cardNo));
	}
}
