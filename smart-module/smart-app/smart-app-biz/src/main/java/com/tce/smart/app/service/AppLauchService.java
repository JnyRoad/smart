package com.tce.smart.app.service;

import java.util.List;

import com.tce.smart.app.entity.AppContentPicture;

/**
 * App启动引导服务接口
 *
 * @author mingkai.wu
 * @date 2019-05-12 18:00:37
 */
public interface AppLauchService {

	/**
	 * 获取引导启动也图片信息
	 *
	 * @return List<AppContentPicture>
	 */
	List<AppContentPicture> getLauchInfo();

}
