package com.tce.smart.platform.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.req.WechatBandingReqDTO;
import com.tce.smart.platform.api.dto.resp.WechatBandingPageRespDTO;
import com.tce.smart.platform.api.dto.resp.WechatBandingRespDTO;
import com.tce.smart.platform.core.entity.SmtWechatBanding;
import com.tce.smart.platform.service.SmtWechatBandingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 微信绑定表
 *
 * @author fushiping
 * @date 2021-10-09 17:20:23
 */
@RestController
@AllArgsConstructor
@Api("微信绑定")
@RequestMapping("/wechat/banding")
public class SmtWechatBandingController extends BaseController {

	private final SmtWechatBandingService smtWechatBandingService;

	/**
	 * 通过id查询微信绑定表
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Long id) {
		return success(smtWechatBandingService.getById(id));
	}

	/**
	 * 微信绑定表list
	 *
	 * @param
	 * @return Result
	 */
	@ApiOperation("微信绑定表列表")
	@PostMapping("/page")
	public Result page(Page page, @RequestBody WechatBandingReqDTO req) {
		return success(smtWechatBandingService.getPage(page,req), WechatBandingPageRespDTO.class);
	}

	/**
	 * 微信绑定表list
	 *
	 * @param
	 * @return Result
	 */
	@ApiOperation("微信详情")
	@GetMapping("/page/{id}")
	public Result getById(@PathVariable("id") String id) {
		return success(smtWechatBandingService.getById(Long.parseLong(id)), WechatBandingPageRespDTO.class);
	}


	/**
	 * 新增微信绑定表
	 *
	 * @param smtWechatBanding 微信绑定表
	 * @return Result
	 */
	@SysLog("新增微信绑定表")
	@PostMapping("/save")
	@Inner
	public Result save(@RequestBody WechatBandingReqDTO smtWechatBanding) {
		return success(smtWechatBandingService.saveInfo(smtWechatBanding));
	}

	/**
	 * 根据openId或者工号获取信息
	 * @param openId
	 * @return
	 */
	@GetMapping("/info/{openId}")
	public Result<WechatBandingRespDTO> getByOpenId(@PathVariable("openId") String openId) {
		SmtWechatBanding banding = smtWechatBandingService.getOne(Wrappers.<SmtWechatBanding>lambdaQuery()
				.eq(SmtWechatBanding::getOpenId, openId).or().eq(SmtWechatBanding::getBadge, openId));
		return success(banding != null ? BeanUtil.toBean(banding, WechatBandingRespDTO.class) : null);
	}

	/**
	 * 修改微信绑定表
	 *
	 * @param smtWechatBanding 微信绑定表
	 * @return Result
	 */
	@SysLog("修改微信绑定表")
	@PostMapping
	public Result updateById(@RequestBody SmtWechatBanding smtWechatBanding) {
		return success(smtWechatBandingService.updateById(smtWechatBanding));
	}

	/**
	 * 通过id删除微信绑定表
	 *
	 * @param id id
	 * @return Result
	 */
	@ApiOperation("删除微信绑定表")
	@PostMapping("/{id}")
	public Result<Boolean> removeById(@PathVariable("id") String id) {
		return success(smtWechatBandingService.removeById(Long.parseLong(id)));
	}

}
