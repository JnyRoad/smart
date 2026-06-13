package com.tce.smart.platform.service.manage;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.manage.EditEhrSetUpReqDTO;
import com.tce.smart.platform.core.entity.manage.SmtEhrSetUp;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-27 10:45:36
 */
public interface SmtEhrSetUpService extends IService<SmtEhrSetUp> {

	/**
	 * 新增或编辑设置
	 * @param reqDTO
	 * @return
	 */
	Boolean edit(EditEhrSetUpReqDTO reqDTO);

	/**
	 * 自动签收定时任务
	 */
	void autoSignTask();

	/**
	 * 每月定时发送提醒
	 */
	void sendMessage();

}
