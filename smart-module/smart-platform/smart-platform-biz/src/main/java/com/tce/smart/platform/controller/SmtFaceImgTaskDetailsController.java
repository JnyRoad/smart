package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.resp.FaceImgTaskDetailsRespDTO;
import com.tce.smart.platform.service.SmtFaceImgTaskDetailsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 *员工人脸图片批量上传任务
 * @author fushiping
 * @date 2021-07-20 17:44:48
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-员工照片批量导入任务详情")
@RequestMapping("/staff/img/detail")
public class SmtFaceImgTaskDetailsController extends BaseController {

  private final SmtFaceImgTaskDetailsService smtFaceImgTaskDetailsService;

  /**
   * 分页查询
   * @param status
   * @param
   * @return
   */
  @ApiOperation("列表")
  @GetMapping("/list")
  public Result getSmtFaceImgTaskDetailsPage(@RequestParam(required = false, value = "status") Integer status,
											 @RequestParam(required = false, value = "taskId") Long taskId) {
    return success(smtFaceImgTaskDetailsService.getByTaskId(status, taskId), FaceImgTaskDetailsRespDTO.class);
  }

	/**
	 * 分页查询
	 * @param status
	 * @param
	 * @return
	 */
	@ApiOperation("分页")
	@GetMapping("/page")
	public Result getPage(Page page, @RequestParam(required = false, value = "status") Integer status,
						  @RequestParam(required = false, value = "taskId") Long taskId) {
		return success(smtFaceImgTaskDetailsService.getPage(page, status, taskId), FaceImgTaskDetailsRespDTO.class);
	}

  /**
   * 通过id删除
   * @param taskId id
   * @return Result
   */
  @ApiOperation("通过id删除")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable String taskId){
    return success(smtFaceImgTaskDetailsService.deleteTaskDetail(Long.parseLong(taskId)));
  }

	/**
	 * 挂失记录导出
	 * @param
	 * @return
	 */
	@ApiOperation("导入记录导出")
	@GetMapping("/excel")
	public ResponseEntity<byte[]> excel(@RequestParam(required = false, value = "status") Integer status,
										@RequestParam(required = false, value = "taskId") Long taskId) {
		return smtFaceImgTaskDetailsService.downLoadExcel(status, taskId);
	}
}
