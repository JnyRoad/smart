package com.tce.smart.platform.service.approval.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.api.dto.req.approval.ApprovalProcessReqDTO;
import com.tce.smart.platform.api.dto.req.approval.EditApprovalConditionReqDTO;
import com.tce.smart.platform.api.dto.req.approval.EditApprovalNodeReqDTO;
import com.tce.smart.platform.api.dto.req.approval.EditApprovalPersonReqDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApproveProcessListReqDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.service.SmtApprovalConditionService;
import com.tce.smart.platform.core.service.SmtApprovalNodeService;
import com.tce.smart.platform.core.service.SmtApprovalPersonService;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.approval.ApprovalNodeService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2021-04-08 16:25:18
 */
@Slf4j
@Service
public class ApprovalNodeServiceImpl implements ApprovalNodeService {

	@Autowired
	private SmtApprovalNodeService smtApprovalNodeService;
	@Autowired
	private SmtApprovalConditionService smtApprovalConditionService;
	@Autowired
	private SmtApprovalPersonService smtApprovalPersonService;
	@Autowired
	private SmtDormitoryStaffService smtDormitoryStaffService;
	@Autowired
	private SmtStaffService smtStaffService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveNode(List<EditApprovalNodeReqDTO> editApprovalNodes) {
		editApprovalNodes.forEach(editApprovalNode -> {
			SmtApprovalNode node = BeanUtils.transform(SmtApprovalNode.class, editApprovalNode);
			smtApprovalNodeService.save(node);
			//新增关联条件
			List<EditApprovalConditionReqDTO> conditions = editApprovalNode.getConditions();
			conditions.forEach(c -> {
				SmtApprovalCondition condition = BeanUtils.transform(SmtApprovalCondition.class, c);
				condition.setNodeId(node.getId());
				smtApprovalConditionService.save(condition);
			});
			//新增关联审批人
			if (editApprovalNode.getIsExistApprover().equals(ApprovalPersonRuleEnum.EXIST.getCode())) {
				List<EditApprovalPersonReqDTO> persons = editApprovalNode.getApprovalPersons();
				persons.forEach(p -> {
					SmtApprovalPerson person = BeanUtils.transform(SmtApprovalPerson.class, p);
					person.setNodeId(node.getId());
					smtApprovalPersonService.save(person);
				});
			}
		});

		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean editNode(List<EditApprovalNodeReqDTO> editApprovalNodes) {
		if (CollUtil.isNotEmpty(editApprovalNodes)) {
			//先删除后新增
			this.deleteNode(editApprovalNodes.get(0).getApprovalId());
			this.saveNode(editApprovalNodes);
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateNode(List<EditApprovalNodeReqDTO> editApprovalNodes) {
		if (CollUtil.isNotEmpty(editApprovalNodes)) {
			List<SmtApprovalNode> nodes = smtApprovalNodeService.getList(editApprovalNodes.get(0).getApprovalId());
			Map<Integer, EditApprovalNodeReqDTO> nodeReqDTOMap = editApprovalNodes.stream().filter(item -> Objects.nonNull(item.getId())).collect(Collectors.toMap(EditApprovalNodeReqDTO::getId, v -> v));

			for (SmtApprovalNode node : nodes) {
				// 删除不存在的节点
				if (!nodeReqDTOMap.containsKey(node.getId())) {
					//删除关联条件
					smtApprovalConditionService.remove(Wrappers.<SmtApprovalCondition>query().lambda().eq(SmtApprovalCondition::getNodeId, node.getId()));
					//删除关联审批人
					smtApprovalPersonService.remove(Wrappers.<SmtApprovalPerson>query().lambda().eq(SmtApprovalPerson::getNodeId, node.getId()));
					//删除节点
					smtApprovalNodeService.removeById(node.getId());
				}
			}

			for (EditApprovalNodeReqDTO editApprovalNode : editApprovalNodes) {
				if (Objects.nonNull(editApprovalNode.getId())) {
					// 更新节点,先删除节点关联的条件和人员
					deleteNodeConditionAndPerson(editApprovalNode.getId());
					updateNode(editApprovalNode);
				} else {
					// 新增节点
					saveNode(editApprovalNode);
				}
			}
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	private void saveNode(EditApprovalNodeReqDTO editApprovalNode) {
		SmtApprovalNode node = BeanUtils.transform(SmtApprovalNode.class, editApprovalNode);
		smtApprovalNodeService.save(node);
		saveNodeConditionAndPerson(editApprovalNode, node);
	}

	private void updateNode(EditApprovalNodeReqDTO editApprovalNode) {
		SmtApprovalNode node = smtApprovalNodeService.getById(editApprovalNode.getId());
		if (Objects.isNull(node)) {
			node = BeanUtils.transform(SmtApprovalNode.class, editApprovalNode);
			smtApprovalNodeService.save(node);
		} else {
			BeanUtil.copyProperties(editApprovalNode, node);
			smtApprovalNodeService.updateById(node);
			if (editApprovalNode.getMsgTemplate() == null) {
				smtApprovalNodeService.update(Wrappers.<SmtApprovalNode>lambdaUpdate()
						.set(SmtApprovalNode::getMsgTemplate, null)
						.eq(SmtApprovalNode::getId, node.getId()));
			}
		}
		saveNodeConditionAndPerson(editApprovalNode, node);
	}

	private void deleteNodeConditionAndPerson(Integer nodeId) {
		//删除关联条件
		smtApprovalConditionService.remove(Wrappers.<SmtApprovalCondition>query().lambda().eq(SmtApprovalCondition::getNodeId, nodeId));
		//删除关联审批人
		smtApprovalPersonService.remove(Wrappers.<SmtApprovalPerson>query().lambda().eq(SmtApprovalPerson::getNodeId, nodeId));
	}

	private void saveNodeConditionAndPerson(EditApprovalNodeReqDTO editApprovalNode, SmtApprovalNode node) {
		//新增关联条件
		List<EditApprovalConditionReqDTO> conditions = editApprovalNode.getConditions();
		for(EditApprovalConditionReqDTO c : conditions) {
			SmtApprovalCondition condition = BeanUtils.transform(SmtApprovalCondition.class, c);
			condition.setNodeId(node.getId());
			smtApprovalConditionService.save(condition);
		}
		//新增关联审批人
		if (editApprovalNode.getIsExistApprover().equals(ApprovalPersonRuleEnum.EXIST.getCode())) {
			List<EditApprovalPersonReqDTO> persons = editApprovalNode.getApprovalPersons();
			for(EditApprovalPersonReqDTO p : persons) {
				SmtApprovalPerson person = BeanUtils.transform(SmtApprovalPerson.class, p);
				person.setNodeId(node.getId());
				smtApprovalPersonService.save(person);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean deleteNode(Integer approvalId) {
		List<SmtApprovalNode> nodes = smtApprovalNodeService.getList(approvalId);
		if (CollUtil.isNotEmpty(nodes)) {
			nodes.forEach(n -> {
				Integer nodeId = n.getId();
				//删除关联条件
				smtApprovalConditionService.remove(Wrappers.<SmtApprovalCondition>query().lambda().eq(SmtApprovalCondition::getNodeId, nodeId));
				//删除关联审批人
				smtApprovalPersonService.remove(Wrappers.<SmtApprovalPerson>query().lambda().eq(SmtApprovalPerson::getNodeId, nodeId));
				//删除节点
				smtApprovalNodeService.removeById(nodeId);
			});
		}
		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public List<ApproveProcessListReqDTO> getApprovalPerson(ApprovalProcessReqDTO reqDTO) {
		List<SmtApprovalNode> nodes = smtApprovalNodeService.getList(reqDTO.getApprovalId());
		if (CollUtil.isEmpty(nodes)) {
			throw new SmartException("审批流程未设置节点！");
		}
		Integer sort = Objects.nonNull(reqDTO.getSort()) ? reqDTO.getSort() : OneOrZeroEnum.ONE.getCode();
		List<ApproveProcessListReqDTO> approveProcessListReqDTOS = new ArrayList<>();
		for (SmtApprovalNode node : nodes) {
			//如果不存在审批人则跳过此节点
			if (node.getIsExistApprover().equals(ApprovalPersonRuleEnum.NONE.getCode())) {
				continue;
			}
			List<SmtApprovalCondition> conditionList = smtApprovalConditionService.getList(node.getId());
			Boolean resultNow;
			//如果条件为空跳过此节点
			if (CollUtil.isEmpty(conditionList)) {
				resultNow = Boolean.TRUE;
			} else {
				//获得节点条件对比结果
				resultNow = this.getEndResult(conditionList, reqDTO);
			}
			if (resultNow) {
				List<SmtApprovalPerson> personList = this.getApprovalPersonList(reqDTO, node.getId(), node.getIsExistApprover());
				if (CollUtil.isNotEmpty(personList)) {
					for (SmtApprovalPerson person : personList) {
						ApproveProcessListReqDTO approveProcess = new ApproveProcessListReqDTO();
						approveProcess.setBusinessId(reqDTO.getBusinessId());
						approveProcess.setApproveType(reqDTO.getEventId());
						approveProcess.setApproveState(ApproveListStateEnum.WAITING.getCode());
						if (sort.equals(OneOrZeroEnum.ONE.getCode())) {
							approveProcess.setApproveState(ApproveListStateEnum.PENDING.getCode());
						}
						approveProcess.setApproveBadge(person.getApproverBadge());
						approveProcess.setSort(sort);
						approveProcess.setPassRule(node.getPassRule());
						approveProcess.setIsAppPush(node.getIsAppPush());
						approveProcess.setMsgTemplate(node.getMsgTemplate());
						approveProcess.setNodeId(node.getId());
						approveProcess.setIsWeChatPush(node.getIsWeChatPush());
						approveProcess.setIsExistApprover(node.getIsExistApprover());
						approveProcessListReqDTOS.add(approveProcess);
					}
					sort++;
				}
			}
		}
		//检查递归设置审批状态 未开启
		//List<ApproveProcessListReqDTO> approveProcessList = this.circleStatus(approveProcessListReqDTOS, OneOrZeroEnum.ONE.getCode(), reqDTO.getApplyBadge());
		return approveProcessListReqDTOS;
	}

	/**
	 * 获得审批人列表
	 *
	 * @param reqDTO
	 * @return
	 */
	private List<SmtApprovalPerson> getApprovalPersonList(ApprovalProcessReqDTO reqDTO, Integer nodeId, Integer type) {
		List<SmtApprovalPerson> personList = new ArrayList<>();
		switch (ApprovalPersonRuleEnum.getEnmu(type)) {
			case EXIST:
				personList = smtApprovalPersonService.getList(nodeId);
				break;
			case LEADER:
				SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(reqDTO.getApplyBadge());
				if(Objects.isNull(staff)) {
					throw new SmartException("员工为空");
				}
				if (Objects.nonNull(staff.getReportTo())) {
					SmtStaff approvalStaff = smtStaffService.getSimpleSttaffByBadge(staff.getReportTo());
					if(Objects.isNull(staff)) {
						throw new SmartException("员工为空");
					}
					SmtApprovalPerson person = SmtApprovalPerson.builder().approverBadge(approvalStaff.getBadge())
							.approverName(approvalStaff.getName()).nodeId(nodeId).sort(SymbolConstants.ONE_INTEGER).build();
					personList.add(person);
				}
				break;
			case ROOMMATE:
				List<SmtDormitoryStaff> dormitoryStaff = smtDormitoryStaffService.getRoommate(reqDTO.getRoomId(), reqDTO.getApplyBadge());
				Integer index = SymbolConstants.ONE_INTEGER;
				for (SmtDormitoryStaff dorStaff : dormitoryStaff) {
					SmtApprovalPerson person = SmtApprovalPerson.builder().approverBadge(dorStaff.getStaffBadge())
							.approverName(dorStaff.getStaffName()).nodeId(nodeId).sort(index).build();
					personList.add(person);
					index++;
				}
				break;
		}
		return personList;
	}

	/**
	 * 递归设置审批状态
	 *
	 * @return
	 */
	private List<ApproveProcessListReqDTO> circleStatus(List<ApproveProcessListReqDTO> approveProcessList, int sort, String badge) {
		//若发起人等于审批人，则自动通过
		for (ApproveProcessListReqDTO approveProcess : approveProcessList) {
			if (approveProcess.getSort().equals(sort)) {
				approveProcess.setApproveState(ApproveListStateEnum.PENDING.getCode());

				if (badge.equals(approveProcess.getApproveBadge())) {

					if (approveProcess.getPassRule().equals(ApprovalPersonPassEnum.ALL.getCode())) {
						approveProcess.setApproveState(ApproveListStateEnum.AGREE.getCode());
						List<ApproveProcessListReqDTO> approves = approveProcessList.stream().filter(app -> app.getSort().equals(sort)).collect(Collectors.toList());
						if (approves.size() == 1) {
							//开启下一级审批
							return this.circleStatus(approveProcessList, sort + 1, badge);
						}
					}

					if (approveProcess.getPassRule().equals(ApprovalPersonPassEnum.ONLY_ONE.getCode())) {
						//本节点其他节点关闭
						for (ApproveProcessListReqDTO approveProcess2 : approveProcessList) {
							if (approveProcess2.getSort().equals(sort)) {
								approveProcess2.setApproveState(ApproveListStateEnum.CLOSE.getCode());
							}
						}
						approveProcess.setApproveState(ApproveListStateEnum.AGREE.getCode());
						//开启下一级审批
						return this.circleStatus(approveProcessList, sort + 1, badge);
					}
				}
			}
		}
		return approveProcessList;
	}

	/**
	 * 获得所有条件合并结果
	 *
	 * @param conditionList
	 * @param reqDTO
	 * @return
	 */
	private Boolean getEndResult(List<SmtApprovalCondition> conditionList, ApprovalProcessReqDTO reqDTO) {
		//获取第一个条件对比结果
		Boolean resultNow = this.getConditionResult(conditionList.get(0), reqDTO);
		//第一个条条件结果与余下条件对比
		if (conditionList.size() > 1) {
			Boolean resultNext;
			for (int i = 1; i < conditionList.size(); i++) {
				resultNext = this.getConditionResult(conditionList.get(i), reqDTO);
				if (conditionList.get(i - 1).getConnector().equals(ApprovalConnectorEnum.AND.getCode())) {
					resultNow = resultNow && resultNext;
				} else {
					resultNow = resultNow || resultNext;
				}
			}
		}
		return resultNow;
	}

	/**
	 * 获得每条条件结果
	 *
	 * @param condition
	 * @param reqDTO
	 * @return
	 */
	private Boolean getConditionResult(SmtApprovalCondition condition, ApprovalProcessReqDTO reqDTO) {
		switch (ApprovalConditionEnum.getEnmu(condition.getConditionType())) {
			case DORMITORY_NAME:
				return this.getCompareResult(condition.getComparator(), reqDTO.getDormitoryId(), condition.getCompareValue());
			case ITEM_TYPE:
				return this.getCompareResult(condition.getComparator(), reqDTO.getArticlesType(), condition.getCompareValue());
			case REPAIR_AREA:
				return this.getCompareResult(condition.getComparator(), reqDTO.getRangeType(), condition.getCompareValue());
			case REPAIR_TYPE:
				return this.getCompareResult(condition.getComparator(), reqDTO.getRepairType(), condition.getCompareValue());
			case QUIT_DORMITORY_REASON:
				return this.getCompareResult(condition.getComparator(), reqDTO.getQuitReason(), condition.getCompareValue());
			case QUIT_DORMITORY_NAME:
				return this.getBatchCompareResult(condition.getComparator(), reqDTO.getDormitoryIds(), condition.getCompareValue());
			default:
				return Boolean.FALSE;
		}
	}

	/**
	 * 多个楼栋条件比较
	 *
	 * @param comparator 对比符
	 * @param target     对比值
	 * @param source     条件设置值
	 * @return
	 */
	private Boolean getBatchCompareResult(Integer comparator, List<Integer> target, String source) {
		for (Integer tar : target) {
			if (this.getCompareResult(comparator, tar, source)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * @param comparator 对比符
	 * @param target     对比值
	 * @param source     条件设置值
	 * @return
	 */
	private Boolean getCompareResult(Integer comparator, Integer target, String source) {
		Integer value = Integer.parseInt(source);
		if (Objects.isNull(target) || StringUtils.isEmpty(source)) {
			return Boolean.FALSE;
		}
		if (ApprovalComparatoreEnum.EQUAL.getCode().equals(comparator)) {
			if (target.equals(value)) {
				return Boolean.TRUE;
			}
		} else {
			if (!target.equals(value)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
}
