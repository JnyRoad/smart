package com.tce.smart.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.admin.api.entity.SysLog;
import com.tce.smart.admin.api.vo.PreLogVO;

import java.util.List;

/**
 * <p>
 * 日志表 服务类
 * </p>
 *
 */
public interface SysLogService extends IService<SysLog> {


	/**
	 * 批量插入前端错误日志
	 *
	 * @param preLogVOList 日志信息
	 * @return true/false
	 */
	Boolean saveBatchLogs(List<PreLogVO> preLogVOList);
}
