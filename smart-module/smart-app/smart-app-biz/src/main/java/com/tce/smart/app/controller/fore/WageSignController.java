package com.tce.smart.app.controller.fore;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.dto.fore.WageSignDto;
import com.tce.smart.app.service.fore.WageSignService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import lombok.AllArgsConstructor;

/**
 * 工资签单
 *
 * @author mingkai.wu
 * @date 2019-05-09 14:46:11
 */
@RestController
@AllArgsConstructor
@RequestMapping("/wage/sign")
public class WageSignController extends BaseController {

	private WageSignService WageSignService;

	/**
	 * 提交签名
	 *
	 * @param wageSignDto 签名信息
	 * @return Result
	 */
	@PostMapping("/save")
	public Result<?> updateStatusById(@RequestBody WageSignDto wageSignDto) {
		return WageSignService.updateToSign(wageSignDto);
	}

}
