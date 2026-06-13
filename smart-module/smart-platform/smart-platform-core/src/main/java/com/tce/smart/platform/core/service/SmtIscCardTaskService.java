package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.isc.IscCardTaskPageReqDTO;
import com.tce.smart.platform.core.entity.SmtIscCardTask;
import com.tce.smart.platform.core.vo.IscCardTaskPageVO;

public interface SmtIscCardTaskService extends IService<SmtIscCardTask> {

	boolean createAddStaffCardTask(Long staffId, String badge, Integer parkId, String cardNo);

	boolean createDeleteStaffCardTask(Long staffId, String badge, Integer parkId, String cardNo);

	boolean isCurrentStaffCardAddTask(SmtIscCardTask task);

	boolean updateDoingTask(SmtIscCardTask task, String expectedActiveKey, String expectedLeaseToken);

	IPage<SmtIscCardTask> getPendingTasks(Page page, long currentTime, int maxRetryTimes);

	IPage<IscCardTaskPageVO> getPage(Page page, IscCardTaskPageReqDTO query);

	boolean markDoing(SmtIscCardTask task, long currentTime, long doingDeadlineTime, int maxRetryTimes);

	/**
	 * 将达到最大重试次数的卡片任务标记为失败，停止自动重试
	 * @return 是否有任务被停止
	 */
	boolean stopExceededRetryCardTasks(int maxRetryTimes, String remark);
}
