package com.tce.smart.platform.service;

import com.tce.smart.platform.core.ao.ParkOrgSetSaveAO;
import com.tce.smart.platform.core.vo.ParkOrgSetEditVo;

/**
 * 园区组织关系服务接口
 *
 * @author mckaywu
 * @date 2019-11-20 10:35:16
 */
public interface SmtParkOrgSetService {

	/**
	 * 查看园区组织关系
	 *
	 * @param parkId 园区ID
	 * @return ParkOrgSetEditVo
	 */
	ParkOrgSetEditVo viewParkOrg(Integer parkId);

	/**
	 * 新增、修改园区组织关系
	 *
	 * @param parkOrgSetSaveAO 园区组织信息保存Ao
	 * @return Boolean true-成功，fasle-失败
	 */
	Boolean saveParkOrg(ParkOrgSetSaveAO parkOrgSetSaveAO);
}
