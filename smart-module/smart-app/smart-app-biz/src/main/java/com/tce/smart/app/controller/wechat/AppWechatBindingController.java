package com.tce.smart.app.controller.wechat;

import com.tce.smart.app.api.dto.*;
import com.tce.smart.app.dto.WechatUserInfoDto;
import com.tce.smart.app.service.AppWechatBindingService;
import com.tce.smart.app.service.wechat.WechatAuthService;
import com.tce.smart.common.core.model.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @description: 微信绑定控制器
 * @date: 2020-08-06 17:53
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "微信绑定")
@RestController
@AllArgsConstructor
@RequestMapping("/wechat")
public class AppWechatBindingController {

	private final AppWechatBindingService appWechatBindingService;
	private final WechatAuthService wechatAuthService;

	/**
	 * 通过code查询微信是否绑定手机号
	 *
	 * @param code 微信用户授权code
	 * @return
	 */
	@ApiOperation("查询微信是否绑定")
	@GetMapping("/isbinding/{code}")
	public Result<WechatBindingInfoDTO> isWechatBinding(@PathVariable String code) {
		return new Result<>(appWechatBindingService.isWechatBinding(code));
	}


	/**
	 * 绑定微信
	 * @return
	 */
	@ApiOperation("绑定微信")
	@PostMapping("/binding")
	public Result<Boolean> saveWechatBinding(@RequestBody WechatBindingReqDTO wechatBindingReqDTO){
		return new Result<>(appWechatBindingService.saveWechatBinding(wechatBindingReqDTO));
	}

	/**
	 * 修改与微信绑定的手机号
	 * @return
	 */
	@ApiOperation("修改与微信绑定的手机号")
	@PostMapping("/updatephone")
	public Result<Boolean> updateBindingPhone(@RequestBody BindingPhoneReqDTO bindingPhoneReqDTO){
		return new Result<>(appWechatBindingService.updateBindingPhone(bindingPhoneReqDTO));
	}

	/**
	 * 修改头像
	 * @return
	 */
	@ApiOperation("修改头像")
	@PostMapping("/updateimg")
	public Result<Boolean> updateImg(@RequestBody VisitorImgReqDTO visitorImgReqDTO){
		return new Result<>(appWechatBindingService.updateImg(visitorImgReqDTO));
	}

	/**
	 * 根据微信code获取工号
	 * @return
	 */
	@ApiOperation("根据微信code获取工号")
	@PostMapping("/getBadge")
	public Result<String> getBadge(@RequestParam String code){
		return new Result<>(wechatAuthService.getBadge(code));
	}

	/**
	 * 根据微信code获取工号
	 * @return
	 */
	@ApiOperation("根据code获得用户信息")
	@PostMapping("/getInfo")
	public Result<WechatUserInfoDto> getUserInfo(@RequestParam String code){
		return new Result<>(wechatAuthService.getUnionId(code));
	}

	/**
	 * 根据微信code获取工号
	 * @return
	 */
	@ApiOperation("许昌园区-根据code绑定用户信息")
	@PostMapping("/xc/banging")
	public Result<Boolean> bangingForXc(@RequestParam String code){
		return new Result<>(wechatAuthService.bangingForXc(code));
	}

	/**
	 * 绑定微信openId与工号的关系
	 * @return
	 */
	@ApiOperation("许昌园区-根据code绑定用户信息")
	@PostMapping("/xc/banging/badge")
	public Result<Boolean> bangingOpenIdAndBadge(@RequestBody @Valid WechatOpenIdBindingReqDTO reqDTO){
		return new Result<>(appWechatBindingService.saveWechatOpenIdAndBadge(reqDTO));
	}

	/**
	 * 解绑微信openId与工号的关系
	 * @return
	 */
	@ApiOperation("许昌园区-解绑微信openId与工号的关系")
	@PostMapping("/xc/unbind")
	public Result<Boolean> bangingOpenIdAndBadge(){
		return new Result<>(appWechatBindingService.unbindWechatOpenIdAndBadge());
	}

	/**
	 * 获取微信签名
	 * @param url
	 * @return
	 */
	@GetMapping("/sign")
	public Result<WxSignDTO> getWxSignByUrl(@RequestParam("url") String url) {
		return new Result<>(wechatAuthService.getWxSignByUrl(url));
	}

	/**
	 * 根据微信code获取工号
	 * @return
	 */
	@ApiOperation("根据微信code获取工号")
	@GetMapping("/getBadge/by-code")
	public Result<String> getBadgeByCode(@RequestParam("code") String code){
		return new Result<>(wechatAuthService.getBadgeByCode(code));
	}
}
