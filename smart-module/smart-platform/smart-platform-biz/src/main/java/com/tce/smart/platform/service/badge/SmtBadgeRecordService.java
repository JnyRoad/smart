package com.tce.smart.platform.service.badge;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.badge.SmtBadgeRecord;

/**
 * 厂牌补领流程表
 *
 * @author fushiping
 * @date 2020-07-07 11:47:27
 */
public interface SmtBadgeRecordService extends IService<SmtBadgeRecord> {

	/**
	 * 新增补领操作记录
	 * @param applyId 补领申请id
	 * @param status 操作状态
	 * @return
	 */
	Boolean insertRecord(String badge, Long applyId, Integer status);

}
