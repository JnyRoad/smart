package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.req.ReleaseApplyPersonDetail;
import com.tce.smart.platform.api.dto.req.ReleaseApplyThingDetail;
import com.tce.smart.platform.api.dto.resp.ArticlesReleaseDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.ReleaseApplyMainRespDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApprovalProcessRecordReqDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtProcessRecordMapper;
import com.tce.smart.platform.core.service.SmtApprovalNodeService;
import com.tce.smart.platform.core.service.SmtApprovalService;
import com.tce.smart.platform.core.vo.StaffInfoVO;
import com.tce.smart.platform.emun.*;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.QRCodeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-28 17:43
 */
@Slf4j
@Component
@AllArgsConstructor
public class ArticlesReleaseDetailWrapper extends BaseWrapper<SmtArticlesRelease, ArticlesReleaseDetailRespDTO> {
	private final SmtParkService smtParkService;
	private final SmtStaffService smtStaffService;
	private final ImageService imageService;
	private final SmtDormitoryStaffService smtDormitoryStaffService;
	private final SmtDormitoryStaffHistoryService smtDormitoryStaffHistoryService;
	private final ApproveListService approveListService;
	private final SmtProcessRecordMapper processRecordMapper;
	private final SmtApprovalService approvalService;
	private final SmtArticlesReleaseMainService releaseMainService;
	private final SmtArticlesReleasePersonService releasePersonService;
	private final SmtArticlesReleaseThingService releaseThingService;
	private final SmtApprovalNodeService approvalNodeService;
	private final SmtDormitoryRoomService dormitoryRoomService;
	private final SmtDormitoryFloorService dormitoryFloorService;
	private final SmtDormitoryService smtDormitoryService;


	@Override
	protected ArticlesReleaseDetailRespDTO warp(SmtArticlesRelease smtArticlesRelease) throws IOException {
		SmtPark park = smtParkService.getById(smtArticlesRelease.getParkId());
		ArticlesReleaseDetailRespDTO detailRespDTO = BeanUtils.transform(ArticlesReleaseDetailRespDTO.class, smtArticlesRelease);
		detailRespDTO.setArticlesTypeName(ArticlesReleaseTypeEnum.desc(smtArticlesRelease.getArticlesType()));
		detailRespDTO.setParkName(park.getParkName());
		detailRespDTO.setStatusName(ArticlesReleaseStatusEnum.desc(smtArticlesRelease.getStatus()));
		if(StringUtils.isEmpty(smtArticlesRelease.getPhone())){
			StaffInfoVO vo = smtStaffService.getBaseinfoById(smtArticlesRelease.getBadge());
			detailRespDTO.setCompName(vo.getSmtStaff().getCompName());
			detailRespDTO.setDeptName(vo.getSmtStaff().getDepName());
			detailRespDTO.setFacePic(imageService.buildImageUrl(vo.getFacePic()));
		}else{
			StaffInfoVO vo = smtStaffService.getSmtStaffInfoByPhone(smtArticlesRelease.getPhone(),smtArticlesRelease.getCarrier());
			if(StringUtils.isNotEmpty(vo.getFacePic())){
				detailRespDTO.setFacePic(vo.getFacePic());
			}
			if(Objects.nonNull(vo.getSmtStaff())){
				detailRespDTO.setCompName(vo.getSmtStaff().getCompName());
				detailRespDTO.setDeptName(vo.getSmtStaff().getDepName());
			}else{
				detailRespDTO.setCompName("-");
				detailRespDTO.setDeptName("-");
			}
		}

		if(smtArticlesRelease.getStatus().equals(ArticlesReleaseStatusEnum.APPROVED.getCode())){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", smtArticlesRelease.getId().toString());
			jsonObject.put("type", ApproveListTypeConstants.ARTICLE+"-"+smtArticlesRelease.getArticlesType());
			String qrCode = null;
			try {
				qrCode = QRCodeUtils.wordsCreateQRCode(jsonObject.toString());
			} catch (Exception ex) {
				log.error("生成物品放行二维码异常：", ex);
			}
			detailRespDTO.setQrCodePic(qrCode);
		}

		if(Objects.nonNull(smtArticlesRelease.getOneImg())) detailRespDTO.setOneImg(imageService.buildImageUrl(smtArticlesRelease.getOneImg()));
		if(Objects.nonNull(smtArticlesRelease.getTwoImg())) detailRespDTO.setTwoImg(imageService.buildImageUrl(smtArticlesRelease.getTwoImg()));
		if(Objects.nonNull(smtArticlesRelease.getThreeImg())) detailRespDTO.setThreeImg(imageService.buildImageUrl(smtArticlesRelease.getThreeImg()));
			if(Objects.nonNull(smtArticlesRelease.getDormitoryId()) && Objects.nonNull(smtArticlesRelease.getFloorId())
				&& Objects.nonNull(smtArticlesRelease.getRoomId()) && Objects.nonNull(smtArticlesRelease.getBedId())){
				SmtDormitory dormitory = smtDormitoryService.getById(smtArticlesRelease.getDormitoryId());
				SmtDormitoryRoom smtDormitoryRoom = dormitoryRoomService.getById(smtArticlesRelease.getRoomId());
				SmtDormitoryFloor smtDormitoryFloor = dormitoryFloorService.getById(smtArticlesRelease.getFloorId());
				detailRespDTO.setDormitoryName(dormitory.getDormitoryName());
				detailRespDTO.setBadge(smtArticlesRelease.getBadge());
				detailRespDTO.setName(smtArticlesRelease.getName());
				detailRespDTO.setFloorName(StringUtils.isEmpty(smtDormitoryFloor.getAliasName()) ? smtDormitoryFloor.getFloorName().toString() : smtDormitoryFloor.getAliasName());
				detailRespDTO.setRoomName(StringUtils.isEmpty(smtDormitoryRoom.getAliasName()) ? smtDormitoryRoom.getRoomName().toString() : smtDormitoryRoom.getAliasName());
			}
		detailRespDTO.setExpire(false);
		if(Objects.nonNull(smtArticlesRelease.getPlannedDepartureTime())){
			if(System.currentTimeMillis() - smtArticlesRelease.getPlannedDepartureTime().getTime() > 0){
				detailRespDTO.setExpire(true);
			}
		}
		List<ApprovalProcessRecordReqDTO> approvalProcess;
		if (StrUtil.isNotBlank(smtArticlesRelease.getProcessId())) {
			// 办公区物品放行
			approvalProcess = processRecord(smtArticlesRelease);
			SmtArticlesReleaseMain releaseMain = releaseMainService.getByReleaseId(smtArticlesRelease.getId());
			Assert.notNull(releaseMain, "查询物品放行详情失败");
			ReleaseApplyMainRespDTO mainRespDTO = BeanUtil.toBean(releaseMain, ReleaseApplyMainRespDTO.class);
			mainRespDTO.setSqrjbDesc(ApplyPersonLevelEnum.getByCode(releaseMain.getSqrjb()));
			mainRespDTO.setSffcDesc(BackFactoryEnum.getByCode(releaseMain.getSffc()));
			mainRespDTO.setFxsxDesc(ReleaseItemEnum.getByCode(releaseMain.getFxsx()));
			mainRespDTO.setFxqcDesc(ReleaseToEnum.getByCode(releaseMain.getFxqc()));
			mainRespDTO.setFxddDesc(FromToPlaceEnum.getByCode(releaseMain.getFxdd()));
			mainRespDTO.setDdddDesc(FromToPlaceEnum.getByCode(releaseMain.getDddd()));
			mainRespDTO.setWpfxlbDesc(ReleaseTypeEnum.getByCode(releaseMain.getWpfxlb()));
			mainRespDTO.setLcbh(smtArticlesRelease.getProcessId());
			mainRespDTO.setSqbm(detailRespDTO.getDeptName());
			detailRespDTO.setApplyMain(mainRespDTO);
			if (ReleaseItemEnum.ITEM_0.getCode().equals(releaseMain.getFxsx()) ||
					ReleaseItemEnum.ITEM_7.getCode().equals(releaseMain.getFxsx())) {
				List<SmtArticlesReleasePerson> personList = releasePersonService.getListByReleaseId(smtArticlesRelease.getId());
				detailRespDTO.setPersonDetailList(BeanUtils.batchTransform(ReleaseApplyPersonDetail.class, personList));
			} else {
				List<SmtArticlesReleaseThing> thingList = releaseThingService.getListByReleaseId(smtArticlesRelease.getId());
				detailRespDTO.setThingDetailList(BeanUtils.batchTransform(ReleaseApplyThingDetail.class, thingList));
			}
		} else {
			// 生活区物品放行
			approvalProcess = getProcess(smtArticlesRelease.getId().toString(), smtArticlesRelease.getName(), smtArticlesRelease.getCreateTime());
		}
		if(CollUtil.isNotEmpty(approvalProcess)) {
			detailRespDTO.setApprovalProcess(approvalProcess);
		}
		// 查询物品放行园区配置
		SmtApproval approval = approvalService.getApproval(smtArticlesRelease.getParkId(), 3);
		if (Objects.nonNull(approval)) {
			detailRespDTO.setIsUploadImg(approval.getIsUploadImg());
		}
		if (StrUtil.isNotBlank(smtArticlesRelease.getSecurityStaff())) {
			SmtStaff vo = smtStaffService.getOne(Wrappers.<SmtStaff>lambdaQuery().eq(SmtStaff::getBadge, smtArticlesRelease.getSecurityStaff()), false);
			detailRespDTO.setSecurityStaff(vo != null ? vo.getBadge() + "-" + vo.getName() : smtArticlesRelease.getSecurityStaff());
		}
		return detailRespDTO;
	}

	private List<ApprovalProcessRecordReqDTO> getProcess(String businessId, String applyName, LocalDateTime createTime) {
		//填写发起人
		List<ApprovalProcessRecordReqDTO> dtoList = new ArrayList<>();
		ApprovalProcessRecordReqDTO recordReqDTO = new ApprovalProcessRecordReqDTO();
		recordReqDTO.setBusinessId(businessId);
		recordReqDTO.setRecordNode(OneOrZeroEnum.ZERO.getCode());
		List<ApprovalProcessRecordReqDTO.StaffInfo> applyPersons = new ArrayList<>();
		ApprovalProcessRecordReqDTO.StaffInfo applyPerson = new ApprovalProcessRecordReqDTO.StaffInfo();
		applyPerson.setStaffName(applyName);
		applyPerson.setResultDesc("提交");
		applyPerson.setCreateDate(createTime);
		applyPersons.add(applyPerson);
		recordReqDTO.setStaffInfos(applyPersons);
		dtoList.add(recordReqDTO);
		List<ApproveList> lists = approveListService.getByStatus(null, businessId, null);
		if (CollUtil.isEmpty(lists)) {
			return dtoList;
		}
		if (Objects.isNull(lists.get(0).getSort())) {
			lists.get(0).setSort(1);
		}
		Map<Integer, List<ApproveList>> map = lists.stream()
				.collect(Collectors.groupingBy(ApproveList::getSort));
		Iterator<Map.Entry<Integer, List<ApproveList>>> entries = map.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<Integer, List<ApproveList>> entry = entries.next();
			ApprovalProcessRecordReqDTO resp = new ApprovalProcessRecordReqDTO();
			List<ApproveList> approves = entry.getValue();
			if (CollUtil.isEmpty(approves)) {
				continue;
			}
			List<ApprovalProcessRecordReqDTO.StaffInfo> staffInfos = new ArrayList<>();
			approves.forEach(approveList -> {
				ApprovalProcessRecordReqDTO.StaffInfo staffInfo = new ApprovalProcessRecordReqDTO.StaffInfo();
				staffInfo.setResult(approveList.getApproveState());
				staffInfo.setResultDesc(ApproveListStateEnum.desc(approveList.getApproveState()));
				staffInfo.setStaffBadge(approveList.getApproveBadge());
				SmtStaff smtStaff = smtStaffService.getSimpleSttaffByBadge(approveList.getApproveBadge());
				if (Objects.isNull(smtStaff)) {
					throw new TCEException("审批人不存在");
				}
				staffInfo.setStaffName(smtStaff.getName());
				staffInfo.setCreateDate(approveList.getCreateTime());
				staffInfo.setRecordDate(approveList.getUpdateTime());
				staffInfo.setRemark(approveList.getRemark());
				staffInfos.add(staffInfo);
			});
			resp.setStaffInfos(staffInfos);
			resp.setRecordNode(approves.get(0).getSort());
			resp.setBusinessId(businessId);

			SmtApprovalNode approvalNode = approvalNodeService.getById(approves.get(0).getNodeId());
			resp.setStatusName(approvalNode.getName());
			dtoList.add(resp);
		}
		return dtoList;
	}

	private List<ApprovalProcessRecordReqDTO> processRecord(SmtArticlesRelease smtArticlesRelease) {
		List<SmtProcessRecord> processRecordList = processRecordMapper.getProcessRecord(smtArticlesRelease.getProcessId());
		List<ApprovalProcessRecordReqDTO> approvalProcess = new ArrayList<>();
		ApprovalProcessRecordReqDTO recordReqDTO = new ApprovalProcessRecordReqDTO();
		recordReqDTO.setBusinessId(smtArticlesRelease.getId().toString());
		recordReqDTO.setRecordNode(OneOrZeroEnum.ZERO.getCode());
		List<ApprovalProcessRecordReqDTO.StaffInfo> applyPersons = new ArrayList<>();
		ApprovalProcessRecordReqDTO.StaffInfo applyPerson = new ApprovalProcessRecordReqDTO.StaffInfo();
		applyPerson.setStaffName(smtArticlesRelease.getName());
		applyPerson.setResultDesc("提交");
		applyPerson.setCreateDate(smtArticlesRelease.getCreateTime());
		applyPersons.add(applyPerson);
		recordReqDTO.setStaffInfos(applyPersons);
		approvalProcess.add(recordReqDTO);

		ApprovalProcessRecordReqDTO resp = new ApprovalProcessRecordReqDTO();
		List<ApprovalProcessRecordReqDTO.StaffInfo> staffInfos = new ArrayList<>();

		for (SmtProcessRecord processRecord : processRecordList) {
			ApprovalProcessRecordReqDTO.StaffInfo staffInfo = new ApprovalProcessRecordReqDTO.StaffInfo();
			staffInfo.setResult(NodeStatusEnum.changeCode(processRecord.getStatus()));
			staffInfo.setResultDesc(NodeStatusEnum.desc(processRecord.getStatus()));
			staffInfo.setStaffBadge(processRecord.getStaffBadge());
			staffInfo.setStaffName(processRecord.getStaffName());
			if (Objects.nonNull(processRecord.getCreatTime())) {
				staffInfo.setCreateDate(LocalDateTime.ofInstant(processRecord.getCreatTime().toInstant(), ZoneId.systemDefault()));
			}
			if (Objects.nonNull(processRecord.getRecordDate())) {
				staffInfo.setRecordDate(LocalDateTime.ofInstant(processRecord.getRecordDate().toInstant(), ZoneId.systemDefault()));
			}
			staffInfo.setRemark(StrUtil.isBlank(processRecord.getRemark()) ? "" : processRecord.getRemark());
			staffInfos.add(staffInfo);
		}
		resp.setStaffInfos(staffInfos);
		resp.setRecordNode(1);
		resp.setBusinessId(smtArticlesRelease.getId().toString());
		approvalProcess.add(resp);

		if (ArticlesReleaseStatusEnum.APPROVED.getCode().equals(smtArticlesRelease.getStatus())) {
			// 归档节点
			ApprovalProcessRecordReqDTO archiveNode = new ApprovalProcessRecordReqDTO();
			archiveNode.setBusinessId(smtArticlesRelease.getId().toString());
			archiveNode.setRecordNode(2);
			List<ApprovalProcessRecordReqDTO.StaffInfo> secondApplyPersons = new ArrayList<>();
			ApprovalProcessRecordReqDTO.StaffInfo secondApplyPerson = new ApprovalProcessRecordReqDTO.StaffInfo();
			secondApplyPerson.setResultDesc("已归档");
			List<ApprovalProcessRecordReqDTO.StaffInfo> nodeInfo = approvalProcess.get(1).getStaffInfos();
			// 获取最后一个审批节点的审批时间
			secondApplyPerson.setRecordDate(nodeInfo.get(nodeInfo.size() - 1).getRecordDate());
			secondApplyPersons.add(secondApplyPerson);
			archiveNode.setStaffInfos(secondApplyPersons);
			approvalProcess.add(archiveNode);
		}
		return approvalProcess;
	}
}
