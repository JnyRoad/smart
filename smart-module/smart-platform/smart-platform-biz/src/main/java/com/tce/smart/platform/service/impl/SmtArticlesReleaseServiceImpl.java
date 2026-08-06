package com.tce.smart.platform.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.api.dto.msg.resp.OaStaffLookupRespDTO;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.platform.api.dto.req.*;
import com.tce.smart.platform.api.dto.req.approval.ApprovalProcessReqDTO;
import com.tce.smart.platform.api.dto.resp.ArticlesReleaseListRespDTO;
import com.tce.smart.platform.api.dto.resp.OfficeReleaseDraftRespDTO;
import com.tce.smart.platform.api.dto.resp.ReleaseStaffLookupRespDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApproveProcessListReqDTO;
import com.tce.smart.platform.core.dto.AppMsgPushDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtArticlesReleaseMapper;
import com.tce.smart.platform.core.service.SmtApprovalNodeService;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.platform.core.vo.StaffInfoVO;
import com.tce.smart.platform.emun.BackFactoryEnum;
import com.tce.smart.platform.emun.ReleaseItemEnum;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.approval.ApprovalService;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.IOUtils;
import com.tce.smart.tool.util.WeChatMsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-23 16:57
 */
@Service
@Slf4j
public class SmtArticlesReleaseServiceImpl extends ServiceImpl<SmtArticlesReleaseMapper, SmtArticlesRelease> implements SmtArticlesReleaseService {
	@Value("${spring.release.sms-url}")
	private String smsUrl;
	@Value("${spring.msg-push.release}")
	private String pushUrl;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private ApprovalService approvalService;
	@Autowired
	private ApproveListService approveListService;
	@Autowired
	private SmtImageService smtImageService;
	@Autowired
	private SmtDormitoryStaffService smtDormitoryStaffService;
	@Autowired
	private IAppMsgPushService appMsgPushService;
	@Autowired
	@Lazy
	private SmtDormitoryService smtDormitoryService;
	@Autowired
	@Lazy
	private SmtDormitoryRoomService smtDormitoryRoomService;
	@Autowired
	private RemoteSmsManageService remoteSmsManageService;
	@Autowired
	private SmtApprovalNodeService smtApprovalNodeService;
	@Autowired
	private SmtMsgTemplateService smtMsgTemplateService;
	@Autowired
	private RemoteOaWorkFlowService remoteOaWorkFlowService;
	@Autowired
	private IOAWorkflowService oaWorkflowService;
	@Autowired
	private SmtProcessRecordService processRecordService;
	@Autowired
	private SmtArticlesReleasePersonService releasePersonService;
	@Autowired
	private SmtArticlesReleaseThingService releaseThingService;
	@Autowired
	private SmtArticlesReleaseMainService releaseMainService;
	@Value("${spring.image.base-url}")
	private String baseImageUrl;

	private ReleaseStaffLookupRespDTO getReleaseStaffLookup(String badge) {
		Result<OaStaffLookupRespDTO> oaInfoByBadge = remoteOaWorkFlowService.getOAInfoByBadge(badge);
		if (oaInfoByBadge == null || !oaInfoByBadge.isSuccess()) {
			throw new TCEException("查询OA员工信息失败");
		}
		OaStaffLookupRespDTO oaData = oaInfoByBadge.getData();
		if (oaData == null) {
			throw new TCEException("OA系统不存在该员工信息");
		}
		ReleaseStaffLookupRespDTO respDTO = new ReleaseStaffLookupRespDTO();
		respDTO.setId(oaData.getId());
		respDTO.setName(oaData.getName());
		return respDTO;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public OfficeReleaseDraftRespDTO createOfficeDraft(String ownerBadge, CreateOfficeReleaseDraftReqDTO request) {
		if (StrUtil.isBlank(ownerBadge) || request == null || request.getParkId() == null) {
			throw new AccessDeniedException("未认证用户不能创建物品放行草稿");
		}
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(ownerBadge);
		if (staff == null) {
			throw new SmartException("员工信息不存在");
		}
		SmtArticlesRelease draft = new SmtArticlesRelease();
		draft.setBadge(ownerBadge);
		draft.setName(staff.getName());
		draft.setCarrier(staff.getName());
		draft.setParkId(request.getParkId());
		draft.setArticlesType(ArticlesReleaseTypeEnum.XC_OFFICE_ZONE.getCode());
		draft.setArticlesDesc(ArticlesReleaseTypeEnum.XC_OFFICE_ZONE.getDesc());
		draft.setStatus(ArticlesReleaseStatusEnum.DRAFT.getCode());
		Assert.isTrue(this.save(draft), "创建物品放行草稿失败");
		OfficeReleaseDraftRespDTO response = new OfficeReleaseDraftRespDTO();
		response.setReleaseId(draft.getId());
		return response;
	}

	@Override
	public ReleaseStaffLookupRespDTO lookupStaffForRelease(String currentBadge, List<Integer> currentParkIds,
			Long releaseId, String badge) {
		// 人员选择仅属于申请人自己的未提交办公区草稿；审批/保安场景不得复用此搜索接口。
		getOfficeDraftForOwner(currentBadge, currentParkIds, releaseId);
		if (StrUtil.isBlank(badge)) {
			throw new SmartException("员工工号不能为空");
		}
		return getReleaseStaffLookup(badge);
	}

	@Override
	public SmtArticlesRelease getReleaseForAuthorizedUser(String currentBadge, List<Integer> currentParkIds, Long releaseId) {
		SmtArticlesRelease release = getByReleaseId(releaseId);
		if (StrUtil.isBlank(currentBadge) || CollUtil.isEmpty(currentParkIds)
				|| release.getParkId() == null || !currentParkIds.contains(release.getParkId())) {
			throw new AccessDeniedException("无权访问该物品放行记录");
		}
		if (currentBadge.equals(release.getBadge()) || currentBadge.equals(release.getGuardBadge())) {
			return release;
		}
		List<ApproveList> approveRecords = approveListService.list(Wrappers.<ApproveList>lambdaQuery()
				.eq(ApproveList::getBusinessId, releaseId.toString())
				.eq(ApproveList::getApproveBadge, currentBadge));
		boolean isApprover = CollUtil.isNotEmpty(approveRecords);
		if (!isApprover) {
			throw new AccessDeniedException("无权访问该物品放行记录");
		}
		return release;
	}

	private SmtArticlesRelease getOfficeDraftForOwner(String ownerBadge, List<Integer> ownerParkIds, Long releaseId) {
		SmtArticlesRelease release = getByReleaseId(releaseId);
		if (StrUtil.isBlank(ownerBadge) || !ownerBadge.equals(release.getBadge())
				|| CollUtil.isEmpty(ownerParkIds) || release.getParkId() == null || !ownerParkIds.contains(release.getParkId())
				|| !ArticlesReleaseTypeEnum.XC_OFFICE_ZONE.getCode().equals(release.getArticlesType())
				|| !ArticlesReleaseStatusEnum.DRAFT.getCode().equals(release.getStatus())) {
			throw new AccessDeniedException("无权提交该物品放行草稿");
		}
		return release;
	}

	@Override
	public SmtArticlesRelease getByApproveId(String approveId) {
		List<ApproveList> approveLists = approveListService.list(Wrappers.<ApproveList>query().lambda().eq(ApproveList::getBusinessId, approveId));
		if (Objects.isNull(approveLists) || approveLists.size() < 1) {
			throw new TCEException("获取物品放行申请记录失败");
		}
		return this.getById(Long.valueOf(approveLists.get(0).getBusinessId()));
	}

	@Override
	public SmtArticlesRelease getByReleaseId(Long id) {
		SmtArticlesRelease release = this.getById(id);
		if (release == null) {
			throw new SmartException("查询无数据");
		}
		return release;
	}

	@Override
	public IPage<SmtArticlesRelease> getArticlesReleasePage(Page page, QueryArticlesReleaseReqDTO reqDTO) {
		List<Integer> parkList = SecurityUtils.getUser().getParkIdList();
		return this.page(page, Wrappers.<SmtArticlesRelease>query().lambda()
				.ne(SmtArticlesRelease::getArticlesType, ArticlesReleaseTypeEnum.XC_OFFICE_ZONE.getCode())
				.eq(Objects.nonNull(reqDTO.getId()), SmtArticlesRelease::getId, reqDTO.getId())
				.eq(StrUtil.isNotBlank(reqDTO.getBadge()), SmtArticlesRelease::getBadge, reqDTO.getBadge())
				.eq(Objects.nonNull(reqDTO.getType()), SmtArticlesRelease::getArticlesType, reqDTO.getType())
				.like(StrUtil.isNotBlank(reqDTO.getName()), SmtArticlesRelease::getName, reqDTO.getName())
				.eq(Objects.nonNull(reqDTO.getParkId()), SmtArticlesRelease::getParkId, reqDTO.getParkId())
				.eq(Objects.nonNull(reqDTO.getStatus()), SmtArticlesRelease::getStatus, reqDTO.getStatus())
				.like(StrUtil.isNotBlank(reqDTO.getLicensePlate()), SmtArticlesRelease::getLicensePlate, reqDTO.getLicensePlate())
				.ge(StrUtil.isNotBlank(reqDTO.getStartTime()), SmtArticlesRelease::getCreateTime, StrUtil.isNotBlank(reqDTO.getStartTime()) ? DateUtils.parse(reqDTO.getStartTime()) : null)
				.le(StrUtil.isNotBlank(reqDTO.getEndTime()), SmtArticlesRelease::getCreateTime, StrUtil.isNotBlank(reqDTO.getEndTime()) ? DateUtils.parse(reqDTO.getEndTime()) : null)
				.in(CollUtil.isNotEmpty(parkList), SmtArticlesRelease::getParkId, parkList)
				.orderByDesc(SmtArticlesRelease::getCreateTime));
	}

	@Override
	public IPage<SmtArticlesRelease> getOfficeReleasePage(Page page, OfficeZoneApproveQueryDTO queryDTO) {
		List<Integer> parkList = SecurityUtils.getUser().getParkIdList();
		String username = SecurityUtils.getUser().getUsername();
		return this.baseMapper.getOfficeZoneApprovalPage(page, queryDTO, parkList, username);
	}

	@Override
	public IPage<SmtArticlesRelease> getPCOfficePage(Page page, QueryArticlesReleaseReqDTO reqDTO) {
		List<Integer> parkList = SecurityUtils.getUser().getParkIdList();
		return this.page(page, Wrappers.<SmtArticlesRelease>query().lambda()
				.eq(SmtArticlesRelease::getArticlesType, ArticlesReleaseTypeEnum.XC_OFFICE_ZONE.getCode())
				.eq(StrUtil.isNotBlank(reqDTO.getBadge()), SmtArticlesRelease::getBadge, reqDTO.getBadge())
				.like(StrUtil.isNotBlank(reqDTO.getName()), SmtArticlesRelease::getName, reqDTO.getName())
				.ge(StrUtil.isNotBlank(reqDTO.getStartTime()), SmtArticlesRelease::getCreateTime, DateUtils.parse(reqDTO.getStartTime()))
				.le(StrUtil.isNotBlank(reqDTO.getEndTime()), SmtArticlesRelease::getCreateTime, DateUtils.parse(reqDTO.getEndTime()))
				.in(CollUtil.isNotEmpty(parkList), SmtArticlesRelease::getParkId, parkList)
				.orderByDesc(SmtArticlesRelease::getCreateTime));
	}

	@Override
	public IPage<SmtArticlesRelease> getBackFactoryPage(Page page, ArticlesBackFactoryReqDTO reqDTO) {
		List<Integer> parkList = SecurityUtils.getUser().getParkIdList();
		List<Long> releaseIds = null;
		if (StrUtil.isNotBlank(reqDTO.getGoodsName())) {
			List<SmtArticlesReleaseThing> thingList = releaseThingService.list(Wrappers.<SmtArticlesReleaseThing>lambdaQuery().like(SmtArticlesReleaseThing::getWpmc, reqDTO.getGoodsName()));
			releaseIds = thingList.stream().map(SmtArticlesReleaseThing::getReleaseId).distinct().collect(Collectors.toList());
		}
		String badge = SecurityUtils.getUser().getUsername();
		return this.page(page, Wrappers.<SmtArticlesRelease>query().lambda()
				.eq(SmtArticlesRelease::getArticlesType, ArticlesReleaseTypeEnum.XC_OFFICE_ZONE.getCode())
				.eq(SmtArticlesRelease::getStatus, ArticlesReleaseStatusEnum.DEPARTURE.getCode())
				.eq(SmtArticlesRelease::getIsBack, BackFactoryEnum.YES.getCode())
				.isNotNull(SmtArticlesRelease::getDepartureTime)
				.in(CollUtil.isNotEmpty(releaseIds), SmtArticlesRelease::getId, releaseIds)
				.eq(StrUtil.isNotBlank(reqDTO.getProcessId()), SmtArticlesRelease::getProcessId, reqDTO.getProcessId())
				.eq(StrUtil.isNotBlank(reqDTO.getBadge()), SmtArticlesRelease::getBadge, reqDTO.getBadge())
				// 待审批，查询还未返厂的数据
				.isNull(Objects.nonNull(reqDTO.getApprovalStatus()) && reqDTO.getApprovalStatus().equals(0), SmtArticlesRelease::getBackTime)
				// 已审批，查询已返厂且确认人员为当前人员的数据
				.and(Objects.nonNull(reqDTO.getApprovalStatus()) && reqDTO.getApprovalStatus().equals(1),
						wrap -> wrap.isNotNull(SmtArticlesRelease::getBackTime)
						.eq(SmtArticlesRelease::getGuardBadge, badge))
				.and(StrUtil.isNotBlank(reqDTO.getName()), wrap -> wrap.like(SmtArticlesRelease::getName, reqDTO.getName()).or()
						.like(SmtArticlesRelease::getCarrier, reqDTO.getName()))
				.ge(StrUtil.isNotBlank(reqDTO.getStartTime()), SmtArticlesRelease::getCreateTime, DateUtils.parse(reqDTO.getStartTime()))
				.le(StrUtil.isNotBlank(reqDTO.getEndTime()), SmtArticlesRelease::getCreateTime, DateUtils.parse(reqDTO.getEndTime()))
				.in(CollUtil.isNotEmpty(parkList), SmtArticlesRelease::getParkId, parkList)
				.orderByDesc(SmtArticlesRelease::getCreateTime));
	}

	@Override
	public List<SmtArticlesRelease> guardGetList(Integer parkId, Integer type) {
		return list(new LambdaQueryWrapper<SmtArticlesRelease>()
				.eq(SmtArticlesRelease::getParkId, parkId)
				.eq(SmtArticlesRelease::getArticlesType, type)
				.eq(SmtArticlesRelease::getStatus, ArticlesReleaseStatusEnum.PENDING_APPROVAL.getCode())
				.orderByDesc(SmtArticlesRelease::getCreateTime));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean status(Long id, String approveBadge, Integer status, String remark) {
		SmtArticlesRelease articlesRelease = this.getById(id);
		if (Objects.nonNull(articlesRelease)) {
			SmtStaff smtStaff = smtStaffService.getSimpleSttaffByBadge(approveBadge);
			if(Objects.isNull(smtStaff)) {
				throw new SmartException("员工为空");
			}
			if (status.equals(ArticlesReleaseStatusEnum.DEPARTURE.getCode())
					|| status.equals(ArticlesReleaseStatusEnum.REFUSE.getCode())) {
				articlesRelease.setSecurityStaff(smtStaff.getName());
				articlesRelease.setDepartureTime(DateUtils.date());
				articlesRelease.setStatus(status);
				if (StringUtils.isNotEmpty(remark)) {
					articlesRelease.setRemark(remark);
				}
				return this.updateById(articlesRelease);
			} else {
				articlesRelease.setApprover(smtStaff.getName());
				articlesRelease.setApproveTime(DateUtils.date());
			}
			Integer approvalStatus = status - 1;
			String businessId = articlesRelease.getId().toString();
			ApproveList approveList = approveListService.getOne(Wrappers.<ApproveList>query().lambda()
					.eq(ApproveList::getBusinessId, id.toString())
					.eq(ApproveList::getApproveBadge, approveBadge).eq(ApproveList::getApproveState, ApproveListStateEnum.PENDING.getCode()));
			approveList.setApproveState(approvalStatus);
			if(StringUtils.isNotEmpty(remark)) {
				approveList.setRemark(remark);
			}
			approveList.setUpdateTime(LocalDateTime.now());
			approveListService.updateById(approveList);
			Integer result = approveListService.updateProcessStatus(businessId, articlesRelease.getBadge(), approvalStatus, approveList.getId());
			switch (ApprovalProcessResultEnum.getEnmu(result)) {
				case ALL_PASS:
					this.sendAppPush(articlesRelease.getBadge(), null, articlesRelease.getId(), SmsTemplateEnum.APP_RELEASE_10602.getCode());
					if (StringUtils.isNotEmpty(articlesRelease.getPhone())) {
						this.sendApprovePassMsg(articlesRelease.getId(), articlesRelease.getPhone());
					}
					articlesRelease.setStatus(ArticlesReleaseStatusEnum.APPROVED.getCode());
					break;
				case PART_PASS:
					List<ApproveList> pendingApprove = approveListService.getByStatus(ApproveListStateEnum.PENDING.getCode(), businessId, null);
					for (ApproveList list : pendingApprove) {
						SmtApprovalNode node = smtApprovalNodeService.getById(list.getNodeId());
						if (node.getIsAppPush().equals(OneOrZeroEnum.ONE.getCode())) {
							this.sendAppPush(list.getApproveBadge(), articlesRelease.getName(), articlesRelease.getId(), SmsTemplateEnum.APP_RELEASE_10604.getCode());
						}
						if (Objects.nonNull(node.getMsgTemplate())) {
							SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(list.getApproveBadge());
							if (Objects.nonNull(staff)) {
								this.sendApproveMsg(articlesRelease.getCarrier(), staff.getPhone(), node.getMsgTemplate());
							}
						}
					}
					articlesRelease.setStatus(ArticlesReleaseStatusEnum.PENDING_APPROVAL.getCode());
					break;
				case ALL_REFUSE:
					this.sendAppPush(articlesRelease.getBadge(), null, articlesRelease.getId(), SmsTemplateEnum.APP_RELEASE_10603.getCode());
					articlesRelease.setStatus(ArticlesReleaseStatusEnum.APPROVAL_FAILED.getCode());
					break;
				default:
					break;
			}
			this.updateById(articlesRelease);
		} else {
			throw new TCEException("获取物品放行失败");
		}
		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean livingStatusUpdate(Long id, String approveBadge, Integer status, String remark) {
		SmtArticlesRelease articlesRelease = this.getById(id);
		if (Objects.isNull(articlesRelease)) {
			throw new TCEException("获取物品放行失败");
		}
		SmtStaff smtStaff = smtStaffService.getSimpleSttaffByBadge(approveBadge);
		if(Objects.isNull(smtStaff)) {
			throw new SmartException("员工为空");
		}
		if (status.equals(ArticlesReleaseStatusEnum.DEPARTURE.getCode())
				|| status.equals(ArticlesReleaseStatusEnum.REFUSE.getCode())) {
			articlesRelease.setSecurityStaff(smtStaff.getName());
			articlesRelease.setDepartureTime(DateUtils.date());
			articlesRelease.setStatus(status);
			if (StringUtils.isNotEmpty(remark)) {
				articlesRelease.setRemark(remark);
			}
			return this.updateById(articlesRelease);
		} else {
			articlesRelease.setApprover(smtStaff.getName());
			articlesRelease.setApproveTime(DateUtils.date());
		}
		Integer approvalStatus = status - 1;
		String businessId = articlesRelease.getId().toString();
		ApproveList approveList = approveListService.getOne(Wrappers.<ApproveList>query().lambda()
				.eq(ApproveList::getBusinessId, id.toString())
				.eq(ApproveList::getApproveBadge, approveBadge)
				.eq(ApproveList::getApproveState, ApproveListStateEnum.PENDING.getCode())
				.orderByAsc(ApproveList::getSort), false);
		if (approveList == null) {
			return Boolean.TRUE;
		}
		approveList.setApproveState(approvalStatus);
		if(StringUtils.isNotEmpty(remark)) {
			approveList.setRemark(remark);
		}
		approveList.setUpdateTime(LocalDateTime.now());
		approveListService.updateById(approveList);
		Integer result = approveListService.updateProcessStatus(businessId, articlesRelease.getBadge(), approvalStatus, approveList.getId());
		switch (ApprovalProcessResultEnum.getEnmu(result)) {
			case ALL_PASS:
				//发起人：审批全部通过通知
				sendWechatMsg(SmsTemplateEnum.WECHAT_RELEASE_11503, articlesRelease, null, articlesRelease.getBadge(), null);
				articlesRelease.setStatus(ArticlesReleaseStatusEnum.APPROVED.getCode());
				break;
			case PART_PASS:
				List<ApproveList> pendingApprove = approveListService.getByStatus(ApproveListStateEnum.PENDING.getCode(), businessId, null);
				for (ApproveList list : pendingApprove) {
					SmtApprovalNode node = smtApprovalNodeService.getById(list.getNodeId());
					if (OneOrZeroEnum.ZERO.getCode().equals(node.getIsWeChatPush())) {
						continue;
					}
					//审批人：待审批通知
					if (ApprovalPersonRuleEnum.ROOMMATE.getCode().equals(node.getIsExistApprover())) {
						sendWechatMsg(SmsTemplateEnum.WECHAT_RELEASE_11501, articlesRelease, node, list.getApproveBadge(), null);
					} else {
						sendWechatMsg(SmsTemplateEnum.WECHAT_RELEASE_11502, articlesRelease, node, list.getApproveBadge(), null);
					}
				}
				articlesRelease.setStatus(ArticlesReleaseStatusEnum.PENDING_APPROVAL.getCode());
				break;
			case ALL_REFUSE:
			case PART_REFUSE:
				SmtApprovalNode approvalNode = smtApprovalNodeService.getById(approveList.getNodeId());
				//发起人：审批拒绝通知
				sendWechatMsg(SmsTemplateEnum.WECHAT_RELEASE_11504, articlesRelease, approvalNode, articlesRelease.getBadge(), remark);
				// 后续节点都关闭
				approveListService.batchUpdateStatus(businessId, null, ApproveListStateEnum.CLOSE.getCode());
				articlesRelease.setStatus(ArticlesReleaseStatusEnum.APPROVAL_FAILED.getCode());
				break;
			default:
				break;
		}
		this.updateById(articlesRelease);
		return Boolean.TRUE;
	}

	private void sendWechatMsg(SmsTemplateEnum templateEnum, SmtArticlesRelease articlesRelease, SmtApprovalNode approvalNode, String sendBadge, String remark) {
		log.info("物品放行微信推送开始：{},{},{}", templateEnum, sendBadge, articlesRelease.getId());
		SmtDormitory dormitory = smtDormitoryService.getById(articlesRelease.getDormitoryId());
		SmtDormitoryRoom room = smtDormitoryRoomService.getById(articlesRelease.getRoomId());
		SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(templateEnum.getCode());
		String head = "物品放行申请" + DateUtils.convert("yyyyMMdd", articlesRelease.getCreateTime());
		String msg = "";
		switch (templateEnum) {
			case WECHAT_RELEASE_11501:
			case WECHAT_RELEASE_11502:
			case WECHAT_RELEASE_11503:
				msg = template.getTempContent().replace("{申请人房间号}", dormitory.getDormitoryName()+room.getRoomName())
						.replace("{申请人}", articlesRelease.getName())
						.replace("{单子标题}", head);
				break;
			case WECHAT_RELEASE_11504:
				msg = template.getTempContent().replace("{申请人房间号}", dormitory.getDormitoryName()+room.getRoomName())
						.replace("{申请人}", articlesRelease.getName())
						.replace("{拒绝原因描述}", remark == null ? "" : remark)
						.replace("{宿舍审批}", approvalNode == null ? "" : approvalNode.getName())
						.replace("{单子标题}", head);
				break;
			default:
				log.info("微信推送模板不存在");
				return;
		}
		// sort: 1-室友，2-宿管，4-申请人
		String backUrl = pushUrl.replace("{id}", articlesRelease.getId().toString());
		switch (templateEnum) {
			case WECHAT_RELEASE_11501:
				backUrl = backUrl.replace("{sort}", "1");
				break;
			case WECHAT_RELEASE_11502:
				backUrl = backUrl.replace("{sort}", "2");
				break;
			case WECHAT_RELEASE_11503:
			case WECHAT_RELEASE_11504:
				backUrl = backUrl.replace("{sort}", "4");
				break;
			default:
				log.info("微信推送模板不存在");
				return;
		}
		try {
            log.info("微信推送参数：{}---{}----{}",sendBadge, backUrl, msg);
			WeChatMsgUtil.sendMsg(sendBadge, msg, null, backUrl);
		} catch (Exception e) {
			log.error("物品放行{}消息推送失败:{}",sendBadge, e.getMessage());
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean securityUpdateForGuard(String guardBadge, List<Integer> guardParkIds, GuardReleaseConfirmReqDTO reqDTO) {
		if (StrUtil.isBlank(guardBadge) || reqDTO == null || reqDTO.getId() == null) {
			throw new AccessDeniedException("未授权保安不能确认物品放行");
		}
		SmtArticlesRelease articlesRelease = this.getById(reqDTO.getId());
		assertGuardParkAccess(guardParkIds, articlesRelease);
		if (!ArticlesReleaseStatusEnum.APPROVED.getCode().equals(articlesRelease.getStatus())
				|| (!ArticlesReleaseStatusEnum.DEPARTURE.getCode().equals(reqDTO.getStatus())
				&& !ArticlesReleaseStatusEnum.REFUSE.getCode().equals(reqDTO.getStatus()))) {
			throw new AccessDeniedException("物品放行状态不允许保安确认");
		}
		Integer parkId = articlesRelease.getParkId();
		SmtArticlesRelease updateRelease = new SmtArticlesRelease();
		updateRelease.setId(articlesRelease.getId());
		if (StringUtils.isNotEmpty(reqDTO.getGuardOneImg())) {
			updateRelease.setOneImg(smtImageService.saveImage(parkId, reqDTO.getGuardOneImg(), SmtImageEnum.ARTICLES_RELEASE.getCode()));
		}
		if (StringUtils.isNotEmpty(reqDTO.getGuardTwoImg())) {
			updateRelease.setTwoImg(smtImageService.saveImage(parkId, reqDTO.getGuardTwoImg(), SmtImageEnum.ARTICLES_RELEASE.getCode()));
		}
		if (StringUtils.isNotEmpty(reqDTO.getGuardThreeImg())) {
			updateRelease.setThreeImg(smtImageService.saveImage(parkId, reqDTO.getGuardThreeImg(), SmtImageEnum.ARTICLES_RELEASE.getCode()));
		}
		updateRelease.setSecurityStaff(guardBadge);
		updateRelease.setDepartureTime(DateUtils.date());
		updateRelease.setStatus(reqDTO.getStatus());
		updateRelease.setRemark(reqDTO.getRemark());
		boolean updated = this.update(updateRelease, Wrappers.<SmtArticlesRelease>lambdaUpdate()
				.eq(SmtArticlesRelease::getId, reqDTO.getId())
				.eq(SmtArticlesRelease::getStatus, ArticlesReleaseStatusEnum.APPROVED.getCode()));
		if (!updated) {
			throw new AccessDeniedException("物品放行状态已变化，请刷新后重试");
		}
		return Boolean.TRUE;
//		if (update && ArticlesReleaseTypeEnum.OFFICE_ZONE.getCode().equals(articlesRelease.getArticlesType())) {
//			// 办公区物品放行，保安放行时，需同步更新OA状态
//			SendSecurityApprovalReqDTO approvalReqDTO = new SendSecurityApprovalReqDTO();
//			approvalReqDTO.setRemark(articlesRelease.getProcessId());
//			approvalReqDTO.setUserid(articlesRelease.getBadge());
//			approvalReqDTO.setRemark(reqDTO.getRemark());
//			Result<Boolean> oaResult = remoteOaWorkFlowService.sendSecurityApproval(approvalReqDTO);
//			if (!oaResult.isSuccess() || !oaResult.getData()) {
//				throw new SmartException("调用OA更新放行时间失败");
//			}
//			update = oaResult.getData();
//		}
//		return update;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean securityBackConfirmForGuard(String guardBadge, List<Integer> guardParkIds, Long releaseId) {
		SmtArticlesRelease articlesRelease = this.getById(releaseId);
		assertGuardParkAccess(guardParkIds, articlesRelease);
		if (StrUtil.isBlank(guardBadge)
				|| !ArticlesReleaseStatusEnum.DEPARTURE.getCode().equals(articlesRelease.getStatus())
				|| !BackFactoryEnum.YES.getCode().equals(articlesRelease.getIsBack())
				|| articlesRelease.getBackTime() != null) {
			throw new AccessDeniedException("物品放行状态不允许确认返厂");
		}
		Date backDate = new Date();
		SmtArticlesRelease updateRelease = new SmtArticlesRelease();
		updateRelease.setId(articlesRelease.getId());
		updateRelease.setBackTime(backDate);
		updateRelease.setGuardBadge(guardBadge);
		boolean update = this.update(updateRelease, Wrappers.<SmtArticlesRelease>lambdaUpdate()
				.eq(SmtArticlesRelease::getId, releaseId)
				.eq(SmtArticlesRelease::getStatus, ArticlesReleaseStatusEnum.DEPARTURE.getCode())
				.eq(SmtArticlesRelease::getIsBack, BackFactoryEnum.YES.getCode())
				.isNull(SmtArticlesRelease::getBackTime));
		if (!update) {
			throw new AccessDeniedException("物品放行状态已变化，请刷新后重试");
		}
		if (update) {
			String backDateStr = DateUtil.formatDate(backDate);
			String backTimeStr = DateUtil.format(backDate, "HH:mm");
			SendWriteBackReturnTimeReqDTO backReturnTimeReqDTO = new SendWriteBackReturnTimeReqDTO();
			backReturnTimeReqDTO.setRequestid(articlesRelease.getProcessId());
			backReturnTimeReqDTO.setFcrq(backDateStr);
			backReturnTimeReqDTO.setFcsj(backTimeStr);
			log.info("返厂确认OA提交数据：{}", backReturnTimeReqDTO);
			Result<Boolean> oaResult = remoteOaWorkFlowService.sendWriteBackReturnTime(backReturnTimeReqDTO);
			if (oaResult == null || !oaResult.isSuccess() || !oaResult.getData()) {
				throw new SmartException("设置OA返厂时间失败");
			}
			update = oaResult.getData();
			log.info("调用OA设置返厂时间结果：{}", update);
			if (update) {
				releasePersonService.update(Wrappers.<SmtArticlesReleasePerson>lambdaUpdate()
						.set(SmtArticlesReleasePerson::getFcrq, backDateStr)
						.set(SmtArticlesReleasePerson::getFcsj, backTimeStr)
						.set(SmtArticlesReleasePerson::getUpdateTime, LocalDateTime.now())
						.eq(SmtArticlesReleasePerson::getReleaseId, releaseId));
				releaseThingService.update(Wrappers.<SmtArticlesReleaseThing>lambdaUpdate()
						.set(SmtArticlesReleaseThing::getWpfcrq, backDateStr)
						.set(SmtArticlesReleaseThing::getWpfcsj, backTimeStr)
						.set(SmtArticlesReleaseThing::getUpdateTime, LocalDateTime.now())
						.eq(SmtArticlesReleaseThing::getReleaseId, releaseId));
			}
		}
		return update;
	}

	/** 保安确认必须位于认证账号持有的数据园区内，缺失园区范围时拒绝。 */
	private void assertGuardParkAccess(List<Integer> guardParkIds, SmtArticlesRelease release) {
		if (CollUtil.isEmpty(guardParkIds) || release.getParkId() == null || !guardParkIds.contains(release.getParkId())) {
			throw new AccessDeniedException("无权确认该园区物品放行");
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveArticlesRelease(AddArticlesReleaseReqDTO reqDTO) {
		SmtArticlesRelease articlesRelease = BeanUtils.transform(SmtArticlesRelease.class, reqDTO);
		if (Objects.nonNull(reqDTO.getDormitoryId())) {
			SmtDormitoryStaff dormitoryStaff = smtDormitoryStaffService.getDormitoryStaff(reqDTO.getDormitoryId(), reqDTO.getFloorId(), reqDTO.getRoomId(), reqDTO.getBedId());
			if (Objects.isNull(dormitoryStaff)) {
				throw new TCEException("该床位未安排住宿");
			}
		}

		/*
		 * bug【6902】供应商物品放行审批成功后---》申请人没有收到对应的通知短信
		 * start by sfj
		 */
		// 如果没有上传手机号，则通过员工号查询出手机号
		if (StrUtil.isBlank(reqDTO.getPhone())) {
			SmtStaff staffTemp = smtStaffService.getSimpleSttaffByBadge(reqDTO.getBadge());
			if(Objects.isNull(staffTemp)) {
				throw new SmartException("员工为空");
			}
			articlesRelease.setPhone(staffTemp.getPhone());
			reqDTO.setPhone(staffTemp.getPhone());
		}
		//先保存图片
		if (StringUtils.isNotEmpty(reqDTO.getOneImg())) {
			articlesRelease.setOneImg(smtImageService.saveImage(reqDTO.getParkId(), reqDTO.getOneImg(), SmtImageEnum.ARTICLES_RELEASE.getCode()));
		}
		if (StringUtils.isNotEmpty(reqDTO.getTwoImg())) {
			articlesRelease.setTwoImg(smtImageService.saveImage(reqDTO.getParkId(), reqDTO.getTwoImg(), SmtImageEnum.ARTICLES_RELEASE.getCode()));
		}
		if (StringUtils.isNotEmpty(reqDTO.getThreeImg())) {
			articlesRelease.setThreeImg(smtImageService.saveImage(reqDTO.getParkId(), reqDTO.getThreeImg(), SmtImageEnum.ARTICLES_RELEASE.getCode()));
		}
		articlesRelease.setPlannedDepartureTime(DateUtils.parse(reqDTO.getPlannedDepartureTime()));
		articlesRelease.setCreateTime(LocalDateTime.now());
		if (StringUtils.isEmpty(articlesRelease.getCarrier())) {
			articlesRelease.setCarrier(articlesRelease.getName());
		}
		String applyName;
		if (ArticlesReleaseTypeEnum.DORMITORY.getCode().equals(articlesRelease.getArticlesType())) {
			if (Objects.isNull(reqDTO.getDormitoryId())) {
				applyName = articlesRelease.getCarrier();
				SmtStaff vo = smtStaffService.getByPhoneAndName(reqDTO.getPhone(), reqDTO.getCarrier());
				if (Objects.isNull(vo)) {
					throw new TCEException("申请人不存在");
				}
				articlesRelease.setBadge(vo.getBadge());
			} else {
				SmtDormitoryStaff dormitoryStaff = smtDormitoryStaffService.getDormitoryStaff(reqDTO.getDormitoryId(), reqDTO.getFloorId(), reqDTO.getRoomId(), reqDTO.getBedId());
				applyName = dormitoryStaff.getStaffName();
				articlesRelease.setBadge(dormitoryStaff.getStaffBadge());
				articlesRelease.setCarrier(applyName);
			}
		} else {
			applyName = reqDTO.getName();
		}
		if (this.save(articlesRelease)) {

			Integer sort = OneOrZeroEnum.ONE.getCode();
			//获得审批人列表
			List<ApproveProcessListReqDTO> approveProcessList = this.getApprovePersonList(articlesRelease, sort);
			//判断是否存在待审批记录，若不存在则所有审批通过
			Boolean flag = Boolean.TRUE;

			for (ApproveProcessListReqDTO approve : approveProcessList) {
				ApproveList approveList = BeanUtils.transform(ApproveList.class, approve);
				approveList.setApproveName(applyName + "提交的物品放行申请");
				if (approveList.getApproveState().equals(ApproveListStateEnum.PENDING.getCode())) {
					//为审批人发送app push消息
					if (approve.getIsAppPush().equals(OneOrZeroEnum.ONE.getCode())) {
						this.sendAppPush(approveList.getApproveBadge(), applyName, articlesRelease.getId(), SmsTemplateEnum.APP_RELEASE_10604.getCode());
					}
					//为审批人发送短信消息
					if (Objects.nonNull(approve.getMsgTemplate())) {
						SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(approve.getApproveBadge());
						if (Objects.nonNull(staff)) {
							this.sendApproveMsg(articlesRelease.getCarrier(), staff.getPhone(), approve.getMsgTemplate());
						}
					}
					flag = Boolean.FALSE;
				}
				//保存审批记录
				approveListService.saveApproveList(approveList);
			}
			if (flag) {
				//所有人审批通过，修改物品放行审批状态
				articlesRelease.setStatus(ArticlesReleaseStatusEnum.APPROVED.getCode());
				this.updateById(articlesRelease);
				//为发起人发送短信
				if (StringUtils.isNotBlank(reqDTO.getPhone())) {
					this.sendApprovePassMsg(articlesRelease.getId(), reqDTO.getPhone());
				}
			}
		}
		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveLivingArticlesRelease(AddArticlesReleaseReqDTO reqDTO) {
		SmtArticlesRelease articlesRelease = BeanUtils.transform(SmtArticlesRelease.class, reqDTO);
		if (Objects.nonNull(reqDTO.getDormitoryId())) {
			SmtDormitoryStaff dormitoryStaff = smtDormitoryStaffService.getDormitoryStaff(reqDTO.getDormitoryId(), reqDTO.getFloorId(), reqDTO.getRoomId(), reqDTO.getBedId());
			if (Objects.isNull(dormitoryStaff)) {
				throw new TCEException("该床位未安排住宿");
			}
		}

		/*
		 * bug【6902】供应商物品放行审批成功后---》申请人没有收到对应的通知短信
		 * start by sfj
		 */
		// 如果没有上传手机号，则通过员工号查询出手机号
		if (StrUtil.isBlank(reqDTO.getPhone())) {
			SmtStaff staffTemp = smtStaffService.getSimpleSttaffByBadge(reqDTO.getBadge());
			if(Objects.isNull(staffTemp)) {
				throw new SmartException("员工为空");
			}
			articlesRelease.setPhone(staffTemp.getPhone());
			reqDTO.setPhone(staffTemp.getPhone());
		}
		//先保存图片
		if (StringUtils.isNotEmpty(reqDTO.getOneImg())) {
			articlesRelease.setOneImg(smtImageService.saveImage(reqDTO.getParkId(), reqDTO.getOneImg(), SmtImageEnum.ARTICLES_RELEASE.getCode()));
		}
		if (StringUtils.isNotEmpty(reqDTO.getTwoImg())) {
			articlesRelease.setTwoImg(smtImageService.saveImage(reqDTO.getParkId(), reqDTO.getTwoImg(), SmtImageEnum.ARTICLES_RELEASE.getCode()));
		}
		if (StringUtils.isNotEmpty(reqDTO.getThreeImg())) {
			articlesRelease.setThreeImg(smtImageService.saveImage(reqDTO.getParkId(), reqDTO.getThreeImg(), SmtImageEnum.ARTICLES_RELEASE.getCode()));
		}
		articlesRelease.setPlannedDepartureTime(DateUtils.parse(reqDTO.getPlannedDepartureTime()));
		articlesRelease.setCreateTime(LocalDateTime.now());
		if (StringUtils.isEmpty(articlesRelease.getCarrier())) {
			articlesRelease.setCarrier(articlesRelease.getName());
		}
		String applyName;
		if (ArticlesReleaseTypeEnum.DORMITORY.getCode().equals(articlesRelease.getArticlesType())) {
			if (Objects.isNull(reqDTO.getDormitoryId())) {
				applyName = articlesRelease.getCarrier();
				SmtStaff vo = smtStaffService.getByPhoneAndName(reqDTO.getPhone(), reqDTO.getCarrier());
				if (Objects.isNull(vo)) {
					throw new TCEException("申请人不存在");
				}
				articlesRelease.setBadge(vo.getBadge());
			} else {
				SmtDormitoryStaff dormitoryStaff = smtDormitoryStaffService.getDormitoryStaff(reqDTO.getDormitoryId(), reqDTO.getFloorId(), reqDTO.getRoomId(), reqDTO.getBedId());
				applyName = dormitoryStaff.getStaffName();
				articlesRelease.setBadge(dormitoryStaff.getStaffBadge());
				articlesRelease.setCarrier(applyName);
			}
		} else {
			applyName = reqDTO.getName();
		}
		if (this.save(articlesRelease)) {
			//判断是否存在待审批记录，若不存在则所有审批通过
			Boolean flag = Boolean.TRUE;
			Integer sort = OneOrZeroEnum.ONE.getCode();
			//获得审批人列表
			List<ApproveProcessListReqDTO> approveProcessList = this.getApprovePersonList(articlesRelease, sort);
			for (ApproveProcessListReqDTO approve : approveProcessList) {
				ApproveList approveList = BeanUtils.transform(ApproveList.class, approve);
				approveList.setApproveName(applyName + "提交的物品放行申请");
				if (ApproveListStateEnum.PENDING.getCode().equals(approveList.getApproveState())) {
					if (OneOrZeroEnum.ONE.getCode().equals(approve.getIsWeChatPush())) {
						//审批人微信审批消息推送
						if (ApprovalPersonRuleEnum.ROOMMATE.getCode().equals(approve.getIsExistApprover())) {
							sendWechatMsg(SmsTemplateEnum.WECHAT_RELEASE_11501, articlesRelease, null, approveList.getApproveBadge(), null);
						} else {
							sendWechatMsg(SmsTemplateEnum.WECHAT_RELEASE_11502, articlesRelease, null, approveList.getApproveBadge(), null);
						}
					}
					flag = Boolean.FALSE;
				}
				//保存审批记录
				approveListService.saveApproveList(approveList);
			}
			if (flag) {
				//所有人审批通过，修改物品放行审批状态
				articlesRelease.setStatus(ArticlesReleaseStatusEnum.APPROVED.getCode());
				this.updateById(articlesRelease);
				//发起人审批通过消息推送
				sendWechatMsg(SmsTemplateEnum.WECHAT_RELEASE_11503, articlesRelease, null, articlesRelease.getBadge(), null);
			}
		}
		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveOfficeArticlesRelease(String ownerBadge, List<Integer> ownerParkIds, OfficeZoneReleaseReqDTO reqDTO) {
		if (reqDTO == null || reqDTO.getReleaseId() == null) {
			throw new AccessDeniedException("必须先创建物品放行草稿");
		}
		SmtArticlesRelease articlesRelease = getOfficeDraftForOwner(ownerBadge, ownerParkIds, reqDTO.getReleaseId());
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(ownerBadge);
		if (Objects.isNull(staff)) {
			throw new SmartException("员工信息不存在");
		}
		Integer parkId = articlesRelease.getParkId();
		articlesRelease.setCarrier(staff.getName());
		articlesRelease.setStatus(ArticlesReleaseStatusEnum.PENDING_APPROVAL.getCode());
		articlesRelease.setName(staff.getName());
		// 放行事项
		articlesRelease.setReleaseItem(reqDTO.getApplyMain().getFxsx());
		// 是否返厂
		articlesRelease.setIsBack(reqDTO.getApplyMain().getSffc());
		// 先保存图片
		if (StringUtils.isNotEmpty(reqDTO.getOneImg())) {
			articlesRelease.setOneImg(smtImageService.saveImage(parkId, reqDTO.getOneImg(), SmtImageEnum.ARTICLES_RELEASE.getCode()));
		}
		if (StringUtils.isNotEmpty(reqDTO.getTwoImg())) {
			articlesRelease.setTwoImg(smtImageService.saveImage(parkId, reqDTO.getTwoImg(), SmtImageEnum.ARTICLES_RELEASE.getCode()));
		}
		if (StringUtils.isNotEmpty(reqDTO.getThreeImg())) {
			articlesRelease.setThreeImg(smtImageService.saveImage(parkId, reqDTO.getThreeImg(), SmtImageEnum.ARTICLES_RELEASE.getCode()));
		}
		boolean save = updateById(articlesRelease);
		if (save) {
			SendReleaseApplyReqDTO applyReqDTO = new SendReleaseApplyReqDTO();
			SmtArticlesReleaseMain releaseMain = new SmtArticlesReleaseMain();
			BeanUtil.copyProperties(reqDTO.getApplyMain(), releaseMain);
			releaseMain.setReleaseId(articlesRelease.getId());
			releaseMain.setSqr(staff.getBadge());
			String imageUrl = null;
			if (StrUtil.isNotBlank(reqDTO.getApplyMain().getFjsc())) {
				String imageId = smtImageService.saveImage(parkId, reqDTO.getApplyMain().getFjsc(), SmtImageEnum.ARTICLES_RELEASE.getCode());
				releaseMain.setFjsc(imageId);
				imageUrl = smtImageService.buildImageUrl(baseImageUrl, imageId);
			}
			save = releaseMainService.save(releaseMain);
			applyReqDTO.setReleaseApplyMainReqDTO(BeanUtil.toBean(reqDTO.getApplyMain(), ReleaseApplyMainReqDTO.class));
			applyReqDTO.getReleaseApplyMainReqDTO().setBadge(ownerBadge);
			applyReqDTO.getReleaseApplyMainReqDTO().setName(staff.getName());
			applyReqDTO.getReleaseApplyMainReqDTO().setLcbh("29061");
			applyReqDTO.getReleaseApplyMainReqDTO().setFjsc(imageUrl);
			if (CollUtil.isNotEmpty(reqDTO.getPersonList())) {
				List<SmtArticlesReleasePerson> personList = new ArrayList<>();
				for (ReleaseApplyPersonDetail personDetail : reqDTO.getPersonList()) {
					SmtArticlesReleasePerson person = BeanUtil.toBean(personDetail, SmtArticlesReleasePerson.class);
					person.setReleaseId(articlesRelease.getId());
					person.setMainId(releaseMain.getId());
					person.setXm(personDetail.getName());
					personList.add(person);
				}
				if (CollUtil.isNotEmpty(personList)) {
					save = releasePersonService.saveBatch(personList);
				}
				applyReqDTO.setReleaseApplyPersonDetailReqDTOs(BeanUtils.batchTransform(ReleaseApplyPersonDetailReqDTO.class, reqDTO.getPersonList()));
			} else {
				applyReqDTO.setReleaseApplyPersonDetailReqDTOs(new ArrayList<>(0));
			}
			if (CollUtil.isNotEmpty(reqDTO.getThingList())) {
				List<SmtArticlesReleaseThing> thingList = new ArrayList<>();
				for (ReleaseApplyThingDetail thingDetail : reqDTO.getThingList()) {
					SmtArticlesReleaseThing thing = BeanUtil.toBean(thingDetail, SmtArticlesReleaseThing.class);
					thing.setReleaseId(articlesRelease.getId());
					thing.setMainId(releaseMain.getId());
					thing.setXm(thingDetail.getName());
					thingList.add(thing);
				}
				if (CollUtil.isNotEmpty(thingList)) {
					save = releaseThingService.saveBatch(thingList);
				}
				applyReqDTO.setReleaseApplyThingDetailReqDTOs(BeanUtils.batchTransform(ReleaseApplyThingDetailReqDTO.class, reqDTO.getThingList()));
			} else {
				applyReqDTO.setReleaseApplyThingDetailReqDTOs(new ArrayList<>(0));
			}
			Assert.isTrue(save, "保存物品放行信息失败");
			log.info("物品放行申请条OA数据：{}", JSONUtil.toJsonStr(applyReqDTO));
			// 调用远程OA系统创建放行条申请单
			Result<String> oaResult = remoteOaWorkFlowService.sendReleaseApply(applyReqDTO);
			if (!oaResult.isSuccess() || StrUtil.isBlank(oaResult.getData())) {
				throw new SmartException("OA流程提交异常");
			}
			String processId = oaResult.getData();
			log.info("创建OA放行申请单结果：{}", processId);
			if (StrUtil.isBlank(processId) || Long.parseLong(processId) < 0) {
				throw new SmartException("创建OA放行申请条失败");
			}
			// 获取流程审批节点信息
			getOAProcess(processId);
			// 补全流程编号
			articlesRelease.setProcessId(processId);
			save = updateById(articlesRelease);
		}
		return save;
	}

	private void getOAProcess(String processId) {
		WorkFlowLogDTO workFlowLogDTO = oaWorkflowService.query(processId);
		log.info("OA创建物品放行条流程查询结果:({})", JSONUtil.toJsonStr(workFlowLogDTO));
		if(ObjectUtil.isNotNull(workFlowLogDTO) && workFlowLogDTO.success()) {
			List<WorkFlowLogDataDTO> flowRecords = workFlowLogDTO.getResultdata();
			if(CollUtil.isNotEmpty(flowRecords)){
				flowRecords.forEach(flowRecord -> saveProcessRecord(processId, flowRecord));
			}
		}
	}

	private void saveProcessRecord(String processId,WorkFlowLogDataDTO process) {
		log.info("process.logType:{}", process.getLOGTYPE());
		SmtProcessRecord smtProcessRecord = processRecordService.getOne(Wrappers.<SmtProcessRecord>query().lambda()
				.eq(SmtProcessRecord::getProcessId, processId)
				.eq(SmtProcessRecord::getStaffBadge, process.getWORKCODE())
				.ne(SmtProcessRecord::getStatus, NodeStatusEnum.FINISHED.getCode())
				.ne(SmtProcessRecord::getStatus, NodeStatusEnum.NOT_FINISHED.getCode()));
		if(ObjectUtil.isNull(smtProcessRecord)) {
			SmtProcessRecord processRecord = new SmtProcessRecord();
			processRecord.setCreatTime(DateUtil.date());
			processRecord.setNodeName(process.getNODENAME());
			processRecord.setProcessId(processId);
			if(StrUtil.isNotBlank(process.getOPERATEDATE()) && StrUtil.isNotBlank(process.getOPERATETIME())) {
				String dateTime = process.getOPERATEDATE() + " " + process.getOPERATETIME();
				processRecord.setRecordDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
			}
			processRecord.setRemark(htmlHandle(process.getREMARK()));
			processRecord.setStaffBadge(process.getWORKCODE());
			processRecord.setStaffName(process.getLASTNAME());
			processRecord.setStatus(process.getLOGTYPE());
			processRecordService.save(processRecord);
		}
	}

	private String htmlHandle(String html) {
		if(StrUtil.isBlank(html)) {
			return "";
		}
		String txtcontent = html.replaceAll("</?[^>]+>", "");
		txtcontent = txtcontent.replaceAll("<a>\\s*|\t|\r|\n</a>", "");
		return StringEscapeUtils.unescapeHtml(txtcontent).trim();
	}

	/**
	 * 请求审批人列表
	 *
	 * @param articlesRelease
	 * @return
	 */
	private List<ApproveProcessListReqDTO> getApprovePersonList(SmtArticlesRelease articlesRelease, Integer sort) {
		ApprovalProcessReqDTO approvalProcess = new ApprovalProcessReqDTO();
		approvalProcess.setArticlesType(articlesRelease.getArticlesType());
		approvalProcess.setBusinessId(articlesRelease.getId().toString());
		approvalProcess.setDormitoryId(articlesRelease.getDormitoryId());
		approvalProcess.setEventId(ApproveListTypeConstants.ARTICLE);
		approvalProcess.setParkId(articlesRelease.getParkId());
		approvalProcess.setApplyBadge(articlesRelease.getBadge());
		approvalProcess.setSort(sort);
		approvalProcess.setRoomId(Collections.singletonList(articlesRelease.getRoomId()));
		List<ApproveProcessListReqDTO> approveProcessList = approvalService.approvalProcess(approvalProcess);
		if (CollUtil.isEmpty(approveProcessList)) {
			throw new TCEException("未添加节点审批人，请联系管理员处理");
		}
		return approveProcessList;
	}


	/**
	 * 审批通过后发送二维码生成短信
	 *
	 * @param id    物品放行主键
	 * @param phone 收信人号码
	 * @return
	 */
	private void sendApprovePassMsg(Long id, String phone) {
		ArticlesReleaseMsgReqDTO articlesReleaseMsgReqDTO = new ArticlesReleaseMsgReqDTO();
		String url = smsUrl.replace("{id}", id.toString());
		if (StringUtils.isNotEmpty(url)) {
			articlesReleaseMsgReqDTO.setUrl(url);
			articlesReleaseMsgReqDTO.setTempCode(SmsTemplateEnum.SMS_RELEASE_10601.getCode());
			articlesReleaseMsgReqDTO.setPhone(phone);
			remoteSmsManageService.sendArticlesRelease(articlesReleaseMsgReqDTO);
		}
	}

	/**
	 * 发送待审批短信
	 *
	 * @param name  申请人
	 * @param phone 收信人号码
	 * @return
	 */
	private void sendApproveMsg(String name, String phone, Integer tempId) {
		SendMsgReqDTO reqDTO = new SendMsgReqDTO();
		reqDTO.setNumber(phone);
		SmtMsgTemplate smtMsgTemplate = smtMsgTemplateService.getById(tempId);
		reqDTO.setTempCode(smtMsgTemplate.getTempCode());
		String smsContent = smtMsgTemplate.getTempContent();
		if (StringUtils.isNotEmpty(name)) {
			smsContent = smsContent.replace("{applyName}", name);
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
	private void sendAppPush(String badge, String name, Long id, String tempCode) {
		AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
		appMsgPushDTO.setBadge(badge);
		appMsgPushDTO.setApplicant(name);
		appMsgPushDTO.setBussiessId(id.toString());
		appMsgPushDTO.setTemplateCode(tempCode);
		appMsgPushService.pushAppMsg(appMsgPushDTO);
	}

	@Override
	public List<SmtArticlesReleasePerson> queryPerson(String badge) {
		return releasePersonService.list(new LambdaQueryWrapper<SmtArticlesReleasePerson>().eq(SmtArticlesReleasePerson::getBadge, badge));
	}

	@Override
	public Boolean savePerson(ReleaseApplyPersonDetail personDetail) {
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(personDetail.getBadge());
		if (Objects.isNull(staff)) {
			throw new SmartException("申请人信息不存在");
		}
		SmtArticlesReleasePerson releasePerson = new SmtArticlesReleasePerson();
		BeanUtil.copyProperties(personDetail, releasePerson);
		return releasePersonService.saveOrUpdate(releasePerson);
	}

	@Override
	public Boolean deletePerson(Long id) {
		return releasePersonService.removeById(id);
	}

	@Override
	public List<SmtArticlesReleaseThing> queryThing(String badge) {
		return releaseThingService.list(new LambdaQueryWrapper<SmtArticlesReleaseThing>().eq(SmtArticlesReleaseThing::getBadge, badge));
	}

	@Override
	public Boolean saveThing(ReleaseApplyThingDetail thingDetail) {
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(thingDetail.getBadge());
		if (Objects.isNull(staff)) {
			throw new SmartException("申请人信息不存在");
		}
		SmtArticlesReleaseThing releaseThing = new SmtArticlesReleaseThing();
		BeanUtil.copyProperties(thingDetail, releaseThing);
		return releaseThingService.saveOrUpdate(releaseThing);
	}

	@Override
	public Boolean deleteThing(Long id) {
		return releaseThingService.removeById(id);
	}

	@Override
	public ResponseEntity<byte[]> exportOfficeReleaseOARecord() {
		long current = 0;
		long size = 100;
		Page page = new Page(current, size);
		OfficeZoneApproveQueryDTO reqDTO = new OfficeZoneApproveQueryDTO();
		List<ArticlesReleaseListRespDTO> releaseList = new ArrayList<>();
		do {
			current++;
			page.setCurrent(current);
			IPage<SmtArticlesRelease> officeReleasePage = getOfficeReleasePage(page, reqDTO);
			if (CollUtil.isEmpty(officeReleasePage.getRecords())) {
				break;
			}
			officeReleasePage.getRecords().forEach(model -> {
				ArticlesReleaseListRespDTO respDTO = new ArticlesReleaseListRespDTO();
				respDTO.setBadge(model.getBadge());
				respDTO.setName(model.getName());
				if(StrUtil.isEmpty(model.getPhone())){
					StaffInfoVO vo = smtStaffService.getBaseinfoById(model.getBadge());
					respDTO.setCompName(vo.getSmtStaff().getCompName());
					respDTO.setDeptName(vo.getSmtStaff().getDepName());
				}else{
					StaffInfoVO vo = smtStaffService.getSmtStaffInfoByPhone(model.getPhone(),model.getCarrier());
					if(Objects.nonNull(vo.getSmtStaff())){
						respDTO.setCompName(vo.getSmtStaff().getCompName());
						respDTO.setDeptName(vo.getSmtStaff().getDepName());
					} else {
						respDTO.setCompName("-");
						respDTO.setDeptName("-");
					}
				}
				respDTO.setReleaseItemDesc(ReleaseItemEnum.getByCode(model.getReleaseItem()));
				respDTO.setProcessId(model.getProcessId());
				respDTO.setCreateTime(model.getCreateTime());
				releaseList.add(respDTO);
			});
		} while (page.hasNext());

		if (CollUtil.isEmpty(releaseList)) {
			throw new TCEException(CommonConstants.SUCCESS, "查询无数据");
		}

		ResponseEntity<byte[]> responseEntity;
		try (Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), ArticlesReleaseListRespDTO.class, releaseList)) {
			String fileName = "许昌裕同放行条记录导出";
			responseEntity = IOUtils.getExcelResponse(fileName, workbook);
		} catch (IOException e) {
			log.error("excel导出异常", e);
			throw new TCEException(ExceptionEnum.UNKNOWN.getCode(), "excel导出异常");
		}
		return responseEntity;
	}
}
