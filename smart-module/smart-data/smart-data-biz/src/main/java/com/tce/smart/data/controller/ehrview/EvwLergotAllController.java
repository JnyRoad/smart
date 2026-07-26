package com.tce.smart.data.controller.ehrview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import cn.hutool.core.date.DateUtil;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.EvwLergotAllRespDTO;
import com.tce.smart.ehrview.core.entity.EvwLergotAll;
import com.tce.smart.ehrview.core.service.EvwLergotAllService;


/**
 * 加班历史记录
 * @author 齐佩
 *
 */
@RestController
@RequestMapping("/evwLergotAll")
public class EvwLergotAllController  extends BaseController {
	@Autowired
	private EvwLergotAllService evwLergotAllService;

	@Inner
	@OpenApi("server")
	@GetMapping("/info")
	public Result<List<EvwLergotAllRespDTO>> info(@RequestParam("badge") String badge, @RequestParam("otTerm") String otTerm){

	 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
	 List<EvwLergotAll> list = null;
		try {
			list = evwLergotAllService.list(Wrappers.<EvwLergotAll>query().lambda()
						.eq(EvwLergotAll::getBADGE, badge)
						.ge(EvwLergotAll::getOTTERM,df.parse(otTerm+" 00:00:00"))
						.lt(EvwLergotAll::getOTTERM, df.parse(otTerm+" 23:59:59")));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

      return success(list,EvwLergotAllRespDTO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwLergotAll> evwLergotAllList = evwLergotAllService.list(badge, queryMonth);
		return success(evwLergotAllList, EvwLergotAllRespDTO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/detail")
	public Result getByBadge(@RequestParam("badge") String badge, @RequestParam("otteam") String otteam){
		EvwLergotAll evwLergotAll = evwLergotAllService.getOne(Wrappers.<EvwLergotAll>query().lambda()
				.eq(EvwLergotAll::getBADGE, badge)
				.eq(EvwLergotAll::getOTTERM, DateUtil.parse(otteam)), Boolean.FALSE);
		return success(evwLergotAll, EvwLergotAllRespDTO.class);
	}
}
