package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthDeleteReqDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDelete;

/**
 *
 *保密区权限定期删除配置
 * @author fushiping
 * @date 2021-07-29 11:13:24
 */
public interface SmtSecurityAuthDeleteService extends IService<SmtSecurityAuthDelete> {

	/**
	 * 获得权限删除配置
	 * @param parkId
	 * @return
	 */
	SmtSecurityAuthDelete getConfig(Integer parkId);

	/**
	 * 获得权限配置分页列表
	 * @param page
	 * @return
	 */
	IPage<SmtSecurityAuthDelete> getList(Page page);

	/**
	 * 编辑权限删除配置
	 * @param reqDTO
	 * @return
	 */
	Boolean editConfig(SecurityAuthDeleteReqDTO reqDTO);

	/**
	 * 自动删除权限任务
	 */
	void deleteAuthTask();

}
