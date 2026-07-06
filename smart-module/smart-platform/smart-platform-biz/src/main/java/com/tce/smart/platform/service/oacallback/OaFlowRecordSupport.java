package com.tce.smart.platform.service.oacallback;

import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.platform.core.ao.WorkFlowRecordAO;
import com.tce.smart.tool.enums.NodeStatusEnum;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 标准"回退判断 + 过程记录循环"复用组件：等价监听器各分支的通用循环。
 * 特殊分支（外宿补贴撤销按最后节点判断、物品放行按 status 字段判断）不使用本组件，保留各自原逻辑。
 */
@Component
public class OaFlowRecordSupport {

	private final ProcessRecordWriter processRecordWriter;

	public OaFlowRecordSupport(ProcessRecordWriter processRecordWriter) {
		this.processRecordWriter = processRecordWriter;
	}

	/**
	 * 循环写入过程记录并检测回退。
	 * @return true=未回退（审批通过路径）；false=存在退回节点
	 */
	public boolean processAndDetectReturn(String processId, List<WorkFlowRecordAO> flowRecords) {
		boolean flag = true;
		if (CollectionUtils.isNotEmpty(flowRecords)) {
			for (WorkFlowRecordAO flowRecord : flowRecords) {
				if (flag) {
					flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
				}
				processRecordWriter.write(processId, ProcessRecordItem.fromCallback(flowRecord));
			}
		}
		return flag;
	}
}
