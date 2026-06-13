package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.WechatBandingReqDTO;
import com.tce.smart.platform.core.entity.SmtWechatBanding;

/**
 * 微信绑定表
 *
 * @author fushiping
 * @date 2021-10-09 17:20:23
 */
public interface SmtWechatBandingService extends IService<SmtWechatBanding> {

	/**
	 * 根据openId获得unionId
	 * @param openId
	 * @return
	 */
	String getUnionId(String openId);

	IPage<SmtWechatBanding> getPage(Page page, WechatBandingReqDTO reqDTO);

	/**
	 * 新增绑定信息
	 * @param smtWechatBanding
	 * @return
	 */
	Boolean saveInfo(WechatBandingReqDTO smtWechatBanding);

}
