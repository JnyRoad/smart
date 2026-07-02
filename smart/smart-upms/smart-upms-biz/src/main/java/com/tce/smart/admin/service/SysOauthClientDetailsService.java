package com.tce.smart.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.admin.api.entity.SysOauthClientDetails;

/**
 * <p>
 * 服务类
 * </p>
 *
 */
public interface SysOauthClientDetailsService extends IService<SysOauthClientDetails> {
	/**
	 * 通过ID删除客户端
	 *
	 * @param id
	 * @return
	 */
	Boolean removeClientDetailsById(String id);

	/**
	 * 根据客户端信息
	 *
	 * @param sysOauthClientDetails
	 * @return
	 */
	Boolean updateClientDetailsById(SysOauthClientDetails sysOauthClientDetails);

	/**
	 * 重置指定客户端的 secret：生成 32 位随机明文，{bcrypt} 编码后落库，
	 * 并吊销该客户端的旧 token + 清客户端详情缓存。
	 *
	 * @param clientId 客户端ID
	 * @return 新生成的明文 secret（仅本次响应返回一次，不落日志）
	 */
	String resetSecret(String clientId);
}
