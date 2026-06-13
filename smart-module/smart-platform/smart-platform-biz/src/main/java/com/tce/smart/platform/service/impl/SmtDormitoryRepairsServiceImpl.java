package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.msg.req.SendMsgReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.platform.api.dto.req.*;
import com.tce.smart.platform.api.dto.req.approval.ApprovalProcessReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtDormitoryRepairsRespVO;
import com.tce.smart.platform.api.dto.resp.approval.ApprovalProcessRecordReqDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApproveProcessListReqDTO;
import com.tce.smart.platform.api.dto.resp.dormitoryrepairs.SmtDormitoryRepairsDetailDTO;
import com.tce.smart.platform.core.dto.AppMsgPushDTO;
import com.tce.smart.platform.core.dto.SmtDormitoryRepairsDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtDormitoryRepairsMapper;
import com.tce.smart.platform.core.service.SmtApprovalNodeService;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.platform.core.vo.SmtDormitoryRepairsDetailVO;
import com.tce.smart.platform.core.vo.SmtDormitoryRepairsVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.approval.ApprovalService;
import com.tce.smart.tool.util.WeChatMsgUtil;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @description: SmtDormitoryRepairsServiceImpl
 * @date: 2020-07-20 13:59
 * @author: wuling
 * @version: 1.0
 */
@Service
@RequiredArgsConstructor
public class SmtDormitoryRepairsServiceImpl extends ServiceImpl<SmtDormitoryRepairsMapper, SmtDormitoryRepairs> implements SmtDormitoryRepairsService {

	private final SmtDormitoryRepairsMapper smtDormitoryRepairsMapper;

	private final SmtStaffService smtStaffService;

	private final SmtImageService smtImageService;

	private final SmtRepairsReplyService smtRepairsReplyService;

	private final ImageService imageService;

	private final ApproveListService approveListService;

	private final ApprovalService approvalService;

	private final RemoteSmsManageService remoteSmsManageService;

	private final IAppMsgPushService appMsgPushService;

	private final SmtApprovalNodeService smtApprovalNodeService;

	private final SmtMsgTemplateService smtMsgTemplateService;

	private final SmtDormitoryService smtDormitoryService;

	@Value("${spring.msg-push.repairs}")
	private String pushUrl;

	@Override
	public IPage<SmtDormitoryRepairsVO> getDormitoryRepairsPage(Page page, SmtDormitoryRepairsReqDTO smtDormitoryRepairsReqDTO) {
		//当前登录用户所属的园区ID列表
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		SmtDormitoryRepairsDTO smtDormitoryRepairsDTO = new SmtDormitoryRepairsDTO();
		BeanUtils.copyProperties(smtDormitoryRepairsReqDTO, smtDormitoryRepairsDTO);
		IPage<SmtDormitoryRepairsVO> dormitoryRepairsPage = smtDormitoryRepairsMapper
				.getDormitoryRepairsPage(page, smtDormitoryRepairsDTO, parkIdList);
		//设置状态和类型的描述信息
		for (SmtDormitoryRepairsVO smtDormitoryRepairsVO : dormitoryRepairsPage.getRecords()) {
			smtDormitoryRepairsVO.setRepairTypeDesc(RepairSTypeEnum.desc(smtDormitoryRepairsVO.getRepairType()));
			smtDormitoryRepairsVO.setRangeTypeDesc(RangeTypeEnum.desc(smtDormitoryRepairsVO.getRangeType()));
			smtDormitoryRepairsVO.setStatusDesc(DormitoryRepairStatusEnum.desc(smtDormitoryRepairsVO.getStatus()));
		}
		return dormitoryRepairsPage;
	}

	@Override
	public IPage<SmtDormitoryRepairsVO> getDormitoryRepairsPage(Page page, SmtDormitoryRepairsReqYutoDTO dto) {
		//当前登录用户所属的园区ID列表
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		SmtDormitoryRepairsDTO smtDormitoryRepairsDTO = new SmtDormitoryRepairsDTO();
		BeanUtils.copyProperties(dto, smtDormitoryRepairsDTO);
		List<Integer> rangeType = new ArrayList<>();
		if (Objects.nonNull(dto.getRangeType())) {
			rangeType.add(dto.getRangeType());
		}
		smtDormitoryRepairsDTO.setRange(rangeType);
		IPage<SmtDormitoryRepairsVO> dormitoryRepairsPage = smtDormitoryRepairsMapper
				.getDormitoryRepairsPage(page, smtDormitoryRepairsDTO, parkIdList);
		SimpleDateFormat formatDay = new SimpleDateFormat(DateUtils.DEFAULT_DATE_TIME_FORMAT);
		//设置状态和类型的描述信息
		for (SmtDormitoryRepairsVO smtDormitoryRepairsVO : dormitoryRepairsPage.getRecords()) {
			smtDormitoryRepairsVO.setRepairTypeDesc(RepairSTypeEnum.desc(smtDormitoryRepairsVO.getRepairType()));
			smtDormitoryRepairsVO.setRangeTypeDesc(RangeTypeEnum.desc(smtDormitoryRepairsVO.getRangeType()));
			smtDormitoryRepairsVO.setStatusDesc(DormitoryRepairStatusEnum.desc(smtDormitoryRepairsVO.getStatus()));
			smtDormitoryRepairsVO.setCreateTime(DateUtils.format(DateUtils.parse(smtDormitoryRepairsVO.getCreateTime()), formatDay));
		}
		return dormitoryRepairsPage;
	}

	@Transactional
	@Override
	public boolean addDormitoryRepairs(SmtDormitoryRepairsAddReqDTO smtDormitoryRepairsAddReqDTO) {
		String staffBadge = SecurityUtils.getUser().getUsername();
		//先保存图片
		StringBuilder imgStrs = new StringBuilder();
		if (CollectionUtil.isNotEmpty(smtDormitoryRepairsAddReqDTO.getFaultImgs())) {
			for (String sImg : smtDormitoryRepairsAddReqDTO.getFaultImgs()) {
				String imageCode = smtImageService.saveImage(smtDormitoryRepairsAddReqDTO.getParkId(), sImg, SmtImageEnum.DORMITORY_REPAIRS.getCode());
				if (!imgStrs.toString().equals("")) {
					imgStrs.append(",");
				}
				imgStrs.append(imageCode);
			}
		}
		//保存报修记录
		SmtDormitoryRepairs smtDormitoryRepairs = SmtDormitoryRepairs.builder()
				.staffBadge(staffBadge)
				.rangeType(smtDormitoryRepairsAddReqDTO.getRangeType())
				.repairType(smtDormitoryRepairsAddReqDTO.getRepairType())
				.dormitoryName(smtDormitoryRepairsAddReqDTO.getDormitoryName())
				.roomName(smtDormitoryRepairsAddReqDTO.getRoomName())
				.parkId(smtDormitoryRepairsAddReqDTO.getParkId())
				.faultDesc(smtDormitoryRepairsAddReqDTO.getFaultDesc())
				.faultImgs(imgStrs.toString())
				.status(DormitoryRepairStatusEnum.WAIT_APPROVAL.getCode())
				.createTime(new Date())
				.build();
		this.save(smtDormitoryRepairs);
		return this.saveApproval(smtDormitoryRepairs);
	}

	/**
	 * 添加审批记录
	 *
	 * @param smtDormitoryRepairs
	 * @return
	 */
	private Boolean saveApproval(SmtDormitoryRepairs smtDormitoryRepairs) {
		List<ApproveProcessListReqDTO> approvePersonList = this.getApprovePersonList(smtDormitoryRepairs);
		//判断是否存在待审批记录，若不存在则所有审批通过
		Boolean flag = Boolean.TRUE;
		String applyName = smtStaffService.getSimpleSttaffByBadge(smtDormitoryRepairs.getStaffBadge()).getName();
		for (ApproveProcessListReqDTO approve : approvePersonList) {
			ApproveList approveList = BeanUtils.transform(ApproveList.class, approve);
			approveList.setApproveName(applyName + "提交的园区报修申请");
			if (approveList.getApproveState().equals(ApproveListStateEnum.PENDING.getCode())) {
				//为审批人发送app push消息
				if (OneOrZeroEnum.ONE.getCode().equals(approve.getIsAppPush())) {
					this.sendAppPush(approveList.getApproveBadge(), applyName, smtDormitoryRepairs.getId().toString(), SmsTemplateEnum.APP_REPAIR_10801.getCode());
				}
				//为审批人发送短信消息
				if (Objects.nonNull(approve.getMsgTemplate())) {
					SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(approve.getApproveBadge());
					if (Objects.nonNull(staff)) {
						this.sendApprovePassMsg(applyName, staff.getPhone(), approve.getMsgTemplate(), RangeTypeEnum.desc(smtDormitoryRepairs.getRangeType()));
					}
				}
				if (OneOrZeroEnum.ONE.getCode().equals(approve.getIsWeChatPush())) {
					SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_REPAIR_11301.getCode());
					String msg = template.getTempContent().replace("{申请人房间号}", smtDormitoryRepairs.getDormitoryName() + smtDormitoryRepairs.getRoomName())
							.replace("{申请人}", applyName)
							.replace("{单子标题}", "报修申请-" + DateUtil.format(smtDormitoryRepairs.getCreateTime(), "yyyyMMdd"));
					WeChatMsgUtil.sendMsg(approve.getApproveBadge(), msg, null, pushUrl.replace("{id}", smtDormitoryRepairs.getId().toString()));
				}
				flag = Boolean.FALSE;
			}
			//保存审批记录
			approveListService.saveApproveList(approveList);
		}
		if (flag) {
			//所有人审批通过，修改物品放行审批状态
			smtDormitoryRepairs.setStatus(DormitoryRepairStatusEnum.WAIT_CONFIRM.getCode());
			this.updateById(smtDormitoryRepairs);
		}
		return Boolean.TRUE;
	}

	/**
	 * 请求审批人列表
	 *
	 * @param smtDormitoryRepairs
	 * @return
	 */
	private List<ApproveProcessListReqDTO> getApprovePersonList(SmtDormitoryRepairs smtDormitoryRepairs) {
		ApprovalProcessReqDTO approvalProcess = new ApprovalProcessReqDTO();
		approvalProcess.setBusinessId(smtDormitoryRepairs.getId().toString());
		approvalProcess.setEventId(ApproveListTypeConstants.REPAIR);
		approvalProcess.setParkId(smtDormitoryRepairs.getParkId());
		approvalProcess.setApplyBadge(smtDormitoryRepairs.getStaffBadge());
		approvalProcess.setRangeType(smtDormitoryRepairs.getRangeType());
		approvalProcess.setRepairType(smtDormitoryRepairs.getRepairType());
		//计算宿舍楼
		if (StringUtils.isNotEmpty(smtDormitoryRepairs.getDormitoryName()) && Objects.nonNull(smtDormitoryRepairs.getParkId())) {
			SmtDormitory dormitory = smtDormitoryService.getOne(Wrappers.<SmtDormitory>query().lambda()
					.eq(SmtDormitory::getParkId, smtDormitoryRepairs.getParkId())
					.eq(SmtDormitory::getDormitoryName, smtDormitoryRepairs.getDormitoryName()));
			if (Objects.nonNull(dormitory)) {
				approvalProcess.setDormitoryId(dormitory.getId());
			}
		}
		List<ApproveProcessListReqDTO> approveProcessList = approvalService.approvalProcess(approvalProcess);
		if (CollUtil.isEmpty(approveProcessList)) {
			throw new SmartException("该园区园区报修审批设置未添加审批人");
		}
		return approveProcessList;
	}

	@Override
	public IPage<SmtDormitoryRepairsRespVO> getDormitoryRepairsPageByStaff(Page page) {
		String staffBadge = SecurityUtils.getUser().getUsername();

		//设置查询条件  只根据员工工号查询
		SmtDormitoryRepairsDTO smtDormitoryRepairsDTO = new SmtDormitoryRepairsDTO();
		smtDormitoryRepairsDTO.setStaffBadge(staffBadge);
		smtDormitoryRepairsDTO.setRange(RangeTypeEnum.codelist());
		IPage dormitoryRepairsPage = smtDormitoryRepairsMapper.getDormitoryRepairsPage(page, smtDormitoryRepairsDTO, null);
		//设置状态和类型的描述信息
		List<SmtDormitoryRepairsRespVO> smtDormitoryRepairsRespVOS = new ArrayList<>();
		for (Object obj : dormitoryRepairsPage.getRecords()) {
			SmtDormitoryRepairsVO smtDormitoryRepairsVO = (SmtDormitoryRepairsVO) obj;
			smtDormitoryRepairsVO.setRangeTypeDesc(RangeTypeEnum.desc(smtDormitoryRepairsVO.getRangeType()));
			smtDormitoryRepairsVO.setRepairTypeDesc(RepairSTypeEnum.desc(smtDormitoryRepairsVO.getRepairType()));
			smtDormitoryRepairsVO.setStatusDesc(DormitoryRepairStatusEnum.WAIT_CONFIRM.getCode()
					.equals(smtDormitoryRepairsVO.getStatus()) ? DormitoryRepairStatusEnum.WAIT_REPAIR.getDesc()
					: DormitoryRepairStatusEnum.desc(smtDormitoryRepairsVO.getStatus()));
			SmtDormitoryRepairsRespVO smtDormitoryRepairsRespVO = new SmtDormitoryRepairsRespVO();
			BeanUtils.copyProperties(smtDormitoryRepairsVO, smtDormitoryRepairsRespVO);
			smtDormitoryRepairsRespVOS.add(smtDormitoryRepairsRespVO);
		}
		dormitoryRepairsPage.setRecords(smtDormitoryRepairsRespVOS);
		return dormitoryRepairsPage;
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateStatus(Long id, String approveBadge, Integer status, String remark) {
		SmtDormitoryRepairs smtDormitoryRepairs = this.getById(id);
		if (Objects.isNull(smtDormitoryRepairs)) {
			throw new SmartException("此园区报修记录不存在");
		}
		SmtStaff smtStaff = smtStaffService.getSimpleSttaffByBadge(smtDormitoryRepairs.getStaffBadge());
		String businessId = id.toString();
		ApproveList approveList = approveListService.getOne(Wrappers.<ApproveList>query().lambda()
				.eq(ApproveList::getBusinessId, businessId)
				.eq(ApproveList::getApproveBadge, approveBadge)
				.eq(ApproveList::getApproveState, ApproveListStateEnum.PENDING.getCode()));
		approveList.setApproveState(status);
		approveList.setRemark(remark);
		approveList.setUpdateTime(LocalDateTime.now());
		approveListService.updateById(approveList);
		Integer result = approveListService.updateProcessStatus(businessId, smtDormitoryRepairs.getStaffBadge(), status, approveList.getId());
		switch (ApprovalProcessResultEnum.getEnmu(result)) {
			case ALL_PASS:
				this.sendAppPush(smtDormitoryRepairs.getStaffBadge(), null, businessId, SmsTemplateEnum.APP_REPAIR_10802.getCode());
				smtDormitoryRepairs.setStatus(DormitoryRepairStatusEnum.WAIT_CONFIRM.getCode());
				break;
			case PART_PASS:
				SmtApprovalNode node = smtApprovalNodeService.getById(approveList.getNodeId());
				if(Objects.isNull(node)) {
					break;
				}
				List<ApproveList> pendingApprove = approveListService.getByStatus(ApproveListStateEnum.PENDING.getCode(), businessId, null);
				if (OneOrZeroEnum.ONE.getCode().equals(node.getIsAppPush())) {
					for (ApproveList list : pendingApprove) {
						this.sendAppPush(list.getApproveBadge(), smtStaff.getName(), businessId, SmsTemplateEnum.APP_REPAIR_10801.getCode());
					}
				}
				//为审批人发送短信消息
				if (Objects.nonNull(node.getMsgTemplate())) {
					for (ApproveList list : pendingApprove) {
						SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(list.getApproveBadge());
						if (Objects.nonNull(staff)) {
							this.sendApprovePassMsg(smtStaff.getName(), staff.getPhone(), node.getMsgTemplate(), RangeTypeEnum.desc(smtDormitoryRepairs.getRangeType()));
						}
					}
				}
				if (OneOrZeroEnum.ONE.getCode().equals(node.getIsWeChatPush())) {
					SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_REPAIR_11301.getCode());
					for (ApproveList list : pendingApprove) {
						this.sendAppPush(list.getApproveBadge(), smtStaff.getName(), businessId, SmsTemplateEnum.APP_REPAIR_10801.getCode());
						String msg = template.getTempContent().replace("{申请人房间号}", smtDormitoryRepairs.getDormitoryName() + smtDormitoryRepairs.getRoomName())
								.replace("{申请人}", smtStaff.getName())
								.replace("{单子标题}", "报修申请-" + DateUtil.format(smtDormitoryRepairs.getCreateTime(), "yyyyMMdd"));
						try {
							WeChatMsgUtil.sendMsg(list.getApproveBadge(),msg, null, pushUrl.replace("{id}", smtDormitoryRepairs.getId().toString()));
						} catch (Exception e) {
							log.error("园区报修-通知处理人微信消息推送失败：{}" + e.getMessage());
						}
					}
				}
				smtDormitoryRepairs.setStatus(DormitoryRepairStatusEnum.WAIT_APPROVAL.getCode());
				break;
			case ALL_REFUSE:
				this.sendAppPush(smtDormitoryRepairs.getStaffBadge(), null, businessId, SmsTemplateEnum.APP_REPAIR_10803.getCode());
				smtDormitoryRepairs.setStatus(DormitoryRepairStatusEnum.REFUSE.getCode());
				break;
			default:
				break;
		}
		smtDormitoryRepairs.setUpdateTime(new Date());
		return this.updateById(smtDormitoryRepairs);
	}


	@Override
	public SmtDormitoryRepairsDetailDTO getStaffReportDetail(Long id) {
		//查询回复记录
		List<SmtRepairsReply> smtRepairsReplies = smtRepairsReplyService.list(new QueryWrapper<SmtRepairsReply>()
				.lambda().eq(SmtRepairsReply::getRepairId, id));

		//查询报修记录详情
		SmtDormitoryRepairsDetailVO smtDormitoryRepairsDetailVO = smtDormitoryRepairsMapper.getStaffReportDetail(id);

		if (smtDormitoryRepairsDetailVO == null) {
			throw new TCEException("报修记录不存在");
		}

		//查询回复记录
		if (!CollectionUtil.isEmpty(smtRepairsReplies)) {
			List<SmtDormitoryRepairsDetailVO.RepairReply> repairReplyList = new ArrayList<>();
			for (SmtRepairsReply smtRepairsReply : smtRepairsReplies) {
				String staffName = smtRepairsReply.getReplyName();
				SmtStaff staff = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, staffName));
				if (Objects.nonNull(staff)) {
					staffName = staff.getName();
				}
				repairReplyList.add(SmtDormitoryRepairsDetailVO.RepairReply.builder()
						.replyStatusDesc(DormitoryRepairStatusEnum.desc(smtRepairsReply.getReplyStatus()))
						.replyName(staffName)
						.replyDesc(smtRepairsReply.getReplyResult())
						.replyTime(smtRepairsReply.getCreateTime())
						.build());
			}
			//设置回复记录
			smtDormitoryRepairsDetailVO.setRepairReplyList(repairReplyList);
		}

		//查询报修图片记录
		if (StringUtils.isNotBlank(smtDormitoryRepairsDetailVO.getFaultImgs())) {
			String[] imgCodes = smtDormitoryRepairsDetailVO.getFaultImgs().split(",");
			//图片访问地址链接
			List<String> imgLinks = new ArrayList<>();
			for (String code : imgCodes) {
				imgLinks.add(imageService.buildImageUrl(code));
			}
			smtDormitoryRepairsDetailVO.setImgs(imgLinks);
		}

		SmtDormitoryRepairsDetailDTO smtDormitoryRepairsDetailDTO = new SmtDormitoryRepairsDetailDTO();
		BeanUtils.copyProperties(smtDormitoryRepairsDetailVO, smtDormitoryRepairsDetailDTO);

		smtDormitoryRepairsDetailDTO.setStatusDesc(DormitoryRepairStatusEnum.desc(smtDormitoryRepairsDetailVO.getStatus()));
		smtDormitoryRepairsDetailDTO.setRepairTypeDesc(RepairSTypeEnum.desc(smtDormitoryRepairsDetailVO.getRepairType()));
		smtDormitoryRepairsDetailDTO.setRangeTypeDesc(RangeTypeEnum.desc(smtDormitoryRepairsDetailVO.getRangeType()));
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		List<ApprovalProcessRecordReqDTO> approvalProcess = approveListService.getProcess(id.toString(),
				smtDormitoryRepairsDetailVO.getName(),
				LocalDateTime.parse(StringUtils.subString(smtDormitoryRepairsDetailVO.getCreateTime(), 0, 19), df));
		if (CollUtil.isNotEmpty(approvalProcess)) {
			smtDormitoryRepairsDetailDTO.setApprovalProcess(approvalProcess);
		}
		return smtDormitoryRepairsDetailDTO;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean replyRepair(ReplyRepairReqDTO replyRepairReqDTO) {
		//添加回复记录
		String userName = SecurityUtils.getUser().getUsername();
		SmtRepairsReply smtRepairsReply = SmtRepairsReply.builder()
				.repairId(replyRepairReqDTO.getId())
				.replyStatus(replyRepairReqDTO.getStatus())
				.replyResult(replyRepairReqDTO.getResult())
				.replyName(userName)
				.createTime(new Date())
				.build();
		smtRepairsReplyService.save(smtRepairsReply);

		//更新报修记录
		SmtDormitoryRepairs smtDormitoryRepairs = this.getById(replyRepairReqDTO.getId());
		smtDormitoryRepairs.setStatus(replyRepairReqDTO.getStatus());
		smtDormitoryRepairs.setResult(replyRepairReqDTO.getResult());
		smtDormitoryRepairs.setUpdateTime(new Date());
		this.updateById(smtDormitoryRepairs);

		//发送报修结果消息
		SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_REPAIR_11302.getCode());
		String msg = template.getTempContent().replace("{结果描述}", DormitoryRepairStatusEnum.desc(replyRepairReqDTO.getStatus()))
				.replace("{单子标题}", "报修申请-" + DateUtil.format(smtDormitoryRepairs.getCreateTime(), "yyyyMMdd"));
		try {
			WeChatMsgUtil.sendMsg(smtDormitoryRepairs.getStaffBadge(), msg, null,  pushUrl.replace("{id}", smtDormitoryRepairs.getId().toString()));
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return Boolean.TRUE;
	}

//	@Override
//	public List<ApproveList> getApproveList(RepairsApproveListReqDTO queryDTO) {
//		List<Integer> stateList = new ArrayList<>();
//		List<Long> businessId = new ArrayList<>();
//		if (ApproveListStateEnum.PENDING.getCode().equals(queryDTO.getRecordState())) {
//			stateList.add(ApproveListStateEnum.PENDING.getCode());
//		} else {
//			stateList.add(ApproveListStateEnum.AGREE.getCode());
//			stateList.add(ApproveListStateEnum.REFUSE.getCode());
//			stateList.add(ApproveListStateEnum.CLOSE.getCode());
//		}
//		String username = SecurityUtils.getUser().getUsername();
//		return this.baseMapper.getNewPage(page, queryDTO, stateList, username);
//	}

	/**
	 * 发送待审批短信
	 *
	 * @param name  申请人
	 * @param phone 收信人号码
	 * @return
	 */
	private void sendApprovePassMsg(String name, String phone, Integer tempId, String type) {
		SendMsgReqDTO reqDTO = new SendMsgReqDTO();
		reqDTO.setNumber(phone);
		SmtMsgTemplate smtMsgTemplate = smtMsgTemplateService.getById(tempId);
		reqDTO.setTempCode(smtMsgTemplate.getTempCode());
		String smsContent = smtMsgTemplate.getTempContent();
		if (StringUtils.isNotEmpty(name)) {
			smsContent = smsContent.replace("{applyName}", name).replace("{type}", type);
		}
		reqDTO.setContents(smsContent);
		remoteSmsManageService.sendMessage(reqDTO);
	}

	/**
	 * 发送app推送
	 *
	 * @param badge 收件人工号
	 * @param name  发起人姓名
	 * @param id    事件主键
	 */
	private void sendAppPush(String badge, String name, String id, String tempCode) {
		AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
		appMsgPushDTO.setBadge(badge);
		appMsgPushDTO.setApplicant(name);
		appMsgPushDTO.setBussiessId(id);
		appMsgPushDTO.setTemplateCode(tempCode);
		appMsgPushService.pushAppMsg(appMsgPushDTO);
	}
}
