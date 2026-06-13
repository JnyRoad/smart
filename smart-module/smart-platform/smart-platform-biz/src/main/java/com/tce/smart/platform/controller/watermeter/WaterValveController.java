package com.tce.smart.platform.controller.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterValveAddDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterValveQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterValveUpdateDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.WaterMeterValveRespDTO;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterValveService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Li.JiaJun
 * @since 2022/5/10 17:35
 */
@RestController
@RequestMapping("/water/meter/valve")
@Api(tags = "智能水表外置阀门")
public class WaterValveController extends BaseController {

	@Autowired
	private SmtWaterMeterValveService valveService;

	@GetMapping("/out/status")
	@ApiOperation(value = "关闭/开启外置阀门开关状态")
	public Result<Boolean> changeOutValveStatus(@RequestParam("valveId") String valveId, @RequestParam("status") Integer status) {
		return success(valveService.changeValveStatus(Long.parseLong(valveId), status));
	}

	@GetMapping("/out/remote-status")
	@ApiOperation(value = "关闭/开启外置阀门远程状态")
	public Result<Boolean> changeOutValveRemoteStatus(@RequestParam("valveId") String valveId, @RequestParam("status") Integer status) {
		return success(valveService.changeValveRemoteStatus(Long.parseLong(valveId), status));
	}

	@GetMapping("/page")
	@ApiOperation(value = "分页查询水表外置阀门")
	public Result<IPage<WaterMeterValveRespDTO>> getValvePage(Page page, WaterMeterValveQueryDTO dto) {
		return success(valveService.getPage(page, dto), WaterMeterValveRespDTO.class);
	}

	@PostMapping("/save")
	@ApiOperation(value = "保存水表外置阀门")
	public Result<Boolean> saveValve(@RequestBody @Valid WaterMeterValveAddDTO dto) {
		return success(valveService.saveValve(dto));
	}

	@PostMapping("/edit")
	@ApiOperation(value = "编辑水表外置阀门")
	public Result<Boolean> editValve(@RequestBody @Valid WaterMeterValveUpdateDTO dto) {
		return success(valveService.updateValve(dto));
	}

	@PostMapping("/del/{valveId}")
	@ApiOperation(value = "删除水表外置阀门")
	public Result<Boolean> delValve(@PathVariable("valveId") Long valveId) {
		return success(valveService.removeById(valveId));
	}
}
