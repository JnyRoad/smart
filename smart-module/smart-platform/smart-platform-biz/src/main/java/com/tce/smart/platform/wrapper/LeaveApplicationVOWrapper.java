package com.tce.smart.platform.wrapper;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.data.convert.Jsr310Converters;
import org.springframework.stereotype.Component;

import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.vo.LeaveApplicationVO;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.LeaveApplicationStatusEnum;

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
public class LeaveApplicationVOWrapper extends BaseWrapper<SmtLeaveApplication, LeaveApplicationVO> {
    private final RemoteDictService remoteDictService;
    @Override
    protected LeaveApplicationVO warp(SmtLeaveApplication leaveApplication) throws IOException {
        LeaveApplicationVO leaveApplicationVO = new LeaveApplicationVO();
        Result<SysDict> sysDict = remoteDictService.findByValue(DictConstants.LEAVE_APPLICATION_REASON, leaveApplication.getLeaveReason()+"", SecurityConstants.FROM_IN);
        if(ObjectUtil.isNotNull(sysDict) && ObjectUtil.isNotNull(sysDict.getData())){
            leaveApplicationVO.setDismissionReasonDesc(sysDict.getData().getLabel());
        }
        sysDict = remoteDictService.findByValue(DictConstants.LEAVE_APPLICATION_TYPE, leaveApplication.getLeaveType()+"", SecurityConstants.FROM_IN);
        if(ObjectUtil.isNotNull(sysDict) && ObjectUtil.isNotNull(sysDict.getData())){
            leaveApplicationVO.setDismissionTypeDesc(sysDict.getData().getLabel());
        }
        leaveApplicationVO.setBuName(leaveApplication.getCompName());
        leaveApplicationVO.setDepName(leaveApplication.getDepName());
        leaveApplicationVO.setEmployeeId(leaveApplication.getBadge());
        leaveApplicationVO.setEmployeeName(leaveApplication.getName());
        leaveApplicationVO.setJobName(leaveApplication.getJobName());
        leaveApplicationVO.setRestDatCount(leaveApplication.getYearHoliday());
        leaveApplicationVO.setEntryTime(DateUtil.format(leaveApplication.getJoinTime(), "yyyy-MM-dd"));
        leaveApplicationVO.setDismissionDate(DateUtil.format(leaveApplication.getLeaveTime(), "yyyy-MM-dd"));
        leaveApplicationVO.setApproveState(leaveApplication.getApproveStatus());
        leaveApplicationVO.setApproveStateDesc(LeaveApplicationStatusEnum.desc(leaveApplication.getApproveStatus()));
        return leaveApplicationVO;
    }
}
