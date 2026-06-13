package com.tce.smart.app.controller.fore;


import com.tce.smart.app.service.AppAdverInfoService;
import com.tce.smart.app.vo.fore.AdverVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/adver")
public class AdverController extends BaseController {

	private final AppAdverInfoService appAdverInfoService;

	/**
	 * 根据广告位置获取广告列表
	 *
	 * @param adverPosition 广告位置
	 * @return 广告列表
	 */
	@GetMapping("/list")
	public Result<List<AdverVo>> getAdverByPosition(@RequestParam Integer adverPosition) {
		return success(appAdverInfoService.getAdverByPosition(adverPosition), AdverVo.class);
	}

}
