package com.tce.smart.platform.wrapper;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.model.LeaveReason;
import com.tce.smart.platform.core.model.LeaveType;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class LeaveTypeWrapper extends BaseWrapper<SysDict, LeaveType> {

    @Override
    protected LeaveType warp(SysDict SysDict) throws IOException {
        LeaveType type = new LeaveType();
        type.setTypeCode(Integer.parseInt(SysDict.getValue()));
        type.setTypeName(SysDict.getLabel());
        return type;
    }
}
