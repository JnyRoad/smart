package com.tce.smart.platform.controller.securityzone;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.securityzone.UpdateFaceImgReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.TaskDetailsRespDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityTaskDetails;
import com.tce.smart.platform.service.securityzone.SmtSecurityTaskDetailsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:17
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-保密区权限下发记录详情")
@RequestMapping("/security/task/details")
public class SmtSecurityTaskDetailsController extends BaseController {

  private final SmtSecurityTaskDetailsService smtSecurityTaskDetailsService;


	/**
	 * 列表查询
	 * @param applyId
	 * @return
	 */
	@GetMapping("/list/{applyId}")
	@ApiOperation("列表查询")
	public Result getList(@PathVariable("applyId") String applyId) {
		return success(smtSecurityTaskDetailsService.getList(Long.parseLong(applyId)), TaskDetailsRespDTO.class);
	}

	/**
	 * 重传照片
	 * @param req
	 * @return
	 */
	@PostMapping("/reload")
	@ApiOperation("重传照片")
	public Result reloadImg(@RequestBody UpdateFaceImgReqDTO req) {
		return success(smtSecurityTaskDetailsService.reloadImg(req));
	}


  /**
   * 分页查询
   * @param page 分页对象
   * @param smtSecurityTaskDetails
   * @return
   */
  @GetMapping("/page")
  public Result getSmtSecurityTaskDetailsPage(Page page, SmtSecurityTaskDetails smtSecurityTaskDetails) {
    return success(smtSecurityTaskDetailsService.page(page, Wrappers.query(smtSecurityTaskDetails)), TaskDetailsRespDTO.class);
  }


  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  @ApiOperation("通过id查询")
  public Result getById(@PathVariable("id") Integer id){
    return success(smtSecurityTaskDetailsService.getById(id), TaskDetailsRespDTO.class);
  }

  /**
   * 修改
   * @param smtSecurityTaskDetails
   * @return Result
   */
  @SysLog("修改")
  @PostMapping
  public Result updateById(@RequestBody SmtSecurityTaskDetails smtSecurityTaskDetails){
    return success(smtSecurityTaskDetailsService.updateById(smtSecurityTaskDetails));
  }

  /**
   * 通过id删除
   * @param id id
   * @return Result
   */
  @SysLog("删除")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return success(smtSecurityTaskDetailsService.removeById(id));
  }

}
