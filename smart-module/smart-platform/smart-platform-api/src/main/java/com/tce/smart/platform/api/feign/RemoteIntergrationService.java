package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.BridgeListenerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @author: luohongwen.
 * @Date:Created in 2019/11/7 .
 * @Description: 提供给分发服务通知调用
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteIntergrationService {
	/**
	 * 接收卡片新增结果
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/access/card/add/reply")
	Result<Boolean> replyOfAddCard(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 接收卡片删除结果
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/access/card/delete/reply")
	Result<Boolean> replyOfDeleteCard(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 接收卡片更新结果
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/access/card/update/reply")
	Result<Boolean> replyOfUpdateCard(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 接收人员通行记录通知
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/access/log/reply")
	Result<Boolean> replyOfAccess(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 接收设备状态变更通知
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/device/change")
	Result<Boolean> changeOfDevice(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 接收车辆通行记录通知
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/gate/log/reply")
	Result<Boolean> replyOfGate(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 接收越界报警
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/camera/cross/border/reply")
	Result<Boolean> replyOfCrossBorder(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 接收人脸抓拍通知
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/camera/log/reply")
	Result<Boolean> replyOfCamera(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 接收人证比对结果
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/terminal/log/reply")
	Result<Boolean> replyOfTerminal(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 当前水表读数更新
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/water/meter/reading/update")
	Result<Boolean> replyOfWaterMeterReading(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 当前电表读数更新
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/ele/meter/reading/update")
	Result<Boolean> replyOfEleMeterReading(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 水表内置阀门设备响应状态更新
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/water/valve/in/update")
	Result<Boolean> replyOfInValveUpdate(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 水表外置阀门设备响应状态更新
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/water/valve/out/update")
	Result<Boolean> replyOfOutValveUpdate(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 电表闸门设备响应状态更新
	 * @param bridgeListenerDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/ele/brake/change")
	Result<Boolean> replyOfEleBrakeUpdate(@RequestBody BridgeListenerDTO bridgeListenerDTO, @RequestHeader(SecurityConstants.FROM) String from);
}
