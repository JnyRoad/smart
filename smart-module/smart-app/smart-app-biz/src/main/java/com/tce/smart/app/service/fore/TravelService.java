package com.tce.smart.app.service.fore;

import java.util.Map;

import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.common.core.model.Result;

/**
 * 出差申请接口
 * @author 梁圆
 *
 */
public interface TravelService {

	/**
	 * 获取出差类表
	 * @param params
	 * @return
	 */
	Result getTravelList(Map<String, Object> params);

	/**
	 * 获取出差的详情
	 * @param vacateAoId
	 * @return
	 */
	Result getTravelDetail(AllApplicationAo vacateAoId);

	/**
	 * 查询出差报告
	 * @param allApplicationAoId
	 * @return
	 */
	Result getTravelInfoReport(AllApplicationAo allApplicationAoId);

	/**
	 * 查询出差日程
	 * @param allApplicationAoId
	 * @return
	 */
	Result getTravelInfoDay(AllApplicationAo allApplicationAoId);

	/**
	 * 查询出差流程
	 * @param allApplicationAoId
	 * @return
	 */
	Result getTravelInfoFlow(AllApplicationAo allApplicationAoId);
}
