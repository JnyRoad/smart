package com.tce.smart.platform.service.admittance;

import com.tce.smart.platform.api.dto.req.admittance.VisitorManualAuthReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorManualAuthOptionsRespDTO;

/**
 * 访客申请管理端手动下发 ISC 人员权限服务。
 */
public interface VisitorManualAuthService {

	/**
	 * 查询申请单可用的人员和公共 ISC 人员权限组选项，车辆列表固定为空。
	 *
	 * @param applyId 申请单 ID
	 * @return 受当前用户园区范围约束的选项
	 */
	VisitorManualAuthOptionsRespDTO getOptions(Long applyId);

	/**
	 * 为申请单中的一名人员创建一个 ISC 下发批次。
	 *
	 * @param request 手动授权请求
	 * @return 批次号字符串
	 */
	String submit(VisitorManualAuthReqDTO request);
}
