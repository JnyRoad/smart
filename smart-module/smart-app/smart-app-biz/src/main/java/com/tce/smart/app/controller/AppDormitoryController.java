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

	/**
	 * 查询当前登录员工的宿舍详情。
	 *
	 * 工号只能从认证上下文取得，客户端不得再通过路径参数指定任意员工。
	 */
	@GetMapping("/me/roomDetail")
	public Result getMyRoomDetail(){
		return success(remoteDormitoryService.getStaffRoomInfo(currentAuthenticatedBadge(), SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
	}

	/**
	 * 查询当前登录员工的宿舍记录列表。
	 *
	 * 内部 Feign 契约显式声明服务令牌标记，避免仅凭可伪造的内部来源头放行。
	 */
	@GetMapping("/me/roomList")
	public Result getMyRoomList(){
		return success(remoteDormitoryService.getStaffRoomInfoList(currentAuthenticatedBadge(), SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
	}

	/**
	 * App 仅能通过内部 Feign 查询当前登录员工，不接受客户端提供的工号。
	 */
	private String currentAuthenticatedBadge() {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AccessDeniedException("未认证用户不可查询员工入住信息");
		}
		SmartUser currentUser = SecurityUtils.getUser(authentication);
		if (currentUser == null || currentUser.getUsername() == null) {
			throw new AccessDeniedException("未认证用户不可查询员工入住信息");
		}
		return currentUser.getUsername();
	}
}
