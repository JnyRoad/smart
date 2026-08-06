package com.tce.smart.data.controller.ehrview;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizLregleaveRegisterRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwLdxRegLeaveAllRespDTO;
import com.tce.smart.ehrview.core.entity.EvwBizLregleaveRegister;
import com.tce.smart.ehrview.core.entity.EvwLdxRegLeaveAll;
import com.tce.smart.ehrview.core.service.IEvwLdxRegLeaveAllService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 11:30
 */
@RestController
@RequestMapping("/evwLdxRegLeaveAll")
public class EvwLdxRegLeaveAllController extends BaseController {
	@Autowired
	public IEvwLdxRegLeaveAllService iEvwLdxRegLeaveAllService;

	/**
	 *  根据员工工号和月份查询调休历史记录
	 * @return
	 */
	@Inner
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwLdxRegLeaveAll> evwLdxRegLeaveAllList = iEvwLdxRegLeaveAllService.list(badge, queryMonth);
		return success(evwLdxRegLeaveAllList, EvwLdxRegLeaveAllRespDTO.class);
	}

	/**
	 *  根据员工工号和日期查询调休历史记录
	 * @return
	 */
	@Inner
	@GetMapping("/list/byDay")
	public Result<List<EvwLdxRegLeaveAllRespDTO>> listByDay(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwLdxRegLeaveAll> evwLdxRegLeaveAllList = iEvwLdxRegLeaveAllService.listByDay(badge, queryMonth);
		return success(evwLdxRegLeaveAllList, EvwLdxRegLeaveAllRespDTO.class);
	}

	@Inner
	@GetMapping("/detail")
	public Result getByBadge(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime){
		EvwLdxRegLeaveAll evwLdxRegLeaveAll = iEvwLdxRegLeaveAllService.getOne(Wrappers.<EvwLdxRegLeaveAll>query().lambda()
				.eq(EvwLdxRegLeaveAll::getBADGE, badge)
				.eq(EvwLdxRegLeaveAll::getBEGINTIME, DateUtil.parse(beginTime)), Boolean.FALSE);
		return success(evwLdxRegLeaveAll, EvwLdxRegLeaveAllRespDTO.class);
	}
}
