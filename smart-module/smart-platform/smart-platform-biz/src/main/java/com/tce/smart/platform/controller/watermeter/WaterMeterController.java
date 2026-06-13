package com.tce.smart.platform.controller.watermeter;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.watermeter.*;
import com.tce.smart.platform.api.dto.resp.watermeter.MeterImportFlushDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.WaterMeterRespDTO;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterTagService;
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
@RequestMapping("/water/meter")
@Api(tags = "智能水表")
public class WaterMeterController extends BaseController {

	@Autowired
	private SmtWaterMeterService waterMeterService;
	@Autowired
	private SmtWaterMeterTagService tagService;

	@GetMapping("/page")
	@ApiOperation(value = "分页查询水表")
	public Result<IPage<WaterMeterRespDTO>> getMeterPage(Page page, WaterMeterQueryDTO dto,
														 @RequestParam(value = "areaIdArray[]", required = false) Integer[] areaIdArray) {
		if (ArrayUtil.isNotEmpty(areaIdArray)) {
			switch (areaIdArray.length) {
				case 1:
					dto.setParkId(areaIdArray[0]);
					break;
				case 2:
					dto.setParkId(areaIdArray[0]);
					dto.setDormitoryId(areaIdArray[1]);
					break;
				case 3:
					dto.setParkId(areaIdArray[0]);
					dto.setDormitoryId(areaIdArray[1]);
					dto.setRoomId(areaIdArray[2]);
					break;
				default:
					break;
			}
		}
		return success(waterMeterService.getPage(page, dto), WaterMeterRespDTO.class);
	}

	@PostMapping("/save")
	@ApiOperation(value = "添加水表数据")
	public Result<Boolean> addMeter(@RequestBody @Valid WaterMeterAddDTO dto) {
		return success(waterMeterService.addMeter(dto));
	}

	@PostMapping("/update")
	@ApiOperation(value = "更新水表数据")
	public Result<Boolean> updateMeter(@RequestBody @Valid WaterMeterUpdateDTO dto) {
		return success(waterMeterService.updateMeter(dto, false));
	}

	@PostMapping("/re/download")
	@ApiOperation(value = "批量重新下载档案")
	public Result<Boolean> reDownload(@RequestBody WaterMeterOperateDTO operate) {
		waterMeterService.reDownload(operate);
		return success(true);
	}

	@PostMapping("/reading")
	@ApiOperation(value = "批量读取水表读数")
	public Result<Boolean> readingConcentrator(@RequestBody WaterMeterOperateDTO operate) {
		waterMeterService.getReading(operate);
		return success(true);
	}

	@PostMapping("/excel/import")
	@ResponseBody
	@ApiOperation(value = "导入水表数据")
	public void excelImport(HttpServletRequest request, HttpServletResponse response, @RequestParam("file") MultipartFile file)
			throws IOException {
		waterMeterService.excelImport(request, response, file.getInputStream());
	}

	@ResponseBody
	@GetMapping("/flushProgress")
	@ApiOperation(value = "刷新导入水表进度")
	public Result<MeterImportFlushDTO> flushProgress(HttpServletRequest request) {
		MeterImportFlushDTO flushDTO = new MeterImportFlushDTO();
		Object maxSize = request.getSession().getAttribute("waterMaxImport");
		Object remainSize = request.getSession().getAttribute("waterRemainImport");
		flushDTO.setMaxSize(Objects.nonNull(maxSize) ? Integer.parseInt(maxSize.toString()) : 0);
		flushDTO.setRemainSize(Objects.nonNull(remainSize) ? Integer.parseInt(remainSize.toString()) : 0);
		return success(flushDTO);
	}

	@PostMapping("/del")
	@ApiOperation(value = "批量删除水表")
	public Result<String> delMeter(@RequestBody WaterMeterOperateDTO delList) {
		return success(waterMeterService.remove(delList));
	}

	@GetMapping("/valve/in/status")
	@ApiOperation(value = "关闭/开启内置阀门")
	public Result<Boolean> changeInValveStatus(@RequestParam("waterMeterId") Long waterMeterId, @RequestParam("status") Integer status) {
		return success(waterMeterService.changeValveStatus(waterMeterId, status));
	}

	@PostMapping("/valve/in/batch/status")
	@ApiOperation(value = "批量关闭/开启内置阀门")
	public Result<String> batchChangeInValveStatus(@RequestBody WaterMeterValveOperateDTO operate) {
		return success(waterMeterService.batchChangeValveStatus(operate));
	}

	@PostMapping("/tag/edit")
	@ApiOperation(value = "批量修改水表标签")
	public Result<Boolean> changeTag(@RequestBody @Valid WaterMeterTagAddDTO dto) {
		return success(tagService.setMeterTag(dto));
	}

	@PostMapping("/device/change")
	@ApiOperation(value = "更换设备")
	public Result<Boolean> deviceChange(@RequestBody @Valid WaterMeterUpdateDTO dto) {
		return success(waterMeterService.updateMeter(dto, true));
	}
}
