package com.tce.smart.data.controller.ehrview;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.resp.EvwCallowanceAlltRespDTO;
import com.tce.smart.ehrview.core.entity.EvwCallowanceAllt;
import com.tce.smart.ehrview.core.service.IEvwCallowanceAlltService;
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
@RequestMapping("/evwCallowanceAllt")
public class EvwCallowanceAlltController extends BaseController {
	@Autowired
	public IEvwCallowanceAlltService iEvwCallowanceAlltService;

	/**
	 *  根据员工工号和月份查询 外宿补贴审批中信息
	 * @return
	 */
	@Inner
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwCallowanceAllt> evwCallowanceAlltList = iEvwCallowanceAlltService.list(badge, queryMonth);
		return success(evwCallowanceAlltList, EvwCallowanceAlltRespDTO.class);
	}

	@Inner
	@GetMapping("/detail")
	public Result getByBadge(@RequestParam("badge") String badge, @RequestParam("beginDate") String beginDate){
		EvwCallowanceAllt evwCallowanceAllt = iEvwCallowanceAlltService.getOne(Wrappers.<EvwCallowanceAllt>query().lambda()
				.eq(EvwCallowanceAllt::getBADGE, badge)
				.eq(EvwCallowanceAllt::getBEGINDATE, DateUtil.parse(beginDate)), Boolean.FALSE);
		return success(evwCallowanceAllt, EvwCallowanceAlltRespDTO.class);
	}
}
