package com.tce.smart.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.api.dto.*;
import com.tce.smart.app.entity.AppWechatBinding;

/**
 * @description: 微信绑定Service
 * @date: 2020-08-06 17:46
 * @author: wuling
 * @version: 1.0
 */
public interface AppWechatBindingService extends IService<AppWechatBinding> {
	/**
	 * 检查微信是否绑定手机号
	 * @param code
	 * @return
	 */
	WechatBindingInfoDTO isWechatBinding(String code);

	/**
	 * 保存微信绑定信息
	 * @return
	 */
	boolean saveWechatBinding(WechatBindingReqDTO wechatBindingReqDTO);

	/**
	 * 保存微信openId与工号的关系
	 * @param reqDTO
	 * @return
	 */
	boolean saveWechatOpenIdAndBadge(WechatOpenIdBindingReqDTO reqDTO);

	/**
	 * 解绑微信openId与工号的关系
	 * @return
	 */
	boolean unbindWechatOpenIdAndBadge();

	/**
	 * 修改微信绑定手机号
	 * @param bindingPhoneReqDTO
	 * @return
	 */
	boolean updateBindingPhone(BindingPhoneReqDTO bindingPhoneReqDTO);

	/**
	 * 修改访问者头像
	 * @param visitorImgReqDTO
	 * @return
	 */
	boolean updateImg(VisitorImgReqDTO visitorImgReqDTO);
}
