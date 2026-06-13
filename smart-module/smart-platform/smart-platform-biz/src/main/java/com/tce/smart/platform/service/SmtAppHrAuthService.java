package com.tce.smart.platform.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtAppHrAuth;

/**
 * app招聘数据权限服务接口
 *
 * @author mckaywu
 * @date 2019-06-12 11:17:37
 */
public interface SmtAppHrAuthService extends IService<SmtAppHrAuth> {

	/**
	 * 获取招聘数据权限
	 *
	 * @return
	 */
	List<SmtAppHrAuth> getHrAuthList();

}
