package com.tce.smart.platform.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.OrganizeRelationReqDTO;
import com.tce.smart.platform.api.dto.resp.OrganizeRelationListRespDTO;
import com.tce.smart.platform.api.dto.resp.OrganizeRelationRespDTO;
import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import com.tce.smart.platform.service.SmtOrganizeRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/org/relation")
@Api(tags = "platform-组织管理")
public class SmtOrganizeRelationController extends BaseController {

	@Autowired
	private SmtOrganizeRelationService smtOrganizeRelationService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperation("分页查询")
	public Result getPage(Page page, @RequestParam(value = "buName", required = false) String buName) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		IPage<SmtOrganizeRelation> list = smtOrganizeRelationService.page(page, Wrappers.<SmtOrganizeRelation>query().lambda()
				.in(SmtOrganizeRelation::getParkId, parkIds)
				.like(StrUtil.isNotEmpty(buName), SmtOrganizeRelation::getCompName, buName)
				.orderByDesc(SmtOrganizeRelation::getCreateTime));
		return success(list, OrganizeRelationListRespDTO.class);
	}

	@GetMapping("/{id}")
	@ApiOperation("根据id获得组织")
	public Result getById(@PathVariable("id") Long id){
		return success(smtOrganizeRelationService.getById(id), OrganizeRelationRespDTO.class);
	}


	@SysLog("新增组织")
	@ApiOperation("新增组织")
	@PostMapping("/save")
	public Result save(@RequestBody OrganizeRelationReqDTO relationReqDTO){
		return success(smtOrganizeRelationService.saveBu(relationReqDTO));
	}



	@SysLog("修改组织")
	@ApiOperation("修改组织")
	@PostMapping("/update")
	public Result updateById(@RequestBody OrganizeRelationReqDTO relationReqDTO){
		return success(smtOrganizeRelationService.updateBu(relationReqDTO));
	}



	@SysLog("删除组织")
	@ApiOperation("删除组织")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable Long id){
		return success(smtOrganizeRelationService.deleteBu(id));
	}


}
