package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityWhiteReqDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityWhite;

import java.util.List;

/**
 * 权限删除白名单
 *
 * @author fushiping
 * @date 2021-07-29 11:13:07
 */
public interface SmtSecurityWhiteService extends IService<SmtSecurityWhite> {

	/**
	 * 获得白名单列表
	 * @param securityId
	 * @return
	 */
	List<SmtSecurityWhite> getList(Long securityId);

	/**
	 * 编辑白名单
	 * @param req
	 * @param deleteConfigId 定期删除配置ID
	 * @return
	 */
	Boolean editList(List<SecurityWhiteReqDTO> req, Long deleteConfigId);

	/**
	 * 根据staffId判断是否存在于白名单中
	 * @param staffId
	 * @return
	 */
	Boolean isExist(Long configId, Long staffId);
}
