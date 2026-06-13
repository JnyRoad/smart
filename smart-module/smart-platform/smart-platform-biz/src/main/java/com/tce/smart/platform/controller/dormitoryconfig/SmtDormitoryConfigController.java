package com.tce.smart.platform.controller.dormitoryconfig;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.dormitoryconfig.DormitoryConfigEditReqDTO;
import com.tce.smart.platform.api.dto.resp.dormitoryconfig.DormitoryConfigListRespDTO;
import com.tce.smart.platform.api.dto.resp.dormitoryconfig.DormitoryConfigRespDTO;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

/**
 *
 *
 * @author fushiping
 * @date 2021-09-14 20:14:53
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-宿舍配置")
@RequestMapping("/dormitory/config")
public class SmtDormitoryConfigController extends BaseController {

  private final SmtDormitoryConfigService smtDormitoryConfigService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param parkId
   * @return
   */
  @ApiOperation("分页查询")
  @GetMapping("/page")
  public Result getSmtDormitoryConfigPage(Page page, Integer parkId) {
    return success(smtDormitoryConfigService.getPage(page, parkId), DormitoryConfigListRespDTO.class);
  }


  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @ApiOperation("通过id查询")
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") String id){
    return success(smtDormitoryConfigService.getById(Long.parseLong(id)), DormitoryConfigRespDTO.class);
  }

  /**
   * 新增
   * @param smtDormitoryConfig
   * @return Result
   */
  @ApiOperation("编辑")
  @PostMapping("/edit")
  public Result save(@RequestBody DormitoryConfigEditReqDTO smtDormitoryConfig){
    return success(smtDormitoryConfigService.editConfig(smtDormitoryConfig));
  }

}
