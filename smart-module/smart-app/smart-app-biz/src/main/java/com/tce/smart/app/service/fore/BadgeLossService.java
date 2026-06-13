package com.tce.smart.app.service.fore;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.vo.fore.BadgeInfoVo;
import com.tce.smart.platform.api.dto.req.badge.QueryLossInfoReqDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeLossInfoRespDTO;

/**
 * 厂牌挂失服务
 * @author fushiping
 * @date 2020/7/9  18:19
 **/
public interface BadgeLossService {

	/**
	 * 获得当前登录用户厂牌信息
	 *
	 * @return BadgeInfoVo 厂牌基础信息
	 */
	BadgeInfoVo getBadgeInfo();

	/**
	 * 厂牌挂失
	 *
	 * @param cardId 厂牌id
	 * @param parkId 园区id
	 * @return true-挂失成功，false-挂失失败
	 */
	Boolean badgeLoss(Long cardId, Integer parkId);



}
