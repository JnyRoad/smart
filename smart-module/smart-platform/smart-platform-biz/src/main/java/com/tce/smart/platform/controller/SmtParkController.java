package com.tce.smart.platform.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryRoomReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.SearchDormitoryRoomDetailReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtParkRespDTO;
import com.tce.smart.platform.core.dto.DormitoryTreeDTO;
import com.tce.smart.platform.core.dto.meter.DormitoryLazyQueryDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.service.SmtDormitoryBedService;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import com.tce.smart.platform.service.SmtParkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 园区表
 *
 * @author 齐佩
 * @date 2019-04-13 13:48:12
 */
@Api(tags = "园区管理")
@RestController
@AllArgsConstructor
@RequestMapping("/park")
public class SmtParkController extends BaseController{
	private final SmtParkService smtParkService;
	private final SmtDormitoryBedService bedService;
	private final SmtDormitoryStaffService dormitoryStaffService;



	/**
	 * 分页查询
	 *
	 * @param page
	 *            分页对象
	 * @param smtPark
	 *            园区表
	 * @return
	 */

	@GetMapping("/page")
	public Result getParkPage(Page page, SmtPark smtPark) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return new Result<>(smtParkService.page(page, Wrappers.query(smtPark)
				.lambda().in(SmtPark::getId, parkIds)));
	}

	/**
	 * 通过id查询园区表
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getParkById(@PathVariable("id") Integer id) {
		return new Result<>(smtParkService.getById(id));
	}

	@SysLog("删除园区表")
	@GetMapping("/delete/{id}")
	public Result deleteById(@PathVariable Integer id) {
		return smtParkService.removeParkById(id);
	}

	/**
	 * 新增园区表
	 *
	 * @param smtPark
	 *            园区表
	 * @return Result
	 */
	@SysLog("新增园区表")
	@PostMapping("/addPark")
	public Result save(@RequestBody SmtPark smtPark) {
		return smtParkService.addPark(smtPark);
	}

	/**
	 * 展示所有的园区
	 * @return
	 */
	@ApiOperation("查询宿管账号所有园区")
	@GetMapping("/dormitory/all")
	public Result<List<SmtParkRespDTO>> getDormitoryParks() {
		return success(smtParkService.getDormitoryParks(),SmtParkRespDTO.class);
	}

	/**
	 * 展示所有的园区
	 * @return
	 */
	@ApiOperation("查询所有园区")
	@GetMapping("/all")
	public Result<List<SmtParkRespDTO>> getParks() {
		return success(smtParkService.getParkList(),SmtParkRespDTO.class);
	}

	/**
	 * 园区-宿舍结构树
	 * @return
	 */
	@ApiOperation("查询所有园区和楼栋的结构树")
	@PostMapping("/allList")
	public Result allList( ) {
		return smtParkService.allList();
	}

	@ApiOperation("查询账号对应园区的楼栋结构树(懒加载方式)")
	@PostMapping("/lazy/park")
	public Result lazyPark(@RequestBody DormitoryLazyQueryDTO queryDTO) {
		return smtParkService.lazyPark(queryDTO);
	}

	/**
	 * 获取宿舍树形结构 不保护房间
	 * @return
	 */
	@ApiOperation("查询所有园区和楼栋的结构树-不包含房间")
	@GetMapping("/dormTreeNonRoom")
	public Result<List<DormitoryTreeDTO>> getDormitoryTreeNonRoom(){
		return new Result<>(smtParkService.getDormitoryTreeNonRoom());
	}

	/**
	 * 根据条件 查询园区-宿舍结构树
	 * @return
	 */
	@ApiOperation("根据条件 查询园区-宿舍结构树")
	@PostMapping("/dormRoomTree")
	public Result<List<DormitoryTreeDTO>> dormRoomTree(@RequestBody SearchDormitoryRoomDetailReqDTO roomDetailReqDTO) {
		return new Result<>(smtParkService.dormRoomTree(roomDetailReqDTO));
	}


	/**
	 * 根据条件 查询宿舍结构树
	 * @return
	 */
	@ApiOperation("根据条件 查询宿舍结构树")
	@GetMapping("/roomTree")
	public Result<List<DormitoryTreeDTO>> roomTree(@RequestParam("parkId") Integer parkId) {
		return new Result<>(smtParkService.roomTree(parkId));
	}


	/**
	 * 根据房间ID 查询园区-宿舍结构树
	 * @return
	 */
	@ApiOperation("根据房间ID 查询园区-宿舍结构树")
	@GetMapping("/dormRoomTree/byRoomId/{roomId}")
	public Result<List<DormitoryTreeDTO>> dormRoomTreeByRoomId(@ApiParam(name = "roomId",value = "房间ID",required = true) @PathVariable Integer roomId) {
		return new Result<>(smtParkService.dormRoomTreeByRoomId(roomId));
	}

	/**
	 * 根据内宿申请Id 查询园区-宿舍结构树
	 * @return
	 */
	@ApiOperation("根据内宿申请Id 查询园区-宿舍结构树")
	@GetMapping("/dormRoomTree/byApplyId/{applyId}")
	public Result<List<DormitoryTreeDTO>> dormRoomTreeByApplyId(@ApiParam(name = "applyId",value = "内宿申请Id",required = true) @PathVariable Long applyId) {
		return new Result<>(smtParkService.dormRoomTreeByApplyId(applyId));
	}

	/**
	 * 根据条件查询房间树行列表
	 * @param dormitoryRoomReqDTO
	 * @return
	 */
	@ApiOperation("根据条件查询房间列表")
	@GetMapping("/tree/condition")
	public Result<List<DormitoryTreeDTO>> getRoomTreeByCondition(DormitoryRoomReqDTO dormitoryRoomReqDTO) {
		return new Result<>(smtParkService.getRoomTreeByCondition(dormitoryRoomReqDTO));
	}


	/**
	 * 园区-宿舍结构树-入住情况
	 * @return
	 */
	@PostMapping("/dormitory/allList")
	public Result dormitoryAllList( ) {
		return smtParkService.dormitoryAllList(dormitoryStaffService);
	}


	/**
	 * 修改园区表
	 *
	 * @param smtPark
	 *            园区表
	 * @return Result
	 */
	@SysLog("修改园区表")
	@PostMapping("/updatePark")
	public Result updateById(@RequestBody SmtPark smtPark) {
		return smtParkService.updateParkById(smtPark);
	}

	/**
	 * 获取园区宿舍信息
	 * @return
	 */
	@GetMapping("/statistics/{parkId}")
	public Result statistics(@PathVariable("parkId") Integer parkId) {
		return smtParkService.statistics(bedService,dormitoryStaffService,parkId);
	}

	@SysLog("app通过id查询园区")
	@Inner
	@GetMapping("/app/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return success(smtParkService.getById(id));
	}

	@SysLog("app接口获取所有园区")
	@Inner
	@GetMapping("/app/all")
	public Result<List<SmtParkRespDTO>> getParkList() {
		return success(smtParkService.getUnStrainedParks(),SmtParkRespDTO.class);
	}

	@SysLog("app接口获取园区分页列表")
	@Inner
	@GetMapping("/app/page")
	public Result<IPage<SmtParkRespDTO>> getSmtParkPage(Page page, SmtPark smtPark) {
		IPage<SmtPark> pageRe= smtParkService.page(page, Wrappers.query(smtPark));
		return success(pageRe, SmtParkRespDTO.class);
	}

	/**
	 * 定位园区
	 *
	 * @param smtPark smtPark
	 *
	 * @return Result
	 */
	@SysLog("app接口定位园区")
	@Inner
	@PostMapping("/location")
	public Result<SmtParkDTO> locationPark(@RequestBody SmtPark smtPark) {
		SmtPark smtParkRs = smtParkService.locationPark(smtPark);
		SmtParkDTO smtParkDTO = new SmtParkDTO();
		if(null != smtParkRs) {
			BeanUtils.copyProperties(smtParkRs, smtParkDTO);
		}
		return success(smtParkDTO);
	}

}
