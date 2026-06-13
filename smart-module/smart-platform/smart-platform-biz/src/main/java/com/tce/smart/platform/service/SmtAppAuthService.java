package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.ao.SmtAppAuthSaveAO;
import com.tce.smart.platform.core.entity.SmtAppAuth;

import java.util.List;

/**
 * App权限服务接口
 *
 * @author mckaywu
 * @date 2019-06-12 11:18:10
 */
public interface SmtAppAuthService extends IService<SmtAppAuth> {

	/**
	 * 分页查看App权限
	 *
	 * @param page       分页参数
	 * @param smtAppAuth 查询条件
	 * @return 分页数据
	 */
	IPage<SmtAppAuth> getSmtAuthPage(Page<SmtAppAuth> page, SmtAppAuth smtAppAuth);

	/**
	 * 查询所有可用权限
	 * @return
	 */
	List<SmtAppAuth> getAuthList();


	/**
	 * 添加新权限
	 *
	 * @param appAuthSaveAO 新增修改权限Ao
	 * @return true-成功
	 */
	Boolean addAuth(SmtAppAuthSaveAO appAuthSaveAO);

	/**
	 * 修改权限
	 *
	 * @param appAuthSaveAO 新增修改权限Ao
	 * @return true-成功
	 */
	Boolean updateAuthById(SmtAppAuthSaveAO appAuthSaveAO);

	/**
	 * 删除权限
	 *
	 * @param id 权限ID
	 * @return true-成功
	 */
	Boolean removeAuthById(Integer id);

	/**
	 * 获取初始化权限
	 *
	 * @return SmtAppAuth 初始化权限
	 */
	SmtAppAuth getInitAuth();

	/**
	 * 根据园区获取初始化权限
	 * @param parkId
	 * @return
	 */
	SmtAppAuth getInitAuth(Integer parkId);

	Boolean getInitFlag(Integer parkId);
}
