package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.entity.SmtLeaveHandover;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.vo.LeaveApplicationRecordDetailVO;
import com.tce.smart.platform.core.vo.LeaveApplicationRecordVO;
import com.tce.smart.platform.service.SmtLeaveHandoverService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.constant.DictConstants;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static cn.hutool.core.util.ObjectUtil.isNotNull;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class LeaveApplicationRecordDetailWrapper extends BaseWrapper<SmtLeaveApplication, LeaveApplicationRecordDetailVO> {
    private final RemoteDictService remoteDictService;
    private final SmtLeaveHandoverService smtLeaveHandoverService;
    private final SmtStaffService  staffService;
    @Override
    protected LeaveApplicationRecordDetailVO warp(SmtLeaveApplication leaveApplication) throws IOException {
		LeaveApplicationRecordDetailVO leaveApplicationRecordDetailVO = new LeaveApplicationRecordDetailVO();
		BeanUtil.copyProperties(leaveApplication,leaveApplicationRecordDetailVO);

        Result<SysDict> sysDict = remoteDictService.findByValue(DictConstants.LEAVE_APPLICATION_REASON, leaveApplication.getLeaveReason()+"", SecurityConstants.FROM_IN);
        if(isNotNull(sysDict) && isNotNull(sysDict.getData())){
			leaveApplicationRecordDetailVO.setLeaveReasonDesc(sysDict.getData().getLabel());
        }
        sysDict = remoteDictService.findByValue(DictConstants.LEAVE_APPLICATION_TYPE, leaveApplication.getLeaveType()+"", SecurityConstants.FROM_IN);
        if(isNotNull(sysDict) && isNotNull(sysDict.getData())){
			leaveApplicationRecordDetailVO.setLeaveTypeDesc(sysDict.getData().getLabel());
        }
		List<SmtLeaveHandover> items = smtLeaveHandoverService.list(Wrappers.<SmtLeaveHandover>query().lambda().eq(SmtLeaveHandover::getApplicationId,leaveApplication.getId()));
		leaveApplicationRecordDetailVO.setItems(items);
		SmtStaff staff = staffService.getOne(Wrappers.<SmtStaff> query().lambda()
				.eq(SmtStaff::getBadge, leaveApplication.getBadge()));
		leaveApplicationRecordDetailVO.setCompName(staff.getCompName());
		leaveApplicationRecordDetailVO.setDepName(staff.getDepName());
		leaveApplicationRecordDetailVO.setJobName(staff.getJobName());
        return leaveApplicationRecordDetailVO;
    }
}
