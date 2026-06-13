package com.tce.smart.platform.controller.securityzone;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityPersonAddReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityPersonExcelAddReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityPersonQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.AllStaffListRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityPersonRelationRespDTO;
import com.tce.smart.platform.service.securityzone.SmtSecurityPersonRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 *保密区人员关联
 * @author fushiping
 * @date 2021-07-29 11:13:00
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-保密区签署管理关联")
@RequestMapping("/security/person")
public class SmtSecurityPersonRelationController extends BaseController {

  private final SmtSecurityPersonRelationService smtSecurityPersonRelationService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param reqDTO
   * @return
   */
  @ApiOperation("分页查询")
  @PostMapping("/page")
  public Result getPage(Page page, @RequestBody(required = false) SecurityPersonQueryReqDTO reqDTO) {
    return success(smtSecurityPersonRelationService.getPage(page,reqDTO), SecurityPersonRelationRespDTO.class);
  }

	/**
	 * 获得园区-bu-部门-员工树形结构
	 * @return
	 */
	@ApiOperation("获得园区-bu-部门-员工树结构")
	@PostMapping("/staff/tree")
	public Result getStaffTree() {
		return success(smtSecurityPersonRelationService.getStaffTree());
	}

	/**
	 * 获得园区-bu-部门-员工树形结构
	 * @return
	 */
	@ApiOperation("获得园区-bu-部门树的员工")
	@GetMapping("/staff/tree/{depId}")
	public Result getStaffTree(@PathVariable("depId") String depId) {
		return success(smtSecurityPersonRelationService.getStaffByDepId(depId));
	}

	/**
	 * 手动添加员工新增关联
	 * @param reqDTO
	 * @return
	 */
	@ApiOperation("添加员工关联-保密区签署管理/项目维护")
	@PostMapping("/save/relation")
	public Result saveRelation(@RequestBody List<SecurityPersonAddReqDTO> reqDTO) {
		return success(smtSecurityPersonRelationService.saveRelation(reqDTO));
	}

	@ApiOperation("添加员工关联-导入")
	@PostMapping("/export/save/relation")
	public Result saveExport(@RequestBody List<SecurityPersonExcelAddReqDTO> reqDTO) {
		return success(smtSecurityPersonRelationService.saveExportRelation(reqDTO));
	}

  /**
   * 批量删除员工关联
   * @param reqDTO 删除条件
   * @return Result
   */
  @ApiOperation("批量删除员工关联")
  @SysLog("批量删除员工关联")
  @PostMapping("/batch/delete")
  public Result batchDelete(@RequestBody(required = false) SecurityPersonQueryReqDTO reqDTO){
    return success(smtSecurityPersonRelationService.batchDelete(reqDTO));
  }

	/**
	 * 获取所有在职员工
	 * @param reqDTO 筛选条件
	 * @return Result
	 */
	@ApiOperation("获取所有在职员工")
	@SysLog("获取所有在职员工-保密区签署管理")
	@PostMapping("/all/staff")
	public Result getAllStaffPage(Page page, @RequestBody(required = false) SecurityPersonQueryReqDTO reqDTO){
		return success(smtSecurityPersonRelationService.getAllStaffPage(page, reqDTO), AllStaffListRespDTO.class);
	}

}
