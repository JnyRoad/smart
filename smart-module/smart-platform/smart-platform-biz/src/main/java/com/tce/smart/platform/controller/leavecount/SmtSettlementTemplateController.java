package com.tce.smart.platform.controller.leavecount;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateEditReqDTO;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateRangeReqDTO;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateReqDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementTemplateItemRespDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementTemplateRangeRespDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementTemplateRangeTreeRespDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementTemplateRespDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplate;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateItem;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateRange;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateItemService;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateRangeService;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author fushiping
 * @date 2022-06-21 11:01:56
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-离职水电结算模板")
@RequestMapping("/settlement/template")
public class SmtSettlementTemplateController extends BaseController {

	private final SmtSettlementTemplateService smtSettlementTemplateService;

	private final SmtSettlementTemplateItemService smtSettlementTemplateItemService;

	private final SmtSettlementTemplateRangeService smtSettlementTemplateRangeService;

	@ApiOperation("分页查询")
	@GetMapping("/page/{parkId}")
	public Result<IPage<SettlementTemplateRespDTO>> getSmtSettlementTemplatePage(Page page, @PathVariable("parkId") Integer parkId) {
		return success(smtSettlementTemplateService.page(page, Wrappers.<SmtSettlementTemplate>query().lambda().eq(SmtSettlementTemplate::getParkId, parkId)), SettlementTemplateRespDTO.class);
	}

	@ApiOperation("根据模板id获得水电扣费金额")
	@GetMapping("/item/{id}")
	public Result<List<SettlementTemplateItemRespDTO>> getItem(@PathVariable("id") String id) {
		return success(smtSettlementTemplateItemService.list(Wrappers.<SmtSettlementTemplateItem>lambdaQuery()
				.eq(SmtSettlementTemplateItem::getTempId, Long.parseLong(id))), SettlementTemplateItemRespDTO.class);
	}

	@ApiOperation("水电扣费项目添加/编辑")
	@PostMapping("/save/item")
	public Result<Boolean> saveItem(@RequestBody @Valid SettlementTemplateEditReqDTO smtSettlementTemplate) {
		return success(smtSettlementTemplateItemService.editItem(smtSettlementTemplate));
	}

	@ApiOperation("水电扣费金额删除")
	@PostMapping("/remove/item/{itemId}")
	public Result<Boolean> removeItem(@PathVariable("itemId") Long itemId) {
		return success(smtSettlementTemplateItemService.removeItem(itemId));
	}

	@ApiOperation("编辑模板")
	@PostMapping("/edit/template")
	public Result editTemp(@RequestBody SettlementTemplateReqDTO smtSettlementTemplate) {
		return success(smtSettlementTemplateService.editTemp(smtSettlementTemplate));
	}

	@ApiOperation("根据模板id获得适用范围")
	@GetMapping("/range/{id}/{type}")
	public Result<List<SettlementTemplateRangeRespDTO>> getRange(@PathVariable("id") String id, @PathVariable("type") String type) {
		return success(smtSettlementTemplateRangeService.list(Wrappers.<SmtSettlementTemplateRange>lambdaQuery()
				.eq(SmtSettlementTemplateRange::getTempId, Long.parseLong(id))
				.eq(SmtSettlementTemplateRange::getType, type)), SettlementTemplateRangeRespDTO.class);
	}

	@ApiOperation("根据园区模板id获取房间树")
	@GetMapping("/range/room/tree")
	public Result<List<SettlementTemplateRangeTreeRespDTO>> getRangeRoomTree(
			@RequestParam("parkId") Integer parkId, @RequestParam("tempId") String tempId,
			@RequestParam("type") String type) {
		return success(smtSettlementTemplateRangeService.getRangeTree(parkId, Long.parseLong(tempId), type));
	}

	@ApiOperation("根据房间id获得适用范围")
	@GetMapping("/range/byRoom/{roomId}")
	public Result<SettlementTemplateRangeRespDTO> getRange(@PathVariable("roomId") String roomId) {
		return success(smtSettlementTemplateRangeService.getOne(Wrappers.<SmtSettlementTemplateRange>lambdaQuery()
				.eq(SmtSettlementTemplateRange::getValue, roomId)), SettlementTemplateRangeRespDTO.class);
	}

	@ApiOperation("适用bu/宿舍(批量修改)")
	@PostMapping("/edit/range")
	public Result updateById(@RequestBody List<SettlementTemplateRangeReqDTO> smtSettlementTemplate) {
		return success(smtSettlementTemplateRangeService.editRangeBatch(smtSettlementTemplate));
	}

	@ApiOperation("适用宿舍(单个宿舍修改)")
	@PostMapping("/edit/range/room")
	public Result updateById(@RequestBody SettlementTemplateRangeReqDTO smtSettlementTemplate) {
		return success(smtSettlementTemplateRangeService.editRangeSingle(smtSettlementTemplate));
	}

	@SysLog("删除")
	@ApiOperation("删除")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable BigDecimal id) {
		return success(smtSettlementTemplateService.removeById(id));
	}

}
