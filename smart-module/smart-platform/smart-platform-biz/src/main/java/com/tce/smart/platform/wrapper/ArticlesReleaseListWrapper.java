package com.tce.smart.platform.wrapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.ArticlesReleaseListRespDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.service.SmtApprovalNodeService;
import com.tce.smart.platform.core.vo.StaffInfoVO;
import com.tce.smart.platform.emun.ReleaseItemEnum;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import com.tce.smart.tool.enums.ArticlesReleaseStatusEnum;
import com.tce.smart.tool.enums.ArticlesReleaseTypeEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-28 17:43
 */
@Component
@AllArgsConstructor
public class ArticlesReleaseListWrapper extends BaseWrapper<SmtArticlesRelease, ArticlesReleaseListRespDTO> {
	private final SmtParkService smtParkService;
	private final SmtStaffService smtStaffService;
	private final ImageService imageService;
	private final SmtProcessRecordService processRecordService;
	private final SmtApprovalNodeService approvalNodeService;
	private final ApproveListService approveListService;
	private final SmtDormitoryStaffService dormitoryStaffService;

	@Override
	protected ArticlesReleaseListRespDTO warp(SmtArticlesRelease releaseModel) throws IOException {
		SmtPark park = smtParkService.getById(releaseModel.getParkId());
		ArticlesReleaseListRespDTO articlesReleaseListRespDTO = BeanUtils.transform(ArticlesReleaseListRespDTO.class, releaseModel);
		articlesReleaseListRespDTO.setArticlesTypeName(ArticlesReleaseTypeEnum.desc(releaseModel.getArticlesType()));
		articlesReleaseListRespDTO.setParkName(park == null ? "-" : park.getParkName());
		articlesReleaseListRespDTO.setStatusName(ArticlesReleaseStatusEnum.desc(releaseModel.getStatus()));
		if(StringUtils.isEmpty(releaseModel.getPhone())){
			StaffInfoVO vo = smtStaffService.getBaseinfoById(releaseModel.getBadge());
			articlesReleaseListRespDTO.setCompName(vo.getSmtStaff().getCompName());
			articlesReleaseListRespDTO.setDeptName(vo.getSmtStaff().getDepName());
			articlesReleaseListRespDTO.setFacePic(imageService.buildImageUrl(vo.getFacePic()));
		}else{
			StaffInfoVO vo = smtStaffService.getSmtStaffInfoByPhone(releaseModel.getPhone(),releaseModel.getCarrier());
			if(StringUtils.isNotEmpty(vo.getFacePic())){
				articlesReleaseListRespDTO.setFacePic(vo.getFacePic());
			}

			if(Objects.nonNull(vo.getSmtStaff())){
				articlesReleaseListRespDTO.setCompName(vo.getSmtStaff().getCompName());
				articlesReleaseListRespDTO.setDeptName(vo.getSmtStaff().getDepName());
			}else{
				articlesReleaseListRespDTO.setCompName("-");
				articlesReleaseListRespDTO.setDeptName("-");
			}
		}

		SmtDormitoryStaff dormitoryStaff = dormitoryStaffService.getDormitoryStaff(releaseModel.getDormitoryId(), releaseModel.getFloorId(), releaseModel.getRoomId(), releaseModel.getBedId());
		if (dormitoryStaff != null) {
			articlesReleaseListRespDTO.setRoomInfo(dormitoryStaff.getDormitoryName()+dormitoryStaff.getRoomName());
		}

		articlesReleaseListRespDTO.setExpire(false);
		if(Objects.nonNull(releaseModel.getPlannedDepartureTime())){
			if(System.currentTimeMillis() - releaseModel.getPlannedDepartureTime().getTime() > 0){
				articlesReleaseListRespDTO.setExpire(true);
			}
		}
		articlesReleaseListRespDTO.setReleaseItemDesc(ReleaseItemEnum.getByCode(releaseModel.getReleaseItem()));
		articlesReleaseListRespDTO.setReleaseStatus(StrUtil.isNotBlank(releaseModel.getSecurityStaff()) ? "已放行" : "未放行");
		articlesReleaseListRespDTO.setBackStatus(Objects.nonNull(releaseModel.getBackTime()) ? "已确认" : "未确认");
		if (StrUtil.isNotBlank(releaseModel.getProcessId())) {
			if (ArticlesReleaseStatusEnum.APPROVED.getCode().equals(releaseModel.getStatus())) {
				articlesReleaseListRespDTO.setOaNode("已归档");
			} else {
				List<SmtProcessRecord> selectList = processRecordService.list(Wrappers.<SmtProcessRecord> query().lambda().eq(SmtProcessRecord::getProcessId, releaseModel.getProcessId()).orderByDesc(SmtProcessRecord::getRecordDate));
				if(selectList.size()>0) {
					//查询流程的最新的状态数据
					String nodeName = selectList.get(0).getNodeName();
					if(StrUtil.isEmpty(nodeName)) {
						articlesReleaseListRespDTO.setOaNode("");
					}else {
						String[] nodeNames = nodeName.split(" ");
						if(nodeNames.length == 2) {
							articlesReleaseListRespDTO.setOaNode(nodeNames[1]);
						}
					}
				}
			}
		} else if (ArticlesReleaseTypeEnum.DORMITORY.getCode().equals(releaseModel.getArticlesType())) {
			// 生活区物品放行
			if (ArticlesReleaseStatusEnum.APPROVED.getCode().equals(releaseModel.getStatus())) {
				articlesReleaseListRespDTO.setOaNode("保安确认(" + (StrUtil.isBlank(releaseModel.getSecurityStaff()) ? "待审批)" : "已放行)"));
			} else if (ArticlesReleaseStatusEnum.APPROVAL_FAILED.getCode().equals(releaseModel.getStatus())) {
				List<ApproveList> nodeList = approveListService.getByStatus(ApproveListStateEnum.REFUSE.getCode(), releaseModel.getId().toString(), null);
				SmtApprovalNode node = approvalNodeService.getById(nodeList.get(0).getNodeId());
				articlesReleaseListRespDTO.setOaNode(node == null ? "" : node.getName()+"(已拒绝)");
			} else if (ArticlesReleaseStatusEnum.PENDING_APPROVAL.getCode().equals(releaseModel.getStatus())) {
				List<ApproveList> nodeList = approveListService.getByStatus(ApproveListStateEnum.PENDING.getCode(), releaseModel.getId().toString(), null);
				if (CollUtil.isEmpty(nodeList)) {
					// 不存在审批节点，表示已审批完成
					articlesReleaseListRespDTO.setOaNode("保安确认(" + (StrUtil.isBlank(releaseModel.getSecurityStaff()) ? "待审批)" : "已放行)"));
				} else {
					SmtApprovalNode node = approvalNodeService.getById(nodeList.get(0).getNodeId());
					articlesReleaseListRespDTO.setOaNode(node == null ? "" : node.getName()+"(待审批)");
				}
			} else {
				articlesReleaseListRespDTO.setOaNode(ArticlesReleaseStatusEnum.desc(releaseModel.getStatus()));
			}
		}

		return articlesReleaseListRespDTO;
	}
}
