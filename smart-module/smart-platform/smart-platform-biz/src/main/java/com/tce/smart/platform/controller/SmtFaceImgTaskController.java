package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.FaceImgTaskQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.FaceImgTaskRespDTO;
import com.tce.smart.platform.core.dto.CheckFacePicDTO;
import com.tce.smart.platform.service.SmtFaceImgTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author fushiping
 * @date 2021-07-20 17:44:40
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-员工照片批量导入任务")
@RequestMapping("/staff/img/task")
public class SmtFaceImgTaskController extends BaseController {

  private final SmtFaceImgTaskService smtFaceImgTaskService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param query
   * @return
   */
  @ApiOperation("分页查询")
  @SysLog("分页查询")
  @PostMapping("/page")
  public Result getSmtFaceImgTaskPage(Page page, @RequestBody(required = false) FaceImgTaskQueryReqDTO query) {
		return success(smtFaceImgTaskService.getPage(page, query), FaceImgTaskRespDTO.class);
	}

	@ApiOperation("员工批量上传图片")
	@SysLog("员工批量上传图片")
	@PostMapping("/upload")
	public Result<Boolean> upload(@RequestBody CheckFacePicDTO check) {
		return success(smtFaceImgTaskService.checkFacePic(check));
	}

  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @ApiOperation("通过id查询")
  @SysLog("通过id查询")
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return success(smtFaceImgTaskService.getById(id), FaceImgTaskRespDTO.class);
  }


  /**
   * 通过id删除
   * @param id id
   * @return Result
   */
  @ApiOperation("删除")
  @SysLog("删除")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Long id){
    return success(smtFaceImgTaskService.deleteTask(id));
  }

}
