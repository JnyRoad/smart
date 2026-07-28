package com.tce.smart.data.controller.ehrview;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.AvaGetskyPayYSHRDTO;
import com.tce.smart.data.api.dto.ehrview.req.AvaGetskyPayYSHRReqDTO;
import com.tce.smart.ehrview.core.entity.AvaGetskyPayYSHR;
import com.tce.smart.ehrview.core.service.AvaGetskyPayYSHRService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/ava/getskypay")
@Slf4j
public class AvaGetskyPayYSHRController  extends BaseController {

	@Autowired
	private AvaGetskyPayYSHRService  avaGetskyPayYSHRService;

	/**
	 * 获取当月得考勤统计
	 * @param badge
	 * @param kqDate
	 * @return
	 * @throws ParseException
	 */
    @Inner
    @OpenApi("server")
    @GetMapping("/info")
    public Result<AvaGetskyPayYSHRDTO> info(@RequestParam("badge") String badge, @RequestParam("kqDate") String kqDate) throws ParseException{

	SimpleDateFormat formatDay = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		AvaGetskyPayYSHR avaGetskyPayYSHR = avaGetskyPayYSHRService.getOne(Wrappers.<AvaGetskyPayYSHR>query().lambda()
                .eq(AvaGetskyPayYSHR::getBadge, badge)
                .eq(AvaGetskyPayYSHR::getTERM, DateUtils.format(formatDay.parse(kqDate), formatDay))
                );
		AvaGetskyPayYSHRDTO avaGetskyPayYSHRDTO = new AvaGetskyPayYSHRDTO();
		BeanUtils.copyProperties(avaGetskyPayYSHR,avaGetskyPayYSHRDTO);
        return success(avaGetskyPayYSHRDTO);
    }

	/**
	 * 获得范围时间内考勤数据
	 * @param startTime
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/list")
    public Result<List<AvaGetskyPayYSHRDTO>> monthList(@RequestParam("startTime") LocalDateTime startTime, @RequestParam("buIds") List<String> buIds) {
		List<AvaGetskyPayYSHR> list = avaGetskyPayYSHRService.list(Wrappers.<AvaGetskyPayYSHR>query()
				.lambda().eq(AvaGetskyPayYSHR::getTERM, Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()))
		.in(AvaGetskyPayYSHR::getCompId, buIds));
		List<AvaGetskyPayYSHRDTO> listDTO = BeanUtils.batchTransform(AvaGetskyPayYSHRDTO.class, list);
		return success(listDTO);
	}

	/**
	 * 根据工号在范围时间内获得数据
	 * @param dto
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/byBadge")
	public Result<List<AvaGetskyPayYSHRDTO>> monthListByBadge(@RequestBody AvaGetskyPayYSHRReqDTO dto) {
		Date date = Date.from(dto.getStartTime().atZone( ZoneId.systemDefault()).toInstant());
		List<AvaGetskyPayYSHR> list = avaGetskyPayYSHRService.list(Wrappers.<AvaGetskyPayYSHR>query()
				.lambda().eq(AvaGetskyPayYSHR::getTERM, date)
		.in(AvaGetskyPayYSHR::getBadge, dto.getBadge()));
		List<AvaGetskyPayYSHRDTO> listDTO = BeanUtils.batchTransform(AvaGetskyPayYSHRDTO.class, list);
		return success(listDTO);
	}
}
