package com.tce.smart.platform.service.badge;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.badge.EditBadgeConfigReqDTO;
import com.tce.smart.platform.core.entity.badge.SmtBadgeConfig;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 厂牌领取设置
 *
 * @author fushiping
 * @date 2020-07-07 11:47:51
 */
public interface SmtBadgeConfigService extends IService<SmtBadgeConfig> {

	/**
	 * 新增或编辑厂牌设置
	 * @param reqDTO
	 * @return
	 */
	Boolean edit(EditBadgeConfigReqDTO reqDTO);

	/**
	 * 分页查询
	 * @param page
	 * @param parkId
	 * @return
	 */
	IPage<SmtBadgeConfig> getPage(Page page, Integer parkId);

	/**
	 * 根据园区获得厂牌补领配置
	 * @param parkId
	 * @return
	 */
	SmtBadgeConfig getConfigByPark(Integer parkId);

}
