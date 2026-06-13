package com.tce.smart.app.controller.fore;

import com.tce.smart.app.service.fore.MessageService;
import com.tce.smart.app.vo.fore.AppMsgPushListVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.resp.QueryAppMsgRecRespDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * App消息模块控制器
 *
 * @author mkwu
 * @date 2019-07-07
 */
@RestController
@RequestMapping("/message")
public class MessageController extends BaseController {

	@Autowired
	private MessageService messageService;

	/**
	 * 设置App消息为已读
	 *
	 * @param recordId 消息记录ID
	 * @return
	 */
	@GetMapping("/update/read")
	public Result<Boolean> changeRecordToRead(@RequestParam("recordId") Integer recordId) {
		return new Result<>(messageService.changeRecordToRead(recordId));
	}


	@GetMapping("/update/all/read")
	public Result<Boolean> changeAllRecordToRead(@RequestParam(value = "deviceNo") String deviceNo) {
		return new Result<>(messageService.changeAllRecordToRead(deviceNo));
	}


	/**
	 * 删除App消息
	 *
	 * @param recordId 消息记录ID
	 * @return
	 */
	@GetMapping("/delete")
	public Result<Boolean> deleteMsg(@RequestParam("recordId") Integer recordId) {
		return new Result<>(messageService.deleteMsg(recordId));
	}

	/**
	 * 删除所有App消息
	 *
	 * @param deviceNo 消息记录ID
	 * @return
	 */
	@GetMapping("/delete/all")
	public Result<Boolean> deleteAllMsg(@RequestParam(value = "deviceNo") String deviceNo) {
		return new Result<>(messageService.deleteAllMsg(deviceNo));
	}

	/**
	 * 统计App消息数
	 *
	 * @param deviceNo 设备编号
	 * @return
	 */
	@GetMapping("/count/app")
	public Result<QueryAppMsgRecRespDTO> countAppMsg(@RequestParam(value = "deviceNo") String deviceNo) {
		return new Result<>(messageService.countAppMsg(deviceNo));
	}

	/**
	 * 获取App消息推送记录
	 *
	 * @param current  当前页
	 * @param size     大小
	 * @param deviceNo 设备编号
	 * @return
	 */
	@GetMapping("/push/list")
	public Result<?> getMessageList(@RequestParam(value = "current") long current,
									@RequestParam(value = "size") long size,
									@RequestParam(value = "deviceNo") String deviceNo) {
		return success(messageService.getAppMsgList(current, size, deviceNo), AppMsgPushListVo.class);
	}
}
