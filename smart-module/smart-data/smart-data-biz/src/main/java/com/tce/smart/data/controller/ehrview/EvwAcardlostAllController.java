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
import com.tce.smart.data.api.dto.ehrview.resp.EvwAcardlostAllRespDTO;
import com.tce.smart.ehrview.core.entity.EvwAcardlostAll;
import com.tce.smart.ehrview.core.service.EvwAcardlostAllService;


/**
 * 补卡历史记录
 * @author 齐佩
 *
 */
@RestController
@RequestMapping("/evwAcardlostAll")
public class EvwAcardlostAllController extends BaseController {
	@Autowired
	private EvwAcardlostAllService evwAcardlostAllService;

	@GetMapping("/info")
	public Result<List<EvwAcardlostAllRespDTO>> info(@RequestParam("badge") String badge, @RequestParam("kqStartDate") String kqStartDate){

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		List<EvwAcardlostAll> list = null;
		try {
			list = evwAcardlostAllService.list(Wrappers.<EvwAcardlostAll>query().lambda()
						.eq(EvwAcardlostAll::getBADGE, badge)
						.eq(EvwAcardlostAll::getKQSTARTDATE, df.parse(kqStartDate+" 00:00:00")));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

      return success(list,EvwAcardlostAllRespDTO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwAcardlostAll> evwAcardlostAllList = evwAcardlostAllService.list(badge, queryMonth);
		return success(evwAcardlostAllList, EvwAcardlostAllRespDTO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/detail")
	public Result getByBadge(@RequestParam("badge") String badge, @RequestParam("kqStartDate") String kqStartDate){
		EvwAcardlostAll evwAcardlostAll = evwAcardlostAllService.getOne(Wrappers.<EvwAcardlostAll>query().lambda()
				.eq(EvwAcardlostAll::getBADGE, badge)
				.eq(EvwAcardlostAll::getKQSTARTDATE, DateUtil.parse(kqStartDate)), Boolean.FALSE);
		return success(evwAcardlostAll, EvwAcardlostAllRespDTO.class);
	}
}
