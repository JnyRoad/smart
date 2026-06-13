package com.tce.smart.platform.wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtLeaveHandover;
import com.tce.smart.platform.core.model.LeaveHandoverDepJjr;
import com.tce.smart.platform.core.model.LeaveHandoverItemJjrVO;
import com.tce.smart.platform.core.mapper.SmtLeaveHandoverMapper;
import com.tce.smart.tool.enums.LeaveHandoverEnum;

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
public class LeaveHandoverDepJjrWrapper extends BaseWrapper<SmtLeaveHandover, LeaveHandoverDepJjr> {
    private final SmtLeaveHandoverMapper leaveHandoverMapper;
    @Override
    protected LeaveHandoverDepJjr warp(SmtLeaveHandover leaveHandover) throws IOException {
        LeaveHandoverDepJjr leaveHandoverDepJjr = new LeaveHandoverDepJjr();
        leaveHandoverDepJjr.setDeptName(leaveHandover.getZrdepName());
        List<SmtLeaveHandover> leaveHandoverList = leaveHandoverMapper.getLeaveHandoverItem(leaveHandover.getApplicationId(),null,null,null,leaveHandover.getZrdep());
        List<LeaveHandoverItemJjrVO> handItem = new ArrayList<>();
        LeaveHandoverItemJjrVO leaveHandoverItemJjrVO = null;
        for (SmtLeaveHandover smtLeaveHandover : leaveHandoverList) {
            leaveHandoverItemJjrVO = new LeaveHandoverItemJjrVO();
            leaveHandoverItemJjrVO.setItemAmt(ObjectUtil.isNull(smtLeaveHandover.getJe())? Double.valueOf(0):smtLeaveHandover.getJe());
            leaveHandoverItemJjrVO.setItemDesc(LeaveHandoverEnum.desc(smtLeaveHandover.getJjClosed()));
            leaveHandoverItemJjrVO.setItemId(smtLeaveHandover.getId());
            leaveHandoverItemJjrVO.setItemName(smtLeaveHandover.getJjItem());
            leaveHandoverItemJjrVO.setItemState(smtLeaveHandover.getJjClosed());
            leaveHandoverItemJjrVO.setReceiverId(smtLeaveHandover.getJjr());
            leaveHandoverItemJjrVO.setReceiverName(smtLeaveHandover.getJjrName());
            handItem.add(leaveHandoverItemJjrVO);
        }
        leaveHandoverDepJjr.setHandItem(handItem);
        return leaveHandoverDepJjr;
    }
}
