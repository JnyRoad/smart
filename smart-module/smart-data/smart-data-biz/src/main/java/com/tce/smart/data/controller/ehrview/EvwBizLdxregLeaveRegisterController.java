package com.tce.smart.data.controller.ehrview;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizLdxregLeaveRegisterRespDTO;
import com.tce.smart.ehrview.core.entity.EvwBizLdxregLeaveRegister;
import com.tce.smart.ehrview.core.service.IEvwBizLdxregLeaveRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 11:33
 */
@RestController
@RequestMapping("/evwBizLdxregLeaveRegister")
public class EvwBizLdxregLeaveRegisterController  extends BaseController {

	@Autowired
	public IEvwBizLdxregLeaveRegisterService iEvwBizLdxregLeaveRegisterService;

	/**
	 *  根据员工工号和月份查询调休历史记录
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwBizLdxregLeaveRegister> evwBizLdxregLeaveRegisterList = iEvwBizLdxregLeaveRegisterService.list(badge, queryMonth);
		return success(evwBizLdxregLeaveRegisterList, EvwBizLdxregLeaveRegisterRespDTO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/detail")
	public Result getByBadge(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime){
		EvwBizLdxregLeaveRegister evwBizLdxregLeaveRegister = iEvwBizLdxregLeaveRegisterService.getOne(Wrappers.<EvwBizLdxregLeaveRegister>query().lambda()
				.eq(EvwBizLdxregLeaveRegister::getBADGE, badge)
				.eq(EvwBizLdxregLeaveRegister::getBEGINTIME, DateUtil.parse(beginTime)), Boolean.FALSE);
		return success(evwBizLdxregLeaveRegister, EvwBizLdxregLeaveRegisterRespDTO.class);
	}
}
