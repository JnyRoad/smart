package com.tce.smart.data.controller.ehrview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import cn.hutool.core.date.DateUtil;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizLcardlostRespDTO;
import com.tce.smart.ehrview.core.entity.EvwBizLcardlost;
import com.tce.smart.ehrview.core.service.EvwBizLcardlostService;


/**
 * 补卡记录
 * @author 齐佩
 *
 */
@Slf4j
@RestController
@RequestMapping("/evwBizLcardlost")
public class EvwBizLcardlostController extends BaseController {
	@Autowired
	private EvwBizLcardlostService evwBizLcardlostService;

	@Inner
	@OpenApi("server")
	@GetMapping("/info")
	public Result<List<EvwBizLcardlostRespDTO>> info(@RequestParam("badge") String badge, @RequestParam("kqStartDate") String kqStartDate){

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		List<EvwBizLcardlost> list = null;
		try {
			list = evwBizLcardlostService.list(Wrappers.<EvwBizLcardlost>query().lambda()
						.eq(EvwBizLcardlost::getBADGE, badge)
						.eq(EvwBizLcardlost::getKQSTARTDATE, df.parse(kqStartDate+" 00:00:00")));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("lost_card_data:{}", list);
      return success(list,EvwBizLcardlostRespDTO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		List<EvwBizLcardlost> evwBizLcardlostList = evwBizLcardlostService.list(badge, queryMonth);
		return success(evwBizLcardlostList, EvwBizLcardlostRespDTO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/detail")
	public Result getByBadge(@RequestParam("badge") String badge, @RequestParam("kqStartDate") String kqStartDate){
		EvwBizLcardlost evwBizLcardlost = evwBizLcardlostService.getOne(Wrappers.<EvwBizLcardlost>query().lambda()
				.eq(EvwBizLcardlost::getBADGE, badge)
				.eq(EvwBizLcardlost::getKQSTARTDATE, DateUtil.parse(kqStartDate)), Boolean.FALSE);
		return success(evwBizLcardlost, EvwBizLcardlostRespDTO.class);
	}
}
