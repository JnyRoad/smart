package com.tce.smart.data.controller.ehrview;

import java.util.List;

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
import com.tce.smart.data.api.dto.ehrview.resp.EvwLregLeaveAllRespDTO;
import com.tce.smart.ehrview.core.entity.EvwLregLeaveAll;
import com.tce.smart.ehrview.core.service.EvwLregLeaveAllService;

import cn.hutool.core.date.DateUtil;

/**
 * 请假信息
 * 控制器
 * @author 齐佩
 *
 */
@RestController
@RequestMapping("/evwLregLeaveAll")
public class EvwLregLeaveAllController  extends BaseController {

	@Autowired
	private EvwLregLeaveAllService evwLregLeaveAllService;
	@Inner
	@OpenApi("server")
	@GetMapping("/info")
	public Result<List<EvwLregLeaveAllRespDTO>> info(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime,@RequestParam("endTime") String endTime){
	  List<EvwLregLeaveAll> list = evwLregLeaveAllService.list(Wrappers.<EvwLregLeaveAll>query().lambda()
					.eq(EvwLregLeaveAll::getBADGE, badge)
					.ge(EvwLregLeaveAll::getBeginTime, DateUtil.parse(beginTime))
					.lt(EvwLregLeaveAll::getEndTime, DateUtil.parse(endTime)));
       return success(list,EvwLregLeaveAllRespDTO.class);
   }


}
