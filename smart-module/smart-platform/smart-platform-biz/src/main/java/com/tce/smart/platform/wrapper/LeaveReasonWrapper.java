package com.tce.smart.platform.wrapper;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.model.LeaveReason;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class LeaveReasonWrapper extends BaseWrapper<SysDict, LeaveReason> {

    @Override
    protected LeaveReason warp(SysDict SysDict) throws IOException {
        LeaveReason reason = new LeaveReason();
        reason.setReasonCode(Integer.parseInt(SysDict.getValue()));
        reason.setReasonName(SysDict.getLabel());
        return reason;
    }
}
