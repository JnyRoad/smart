package com.tce.smart.schedule.service.platform;


/***
 * description: 图片存储服务类 <br>
 * date: 2019/12/11 9:23 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
public interface IMoveImageService {

	/**
	 *分页迁移Hbase图片
	 */
	void pageSaveHbaeImage();
}
