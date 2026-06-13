package com.tce.smart.app.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.feign.RemoteOutDormitoryStaffService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Auther: guohongtai
 * @Date: 2020-08-19 18:08
 */
@RestController
@AllArgsConstructor
@RequestMapping("/appout/dormitory/staff")
public class AppOutDormitoryStaffController extends BaseController {
	private final RemoteOutDormitoryStaffService remoteOutDormitoryStaffService;

	@GetMapping("/getOutDormitoryInfo")
	public Result getOutDormitoryInfo(@RequestParam("staffBadge") String staffBadge){
		return remoteOutDormitoryStaffService.getOutDormitoryInfo(staffBadge, 11, SecurityConstants.FROM_IN);
	}
}