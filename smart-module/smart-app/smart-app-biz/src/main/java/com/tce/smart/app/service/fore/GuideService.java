package com.tce.smart.app.service.fore;

import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 引导帮助服务接口
 *
 * @author mingkai.wu
 * @date 2019-05-10 16:16:25
 */
public interface GuideService {

	/**
	 * 获取欢迎页内容
	 *
	 * @param params 分页参数
	 * @return Page<?>
	 */
	Page<?> getWelcome(Map<String, Object> params);

}
