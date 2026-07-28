package com.tce.smart.data.controller.ehrview;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.resp.EvwCallowanceCancelAlltRespDTO;
import com.tce.smart.ehrview.core.entity.EvwCallowanceCancelAllt;
import com.tce.smart.ehrview.core.service.IEvwCallowanceCancelAlltService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Descripition: 外宿补贴审批中撤销信息查询
 * @Auther: guohongtai
 * @Date: 2020-07-21 11:11
 */
@RestController
@RequestMapping("/evwCallowanceCancelAllt")
public class EvwCallowanceCancelAlltController extends BaseController {
	@Autowired
	public IEvwCallowanceCancelAlltService iEvwCallowanceCancelAlltService;

	/**
	 *  根据员工工号和月份查询 外宿补贴审批中撤销信息
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwCallowanceCancelAllt> evwCallowanceCancelAlltList = iEvwCallowanceCancelAlltService.list(badge, queryMonth);
		return success(evwCallowanceCancelAlltList, EvwCallowanceCancelAlltRespDTO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/detail")
	public Result getByBadge(@RequestParam("badge") String badge, @RequestParam("backDate") String backDate){
		EvwCallowanceCancelAllt evwCallowanceCancelAllt = iEvwCallowanceCancelAlltService.getOne(Wrappers.<EvwCallowanceCancelAllt>query().lambda()
				.eq(EvwCallowanceCancelAllt::getBADGE, badge)
				.eq(EvwCallowanceCancelAllt::getBACKDATE, DateUtil.parse(backDate)), Boolean.FALSE);
		return success(evwCallowanceCancelAllt, EvwCallowanceCancelAlltRespDTO.class);
	}
}
