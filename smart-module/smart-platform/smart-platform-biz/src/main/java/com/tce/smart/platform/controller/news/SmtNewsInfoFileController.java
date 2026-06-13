package com.tce.smart.platform.controller.news;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.news.NewsInfoFileReqDTO;
import com.tce.smart.platform.service.news.SmtNewsInfoFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 18:00:09
 */
@RestController
@AllArgsConstructor
@Api(tags = "消息发布-文件管理")
@RequestMapping("/news/file")
public class SmtNewsInfoFileController extends BaseController {

  private final SmtNewsInfoFileService smtNewsInfoFileService;

  /**
   * 文件上传
   * @return
   */
  @ApiOperation("文件上传")
  @PostMapping("/upload")
  public Result<String> upload(NewsInfoFileReqDTO fileReqDTO, @RequestParam("data") MultipartFile data) throws IOException {
    return success(smtNewsInfoFileService.upload(fileReqDTO, data));
  }

	/**
	 * 文件下载
	 * @return
	 */
	@ApiOperation("文件下载byte")
	@GetMapping("/download/{id}")
	public Result<byte[]> download(@PathVariable("id")String id) {
		return success(smtNewsInfoFileService.download(id));
	}

	/**
	 * 文件下载
	 * @return
	 */
	@ApiOperation("文件流输出")
	@GetMapping("/stream/{id}")
	public void getStream(HttpServletRequest request, HttpServletResponse response, @PathVariable("id")String id) {
		smtNewsInfoFileService.getStream(request, response, id);
	}


  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return success(smtNewsInfoFileService.getById(id));
  }


  /**
   * 通过id删除
   * @param id id
   * @return Result
   */
  @SysLog("删除")
  @PostMapping("/{id}")
  @ApiOperation("删除")
  public Result removeById(@PathVariable String id){
    return success(smtNewsInfoFileService.removeById(Long.parseLong(id)));
  }

}
