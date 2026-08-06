package com.tce.smart.data.controller.ehrview;

import java.util.List;
import java.util.Objects;

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
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizLregleaveRegisterRespDTO;
import com.tce.smart.ehrview.core.entity.EvwBizLregleaveRegister;
import com.tce.smart.ehrview.core.service.EvwBizLregleaveRegisterService;

import cn.hutool.core.date.DateUtil;

/**
 * 请假信息
 * 控制器
 * @author 齐佩
 *
 */
@RestController
@RequestMapping("/evwBizLregleaveRegister")
public class  EvwBizLregleaveRegisterController extends BaseController {
	 @Autowired
	 private EvwBizLregleaveRegisterService evwBizLregleaveRegisterService;

    @Inner
    @OpenApi("server")
    @GetMapping("/info")
    public Result<List<EvwBizLregleaveRegisterRespDTO>> info(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime,@RequestParam("endTime") String endTime){
	List<EvwBizLregleaveRegister> list = evwBizLregleaveRegisterService.list(Wrappers.<EvwBizLregleaveRegister>query().lambda()
					.eq(EvwBizLregleaveRegister::getBADGE, badge)
					.ge(Objects.nonNull(beginTime), EvwBizLregleaveRegister::getBeginTime, DateUtil.parse(beginTime))
					.lt(Objects.nonNull(endTime), EvwBizLregleaveRegister::getEndTime, DateUtil.parse(endTime)));
        return success(list, EvwBizLregleaveRegisterRespDTO.class);
    }

	@Inner
	@OpenApi("server")
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwBizLregleaveRegister> evwBizLregleaveRegisterList = evwBizLregleaveRegisterService.list(badge, queryMonth);
		return success(evwBizLregleaveRegisterList, EvwBizLregleaveRegisterRespDTO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/detail")
	public Result getByBadge(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime){
		EvwBizLregleaveRegister evwBizLregleaveRegister = evwBizLregleaveRegisterService.getOne(Wrappers.<EvwBizLregleaveRegister>query().lambda()
				.eq(EvwBizLregleaveRegister::getBADGE, badge)
				.eq(EvwBizLregleaveRegister::getBeginTime, DateUtil.parse(beginTime)), Boolean.FALSE);
		return success(evwBizLregleaveRegister, EvwBizLregleaveRegisterRespDTO.class);
	}
}
