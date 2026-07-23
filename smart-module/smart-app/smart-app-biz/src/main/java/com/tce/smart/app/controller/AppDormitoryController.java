package com.tce.smart.app.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.SmtDormitoryReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.SearchDormitoryRoomDetailReqDTO;
import com.tce.smart.platform.api.feign.RemoteDormitoryService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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
		return success(remoteDormitoryService.getStaffRoomInfo(currentUserBadge(staffBadge), SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
	}

	@GetMapping("/roomList/{staffBadge}")
	public Result getStaffRoomInfoList(@PathVariable("staffBadge") String staffBadge){
		return success(remoteDormitoryService.getSimpleStaffRoomList(currentUserBadge(staffBadge), SecurityConstants.FROM_IN));
	}

	/**
	 * App 仅能通过内部 Feign 查询当前登录员工，路径参数保留用于兼容旧客户端但不能改变查询主体。
	 */
	private String currentUserBadge(String requestedBadge) {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AccessDeniedException("未认证用户不可查询员工入住信息");
		}
		SmartUser currentUser = SecurityUtils.getUser(authentication);
		if (currentUser == null || currentUser.getUsername() == null || !currentUser.getUsername().equals(requestedBadge)) {
			throw new AccessDeniedException("不可查询其他员工入住信息");
		}
		return currentUser.getUsername();
	}
}
