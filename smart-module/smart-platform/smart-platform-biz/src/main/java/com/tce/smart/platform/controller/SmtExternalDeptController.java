package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.ExternalDeptReqDTO;
import com.tce.smart.platform.api.dto.resp.ExternalDepTree;
import com.tce.smart.platform.api.dto.resp.ExternalDeptRespDTO;
import com.tce.smart.platform.core.entity.SmtExternalDept;
import com.tce.smart.platform.service.SmtExternalDeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Api(tags = "platform-部门管理")
@RequestMapping("/ext/dept")
public class SmtExternalDeptController extends BaseController {

	@Autowired
	private SmtExternalDeptService smtExternalDeptService;


	/**
	 * 分页查询
	 * @param
	 * @return
	 */
	@ApiOperation("获得组织-部门树形结构")
	@GetMapping("/tree")
	public Result getPage() {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		List<ExternalDepTree> list = smtExternalDeptService.getCompTree(parkIds);
		return success(list);
	}

	/**
	 * 获得所有部门
	 * @param
	 * @return
	 */
	@ApiOperation("获得所有部门")
	@GetMapping("/list")
	public Result getParentDept() {
		List<SmtExternalDept> list = smtExternalDeptService.getList();
		return success(list, ExternalDeptRespDTO.class);
	}

	/**
	 * 获得主管
	 * @param
	 * @return
	 */
	@ApiOperation("根据部门id获得部门主管工号+姓名")
	@GetMapping("/director/{id}")
	public Result getDirector(@PathVariable("id") Long id) {
		SmtExternalDept dept = smtExternalDeptService.getById(id);
		return success(dept.getDirector() + "-" + dept.getDirectorName());
	}


	@ApiOperation("根据id获得部门详情")
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Long id){
		return success(smtExternalDeptService.getById(id), ExternalDeptRespDTO.class);
	}


	@SysLog("保存部门")
	@ApiOperation("编辑部门")
	@PostMapping("/save")
	public Result save(@RequestBody ExternalDeptReqDTO externalDeptReqDTO){
		return success(smtExternalDeptService.editDept(externalDeptReqDTO));
	}



	@SysLog("删除部门")
	@ApiOperation("删除部门")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable Long id){
		return success(smtExternalDeptService.deleteDept(id));
	}

}
