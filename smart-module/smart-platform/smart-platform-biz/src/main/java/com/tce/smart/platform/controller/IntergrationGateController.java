package com.tce.smart.platform.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.dispatcher.api.dto.req.ImageDTO;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.BridgeListenerDTO;
import com.tce.smart.platform.core.dto.AddSnapVehicleDTO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceVehicle;
import com.tce.smart.platform.core.entity.SmtVehicle;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.service.SmtSnapVehicleService;
import com.tce.smart.platform.service.SmtVehicleService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.SmtImageEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @description: IntergrationGateController
 * @date: 2020-07-02 16:59
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/inner/gate")
public class IntergrationGateController extends BaseController {

	@Resource
	private SmtSnapVehicleService smtSnapVehicleService;

	@Resource
	private ImageService imageService;

	@Resource
	private SmtVehicleService smtVehicleService;

	@Resource
	private SmtDeviceService smtDeviceService;


	/**
	 * 接收车辆通行记录通知
	 * @param bridgeListenerDTO
	 * @return
	 */
	@Inner
	@PostMapping("/log/reply")
	public Result<Boolean> replyOfGate(@RequestBody BridgeListenerDTO bridgeListenerDTO){
		log.info("接收车辆通行记录{}",bridgeListenerDTO.getContent());
		if(StringUtils.isBlank(bridgeListenerDTO.getContent())){
			throw new TCEException("车辆通行记录收到数据为空");
		}
		/**
		 * 车辆通行通知信息如下
		 * {"cardNo":"粤S0W2Q0","chnNo":1,"deviceCode":"8623bad42751458bbc375316e1c5ca70",
		 * "direction":0,"eventTime":1600764820,"letPass":1,"remainParkingLot":0,
		 * "snapPhoto":"60620EE87AEF4D44A828C90AFEDEE2E0","totalParkingLot":8631576,
		 * "vehicleBrand":0,"vehicleColor":0,"vehicleLicence":""}
		 */
		JSONObject object = JSONUtil.parseObj(bridgeListenerDTO.getContent());

		AddSnapVehicleDTO addSnapVehicleDTO = new AddSnapVehicleDTO();
		addSnapVehicleDTO.setCardNo(object.getStr("cardNo"));
		addSnapVehicleDTO.setDeviceId(object.getStr("deviceCode"));
		addSnapVehicleDTO.setVehiclePlate(object.getStr("vehicleLicence"));
		addSnapVehicleDTO.setChannelNo(object.getInt("chnNo"));
		addSnapVehicleDTO.setLetPass(object.getInt("letPass"));
		addSnapVehicleDTO.setSnapPhotoId(object.getStr("snapPhoto"));
		addSnapVehicleDTO.setVehicleBrand(object.getStr("vehicleBrand"));
		addSnapVehicleDTO.setVehicleColor(object.getInt("vehicleColor"));

		long nowTimeLong= object.getLong("eventTime").longValue()*1000;
		DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String nowTimeStr = ymdhmsFormat.format(nowTimeLong);

		try {
			addSnapVehicleDTO.setSnapTime(ymdhmsFormat.parse(nowTimeStr));
		} catch (Exception e){}

		addSnapVehicleDTO.setCreateTime(LocalDateTime.now());

		boolean res = smtSnapVehicleService.saveSnapVehicle(addSnapVehicleDTO);
		return success(res);
	}
}
