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
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizAregotRegisterRespDTO;
import com.tce.smart.ehrview.core.entity.EvwBizAregotRegister;
import com.tce.smart.ehrview.core.service.EvwBizAregotRegisterService;

/**
 * 审批中的加班
 * 控制器
 * @author 齐佩
 *
 */
@RestController
@RequestMapping("/evwBizAregotRegister")
public class EvwBizAregotRegisterController extends BaseController {
	@Autowired
	private EvwBizAregotRegisterService evwBizAregotRegisterService;

   @Inner
   @OpenApi("server")
   @GetMapping("/info")
   public Result<List<EvwBizAregotRegisterRespDTO>> info(@RequestParam("badge") String badge, @RequestParam("otTerm") String otTerm){
	 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		List<EvwBizAregotRegister> list = null;
		try {
			list = evwBizAregotRegisterService.list(Wrappers.<EvwBizAregotRegister>query().lambda()
						.eq(EvwBizAregotRegister::getBADGE, badge)
						.ge(EvwBizAregotRegister::getOTTERM,df.parse(otTerm+" 00:00:00"))
						.lt(EvwBizAregotRegister::getOTTERM, df.parse(otTerm+" 23:59:59")));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return success(list,EvwBizAregotRegisterRespDTO.class);
   }

	@Inner
	@OpenApi("server")
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwBizAregotRegister> evwBizAregotRegisterList = evwBizAregotRegisterService.list(badge, queryMonth);
		return success(evwBizAregotRegisterList, EvwBizAregotRegisterRespDTO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/detail")
	public Result getByBadge(@RequestParam("badge") String badge, @RequestParam("otterm") String otterm){
		EvwBizAregotRegister evwBizAregotRegister = evwBizAregotRegisterService.getOne(Wrappers.<EvwBizAregotRegister>query().lambda()
				.eq(EvwBizAregotRegister::getBADGE, badge)
				.eq(EvwBizAregotRegister::getOTTERM, DateUtil.parse(otterm)), Boolean.FALSE);
		return success(evwBizAregotRegister, EvwBizAregotRegisterRespDTO.class);
	}
}
