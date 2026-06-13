package com.tce.smart.bridge.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.bridge.core.entity.ExceptionLog;

import java.util.List;

/**
 * <p>
 * 园区异常消息表 服务类
 * </p>
 */
public interface ExceptionLogService extends IService<ExceptionLog> {
	List<ExceptionLog> getList(Integer id, Integer size);

	boolean insert(String key, String value);
}
