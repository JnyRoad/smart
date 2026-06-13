package com.tce.smart.platform.wrapper;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.model.LeaveRecordList;
import com.tce.smart.platform.core.vo.LeaveRecordVO;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.enums.LeaveApplicationEnum;
import com.tce.smart.tool.enums.NodeStatusEnum;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
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
public class LeaveRecordListWrapper extends BaseWrapper<LeaveRecordVO, LeaveRecordList> {
    @Override
    protected LeaveRecordList warp(LeaveRecordVO LeaveRecordVO) throws IOException {
        LeaveRecordList leaveRecordList = new LeaveRecordList();
        BeanUtil.copyProperties(LeaveRecordVO, leaveRecordList);
        StringBuilder recordDesc = new StringBuilder();
        if(StrUtil.isEmpty(LeaveRecordVO.getNodeName())) {
	LeaveRecordVO.setNodeName("");
        }else {
	String[] nodeNames = LeaveRecordVO.getNodeName().split(" ");
	if(nodeNames.length == 2) {
		LeaveRecordVO.setNodeName(nodeNames[1]);
	}
        }
        recordDesc.append(LeaveRecordVO.getNodeName())
	.append('(')
	.append(NodeStatusEnum.desc(LeaveRecordVO.getNodeState()))
	.append(')');

        leaveRecordList.setRecordDesc(recordDesc.toString());
        leaveRecordList.setDismissionDate(DateUtil.format(LeaveRecordVO.getLeaveTime(), "yyyy-MM-dd"));
        leaveRecordList.setProcessId(LeaveRecordVO.getProcessId());
        leaveRecordList.setRecordDate(DateUtil.format(LeaveRecordVO.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        leaveRecordList.setRecordId(LeaveRecordVO.getId());
        leaveRecordList.setRecordTitle(LeaveRecordVO.getName()+LeaveApplicationEnum.desc(LeaveRecordVO.getLeaveStatus())+ApproveListTypeConstants.LEAVE_TITLE);
        return leaveRecordList;
    }
}
