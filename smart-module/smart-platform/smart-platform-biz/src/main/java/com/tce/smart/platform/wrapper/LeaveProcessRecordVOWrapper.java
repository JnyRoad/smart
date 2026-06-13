package com.tce.smart.platform.wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.model.ProcessRecordFlow;
import com.tce.smart.platform.core.vo.LeaveRrocessRecordVO;
import com.tce.smart.platform.core.mapper.SmtProcessRecordMapper;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.LeaveApplicationEnum;
import com.tce.smart.tool.enums.NodeStatusEnum;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
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
public class LeaveProcessRecordVOWrapper extends BaseWrapper<SmtLeaveApplication, LeaveRrocessRecordVO> {
    private final RemoteDictService remoteDictService;
    private final SmtProcessRecordMapper processRecordMapper;
    @Override
    protected LeaveRrocessRecordVO warp(SmtLeaveApplication leaveApplication) throws IOException {
        LeaveRrocessRecordVO leaveRrocessRecordVO = new LeaveRrocessRecordVO();
        Result<SysDict> sysDict = remoteDictService.findByValue(DictConstants.LEAVE_APPLICATION_REASON, leaveApplication.getLeaveReason()+"", SecurityConstants.FROM_IN);
        if(ObjectUtil.isNotNull(sysDict) && ObjectUtil.isNotNull(sysDict.getData())){
            leaveRrocessRecordVO.setDismissionReasonDesc(sysDict.getData().getLabel());
        }
        sysDict = remoteDictService.findByValue(DictConstants.LEAVE_APPLICATION_TYPE, leaveApplication.getLeaveType()+"", SecurityConstants.FROM_IN);
        if(ObjectUtil.isNotNull(sysDict) && ObjectUtil.isNotNull(sysDict.getData())){
            leaveRrocessRecordVO.setDismissionTypeDesc(sysDict.getData().getLabel());
        }
        leaveRrocessRecordVO.setBuName(leaveApplication.getCompName());
        leaveRrocessRecordVO.setDepName(leaveApplication.getDepName());
        leaveRrocessRecordVO.setEmployeeId(leaveApplication.getBadge());
        leaveRrocessRecordVO.setEmployeeName(leaveApplication.getName());
        leaveRrocessRecordVO.setJobName(leaveApplication.getJobName());
        leaveRrocessRecordVO.setRestDatCount(leaveApplication.getYearHoliday());
        leaveRrocessRecordVO.setEntryTime(DateUtil.format(leaveApplication.getJoinTime(), "yyyy-MM-dd"));
        leaveRrocessRecordVO.setDismissionDate(DateUtil.format(leaveApplication.getLeaveTime(), "yyyy-MM-dd"));
        leaveRrocessRecordVO.setApproveState(leaveApplication.getApproveStatus());
        leaveRrocessRecordVO.setApproveStateDesc(LeaveApplicationEnum.desc(leaveApplication.getApproveStatus()));
        List<SmtProcessRecord> processRecordList = processRecordMapper.getProcessRecord(leaveApplication.getProcessId());
        List<ProcessRecordFlow>  recordList = new ArrayList<>();
        for (SmtProcessRecord processRecord : processRecordList) {
            ProcessRecordFlow processRecordVO = new ProcessRecordFlow();
            processRecordVO.setNodeName(processRecord.getNodeName());
            processRecordVO.setNodeState(processRecord.getStatus());
            processRecordVO.setProcessDesc(NodeStatusEnum.desc(processRecord.getStatus()));
            processRecordVO.setProcessDate(DateUtil.format(processRecord.getRecordDate(), "yyyy-MM-dd"));
            recordList.add(processRecordVO);
        }
        leaveRrocessRecordVO.setRecordList(recordList);
        return leaveRrocessRecordVO;
    }
}
