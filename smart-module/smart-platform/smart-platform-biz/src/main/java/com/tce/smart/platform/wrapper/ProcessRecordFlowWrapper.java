package com.tce.smart.platform.wrapper;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.model.ProcessRecordFlow;
import com.tce.smart.tool.enums.NodeStatusEnum;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class ProcessRecordFlowWrapper extends BaseWrapper<SmtProcessRecord, ProcessRecordFlow> {
    @Override
    protected ProcessRecordFlow warp(SmtProcessRecord processRecord) throws IOException {
        ProcessRecordFlow processRecordVO = new ProcessRecordFlow();
        processRecordVO.setNodeName(processRecord.getNodeName());
        if(StrUtil.isEmpty(processRecord.getNodeName())) {
	processRecordVO.setNodeName("");
        }else {
	String[] nodeNames = processRecord.getNodeName().split(" ");
	if(nodeNames.length == 2) {
		processRecordVO.setNodeName(nodeNames[1]);
	}
        }
        processRecordVO.setNodeState(processRecord.getStatus());
        processRecordVO.setProcessDesc(NodeStatusEnum.nodeStatus(processRecord.getStatus()).getDesc());
        processRecordVO.setProcessDate(ObjectUtil.isNull(processRecord.getRecordDate()) ? "" :DateUtil.format(processRecord.getRecordDate(), "yyyy-MM-dd"));
        processRecordVO.setRemark(StrUtil.isBlank(processRecord.getRemark()) ? "" : processRecord.getRemark());
		processRecordVO.setStaffName(processRecord.getStaffName());
        return processRecordVO;
    }
}
