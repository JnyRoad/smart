package com.tce.smart.platform.controller.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.watermeter.*;
import com.tce.smart.platform.api.dto.resp.watermeter.EleMeterRespDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.MeterImportFlushDTO;
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
import java.util.Objects;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:51
 */
@RestController
@RequestMapping("/ele/meter")
@Api(tags = "智能电表")
public class EleMeterController extends BaseController {

	@Autowired
	private SmtEleMeterService meterService;
	@Autowired
	private SmtEleMeterTagService tagService;

	@GetMapping("/page")
	@ApiOperation(value = "分页查询电表")
	public Result<IPage<EleMeterRespDTO>> getMeterPage(Page page, EleMeterQueryDTO dto) {
		return success(meterService.getPage(page, dto), EleMeterRespDTO.class);
	}

	@PostMapping("/save")
	@ApiOperation(value = "添加电表数据")
	public Result<Boolean> addMeter(@RequestBody @Valid EleMeterAddDTO dto) {
		return success(meterService.addMeter(dto));
	}

	@PostMapping("/update")
	@ApiOperation(value = "更新电表数据")
	public Result<Boolean> updateMeter(@RequestBody @Valid EleMeterUpdateDTO dto) {
		return success(meterService.updateMeter(dto, false));
	}

	@PostMapping("/reading")
	@ApiOperation(value = "批量读取电表读数")
	public Result<Boolean> readingConcentrator(@RequestBody EleMeterOperateDTO operate) {
		meterService.getReading(operate);
		return success(true);
	}

	@PostMapping("/re/download")
	@ApiOperation(value = "批量重新下载档案")
	public Result<Boolean> reDownload(@RequestBody EleMeterOperateDTO operate) {
		meterService.reDownload(operate);
		return success(true);
	}

	@PostMapping("/excel/import")
	@ResponseBody
	@ApiOperation(value = "导入电表数据")
	public void excelImport(HttpServletRequest request, HttpServletResponse response, @RequestParam("file") MultipartFile file)
			throws IOException {
		meterService.excelImport(request, response, file.getInputStream());
	}

	@ResponseBody
	@GetMapping("/flushProgress")
	@ApiOperation(value = "刷新导入电表进度")
	public Result<MeterImportFlushDTO> flushProgress(HttpServletRequest request) {
		MeterImportFlushDTO flushDTO = new MeterImportFlushDTO();
		Object maxSize = request.getSession().getAttribute("eleMaxImport");
		Object remainSize = request.getSession().getAttribute("eleRemainImport");
		flushDTO.setMaxSize(Objects.nonNull(maxSize) ? Integer.parseInt(maxSize.toString()) : 0);
		flushDTO.setRemainSize(Objects.nonNull(remainSize) ? Integer.parseInt(remainSize.toString()) : 0);
		return success(flushDTO);
	}

	@PostMapping("/del")
	@ApiOperation(value = "批量删除电表")
	public Result<String> delMeter(@RequestBody EleMeterOperateDTO delList) {
		return success(meterService.remove(delList));
	}

	@GetMapping("/brake/change")
	@ApiOperation(value = "关闭/开启电表闸门")
	public Result<Boolean> changeBrake(@RequestParam("eleMeterId") Long eleMeterId, @RequestParam("status") Integer status) {
		return success(meterService.changeBrake(eleMeterId, status));
	}

	@PostMapping("/brake/batch/change")
	@ApiOperation(value = "批量关闭/开启电表闸门")
	public Result<String> batchChangeBrakeStatus(@RequestBody EleMeterBrakeOperateDTO operate) {
		return success(meterService.batchChangeBrake(operate));
	}

	@PostMapping("/tag/edit")
	@ApiOperation(value = "批量修改电表标签")
	public Result<Boolean> changeTag(@RequestBody @Valid EleMeterTagAddDTO dto) {
		return success(tagService.setMeterTag(dto));
	}

	@PostMapping("/device/change")
	@ApiOperation(value = "更换设备")
	public Result<Boolean> deviceChange(@RequestBody @Valid EleMeterUpdateDTO dto) {
		return success(meterService.updateMeter(dto, true));
	}
}
