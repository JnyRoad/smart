package com.tce.smart.app.service.fore.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.service.fore.TravelService;
import com.tce.smart.common.core.constant.PaginationConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.feign.RemoteTravelApplicationService;

import cn.hutool.core.map.MapUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/**
 * 出差接口实现
 * @author ly
 *
 */
@Service
@AllArgsConstructor
@Slf4j
public class TravelServiceImpl implements TravelService {


	private RemoteTravelApplicationService remoteTravelApplicationService;

	/**
	 * 获取出差的列表
	 */
	@Override
	public Result getTravelList(Map<String, Object> params) {
/*		String staffBadge = "017491";
*/		String staffBadge = SecurityUtils.getUser().getUsername();
		 return remoteTravelApplicationService.getSmtTravelApplicationPage(MapUtil.getInt(params, PaginationConstants.CURRENT), MapUtil.getInt(params, PaginationConstants.SIZE),
					staffBadge,SecurityConstants.FROM_IN);
	}
	/**
	 * 获取出差详情
	 */
	@Override
	public Result getTravelDetail(AllApplicationAo allApplicationAoId) {
		return remoteTravelApplicationService.getById(Integer.parseInt(allApplicationAoId.getRecordId()),SecurityConstants.FROM_IN);
	}

	/**
	 * 获取出差报告
	 */
	@Override
	public Result getTravelInfoReport(AllApplicationAo allApplicationAoId) {
		return remoteTravelApplicationService.getInfoReport(Integer.parseInt(allApplicationAoId.getRecordId()),SecurityConstants.FROM_IN);
	}

	/**
	 * 获取出差日程
	 */
	@Override
	public Result getTravelInfoDay(AllApplicationAo allApplicationAoId) {
		return remoteTravelApplicationService.getInfoDay(Integer.parseInt(allApplicationAoId.getRecordId()),SecurityConstants.FROM_IN);
	}

	/**
	 * 获取出差流程
	 */
	@Override
	public Result getTravelInfoFlow(AllApplicationAo allApplicationAoId) {
		return remoteTravelApplicationService.getInfoFlow(Integer.parseInt(allApplicationAoId.getRecordId()),SecurityConstants.FROM_IN);
	}
}
