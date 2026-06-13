package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.service.SmtApprovalNodeService;
import com.tce.smart.platform.core.service.SmtLeaveApplicationService;
import com.tce.smart.platform.core.vo.ApproveItemVO;
import com.tce.smart.platform.core.vo.ApproveListVO;
import com.tce.smart.platform.service.SmtArticlesReleaseService;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import com.tce.smart.platform.service.SmtVisitorService;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import com.tce.smart.tool.enums.ArticlesReleaseTypeEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class ApproveListWrapper extends BaseWrapper<ApproveList, ApproveListVO> {
    private final SmtLeaveApplicationService leaveApplicationService;
    private final SmtVisitorService visitorService;
    private final SmtArticlesReleaseService articlesReleaseService;
    private final SmtDormitoryStaffService dormitoryStaffService;
    private final SmtApprovalNodeService approvalNodeService;

    @Override
    protected ApproveListVO warp(ApproveList approveList) throws IOException {
        ApproveListVO approveListVO = new ApproveListVO();
        BeanUtil.copyProperties(approveList, approveListVO);
        approveListVO.setApproveId(approveList.getBusinessId());
        approveListVO.setApproveDesc(ApproveListStateEnum.desc(approveList.getApproveState()));
        List<ApproveItemVO> itemList = new ArrayList<ApproveItemVO>();
        if(approveList.getApproveType().equals(ApproveListTypeConstants.LEAVE)){
            SmtLeaveApplication leaveApplication = leaveApplicationService.getOne(Wrappers.<SmtLeaveApplication>query().lambda().eq(SmtLeaveApplication::getProcessId, approveList.getBusinessId()));
            ApproveItemVO approveItemVO = new ApproveItemVO();
            approveItemVO.setItemName(ApproveListTypeConstants.LEAVE_DATE);
            approveItemVO.setItemValue(DateUtil.formatDate(leaveApplication.getLeaveTime()));
            itemList.add(approveItemVO);
        }else if(approveList.getApproveType().equals(ApproveListTypeConstants.VISITOR)) {
            SmtVisitor visitor = visitorService.getById(approveList.getBusinessId());
            ApproveItemVO startApproveItemVO = new ApproveItemVO();
            startApproveItemVO.setItemName(ApproveListTypeConstants.START_TIME);
            startApproveItemVO.setItemValue(ObjectUtil.isNotNull(visitor) ?DateUtil.formatDateTime(visitor.getStartTime()):"");
            itemList.add(startApproveItemVO);

            ApproveItemVO endApproveItemVO = new ApproveItemVO();
            endApproveItemVO.setItemName(ApproveListTypeConstants.END_TIME);
            endApproveItemVO.setItemValue(ObjectUtil.isNotNull(visitor) ?DateUtil.formatDateTime(visitor.getEndTime()):"");
            itemList.add(endApproveItemVO);

			approveListVO.setStatus(ObjectUtil.isNotNull(visitor) && System.currentTimeMillis() - visitor.getEndTime().getTime() > 0?1:0);
        } else if (approveList.getApproveType().equals(ApproveListTypeConstants.ARTICLE)) {
			SmtArticlesRelease release = articlesReleaseService.getById(approveList.getBusinessId());
			if (Objects.nonNull(release)) {
				approveListVO.setArticlesTypeDesc(ArticlesReleaseTypeEnum.desc(release.getArticlesType()));
				approveListVO.setArticleName(release.getArticlesDesc());
				approveListVO.setCarrier(release.getCarrier());
				SmtDormitoryStaff dormitoryStaff = dormitoryStaffService.getDormitoryStaff(release.getDormitoryId(), release.getFloorId(), release.getRoomId(), release.getBedId());
				approveListVO.setRoomInfo(dormitoryStaff == null ? null : dormitoryStaff.getDormitoryName() + dormitoryStaff.getRoomName());
			}
			SmtApprovalNode approvalNode = approvalNodeService.getById(approveList.getNodeId());
			approveListVO.setApproveNodeDesc(approvalNode != null ? approvalNode.getName() + "(待审批)" : "");
		}
        approveListVO.setApproveItem(itemList);
        return approveListVO;
    }
}
