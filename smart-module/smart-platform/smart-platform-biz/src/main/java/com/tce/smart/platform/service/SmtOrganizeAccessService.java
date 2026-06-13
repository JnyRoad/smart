package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtOrganizeAccess;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/2 16:56
 */
public interface SmtOrganizeAccessService extends IService<SmtOrganizeAccess> {

	/**
	 * 通过组织id获取门禁id列表
	 * @param organizeId
	 * @return
	 */
	List<Integer> getDeviceAuthId(Long organizeId);

	/**
	 * 通过组织id删除门禁id列表
	 * @param organizeId
	 * @return
	 */
	Boolean delByOrgId(Long organizeId);
}
