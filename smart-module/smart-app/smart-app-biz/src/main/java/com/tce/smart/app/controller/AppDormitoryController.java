package com.tce.smart.app.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.SmtDormitoryReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.SearchDormitoryRoomDetailReqDTO;
import com.tce.smart.platform.api.feign.RemoteDormitoryService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Title: AppDormitoryController
 * @Descripition: 宿舍
 * @Auther: guohongtai
 * @Date: 2020-10-14 21:26
 */
@RestController
@AllArgsConstructor
@RequestMapping("/appdormitory")
public class AppDormitoryController extends BaseController {
	private final RemoteDormitoryService remoteDormitoryService;

	@PostMapping("/queryDormitory")
	public Result queryDormitory(@RequestBody SmtDormitoryReqDTO smtDormitory){
		return success(remoteDormitoryService.queryDormitory(smtDormitory, SecurityConstants.FROM_IN));
	}

	@PostMapping("/queryRoom")
	public Result queryRoom(@RequestBody SearchDormitoryRoomDetailReqDTO smtDormitoryRoom){
		return success(remoteDormitoryService.queryRoom(smtDormitoryRoom, SecurityConstants.FROM_IN));
	}

	@GetMapping("/roomDetail/{staffBadge}")
	public Result getStaffRoomInfo(@PathVariable("staffBadge") String staffBadge){
		return success(remoteDormitoryService.getStaffRoomInfo(staffBadge, SecurityConstants.FROM_IN));
	}

	@GetMapping("/roomList/{staffBadge}")
	public Result getStaffRoomInfoList(@PathVariable("staffBadge") String staffBadge){
		return success(remoteDormitoryService.getSimpleStaffRoomList(staffBadge, SecurityConstants.FROM_IN));
	}

	@GetMapping("/roomDetailByPhone/{phone}/{name}")
	public Result getStaffRoomInfoByPhone(@PathVariable("phone") String phone,@PathVariable("name") String name){
		return success(remoteDormitoryService.getStaffRoomInfoByPhone(phone,name, SecurityConstants.FROM_IN));
	}
}
