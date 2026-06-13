package com.tce.smart.platform.controller.watermeter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.BridgeListenerDTO;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterDataUpdateDTO;
import com.tce.smart.platform.api.dto.req.watermeter.SmartBrakeUpdateDTO;
import com.tce.smart.platform.api.dto.req.watermeter.SmartValveDataUpdateDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterDataUpdateDTO;
import com.tce.smart.platform.service.watermeter.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sunfujian
 * @since 2021/11/10 16:37
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/inner")
public class SmartMeterController extends BaseController {

	private final SmtWaterMeterService waterMeterService;
	private final SmtWaterMeterValveService valveService;
	private final SmtWaterMeterHistoryService waterMeterHistoryService;
	private final SmtEleMeterHistoryService eleMeterHistoryService;
	private final SmtEleMeterService eleMeterService;

	@PostMapping("/water/meter/reading/update")
	public Result<Boolean> waterMeterReadingUpdate(@RequestBody BridgeListenerDTO bridgeListenerDTO) {
		log.info("接收水表读数响应结果：{}", bridgeListenerDTO.getContent());
		if(StrUtil.isBlank(bridgeListenerDTO.getContent())){
			throw new SmartException("接收水表读数响应数据为空");
		}
		WaterMeterDataUpdateDTO dataDTO = JSONUtil.toBean(bridgeListenerDTO.getContent(), WaterMeterDataUpdateDTO.class);
		return success(waterMeterHistoryService.saveCurrentReading(dataDTO));
	}

	@PostMapping("/ele/meter/reading/update")
	public Result<Boolean> eleMeterReadingUpdate(@RequestBody BridgeListenerDTO bridgeListenerDTO) {
		log.info("接收电表读数响应结果：{}", bridgeListenerDTO.getContent());
		if(StrUtil.isBlank(bridgeListenerDTO.getContent())){
			throw new SmartException("接收电表读数响应数据为空");
		}
		EleMeterDataUpdateDTO dataDTO = JSONUtil.toBean(bridgeListenerDTO.getContent(), EleMeterDataUpdateDTO.class);
		return success(eleMeterHistoryService.saveCurrentReading(dataDTO));
	}

	@PostMapping("/water/valve/in/update")
	public Result<Boolean> inValveUpdate(@RequestBody BridgeListenerDTO bridgeListenerDTO) {
		log.info("接收水表内置阀门响应结果：{}", bridgeListenerDTO.getContent());
		if(StrUtil.isBlank(bridgeListenerDTO.getContent())){
			throw new SmartException("接收水表内置阀门响应数据为空");
		}
		SmartValveDataUpdateDTO dataDTO = JSONUtil.toBean(bridgeListenerDTO.getContent(), SmartValveDataUpdateDTO.class);
		return success(waterMeterService.changeValveStatus(dataDTO));
	}

	@PostMapping("/water/valve/out/update")
	public Result<Boolean> outValveUpdate(@RequestBody BridgeListenerDTO bridgeListenerDTO) {
		log.info("接收水表外置阀门响应结果：{}", bridgeListenerDTO.getContent());
		if(StrUtil.isBlank(bridgeListenerDTO.getContent())){
			throw new SmartException("接收水表外置阀门响应数据为空");
		}
		SmartValveDataUpdateDTO dataDTO = JSONUtil.toBean(bridgeListenerDTO.getContent(), SmartValveDataUpdateDTO.class);
		return success(valveService.changeValveStatus(dataDTO));
	}

	@PostMapping("/ele/brake/change")
	public Result<Boolean> brakeChange(@RequestBody BridgeListenerDTO bridgeListenerDTO) {
		log.info("接收电表闸门响应结果：{}", bridgeListenerDTO.getContent());
		if(StrUtil.isBlank(bridgeListenerDTO.getContent())){
			throw new SmartException("接收电表闸门响应数据为空");
		}
		SmartBrakeUpdateDTO dataDTO = JSONUtil.toBean(bridgeListenerDTO.getContent(), SmartBrakeUpdateDTO.class);
		return success(eleMeterService.changeBrakeStatus(dataDTO));
	}
}
