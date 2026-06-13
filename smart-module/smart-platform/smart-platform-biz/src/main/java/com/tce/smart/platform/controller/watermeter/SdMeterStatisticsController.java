package com.tce.smart.platform.controller.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.watermeter.*;
import com.tce.smart.platform.api.dto.resp.watermeter.EleMeterRespDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.MeterImportFlushDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.SdMeterStatisticsRespDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.SdUseStatisticsRespDTO;
import com.tce.smart.platform.service.watermeter.SmtEleMeterService;
import com.tce.smart.platform.service.watermeter.SmtEleMeterTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/sd/statistics")
@Api(tags = "智能水电表数据统计")
public class SdMeterStatisticsController extends BaseController {

	@Autowired
	private SmtEleMeterService meterService;

	@GetMapping("/page")
	@ApiOperation(value = "分页查询水电抄表统计")
	public Result<IPage<SdMeterStatisticsRespDTO>> getMeterStatisticsPage(Page page, SdMeterStatisticsQueryDTO dto) {
		return success(meterService.getMeterStatisticsPage(page, dto));
	}

	@GetMapping("/use-page")
	@ApiOperation(value = "分页查询水电用量统计")
	public Result<IPage<SdUseStatisticsRespDTO>> getUseStatisticsPage(Page page, SdUseStatisticsQueryDTO dto,
																	  @RequestParam(value = "deviceIds[]",required=false) Long[] deviceIds,
																	  @RequestParam(value = "deviceTagList[]",required=false) Long[] deviceTagList) {
		return success(meterService.getUseStatisticsPage(page, dto,deviceIds,deviceTagList));
	}

	@GetMapping("/list")
	@ApiOperation(value = "查询水电抄表统计")
	public Result<List<SdMeterStatisticsRespDTO>> getMeterStatisticsList(SdMeterStatisticsQueryDTO dto) {
		return success(meterService.getMeterStatisticsList(dto));
	}

	@GetMapping("/use-list")
	@ApiOperation(value = "查询水电用量统计")
	public Result<List<SdUseStatisticsRespDTO>> getUseStatisticsList(SdUseStatisticsQueryDTO dto,
																	 @RequestParam(value = "deviceIds[]",required=false) Long[] deviceIds,
																	 @RequestParam(value = "deviceTagList[]",required=false) Long[] deviceTagList) {
		return success(meterService.getUseStatisticsList(dto,deviceIds,deviceTagList));
	}
}
