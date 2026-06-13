package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.OperateLogQueryDTO;
import com.tce.smart.platform.core.dto.OperateLogDTO;
import com.tce.smart.platform.core.entity.SmtOperateLog;

import java.util.List;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 14:08
 */
public interface SmtOperateLogService extends IService<SmtOperateLog> {

	/**
	 * 通过操作目标id分页获取操作记录
	 *
	 * @param page
	 * @param dto
	 * @return
	 */
	IPage<SmtOperateLog> getPage(Page page, OperateLogQueryDTO dto);

	/**
	 * 通过操作目标id获取操作记录
	 *
	 * @param dto
	 * @return
	 */
	List<SmtOperateLog> getList(OperateLogQueryDTO dto);

	/**
	 * 新增操作日志
	 *
	 * @param operateLog
	 * @return
	 */
	Boolean addLog(OperateLogDTO operateLog);
}
