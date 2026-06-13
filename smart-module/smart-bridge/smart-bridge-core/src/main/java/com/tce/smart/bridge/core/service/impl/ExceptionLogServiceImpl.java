package com.tce.smart.bridge.core.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.bridge.core.entity.ExceptionLog;
import com.tce.smart.bridge.core.mapper.ExceptionLogMapper;
import com.tce.smart.bridge.core.service.ExceptionLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 公司表 服务实现类
 * </p>
 */
@Service
public class ExceptionLogServiceImpl extends ServiceImpl<ExceptionLogMapper, ExceptionLog> implements ExceptionLogService {

	@Override
	public List<ExceptionLog> getList(Integer id, Integer size) {
		return this.baseMapper.getList(id, size);
	}

	@Override
	public boolean insert(String key, String value) {
		ExceptionLog exceptionLog = new ExceptionLog();
		exceptionLog.setEventType(key);
		exceptionLog.setMessage(value);
		exceptionLog.setCreateTime(LocalDateTime.now());
		return exceptionLog.insert();
	}
}
