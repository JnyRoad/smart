package com.tce.smart.platform.wrapper;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.vo.LeaveHandoverApplicationVO;
import com.tce.smart.platform.core.mapper.SmtLeaveHandoverMapper;
import com.tce.smart.tool.constant.DictConstants;

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
public class LeaveHandoverApplicationVOWrapper extends BaseWrapper<SmtLeaveApplication, LeaveHandoverApplicationVO> {
    private final RemoteDictService remoteDictService;
    private final SmtLeaveHandoverMapper leaveHandoverMapper;
    @Override
    protected LeaveHandoverApplicationVO warp(SmtLeaveApplication leaveApplication) throws IOException {
        LeaveHandoverApplicationVO leaveHandoverApplicationVO = new LeaveHandoverApplicationVO();
        Result<SysDict> sysDict = remoteDictService.findByValue(DictConstants.LEAVE_APPLICATION_REASON, leaveApplication.getLeaveReason()+"", SecurityConstants.FROM_IN);
        if(ObjectUtil.isNotNull(sysDict) && ObjectUtil.isNotNull(sysDict.getData())){
            leaveHandoverApplicationVO.setDismissionReasonDesc(sysDict.getData().getLabel());
        }
        sysDict = remoteDictService.findByValue(DictConstants.LEAVE_APPLICATION_TYPE, leaveApplication.getLeaveType()+"", SecurityConstants.FROM_IN);
        if(ObjectUtil.isNotNull(sysDict) && ObjectUtil.isNotNull(sysDict.getData())){
            leaveHandoverApplicationVO.setDismissionTypeDesc(sysDict.getData().getLabel());
        }
        leaveHandoverApplicationVO.setProcessId(leaveApplication.getProcessId());
        leaveHandoverApplicationVO.setBuName(leaveApplication.getCompName());
        leaveHandoverApplicationVO.setDepName(leaveApplication.getDepName());
        leaveHandoverApplicationVO.setEmployeeId(leaveApplication.getBadge());
        leaveHandoverApplicationVO.setEmployeeName(leaveApplication.getName());
        leaveHandoverApplicationVO.setJobName(leaveApplication.getJobName());
        leaveHandoverApplicationVO.setRestDatCount(leaveApplication.getYearHoliday());
        leaveHandoverApplicationVO.setEntryTime(DateUtil.format(leaveApplication.getJoinTime(), "yyyy-MM-dd"));
        leaveHandoverApplicationVO.setDismissionDate(DateUtil.format(leaveApplication.getLeaveTime(), "yyyy-MM-dd"));
        leaveHandoverApplicationVO.setApproveStatus(leaveApplication.getApproveStatus());
        return leaveHandoverApplicationVO;
    }
}
