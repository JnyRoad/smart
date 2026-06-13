package com.tce.smart.app.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.entity.AppIcbcConfig;

/**
 * 工商银行接入配置服务接口
 *
 * @author mkwu
 * @date 2019-08-23
 */
public interface AppIcbcConfigService extends IService<AppIcbcConfig> {

	/**
	 * 查询当前使用的配置
	 *
	 * @return 可用的配置列表
	 */
	List<AppIcbcConfig> getCurrentConfig();
}
