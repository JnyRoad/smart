package com.tce.smart.platform.controller.securityzone;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.securityzone.*;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityZoneRespDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityZone;
import com.tce.smart.platform.service.securityzone.SmtSecurityZoneService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 *
 *保密区项目维护
 * @author fushiping
 * @date 2021-07-29 11:12:46
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-保密区项目维护")
@RequestMapping("/security/zone")
public class SmtSecurityZoneController extends BaseController {

  private final SmtSecurityZoneService smtSecurityZoneService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param
   * @return
   */
  @PostMapping("/page")
  @ApiOperation("分页查询")
  public Result getPage(Page page, @RequestBody(required = false) SecurityZoneQueryReqDTO query) {
    return success(smtSecurityZoneService.getPage(page,query), SecurityZoneRespDTO.class);
  }

	/**
	 * 列表查询
	 * @param
	 * @param
	 * @return
	 */
	@PostMapping("/list")
	@ApiOperation("列表查询")
	public Result getPage(@RequestParam(value = "parkId", required = false) Integer parkId) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return success(smtSecurityZoneService.list(Wrappers.<SmtSecurityZone>query().lambda().in(SmtSecurityZone::getParkId, parkIds)
				.eq(Objects.nonNull(parkId), SmtSecurityZone::getParkId, parkId)), SecurityZoneRespDTO.class);
	}


  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  @ApiOperation("通过id查询")
  public Result getById(@PathVariable("id") Integer id){
    return success(smtSecurityZoneService.getById(id), SecurityZoneRespDTO.class);
  }


  /**
   * 新增
   * @param smtSecurityZone
   * @return Result
   */
  @SysLog("新增")
  @ApiOperation("新增")
  @PostMapping("/save")
  public Result save(@RequestBody SecurityZoneEditReqDTO smtSecurityZone){
    return success(smtSecurityZoneService.saveZone(smtSecurityZone));
  }

  /**
   * 修改
   * @param smtSecurityZone
   * @return Result
   */
  @SysLog("修改")
  @ApiOperation("修改")
  @PostMapping("/edit")
  public Result updateById(@RequestBody SecurityZoneEditReqDTO smtSecurityZone){
    return success(smtSecurityZoneService.editZone(smtSecurityZone));
  }

  /**
   * 通过id删除
   * @param query id
   * @return Result
   */
  @SysLog("批量删除")
  @ApiOperation("批量删除")
  @PostMapping("/batch/delete")
  public Result removeById(@RequestBody SecurityZoneQueryReqDTO query){
    return success(smtSecurityZoneService.deleteZone(query));
  }

	/**
	 * 通过id查询
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/byStaff/{staffId}")
	@ApiOperation("根据员工id查看员工保密区权限-保密区签署管理")
	public Result getByStaffId(@PathVariable("staffId") Long id){
		return success(smtSecurityZoneService.getSecurityZoneByStaff(id), SecurityZoneRespDTO.class);
	}


	/**
	 * 添加人员时选定查询待关联员工
	 * @param reqDTO
	 * @return
	 */
	@ApiOperation("添加人员时选定查询待关联员工")
	@PostMapping("/ready/Staff")
	public Result getReadyStaff(@RequestBody SecurityStaffQueryReqDTO reqDTO) {
		return success(smtSecurityZoneService.getStaffByInfo(reqDTO));
	}

	/**
	 * 员工选定后再次筛选
	 * @param reqDTO
	 * @return
	 */
	@ApiOperation("员工选定后再次筛选")
	@PostMapping("/query/Staff")
	public Result getQueryStaff(@RequestBody SecurityStaffCheckReqDTO reqDTO) {
		return success(smtSecurityZoneService.getCheckStaff(reqDTO));
	}

	/**
	 * 员工申请权限时获取错误信息
	 * @param
	 * @return
	 */
	@ApiOperation("员工申请权限时获取错误信息")
	@PostMapping("/query/remark")
	public Result getQueryStaff(@RequestBody List<AuthApplyRemarkReqDTO> req) {
		return success(smtSecurityZoneService.getAuthRemark(req));
	}

}
