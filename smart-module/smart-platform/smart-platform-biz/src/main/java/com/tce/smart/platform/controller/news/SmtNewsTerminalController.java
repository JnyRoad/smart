package com.tce.smart.platform.controller.news;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.platform.api.dto.req.news.NewsTerminalReqDTO;
import com.tce.smart.platform.api.dto.resp.news.NewsTerminalRespDTO;
import com.tce.smart.platform.core.entity.news.SmtNewsTerminal;
import com.tce.smart.platform.service.news.SmtNewsTerminalService;
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
 * @date 2022-02-16 17:59:47
 */
@RestController
@Api(tags = "消息发布-终端管理")
@AllArgsConstructor
@RequestMapping("/news/terminal")
public class SmtNewsTerminalController extends BaseController {

  private final SmtNewsTerminalService smtNewsTerminalService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param name
   * @return
   */
  @ApiOperation("分页查询")
  @GetMapping("/page")
  public Result getPage(Page page, @RequestParam(value = "name",required = false) String name) {
    return success(smtNewsTerminalService.page(page,Wrappers.<SmtNewsTerminal>query()
			.lambda().like(StringUtils.isNotEmpty(name), SmtNewsTerminal::getName, name)
			.orderByDesc(SmtNewsTerminal::getCreateTime)), NewsTerminalRespDTO.class);
  }


  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @ApiOperation("通过id查询")
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Long id){
    return success(smtNewsTerminalService.getById(id), NewsTerminalRespDTO.class);
  }

  /**
   * 新增
   * @param newsTerminalReqDTO
   * @return Result
   */
  @SysLog("新增")
  @ApiOperation("新增/编辑")
  @PostMapping("/edit")
  public Result edit(@RequestBody NewsTerminalReqDTO newsTerminalReqDTO){
    return success(smtNewsTerminalService.edit(newsTerminalReqDTO));
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
    return success(smtNewsTerminalService.removeById(id));
  }

	/**
	 * 检查资源是否到期
	 * @return
	 */
	@ApiOperation("检查资源是否到期")
	@Inner
	@OpenApi("server")
	@GetMapping("/check")
	public void checkPublic(){
		smtNewsTerminalService.checkPublic();
	}

}
