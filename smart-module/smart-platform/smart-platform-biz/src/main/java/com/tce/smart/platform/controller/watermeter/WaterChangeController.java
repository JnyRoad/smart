package com.tce.smart.platform.controller.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterChangeQueryDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.WaterMeterChangeRespDTO;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterChangeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Li.JiaJun
 * @since 2022/5/12 11:01
 */
@RestController
@RequestMapping("/water/meter/change")
@Api(tags = "水表更换记录")
public class WaterChangeController extends BaseController {

	@Autowired
	private SmtWaterMeterChangeService changeService;

	@GetMapping("/page")
	@ApiOperation(value = "分页查询电表更换记录")
	public Result<IPage<WaterMeterChangeRespDTO>> getConcentratorPage(Page page, WaterMeterChangeQueryDTO dto) {
		return success(changeService.getPage(page, dto), WaterMeterChangeRespDTO.class);
	}
}
