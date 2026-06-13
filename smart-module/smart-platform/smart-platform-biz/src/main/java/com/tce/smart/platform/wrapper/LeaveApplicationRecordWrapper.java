package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.dto.LeaveApplicationRecordDTO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.vo.LeaveApplicationRecordVO;
import com.tce.smart.platform.core.vo.LeaveApplicationVO;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.LeaveApplicationStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class LeaveApplicationRecordWrapper extends BaseWrapper<SmtLeaveApplication, LeaveApplicationRecordVO> {
    private final RemoteDictService remoteDictService;
    @Override
    protected LeaveApplicationRecordVO warp(SmtLeaveApplication leaveApplication) throws IOException {
		LeaveApplicationRecordVO leaveApplicationRecordVO = new LeaveApplicationRecordVO();
		BeanUtil.copyProperties(leaveApplication,leaveApplicationRecordVO);

        Result<SysDict> sysDict = remoteDictService.findByValue(DictConstants.LEAVE_APPLICATION_REASON, leaveApplication.getLeaveReason()+"", SecurityConstants.FROM_IN);
        if(ObjectUtil.isNotNull(sysDict) && ObjectUtil.isNotNull(sysDict.getData())){
			leaveApplicationRecordVO.setLeaveReasonDesc(sysDict.getData().getLabel());
        }
        sysDict = remoteDictService.findByValue(DictConstants.LEAVE_APPLICATION_TYPE, leaveApplication.getLeaveType()+"", SecurityConstants.FROM_IN);
        if(ObjectUtil.isNotNull(sysDict) && ObjectUtil.isNotNull(sysDict.getData())){
			leaveApplicationRecordVO.setLeaveTypeDesc(sysDict.getData().getLabel());
        }

        return leaveApplicationRecordVO;
    }
}
