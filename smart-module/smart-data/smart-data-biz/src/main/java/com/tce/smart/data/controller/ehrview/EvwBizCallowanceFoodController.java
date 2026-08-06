package com.tce.smart.data.controller.ehrview;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizCallowanceFoodRespDTO;
import com.tce.smart.ehrview.core.entity.EvwBizCallowanceFood;
import com.tce.smart.ehrview.core.service.IEvwBizCallowanceFoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Descripition: 外宿补贴审批中信息查询
 * @Auther: guohongtai
 * @Date: 2020-07-20 21:17
 */
@RestController
@RequestMapping("/evwBizCallowanceFood")
public class EvwBizCallowanceFoodController extends BaseController {
	@Autowired
	public IEvwBizCallowanceFoodService iEvwCallowanceAllService;

	/**
	 *  根据员工工号和月份查询 外宿补贴审批中信息
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwBizCallowanceFood> evwCallowanceAllList = iEvwCallowanceAllService.list(badge, queryMonth);
		return success(evwCallowanceAllList, EvwBizCallowanceFoodRespDTO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/detail")
	public Result getByBadge(@RequestParam("badge") String badge, @RequestParam("beginDate") String beginDate){
		EvwBizCallowanceFood evwBizCallowanceFood = iEvwCallowanceAllService.getOne(Wrappers.<EvwBizCallowanceFood>query().lambda()
				.eq(EvwBizCallowanceFood::getBADGE, badge)
				.eq(EvwBizCallowanceFood::getBEGINDATE, DateUtil.parse(beginDate)), Boolean.FALSE);
		return success(evwBizCallowanceFood, EvwBizCallowanceFoodRespDTO.class);
	}
}
