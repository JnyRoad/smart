package com.tce.smart.platform.controller.badge;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.badge.EditBadgeConfigReqDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeConfigListRespDTO;
import com.tce.smart.platform.service.badge.SmtBadgeConfigService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import javax.validation.Valid;
import java.util.Objects;

/**
 * 厂牌领取设置
 *
 * @author fushiping
 * @date 2020-07-07 11:47:51
 */
@RestController
@AllArgsConstructor
@RequestMapping("/badge/config")
public class SmtBadgeConfigController extends BaseController {

  private final SmtBadgeConfigService smtBadgeConfigService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param parkId 厂牌查询对象
   * @return
   */
  @GetMapping("/page")
  public Result getSmtBadgeConfigPage(Page page, @RequestParam(value = "parkId", required = false) Integer parkId) {
	  return success(smtBadgeConfigService.getPage(page, parkId), BadgeConfigListRespDTO.class);
  }

	/**
	 * 通过id查询厂牌领取设置
	 * @param parkId id
	 * @return Result
	 */
	@GetMapping("by/parkId")
	public Result getByParkId(@RequestParam("parkId") Integer parkId){
		return success(smtBadgeConfigService.getConfigByPark(parkId), BadgeConfigListRespDTO.class);
	}


  /**
   * 通过id查询厂牌领取设置
   * @param id id
   * @return Result
   */
  @GetMapping("by/id")
  public Result getById(@RequestParam("id") Integer id){
    return success(smtBadgeConfigService.getById(id), BadgeConfigListRespDTO.class);
  }

  /**
   * 新增厂牌领取设置
   * @param reqDTO 厂牌领取设置
   * @return Result
   */
  @SysLog("新增厂牌领取设置")
  @PostMapping("/edit")
  public Result save(@Valid @RequestBody EditBadgeConfigReqDTO reqDTO){
    return success(smtBadgeConfigService.edit(reqDTO));
  }

  /**
   * 通过id删除厂牌领取设置
   * @param id id
   * @return Result
   */
  @SysLog("删除厂牌领取设置")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return success(smtBadgeConfigService.removeById(id));
  }

}
