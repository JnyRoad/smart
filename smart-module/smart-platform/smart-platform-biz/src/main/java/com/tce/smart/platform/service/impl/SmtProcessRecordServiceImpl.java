package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.event.SmartEventPublisher;
/*import com.tce.smart.common.core.event.SmartEventPublisher;*/
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.mapper.SmtProcessRecordMapper;
import com.tce.smart.platform.service.SmtProcessRecordService;

import cn.hutool.core.collection.CollectionUtil;
import com.tce.smart.tool.enums.ApplicationEnum;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 流程审批
 *
 * @author 梁圆
 * @date 2019-05-15 11:34:54
 */
@Slf4j
@Service
public class SmtProcessRecordServiceImpl extends ServiceImpl<SmtProcessRecordMapper, SmtProcessRecord> implements SmtProcessRecordService {
    @Autowired
    private SmartEventPublisher smartEventPublisher;
    @Override
    @Transactional
    public void saveProcessRecord(WorkFlowAO workFlowAO) {
//        final String processId = workFlowAO.getRequestid();
//        List<WorkFlowRecordAO> flowRecords = workFlowAO.getFlowRecord();
//        if(CollectionUtils.isNotEmpty(flowRecords)){
//            flowRecords.forEach(flowRecord->processRecord(processId, flowRecord));
//        }
        smartEventPublisher.publish(workFlowAO);
    }
//    private void processRecord(String processId, WorkFlowRecordAO process){
//    	if(!process.getLogtype().equals(NodeStatusEnum.INTERVENTION.getCode())) {
//    		//1、判重
//            SmtProcessRecord smtProcessRecord = this.baseMapper.selectOne(Wrappers.<SmtProcessRecord> query().lambda()
//                    .eq(SmtProcessRecord::getProcessId, processId)
//                    .eq(SmtProcessRecord::getNodeName, process.getNodename())
////                    .eq(SmtProcessRecord::getStatementStatus, process.getLogtype())
//                    .eq(SmtProcessRecord::getStaffName, process.getLastname()));
//            if(Objects.nonNull(smtProcessRecord)){
//                log.info("流程记录重复，不写入数据库：processId = {} , nodeName = {}。", processId, process.getNodename());
//                return;
//            }
//            //2、入库
//            SmtProcessRecord processRecord = new SmtProcessRecord();
//            processRecord.setStaffName(process.getLastname());
//            processRecord.setStaffBadge(process.getWorkcode());
//            processRecord.setProcessId(processId);
//            processRecord.setStatementStatus(process.getLogtype());
//            processRecord.setRemark(process.getRemark());
//            processRecord.setRecordDate(DateUtils.parseDateTime(process.getOperatedate() +" "+ process.getOperatetime()));
//            processRecord.setCreatTime(DateUtils.date());
//            processRecord.setNodeName(process.getNodename());
//            processRecord.insert();
//    	}
//    }
	@Override
	public boolean getHandoverRecord(String processId, String staffBadge) {
		List<SmtProcessRecord> list = this.baseMapper.getHandoverRecord(processId, staffBadge);
		return CollectionUtil.isNotEmpty(list) && list.size() > 0;
	}

	@Override
	public String getStatus(String processId) {
		List<SmtProcessRecord> selectList = this.list(Wrappers.<SmtProcessRecord> query().lambda().eq(SmtProcessRecord::getProcessId, processId).orderByDesc(SmtProcessRecord::getRecordDate));
		if(selectList.size()>0) {
			//查询流程的最新的状态数据
			String status = selectList.get(0).getStatus();
			if(status.equals(ApplicationEnum.RECORD_STATUS_e.getCode()) || status.equals(ApplicationEnum.RECORD_STATUS_0.getCode())) {
				return ApplicationEnum.RECORD_STATUS_11.getDesc();
			}else if(status.equals(ApplicationEnum.RECORD_STATUS_3.getCode())){
				return ApplicationEnum.RECORD_STATUS_12.getDesc();
			}else if(status.equals(ApplicationEnum.RECORD_STATUS_13.getCode())){
				return ApplicationEnum.RECORD_STATUS_13.getDesc();
			}else {
				return ApplicationEnum.RECORD_STATUS_10.getDesc();
			}
		}
		return null;
	}
}
