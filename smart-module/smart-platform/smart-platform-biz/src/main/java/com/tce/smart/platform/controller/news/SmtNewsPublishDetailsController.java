package com.tce.smart.platform.controller.news;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.news.SaveNewsInfoReqDTO;
import com.tce.smart.platform.api.dto.req.news.SearchNewsListReqDTO;
import com.tce.smart.platform.api.dto.resp.news.NewsDetailsRespDTO;
import com.tce.smart.platform.api.dto.resp.news.NewsListRespDTO;
import com.tce.smart.platform.service.news.SmtNewsPublishDetailsService;
import com.tce.smart.platform.service.news.SmtNewsTerminalService;
import com.tce.smart.tool.enums.NewsPublicTypeEnum;
import com.tce.smart.tool.enums.NewsTextMoveTypeEnum;
import com.tce.smart.tool.enums.NewsTimeTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import java.math.BigDecimal;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 18:00:02
 */
@RestController
@Api(tags = "消息发布-消息编辑")
@AllArgsConstructor
@RequestMapping("/news/details")
public class SmtNewsPublishDetailsController extends BaseController {

  private final SmtNewsPublishDetailsService smtNewsPublishDetailsService;

  private final SmtNewsTerminalService smtNewsTerminalService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param query
   * @return
   */
  @ApiOperation("分页查询")
  @PostMapping("/page")
  public Result getSmtNewsPublishDetailsPage(Page page, @RequestBody(required = false) SearchNewsListReqDTO query) {
    return success(smtNewsPublishDetailsService.queryPage(page,query), NewsListRespDTO.class);
  }

	/**
	 * 新增
	 * @param
	 * @return Result
	 */
	@ApiOperation("编辑/新增")
	@PostMapping("/edit")
	public Result edit(@RequestBody SaveNewsInfoReqDTO saveNewsInfoReqDTO){
		return success(smtNewsPublishDetailsService.edit(saveNewsInfoReqDTO, smtNewsTerminalService));
	}


  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @ApiOperation("通过id查询")
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") String id){
    return success(smtNewsPublishDetailsService.getById(Long.parseLong(id)), NewsDetailsRespDTO.class);
  }


  /**
   * 取消发布
   * @param id
   * @return Result
   */
  @SysLog("取消发布")
  @ApiOperation("取消发布")
  @GetMapping("/cancel/{id}")
  public Result cancelInfo(@PathVariable("id") String id){
    return success(smtNewsPublishDetailsService.cancelInfo(Long.parseLong(id)));
  }

  /**
   * 通过id删除
   * @param id id
   * @return Result
   */
  @SysLog("删除")
  @ApiOperation("通过id删除")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable("id") String id){
    return success(smtNewsPublishDetailsService.deleteById(Long.parseLong(id), smtNewsTerminalService));
  }

	/**
	 * 获得资源类型枚举
	 * @param
	 * @return Result
	 */
	@SysLog("获得资源类型枚举")
	@ApiOperation("获得资源类型枚举")
	@GetMapping("/info/type")
	public Result getInfoType(){
		return success(NewsPublicTypeEnum.getTypeList());
	}

	/**
	 * 获得发布类型枚举
	 * @param
	 * @return Result
	 */
	@SysLog("获得发布类型枚举")
	@ApiOperation("获得发布类型枚举")
	@GetMapping("/time/type")
	public Result getTimeType(){
		return success(NewsTimeTypeEnum.getTypeList());
	}

	/**
	 * 获得文字滚动类型枚举
	 * @param
	 * @return Result
	 */
	@SysLog("获得文字滚动类型枚举")
	@ApiOperation("获得文字滚动类型枚举")
	@GetMapping("/text/move")
	public Result getTextMove(){
		return success(NewsTextMoveTypeEnum.getTypeList());
	}

}
