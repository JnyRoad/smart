package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.RepairsApproveListReqDTO;
import com.tce.smart.platform.api.dto.req.approval.ApproveListQueryDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApprovalProcessRecordReqDTO;
import com.tce.smart.platform.core.dto.RepairsApprovalListDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.ApproveListMapper;
import com.tce.smart.platform.core.service.SmtApprovalNodeService;
import com.tce.smart.platform.service.ApproveListService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.SmtVisitorApprovalProxyService;
import com.tce.smart.platform.service.SmtVisitorService;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.enums.ApprovalPersonPassEnum;
import com.tce.smart.tool.enums.ApprovalProcessResultEnum;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审批记录
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@Slf4j
@Service
public class ApproveListServiceImpl extends ServiceImpl<ApproveListMapper, ApproveList> implements ApproveListService {

	@Autowired
	private SmtVisitorService smtVisitorService;

	@Autowired
	private SmtVisitorApprovalProxyService smtVisitorApprovalProxyService;

	@Autowired
	private SmtStaffService smtStaffService;

	@Autowired
	private SmtApprovalNodeService smtApprovalNodeService;


	@Override
	public boolean updateState(ApproveList approveList) {
		if (ObjectUtil.isNull(approveList.getApproveType())
				|| ObjectUtil.isNull(approveList.getApproveState())
				|| StrUtil.isBlank(approveList.getApproveBadge())
				|| StrUtil.isBlank(approveList.getBusinessId())) {
			log.info("待我审批修改失败，参数非法");
			return false;
		}
		if (approveList.getApproveType().equals(ApproveListTypeConstants.VISITOR)) {
			//如果是访客审批  可能有代理人的情况 代理人和被代理人的状态同时修改

			//查询访客记录
			SmtVisitor visitor = smtVisitorService.getById(approveList.getBusinessId());

			//查询被访人的代理人
			List<SmtVisitorApprovalProxy> approvalProxies = smtVisitorApprovalProxyService.list(new LambdaQueryWrapper<SmtVisitorApprovalProxy>()
					.eq(SmtVisitorApprovalProxy::getIntervieweeBadge, visitor.getReceptionistBadge())
					.eq(SmtVisitorApprovalProxy::getParkId, visitor.getParkId())
			);

			List<String> badgeList = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(approvalProxies)) {

				//所有代理人的工号
				badgeList = approvalProxies.stream().map(SmtVisitorApprovalProxy::getProxyBadge).collect(Collectors.toList());

				//存在代理人的情况  被访人的记录也要被审批
				badgeList.add(visitor.getReceptionistBadge());
			}

			//当前操作人的工号
			badgeList.add(approveList.getApproveBadge());

			ApproveList updateApprove = new ApproveList();
			updateApprove.setApproveState(approveList.getApproveState());
			return this.update(updateApprove, new LambdaUpdateWrapper<ApproveList>()
					.eq(ApproveList::getBusinessId, approveList.getBusinessId())
					.in(ApproveList::getApproveBadge, badgeList)
			);
		}
		return this.baseMapper.updateState(approveList) > 0;
	}

	@Override
	public boolean saveApproveList(ApproveList approveList) {
		approveList.setCreateTime(LocalDateTime.now());
		return this.save(approveList);
	}

	/**
	 * 单审批人通过则通过情况下上级节点通过，开启下一节点
	 *
	 * @param businessId
	 * @param sort
	 * @param badge
	 * @return true 所有节点通过  false  并非所有节点通过
	 */
	@Override
	public Integer openNextNode(String businessId, Integer sort, String badge) {
		//获得下级节点
		List<ApproveList> nextApproveList2 = this.list(Wrappers.<ApproveList>query().lambda()
				.eq(ApproveList::getBusinessId, businessId)
				.eq(ApproveList::getSort, sort));
		if (CollUtil.isNotEmpty(nextApproveList2)) {
			this.batchUpdateStatus(businessId, sort, ApproveListStateEnum.PENDING.getCode());
			for (ApproveList approveList : nextApproveList2) {
				//审批人等于发起人，自动通过
				if (approveList.getApproveBadge().equals(badge)) {
					//本审批通过
					approveList.setApproveState(ApproveListStateEnum.AGREE.getCode());
					this.updateById(approveList);
					if (approveList.getPassRule().equals(ApprovalPersonPassEnum.ALL.getCode())) {
						return this.openNextNode2(businessId, sort, badge);
					}
					//本节点其他审批关闭
					this.batchUpdateStatus(businessId, approveList.getSort(), ApproveListStateEnum.CLOSE.getCode());
					//下一节点打开
					return this.openNextNode(businessId, sort + 1, badge);
				}
			}
			return ApprovalProcessResultEnum.PART_PASS.getCode();
		}
		this.batchUpdateStatus(businessId, sort, ApproveListStateEnum.CLOSE.getCode());
		return ApprovalProcessResultEnum.ALL_PASS.getCode();
	}

	/**
	 * 所有人通过则通过情况下上级节点通过，开启下一节点
	 *
	 * @param businessId
	 * @param sort
	 * @param badge
	 * @return true 所有节点通过  false  部分节点通过
	 */
	@Override
	public Integer openNextNode2(String businessId, Integer sort, String badge) {
		List<ApproveList> pendingApprove = this.getByStatus(ApproveListStateEnum.PENDING.getCode(), businessId, sort);
		if (CollUtil.isNotEmpty(pendingApprove)) {
			return ApprovalProcessResultEnum.NODE_PART_PASS.getCode();
		}
		//所有节点通过，开启相下一节点
		return this.openNextNode(businessId, sort + 1, badge);
	}

	@Override
	public Boolean batchUpdateStatus(String businessId, Integer sort, Integer newStatus) {
		UpdateWrapper<ApproveList> wrapper = new UpdateWrapper<>();
		List<Integer> status = new ArrayList<>();
		status.add(ApproveListStateEnum.PENDING.getCode());
		status.add(ApproveListStateEnum.WAITING.getCode());
		wrapper.set("APPROVE_STATE", newStatus)
				.in("APPROVE_STATE", status)
				.eq("BUSINESS_ID", businessId)
				.eq(Objects.nonNull(sort), "SORT", sort);
		this.update(null, wrapper);
		return Boolean.TRUE;
	}


	@Override
	public List<ApproveList> getByStatus(Integer status, String businessId, Integer sort) {
		return this.list(Wrappers.<ApproveList>query().lambda()
				.eq(StringUtils.isNotEmpty(businessId), ApproveList::getBusinessId, businessId)
				.eq(Objects.nonNull(sort), ApproveList::getSort, sort)
				.eq(Objects.nonNull(status), ApproveList::getApproveState, status).orderByAsc(ApproveList::getSort));
	}

	@Override
	public String getNewApprove(Integer status, String businessId) {
		List<ApproveList> approveLists = this.list(Wrappers.<ApproveList>query().lambda()
				.eq(StringUtils.isNotEmpty(businessId), ApproveList::getBusinessId, businessId)
				.eq(Objects.nonNull(status), ApproveList::getApproveState, status).orderByDesc(ApproveList::getSort));
		if (CollUtil.isEmpty(approveLists)) {
			return null;
		}
		SmtApprovalNode node = smtApprovalNodeService.getById(approveLists.get(0).getNodeId());
		if(Objects.isNull(node)) {
			return null;
		}
		return node.getName();
	}

	@Override
	public List<ApproveList> getByType(List<Integer> status, Integer type, String badge) {
		return this.list(Wrappers.<ApproveList>query().lambda()
				.eq(StringUtils.isNotEmpty(badge), ApproveList::getApproveBadge, badge)
				.eq(Objects.nonNull(type), ApproveList::getApproveType, type)
				.in(CollUtil.isNotEmpty(status), ApproveList::getApproveState, status));
	}

	@Override
	public IPage getApproveList(Page page, ApproveList approveList) {
		if (approveList.getApproveState().equals(ApproveListStateEnum.PENDING.getCode())) {
			return this.baseMapper.getPageStart(page, approveList);
		} else {
			return this.baseMapper.getPageEnd(page, approveList);
		}
	}

	@Override
	public IPage<RepairsApprovalListDTO> getRepairsApproveList(Page page, RepairsApproveListReqDTO reqDTO) {
		ApproveList approveList = new ApproveList();
		approveList.setApproveState(reqDTO.getRecordState());
		approveList.setApproveType(reqDTO.getRecordType());
		approveList.setApproveBadge(SecurityUtils.getUser().getUsername());
		if (approveList.getApproveState().equals(ApproveListStateEnum.PENDING.getCode())) {
			return this.baseMapper.getRepairsWaitPass(page, approveList);
		} else {
			return this.baseMapper.getRepairsPass(page, approveList);
		}
	}

	@Override
	public IPage getNewPage(Page page, ApproveListQueryDTO queryDTO) {
		List<Integer> stateList = new ArrayList<>();
		if (ApproveListStateEnum.PENDING.getCode().equals(queryDTO.getRecordState())) {
			stateList.add(0);
		} else {
			stateList.add(1);
			stateList.add(2);
			stateList.add(3);
		}
		String username = SecurityUtils.getUser().getUsername();
		return this.baseMapper.getNewPage(page, queryDTO, stateList, username);
	}

	/**
	 * 获得审批流程与审批人
	 *
	 * @param businessId
	 * @return
	 */
	@Override
	public List<ApprovalProcessRecordReqDTO> getProcess(String businessId, String applyName, LocalDateTime createTime) {
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
		List<ApproveList> lists = this.getByStatus(null, businessId, null);
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
				log.info("审批人信息：{}", approveList);
				if (Objects.isNull(smtStaff)) {
					return;
				}
				staffInfo.setStaffName(smtStaff.getName());
				staffInfo.setCreateDate(approveList.getCreateTime());
				staffInfo.setRecordDate(approveList.getUpdateTime());
				staffInfo.setRemark(approveList.getRemark());
				staffInfos.add(staffInfo);
			});
			SmtApprovalNode node = smtApprovalNodeService.getById(approves.get(0).getNodeId());
			if (Objects.nonNull(node)) {
				resp.setStatusName(node.getName());
			}
			resp.setStaffInfos(staffInfos);
			resp.setRecordNode(approves.get(0).getSort());
			resp.setBusinessId(businessId);
			dtoList.add(resp);
		}
		return dtoList;
	}

	/**
	 * 修改审批状态
	 *
	 * @param businessId
	 * @param applyBadge
	 * @param approvalStatus
	 * @return
	 */
	@Override
	public Integer updateProcessStatus(String businessId, String applyBadge, Integer approvalStatus, Integer approvalListId) {
		ApproveList approveList = this.getById(approvalListId);
		approveList.setApproveState(approvalStatus);
		this.updateById(approveList);
		if (approvalStatus.equals(ApproveListStateEnum.AGREE.getCode())) {
			//策略方式为"审批人任其一通过，进入下个流程"
			if (approveList.getPassRule().equals(ApprovalPersonPassEnum.ONLY_ONE.getCode())) {
				//本节点其他审批关闭
				this.batchUpdateStatus(businessId, approveList.getSort(), ApproveListStateEnum.CLOSE.getCode());
				return this.openNextNode(approveList.getBusinessId(), approveList.getSort() + 1, applyBadge);
			}
			//检查是否所有的审批都已通过
			return this.openNextNode2(approveList.getBusinessId(), approveList.getSort(), applyBadge);
		}
		//策略方式为"审批人全部通过，进入下个流程"  则结束流程
		if (approveList.getPassRule().equals(ApprovalPersonPassEnum.ALL.getCode())) {
			this.batchUpdateStatus(businessId, null, ApproveListStateEnum.CLOSE.getCode());
			return ApprovalProcessResultEnum.ALL_REFUSE.getCode();
		}
		//检查是否所有审批结果都为拒绝
		List<ApproveList> closeApprove = this.getByStatus(ApproveListStateEnum.PENDING.getCode(), businessId, approveList.getSort());
		if (CollUtil.isEmpty(closeApprove)) {
			return ApprovalProcessResultEnum.ALL_REFUSE.getCode();
		}
		return ApprovalProcessResultEnum.PART_REFUSE.getCode();
	}

	@Override
	public List<ApproveList> getByBusinessId(String businessId) {
		return this.list(Wrappers.<ApproveList>query().lambda().eq(ApproveList::getBusinessId, businessId));
	}
}
