package com.tce.smart.data.controller.ehrview;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.resp.EvwCotherAllowanceAllRespDTO;
import com.tce.smart.ehrview.core.entity.EvwCotherAllowanceAll;
import com.tce.smart.ehrview.core.service.IEvwCotherAllowanceAllService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @Descripition: 补贴历史记录查询
 * @Auther: guohongtai
 * @Date: 2020-07-22 09:06
 */
@RestController
@RequestMapping("/evwCotherAllowanceAll")
public class EvwCotherAllowanceAllController extends BaseController {
	@Autowired
	public IEvwCotherAllowanceAllService iEvwCotherAllowanceAllService;

	/**
	 *  根据员工工号和月份查询 补贴历史记录查询
	 * @return
	 */
	@Inner
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwCotherAllowanceAll> evwCotherAllowanceAllList = iEvwCotherAllowanceAllService.list(badge, queryMonth);
		return success(evwCotherAllowanceAllList, EvwCotherAllowanceAllRespDTO.class);
	}

	@Inner
	@GetMapping("/detail")
	public Result getByBadge(@RequestParam("badge") String badge, @RequestParam("beginDate") String beginDate){
		EvwCotherAllowanceAll evwCotherAllowanceAll = iEvwCotherAllowanceAllService.getOne(Wrappers.<EvwCotherAllowanceAll>query().lambda()
				.eq(EvwCotherAllowanceAll::getBADGE, badge)
				.eq(EvwCotherAllowanceAll::getBEGINDATE, DateUtil.parse(beginDate)), Boolean.FALSE);
		return success(evwCotherAllowanceAll, EvwCotherAllowanceAllRespDTO.class);
	}
}
