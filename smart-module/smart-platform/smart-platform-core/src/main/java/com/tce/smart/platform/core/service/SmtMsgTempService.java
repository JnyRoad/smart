package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.MsgTempDTO;
import com.tce.smart.platform.core.entity.SmtMsgTemp;

import java.util.List;

/**
 * @author Li.JiaJun
 * @since 2022/9/26 14:30
 */
public interface SmtMsgTempService extends IService<SmtMsgTemp> {

	/**
	 * 新增/更新模板
	 *
	 * @param dto
	 * @return
	 */
	Boolean save(MsgTempDTO dto);

	/**
	 * 模板列表
	 *
	 * @return
	 */
	List<SmtMsgTemp> getList();

	/**
	 * 通过园区id获取工号
	 *
	 * @param parkId
	 * @return
	 */
	List<String> getBadgeByParkId(Integer parkId);
}
