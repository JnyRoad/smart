package com.tce.smart.admin.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.admin.api.entity.SysMoveDataTask;

/**
 * <p>
 * 数据转移务表配置 服务类
 * </p>
 *
 */
public interface SysMoveDataTaskService extends IService<SysMoveDataTask> {

	/**
	 * 查询模块表配置信息
	 *
	 * @param moduleType 模块类型
	 * @return
	 */
	List<SysMoveDataTask> getModuleTaskList(Integer moduleType);

	/**
	 * 处理历史数据
	 *
	 * @param sysMoveDataTask 转移任务表信息
	 */
	void processData(SysMoveDataTask sysMoveDataTask);
}
