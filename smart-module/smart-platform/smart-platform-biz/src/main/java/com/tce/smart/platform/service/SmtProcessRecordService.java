package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.SmtProcessRecord;

/**
 * 流程审批表
 *
 * @author 梁圆
 * @date 2019-05-15 11:34:54
 */
public interface SmtProcessRecordService extends IService<SmtProcessRecord> {

    void saveProcessRecord(WorkFlowAO workFlowAO);


    boolean getHandoverRecord(String processId,String staffBadge);

	/**
	 * 返回流程状态
	 * @param processId
	 * @return
	 */
    String getStatus(String processId);
}
