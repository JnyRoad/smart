package com.tce.smart.data.controller.ehrview;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizLregleaveRespDTO;
import com.tce.smart.ehrview.core.entity.EvwBizLregleave;
import com.tce.smart.ehrview.core.service.EvwBizLregleaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-20 18:57
 */
@RestController
@RequestMapping("/evwBizLregleave")
public class EvwBizLregleaveController extends BaseController {
	@Autowired
	private EvwBizLregleaveService evwBizLregleaveService;

	@Inner
	@GetMapping("/info")
	public Result<List<EvwBizLregleaveRespDTO>> info(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime, @RequestParam("endTime") String endTime){
		List<EvwBizLregleave> list = evwBizLregleaveService.list(Wrappers.<EvwBizLregleave>query().lambda()
				.eq(EvwBizLregleave::getBADGE, badge)
				.ge(Objects.nonNull(beginTime), EvwBizLregleave::getEndTime, DateUtil.parse(beginTime))
				.lt(Objects.nonNull(endTime), EvwBizLregleave::getBeginTime, DateUtil.parse(endTime)));
		return success(list, EvwBizLregleaveRespDTO.class);
	}

	@Inner
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwBizLregleave> evwBizLregleaveList = evwBizLregleaveService.list(badge, queryMonth);
		return success(evwBizLregleaveList, EvwBizLregleaveRespDTO.class);
	}

	@Inner
	@GetMapping("/detail")
	public Result getByBadge(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime){
		EvwBizLregleave evwBizLregleave = evwBizLregleaveService.getOne(Wrappers.<EvwBizLregleave>query().lambda()
				.eq(EvwBizLregleave::getBADGE, badge)
				.eq(EvwBizLregleave::getBeginTime, DateUtil.parse(beginTime)), Boolean.FALSE);
		return success(evwBizLregleave, EvwBizLregleaveRespDTO.class);
	}
}
