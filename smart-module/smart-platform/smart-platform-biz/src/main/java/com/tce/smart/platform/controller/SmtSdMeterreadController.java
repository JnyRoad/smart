package com.tce.smart.platform.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.StatementDetailDTO;
import com.tce.smart.platform.api.dto.req.DormitoryMeterQueryDTO;
import com.tce.smart.platform.api.dto.req.RoomMeterQueryDTO;
import com.tce.smart.platform.api.dto.req.SmtSdMeterreadReqDTO;
import com.tce.smart.platform.api.dto.req.sddto.RoomSDMeterreadReqDTO;
import com.tce.smart.platform.api.dto.req.sddto.SdMeterreadDetailReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.DormitorySDMeterreadNewRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.DormitorySDMeterreadRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.RoomSDMeterreadRespDTO;
import com.tce.smart.platform.core.dto.SmtSdMeterreadDTO;
import com.tce.smart.platform.core.vo.SmtSdMeterreadVO;
import com.tce.smart.platform.service.settlement.SmtCommonSDMeterreadService;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 房间水电抄表管理
 * @date: 2020-07-10 9:57
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "房间水电抄表管理")
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/meterread")
public class SmtSdMeterreadController {

	private final SmtSdMeterreadService smtSdMeterreadService;

	private final SmtCommonSDMeterreadService smtCommonSDMeterreadService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param smtSdMeterreadReqDTO
	 * @return
	 */
	@ApiOperation("分页查询房间水电抄表数据")
	@GetMapping("/page")
	public Result<IPage<SmtSdMeterreadVO>> getSDMeterreadPage(Page page, SmtSdMeterreadReqDTO smtSdMeterreadReqDTO) {
		return new Result<>(smtSdMeterreadService.getSDMeterreadPage(page, smtSdMeterreadReqDTO));
	}

	/**
	 * 查询房间水电抄表数据
	 * @param smtSdMeterreadReqDTO
	 * @return
	 */
	@ApiOperation("查询房间水电抄表数据")
	@GetMapping("/info-list")
	public Result<List<SmtSdMeterreadVO>> getRoomSDMeterreadInfo(SmtSdMeterreadReqDTO smtSdMeterreadReqDTO) {
		return new Result<>(smtSdMeterreadService.getRoomSDMeterreadInfo(smtSdMeterreadReqDTO));
	}

	/**
	 * 查询房间水电抄表数据
	 * @return
	 */
	@ApiOperation("查询房间水电抄表数据")
	@GetMapping("/{roomId}")
	public Result<DormitorySDMeterreadRespDTO> getRoomSDMeterread(@ApiParam(name = "roomId",value = "房间ID",required = true) @PathVariable Integer roomId,
																  @ApiParam(name = "meterMonth",value = "抄表月份",required = true) @DateTimeFormat(pattern = "yyyy-MM") @RequestParam Date meterMonth){
		return new Result<>(smtSdMeterreadService.getRoomSDMeterread(roomId,meterMonth,smtCommonSDMeterreadService));
	}

	/**
	 * 查询一个楼层的房间的水电抄表数据
	 * @return
	 */
	@ApiOperation("按楼层查询所有房间的水电抄表数据")
	@GetMapping("/byFloor/{floorId}")
	public Result<List<DormitorySDMeterreadRespDTO>> getFloorSDMeterread(@ApiParam(name = "floorId",value = "楼层ID",required = true) @PathVariable Integer floorId,
																		 @ApiParam(name = "meterMonth",value = "抄表月份",required = true) @DateTimeFormat(pattern = "yyyy-MM") @RequestParam Date meterMonth){
		return new Result<>(smtSdMeterreadService.getFloorSDMeterread(floorId,meterMonth));
	}

	/**
	 * 查询一个楼层的房间的水电抄表数据
	 * @return
	 */
	@ApiOperation("按楼层查询所有房间的水电抄表数据-新接口")
	@GetMapping("/byFloor/new")
	public Result<List<DormitorySDMeterreadNewRespDTO>> getFloorSDMeterreadNew(RoomMeterQueryDTO roomMeterQueryDTO){
		return new Result<>(smtSdMeterreadService.getFloorSDMeterreadNew(roomMeterQueryDTO));
	}

	/**
	 * 查询多个楼栋的水电抄表数据
	 * @return
	 */
	@ApiOperation("查询多个楼栋的水电抄表数据")
	@GetMapping("/byDormitory")
	public Result<List<DormitorySDMeterreadNewRespDTO>> getDormitorySDMeterread(DormitoryMeterQueryDTO dormitoryMeterQueryDTO){
		return new Result<>(smtSdMeterreadService.getDormitorySDMeterread(dormitoryMeterQueryDTO));
	}

	/**
	 * 快捷抄电表
	 * @return
	 */
	@ApiOperation("快捷抄电表")
	@PostMapping("/batch")
	public Result<Boolean> saveBatchSDMeterread(@RequestBody List<SdMeterreadDetailReqDTO> detailReqDTOS){
		return new Result<>(smtSdMeterreadService.saveBatchSDMeterread(detailReqDTOS));
	}

	/**
	 * 分页查询已结算的房间数据抄表
	 * @param page 分页对象
	 * @param smtSdMeterreadReqDTO 已结算房间数据
	 * @return
	 */
	@GetMapping("/statmentpage")
	public Result getSDStatementPage(Page page, SmtSdMeterreadReqDTO smtSdMeterreadReqDTO) {
		smtSdMeterreadReqDTO.setIsStatement(true);
		return new Result<>(smtSdMeterreadService.getSDMeterreadPage(page, smtSdMeterreadReqDTO));
	}

	/**
	 * 添加房间水电抄表记录
	 * @param smtSdMeterreadDTO 水电模板记录
	 * @return Result
	 */
	@PostMapping("/add")
	public Result save(@RequestBody SmtSdMeterreadDTO smtSdMeterreadDTO) {
		return new Result<>(smtSdMeterreadService.addSDMeterreadRecord(smtSdMeterreadDTO));
	}

	/**
	 * 生成结算数据
	 * @return Result
	 */
	@ApiOperation("生成结算数据")
	@PostMapping("/generateStatement")
	public Result<String> generateStatement(@RequestParam(value = "dormitoryId")Integer dormitoryId) {
		return new Result<>(smtSdMeterreadService.generateSDStatementDetail(dormitoryId));
	}

	/**
	 * 查看房间水电结算明细
	 * @param mrId 抄表记录Id
	 * @return Result
	 */
	@ApiOperation("查看房间水电结算明细")
	@GetMapping("/statement/{mrId}")
	public Result<StatementDetailDTO> getRoomStatementDetail(@ApiParam(name = "mrId",value = "房间抄表记录Id",required = true)@PathVariable("mrId") Long mrId){
		Assert.notNull(mrId,"抄表记录Id不能为NULL");
		return new Result<>(smtSdMeterreadService.queryRoomStatementDetail(mrId));
	}

	/**
	 * 查看房间列表水电抄表状态
	 * @param roomSDMeterreadReqDTO
	 * @return Result
	 */
	@ApiOperation("查看房间列表水电抄表状态")
	@PostMapping("/room/status")
	public Result<RoomSDMeterreadRespDTO> queryRoomMeterStatus(@RequestBody RoomSDMeterreadReqDTO roomSDMeterreadReqDTO){
		return new Result<>(smtSdMeterreadService.queryRoomMeterStatus(roomSDMeterreadReqDTO));
	}

	@GetMapping("/get-template")
	@ApiOperation(value = "获取水电导入模板")
	public ResponseEntity<byte[]> getSdImportTemplate(@RequestParam("meterMonth") String meterMonth,@RequestParam(value = "dormitoryIds")String dormitoryIds) {
		List<Integer> dormitoryIdList = Arrays.asList(dormitoryIds.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
		return smtSdMeterreadService.getSdImportTemplate(meterMonth,dormitoryIdList);
	}

	@PostMapping("/import")
	@ApiOperation(value = "导入水电模板")
	public ResponseEntity<byte[]> importDormitorySd(@RequestParam("meterMonth") String meterMonth,
													@RequestParam("multipartFile") MultipartFile multipartFile, HttpServletResponse httpServletResponse) {
		return smtSdMeterreadService.importDormitorySd(meterMonth,multipartFile,httpServletResponse);
	}
}
