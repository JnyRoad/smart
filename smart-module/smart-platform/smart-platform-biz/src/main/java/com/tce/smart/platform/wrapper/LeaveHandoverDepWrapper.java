package com.tce.smart.platform.wrapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tce.smart.platform.core.entity.SmtLbejConfig;
import com.tce.smart.platform.service.SmtLbejConfigService;
import org.springframework.stereotype.Component;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtLeaveHandover;
import com.tce.smart.platform.core.model.LeaveHandoverDep;
import com.tce.smart.platform.core.model.LeaveHandoverItemJjrVO;
import com.tce.smart.platform.core.mapper.SmtLeaveHandoverMapper;
import com.tce.smart.tool.constant.LeaveHandoverConstants;

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
public class LeaveHandoverDepWrapper extends BaseWrapper<SmtLeaveHandover, LeaveHandoverDep> {
    private final SmtLeaveHandoverMapper leaveHandoverMapper;
    @Override
    protected LeaveHandoverDep warp(SmtLeaveHandover leaveHandover) throws IOException {
        LeaveHandoverDep leaveHandoverDep = new LeaveHandoverDep();
        leaveHandoverDep.setDeptName(leaveHandover.getZrdepName());
        List<SmtLeaveHandover> leaveHandoverList = leaveHandoverMapper.getLeaveHandoverItem(leaveHandover.getApplicationId(),leaveHandover.getJjr(),LeaveHandoverConstants.TRUE,null,leaveHandover.getZrdep());
        List<LeaveHandoverItemJjrVO> handItem = new ArrayList<>();
        LeaveHandoverItemJjrVO leaveHandoverDepItemVO = null;
        for (SmtLeaveHandover smtLeaveHandover : leaveHandoverList) {
            leaveHandoverDepItemVO = new LeaveHandoverItemJjrVO();
            leaveHandoverDepItemVO.setItemAmt(ObjectUtil.isNull(smtLeaveHandover.getJe())? 0:smtLeaveHandover.getJe());
            leaveHandoverDepItemVO.setItemDesc(StrUtil.isBlank(smtLeaveHandover.getJjRemark())?"":smtLeaveHandover.getJjRemark());
            leaveHandoverDepItemVO.setItemId(smtLeaveHandover.getId());
            leaveHandoverDepItemVO.setItemName(smtLeaveHandover.getJjItem());
            leaveHandoverDepItemVO.setItemState(smtLeaveHandover.getJjClosed());
            handItem.add(leaveHandoverDepItemVO);
        }
        leaveHandoverDep.setHandItem(handItem);
        return leaveHandoverDep;
    }
}
