package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.platform.api.dto.req.DormitoryQuitApplyEditReqDTO;
import com.tce.smart.platform.api.dto.req.DormitoryQuitApplyQueryDTO;
import com.tce.smart.platform.api.dto.req.QuitDorApplyQueryReqDTO;
import com.tce.smart.platform.api.dto.req.approval.ApprovalProcessReqDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApproveProcessListReqDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtDormitoryQuitApplyMapper;
import com.tce.smart.platform.core.service.SmtApprovalNodeService;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.approval.ApprovalService;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.ToolUtils;
import com.tce.smart.tool.util.WeChatMsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Auther: fushiping
 * @Date:
 */
@Service
@Slf4j
public class SmtDormitoryQuitApplyServiceImpl extends ServiceImpl<SmtDormitoryQuitApplyMapper, SmtDormitoryQuitApply> implements SmtDormitoryQuitApplyService {

	@Value("${spring.release.sms-url}")
	private String smsUrl;
	@Value("${spring.msg-push.quitdor}")
	private String pushUrl;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private ApprovalService approvalService;
	@Autowired
	private ApproveListService approveListService;
	@Autowired
	private RemoteSmsManageService remoteSmsManageService;
	@Autowired
	private SmtApprovalNodeService smtApprovalNodeService;
	@Autowired
	private SmtMsgTemplateService smtMsgTemplateService;
	@Autowired
	private SmtDormitoryStaffService smtDormitoryStaffService;
	@Autowired
	private SmtDormitoryRoomService smtDormitoryRoomService;


	@Override
	public IPage<SmtDormitoryQuitApply> getPage(Page page, DormitoryQuitApplyQueryDTO reqDTO) {
		if (Objects.nonNull(reqDTO.getRoomNum())) {
			List<SmtDormitoryRoom> roomId = smtDormitoryRoomService.list(Wrappers.<SmtDormitoryRoom>query().lambda()
					.like(SmtDormitoryRoom::getRoomName, reqDTO.getRoomNum()));
			if (CollUtil.isNotEmpty(roomId)) {
				List<Integer> roomIds = roomId.stream().map(SmtDormitoryRoom::getId).collect(Collectors.toList());
				reqDTO.setRoomIds(roomIds);
			}
		}
		List<Integer> parkList = SecurityUtils.getUser().getParkIdList();
		reqDTO.setParkList(parkList);
		return baseMapper.getPage(page, reqDTO);
	}

	@Override
	public IPage<SmtDormitoryQuitApply> getApprovalList(Page page, QuitDorApplyQueryReqDTO query) {
		List<Integer> approvalStatus = new ArrayList<>();
		String badge = SecurityUtils.getUser().getUsername();
		if (OneOrZeroEnum.ZERO.getCode().equals(query.getIsSecurityGuard())) {
			if (ApproveListStateEnum.PENDING.getCode().equals(query.getStatus())) {
				//查询待审批
				return this.page(page, Wrappers.<SmtDormitoryQuitApply>query().lambda()
						.eq(SmtDormitoryQuitApply::getParkId, query.getParkId())
						.eq(Objects.nonNull(query.getBadge()), SmtDormitoryQuitApply::getBadge, query.getBadge())
						.like(Objects.nonNull(query.getName()), SmtDormitoryQuitApply::getName, query.getName())
						.eq(SmtDormitoryQuitApply::getStatus, ArticlesReleaseStatusEnum.APPROVED.getCode())
						.orderByDesc(SmtDormitoryQuitApply::getCreateTime));
			} else {
				//查询已审批
				return this.page(page, Wrappers.<SmtDormitoryQuitApply>query().lambda()
						.eq(SmtDormitoryQuitApply::getParkId, query.getParkId())
						.eq(Objects.nonNull(query.getBadge()), SmtDormitoryQuitApply::getBadge, query.getBadge())
						.like(Objects.nonNull(query.getName()), SmtDormitoryQuitApply::getName, query.getName())
						.ge(SmtDormitoryQuitApply::getStatus, ArticlesReleaseStatusEnum.APPROVAL_FAILED.getCode())
						.orderByDesc(SmtDormitoryQuitApply::getCreateTime));
			}
		}
		if (ApproveListStateEnum.PENDING.getCode().equals(query.getStatus())) {
			//查询待审批
			approvalStatus.add(ApproveListStateEnum.PENDING.getCode());
		} else {
			//查询已审批
			approvalStatus.add(ApproveListStateEnum.AGREE.getCode());
			approvalStatus.add(ApproveListStateEnum.REFUSE.getCode());
		}
		List<ApproveList> pendingApprove = approveListService.getByType(approvalStatus, ApproveListTypeConstants.QUIT_DORMITORY, badge);
		if (CollUtil.isEmpty(pendingApprove)) {
			return new Page<>();
		}
		List<Long> businessId = pendingApprove.stream().map(approve -> {
			return Long.parseLong(approve.getBusinessId());
		}).collect(Collectors.toList());
		return this.page(page, Wrappers.<SmtDormitoryQuitApply>query().lambda()
				.in(SmtDormitoryQuitApply::getId, businessId)
				.orderByDesc(SmtDormitoryQuitApply::getCreateTime));
	}

	@Override
	public IPage<SmtDormitoryQuitApply> getCheckList(Page page, Integer parkId) {
		return this.page(page, Wrappers.<SmtDormitoryQuitApply>query().lambda()
				.in(Objects.nonNull(parkId), SmtDormitoryQuitApply::getParkId, parkId)
				.eq(SmtDormitoryQuitApply::getStatus, ArticlesReleaseStatusEnum.APPROVED.getCode())
				.orderByDesc(SmtDormitoryQuitApply::getCreateTime));
	}

	@Override
	public SmtDormitoryQuitApply getCheckByCode(String code) {
		if (SymbolConstants.ZERO_STRING.toString().equals(code)) {
			throw new SmartException("该预约码已失效");
		}
		SmtDormitoryQuitApply apply = this.getOne(Wrappers.<SmtDormitoryQuitApply>query().lambda()
				.eq(Objects.nonNull(code), SmtDormitoryQuitApply::getSmsCode, code));
		if (Objects.isNull(apply)) {
			throw new SmartException("该预约码已失效");
		}
		return apply;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean status(Long id, String approveBadge, Integer status, String remark) {
		SmtDormitoryQuitApply quitApply = this.getById(id);
		SmtStaff smtStaff = smtStaffService.getSimpleSttaffByBadge(approveBadge);
		if (Objects.isNull(smtStaff)) {
			throw new SmartException("员工为空");
		}
		if (ArticlesReleaseStatusEnum.DEPARTURE.getCode().equals(status)
				|| ArticlesReleaseStatusEnum.REFUSE.getCode().equals(status)) {
			quitApply.setSecurityStaff(smtStaff.getName());
			quitApply.setLeaveTime(LocalDateTime.now());
			quitApply.setStatus(status);
			quitApply.setSmsCode(SymbolConstants.ZERO_STRING.toString());
			if (StringUtils.isNotEmpty(remark)) {
				quitApply.setSecurityRemark(remark);
			}
			if (ArticlesReleaseStatusEnum.DEPARTURE.getCode().equals(status)) {
				//延时退宿
				//this.quitDor(quitApply.getRoomIds(), quitApply.getStaffId(), quitApply.getQuitReason());
			}
			return this.updateById(quitApply);
		}
		String businessId = id.toString();
		ApproveList approveList = approveListService.getOne(Wrappers.<ApproveList>query().lambda()
				.eq(ApproveList::getBusinessId, id.toString())
				.eq(ApproveList::getApproveBadge, approveBadge)
				.eq(ApproveList::getApproveState, ApproveListStateEnum.PENDING.getCode()));
		approveList.setApproveState(ArticlesReleaseStatusEnum.APPROVED.getCode().equals(status) ?
				ApproveListStateEnum.AGREE.getCode() : ApproveListStateEnum.REFUSE.getCode());
		if (StringUtils.isNotEmpty(remark)) {
			approveList.setRemark(remark);
		}
		approveList.setCreateTime(LocalDateTime.now());
		approveListService.updateById(approveList);
		Integer result = approveListService.updateProcessStatus(businessId, quitApply.getBadge(), approveList.getApproveState(), approveList.getId());
		List<Integer> roomIdList = ToolUtils.splitInt(quitApply.getRoomIds());
		List<SmtDormitoryStaff> staffs = smtDormitoryStaffService.list(Wrappers.<SmtDormitoryStaff>query().lambda()
				.in(SmtDormitoryStaff::getRoomId, roomIdList).eq(SmtDormitoryStaff::getStaffBadge, quitApply.getBadge()));
		List<String> roomNameList = new ArrayList<>();
		staffs.forEach(staff -> {
			roomNameList.add(staff.getDormitoryName() + staff.getRoomName());
		});
		String head = "退宿申请-" + DateUtils.convert("yyyyMMdd", quitApply.getCreateTime());
		SmtMsgTemplate template;
		String msg;
		switch (ApprovalProcessResultEnum.getEnmu(result)) {
			case ALL_PASS:
				//发起人：审批全部通过通知
				template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_DORMITORY_QUIT_11203.getCode());
				msg = template.getTempContent().replace("{申请人房间号}", StringUtils.join(SymbolConstants.COMMA, roomNameList))
						.replace("{申请人}", quitApply.getName())
						.replace("{单子标题}", head);
				try {
					WeChatMsgUtil.sendMsg(quitApply.getBadge(), msg, null, pushUrl.replace("{id}", quitApply.getId().toString()));
				} catch (Exception e) {
					log.error("消息推送失败:{}", e.getMessage());
				}
				quitApply.setSmsCode(RandomUtil.randomNumbers(6));
				quitApply.setPassTime(LocalDateTime.now());
				quitApply.setStatus(ArticlesReleaseStatusEnum.APPROVED.getCode());
				break;
			case PART_PASS:
				quitApply.setStatus(ArticlesReleaseStatusEnum.PENDING_APPROVAL.getCode());
				List<ApproveList> pendingApprove = approveListService.getByStatus(ApproveListStateEnum.PENDING.getCode(), businessId, null);
				for (ApproveList list : pendingApprove) {
					SmtApprovalNode node = smtApprovalNodeService.getById(list.getNodeId());
					if (OneOrZeroEnum.ZERO.getCode().equals(node.getIsWeChatPush())) {
						break;
					}
					//审批人：待审批通知
					if (ApprovalPersonRuleEnum.ROOMMATE.getCode().equals(node.getIsExistApprover())) {
						template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_DORMITORY_QUIT_11201.getCode());
					} else {
						template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_DORMITORY_QUIT_11202.getCode());
					}
					msg = template.getTempContent().replace("{申请人房间号}", StringUtils.join(SymbolConstants.COMMA, roomNameList))
							.replace("{申请人}", quitApply.getName())
							.replace("{单子标题}", head);
					try {
						WeChatMsgUtil.sendMsg(list.getApproveBadge(), msg, null, pushUrl.replace("{id}", quitApply.getId().toString()));
					} catch (Exception e) {
						log.error("消息推送失败:{}", e.getMessage());
					}
				}
				break;
			case PART_REFUSE:
			case ALL_REFUSE:
				//发起人：审批拒绝通知
				template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_DORMITORY_QUIT_11204.getCode());
				msg = template.getTempContent().replace("{申请人房间号}", StringUtils.join(SymbolConstants.COMMA, roomNameList))
						.replace("{申请人}", quitApply.getName())
						.replace("{拒绝原因描述}", remark)
						.replace("{单子标题}", head);
				try {
					WeChatMsgUtil.sendMsg(quitApply.getBadge(), msg, null, pushUrl.replace("{id}", quitApply.getId().toString()));
				} catch (Exception e) {
					log.error("消息推送失败:{}", e.getMessage());
				}
				quitApply.setStatus(ArticlesReleaseStatusEnum.APPROVAL_FAILED.getCode());
				break;
			default:
				break;
		}
		return this.updateById(quitApply);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean dealyQuit() {
		log.info("开始延迟退宿执行");
		List<SmtDormitoryQuitApply> list = this.list(Wrappers.<SmtDormitoryQuitApply>lambdaQuery()
				.in(SmtDormitoryQuitApply::getStatus,
						ArticlesReleaseStatusEnum.APPROVED.getCode(), ArticlesReleaseStatusEnum.DEPARTURE.getCode())
				.eq(SmtDormitoryQuitApply::getIsHandle, OneOrZeroEnum.ZERO.getCode()));
		if (CollUtil.isEmpty(list)) {
			return null;
		}
		list.forEach(quit -> {
			List<Integer> roomIdList = ToolUtils.splitInt(quit.getRoomIds());
			List<SmtDormitoryStaff> staffs = smtDormitoryStaffService.list(Wrappers.<SmtDormitoryStaff>query().lambda()
					.in(SmtDormitoryStaff::getRoomId, roomIdList)
					.eq(SmtDormitoryStaff::getStaffBadge, quit.getBadge()));
			if (CollUtil.isEmpty(staffs)) {
				log.error("房间id：{}，工号：{}，入住记录为空，无法办理退宿", roomIdList, quit.getBadge());
				return;
			}
			staffs.forEach(room -> smtDormitoryStaffService.checkOutDormitory(room.getId(), quit.getQuitReason()));
			quit.setIsHandle(OneOrZeroEnum.ONE.getCode());
			this.updateById(quit);
		});
		log.info("结束延迟退宿执行");
		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveApply(DormitoryQuitApplyEditReqDTO reqDTO) {
		String badge = reqDTO.getBadge();
		if (StringUtils.isEmpty(badge)) {
			throw new SmartException("申请人为空");
		}
		if (CollUtil.isEmpty(reqDTO.getRoomIds())) {
			throw new SmartException("申请退宿宿舍为空");
		}
		//是否重复申请
		this.checkRooms(reqDTO);
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(badge);
		if (Objects.isNull(staff)) {
			throw new SmartException("员工为空");
		}
		SmtDormitoryQuitApply quitApply = BeanUtils.transform(SmtDormitoryQuitApply.class, reqDTO);
		quitApply.setStaffId(staff.getId());
		//保存退宿宿舍
		quitApply.setRoomIds(StringUtils.join(SymbolConstants.COMMA, reqDTO.getRoomIds()));
		//保存图片
		if (CollUtil.isNotEmpty(reqDTO.getImgs())) {
			quitApply.setImgs(StringUtils.join(SymbolConstants.COMMA, reqDTO.getImgs()));
		}
		quitApply.setStatus(ArticlesReleaseStatusEnum.PENDING_APPROVAL.getCode());
		quitApply.setIsHandle(OneOrZeroEnum.ZERO.getCode());
		if (this.save(quitApply)) {
			//判断是否存在待审批记录，若不存在则所有审批通过
			Boolean flag = Boolean.TRUE;
			//获得审批人列表
			reqDTO.setId(quitApply.getId());
			List<ApproveProcessListReqDTO> approveProcessList = this.getApprovePersonList(reqDTO);
			//微信推送参数
			SmtMsgTemplate template;
			String msg;
			String head = "退宿申请-" + DateUtils.convert("yyyyMMdd", quitApply.getCreateTime());
			List<Integer> roomIdList = ToolUtils.splitInt(quitApply.getRoomIds());
			List<SmtDormitoryStaff> staffs = smtDormitoryStaffService.list(Wrappers.<SmtDormitoryStaff>query().lambda()
					.in(SmtDormitoryStaff::getRoomId, roomIdList).eq(SmtDormitoryStaff::getStaffBadge, quitApply.getBadge()));
			List<String> roomNameList = new ArrayList<>();
			staffs.forEach(staffRoom -> {
				roomNameList.add(staffRoom.getDormitoryName() + staffRoom.getRoomName());
			});
			for (ApproveProcessListReqDTO approve : approveProcessList) {
				ApproveList approveList = BeanUtils.transform(ApproveList.class, approve);
				approveList.setApproveType(ApproveListTypeConstants.QUIT_DORMITORY);
				approveList.setApproveName(quitApply.getName() + "提交的退宿申请申请");
				//保存审批记录
				approveListService.saveApproveList(approveList);

				if (approveList.getApproveState().equals(ApproveListStateEnum.PENDING.getCode())) {
					flag = Boolean.FALSE;
					//审批人微信审批消息推送
					if (ApprovalPersonRuleEnum.ROOMMATE.getCode().equals(approve.getIsExistApprover())) {
						template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_DORMITORY_QUIT_11201.getCode());
					} else {
						template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_DORMITORY_QUIT_11202.getCode());
					}
					msg = template.getTempContent().replace("{申请人房间号}", StringUtils.join(SymbolConstants.COMMA, roomNameList))
							.replace("{申请人}", quitApply.getName())
							.replace("{单子标题}", head);
					try {
						WeChatMsgUtil.sendMsg(approveList.getApproveBadge(), msg, null, pushUrl.replace("{id}", quitApply.getId().toString()));
					} catch (Exception e) {
						log.error("消息推送失败:{}", e.getMessage());
					}
				}
			}
			if (flag) {
				//所有人审批通过，修改物品放行审批状态
				quitApply.setPassTime(LocalDateTime.now());
				quitApply.setStatus(ArticlesReleaseStatusEnum.APPROVED.getCode());
				this.updateById(quitApply);
				//发起人审批通过消息推送
				template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_DORMITORY_QUIT_11203.getCode());
				msg = template.getTempContent().replace("{申请人房间号}", StringUtils.join(SymbolConstants.COMMA, roomNameList))
						.replace("{申请人}", quitApply.getName())
						.replace("{单子标题}", head);

				try {
					WeChatMsgUtil.sendMsg(quitApply.getBadge(), msg, null, pushUrl.replace("{id}", quitApply.getId().toString()));
				} catch (Exception e) {
					log.error("消息推送失败:{}", e.getMessage());
				}
			}
		}
		return Boolean.TRUE;
	}

	private void checkRooms(DormitoryQuitApplyEditReqDTO reqDTO) {
		List<SmtDormitoryQuitApply> reList = this.list(Wrappers.<SmtDormitoryQuitApply>query().lambda()
				.eq(SmtDormitoryQuitApply::getBadge, reqDTO.getBadge())
				.notIn(SmtDormitoryQuitApply::getStatus, ArticlesReleaseStatusEnum.APPROVAL_FAILED.getCode(),
						ArticlesReleaseStatusEnum.REFUSE.getCode()));
		if (CollUtil.isEmpty(reList)) {
			return;
		}
		//新申请宿舍
		List<Integer> reRooms = reqDTO.getRoomIds();
		reList.forEach(quitApply -> {
			int[] rooms = StringUtils.splitToInt(quitApply.getRoomIds(), SymbolConstants.COMMA);
			List<Integer> returnList = new ArrayList<>();
			returnList.addAll(IntStream.of(rooms).boxed().collect(Collectors.toList()));
			List<Integer> allIds = returnList.stream().filter(item -> reRooms.contains(item)).collect(Collectors.toList());
			if (CollUtil.isEmpty(allIds)) {
				return;
			}
			if(quitApply.getIsHandle().equals(1)) {
				List<SmtDormitoryStaff> smtDormitoryStaff = smtDormitoryStaffService.list(Wrappers.<SmtDormitoryStaff>query().lambda()
						.eq(SmtDormitoryStaff::getStaffBadge, quitApply.getBadge())
						.in(SmtDormitoryStaff::getRoomId, returnList));
				if(CollUtil.isNotEmpty(smtDormitoryStaff)) {
					return;
				}
			}
			List<SmtDormitoryRoom> roomList = smtDormitoryRoomService.list(Wrappers.<SmtDormitoryRoom>query().lambda().in(SmtDormitoryRoom::getId, allIds));
			List<Integer> roomName = roomList.stream().map(SmtDormitoryRoom::getRoomName).collect(Collectors.toList());
			throw new SmartException("房间号为：" + StringUtils.join(SymbolConstants.COMMA, roomName) + "的房间已申请过退宿");
		});
	}

	/**
	 * 请求审批人列表
	 *
	 * @param quitApply
	 * @return
	 */
	private List<ApproveProcessListReqDTO> getApprovePersonList(DormitoryQuitApplyEditReqDTO quitApply) {
		ApprovalProcessReqDTO approvalProcess = new ApprovalProcessReqDTO();
		approvalProcess.setBusinessId(quitApply.getId().toString());
		approvalProcess.setDormitoryIds(quitApply.getDormitoryIds().stream().distinct().collect(Collectors.toList()));
		approvalProcess.setEventId(ApproveListTypeConstants.QUIT_DORMITORY);
		approvalProcess.setParkId(quitApply.getParkId());
		approvalProcess.setRoomId(quitApply.getRoomIds());
		approvalProcess.setQuitReason(quitApply.getQuitReason());
		approvalProcess.setApplyBadge(quitApply.getBadge());
		List<ApproveProcessListReqDTO> approveProcessList = approvalService.approvalProcess(approvalProcess);
		return approveProcessList;
	}

}
