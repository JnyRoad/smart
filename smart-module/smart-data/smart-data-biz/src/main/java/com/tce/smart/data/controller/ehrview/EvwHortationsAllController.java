package com.tce.smart.data.controller.ehrview;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.resp.EvwHortationsAllRespDTO;
import com.tce.smart.ehrview.core.entity.EvwHortationsAll;
import com.tce.smart.ehrview.core.service.IEvwHortationsAllService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-14 10:03
 */
@RestController
@RequestMapping("/evwHortationsall")
public class EvwHortationsAllController extends BaseController {

	@Autowired
	public IEvwHortationsAllService iEvwHortationsAllService;

	/**
	 *  根据员工工号和月份查询奖惩记录
	 * @return
	 */
	@Inner
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwHortationsAll> evwHortationsAllList = iEvwHortationsAllService.list(badge, queryMonth);
		return success(evwHortationsAllList, EvwHortationsAllRespDTO.class);
	}

}
