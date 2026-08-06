package com.tce.smart.data.controller.ehrview;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizCallowanceRespDTO;
import com.tce.smart.ehrview.core.entity.EvwBizCallowance;
import com.tce.smart.ehrview.core.service.IEvwBizCallowanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @Descripition: 审批中的-补贴信息
 * @Auther: guohongtai
 * @Date: 2020-07-22 09:06
 */
@RestController
@RequestMapping("/evwBizCallowance")
public class EvwBizCallowanceController extends BaseController {
	@Autowired
	public IEvwBizCallowanceService iEvwBizCallowanceService;

	/**
	 *  根据员工工号和月份查询 审批中的-补贴信息
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwBizCallowance> evwBizCallowanceList = iEvwBizCallowanceService.list(badge, queryMonth);
		return success(evwBizCallowanceList, EvwBizCallowanceRespDTO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/detail")
	public Result getByBadge(@RequestParam("badge") String badge, @RequestParam("beginDate") String beginDate){
		EvwBizCallowance evwBizCallowance = iEvwBizCallowanceService.getOne(Wrappers.<EvwBizCallowance>query().lambda()
				.eq(EvwBizCallowance::getBADGE, badge)
				.eq(EvwBizCallowance::getBEGINDATE, DateUtil.parse(beginDate)), Boolean.FALSE);
		return success(evwBizCallowance, EvwBizCallowanceRespDTO.class);
	}
}
