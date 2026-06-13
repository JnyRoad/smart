package com.tce.smart.app.controller.fore;

import cn.hutool.core.date.DateUtil;
import com.tce.smart.common.core.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.service.fore.AgreementService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import java.time.LocalDateTime;

/**
 * 协议服务控制器
 *
 * @author mckaywu
 * @date 2019-06-17 10:01:35
 */
@RestController
@RequestMapping("/agreement")
public class AgreementController extends BaseController {

	@Autowired
	private AgreementService agreementService;

	/**
	 * 获取外宿协议协议信息
	 *
	 * @param parkId 园区ID
	 * @return
	 */
	@GetMapping("/room/out")
	public Result<?> getRootOutAgree(@RequestParam(value = "parkId") String parkId) {
		return success(agreementService.getRootOutAgree(parkId));
	}

	/**
	 * 获取app用户服务协议信息
	 * @return
	 */
	@GetMapping("/service")
	public Result<?> getServiceAgree() {
		return success(agreementService.getServiceAgree());
	}

}
