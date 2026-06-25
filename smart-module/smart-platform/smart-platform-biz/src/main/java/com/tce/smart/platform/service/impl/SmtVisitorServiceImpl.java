package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.algorithm.api.dto.resp.FaceFeaturesDTO;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.SnapVehicleConstants;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.WebUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsBlackRespDTO;
import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwEmphrYsService;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.data.api.vo.msg.SendSmsVo;
import com.tce.smart.platform.api.dto.CarCardDTO;
import com.tce.smart.platform.api.dto.CardDTO;
import com.tce.smart.platform.api.dto.req.VisitorAgainReqDTO;
import com.tce.smart.platform.api.dto.resp.GetSmtFellowVisitorRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchVisitorDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.VisitorListRespDTO;
import com.tce.smart.platform.api.dto.resp.commonconfig.ConfigVisitorApprovalDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtSnapPersonMapper;
import com.tce.smart.platform.core.mapper.SmtSnapVehicleMapper;
import com.tce.smart.platform.core.mapper.SmtVisitorMapper;
import com.tce.smart.platform.core.service.*;
import com.tce.smart.platform.core.vo.*;
import com.tce.smart.platform.emun.ParkTypeEnum;
import com.tce.smart.platform.service.SmtVisitorService;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.RegexUtils;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 访客表
 *
 * @author liangyuan
 */

@Slf4j
@Service
public class SmtVisitorServiceImpl extends ServiceImpl<SmtVisitorMapper, SmtVisitor> implements SmtVisitorService {
	private static final long OA_STATUS_SYNC_PAGE_SIZE = 50L;
	private static final int OA_STATUS_RECHECK_BATCH_SIZE = 200;
	private static final int OA_STATUS_RECHECK_ID_LIMIT = 10000;
	private static final long SHARED_SYNC_STATE_KEEP_HOURS = 24L;
	private static final String DEVICE_TASK_EXISTS_MESSAGE = "任务已存在";
	private static final String ISC_VEHICLE_AUTH_UNSUPPORTED_MESSAGE = "ISC车辆权限不支持下发";
	private static final String OA_STATUS_CURSOR_KEY = "smart:visitor:oa-status:cursor";
	private static final String OA_STATUS_RECHECK_KEY = "smart:visitor:oa-status:recheck";
	private final AtomicLong oaStatusCursor = new AtomicLong();
	private final Set<Long> oaStatusRecheckIds = Collections.synchronizedSet(new LinkedHashSet<>());

	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private IOAWorkflowService oaWorkflowService;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private SmtFellowVisitorService smtFellowVisitorService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtOutDormitoryStaffService smtOutDormitoryStaffService;
	@Autowired
	private SmtSnapPersonMapper smtSnapPersonMapper;
	@Autowired
	private SmtSnapVehicleMapper smtSnapVehicleMapper;
	@Autowired
	private RemoteAlgorithmService remoteAlgorithmService;
	@Autowired
	private ApproveListService approveListService;
	@Autowired
	private RemoteOaWorkFlowService remoteOaWorkFlowService;
	@Autowired
	private SmtVisitJcheLimitService smtVisitJcheLimitService;
	@Autowired
	private RemoteSmsManageService remoteSmsManageService;
	@Autowired
	private SmtDeviceService smtDeviceDevice;
	@Autowired
	private SmtDeviceAuthorityRelationService deviceAuthorityRelationService;
	@Autowired
	private SmtDeviceTaskService smtDeviceTaskService;
	@Autowired
	private ImageService imageService;
	@Autowired
	private SmtImageService smtImageService;
	@Autowired
	private IAppMsgPushService appMsgPushService;
	@Autowired
	private SmtVisitorProcessRecordService smtVisitorProcessRecordService;
	@Autowired
	private SmtBlackVisitorService smtBlackVisitorService;
	@Autowired
	private SmtDeviceMapper smtDeviceMapper;
	@Autowired
	private SmtWhiteJobService smtWhiteJobService;
	@Autowired
	private RemoteDictService remoteDictService;
	@Autowired
	private SmtNoticeSwitchService smtNoticeSwitchService;
	@Autowired
	private SmtVehicleBlackService smtVehicleBlackService;
	@Autowired
	private RemoteEvwEmphrYsService remoteEvwEmphrYsService;
	@Autowired
	private SmtAdmittanceFellowService smtAdmittanceFellowService;
	@Autowired
	private SmtVisitorApprovalWhiteService smtVisitorApprovalWhiteService;
	@Autowired
	private SmtCommonConfigService smtCommonConfigService;
	@Autowired
	private SmtTaskDownRecordService smtTaskDownRecordService;
	@Autowired
	private SmtIscDownRecordService smtIscDownRecordService;
	@Autowired
	private SmtAdmittanceApplyService smtAdmittanceApplyService;
	@Autowired
	private SmtVisitorApprovalProxyService smtVisitorApprovalProxyService;

	@Value("${spring.visitor.put-offset-hour:2}")
	private Integer putOffsetHour;
	@Value("${spring.visitor.code-url}")
	private String codeUrl;
	@Value("${spring.visitor.remote-url}")
	private String remoteUrl;
	@Value("${spring.visitor.remote-path}")
	private String remotePath;
	@Value("${smart.hf-park-id}")
	private Integer hfParkId;
	@Value("${spring.visitor.hf-token}")
	private String hfToken;

	/**
	 * 修改状态访客审核
	 */
	@Override
	public Boolean updateVisitorStatusById(SmtVisitor smtVisitor) {
		//查询访客id是否存在
		int selectCount = this.count(Wrappers.<SmtVisitor>query().lambda()
				.eq(SmtVisitor::getId, smtVisitor.getId())
		);
		if (selectCount < 0) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_ID_ERROR);
		}
		//判断状态是否正确
		if (!RegexUtils.matchStatus(smtVisitor.getStatus().toString())) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_STATUS_ERROR);
		}
		SmtVisitor byId = this.getById(smtVisitor.getId());
		//更新访客流程表
		List<SmtVisitorProcessRecord> visitorProcessRecords = smtVisitorProcessRecordService.list(Wrappers.<SmtVisitorProcessRecord>query().lambda()
				.eq(SmtVisitorProcessRecord::getVisitorId, smtVisitor.getId())
				.eq(SmtVisitorProcessRecord::getStaffBadge, smtVisitor.getReceptionistBadge())
				.orderByAsc(SmtVisitorProcessRecord::getRecordNode));
		if (CollectionUtils.isEmpty(visitorProcessRecords)) {
			//查询是否存在代理人
			SmtVisitorApprovalProxy approvalProxie = smtVisitorApprovalProxyService.getOne(new LambdaQueryWrapper<SmtVisitorApprovalProxy>()
					.eq(SmtVisitorApprovalProxy::getParkId, byId.getParkId())
					.eq(SmtVisitorApprovalProxy::getProxyBadge, smtVisitor.getReceptionistBadge())
					.eq(SmtVisitorApprovalProxy::getIntervieweeBadge, byId.getReceptionistBadge())
			);
			if (null != approvalProxie) {
				visitorProcessRecords = smtVisitorProcessRecordService.list(Wrappers.<SmtVisitorProcessRecord>query().lambda()
						.eq(SmtVisitorProcessRecord::getVisitorId, smtVisitor.getId())
						.eq(SmtVisitorProcessRecord::getStaffBadge, byId.getReceptionistBadge())
						.orderByAsc(SmtVisitorProcessRecord::getRecordNode));
			}
		}
		for (SmtVisitorProcessRecord visitorProcessRecord : visitorProcessRecords) {
			visitorProcessRecord.setStatus(smtVisitor.getStatus());
			visitorProcessRecord.setStatusName(VisitorProcessEnum.desc(smtVisitor.getStatus()));
			visitorProcessRecord.setRecordDate(DateUtil.date());
			smtVisitorProcessRecordService.updateById(visitorProcessRecord);
		}

		//判断访客的所有流程是否已全部审批
		List<SmtVisitorProcessRecord> list = smtVisitorProcessRecordService.list(Wrappers.<SmtVisitorProcessRecord>query().lambda()
				.eq(SmtVisitorProcessRecord::getVisitorId, smtVisitor.getId())
				.orderByAsc(SmtVisitorProcessRecord::getRecordNode));
		log.info("所有流程：" + list);

		Boolean allProcess = true;
		Boolean isRefuse = false;
		for (SmtVisitorProcessRecord smtVisitorProcessRecord : list) {

			if (!smtVisitorProcessRecord.getStatus().equals(VisitorProcessEnum.PASS_0.getCode())) {
				allProcess = false;
			}
			if (smtVisitorProcessRecord.getStatus().equals(VisitorProcessEnum.REFUSE_1.getCode())) {
				isRefuse = true;
			}
		}
		//如果有两个节点，如果第一个节点审批通过，要通知第二个节点得领导
		if (list.size() > 1) {
			if (smtVisitor.getStatus().equals(SmtVisitorEnum.PASS_STATUS.getType()) && !allProcess) {
				SmtStaff reportToInfo = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, list.get(1).getStaffBadge()));
				log.info("===领导信息===：" + reportToInfo);
				//推送App消息
				AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
				appMsgPushDTO.setBadge(reportToInfo.getBadge());
				appMsgPushDTO.setBussiessId(String.valueOf(byId.getId()));
				appMsgPushDTO.setTemplateCode(SmsTemplateEnum.APP_PUSH_1301.getCode());
				appMsgPushService.pushAppMsg(appMsgPushDTO);

				//主管审批访客通知
				Result<SendSmsVo> sendMessage = sendMessage(reportToInfo.getPhone(), byId.getVisitorName(),
						SmsTemplateEnum.VISIT_1008.getCode(), byId.getReceptionistName(), DateUtils.formatDateTime(byId.getStartTime()),
						null, null, byId.getCompany(), reportToInfo.getName(), null, null,
						ParkNoticeTypeEnum.VISIT_APPROVE_BY_MANAGER.getCode(), byId.getParkId());
				//添加审核记录
				ApproveList approveList = new ApproveList();
				approveList.setBusinessId(byId.getId().toString());
				approveList.setApproveName(byId.getVisitorName() + "提交的访客申请");
				approveList.setApproveType(ApproveListTypeConstants.VISITOR);
				approveList.setApproveBadge(reportToInfo.getBadge());
				approveList.setApproveState(ApproveListStateEnum.PENDING.getCode());
				log.info("approveList:" + approveList);
				approveListService.saveApproveList(approveList);
			}
			//D级及以上，拒绝后，要通知C级人员
			if (smtVisitor.getStatus().equals(SmtVisitorEnum.NOTPASS_STATUS.getType())) {
				//拒绝短息
				log.info("领导已拒绝预约");
				Result<SendSmsVo> sendMessage2 = sendMessage(byId.getReceptionistPhone(), byId.getVisitorName(), SmsTemplateEnum.VISIT_10011.getCode(), byId.getReceptionistName(), DateUtils.formatDateTime(byId.getStartTime()),
						null, null, byId.getCompany(), null,
						smtVisitor.getRemark(), null, ParkNoticeTypeEnum.VISIT_APPLY_FAILD.getCode(), byId.getParkId());
				log.info("领导已拒绝预约:" + sendMessage2);
			}
		}

		log.info("allProcess:" + allProcess);
		log.info("isRefuse:" + isRefuse);
		//更新访客状态
		if (!StringUtils.isEmpty(smtVisitor.getStartTime())) {
			byId.setStartTime(smtVisitor.getStartTime());
		}
		if (!StringUtils.isEmpty(smtVisitor.getEndTime())) {
			byId.setEndTime(smtVisitor.getEndTime());
		}
		if (allProcess) {
			byId.setStatus(SmtVisitorEnum.PASS_STATUS.getType());
		}
		if (isRefuse) {
			byId.setStatus(SmtVisitorEnum.NOTPASS_STATUS.getType());
			byId.setRemark(smtVisitor.getRemark()); //拒绝原因
		}
		//根据访客的id查询访客的详细信息
		SmtVisitor visitor = this.getById(smtVisitor.getId());
		log.info("getReceptionistBadge:" + smtVisitor.getReceptionistBadge());
		if (smtVisitor.getStatus().equals(SmtVisitorEnum.PASS_STATUS.getType())) {
			//修改审核状态，已经通过
			if (StringUtils.isEmpty(visitor.getSmsCode())) {
				byId.setSmsCode(RandomUtil.randomNumbers(6));
			}
			updateApproveState(visitor.getId(), smtVisitor.getReceptionistBadge(), ApproveListStateEnum.AGREE.getCode());
		} else if (smtVisitor.getStatus().equals(SmtVisitorEnum.NOTPASS_STATUS.getType())) {
			//修改审核状态，已经拒绝
			updateApproveState(visitor.getId(), smtVisitor.getReceptionistBadge(), ApproveListStateEnum.REFUSE.getCode());
		}
		log.info("visitor:" + visitor);

		boolean updateById = this.updateById(byId);
		if (updateById) {
			//判断是否为状态为0：已经通过
			if (allProcess && smtVisitor.getStatus().equals(SmtVisitorEnum.PASS_STATUS.getType())) {
				//添加定时任务，下发闸机或者道闸
				addTaskVisitor(visitor);
				//给访客发送短信,调用短信发送接口
				String deviceName = "行政门";
				if (!StringUtils.isEmpty(byId.getVehiclePlate())) {
					deviceName = "物流门";
				}

				//预约成功通知
				Result<SendSmsVo> sendMessage = sendMessage(visitor.getVisitorPhone(), visitor.getVisitorName(),
						SmsTemplateEnum.VISIT_1001.getCode(), visitor.getReceptionistName(),
						DateUtils.formatDateTime(visitor.getStartTime()), null, deviceName, visitor.getCompany(),
						null, null, byId.getSmsCode(), ParkNoticeTypeEnum.VISIT_APPLY_SUCCESS.getCode(),
						visitor.getParkId());
				if (ObjectUtil.isNotNull(sendMessage) && !sendMessage.isSuccess()) {
					//发送失败后，要发短息通知
					sendMessageError(visitor.getReceptionistPhone(), SmsTemplateEnum.SMS_12001.getCode(),
							SmsTemplateEnum.VISIT_1001.getDesc(),
							sendMessage.getMsg(),
							ParkNoticeTypeEnum.SMS_SEND_FAILD.getCode(), visitor.getParkId());

				}
				//预约成功通知被访人,调用短信发送接口
				Result<SendSmsVo> sendMessage2 = sendMessage(visitor.getReceptionistPhone(), visitor.getVisitorName(), SmsTemplateEnum.VISIT_1006.getCode(), visitor.getReceptionistName(), DateUtils.formatDateTime(visitor.getStartTime()), null, null, visitor.getCompany(), null, null, null, ParkNoticeTypeEnum.VISIT_APPLY_SUCCESS_NOTICE_HOST.getCode(), visitor.getParkId());
				if (ObjectUtil.isNotNull(sendMessage2) && !sendMessage2.isSuccess()) {
					//发送失败后，要发短息通知
					sendMessageError(visitor.getReceptionistPhone(),
							SmsTemplateEnum.SMS_12001.getCode(), SmsTemplateEnum.VISIT_1006.getDesc(),
							sendMessage2.getMsg(), ParkNoticeTypeEnum.SMS_SEND_FAILD.getCode(), visitor.getParkId());
				}

			} else if (smtVisitor.getStatus().equals(SmtVisitorEnum.NOTPASS_STATUS.getType())) {
				//给访客发送短信，告诉访客预约失败，被拒绝
//				log.info("被拒绝");
//				if (list.size() > 1 && list.get(1).getStatus().equals(VisitorProcessEnum.REFUSE_1.getCode())) {
//					sendMessage(visitor.getReceptionistPhone(), visitor.getVisitorName(), SmsTemplateEnum.VISIT_10011.getCode(), visitor.getReceptionistName(), DateUtils.formatDateTime(visitor.getStartTime()), null, null, visitor.getCompany(), null, visitor.getRemark(), null,null);
//					}else {
				try {
					sendMessage(visitor.getVisitorPhone(), visitor.getVisitorName(), SmsTemplateEnum.VISIT_1002.getCode(), visitor.getReceptionistName(),
							DateUtils.formatDateTime(visitor.getStartTime()), null, null, visitor.getCompany(), null,
							smtVisitor.getRemark(), null, ParkNoticeTypeEnum.VISIT_APPLY_FAILD.getCode(), visitor.getParkId());
				} catch (Exception e) {
					log.error("发送访客预约拒绝短信异常-->{}", e.getMessage());
				}
				log.info("拒绝流程正常");
			}
		} else {
			throw new TCEException("修改状态异常");
		}
		log.info("审批流程正常");
		return true;
	}

	/**
	 * 修改审批的状态
	 *
	 * @param visitorId         visitorId
	 * @param receptionistBadge receptionistBadge
	 * @param approveState      approveState
	 */
	public void updateApproveState(Long visitorId, String receptionistBadge, Integer approveState) {
		//判断是否有访客
		List<ApproveList> selectList = approveListService.list(Wrappers.<ApproveList>query().lambda()
				.eq(ApproveList::getBusinessId, visitorId)
				.eq(ApproveList::getApproveType, ApproveListTypeConstants.VISITOR));
		if (selectList.size() > 0) {
			ApproveList approveList = new ApproveList();
			approveList.setBusinessId(visitorId.toString());
			approveList.setApproveState(approveState);
			approveList.setApproveType(ApproveListTypeConstants.VISITOR);
			approveList.setApproveBadge(receptionistBadge);
			boolean updateState = approveListService.updateState(approveList);
		}
	}


	/**
	 * @Title:查询是否有随行人员
	 * @Param :
	 * @Exception :
	 * @Return :List<SmtFellowVisitor>
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月17日 上午11:42:27
	 */
	private List<GetSmtFellowVisitorVO> getFellowPerson(Long visitorId) {
		SmtVisitor smtVisitor = new SmtVisitor();
		smtVisitor.setId(visitorId);
		List<GetSmtFellowVisitorVO> fellowVisitorList = smtFellowVisitorService.selectListByVisitorId(smtVisitor);
		return fellowVisitorList;
	}

	private void checkBlack(SmtVisitor smtVisitor) {
		SmtStaff smtStaffList = smtStaffService.getSimpleSttaffByBadge(smtVisitor.getReceptionistBadge());
		List<SmtVisitJcheLimit> jcheLimit = smtVisitJcheLimitService.listByJcheId(smtVisitor.getParkId(), smtStaffList.getJcheId(), ConfigBusinessEnum.VISITOR.getCode());
		if (Objects.isNull(jcheLimit)) {
			throw new TCEException("被访人没有预约权限");
		}

		//判断此人员是不是 加入黑名单
		//if (smtVisitor.getCause().equals(VisitorEnum.CAUSE_5.getCode())) {
		if (!StringUtils.isEmpty(smtVisitor.getCertNo())) {
			SmtBlackVisitor smtBlackVisitor = smtBlackVisitorService.getOne(Wrappers.<SmtBlackVisitor>query().lambda()
					.eq(SmtBlackVisitor::getParkId, smtVisitor.getParkId())
					.eq(SmtBlackVisitor::getCardNo, smtVisitor.getCertNo()));
			if (Objects.nonNull(smtBlackVisitor)) {
				throw new TCEException("此访客已被加入访客黑名单，不能邀请");
			}

			Result<List<EvwEmphrYsBlackRespDTO>> black = remoteEvwEmphrYsService.getBlackInfo(smtVisitor.getCertNo(), SecurityConstants.FROM_IN);
			if (black.isSuccess()) {
				if (black.getData() != null) {
					List<EvwEmphrYsBlackRespDTO> data = black.getData();
					if (data.size() > 0) {
						throw new TCEException("此访客已被加入访客黑名单，不能邀请");
					}
				}
			}
		}
		//}

		if (!StringUtils.isEmpty(smtVisitor.getVehiclePlate())) {
			SmtVehicleBlack smtVehicleBlack = smtVehicleBlackService.getOne(Wrappers.<SmtVehicleBlack>query().lambda()
					.eq(SmtVehicleBlack::getVehiclePlate, smtVisitor.getVehiclePlate()).eq(SmtVehicleBlack::getParkId, smtVisitor.getParkId()));
			if (Objects.nonNull(smtVehicleBlack)) {
				throw new TCEException("该车牌号已加入黑名单车辆，不能预约");
			}
		}
	}

	/**
	 * 访客预约添加
	 *
	 * @throws ParseException
	 */
	@Override
	public SmtVisitor saveSmtVisitor(SaveSmtVisitor saveSmtVisitor) {
		SmtVisitor smtVisitor = new SmtVisitor();
		smtVisitor.setDelFlag(0);
		smtVisitor.setVisitorName(saveSmtVisitor.getVisitorName());
		smtVisitor.setParkId(saveSmtVisitor.getParkId());
		smtVisitor.setCause(saveSmtVisitor.getCause());
		smtVisitor.setCertNo(saveSmtVisitor.getCertNo());
		smtVisitor.setCertType(saveSmtVisitor.getCertType());
		smtVisitor.setReceptionistBadge(saveSmtVisitor.getReceptionistBadge());
		//判断是否有车牌
		if (!StringUtils.isEmpty(saveSmtVisitor.getVehiclePlate())) {
			smtVisitor.setIsVehicle(SmtVisitorEnum.IS_VEHICLE.getType());
			smtVisitor.setVehiclePlate(saveSmtVisitor.getVehiclePlate());
		} else {
			smtVisitor.setIsVehicle(SmtVisitorEnum.NOT_VEHICLE.getType());
		}
		//黑名单检测
		try {
			this.checkBlack(smtVisitor);
		} catch (Exception e) {
			throw new TCEException(e.getMessage());
		}

		smtVisitor.setVisitorPhotoId(saveSmtVisitor.getVisitorPhoto());
		smtVisitor.setVisitorPhone(saveSmtVisitor.getVisitorPhone());

		smtVisitor.setCompany(saveSmtVisitor.getCompany());

		if (!StringUtils.isEmpty(saveSmtVisitor.getStartTime())) {
			smtVisitor.setStartTime(DateUtils.parse(saveSmtVisitor.getStartTime()));
		}
		if (!StringUtils.isEmpty(saveSmtVisitor.getEndTime())) {
			smtVisitor.setEndTime(DateUtils.parse(saveSmtVisitor.getEndTime()));
		}
		smtVisitor.setReceptionistName(saveSmtVisitor.getReceptionistName());
		smtVisitor.setReceptionistPhone(saveSmtVisitor.getReceptionistPhone());

		//正则判断
		ExceptionTypeEnum exceptionType = visitorCheck(smtVisitor);
		if (!exceptionType.equals(ExceptionTypeEnum.CHECK_SUCCESS)) {
			throw new TCEException(exceptionType);
		}
		//判断是否需要上级审批 ，默认为不需要 。true为需要上级审批，
		Boolean needApproval = Boolean.FALSE;
		ConfigVisitorApprovalDTO approveConfig = smtCommonConfigService.getVisitorApprove(saveSmtVisitor.getParkId());
		if (Objects.nonNull(approveConfig)) {
			needApproval = OneOrZeroEnum.ONE.getCode().equals(approveConfig.getNeedApproval()) ? Boolean.TRUE : Boolean.FALSE;
		}
		//从内向外
		if (!StringUtils.isEmpty(saveSmtVisitor.getPromoterBadge()) && !needApproval) {
			smtVisitor.setSmsCode(RandomUtil.randomNumbers(6));
			smtVisitor.setStatus(SmtVisitorEnum.PASS_STATUS.getType());
			smtVisitor.setPromoterBadge(saveSmtVisitor.getPromoterBadge());
		} else if (!StringUtils.isEmpty(saveSmtVisitor.getPromoterBadge()) && needApproval) {
			smtVisitor.setStatus(SmtAppVisitorEnum.UNTREATED_STATUS.getType());
			smtVisitor.setPromoterBadge(saveSmtVisitor.getPromoterBadge());
		} else {
			//从外向内
			smtVisitor.setStatus(SmtAppVisitorEnum.UNTREATED_STATUS.getType());
		}
		//添加一个访客时默认没有给访客发送预定多少分钟是否提醒的短信
		smtVisitor.setIsSend(SmtVisitorEnum.NOT_IS_SEND.getType());
		smtVisitor.setCreateTime(DateUtils.date());
		//驻场预约加身份证正反面
		if (saveSmtVisitor.getCause().equals(VisitorEnum.CAUSE_5.getCode()) || saveSmtVisitor.getCause().equals(VisitorEnum.CAUSE_7.getCode())) {
			//根据图片获取图片id
			String frontPicId = smtImageService.saveImage(saveSmtVisitor.getParkId(), saveSmtVisitor.getVisitorFrontPhoto(), SmtImageEnum.TYPE_VISITOR_IDCARD_FRONT.getCode());
			String backPicId = smtImageService.saveImage(saveSmtVisitor.getParkId(), saveSmtVisitor.getVisitorBackPhoto(), SmtImageEnum.TYPE_VISITOR_IDCARD_BACK.getCode());
			smtVisitor.setVisitorFrontPhotoId(frontPicId);
			smtVisitor.setVisitorBackPhotoId(backPicId);
			smtVisitor.setRemark(saveSmtVisitor.getRemark());
		}
		//添加访客信息
		boolean saveBoolean = this.save(smtVisitor);
		if (saveBoolean) {
			SmtStaff visitStaffInfo = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, smtVisitor.getPromoterBadge()));
			//添加审批流程
			Boolean leaderApprove = this.addVisitorInnerProcess(smtVisitor, visitStaffInfo, needApproval);
			//判断是否为审批状态是否为已通过或者无需二级审批
			if (smtVisitor.getStatus().equals(SmtVisitorEnum.PASS_STATUS.getType()) || !leaderApprove) {
				if (SmtAppVisitorEnum.UNTREATED_STATUS.getType().equals(smtVisitor.getStatus())) {
					smtVisitor.setStatus(SmtVisitorEnum.PASS_STATUS.getType());
					smtVisitor.setSmsCode(RandomUtil.randomNumbers(6));
					this.updateById(smtVisitor);
				}
				//发送短信给访客提醒预约成功，调用发短信接口
				String deviceName = "行政门";
				if (!StringUtils.isEmpty(saveSmtVisitor.getVehiclePlate())) {
					deviceName = "物流门";
				}
				//预约成功通知
				sendMessage(smtVisitor.getVisitorPhone(), smtVisitor.getVisitorName(), SmsTemplateEnum.VISIT_1001.getCode(),
						smtVisitor.getReceptionistName(), DateUtils.formatDateTime(smtVisitor.getStartTime()), null,
						deviceName, smtVisitor.getCompany(), null, null,
						smtVisitor.getSmsCode(), ParkNoticeTypeEnum.VISIT_APPLY_SUCCESS.getCode(), smtVisitor.getParkId());
				//预约成功通知被访人,调用短信发送接口
				sendMessage(smtVisitor.getReceptionistPhone(), smtVisitor.getVisitorName(), SmsTemplateEnum.VISIT_1006.getCode(),
						smtVisitor.getReceptionistName(), DateUtils.formatDateTime(smtVisitor.getStartTime()), null,
						null, smtVisitor.getCompany(), null, null, null,
						ParkNoticeTypeEnum.VISIT_APPLY_SUCCESS_NOTICE_HOST.getCode(), smtVisitor.getParkId());
				//添加定时任务，下发闸机或者道闸
				addTaskVisitor(smtVisitor);
			} else if (needApproval) {
				//添加上级审批
				this.addInnerApproveList(smtVisitor, visitStaffInfo.getReportTo());
				//推送App消息
				SmtStaff reportToInfo = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda()
						.eq(SmtStaff::getBadge, visitStaffInfo.getReportTo()));
				AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
				appMsgPushDTO.setBadge(reportToInfo.getBadge());
				appMsgPushDTO.setBussiessId(String.valueOf(smtVisitor.getId()));
				appMsgPushDTO.setTemplateCode(SmsTemplateEnum.APP_PUSH_1301.getCode());
				appMsgPushService.pushAppMsg(appMsgPushDTO);
				//主管审批访客通知
				Result<SendSmsVo> sendMessage = sendMessage(reportToInfo.getPhone(), smtVisitor.getVisitorName(),
						SmsTemplateEnum.VISIT_1008.getCode(), smtVisitor.getReceptionistName(), DateUtils.formatDateTime(smtVisitor.getStartTime()),
						null, null, smtVisitor.getCompany(), reportToInfo.getName(), null, null,
						ParkNoticeTypeEnum.VISIT_APPROVE_BY_MANAGER.getCode(), smtVisitor.getParkId());
			} else {
				this.AddApproveList(smtVisitor);
			}
		}
		return smtVisitor;
	}

	/**
	 * 添加访客邀约审批流程
	 *
	 * @param smtVisitor
	 * @param selectOne
	 * @return true：有二级审批流程 false 无二级审批流程
	 */
	private Boolean addVisitorInnerProcess(SmtVisitor smtVisitor, SmtStaff selectOne, Boolean flag) {
		SmtVisitorProcessRecord record = new SmtVisitorProcessRecord();
		record.setCreateDate(DateUtil.date());
		record.setRecordDate(DateUtil.date());
		record.setRecordNode(1);
		record.setRemark("");
		record.setStaffBadge(smtVisitor.getPromoterBadge());
		record.setStaffName(smtVisitor.getReceptionistName());
		record.setStatus(VisitorProcessEnum.PASS_0.getCode());
		record.setStatusName(VisitorProcessEnum.PASS_0.getDesc());
		record.setVisitorId(smtVisitor.getId());
		record.setStaffJche(selectOne.getJcheName());
		record.insert();
		if (!flag) {
			return Boolean.FALSE;
		}
		//判断被访人的岗位是否在白名单里，如果在白名单不需要二级审批
		List<SmtWhiteJob> list = new ArrayList<>();
		if (!StringUtils.isEmpty(selectOne.getJobId())) {
			list = smtWhiteJobService.list(Wrappers.<SmtWhiteJob>query().lambda().eq(SmtWhiteJob::getJobId, selectOne.getJobId()));
			log.info("白名单岗位，不需要二级审批：" + list);
		}
		//课长及以下级层需要二级审批
		String welfare = "abc";
		Boolean b = Boolean.FALSE;
		if (StringUtils.isEmpty(selectOne.getWelfareLevel()) ||
				welfare.contains(selectOne.getWelfareLevel().toLowerCase())) {
			b = Boolean.TRUE;
		}
		//判断被访人是否在被访审批白名单中
		int whiteCount = smtVisitorApprovalWhiteService.count(new LambdaQueryWrapper<SmtVisitorApprovalWhite>()
				.eq(SmtVisitorApprovalWhite::getStaffBadge, selectOne.getBadge())
				.eq(SmtVisitorApprovalWhite::getParkId, smtVisitor.getParkId())
		);

		//被访人岗位不在白名单中，层级在课长以下，工号不在审批白名单中
		if (CollectionUtils.isEmpty(list) && b && (whiteCount < 1)) {
			//上级领导员工号
			String reportTo = selectOne.getReportTo();
			if (!StringUtils.isEmpty(reportTo)) {
				SmtStaff reportToInfo = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, reportTo));
				SmtVisitorProcessRecord record2 = new SmtVisitorProcessRecord();
				record2.setCreateDate(DateUtil.date());
				record2.setRecordDate(DateUtil.date());
				record2.setRecordNode(2);
				record2.setRemark("");
				record2.setStaffBadge(reportToInfo.getBadge());
				record2.setStaffName(reportToInfo.getName());
				record2.setStatus(VisitorProcessEnum.WATING_2.getCode());
				record2.setStatusName(VisitorProcessEnum.WATING_2.getDesc());
				record2.setVisitorId(smtVisitor.getId());
				record2.setStaffJche(reportToInfo.getJcheName());
				record2.insert();
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}


	/**
	 * 公众号访客预约添加
	 *
	 * @throws ParseException
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public SmtVisitor saveWechatSmtVisitor(SaveWechatSmtVisitorDTO saveSmtVisitor) {
		SmtVisitor smtVisitor = new SmtVisitor();
		smtVisitor.setDelFlag(0);
		smtVisitor.setVisitorName(saveSmtVisitor.getVisitorName());
		smtVisitor.setCause(saveSmtVisitor.getCause());
		smtVisitor.setCertNo(saveSmtVisitor.getCertNo());
		smtVisitor.setParkId(saveSmtVisitor.getParkId());
		smtVisitor.setReceptionistBadge(saveSmtVisitor.getReceptionistBadge());
		smtVisitor.setCertType(saveSmtVisitor.getCertType());
		if (!StringUtils.isEmpty(saveSmtVisitor.getVehiclePlate())) {
			smtVisitor.setIsVehicle(SmtVisitorEnum.IS_VEHICLE.getType());
			smtVisitor.setVehiclePlate(saveSmtVisitor.getVehiclePlate());
		} else {
			smtVisitor.setIsVehicle(SmtVisitorEnum.NOT_VEHICLE.getType());
		}

		smtVisitor.setVisitorPhotoId(saveSmtVisitor.getVisitorPhotoId());
		smtVisitor.setVisitorPhone(saveSmtVisitor.getVisitorPhone());

		smtVisitor.setCompany(saveSmtVisitor.getCompany());


		if (!StringUtils.isEmpty(saveSmtVisitor.getStartTime())) {
			smtVisitor.setStartTime(DateUtils.parse(saveSmtVisitor.getStartTime()));
		}
		if (!StringUtils.isEmpty(saveSmtVisitor.getEndTime())) {
			smtVisitor.setEndTime(DateUtils.parse(saveSmtVisitor.getEndTime()));
		}

		smtVisitor.setReceptionistName(saveSmtVisitor.getReceptionistName());
		smtVisitor.setReceptionistPhone(saveSmtVisitor.getReceptionistPhone());


		if (StringUtils.isEmpty(smtVisitor.getVisitorPhotoId())) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_PHOTO_ID_EMPTY);
		}
		//正则判断
		ExceptionTypeEnum exceptionType = visitorCheck(smtVisitor);
		if (!exceptionType.equals(ExceptionTypeEnum.CHECK_SUCCESS)) {
			throw new TCEException(exceptionType);
		}

		//从外向内
		smtVisitor.setStatus(SmtAppVisitorEnum.UNTREATED_STATUS.getType());
		//此被访人员的员工信息
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, saveSmtVisitor.getReceptionistBadge()));
		log.info("被访人的信息：" + selectOne);
		smtVisitor.setReceptionistLevel(selectOne.getWelfareLevel());
		/*	smtVisitor.setParkId(selectOne.getParkId());*/
		//添加一个访客时默认没有给访客发送预定多少分钟是否提醒的短信
		smtVisitor.setIsSend(SmtVisitorEnum.NOT_IS_SEND.getType());
		smtVisitor.setCreateTime(DateUtils.date());
		//驻场预约加身份证正反面,驻场说明
		if (saveSmtVisitor.getCause().equals(VisitorEnum.CAUSE_5.getCode()) || saveSmtVisitor.getCause().equals(VisitorEnum.CAUSE_7.getCode())) {
			//根据图片获取图片id
			String frontPicId = smtImageService.saveImage(saveSmtVisitor.getParkId(), saveSmtVisitor.getVisitorFrontPhoto(), SmtImageEnum.TYPE_VISITOR_IDCARD_FRONT.getCode());
			String backPicId = smtImageService.saveImage(saveSmtVisitor.getParkId(), saveSmtVisitor.getVisitorBackPhoto(), SmtImageEnum.TYPE_VISITOR_IDCARD_BACK.getCode());
			smtVisitor.setVisitorFrontPhotoId(frontPicId);
			smtVisitor.setVisitorBackPhotoId(backPicId);
			smtVisitor.setRemark(saveSmtVisitor.getRemark());
		}
		//添加访客信息
		boolean saveBoolean = this.save(smtVisitor);
		if (saveBoolean) {
			//添加访客审批流程
			addVisitorProcess(smtVisitor, selectOne);
			//添加待我审核
			AddApproveList(smtVisitor);

			//查询被访人是否有代理人
			List<SmtVisitorApprovalProxy> proxyList = smtVisitorApprovalProxyService.list(new LambdaQueryWrapper<SmtVisitorApprovalProxy>()
					.eq(SmtVisitorApprovalProxy::getIntervieweeBadge, smtVisitor.getReceptionistBadge())
			);
			if (CollectionUtils.isNotEmpty(proxyList)) {
				proxyList.forEach(item -> {
					//添加代理人审批
					ApproveList proApproveList = new ApproveList();
					proApproveList.setBusinessId(smtVisitor.getId().toString());
					proApproveList.setApproveName(smtVisitor.getVisitorName() + "提交的访客申请");
					proApproveList.setApproveType(ApproveListTypeConstants.VISITOR);
					proApproveList.setApproveBadge(item.getProxyBadge());
					proApproveList.setApproveState(ApproveListStateEnum.PENDING.getCode());
					approveListService.saveApproveList(proApproveList);

					//查询代理人员工信息
					StaffInfoVO smtStaffInfoByBadge = smtStaffService.getSmtStaffInfoByBadge(item.getProxyBadge());

					if (null != smtStaffInfoByBadge && !StringUtils.isEmpty(smtStaffInfoByBadge.getSmtStaff().getPhone())) {
						//发送代理人短信
						Result<SendSmsVo> sendMessage = sendVisitorProxyMessage(smtStaffInfoByBadge.getSmtStaff().getPhone(), smtVisitor.getVisitorName(), SmsTemplateEnum.VISIT_10012.getCode(), smtVisitor.getReceptionistName(), DateUtils.formatDateTime(smtVisitor.getStartTime()), null, null, smtVisitor.getCompany(), null, null, null, ParkNoticeTypeEnum.VISIT_APPLY.getCode(), smtVisitor.getParkId());
						if (ObjectUtil.isNotNull(sendMessage) && !sendMessage.isSuccess()) {
							log.info("信息发送result:{}", sendMessage);
							//发送失败后，要发短息通知
							sendMessageError(smtStaffInfoByBadge.getSmtStaff().getPhone(), SmsTemplateEnum.SMS_12001.getCode(),
									SmsTemplateEnum.VISIT_10012.getDesc(), sendMessage.getMsg(),
									ParkNoticeTypeEnum.SMS_SEND_FAILD.getCode(), smtVisitor.getParkId());
						}
					}
				});
			}

			//推送被访人短信
			Result<SendSmsVo> sendMessage = sendMessage(smtVisitor.getReceptionistPhone(), smtVisitor.getVisitorName(),
					SmsTemplateEnum.VISIT_1005.getCode(),
					smtVisitor.getReceptionistName(), DateUtils.formatDateTime(smtVisitor.getStartTime()),
					null, null, smtVisitor.getCompany(), null, null,
					null, ParkNoticeTypeEnum.VISIT_APPLY.getCode(), smtVisitor.getParkId());
			if (ObjectUtil.isNotNull(sendMessage) && !sendMessage.isSuccess()) {
				log.info("信息发送result:{}", sendMessage);
				//发送失败后，要发短息通知
				sendMessageError(smtVisitor.getReceptionistPhone(), SmsTemplateEnum.SMS_12001.getCode(), SmsTemplateEnum.VISIT_1005.getDesc(), sendMessage.getMsg(), ParkNoticeTypeEnum.SMS_SEND_FAILD.getCode(), smtVisitor.getParkId());
			}
			//推送App消息
			AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
			appMsgPushDTO.setBadge(saveSmtVisitor.getReceptionistBadge());
			appMsgPushDTO.setBussiessId(String.valueOf(smtVisitor.getId()));
			appMsgPushDTO.setTemplateCode(SmsTemplateEnum.APP_PUSH_1301.getCode());
			/*
			 * 修复【裕同科技石岩龙岗智慧园区#6756】缺陷
			 * 访客申请消息，申请消息的标题名字应该是访客的名字，而不是被访人的名字，不然单子太多，员工不知道哪张单子是谁的，得一个一个点进去看
			 * start by sfj
			 */
			appMsgPushDTO.setApplicant(saveSmtVisitor.getVisitorName());
			// end
			appMsgPushService.pushAppMsg(appMsgPushDTO);
		}
		return smtVisitor;
	}

	/**
	 * 公众号访客再约一次
	 *
	 * @throws ParseException
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean wechatSmtVisitorAgain(VisitorAgainReqDTO visitorAgainReqDTO) {
		//修改预约记录为 未处理
		//修改开始时间和结束时间
		SmtVisitor smtVisitor = new SmtVisitor();
		smtVisitor.setStatus(SmtAppVisitorEnum.UNTREATED_STATUS.getType());
		smtVisitor.setStartTime(visitorAgainReqDTO.getStartTime());
		smtVisitor.setEndTime(visitorAgainReqDTO.getEndTime());
		boolean updateRes = this.update(smtVisitor, new LambdaUpdateWrapper<SmtVisitor>()
				.eq(SmtVisitor::getId, visitorAgainReqDTO.getId()));
		if (updateRes) {
			//查询预约记录
			SmtVisitor visitorRecord = this.getById(visitorAgainReqDTO.getId());
			//查询被访员工信息
			SmtStaff visitoredStaff = smtStaffService.getSimpleSttaffByBadge(visitorRecord.getReceptionistBadge());
			//添加访客审批流程
			addVisitorProcess(visitorRecord, visitoredStaff);
			//添加待我审核
			AddApproveList(smtVisitor);
			//推送被访人短信
			Result<SendSmsVo> sendMessage = sendMessage(smtVisitor.getReceptionistPhone(), smtVisitor.getVisitorName(), SmsTemplateEnum.VISIT_1005.getCode(), smtVisitor.getReceptionistName(), DateUtils.formatDateTime(smtVisitor.getStartTime()), null, null, smtVisitor.getCompany(), null, null, null, ParkNoticeTypeEnum.VISIT_APPLY.getCode(), smtVisitor.getParkId());
			if (ObjectUtil.isNotNull(sendMessage) && !sendMessage.isSuccess()) {
				//发送失败后，要发短息通知
				sendMessageError(smtVisitor.getReceptionistPhone(), SmsTemplateEnum.SMS_12001.getCode(), SmsTemplateEnum.VISIT_1005.getDesc(), sendMessage.getMsg(), ParkNoticeTypeEnum.SMS_SEND_FAILD.getCode(), smtVisitor.getParkId());
			}
			//推送App消息
			AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
			appMsgPushDTO.setBadge(visitorRecord.getReceptionistBadge());
			appMsgPushDTO.setBussiessId(String.valueOf(smtVisitor.getId()));
			appMsgPushDTO.setTemplateCode(SmsTemplateEnum.APP_PUSH_1301.getCode());
			appMsgPushService.pushAppMsg(appMsgPushDTO);
		} else {
			return false;
		}
		return true;
	}


	//添加访客审批流程
	private void addVisitorProcess(SmtVisitor smtVisitor, SmtStaff selectOne) {
		// TODO Auto-generated method stub
		SmtVisitorProcessRecord smtVisitorProcessRecord = new SmtVisitorProcessRecord();
		smtVisitorProcessRecord.setCreateDate(DateUtil.date());
		smtVisitorProcessRecord.setRecordDate(DateUtil.date());
		smtVisitorProcessRecord.setRecordNode(1);
		smtVisitorProcessRecord.setRemark("");
		smtVisitorProcessRecord.setStaffBadge(smtVisitor.getReceptionistBadge());
		smtVisitorProcessRecord.setStaffName(smtVisitor.getReceptionistName());
		smtVisitorProcessRecord.setStatus(VisitorProcessEnum.WATING_2.getCode());
		smtVisitorProcessRecord.setStatusName(VisitorProcessEnum.WATING_2.getDesc());
		smtVisitorProcessRecord.setVisitorId(smtVisitor.getId());
		smtVisitorProcessRecord.setStaffJche(selectOne.getJcheName());
		smtVisitorProcessRecordService.save(smtVisitorProcessRecord);
		//判断被访人的岗位是否在白名单里，如果在白名单不需要二级审批
		List<SmtWhiteJob> list = new ArrayList<>();
		if (!StringUtils.isEmpty(selectOne.getJobId())) {
			list = smtWhiteJobService.list(Wrappers.<SmtWhiteJob>query().lambda().eq(SmtWhiteJob::getJobId, selectOne.getJobId()));
			log.info("白名单岗位，不需要二级审批：" + list);
		}
		//课长及以下级层需要二级审批
		String welfare = "abc";
		Boolean b = Boolean.FALSE;
		if (StringUtils.isEmpty(selectOne.getWelfareLevel()) ||
				welfare.contains(selectOne.getWelfareLevel().toLowerCase())) {
			b = Boolean.TRUE;
		}
		//判断被访人是否在被访审批白名单中
		int whiteCount = smtVisitorApprovalWhiteService.count(new LambdaQueryWrapper<SmtVisitorApprovalWhite>()
				.eq(SmtVisitorApprovalWhite::getStaffBadge, selectOne.getBadge())
				.eq(SmtVisitorApprovalWhite::getParkId, smtVisitor.getParkId())
		);

		//被访人岗位不在白名单中，层级在课长以下，工号不在审批白名单中
		if (CollectionUtils.isEmpty(list) && b && (whiteCount < 1)) {
			//上级领导员工号
			String reportTo = selectOne.getReportTo();
			if (!StringUtils.isEmpty(reportTo)) {
				SmtStaff reportToInfo = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, reportTo));
				SmtVisitorProcessRecord record = new SmtVisitorProcessRecord();
				record.setCreateDate(DateUtil.date());
				record.setRecordDate(DateUtil.date());
				record.setRecordNode(2);
				record.setRemark("");
				record.setStaffBadge(reportToInfo.getBadge());
				record.setStaffName(reportToInfo.getName());
				record.setStatus(VisitorProcessEnum.WATING_2.getCode());
				record.setStatusName(VisitorProcessEnum.WATING_2.getDesc());
				record.setVisitorId(smtVisitor.getId());
				record.setStaffJche(reportToInfo.getJcheName());
				smtVisitorProcessRecordService.save(record);
			}
		}
	}

	/**
	 * 添加下发任务
	 *
	 * @param smtVisitor
	 */
	private void addTaskVisitor(SmtVisitor smtVisitor) {
		if (!Objects.isNull(smtVisitor)) {
			Integer parkId = smtVisitor.getParkId();
			//查询访客人员设备权限
			List<SmtDeviceAuthorityRelation> visitorDeviceList = deviceAuthorityRelationService.getRelationAuth(parkId,
					BusinessAuthorityEnum.VISITOR_FACE.getCode(), DeviceAuthorityEnum.VISITOR);
			//查询访客车辆的的设备权限
			List<SmtDeviceAuthorityRelation> vehicleDeviceList = deviceAuthorityRelationService.getRelationAuth(parkId,
					BusinessAuthorityEnum.VISITOR_VEHICLE.getCode(), DeviceAuthorityEnum.VISITOR_VEHICLE);
			//下发闸机,下发道闸
			addVisitor(smtVisitor, visitorDeviceList, vehicleDeviceList);
		}
	}

	/**
	 * 添加访客的闸机数据
	 *
	 * @param visitor
	 * @param visitorDeviceList
	 * @param vehicleDeviceList
	 */
	private void addVisitor(SmtVisitor visitor, List<SmtDeviceAuthorityRelation> visitorDeviceList, List<SmtDeviceAuthorityRelation> vehicleDeviceList) {

		//添加访客设备权限
		addCard(visitor, visitorDeviceList);
		//添加随行人员设备权限
		addFellow(visitor, visitorDeviceList);
		//判断是否有车牌 有车牌下发道闸
		if (visitor.getIsVehicle().equals(SmtVisitorEnum.IS_VEHICLE.getType())) {
			addCarCard(visitor, vehicleDeviceList);
		}
	}

	/**
	 * 随行人员下发闸机
	 *
	 * @param visitor
	 * @param visitorDeviceList
	 */
	private void addFellow(SmtVisitor visitor, List<SmtDeviceAuthorityRelation> visitorDeviceList) {
		//判断是否有随行人员
		List<GetSmtFellowVisitorVO> fellowList = getFellowPerson(visitor.getId());
		if (CollectionUtils.isNotEmpty(fellowList)) {
			fellowList.forEach(f -> addCard(visitor, f, visitorDeviceList));
		}
	}

	/**
	 * 访客的随行人员添加闸机
	 *
	 * @param visitor
	 * @param fellowVisitorVO
	 * @param deviceAuthorityRelations
	 */
	private void addCard(SmtVisitor visitor, GetSmtFellowVisitorVO fellowVisitorVO, List<SmtDeviceAuthorityRelation> deviceAuthorityRelations) {
		if (CollectionUtils.isNotEmpty(deviceAuthorityRelations)) {
			deviceAuthorityRelations.forEach(d -> {
				addCard(
						visitor,
						fellowVisitorVO,
						d.getDeviceId()
				);
			});
		}
	}

	/**
	 * 访客添加闸机
	 *
	 * @param visitor
	 * @param deviceAuthorityRelations
	 */
	private void addCard(SmtVisitor visitor, List<SmtDeviceAuthorityRelation> deviceAuthorityRelations) {
		if (CollectionUtils.isNotEmpty(deviceAuthorityRelations)) {
			deviceAuthorityRelations.forEach(d -> {
				addCard(visitor, d.getDeviceId());
			});
		}
	}

	/**
	 * 访客随行人员下发闸机
	 *
	 * @param visitor
	 * @param deviceId
	 */
	private void addCard(SmtVisitor visitor, GetSmtFellowVisitorVO fellowVisitorVO, String deviceId) {
		DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
		deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
		deviceTaskVO.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		deviceTaskVO.setCardNo(fellowVisitorVO.getId().toString());
		deviceTaskVO.setDeviceCode(deviceId);
		deviceTaskVO.setCardType(SmtVisitorEnum.CARD_TYPE_7.getType());
		deviceTaskVO.setImageId(fellowVisitorVO.getFellowPhotoId());
		deviceTaskVO.setDeviceType(DeviceTaskConstants.CARD);
		deviceTaskVO.setStartTime(DateUtils.offsetHour(visitor.getStartTime(), -putOffsetHour).getTime() / 1000);
		deviceTaskVO.setOverTime(visitor.getEndTime().getTime() / 1000);
		deviceTaskVO.setGeneral(fellowVisitorVO.getFellowName());
		saveRequiredDeviceTask(deviceTaskVO);
	}

	/**
	 * 访客下发闸机
	 *
	 * @param visitor
	 * @param deviceId
	 */
	private void addCard(SmtVisitor visitor, String deviceId) {
		if (com.tce.smart.common.core.util.StringUtils.isNotEmpty(visitor.getVisitorPhotoId())) {
			DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
			deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
			deviceTaskVO.setServiceType(DeviceTaskConstants.CARD_VISITOR);
			deviceTaskVO.setCardNo(visitor.getId().toString());
			deviceTaskVO.setDeviceCode(deviceId);
			deviceTaskVO.setGeneral(visitor.getVisitorName());
			deviceTaskVO.setCardType(SmtVisitorEnum.CARD_TYPE_7.getType());
			deviceTaskVO.setImageId(visitor.getVisitorPhotoId());
			deviceTaskVO.setDeviceType(DeviceTaskConstants.CARD);
			deviceTaskVO.setStartTime(DateUtils.offsetHour(visitor.getStartTime(), -putOffsetHour).getTime() / 1000);
			deviceTaskVO.setOverTime(visitor.getEndTime().getTime() / 1000);
			deviceTaskVO.setApplyBadge(visitor.getCertNo());			//存储访客的身份证
			saveRequiredDeviceTask(deviceTaskVO);
		}
	}

	/**
	 * 下发道闸
	 *
	 * @param visitor
	 * @param deviceAuthorityRelations
	 */
	private void addCarCard(SmtVisitor visitor, List<SmtDeviceAuthorityRelation> deviceAuthorityRelations) {
		if (CollectionUtils.isNotEmpty(deviceAuthorityRelations)) {
			deviceAuthorityRelations.forEach(d -> {
				//下发道闸
				addCarCard(
						visitor,
						d.getDeviceId()
				);
			});
		}
	}

	/**
	 * 下发道闸
	 *
	 * @param visitor
	 * @param deviceId
	 */
	private void addCarCard(SmtVisitor visitor, String deviceId) {
		DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
		deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
		deviceTaskVO.setServiceType(DeviceTaskConstants.CAR_VISITOR);
		deviceTaskVO.setCardNo(visitor.getId().toString());
		deviceTaskVO.setDeviceCode(deviceId);
		deviceTaskVO.setGeneral(visitor.getVehiclePlate());
		deviceTaskVO.setCardType(SmtVisitorEnum.CAR_CARD_TYPE_0.getType());
		deviceTaskVO.setDeviceType(DeviceTaskConstants.CAR);
		deviceTaskVO.setStartTime(DateUtils.offsetHour(visitor.getStartTime(), -putOffsetHour).getTime() / 1000);
		deviceTaskVO.setOverTime(visitor.getEndTime().getTime() / 1000);
		deviceTaskVO.setApplyBadge(visitor.getCertNo());
		saveRequiredDeviceTask(deviceTaskVO);
	}

	private void saveRequiredDeviceTask(DeviceTaskVO deviceTaskVO) {
		String taskResult = smtDeviceTaskService.saveTask(deviceTaskVO);
		if (DEVICE_TASK_EXISTS_MESSAGE.equals(taskResult)) {
			log.info("访客下发任务已存在，deviceCode={}，cardNo={}", deviceTaskVO.getDeviceCode(), deviceTaskVO.getCardNo());
			return;
		}
		if (isUnsupportedIscVehicleTask(deviceTaskVO, taskResult)) {
			log.info("访客ISC车辆权限不支持下发，按跳过成功处理，deviceCode={}，cardNo={}",
					deviceTaskVO.getDeviceCode(), deviceTaskVO.getCardNo());
			return;
		}
		if (!isDeviceTaskId(taskResult)) {
			throw new IllegalStateException("访客下发任务创建失败，deviceCode=" + deviceTaskVO.getDeviceCode()
					+ "，cardNo=" + deviceTaskVO.getCardNo() + "，result=" + taskResult);
		}
	}

	private boolean isUnsupportedIscVehicleTask(DeviceTaskVO deviceTaskVO, String taskResult) {
		return deviceTaskVO != null
				&& DeviceTaskConstants.CAR.equals(deviceTaskVO.getDeviceType())
				&& ISC_VEHICLE_AUTH_UNSUPPORTED_MESSAGE.equals(taskResult);
	}

	private boolean isDeviceTaskId(String taskResult) {
		if (StrUtil.isBlank(taskResult)) {
			return false;
		}
		for (int i = 0; i < taskResult.length(); i++) {
			if (!Character.isDigit(taskResult.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	//添加被访人待我审批信息和审批代理人带我审核信息
	public void AddApproveList(SmtVisitor smtVisitor) {
		ApproveList approveList = new ApproveList();
		approveList.setBusinessId(smtVisitor.getId().toString());
		approveList.setApproveName(smtVisitor.getVisitorName() + "提交的访客申请");
		approveList.setApproveType(ApproveListTypeConstants.VISITOR);
		approveList.setApproveBadge(smtVisitor.getReceptionistBadge());
		approveList.setApproveState(ApproveListStateEnum.PENDING.getCode());
		approveListService.saveApproveList(approveList);
	}

	//添加访客邀约二级审批信息
	public void addInnerApproveList(SmtVisitor smtVisitor, String badge) {
		ApproveList approveList = new ApproveList();
		approveList.setBusinessId(smtVisitor.getId().toString());
		approveList.setApproveName(smtVisitor.getVisitorName() + "的访客申请");
		approveList.setApproveType(ApproveListTypeConstants.VISITOR);
		approveList.setApproveBadge(badge);
		approveList.setApproveState(ApproveListStateEnum.PENDING.getCode());
		approveListService.saveApproveList(approveList);
	}


	/**
	 * @Title:访客的正则判断
	 * @Param :visitor
	 * @Return :ExceptionType
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月15日 下午3:28:48
	 */
	private ExceptionTypeEnum visitorCheck(SmtVisitor visitor) {
		String visitorName = visitor.getVisitorName();
		String visitorPhone = visitor.getVisitorPhone();
		String vehiclePlate = visitor.getVehiclePlate();
		Integer cause = visitor.getCause();
		Date startTime = visitor.getStartTime();
		Date endTime = visitor.getEndTime();
		String promoterBadge = visitor.getPromoterBadge();

		if (!RegexUtils.matchName(visitorName)) {
			return ExceptionTypeEnum.VISITOR_NAME_LENGTH_ERROR;
		}
		if (!RegexUtils.matchPhone(visitorPhone)) {
			return ExceptionTypeEnum.VISITOR_PHONE_ERROR;
		}
//		为满足访客预约接待入园方式 人脸和车牌可以同时为空 注释掉下面的判断
//		if (StringUtils.isEmpty(visitorPhotoId) && StringUtils.isEmpty(vehiclePlate)) {
//			return ExceptionTypeEnum.VISITOR_PHOTO_ID_PLATFORM_EMPTY;
//		}
		if (!StringUtils.isEmpty(promoterBadge)) {
			SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, promoterBadge));
			if (Objects.isNull(selectOne)) {
				return ExceptionTypeEnum.VISITOR_PROMOTERBADGE_NULL;
			}
			if ("11".equals(selectOne.getJcheId()) || "8".equals(selectOne.getJcheId()) || "9".equals(selectOne.getJcheId())) {
				return ExceptionTypeEnum.VISITOR_JCHE_ID_ERROR;
			}
		}
		if (!StringUtils.isEmpty(vehiclePlate)) {
			if (!RegexUtils.matchVehicle(vehiclePlate)) {
				return ExceptionTypeEnum.VISITOR_VEHICLE_PLATE_ERROR;
			}
		}
		if (Objects.isNull(cause)) {
			return ExceptionTypeEnum.VISITOR_CAUSE_EMPTY;
		}
		if (StringUtils.isEmpty(startTime)) {
			return ExceptionTypeEnum.VISITOR_STARTTIME_EMPTY;
		}
		if (StringUtils.isEmpty(endTime)) {
			return ExceptionTypeEnum.VISITOR_ENDTIME_EMPTY;
		}
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		if (!RegexUtils.matchDate(sf.format(startTime))) {
			return ExceptionTypeEnum.VISITOR_STARTTIME_FORMAT_ERROR;
		}
		if (!RegexUtils.matchDate(sf.format(endTime))) {
			return ExceptionTypeEnum.VISITOR_ENDTIME_FORMAT_ERROR;
		}
		if (endTime.before(startTime)) {
			return ExceptionTypeEnum.VISITOR_ENDTIME_CANT_BEFORE_STARTTIME;
		}
		return ExceptionTypeEnum.CHECK_SUCCESS;
	}

	/**
	 * @Title:随行人员的正则
	 * @Param :fellowVisitor
	 * @Return :ExceptionType
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月15日 下午3:28:38
	 */
	private ExceptionTypeEnum FellowVisitorCheck(SmtFellowVisitor fellowVisitor) {
		Long visitorId = fellowVisitor.getVisitorId();
		String fellowName = fellowVisitor.getFellowName();
		String fellowPhotoId = fellowVisitor.getFellowPhotoId();
		if (Objects.isNull(visitorId)) {
			return ExceptionTypeEnum.VISITOR_ID_NULL;
		}
		if (StringUtils.isEmpty(fellowName)) {
			return ExceptionTypeEnum.FELLOW_NAME_EMPTY;
		}
		if (StringUtils.isEmpty(fellowPhotoId)) {
			return ExceptionTypeEnum.FELLOW_PHOTO_ID_EMPTY;
		}
		if (!RegexUtils.matchName(fellowName)) {
			return ExceptionTypeEnum.FELLOW_NAME_LENGTH_ERROR;
		}
		return ExceptionTypeEnum.CHECK_SUCCESS;
	}

	/**
	 * 后台查询访客的详细信息
	 */
	@Override
	public SearchVisitorDetail searchVisitorDetailById(Long id, SmtSnapVehicleService smtSnapVehicleService) {
		SmtVisitor smtVisitor = new SmtVisitor();
		smtVisitor.setId(id);
		//根据访客id查询访客的和被访人的相关信息
		SearchVisitorDetail searchVisitorDetail = this.baseMapper.selectVisitorById(smtVisitor);
		searchVisitorDetail.setCauseDesc(VisitorEnum.desc(searchVisitorDetail.getCause()));
		if (ObjectUtil.isNotNull(searchVisitorDetail.getVisitorPhotoId())) {
			searchVisitorDetail.setVisitorPhoto(imageService.buildImageUrl(searchVisitorDetail.getVisitorPhotoId()));
		}
		if (Objects.nonNull(searchVisitorDetail.getCarryThing())) {
			searchVisitorDetail.setCarryThingDesc(HfVisitCarryItemsEnum.desc(searchVisitorDetail.getCarryThing()));
		}
		if (ObjectUtil.isNotNull(searchVisitorDetail.getVisitorFrontPhotoId())) {
			searchVisitorDetail.setVisitorFrontPhoto(imageService.buildImageUrl(searchVisitorDetail.getVisitorFrontPhotoId()));
		}
		if (ObjectUtil.isNotNull(searchVisitorDetail.getVisitorBackPhotoId())) {
			searchVisitorDetail.setVisitorBackPhoto(imageService.buildImageUrl(searchVisitorDetail.getVisitorBackPhotoId()));
		}
		if (StrUtil.isNotEmpty(searchVisitorDetail.getTripCode())) {
			searchVisitorDetail.setTripCode(imageService.buildImageUrl(searchVisitorDetail.getTripCode()));
		}
		if (StrUtil.isNotEmpty(searchVisitorDetail.getHealthcode())) {
			searchVisitorDetail.setHealthcode(imageService.buildImageUrl(searchVisitorDetail.getHealthcode()));
		}
		List<SnapVisitor> snapVisitorList = new ArrayList<>();

		//根据访客的id查询跟随人员的信息
		List<GetSmtFellowVisitorVO> fellowVisitorList = getFellowPerson(searchVisitorDetail.getVisitorId());
		if (fellowVisitorList.size() > 0) {
			for (GetSmtFellowVisitorVO vo : fellowVisitorList) {
				if (ObjectUtil.isNotNull(vo.getFellowPhotoId())) {
					vo.setFellowPhoto(imageService.buildImageUrl(vo.getFellowPhotoId()));
				}
				if (StrUtil.isNotEmpty(vo.getCertPic())) {
					vo.setCertPic(imageService.buildImageUrl(vo.getCertPic()));
				}
				if (Objects.nonNull(vo.getCertType())) {
					vo.setCertTypeDesc(AdmittancePersonCertTypeEnum.desc(vo.getCertType()));
				}
			}
			searchVisitorDetail.setFellowVisitorList(fellowVisitorList);
		}
		//判断是否已经到达，如果到达则查询抓拍信息
		if (searchVisitorDetail.getStatus().equals(SmtVisitorEnum.COME_STATUS.getType())) {
			//查询抓拍的信息,当访客没有车辆时
			if (StringUtils.isEmpty(searchVisitorDetail.getVehiclePlate())) {
				//查询访客的抓拍记录图片
				SnapVisitor snapVisitor = getSnapPerson(searchVisitorDetail.getVisitorId());
				//添加访客的抓拍信息
				if (snapVisitor != null) {
					snapVisitorList.add(snapVisitor);
				}
				//判断是否有随行人员
				if (fellowVisitorList.size() > 0) {
					for (int j = 0; j < fellowVisitorList.size(); j++) {
						SnapVisitor snapVisitorFollow = getSnapPerson(fellowVisitorList.get(j).getId());
						//获取抓拍最早的一张图片信息
						if (snapVisitorFollow != null) {
							snapVisitorList.add(snapVisitorFollow);
						}
					}
				}
			} else {
				//获取车牌抓拍信息
				SnapVisitor snapVisitorVehicle = getSnapVehicle(searchVisitorDetail.getVisitorId(), smtSnapVehicleService);
				//开车抓拍的信息,//当有车牌时，没有随行人员的访客
				if (snapVisitorVehicle != null) {
					//获取车辆的抓拍信息
					snapVisitorList.add(snapVisitorVehicle);
				} else {
					//如果访客没有开车进来，则查询抓拍的人脸信息
					SnapVisitor snapVisitor = getSnapPerson(searchVisitorDetail.getVisitorId());
					//添加访客的抓拍信息
					if (snapVisitor != null) {
						snapVisitorList.add(snapVisitor);
					}
				}
				//判断是否有随行人员
				if (fellowVisitorList.size() > 0) {
					for (int j = 0; j < fellowVisitorList.size(); j++) {
						//判断随行人员是否下车刷脸进入
						SnapVisitor snapVisitorFollow = getSnapPerson(fellowVisitorList.get(j).getId());
						if (snapVisitorFollow != null) {
							snapVisitorList.add(snapVisitorFollow);
						}
					}
				}
			}
		}
		if (snapVisitorList.size() > 0) {
			ListSort(snapVisitorList);
		}
		searchVisitorDetail.setSnapVisitorList(snapVisitorList);

		//审批流程
		List<SmtVisitorProcessRecord> processList = new ArrayList<>();
		if (StrUtil.isNotEmpty(searchVisitorDetail.getProcessId())) {
			processList = this.getOaProcess(searchVisitorDetail.getProcessId());
		} else {
			processList = getVisitorProcess(id);
		}
		searchVisitorDetail.setProcessList(processList);

		SmtPark park = smtParkService.getById(searchVisitorDetail.getParkId());
		searchVisitorDetail.setParkName(park.getParkName());

		if (null != searchVisitorDetail.getCertType()) {
			if (hfParkId.toString().equals(searchVisitorDetail.getParkId())) {
				searchVisitorDetail.setCertTypeDesc(AdmittancePersonCertTypeEnum.desc(searchVisitorDetail.getCertType()));
			} else {
				searchVisitorDetail.setCertTypeDesc(CertTypeEnum.desc(searchVisitorDetail.getCertType()));
			}
		}

		return searchVisitorDetail;

	}

	/**
	 * 获得oa审批流程
	 *
	 * @param processId
	 * @return
	 */
	private List<SmtVisitorProcessRecord> getOaProcess(String processId) {
		List<FlowVO> vos = new ArrayList<>();
		smtOutDormitoryStaffService.getOAProcessFlow(processId, vos);
		List<SmtVisitorProcessRecord> processRecords = new ArrayList<>();
		if (CollUtil.isNotEmpty(vos)) {
			for (FlowVO vo : vos) {
				SmtVisitorProcessRecord record = new SmtVisitorProcessRecord();
				record.setCreateDate(vo.getProcessDate());
				record.setRecordDate(vo.getProcessDate());
				record.setStaffBadge(vo.getCreateUser());
				record.setStatusName(vo.getProcessDesc());
				record.setStaffName(vo.getCreateUser());
				processRecords.add(record);
			}
		}
		return processRecords;
	}

	private static void ListSort(List<SnapVisitor> list) {
		{    //排序方法
			Collections.sort(list, new Comparator<SnapVisitor>() {
				@Override
				public int compare(SnapVisitor o1, SnapVisitor o2) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						// format.format(o1.getTime()) 表示 date转string类型 如果是string类型就不要转换了
						Date dt1 = format.parse(format.format(o1.getSnapTime()));
						Date dt2 = format.parse(format.format(o2.getSnapTime()));
						// 这是由大向小排序   如果要由小向大转换比较符号就可以
						if (dt1.getTime() < dt2.getTime()) {
							return 1;
						} else if (dt1.getTime() > dt2.getTime()) {
							return -1;
						} else {
							return 0;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return 0;
				}
			});
		}
	}

	/**
	 * @Title:获取抓拍人员图片数据
	 * @Param :
	 * @Return :List<SmtSnapPerson>
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月17日 上午11:42:27
	 */
	private SnapVisitor getSnapPerson(Long id) {
		List<SmtSnapPerson> smtSnapList = smtSnapPersonMapper.selectList(Wrappers.<SmtSnapPerson>query().lambda().eq(SmtSnapPerson::getPersonId, id).orderByAsc(SmtSnapPerson::getSnapTime));
		//获取抓拍最早的一张图片信息
		if (smtSnapList.size() > 0) {
			SnapVisitor snapVisitorPerson = new SnapVisitor();
			snapVisitorPerson.setSnapPhotoId(smtSnapList.get(0).getSnapPhotoId());
			snapVisitorPerson.setSnapTime(smtSnapList.get(0).getSnapTime());
			snapVisitorPerson.setSnapPhoto(imageService.buildImageUrl(smtSnapList.get(0).getParkId(), smtSnapList.get(0).getSnapPhotoId()));
			return snapVisitorPerson;
		}
		return null;
	}

	/**
	 * @Title:获取抓拍最新的人员图片数据
	 * @Param :
	 * @Return :List<SmtSnapPerson>
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月17日 上午11:42:27
	 */
	private SnapVisitor getSnapNewPerson(Long id) {
		List<SmtSnapPerson> smtSnapList = smtSnapPersonMapper.selectList(Wrappers.<SmtSnapPerson>query().lambda().eq(SmtSnapPerson::getPersonId, id).orderByDesc(SmtSnapPerson::getSnapTime));

		//获取抓拍最早的一张图片信息
		if (smtSnapList.size() > 0) {
			SnapVisitor snapVisitorPerson = new SnapVisitor();
			snapVisitorPerson.setSnapPhotoId(smtSnapList.get(0).getSnapPhotoId());
			snapVisitorPerson.setSnapPhoto(imageService.buildImageUrl(smtSnapList.get(0).getSnapPhotoId()));
			snapVisitorPerson.setSnapTime(smtSnapList.get(0).getSnapTime());
			return snapVisitorPerson;
		}
		return null;
	}

	/**
	 * @Title:获取抓拍车辆图片数据
	 * @Param :
	 * @Return :List<SmtSnapPerson>
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月17日 上午11:42:27
	 */
	private SnapVisitor getSnapVehicle(Long id, SmtSnapVehicleService smtSnapVehicleService) {
		List<SmtSnapVehicle> smtSnapVehicleList = smtSnapVehicleService.list(Wrappers.<SmtSnapVehicle>query().lambda().eq(SmtSnapVehicle::getDriverId, id).orderByAsc(SmtSnapVehicle::getSnapTime));
		//获取抓拍最早的一张图片信息
		if (smtSnapVehicleList.size() > 0) {
			SnapVisitor snapVisitorVehicle = new SnapVisitor();
			snapVisitorVehicle.setSnapPhotoId(smtSnapVehicleList.get(0).getSnapPhotoId());
			snapVisitorVehicle.setSnapPhoto(imageService.buildImageUrl(smtSnapVehicleList.get(0).getParkId(), smtSnapVehicleList.get(0).getSnapPhotoId()));
			snapVisitorVehicle.setSnapTime(smtSnapVehicleList.get(0).getSnapTime());
			return snapVisitorVehicle;
		}
		return null;

	}

	/**
	 * 获取当天的访客预约信息
	 */
	@Override
	public IPage<SearchTodayVisitor> getTodayVisitor(Page page) {


		SearchTodayVisitorDTO searchTodayVisitorDTO = new SearchTodayVisitorDTO();
		searchTodayVisitorDTO.setStartTime(getTodayEndTime());
		searchTodayVisitorDTO.setEndTime(DateUtil.format((DateUtils.date()), "yyyy-MM-dd HH:mm:ss"));
		/*searchTodayVisitorDTO.setStartTime(getTodayStartTime());
		searchTodayVisitorDTO.setEndTime(getTodayEndTime());*/
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		return this.baseMapper.getTodayVisitor(page, searchTodayVisitorDTO, parkIdList);
	}

	private String getTodayStartTime() {

		DateFormat dateFormat = DateFormat.getDateTimeInstance();//格式化后的时间格式：2016-2-19 20:54:53
		Calendar todayStart = new GregorianCalendar();
		todayStart.set(Calendar.HOUR_OF_DAY, 0);
		todayStart.set(Calendar.MINUTE, 0);
		todayStart.set(Calendar.SECOND, 0);
		return dateFormat.format(todayStart.getTime());
	}

	private String getTodayEndTime() {
		DateFormat dateFormat = DateFormat.getDateTimeInstance();//格式化后的时间格式：2016-2-19 20:54:53
		Calendar todayEnd = Calendar.getInstance();
		todayEnd.set(Calendar.HOUR_OF_DAY, 23);
		todayEnd.set(Calendar.MINUTE, 59);
		todayEnd.set(Calendar.SECOND, 59);
		return dateFormat.format(todayEnd.getTime());
	}

	/**
	 * 根据personID查询判断访客是否存在
	 */

	private Boolean getVisitorByPersonId(Long personId) {
		Integer selectCount = this.baseMapper
				.selectCount(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getId, personId));
        return selectCount > 0;
    }

	/**
	 * 根据personID查询判断随行人员是否存在
	 */

	private Boolean getVisitorFellowByPersonId(Long personId) {
		Integer selectFellowCount = smtFellowVisitorService
				.count(Wrappers.<SmtFellowVisitor>query().lambda().eq(SmtFellowVisitor::getId, personId));
        return selectFellowCount > 0;
    }

	/**
	 * 获取当天最新条的抓拍信息
	 */
	@Override
	public SearchTadayVisitorDetail getNewSnapVisitor() {
		List<SnapTodayFellowVisitor> snapTodayFellowVisitorList = new ArrayList<SnapTodayFellowVisitor>();
		SearchTadayVisitorDetail searchTadayVisitorDetail = new SearchTadayVisitorDetail();
		//当访客没有车,获取最新的抓拍信息
		SearchSnapPersonAccessDTO searchSnapPersonAccessDto = new SearchSnapPersonAccessDTO();
		searchSnapPersonAccessDto.setStartTime(getTodayStartTime());
		searchSnapPersonAccessDto.setEndTime(getTodayEndTime());
		searchSnapPersonAccessDto.setPersonType(SmtVisitorEnum.VISITOR_TYPE.getType());
		searchSnapPersonAccessDto.setEventType(EventTypeEnum.EVENT_TYPE_1.getCode());
		List<SmtSnapPerson> smtSnapList = smtSnapPersonMapper.getSnapPersonList(searchSnapPersonAccessDto);
		if (smtSnapList.size() > 0) {
			Long personId = smtSnapList.get(0).getPersonId();
			String SnapPhotoId = smtSnapList.get(0).getSnapPhotoId();
			//判断是否为访客的id,如果为访客的id
			if (getVisitorByPersonId(personId)) {
				//根据访客id查询访客的和被访人的相关信息
				searchTadayVisitorDetail = selectTodayVisitorById(personId);
				//获取该访客的最新抓拍的信息并且存入
				searchTadayVisitorDetail.setSnapPhotoId(SnapPhotoId);
				searchTadayVisitorDetail.setSnapPhoto(imageService.buildImageUrl(SnapPhotoId));
				searchTadayVisitorDetail.setSnapTime(smtSnapList.get(0).getSnapTime());
				searchTadayVisitorDetail.setAreaName(smtSnapList.get(0).getAreaName());

				SmtPark park = smtParkService.getById(smtSnapList.get(0).getParkId());
				searchTadayVisitorDetail.setParkName(park.getParkName());
			}
			//判断是否为随行人员的id,如果为随行人员的id
			if (getVisitorFellowByPersonId(personId)) {
				SmtFellowVisitor selectOne = smtFellowVisitorService.getOne(Wrappers.<SmtFellowVisitor>query().lambda().eq(SmtFellowVisitor::getId, personId));
				//根据访客id查询访客的和被访人的相关信息
				searchTadayVisitorDetail = selectTodayVisitorById(selectOne.getVisitorId());
				//获取最新抓拍的访客数据
				SnapVisitor snapNewPerson = getSnapNewPerson(selectOne.getVisitorId());
				SnapTodayFellowVisitor snapTodayFellowVisitor = new SnapTodayFellowVisitor();
				if (snapNewPerson != null) {
					searchTadayVisitorDetail.setSnapTime(snapNewPerson.getSnapTime());
					snapTodayFellowVisitor.setSnapPhoto(imageService.buildImageUrl(snapNewPerson.getSnapPhotoId()));
				}
				snapTodayFellowVisitor.setFellowPhoto(imageService.buildImageUrl(selectOne.getFellowPhotoId()));
				snapTodayFellowVisitorList.add(snapTodayFellowVisitor);
			}
		}
		searchTadayVisitorDetail.setSnapTodayFellowVisitorList(snapTodayFellowVisitorList);
		//当有车时
		SnapVehicleAccessDTO snapVehicleAccessDTO = new SnapVehicleAccessDTO();
		snapVehicleAccessDTO.setStartTime(getTodayStartTime());
		snapVehicleAccessDTO.setEndTime(getTodayEndTime());
		snapVehicleAccessDTO.setVehicleAscription(SmtVisitorEnum.VEHICLE_TYPE.getType());
		snapVehicleAccessDTO.setEventType(EventTypeEnum.EVENT_TYPE_1.getCode());
		List<SmtSnapVehicle> smtSnapVehicleList = smtSnapVehicleMapper.getSnapVehicleList(snapVehicleAccessDTO);
		if (smtSnapVehicleList.size() > 0) {
			//判断是否有抓拍人员数据
			if (smtSnapList.size() > 0) {
				//判断抓拍车辆的数据时间与抓拍人员的图片时间做对比
				if (smtSnapVehicleList.get(0).getSnapTime().after(smtSnapList.get(0).getSnapTime())) {
					//根据访客id查询访客的和被访人的相关信息
					searchTadayVisitorDetail = selectTodayVisitorById(smtSnapVehicleList.get(0).getDriverId());
					searchTadayVisitorDetail.setSnapPhotoId(smtSnapVehicleList.get(0).getSnapPhotoId());
					searchTadayVisitorDetail.setSnapPhoto(imageService.buildImageUrl(smtSnapVehicleList.get(0).getSnapPhotoId()));
					searchTadayVisitorDetail.setSnapTime(smtSnapVehicleList.get(0).getSnapTime());
					searchTadayVisitorDetail.setAreaName(smtSnapVehicleList.get(0).getAreaName());
				}
			} else {
				//根据访客id查询访客的和被访人的相关信息
				searchTadayVisitorDetail = selectTodayVisitorById(smtSnapVehicleList.get(0).getDriverId());
				searchTadayVisitorDetail.setSnapPhotoId(smtSnapVehicleList.get(0).getSnapPhotoId());
				searchTadayVisitorDetail.setSnapPhoto(imageService.buildImageUrl(smtSnapVehicleList.get(0).getSnapPhotoId()));
				searchTadayVisitorDetail.setSnapTime(smtSnapVehicleList.get(0).getSnapTime());
				searchTadayVisitorDetail.setAreaName(smtSnapVehicleList.get(0).getAreaName());
			}

		}
		return searchTadayVisitorDetail;
	}

	/**
	 * @Title:根据访客id查询访客的和被访人的相关信息
	 * @Param :
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月17日 上午11:42:27
	 */
	private SearchTadayVisitorDetail selectTodayVisitorById(Long personId) {
		SmtVisitor SmtVisitor = new SmtVisitor();
		SmtVisitor.setId(personId);
		SearchTadayVisitorDetail searchTadayVisitorDetail = this.baseMapper.selectTodayVisitorById(SmtVisitor);
		searchTadayVisitorDetail.setId(personId);
		if (ObjectUtil.isNotNull(searchTadayVisitorDetail.getVisitorPhotoId())) {
			searchTadayVisitorDetail.setVisitorPhoto(imageService.buildImageUrl(searchTadayVisitorDetail.getVisitorPhotoId()));
		}
		searchTadayVisitorDetail.setCauseDesc(VisitorEnum.desc(searchTadayVisitorDetail.getCause()));
		return searchTadayVisitorDetail;
	}

	/**
	 * 查询访客的分页信息
	 */
	@Override
	public IPage<SearchSmtVisitorVO> getSmtVisitorPage(Page page, SearchSmtVisitorDTO searchSmtVisitorDTO) {

		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		IPage<SearchSmtVisitorVO> smtVisitorPage = this.baseMapper.getSmtVisitorPage(page, searchSmtVisitorDTO, parkIdList);
		if (smtVisitorPage.getRecords().size() > 0) {
			for (int i = 0; i < smtVisitorPage.getRecords().size(); i++) {

				SearchSmtVisitorVO visitorVO = smtVisitorPage.getRecords().get(i);

				//根据图片的id获取图片的base64位
				if (ObjectUtil.isNotNull(visitorVO.getVisitorPhotoId())) {
					visitorVO.setVisitorPhoto(imageService.buildImageUrl(visitorVO.getVisitorPhotoId()));
				}

				//查询是否有行程码与健康码
				if (StrUtil.isNotEmpty(visitorVO.getHealthcode())) {
					visitorVO.setHealthcode("是");
				} else {
					visitorVO.setHealthcode("否");
				}
				if (StrUtil.isNotEmpty(visitorVO.getTripCode())) {
					visitorVO.setTripCode("是");
				} else {
					visitorVO.setTripCode("否");
				}
				//查询访客人脸是否存在下发成功记录
				int cardCount = smtTaskDownRecordService.count(new LambdaQueryWrapper<SmtTaskDownRecord>()
						.eq(SmtTaskDownRecord::getCardNo, visitorVO.getId())
						.eq(SmtTaskDownRecord::getDeviceType, DeviceTaskConstants.CARD)
						.eq(SmtTaskDownRecord::getServiceType, DeviceTaskConstants.CARD_VISITOR)
				);

				if (cardCount == 0) {
					//查询ISC下发成功记录
					cardCount = smtIscDownRecordService.count(new LambdaQueryWrapper<SmtIscDownRecord>()
							.eq(SmtIscDownRecord::getCardNo, visitorVO.getId())
							.eq(SmtIscDownRecord::getDeviceType, DeviceTaskConstants.CARD)
							.eq(SmtIscDownRecord::getServiceType, DeviceTaskConstants.CARD_VISITOR));
				}

				//查询访客车辆是否存在下发成功记录
				int carCount = 0;
				if (com.tce.smart.common.core.util.StringUtils.isNotBlank(visitorVO.getVehiclePlate())) {
					//存在车牌
					carCount = smtTaskDownRecordService.count(new LambdaQueryWrapper<SmtTaskDownRecord>()
							.eq(SmtTaskDownRecord::getCardNo, visitorVO.getId())
							.eq(SmtTaskDownRecord::getGeneral, visitorVO.getVehiclePlate())
							.eq(SmtTaskDownRecord::getDeviceType, DeviceTaskConstants.CAR)
							.eq(SmtTaskDownRecord::getServiceType, DeviceTaskConstants.CAR_VISITOR)
					);
					if (carCount == 0) {
						carCount = smtIscDownRecordService.count(new LambdaQueryWrapper<SmtIscDownRecord>()
								.eq(SmtIscDownRecord::getCardNo, visitorVO.getId())
								.eq(SmtIscDownRecord::getGeneral, visitorVO.getVehiclePlate())
								.eq(SmtIscDownRecord::getDeviceType, DeviceTaskConstants.CAR)
								.eq(SmtIscDownRecord::getServiceType, DeviceTaskConstants.CAR_VISITOR));
					}
				}
				//此处的权限状态 只用于展示 因此没有使用状态枚举
				visitorVO.setHasAuth(0);
				if (cardCount > 0 || carCount > 0) {
					visitorVO.setHasAuth(1);
				}

			}
		}
		return smtVisitorPage;
	}

	/**
	 * 根据图片base64获取图片的id
	 *
	 * @param photo
	 */
	public String getPhotoId(Integer parkId, String photo) {
		//if(!StringUtils.isEmpty(photo)) {
		//图片压缩
		byte[] bytes = Base64.decodeBase64(photo);
		log.info("【图片压缩】 图片原大小={}kb", bytes.length / 1024);
		while (bytes.length > 200 * 1024) {
			bytes = compressPicForScale(bytes, (long) ((bytes.length / 1024) * 0.95));
		}
		log.info("【图片压缩】 压缩后大小={}kb", bytes.length / 1024);
		String encodePhoto = Base64.encodeBase64String(bytes);
		//Result result = remoteFaceService.faceImageStore(encodePhoto, SecurityConstants.FROM_IN);

//		Map<String, Object> queryFeatureReq = new HashMap<>();
//		queryFeatureReq.put("imageData", encodePhoto);
//		DispatcherDTO<Map<String, Object>> dispatcherDTO = new DispatcherDTO<>();
//		dispatcherDTO.setEventId(IdUtil.simpleUUID());
//		dispatcherDTO.setEventType(EventEnum.FACE_FEATURE_EXTRACT.getCode());
//		dispatcherDTO.setParkId(parkId);
//		dispatcherDTO.setData(queryFeatureReq);
//		Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
		Result<FaceFeaturesDTO> result = remoteAlgorithmService.getFaceFeatures(encodePhoto, SecurityConstants.FROM_IN);
		log.info("获取人脸特征值 result:{}", result);
/*		JsonObject data = bodyObject.getAsJsonObject("data");
		if(null!=data){
			String faceFeature = data.get("faceFeature").getAsString();
			if(null!=faceFeature){
				String blobId =  blobService.saveBlob(base64Face);
				log.info("图片是人脸,存储到BlobId :{}",blobId);
				result.setData(blobId);
			}
		}*/

		//判断是否等于0
		if (result.getCode().equals(0)) {
			/*			JSONObject obj = (JSONObject) result.getData();*/
			return result.getData().getFaceFeature();
		} else {
			throw new TCEException(ExceptionTypeEnum.VISITOR_PHOTO_ERROR);
		}
	/*	}else {
		    throw new TCEException(ExceptionTypeEnum.VISITOR_PHOTO_NULL);
		}*/
	}

	/**
	 * 车辆抓拍记录访客车辆信息补充
	 *
	 * @param entity 抓拍车辆信息
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void visitorSnapHandle(AddSnapVehicleDTO entity, SmtSnapVehicleService smtSnapVehicleService) {
		if (!StringUtils.isEmpty(entity.getCardNo())) {
			SmtVisitor selectOne = this.baseMapper.selectOne(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getId, entity.getCardNo()));
			if (selectOne != null) {
				//首次进门，并是未到达的状态下发短信
				if (entity.getEventType().equals(VehicleEventTypEnum.IN.getCode()) && !selectOne.getStatus().equals(SmtVisitorEnum.COME_STATUS.getType())) {
					selectOne.setStatus(SmtVisitorEnum.COME_STATUS.getType());

					this.baseMapper.updateById(selectOne);
					//根据设备的id查询设备名称
					SmtDevice selectDeviceById = smtDeviceDevice.getById(entity.getDeviceId());
					//给访被访人发送短信,调用短信发送接口
					sendMessage(selectOne.getReceptionistPhone(), selectOne.getVisitorName(), SmsTemplateEnum.VISIT_1004.getCode(), selectOne.getReceptionistName(), DateUtils.formatDateTime(selectOne.getStartTime()), DateUtils.formatDateTime(entity.getSnapTime()), selectDeviceById.getDeviceName(), selectOne.getCompany(), null, null, null, ParkNoticeTypeEnum.VISITOR_ARRIVE_REAL.getCode(), selectOne.getParkId());
				}
				//根据设备id查询通道号和地点
				GetDeviceVO getDeviceVo = getDevice(entity.getDeviceId());

				//访客车辆首次出门下发短信,除驻场人员外
				if (entity.getEventType().equals(VehicleEventTypEnum.OUT.getCode()) && !selectOne.getCause().equals(VisitorEnum.CAUSE_5.getCode())) {
					int snapCount = smtSnapVehicleService.count(Wrappers.<SmtSnapVehicle>query().lambda()
							.eq(SmtSnapVehicle::getDriverId, selectOne.getId())
							.eq(SmtSnapVehicle::getVehicleAscription, SnapVehicleConstants.VISITOR_VEHICLE)
							.eq(SmtSnapVehicle::getEventType, EventTypeEnum.EVENT_TYPE_2.getCode()));
					selectOne.setStatus(VisitorStatusEnum.CAUSE_5.getCode());
					this.baseMapper.updateById(selectOne);
					if (snapCount == 0) {
						log.info("=====访客车辆首次出门===");
						sendMessage(selectOne.getReceptionistPhone(), selectOne.getVisitorName(), SmsTemplateEnum.VISIT_1007.getCode(), selectOne.getReceptionistName(), DateUtils.formatDateTime(selectOne.getStartTime()), DateUtils.formatDateTime(entity.getSnapTime()), getDeviceVo.getDeviceName(), null, null, null, null, null, selectOne.getParkId());

					}
					//访客出门后删除车辆，删除访客，删除随行人员
					log.info("================removeTask-starting===========");
					//查询访客人员设备权限
					List<SmtDeviceAuthorityRelation> deviceAuthList = deviceAuthorityRelationService.getRelationAuth(selectOne.getParkId(),
							BusinessAuthorityEnum.VISITOR_FACE.getCode(), DeviceAuthorityEnum.VISITOR);
					//查询访客车辆的的设备权限
					List<SmtDeviceAuthorityRelation> deviceVehicleAuthList = deviceAuthorityRelationService.getRelationAuth(selectOne.getParkId(),
							BusinessAuthorityEnum.VISITOR_VEHICLE.getCode(), DeviceAuthorityEnum.VISITOR_VEHICLE);
					delPersonCardTask(selectOne, deviceAuthList);
					delCarCardTask(selectOne, deviceVehicleAuthList);

				}

				/*if(!selectOne.getStatementStatus().equals(SmtVisitorEnum.COME_STATUS.getType())) {
					selectOne.setStatementStatus(SmtVisitorEnum.COME_STATUS.getType());
					this.baseMapper.updateById(selectOne);
				}*/
				entity.setVehicleAscription(VehicleBelongTypeEnum.VISITOR_VEHICLE.getCode());
				entity.setDriverId(selectOne.getId());
				entity.setDriverName(selectOne.getVisitorName());
				entity.setDriverPhone(selectOne.getVisitorPhone());
				entity.setDriverType(VehicleBelongTypeEnum.VISITOR_VEHICLE.getCode());
			}
		}
	}


	private void delCarCardTask(SmtVisitor smtVisitor, List<SmtDeviceAuthorityRelation> deviceAuthList) {
		// TODO Auto-generated method stub
		DeviceTaskVO deviceTaskVO;
		for (int i = 0; i < deviceAuthList.size(); i++) {
			//查询是否已生成删除任务
			SmtDeviceTask deviceTask = smtDeviceTaskService.getOne(new LambdaQueryWrapper<SmtDeviceTask>()
					.eq(SmtDeviceTask::getCardNo, smtVisitor.getId())
					.eq(SmtDeviceTask::getDeviceCode, deviceAuthList.get(i).getDeviceId())
					.eq(SmtDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
					.eq(SmtDeviceTask::getDeviceType, DeviceTaskConstants.CAR)
					.eq(SmtDeviceTask::getServiceType, DeviceTaskServiceTypeEnum.CAR_VISITOR.getCode())
					.eq(SmtDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
			);
			if (null != deviceTask) {
				//访客预约已存在待处理的删除任务 访客出门后 把删除时间调整为当前
				deviceTask.setOverTime(DateUtils.currentSeconds());
				smtDeviceTaskService.updateById(deviceTask);
			} else {
				deviceTaskVO = new DeviceTaskVO();
				deviceTaskVO.setAction(DeviceTaskConstants.DEL);
				deviceTaskVO.setCardNo(smtVisitor.getId().toString());
				deviceTaskVO.setDeviceCode(deviceAuthList.get(i).getDeviceId());
				deviceTaskVO.setStartTime(DateUtils.currentSeconds());
				deviceTaskVO.setOverTime(DateUtils.currentSeconds());
				deviceTaskVO.setGeneral(smtVisitor.getVehiclePlate());
				deviceTaskVO.setDeviceType(DeviceTaskConstants.CAR);
				deviceTaskVO.setServiceType(DeviceTaskServiceTypeEnum.CAR_VISITOR.getCode());
				smtDeviceTaskService.saveTask(deviceTaskVO);
			}
		}
	}


	private CarCardDTO delCarCardInfo(String visitorId, String deviceId) {
		// TODO Auto-generated method stub
		CarCardDTO carCardInfo = new CarCardDTO();
		carCardInfo.setDeviceCode(deviceId);
		carCardInfo.setCardNo(visitorId);
		return carCardInfo;
	}

	/**
	 * 添加人员卡片删除任务
	 *
	 * @param smtVisitor     访客信息
	 * @param deviceAuthList 设备权限列表
	 */
	private void delPersonCardTask(SmtVisitor smtVisitor, List<SmtDeviceAuthorityRelation> deviceAuthList) {
		DeviceTaskVO deviceTaskVO;
		CardDTO cardInfo = null;
		for (int i = 0; i < deviceAuthList.size(); i++) {
			//查询是否已生成删除任务
			SmtDeviceTask deviceTask = smtDeviceTaskService.getOne(new LambdaQueryWrapper<SmtDeviceTask>()
					.eq(SmtDeviceTask::getCardNo, smtVisitor.getId())
					.eq(SmtDeviceTask::getDeviceCode, deviceAuthList.get(i).getDeviceId())
					.eq(SmtDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
					.eq(SmtDeviceTask::getDeviceType, DeviceTaskConstants.CARD)
					.eq(SmtDeviceTask::getServiceType, DeviceTaskServiceTypeEnum.CARD_VISITOR.getCode())
					.eq(SmtDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
			);
			if (null != deviceTask) {
				//访客预约已存在待处理的删除任务 访客出门后 把删除时间调整为当前
				deviceTask.setOverTime(DateUtils.currentSeconds());
				smtDeviceTaskService.updateById(deviceTask);
			} else {
				deviceTaskVO = new DeviceTaskVO();
				deviceTaskVO.setAction(DeviceTaskConstants.DEL);
				deviceTaskVO.setCardNo(smtVisitor.getId().toString());
				deviceTaskVO.setDeviceCode(deviceAuthList.get(i).getDeviceId());
				deviceTaskVO.setStartTime(DateUtils.currentSeconds());
				deviceTaskVO.setOverTime(DateUtils.currentSeconds());
				deviceTaskVO.setImageId(smtVisitor.getVisitorPhotoId());
				deviceTaskVO.setGeneral(smtVisitor.getVisitorName());
				deviceTaskVO.setDeviceType(DeviceTaskConstants.CARD);
				deviceTaskVO.setServiceType(DeviceTaskServiceTypeEnum.CARD_VISITOR.getCode());
				deviceTaskVO.setApplyBadge(smtVisitor.getCertNo());
				smtDeviceTaskService.saveTask(deviceTaskVO);
			}
		}
	}

	/**
	 * 人员卡片删除
	 *
	 * @param visitorId 访客预约ID
	 * @param deviceId  设备编号
	 * @return
	 */
	private CardDTO delCardInfo(String visitorId, String deviceId) {
		CardDTO cardDTO = new CardDTO();
		cardDTO.setCardNo(visitorId);
		cardDTO.setDeviceCode(deviceId);
		return cardDTO;
	}

	/**
	 * @Title:查询设备id通道和地点
	 * @Param :
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月18日 上午11:42:27
	 */
	private GetDeviceVO getDevice(String id) {
		SmtDevice smtDevice = new SmtDevice();
		smtDevice.setId(id);
		/*		GetDeviceVO getDeviceVo = smtDeviceMapper.getDeviceById(smtDevice);
		 */
		GetDeviceVO getDeviceVo = smtDeviceMapper.getDeviceById(id);
		return getDeviceVo;
	}

	/**
	 * 发送短信通知
	 *
	 * @param number          number
	 * @param visitorName     visitorName
	 * @param tempCode        tempCode
	 * @param hostName        hostName
	 * @param appointmentDate appointmentDate
	 * @param realityDate     realityDate
	 * @param deviceName      deviceName
	 * @param reportToName
	 */
	public Result<SendSmsVo> sendMessage(String number, String visitorName, String tempCode, String hostName,
										 String appointmentDate, String realityDate, String deviceName, String company, String reportToName,
										 String refuseDes, String smsCode, String swichCode, Integer parkId) {
		//给访客发送短信,调用短信发送接口

		int noticeSwitch = smtNoticeSwitchService.count(Wrappers.<SmtNoticeSwitch>query().lambda()
				.eq(SmtNoticeSwitch::getSwitchCode, swichCode)
				.eq(SmtNoticeSwitch::getIsOn, 1)
				.eq(SmtNoticeSwitch::getParkId, parkId));

		SmtPark smtPark = smtParkService.getById(parkId);
		AppointmentMsgReqDTO appointmentMsgAo = new AppointmentMsgReqDTO();
		appointmentMsgAo.setNumber(number);
		appointmentMsgAo.setVisitorName(visitorName);
		appointmentMsgAo.setTempCode(tempCode);
		appointmentMsgAo.setHostName(hostName);
		appointmentMsgAo.setAppointmentDate(appointmentDate);
		appointmentMsgAo.setRealityDate(realityDate);
		appointmentMsgAo.setDeviceName(deviceName);
		appointmentMsgAo.setCompany(company);
		appointmentMsgAo.setReportToName(reportToName);
		appointmentMsgAo.setRefuseDes(refuseDes);
		appointmentMsgAo.setSmsCode(smsCode);
		appointmentMsgAo.setParkName(smtPark.getParkName());
		if (Objects.nonNull(smsCode)) {
			SmtVisitor smtVisitor = this.baseMapper.selectOne(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getSmsCode, smsCode).eq(SmtVisitor::getDelFlag, 0));
			appointmentMsgAo.setCodeUrl(codeUrl.replace("{id}", String.valueOf(smtVisitor.getId())));
		}

		if (noticeSwitch > 0) {
			Result<SendSmsVo> sendAppointmentSms = remoteSmsManageService.sendAppointmentSms(appointmentMsgAo);
			log.info("remoteSmsManageService.sendAppointmentSms result={}" + sendAppointmentSms);
			return sendAppointmentSms;
		}
		return null;
	}

	public Result<SendSmsVo> sendVisitorProxyMessage(String number, String visitorName, String tempCode, String hostName, String appointmentDate, String realityDate, String deviceName, String company, String reportToName, String refuseDes, String smsCode, String swichCode, Integer parkId) {
		//给访客审批代理人发送短信,调用短信发送接口
		SmtPark smtPark = smtParkService.getById(parkId);
		AppointmentMsgReqDTO appointmentMsgAo = new AppointmentMsgReqDTO();
		appointmentMsgAo.setNumber(number);
		appointmentMsgAo.setVisitorName(visitorName);
		appointmentMsgAo.setTempCode(tempCode);
		appointmentMsgAo.setHostName(hostName);
		appointmentMsgAo.setAppointmentDate(appointmentDate);
		appointmentMsgAo.setRealityDate(realityDate);
		appointmentMsgAo.setDeviceName(deviceName);
		appointmentMsgAo.setCompany(company);
		appointmentMsgAo.setReportToName(reportToName);
		appointmentMsgAo.setRefuseDes(refuseDes);
		appointmentMsgAo.setSmsCode(smsCode);
		appointmentMsgAo.setParkName(smtPark.getParkName());
		if (Objects.nonNull(smsCode)) {
			SmtVisitor smtVisitor = this.baseMapper.selectOne(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getSmsCode, smsCode).eq(SmtVisitor::getDelFlag, 0));
			appointmentMsgAo.setCodeUrl(codeUrl.replace("{id}", String.valueOf(smtVisitor.getId())));
		}

		Result<SendSmsVo> sendAppointmentSms = remoteSmsManageService.sendVisitorProxySms(appointmentMsgAo);
		log.info("访客审批代理人短信发送结果:{}" + sendAppointmentSms);
		return sendAppointmentSms;
	}


	/**
	 * 短信发送失败后，再次发送
	 *
	 * @param number
	 * @param tempCode
	 * @param tempNameError
	 * @param remark
	 */
	public void sendMessageError(String number, String tempCode, String tempNameError, String remark, String swichCode, Integer parkId) {
		SmtNoticeSwitch noticeSwitch = smtNoticeSwitchService.getOne(Wrappers.<SmtNoticeSwitch>query().lambda()
				.eq(SmtNoticeSwitch::getSwitchCode, swichCode)
				.eq(SmtNoticeSwitch::getIsOn, 1)
				.eq(SmtNoticeSwitch::getParkId, parkId));

		SendSmsErrorReqDTO sendSmsErrorAo = new SendSmsErrorReqDTO();
		sendSmsErrorAo.setPhoneNumber(number);
		sendSmsErrorAo.setTempCode(tempCode);
		sendSmsErrorAo.setTempNameError(tempNameError);
		sendSmsErrorAo.setRemark(remark);
		if (ObjectUtil.isNotNull(noticeSwitch)) {
			log.info("remoteSmsManageService:" + remoteSmsManageService);
			Result sendSmsError = remoteSmsManageService.sendSmsError(sendSmsErrorAo);
			log.info("remoteSmsManageService.sendSmsError Result={} :" + sendSmsError);
		}
	}

	/**
	 * 查询被访人是否存在
	 */
	@Override
	public SmtVisitor searchReceptionist(SmtVisitor smtVisitor) {
		SmtVisitor smtVisitors = new SmtVisitor();
		List<SmtStaff> smtStaffList = this.baseMapper.searchReceptionist(smtVisitor);
		if (CollectionUtils.isEmpty(smtStaffList)) {
			smtStaffList = this.baseMapper.searchReceptionistForTemp(smtVisitor);
		}
		if (smtStaffList.size() <= 0) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_RECEPTIONIST_ERROR);
		}
		smtVisitors.setReceptionistBadge(smtStaffList.get(0).getBadge());
		List<SmtVisitJcheLimit> jcheLimit = smtVisitJcheLimitService.listByJcheId(smtVisitor.getParkId(), smtStaffList.get(0).getJcheId(), ConfigBusinessEnum.VISITOR.getCode());
		if (CollectionUtils.isNotEmpty(jcheLimit)) {
			throw new TCEException("被访人没有预约权限");
		}
		return smtVisitors;
	}

	/**
	 * 查询被访人是否存在
	 */
	@Override
	public SmtVisitor searchReceptionistForApp(SmtVisitor smtVisitor) {
		SmtVisitor smtVisitors = new SmtVisitor();
		List<SmtStaff> smtStaffList = this.baseMapper.searchReceptionist(smtVisitor);
		if (CollectionUtils.isEmpty(smtStaffList)) {
			smtStaffList = this.baseMapper.searchReceptionistForTemp(smtVisitor);
		}
		if (CollectionUtils.isEmpty(smtStaffList)) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_RECEPTIONIST_ERROR);
		}
		SmtStaff smtStaff = smtStaffList.get(0);
		smtVisitors.setReceptionistBadge(smtStaff.getBadge());
		if (hfParkId.equals(smtVisitor.getParkId())) {
			if (StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode().equals(smtStaff.getStatus())
					|| StaffStatusEnum.UNKNOWN.getCode().equals(smtStaff.getStatus())) {
				throw new SmartException("外包人员或临时人员无法设为被访人");
			}
		}
		List<SmtVisitJcheLimit> jcheLimit = smtVisitJcheLimitService.listByJcheId(smtVisitor.getParkId(), smtStaff.getJcheId(), ConfigBusinessEnum.VISITOR.getCode());
		if (CollectionUtils.isNotEmpty(jcheLimit)) {
			throw new TCEException("被访人没有预约权限");
		}
		return smtVisitors;
	}

	/**
	 * 查询app的访客详情
	 */
	@Override
	public SearchAppVisitorDetailVO searchAppVisitorDetailById(Long id) {
		//根据访客id查询访客的和被访人的相关信息
		SmtVisitor SmtVisitor = new SmtVisitor();
		SmtVisitor.setId(id);
		SearchAppVisitorDetailVO searchAppVisitorDetailVO = this.baseMapper.selectAppVisitorById(SmtVisitor);
		searchAppVisitorDetailVO.setStatusDesc(VisitorStatusEnum.desc(searchAppVisitorDetailVO.getStatus()));
		searchAppVisitorDetailVO.setCauseDesc(VisitorEnum.desc(searchAppVisitorDetailVO.getCause()));
		searchAppVisitorDetailVO.setVisitorPhoto(searchAppVisitorDetailVO.getVisitorPhotoId());
		if (Objects.nonNull(searchAppVisitorDetailVO.getCarryThing())) {
			searchAppVisitorDetailVO.setCarryThingDesc(HfVisitCarryItemsEnum.desc(searchAppVisitorDetailVO.getCarryThing()));
		}
		//根据访客的id查询跟随人员的信息
		List<GetSmtFellowVisitorVO> fellowPersonList = getFellowPerson(searchAppVisitorDetailVO.getVisitorId());
		if (fellowPersonList.size() > 0) {
			for (GetSmtFellowVisitorVO vo : fellowPersonList) {
				if (ObjectUtil.isNotNull(vo.getFellowPhotoId())) {
					vo.setFellowPhoto(imageService.buildImageUrl(vo.getFellowPhotoId()));
				}
				if (StrUtil.isNotEmpty(vo.getCertPic())) {
					vo.setCertPic(imageService.buildImageUrl(vo.getCertPic()));
				}
				if (Objects.nonNull(vo.getCertType())) {
					vo.setCertTypeDesc(AdmittancePersonCertTypeEnum.desc(vo.getCertType()));
				}
			}
			searchAppVisitorDetailVO.setFellowVisitorList(fellowPersonList);
		}

		List<SmtVisitorProcessRecord> processList = new ArrayList<>();
		if (StrUtil.isNotEmpty(searchAppVisitorDetailVO.getProcessId())) {
			processList = this.getOaProcess(searchAppVisitorDetailVO.getProcessId());
		} else {
			processList = getVisitorProcess(searchAppVisitorDetailVO.getVisitorId());
		}
		if (CollectionUtils.isNotEmpty(processList)) {
			searchAppVisitorDetailVO.setProcessList(processList);
		}

		return searchAppVisitorDetailVO;
	}

	/**
	 * 查询访客得审批流程
	 *
	 * @param visitorId
	 * @return
	 */
	private List<SmtVisitorProcessRecord> getVisitorProcess(Long visitorId) {
		// TODO Auto-generated method stub
		List<SmtVisitorProcessRecord> list = smtVisitorProcessRecordService.list(Wrappers.<SmtVisitorProcessRecord>query().lambda().eq(SmtVisitorProcessRecord::getVisitorId, visitorId).orderByAsc(SmtVisitorProcessRecord::getRecordNode));
		return list;
	}

	/**
	 * 查询app的列表
	 */
	@Override
	public IPage<SearchAppSmtVisitorVO> searchAppVisitorPage(Page page, SearchAppVisitorDTO searchAppVisitorDTO) {
		SearchVisitorAppDTO searchVisitorAppDTO = new SearchVisitorAppDTO();
		//判断员工号是否为空值
		if (StringUtils.isEmpty(searchAppVisitorDTO.getStaffBadge())) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_STAFF_ID_ERROR);
		}
		//判断预约类型是否为空
		if (StringUtils.isEmpty(searchAppVisitorDTO.getVisitListType())) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_TYPE_ERROR);
		}
		//获取当前时间和往前推两天的时间
		searchVisitorAppDTO.setStartTime(DateUtils.formatDateTime(DateUtils.offsetDay(DateUtils.date(), -2)));
		searchVisitorAppDTO.setEndTime(DateUtils.now());
		//查询待我审核的访客信息
		if (searchAppVisitorDTO.getVisitListType().equals(SmtAppVisitorEnum.VISITOR_LIST_TYPE2.getType())) {
			searchVisitorAppDTO.setReceptionistBadge(searchAppVisitorDTO.getStaffBadge());
			searchVisitorAppDTO.setStatus(VisitorStatusEnum.CAUSE_4.getCode());
			IPage<SearchAppSmtVisitorVO> searchAppVisitorPage = this.baseMapper.searchAppVisitorPage(page, searchVisitorAppDTO);
			return getAppVisitor(searchAppVisitorPage);
		}
		//默认查我发起的预约
		searchVisitorAppDTO.setPromoterBadge(searchAppVisitorDTO.getStaffBadge());
		IPage<SearchAppSmtVisitorVO> searchAppVisitorPage = this.baseMapper.searchAppVisitorPage(page, searchVisitorAppDTO);
		return getAppVisitor(searchAppVisitorPage);
	}

	/**
	 * 给app的集合里面的访客加访客图片base64位图片
	 */
	public IPage<SearchAppSmtVisitorVO> getAppVisitor(IPage<SearchAppSmtVisitorVO> searchAppVisitorPage) {
		if (searchAppVisitorPage.getRecords().size() > 0) {
			for (int i = 0; i < searchAppVisitorPage.getRecords().size(); i++) {
				searchAppVisitorPage.getRecords().get(i).setStatusDesc(VisitorStatusEnum.desc(searchAppVisitorPage.getRecords().get(i).getStatus()));
				searchAppVisitorPage.getRecords().get(i).setCauseDesc(VisitorEnum.desc(searchAppVisitorPage.getRecords().get(i).getCause()));
				//根据图片的id获取图片的base64位
				String photo = searchAppVisitorPage.getRecords().get(i).getVisitorPhotoId();
				searchAppVisitorPage.getRecords().get(i).setVisitorPhoto(photo);
				searchAppVisitorPage.getRecords().get(i).setProcessNodeName("审批结束");
				List<SmtVisitorProcessRecord> list = smtVisitorProcessRecordService.list(Wrappers.<SmtVisitorProcessRecord>query().lambda().eq(SmtVisitorProcessRecord::getVisitorId, searchAppVisitorPage.getRecords().get(i).getVisitorId()).orderByAsc(SmtVisitorProcessRecord::getRecordNode));
				log.info("所有流程：" + list);
				if (list.size() > 0) {
					if (list.get(0).getStatus().equals(VisitorProcessEnum.WATING_2.getCode())) {
						searchAppVisitorPage.getRecords().get(i).setProcessNodeName(list.get(0).getStaffName());
					}
				}
				if (list.size() > 1) {
					if (list.get(0).getStatus().equals(VisitorProcessEnum.WATING_2.getCode())) {
						searchAppVisitorPage.getRecords().get(i).setProcessNodeName(list.get(0).getStaffName());
					} else if (list.get(1).getStatus().equals(VisitorProcessEnum.WATING_2.getCode())) {
						searchAppVisitorPage.getRecords().get(i).setProcessNodeName(list.get(1).getStaffName());
					}
				}

			}
		}

		return searchAppVisitorPage;
	}

	@Override
	public void addWechatFellowVisitor(AddWechatFellowVisitorDTO addFellowVisitorDTO) {
		//判断是否有随行人员
		if (CollectionUtils.isNotEmpty(addFellowVisitorDTO.getFollowList())) {
			for (int i = 0; i < addFellowVisitorDTO.getFollowList().size(); i++) {
				//正则判断随行人员
				addFellowVisitorDTO.getFollowList().get(i).setVisitorId(addFellowVisitorDTO.getVisitId());
				addFellowVisitorDTO.getFollowList().get(i).setFellowPhotoId(addFellowVisitorDTO.getFollowList().get(i).getFellowPhotoId());
				ExceptionTypeEnum fellowexceptionType = FellowVisitorCheck(addFellowVisitorDTO.getFollowList().get(i));
				if (!fellowexceptionType.equals(ExceptionTypeEnum.CHECK_SUCCESS)) {
					throw new TCEException(fellowexceptionType);
				}
				SmtFellowVisitor smtFellowVisitor = new SmtFellowVisitor();
				smtFellowVisitor.setFellowName(addFellowVisitorDTO.getFollowList().get(i).getFellowName());
				smtFellowVisitor.setFellowPhotoId(addFellowVisitorDTO.getFollowList().get(i).getFellowPhotoId());
				smtFellowVisitor.setVisitorId(addFellowVisitorDTO.getVisitId());
				//添加随行人员
				smtFellowVisitor.insert();
			}
		}
	}

	@Override
	public void addFellowVisitor(AddFellowVisitorDTO addFellowVisitorDTO) {
		//判断是否有随行人员
		if (CollectionUtils.isNotEmpty(addFellowVisitorDTO.getFollowList())) {
			//查询访客信息
			SmtVisitor visitor = this.getById(addFellowVisitorDTO.getVisitId());
			//查询访客人员设备权限
			List<SmtDeviceAuthorityRelation> visitorDeviceList = deviceAuthorityRelationService.getRelationAuth(visitor.getParkId(),
					BusinessAuthorityEnum.VISITOR_FACE.getCode(), DeviceAuthorityEnum.VISITOR);
			for (int i = 0; i < addFellowVisitorDTO.getFollowList().size(); i++) {
				//根据访客的图片调用获取图片的接口
				if (ObjectUtil.isNull(addFellowVisitorDTO.getFollowList().get(i).getFellowPhoto())) {
					throw new TCEException(ExceptionTypeEnum.VISITOR_PHOTO_NULL);
				}
				//正则判断随行人员
				addFellowVisitorDTO.getFollowList().get(i).setVisitorId(addFellowVisitorDTO.getVisitId());
				addFellowVisitorDTO.getFollowList().get(i).setFellowPhotoId(addFellowVisitorDTO.getFollowList().get(i).getFellowPhoto());
				ExceptionTypeEnum fellowexceptionType = FellowVisitorCheck(addFellowVisitorDTO.getFollowList().get(i));
				if (!fellowexceptionType.equals(ExceptionTypeEnum.CHECK_SUCCESS)) {
					throw new TCEException(fellowexceptionType);
				}
				SmtFellowVisitor smtFellowVisitor = new SmtFellowVisitor();
				smtFellowVisitor.setFellowName(addFellowVisitorDTO.getFollowList().get(i).getFellowName());
				smtFellowVisitor.setFellowPhotoId(addFellowVisitorDTO.getFollowList().get(i).getFellowPhoto());
				smtFellowVisitor.setVisitorId(addFellowVisitorDTO.getVisitId());
				//添加随行人员
				boolean insert = smtFellowVisitor.insert();
				//给随行人员下发闸机
				if (insert) {
					GetSmtFellowVisitorVO fellowVisitorVO = new GetSmtFellowVisitorVO();
					fellowVisitorVO.setId(smtFellowVisitor.getId());
					fellowVisitorVO.setFellowName(smtFellowVisitor.getFellowName());
					fellowVisitorVO.setFellowPhotoId(smtFellowVisitor.getFellowPhotoId());
					addCard(visitor, fellowVisitorVO, visitorDeviceList);
				}
			}
		}
	}

	/**
	 * 查询未审核的接口
	 */
	@Override
	public Integer searchAppVisitorCount(SearchAppVisitorCountDTO searchAppVisitorCountDTO) {
		//判断员工号是否为空值
		if (StringUtils.isEmpty(searchAppVisitorCountDTO.getStaffBadge())) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_STAFF_ID_ERROR);
		}
		//查询未处理的访客
		int selectCount = this.count(Wrappers.<SmtVisitor>query().lambda()
				.eq(SmtVisitor::getStatus, SmtAppVisitorEnum.UNTREATED_STATUS.getType())
				.eq(SmtVisitor::getReceptionistBadge, searchAppVisitorCountDTO.getStaffBadge())
				.ge(SmtVisitor::getCreateTime, DateUtils.offsetDay(DateUtils.date(), -2))
				.le(SmtVisitor::getCreateTime, DateUtils.date())
				.gt(SmtVisitor::getEndTime, DateUtils.date())

		);
		return selectCount;
	}

	//访客当天进门总条数
	@Override
	public SnapPersonCountVO searchComeInToday() {
		SnapPersonCountVO snapPersonCountVO = new SnapPersonCountVO();
		Integer selectCount = smtSnapPersonMapper.selectCount(Wrappers.<SmtSnapPerson>query().lambda()
				.eq(SmtSnapPerson::getEventType, EventTypeEnum.EVENT_TYPE_1.getCode())
				.eq(SmtSnapPerson::getPersonType, SmtVisitorEnum.VISITOR_TYPE.getType())
				.ge(SmtSnapPerson::getCreateTime, DateUtil.beginOfDay(DateUtils.date()))
				.le(SmtSnapPerson::getCreateTime, DateUtil.endOfDay(DateUtils.date()))
		);
		snapPersonCountVO.setCount(selectCount);
		return snapPersonCountVO;
	}

	//访客当天出门总条数
	@Override
	public SnapPersonCountVO searchVisitorOutToday() {
		SnapPersonCountVO snapPersonCountVO = new SnapPersonCountVO();
		Integer selectCount = smtSnapPersonMapper.selectCount(Wrappers.<SmtSnapPerson>query().lambda()
				.eq(SmtSnapPerson::getEventType, EventTypeEnum.EVENT_TYPE_2.getCode())
				.eq(SmtSnapPerson::getPersonType, SmtVisitorEnum.VISITOR_TYPE.getType())
				.ge(SmtSnapPerson::getCreateTime, DateUtil.beginOfDay(DateUtils.date()))
				.le(SmtSnapPerson::getCreateTime, DateUtil.endOfDay(DateUtils.date()))
		);
		snapPersonCountVO.setCount(selectCount);
		return snapPersonCountVO;
	}

	//访客当天抓拍数据
	public List<SmtSnapPerson> getSnapVisitorToday() {
		List<SmtSnapPerson> selectList = smtSnapPersonMapper.selectList(Wrappers.<SmtSnapPerson>query().lambda()
				.eq(SmtSnapPerson::getPersonType, SmtVisitorEnum.VISITOR_TYPE.getType())
				.ge(SmtSnapPerson::getCreateTime, DateUtil.beginOfDay(DateUtils.date()))
				.le(SmtSnapPerson::getCreateTime, DateUtil.endOfDay(DateUtils.date()))
		);
		return selectList;
	}

	//访客当天抓拍数据
	public List<SmtSnapPerson> getSnapVisitorTodays(Integer parkId) {
		//List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		List<SmtSnapPerson> selectList = smtSnapPersonMapper.selectList(Wrappers.<SmtSnapPerson>query().lambda()
				.eq(SmtSnapPerson::getEventType, EventTypeEnum.EVENT_TYPE_1.getCode())
				.eq(SmtSnapPerson::getPersonType, SmtVisitorEnum.VISITOR_TYPE.getType())
				.ge(SmtSnapPerson::getCreateTime, DateUtil.beginOfDay(DateUtils.date()))
				.le(SmtSnapPerson::getCreateTime, DateUtil.endOfDay(DateUtils.date()))
				.eq(SmtSnapPerson::getParkId, parkId)
		);
		return selectList;
	}

	/**
	 * 访客分析数据查询
	 */
	@Override
	public List<SearchVisitorAnalysisVO> searchVisitorAnalysisToday(Integer parkId) {
		List<SearchVisitorAnalysisVO> list = new ArrayList<SearchVisitorAnalysisVO>();
		//查询当天的抓拍访客数据
		List<SmtSnapPerson> selectList = getSnapVisitorTodays(parkId);
		SearchVisitorAnalysisVO searchVisitorAnalysisVO1 = new SearchVisitorAnalysisVO();
		SearchVisitorAnalysisVO searchVisitorAnalysisVO2 = new SearchVisitorAnalysisVO();
		SearchVisitorAnalysisVO searchVisitorAnalysisVO3 = new SearchVisitorAnalysisVO();
		SearchVisitorAnalysisVO searchVisitorAnalysisVO4 = new SearchVisitorAnalysisVO();
		SearchVisitorAnalysisVO searchVisitorAnalysisVO5 = new SearchVisitorAnalysisVO();

		Integer cause1 = 0;
		Integer cause2 = 0;
		Integer cause3 = 0;
		Integer cause4 = 0;
		Integer cause5 = 0;
		if (CollectionUtils.isNotEmpty(selectList)) {
			for (int i = 0; i < selectList.size(); i++) {
				SmtVisitor selectOne = new SmtVisitor();
				if (!Objects.isNull(selectList.get(i))) {
					//判断是为访客的id
					if (getVisitorByPersonId(selectList.get(i).getPersonId())) {
						selectOne = this.baseMapper.selectOne(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getId, selectList.get(i).getPersonId()));
					}
					//判断是否是随行人员id
					if (getVisitorFellowByPersonId(selectList.get(i).getPersonId())) {
						SmtFellowVisitor selectFellowOne = smtFellowVisitorService.getOne(Wrappers.<SmtFellowVisitor>query().lambda().eq(SmtFellowVisitor::getId, selectList.get(i).getPersonId()));
						selectOne = this.baseMapper.selectOne(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getId, selectFellowOne.getVisitorId()));
					}
				}
				if (!Objects.isNull(selectOne)) {
					//判断来访事由
					if (selectOne.getCause().equals(VisitorEnum.CAUSE_1.getCode())) {
						cause1++;
					} else if (selectOne.getCause().equals(VisitorEnum.CAUSE_2.getCode())) {
						cause2++;
					} else if (selectOne.getCause().equals(VisitorEnum.CAUSE_3.getCode())) {
						cause3++;
					} else if (selectOne.getCause().equals(VisitorEnum.CAUSE_4.getCode())) {
						cause4++;
					} else if (selectOne.getCause().equals(VisitorEnum.CAUSE_5.getCode())) {
						cause5++;
					}
				}
			}
		}
		searchVisitorAnalysisVO1.setCauseCount(cause1);
		searchVisitorAnalysisVO1.setCauseDesc(VisitorEnum.CAUSE_1.getDesc());
		list.add(searchVisitorAnalysisVO1);
		searchVisitorAnalysisVO2.setCauseCount(cause2);
		searchVisitorAnalysisVO2.setCauseDesc(VisitorEnum.CAUSE_2.getDesc());
		list.add(searchVisitorAnalysisVO2);
		searchVisitorAnalysisVO3.setCauseCount(cause3);
		searchVisitorAnalysisVO3.setCauseDesc(VisitorEnum.CAUSE_3.getDesc());
		list.add(searchVisitorAnalysisVO3);
		searchVisitorAnalysisVO4.setCauseCount(cause4);
		searchVisitorAnalysisVO4.setCauseDesc(VisitorEnum.CAUSE_4.getDesc());
		list.add(searchVisitorAnalysisVO4);
		searchVisitorAnalysisVO5.setCauseCount(cause5);
		searchVisitorAnalysisVO5.setCauseDesc(VisitorEnum.CAUSE_5.getDesc());
		list.add(searchVisitorAnalysisVO5);
		return list;
	}

	/**
	 * 访客当日设备进出数据分析
	 */
	public void getVisitorDeviceToday(List<SearchVisitorDeviceVO> listVisitor, SmtDevice smtDevice) {
		if (!Objects.isNull(smtDevice)) {
			SearchVisitorDeviceVO searchVisitorDeviceVO = new SearchVisitorDeviceVO();
			searchVisitorDeviceVO.setDeviceName(smtDevice.getDeviceName());
			searchVisitorDeviceVO.setDeviceId(smtDevice.getId());
			searchVisitorDeviceVO.setEventType(smtDevice.getEventType());
			searchVisitorDeviceVO.setEventTypeDesc(EventTypeEnum.desc(smtDevice.getEventType()));
			searchVisitorDeviceVO.setEventCount(0);
			listVisitor.add(searchVisitorDeviceVO);
		}
	}

	/**
	 * 访客当日设备进出数据分析
	 */
	@Override
	public List<SearchVisitorDeviceVO> searchVisitorDeviceToday() {
		List<SearchVisitorDeviceVO> listVisitor = new ArrayList<SearchVisitorDeviceVO>();
		//获取访客闸机的数据
		List<SmtDevice> list = smtDeviceDevice.selectDeviceByAuthId(DeviceAuthorityEnum.VISITOR.getCode());
		if (CollectionUtils.isNotEmpty(list)) {
			list.forEach(d -> {
				getVisitorDeviceToday(
						listVisitor,
						d
				);
			});
		}
		//获取当天抓拍的访客数据
		List<SmtSnapPerson> selectList = getSnapVisitorToday();
		//判断为空
		if (CollectionUtils.isNotEmpty(selectList)) {
			//循环抓拍数据
			for (int i = 0; i < selectList.size(); i++) {
				if (!Objects.isNull(selectList.get(i))) {
					//循环设备数据
					for (int j = 0; j < listVisitor.size(); j++) {
						//判断抓拍的设备id和设备的id是否相同
						if (selectList.get(i).getDeviceId().equals(listVisitor.get(j).getDeviceId())) {
							Integer count = listVisitor.get(j).getEventCount() + 1;
							listVisitor.get(j).setEventCount(count);
						}
					}
				}
			}
		}
		return listVisitor;
	}


	//根据设备查询当天最新抓拍人员抓拍数据
	@Override
	public void getSnapPersonLasted(SearchVisitorDeviceAnalysisVO dd, String deviceId) {
		List<SmtSnapPerson> selectList = smtSnapPersonMapper.selectList(Wrappers.<SmtSnapPerson>query().lambda()
						.eq(SmtSnapPerson::getDeviceId, deviceId)
//				.ge(SmtSnapPerson::getCreateTime, DateUtil.beginOfDay(DateUtils.date()))
//				.le(SmtSnapPerson::getCreateTime, DateUtil.endOfDay(DateUtils.date()))
						.orderByDesc(SmtSnapPerson::getSnapTime)
		);

		DeviceVO device = smtDeviceDevice.getDeviceById(deviceId);
		if (Objects.nonNull(device)) {
			dd.setDeviceName(device.getDeviceName());
		}

		if (CollectionUtils.isNotEmpty(selectList)) {
			SmtSnapPerson snapPerson = selectList.get(0);
			if (!Objects.isNull(snapPerson)) {
				dd.setPersonName(snapPerson.getPersonName());
				dd.setSnapTime(snapPerson.getSnapTime());
				dd.setSnapPhotoUrl(imageService.buildImageUrl(snapPerson.getSnapPhotoId()));
				dd.setEventType(snapPerson.getEventType());
				dd.setEventTypeDesc(EventTypeEnum.desc(snapPerson.getEventType()));

				//身份判断
				if (!Objects.isNull(snapPerson.getPersonType())) {
					dd.setPersonType(snapPerson.getPersonType());
					dd.setPersonTypeDesc(SnapPersonTypeEnum.desc(snapPerson.getPersonType()));
				} else {
					dd.setPersonTypeDesc("");
				}
				//判断是否为访客数据
				if (getVisitorByPersonId(snapPerson.getPersonId())) {
					SmtVisitor selectVisitorById = selectVisitorById(snapPerson.getPersonId());
					if (!Objects.isNull(selectVisitorById)) {
						//获取公司名称，人员图片url
						dd.setCompany(selectVisitorById.getCompany());
						dd.setPersonUrl(imageService.buildImageUrl(selectVisitorById.getVisitorPhotoId()));
					}
				} else if (getVisitorFellowByPersonId(snapPerson.getPersonId())) {
					//判断是否为随行人员的id,如果为随行人员的id
					SmtFellowVisitor selectOne = smtFellowVisitorService.getOne(Wrappers.<SmtFellowVisitor>query().lambda().eq(SmtFellowVisitor::getId, snapPerson.getPersonId()));
					//获取人员图片url
					SmtVisitor selectVisitorById = selectVisitorById(selectOne.getVisitorId());
					dd.setPersonUrl(imageService.buildImageUrl(selectOne.getFellowPhotoId()));
					//获取公司名称
					if (!Objects.isNull(selectVisitorById)) {
						dd.setCompany(selectVisitorById.getCompany());
					}
				} else if (smtAdmittanceFellowService.isExistFellow(snapPerson.getPersonId())) {
					SmtAdmittanceFellow fellow = smtAdmittanceFellowService.getById(snapPerson.getPersonId());
					dd.setPersonUrl(imageService.buildImageUrl(fellow.getFellowPhotoId()));
					SmtAdmittanceApply apply = smtAdmittanceApplyService.getById(fellow.getVisitorId());
					dd.setCompany(apply.getCompany());
				} else {
					//判断是否为员工
					SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getId, snapPerson.getPersonId()));
					if (!Objects.isNull(selectOne)) {
						dd.setPersonUrl(imageService.buildImageUrl(selectOne.getFacePicId()));
						dd.setCompany(selectOne.getCompName());
					}
				}
			}
		}
	}

	/**
	 * 根据id查询访客
	 *
	 * @param personId personId
	 * @return
	 */
	private SmtVisitor selectVisitorById(Long personId) {
		SmtVisitor selectOne = this.baseMapper.selectOne(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getId, personId));
		return selectOne;
	}


	/**
	 * 访客当日设备最新抓拍数据分析
	 */
	public void getVisitorDeviceAnalysisToday(List<SearchVisitorDeviceAnalysisVO> list, SmtDevice smtDevice) {
		if (!Objects.isNull(smtDevice)) {
			SearchVisitorDeviceAnalysisVO searchVisitorDeviceAnalysisVO = new SearchVisitorDeviceAnalysisVO();
			searchVisitorDeviceAnalysisVO.setDeviceName(smtDevice.getDeviceName());
			searchVisitorDeviceAnalysisVO.setDeviceId(smtDevice.getId());
			list.add(searchVisitorDeviceAnalysisVO);
		}
	}

	//访客抓拍最新设备信息
	public List<SearchVisitorDeviceAnalysisVO> searchVisitorDeviceAnalysisToday() {
		List<SearchVisitorDeviceAnalysisVO> devicelist = new ArrayList<SearchVisitorDeviceAnalysisVO>();
		//获取访客闸机的数据
		List<SmtDevice> list = smtDeviceDevice.selectDeviceByAuthId(DeviceAuthorityEnum.VISITOR.getCode());
		if (CollectionUtils.isNotEmpty(list)) {
			list.forEach(d -> {
				getVisitorDeviceAnalysisToday(
						devicelist,
						d
				);
			});
		}
		//判断设备数据是否为空
		if (CollectionUtils.isNotEmpty(devicelist)) {
			//循环查该设备最新的抓拍数据
			devicelist.forEach(dd -> {
				getSnapPersonLasted(
						dd,
						dd.getDeviceId()
				);
			});
		}
		return devicelist;
	}


	/**
	 * 根据指定大小压缩图片
	 *
	 * @param imageBytes  源图片字节数组
	 * @param desFileSize 指定图片大小，单位kb
	 * @return 压缩质量后的图片字节数组
	 */
	public static byte[] compressPicForScale(byte[] imageBytes, long desFileSize) {
		if (imageBytes == null || imageBytes.length <= 0 || imageBytes.length < desFileSize * 1024) {
			return imageBytes;
		}
		long srcSize = imageBytes.length;
		double accuracy = getAccuracy(srcSize / 1024);
		try {
			while (imageBytes.length > desFileSize * 1024) {
				ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream(imageBytes.length);
				Thumbnails.of(inputStream)
						.scale(accuracy)
						.outputQuality(accuracy)
						.toOutputStream(outputStream);
				imageBytes = outputStream.toByteArray();
			}
			log.info("【图片压缩】 图片原大小={}kb | 压缩后大小={}kb",
					srcSize / 1024, imageBytes.length / 1024);
		} catch (Exception e) {
			log.error("【图片压缩】msg=图片压缩失败!", e);
		}
		return imageBytes;
	}

	/**
	 * 自动调节精度(经验数值)
	 *
	 * @param size 源图片大小
	 * @return 图片压缩质量比
	 */
	private static double getAccuracy(long size) {
		double accuracy;
		accuracy = 0.85;
		return accuracy;
	}

	@Override
	public Boolean checkBlackVisitor(SmtVisitor smtVisitor) {
		// TODO Auto-generated method stub
		//判断此人员是不是 加入黑名单  false-黑名单   true-不是黑名单
		SmtBlackVisitor smtBlackVisitor = smtBlackVisitorService.getOne(Wrappers.<SmtBlackVisitor>query().lambda().eq(SmtBlackVisitor::getCardNo, smtVisitor.getCertNo()));
		log.info("smtBlackVisitor:" + smtBlackVisitor);
		if (Objects.nonNull(smtBlackVisitor)) {
			return false;
		}

		Result<List<EvwEmphrYsBlackRespDTO>> black = remoteEvwEmphrYsService.getBlackInfo(smtVisitor.getCertNo(), SecurityConstants.FROM_IN);
		if (black.isSuccess()) {
			if (black.getData() != null) {
				List<EvwEmphrYsBlackRespDTO> data = black.getData();
                return data.size() <= 0;
			}
		}

		return true;
	}

	@Override
	public Result getVisitorRefuseType() {
		// TODO Auto-generated method stub
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.VISITOR_REFUSE, SecurityConstants.FROM_IN);
		List<SysDict> data = findByType.getData();
		List<VisitorRefuse> refuseList = new ArrayList<>();
		for (SysDict sysDict : data) {
			VisitorRefuse refuse = new VisitorRefuse();
			refuse.setRefuseCode(Integer.parseInt(sysDict.getValue()));
			refuse.setRefuseDes(sysDict.getLabel());
			refuseList.add(refuse);
		}
		return new Result<>(refuseList);
	}

	@Override
	public Boolean checkBlackVehicle(SmtVisitor smtVisitor) {
		// TODO Auto-generated method stub
		//判断此车牌号是不是 加入黑名单  false-黑名单   true-不是黑名单

		if (!StringUtils.isEmpty(smtVisitor.getVehiclePlate())) {
			SmtVehicleBlack smtVehicleBlack = smtVehicleBlackService.getOne(Wrappers.<SmtVehicleBlack>query().lambda()
					.eq(SmtVehicleBlack::getVehiclePlate, smtVisitor.getVehiclePlate()).eq(SmtVehicleBlack::getParkId, smtVisitor.getParkId()));
			if (Objects.nonNull(smtVehicleBlack)) {
				throw new TCEException("该车牌号已加入黑名单车辆，不能预约");
			}
		}
		return true;
	}

	@Override
	public IPage<VisitorListRespDTO> getVisitRecord(Page page, String visitorPhone) {
		IPage iPage = this.pageMaps(page, new LambdaQueryWrapper<SmtVisitor>()
				.eq(SmtVisitor::getVisitorPhone, visitorPhone)
				.orderByDesc(SmtVisitor::getCreateTime));
		List<VisitorListRespDTO> visitorListRespDTOList = new ArrayList<>();

		for (Object obj : iPage.getRecords()) {
			SmtVisitor smtVisitor = (SmtVisitor) obj;
			VisitorListRespDTO visitorListRespDTO = new VisitorListRespDTO();
			BeanUtils.copyProperties(smtVisitor, visitorListRespDTO);
			visitorListRespDTO.setCauseDes(VisitorEnum.desc(smtVisitor.getCause()));
			visitorListRespDTO.setVisitorImg(imageService.buildImageUrl(smtVisitor.getVisitorPhotoId()));

			visitorListRespDTOList.add(visitorListRespDTO);
		}
		return iPage;
	}

	@Override
	public SearchVisitorDetailRespDTO searchVisitorByCode(String code) {
		SmtVisitor smtVisitor = this.baseMapper.selectOne(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getSmsCode, code).eq(SmtVisitor::getDelFlag, 0));
		if (Objects.isNull(smtVisitor)) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_CODE_ERROR);
		}
		SearchVisitorDetail searchVisitorDetail = this.baseMapper.selectVisitorById(smtVisitor);
		if (Objects.nonNull(smtVisitor.getPromoterBadge())) {
			searchVisitorDetail.setIsVip(1);
		} else {
			searchVisitorDetail.setIsVip(0);
		}
		searchVisitorDetail.setCauseDesc(VisitorEnum.desc(searchVisitorDetail.getCause()));

		SmtPark park = smtParkService.getById(searchVisitorDetail.getParkId());
		searchVisitorDetail.setParkName(park.getParkName());
		SearchVisitorDetailRespDTO respDTO = new SearchVisitorDetailRespDTO();
		respDTO.setCauseDesc(searchVisitorDetail.getCauseDesc());
		respDTO.setCompany(searchVisitorDetail.getCompany());
		respDTO.setEndTime(searchVisitorDetail.getEndTime());
		respDTO.setIsVip(searchVisitorDetail.getIsVip());
		respDTO.setTripCode(searchVisitorDetail.getTripCode());
		respDTO.setHealthcode(searchVisitorDetail.getHealthcode());
		respDTO.setParkName(searchVisitorDetail.getParkName());
		respDTO.setReceptionistName(searchVisitorDetail.getReceptionistName());
		respDTO.setStartTime(searchVisitorDetail.getStartTime());
		respDTO.setVisitorName(searchVisitorDetail.getVisitorName());
		respDTO.setVisitorPhone(searchVisitorDetail.getVisitorPhone());
		respDTO.setVisitorPhoto(smtVisitor.getVisitorPhotoId());
		respDTO.setDelFlag(smtVisitor.getDelFlag());
		respDTO.setId(smtVisitor.getId());
		respDTO.setRemotePath(remotePath);
		List<GetSmtFellowVisitorVO> fellowVisitorList = smtFellowVisitorService.selectListByVisitorId(smtVisitor);
		List<GetSmtFellowVisitorRespDTO> smtFellowVisitorRespDTOS = new ArrayList<>();
		for (GetSmtFellowVisitorVO getSmtFellowVisitorVO : fellowVisitorList) {
			GetSmtFellowVisitorRespDTO getSmtFellowVisitorRespDTO = new GetSmtFellowVisitorRespDTO();
			getSmtFellowVisitorRespDTO.setFellowName(getSmtFellowVisitorVO.getFellowName());
			getSmtFellowVisitorRespDTO.setFellowPhotoId(getSmtFellowVisitorVO.getFellowPhotoId());
			getSmtFellowVisitorRespDTO.setCertNo(getSmtFellowVisitorVO.getCertNo());
			getSmtFellowVisitorRespDTO.setCertTypeDesc(AdmittancePersonCertTypeEnum.desc(getSmtFellowVisitorVO.getCertType()));
			getSmtFellowVisitorRespDTO.setCertPic(getSmtFellowVisitorVO.getCertPic());
			smtFellowVisitorRespDTOS.add(getSmtFellowVisitorRespDTO);
		}
		respDTO.setFellowVisitorList(smtFellowVisitorRespDTOS);
		respDTO.setQrCode(createQRCode(smtVisitor.getSmsCode()));
		respDTO.setSmsCode(smtVisitor.getSmsCode());
		return respDTO;
	}

	@Override
	public SearchVisitorDetailRespDTO searchVisitorById(Long id) {
		SmtVisitor smtVisitor = this.baseMapper.selectOne(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getId, id));
		SearchVisitorDetail searchVisitorDetail = this.baseMapper.selectVisitorById(smtVisitor);
		searchVisitorDetail.setCauseDesc(VisitorEnum.desc(searchVisitorDetail.getCause()));
		if (ObjectUtil.isNotNull(searchVisitorDetail.getVisitorPhotoId())) {
			searchVisitorDetail.setVisitorPhoto(imageService.buildImageUrl(searchVisitorDetail.getVisitorPhotoId()));
		}

		SmtPark park = smtParkService.getById(searchVisitorDetail.getParkId());
		SearchVisitorDetailRespDTO respDTO = new SearchVisitorDetailRespDTO();

		if (System.currentTimeMillis() - searchVisitorDetail.getEndTime().getTime() < 0) {
			respDTO.setCauseDesc(searchVisitorDetail.getCauseDesc());
			respDTO.setCompany(searchVisitorDetail.getCompany());
			respDTO.setParkName(park.getParkName());
			respDTO.setReceptionistName(replaceStar(searchVisitorDetail.getReceptionistName()));
			respDTO.setReceptionistPhone(searchVisitorDetail.getReceptionistPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
			respDTO.setEndTime(searchVisitorDetail.getEndTime());
			respDTO.setStartTime(searchVisitorDetail.getStartTime());
			respDTO.setVisitorName(replaceStar(searchVisitorDetail.getVisitorName()));
			respDTO.setVisitorPhone(searchVisitorDetail.getVisitorPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
			respDTO.setDelFlag(smtVisitor.getDelFlag());
			respDTO.setQrCode(createQRCode(smtVisitor.getSmsCode()));
			respDTO.setSmsCode(smtVisitor.getSmsCode());
		} else {
			respDTO.setDelFlag(2);
		}
		return respDTO;
	}

	private String replaceStar(String userName) {
		int nameLength = userName.length();
		if (nameLength == 1) {
			return userName;
		} else {
			return userName.charAt(0) + StrUtil.repeat("*", nameLength - 1);
		}
	}

	private String createQRCode(String content) {
		int QR_CODE_WIDTH = 1200;
		int QR_CODE_HEIGHT = 1200;

		Map<EncodeHintType, Object> HINTS = new HashMap<>(3);
		HINTS.put(EncodeHintType.CHARACTER_SET, "utf-8");
		HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
		HINTS.put(EncodeHintType.MARGIN, 2);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		//生成二维码
		try {
			BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT, HINTS);
			MatrixToImageWriter.writeToStream(bitMatrix, "png", out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Base64Utils.encodeToString(out.toByteArray());
	}

	@Override
	public Boolean delSmsCode(Long id) {
		return this.baseMapper.updateSmsCode(id);
	}

	@Override
	public Boolean smbPutPhoto(Long id) {
		Boolean isSuccess = true;
		String url = remoteUrl.replace("{ip}", WebUtils.getIP());
		log.info("上传客户端共享文件夹的IP: " + url);
		SmtVisitor smtVisitor = this.baseMapper.selectOne(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getId, id));
		List<GetSmtFellowVisitorVO> fellowVisitorList = smtFellowVisitorService.selectListByVisitorId(smtVisitor);
		byte[] visitorBytes = smtImageService.getImageBinaryByCode(smtVisitor.getVisitorPhotoId());
		InputStream inputStream = null;
		OutputStream outputStream = null;
		byte[] fellowVisitorBytes = null;
		try {
			if (Objects.nonNull(visitorBytes) && visitorBytes.length > 0) {
				inputStream = new ByteArrayInputStream(visitorBytes);
				SmbFile smbfile = new SmbFile(url + "/" + smtVisitor.getVisitorPhotoId() + ".jpg");
				smbfile.setConnectTimeout(5 * 1000); //5秒超时
				smbfile.connect();
				outputStream = new BufferedOutputStream(new SmbFileOutputStream(smbfile));
				byte[] buffer = new byte[4096];
				int len;
				while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
					outputStream.write(buffer, 0, len);
				}
				outputStream.flush();

				for (GetSmtFellowVisitorVO fellowVisitorVO : fellowVisitorList) {
					fellowVisitorBytes = smtImageService.getImageBinaryByCode(fellowVisitorVO.getFellowPhotoId());
					if (Objects.nonNull(fellowVisitorBytes) && fellowVisitorBytes.length > 0) {
						inputStream = new ByteArrayInputStream(visitorBytes);
						smbfile = new SmbFile(url + "/" + fellowVisitorVO.getFellowPhotoId() + ".jpg");
						smbfile.connect();
						outputStream = new BufferedOutputStream(new SmbFileOutputStream(smbfile));
						while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
							outputStream.write(buffer, 0, len);
						}
						outputStream.flush();
					} else {
						log.info("随行人员照片为空, {}", fellowVisitorVO.getId());
					}
					fellowVisitorBytes = null;
				}
			} else {
				log.info("访客照片为空, {}", id);
			}
		} catch (Exception e) {
			log.error("照片上传共享文件夹异常", e);
			throw new TCEException("照片上传共享文件夹错误");
		} finally {
			try {
				if (Objects.nonNull(outputStream)) {
					outputStream.close();
				}
				if (Objects.nonNull(inputStream)) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return isSuccess;
	}

	@Override
	public Boolean repeatVisitorDeviceAuth(Long id) {
		//查询访客记录
		SmtVisitor visitor = this.getById(id);
		if (null == visitor) {
			throw new TCEException("访客预约记录不存在");
		}

		if (visitor.getEndTime().getTime() < new Date().getTime()) {
			throw new TCEException("预约时间已到期");
		}

		//查询当前的访客权限
		Integer parkId = visitor.getParkId();
		//查询访客人员设备权限
		List<SmtDeviceAuthorityRelation> visitorDeviceList = deviceAuthorityRelationService.getRelationAuth(parkId,
				BusinessAuthorityEnum.VISITOR_FACE.getCode(), DeviceAuthorityEnum.VISITOR);
		for (SmtDeviceAuthorityRelation relation : visitorDeviceList) {
			//检查该设备是否已经下发成功过或者正在下发
			Boolean recoed = checkDeviceTaskRecoed(id.toString(), relation.getDeviceId());
			if (!recoed) {
				//生成新的下发任务
				DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
				deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
				deviceTaskVO.setServiceType(DeviceTaskConstants.CARD_VISITOR);
				deviceTaskVO.setCardNo(visitor.getId().toString());
				deviceTaskVO.setDeviceCode(relation.getDeviceId());
				deviceTaskVO.setGeneral(visitor.getVisitorName());
				deviceTaskVO.setCardType(SmtVisitorEnum.CARD_TYPE_7.getType());
				deviceTaskVO.setImageId(visitor.getVisitorPhotoId());
				deviceTaskVO.setDeviceType(DeviceTaskConstants.CARD);
				deviceTaskVO.setStartTime(new Date().getTime() / 1000);                //当前时间
				deviceTaskVO.setOverTime(visitor.getEndTime().getTime() / 1000);
				saveRequiredDeviceTask(deviceTaskVO);
			}
		}

		if (!StringUtils.isEmpty(visitor.getVehiclePlate())) {
			//存在车牌
			//查询访客车辆的的设备权限
			List<SmtDeviceAuthorityRelation> vehicleDeviceList = deviceAuthorityRelationService.getRelationAuth(parkId,
					BusinessAuthorityEnum.VISITOR_VEHICLE.getCode(), DeviceAuthorityEnum.VISITOR_VEHICLE);
			for (SmtDeviceAuthorityRelation relation : vehicleDeviceList) {
				Boolean recoed = checkDeviceTaskRecoed(id.toString(), relation.getDeviceId());
				if (!recoed) {
					//生成新的下发任务
					DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
					deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
					deviceTaskVO.setServiceType(DeviceTaskConstants.CAR_VISITOR);
					deviceTaskVO.setCardNo(visitor.getId().toString());
					deviceTaskVO.setDeviceCode(relation.getDeviceId());
					deviceTaskVO.setGeneral(visitor.getVehiclePlate());
					deviceTaskVO.setCardType(SmtVisitorEnum.CARD_TYPE_7.getType());
					deviceTaskVO.setImageId(visitor.getVisitorPhotoId());
					deviceTaskVO.setDeviceType(DeviceTaskConstants.CAR);
					deviceTaskVO.setStartTime(new Date().getTime() / 1000);                //当前时间
					deviceTaskVO.setOverTime(visitor.getEndTime().getTime() / 1000);
					deviceTaskVO.setApplyBadge(visitor.getCertNo());
					saveRequiredDeviceTask(deviceTaskVO);
				}
			}
		}
		return true;
	}

	/**
	 * 检查是否下发成功或正在下发
	 *
	 * @param cardNo
	 * @param deviceId
	 * @return
	 */
	private Boolean checkDeviceTaskRecoed(String cardNo, String deviceId) {
		//检查该设备是否已经下发成功过或者正在下发
		int count = smtTaskDownRecordService.count(new LambdaQueryWrapper<SmtTaskDownRecord>()
				.eq(SmtTaskDownRecord::getCardNo, cardNo)
				.eq(SmtTaskDownRecord::getDeviceCode, deviceId)
		);
		if (count == 0) {
			//不存在下发成功的记录 再查询正在下发中的记录
			int count1 = smtDeviceTaskService.count(new LambdaQueryWrapper<SmtDeviceTask>()
					.eq(SmtDeviceTask::getCardNo, cardNo)
					.eq(SmtDeviceTask::getDeviceCode, cardNo)
					.eq(SmtDeviceTask::getAction, DeviceTaskActionEnum.DOWN.getCode())
					.eq(SmtDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
			);
            //存在正在下发中的任务
            return count1 > 0;
		} else {
			//已存在下发成功记录
			return true;
		}
    }

	/**
	 * 访客预约添加
	 *
	 * @throws ParseException
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public SmtVisitor saveHfVisitor(SaveSmtVisitor saveSmtVisitor) {
		SmtVisitor smtVisitor = BeanUtils.transform(SmtVisitor.class, saveSmtVisitor);
		smtVisitor.setDelFlag(SymbolConstants.ZERO_INTEGER);
		//判断是否有车牌
		smtVisitor.setIsVehicle(StringUtils.isEmpty(saveSmtVisitor.getVehiclePlate())
				? SmtVisitorEnum.NOT_VEHICLE.getType() : SmtVisitorEnum.IS_VEHICLE.getType());
		//黑名单检测
		this.checkBlack(smtVisitor);
		smtVisitor.setVisitorPhotoId(saveSmtVisitor.getVisitorPhoto());
		if (!StringUtils.isEmpty(saveSmtVisitor.getStartTime())) {
			smtVisitor.setStartTime(DateUtils.parse(saveSmtVisitor.getStartTime()));
		}
		if (!StringUtils.isEmpty(saveSmtVisitor.getEndTime())) {
			smtVisitor.setEndTime(DateUtils.parse(saveSmtVisitor.getEndTime()));
		}
		//正则判断
		ExceptionTypeEnum exceptionType = visitorCheck(smtVisitor);
		if (!exceptionType.equals(ExceptionTypeEnum.CHECK_SUCCESS)) {
			throw new TCEException(exceptionType);
		}
		smtVisitor.setStatus(SmtAppVisitorEnum.UNTREATED_STATUS.getType());
		//添加一个访客时默认没有给访客发送预定多少分钟是否提醒的短信
		smtVisitor.setIsSend(SmtVisitorEnum.NOT_IS_SEND.getType());
		smtVisitor.setCreateTime(DateUtils.date());
		smtVisitor.setRemark(saveSmtVisitor.getRemark());
		smtVisitor.setVisitorFrontPhotoId(saveSmtVisitor.getVisitorFrontPhoto());
		//提交OA申请
		String processId = this.sendHfOaVisit(saveSmtVisitor);
		smtVisitor.setProcessId(processId);
		//添加访客信息
		this.save(smtVisitor);
		//添加随行人员
		this.saveHfFellow(smtVisitor.getId(), saveSmtVisitor.getFellowVisitorList());
		return smtVisitor;
	}

	/**
	 * 合肥访客邀约
	 *
	 * @return
	 */
	@Override
	public String saveHfInvitation(SaveSmtVisitor saveSmtVisitor, String token) {
		if (!hfToken.equals(token)) {
			throw new SmartException("token无效");
		}
		SmtVisitor smtVisitor = BeanUtils.transform(SmtVisitor.class, saveSmtVisitor);
		//判断是否有车牌
		smtVisitor.setIsVehicle(StringUtils.isEmpty(saveSmtVisitor.getVehiclePlate())
				? SmtVisitorEnum.NOT_VEHICLE.getType() : SmtVisitorEnum.IS_VEHICLE.getType());
		List<SaveFellowVisitorDTO> fellowVisitor = saveSmtVisitor.getFellowVisitorList();
		if (CollUtil.isEmpty(fellowVisitor)) {
			throw new SmartException("访客列表为空");
		}
		//被访人
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(saveSmtVisitor.getReceptionistBadge());
		if (Objects.isNull(staff)) {
			throw new SmartException("被访人不存在");
		}
		smtVisitor.setReceptionistPhone(staff.getPhone());
		smtVisitor.setReceptionistBadge(staff.getBadge());
		smtVisitor.setReceptionistName(staff.getName());
		smtVisitor.setReceptionistLevel(staff.getWelfareLevel());
		smtVisitor.setParkId(hfParkId);
		//列表第一位访客为主访客
		SaveFellowVisitorDTO visitor = fellowVisitor.get(0);
		smtVisitor.setVisitorPhotoId(smtImageService.saveImage(hfParkId,
				visitor.getFellowPhoto(), SmtImageEnum.TYPE_VISITOR_FACE.getCode()));
		smtVisitor.setVisitorFrontPhotoId(smtImageService.saveImage(hfParkId, visitor.getCertPic(),
				SmtImageEnum.TYPE_VISITOR_IDCARD_FRONT.getCode()));
		if (StrUtil.isNotEmpty(saveSmtVisitor.getTripCode())) {
			smtVisitor.setTripCode(smtImageService.saveImage(hfParkId, saveSmtVisitor.getTripCode(),
					SmtImageEnum.TYPE_UNKNOWN.getCode()));
		}
		if (StrUtil.isNotEmpty(saveSmtVisitor.getHealthcode())) {
			smtVisitor.setHealthcode(smtImageService.saveImage(hfParkId, saveSmtVisitor.getHealthcode(),
					SmtImageEnum.TYPE_UNKNOWN.getCode()));
		}
		smtVisitor.setVisitorName(visitor.getFellowName());
		smtVisitor.setCertNo(visitor.getCertNo());
		smtVisitor.setCertType(visitor.getCertType());
		if (!StringUtils.isEmpty(saveSmtVisitor.getStartTime())) {
			smtVisitor.setStartTime(DateUtils.parse(saveSmtVisitor.getStartTime()));
		}
		if (!StringUtils.isEmpty(saveSmtVisitor.getEndTime())) {
			smtVisitor.setEndTime(DateUtils.parse(saveSmtVisitor.getEndTime()));
		}
		//正则判断
		ExceptionTypeEnum exceptionType = visitorCheck(smtVisitor);
		if (!exceptionType.equals(ExceptionTypeEnum.CHECK_SUCCESS)) {
			throw new TCEException(exceptionType);
		}
		smtVisitor.setStatus(VisitorStatusEnum.Status_0.getCode());
		//添加一个访客时默认没有给访客发送预定多少分钟是否提醒的短信
		smtVisitor.setIsSend(SmtVisitorEnum.NOT_IS_SEND.getType());
		smtVisitor.setCreateTime(DateUtils.parse(saveSmtVisitor.getCreateTime()));
		//添加访客信息
		log.info("【合肥访客邀约推送2】:{}", smtVisitor);
		this.save(smtVisitor);
		//添加随行人员
		if (fellowVisitor.size() > 1) {
			fellowVisitor.remove(0);
			this.saveHfFellow(smtVisitor.getId(), fellowVisitor);
		}
		//下发权限与发送短信
		this.updateHfStatus(smtVisitor);
		return smtVisitor.getId().toString();
	}

	/**
	 * 发送OA申请
	 *
	 * @param saveSmtVisitor
	 * @return
	 */
	private String sendHfOaVisit(SaveSmtVisitor saveSmtVisitor) {
		SendVisitApplyReqDTO sendVisitApplyReqDTO = new SendVisitApplyReqDTO();
		VisitApplyMainReqDTO visitApplyMainReqDTO = new VisitApplyMainReqDTO();
		List<VisitApplyPersonReqDTO> visitApplyPersonList = new ArrayList<>();
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(saveSmtVisitor.getReceptionistBadge());
		if (Objects.isNull(staff) || StrUtil.isBlank(staff.getWelfareLevel())) {
			throw new SmartException("被访人福利层次为空");
		}
		SmtPark park = smtParkService.getById(saveSmtVisitor.getParkId());
		if (Objects.isNull(park)) {
			throw new SmartException("来访园区不存在");
		}
		//主表
		visitApplyMainReqDTO.setLfdw(saveSmtVisitor.getCompany());
		visitApplyMainReqDTO.setLfyq(ParkTypeEnum.code(park.getParkName()));
		visitApplyMainReqDTO.setXcm2(SymbolConstants.BLANK);
		visitApplyMainReqDTO.setJkm2(SymbolConstants.BLANK);
		visitApplyMainReqDTO.setCph(SymbolConstants.BLANK);
		visitApplyMainReqDTO.setBz(SymbolConstants.BLANK);
		visitApplyMainReqDTO.setFlcj(staff.getWelfareLevel());
		visitApplyMainReqDTO.setXdwp(HfVisitCarryItemsEnum.ITEM_7.getCode().toString());
		visitApplyMainReqDTO.setStartTime(saveSmtVisitor.getStartTime());
		visitApplyMainReqDTO.setEndTime(saveSmtVisitor.getEndTime());
		visitApplyMainReqDTO.setSqsj(DateUtils.format(LocalDateTime.now()));
		visitApplyMainReqDTO.setLfsy(VisitorEnum.oaCode(saveSmtVisitor.getCause()).toString());
		visitApplyMainReqDTO.setLfrsjhm(saveSmtVisitor.getVisitorPhone());
		visitApplyMainReqDTO.setBadge(saveSmtVisitor.getReceptionistBadge());
		if (Objects.nonNull(saveSmtVisitor.getVehiclePlate())) {
			visitApplyMainReqDTO.setCph(saveSmtVisitor.getVehiclePlate());
		}
		if (Objects.nonNull(saveSmtVisitor.getCarryThing())) {
			visitApplyMainReqDTO.setXdwp(saveSmtVisitor.getCarryThing().toString());
		}
		if (StrUtil.isNotEmpty(saveSmtVisitor.getRemark())) {
			visitApplyMainReqDTO.setBz(saveSmtVisitor.getRemark());
		}
		if (StrUtil.isNotBlank(saveSmtVisitor.getTripCode())) {
			visitApplyMainReqDTO.setXcm2(imageService.buildImageUrl(saveSmtVisitor.getParkId(), saveSmtVisitor.getTripCode()));
		}
		if (StrUtil.isNotBlank(saveSmtVisitor.getHealthcode())) {
			visitApplyMainReqDTO.setJkm2(imageService.buildImageUrl(saveSmtVisitor.getParkId(), saveSmtVisitor.getHealthcode()));
		}
		VisitApplyPersonReqDTO visitor = new VisitApplyPersonReqDTO();
		visitor.setXm(saveSmtVisitor.getVisitorName());
		visitor.setZjlx(saveSmtVisitor.getCertType().toString());
		visitor.setFkzp2(imageService.buildImageUrl(saveSmtVisitor.getParkId(), saveSmtVisitor.getVisitorPhoto()));
		if (StrUtil.isNotEmpty(saveSmtVisitor.getVisitorFrontPhoto())) {
			visitor.setZjzp2(imageService.buildImageUrl(saveSmtVisitor.getParkId(), saveSmtVisitor.getVisitorFrontPhoto()));
		}
		visitor.setZjhm(saveSmtVisitor.getCertNo());
		visitor.setJrjssj(saveSmtVisitor.getEndTime());
		visitor.setJrkssj(saveSmtVisitor.getStartTime());
		visitApplyPersonList.add(visitor);
		List<SaveFellowVisitorDTO> visitorDTOS = saveSmtVisitor.getFellowVisitorList();
		if (CollUtil.isNotEmpty(visitorDTOS)) {
			visitorDTOS.forEach(visitApply -> {
				VisitApplyPersonReqDTO reqDTO = new VisitApplyPersonReqDTO();
				reqDTO.setZjzp2(SymbolConstants.BLANK);
				reqDTO.setFkzp2(SymbolConstants.BLANK);
				reqDTO.setXm(visitApply.getFellowName());
				if (Objects.nonNull(visitApply.getCertType())) {
					reqDTO.setZjlx(visitApply.getCertType().toString());
					reqDTO.setZjhm(visitApply.getCertNo());
				}
				if (StrUtil.isNotEmpty(visitApply.getCertPic())) {
					reqDTO.setZjzp2(imageService.buildImageUrl(saveSmtVisitor.getParkId(), visitApply.getCertPic()));
				}
				if (StrUtil.isNotEmpty(visitApply.getFellowPhoto())) {
					reqDTO.setFkzp2(imageService.buildImageUrl(saveSmtVisitor.getParkId(), visitApply.getFellowPhoto()));
				}
				reqDTO.setJrjssj(saveSmtVisitor.getEndTime());
				reqDTO.setJrkssj(saveSmtVisitor.getStartTime());
				visitApplyPersonList.add(reqDTO);
			});
		}
		sendVisitApplyReqDTO.setVisitApplyMainReqDTO(visitApplyMainReqDTO);
		sendVisitApplyReqDTO.setVisitApplyPersonReqDTOS(visitApplyPersonList);
		String processId = null;

		Result<String> result = remoteOaWorkFlowService.sendVisitApply(sendVisitApplyReqDTO);
		if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
			throw new SmartException("OA流程提交异常");
		}
		processId = result.getData();
		if (Long.parseLong(processId) < 1) {
			throw new SmartException("OA流程提交异常,OA错误码" + processId);
		}
		return processId;
	}

	/**
	 * 保存合肥访客随行人员
	 *
	 * @param visitorId
	 * @param fellowVisitorList
	 * @return
	 */
	private Boolean saveHfFellow(Long visitorId, List<SaveFellowVisitorDTO> fellowVisitorList) {
		if (CollUtil.isEmpty(fellowVisitorList)) {
			return Boolean.TRUE;
		}
		fellowVisitorList.forEach(req -> {
			SmtFellowVisitor smtFellowVisitor = BeanUtils.transform(SmtFellowVisitor.class, req);
			smtFellowVisitor.setFellowPhotoId(req.getFellowPhoto());
			smtFellowVisitor.setVisitorId(visitorId);
			smtFellowVisitorService.save(smtFellowVisitor);
		});
		return Boolean.TRUE;
	}

	@Override
	public Boolean updateHfStatus(SmtVisitor smtVisitor) {
		log.info("合肥访客预约状态更新：{}", smtVisitor);
		//过期审批不下发 只修改状态
		if (smtVisitor.getEndTime() != null && new Date().after(smtVisitor.getEndTime())) {
			smtVisitor.setStatus(VisitorStatusEnum.CAUSE_6.getCode());
			this.updateById(smtVisitor);
			return Boolean.TRUE;
		}
		//判断是否为状态为0：已经通过
		if (smtVisitor.getStatus().equals(SmtVisitorEnum.PASS_STATUS.getType())) {
			if (StrUtil.isBlank(smtVisitor.getSmsCode())) {
				smtVisitor.setSmsCode(RandomUtil.randomNumbers(6));
			}
			this.updateById(smtVisitor);
			//添加定时任务，下发闸机或者道闸
			addTaskVisitor(smtVisitor);
			sendVisitorPassNoticeSafely(smtVisitor);
			sendReceptionistPassNoticeSafely(smtVisitor);
			return Boolean.TRUE;
		} else if (smtVisitor.getStatus().equals(SmtVisitorEnum.NOTPASS_STATUS.getType())) {
			try {
				sendMessage(smtVisitor.getVisitorPhone(), smtVisitor.getVisitorName(), SmsTemplateEnum.VISIT_1002.getCode(), smtVisitor.getReceptionistName(),
						DateUtils.formatDateTime(smtVisitor.getStartTime()), null, null, smtVisitor.getCompany(), null,
						smtVisitor.getRemark(), null, ParkNoticeTypeEnum.VISIT_APPLY_FAILD.getCode(), smtVisitor.getParkId());
			} catch (Exception e) {
				log.error("发送访客预约拒绝短信异常-->{}", e.getMessage());
			}
		}
		return this.updateById(smtVisitor);
	}

	private void sendVisitorPassNoticeSafely(SmtVisitor smtVisitor) {
		try {
			Result<SendSmsVo> sendResult = sendMessage(smtVisitor.getVisitorPhone(), smtVisitor.getVisitorName(),
					SmsTemplateEnum.VISIT_1001.getCode(), smtVisitor.getReceptionistName(),
					DateUtils.formatDateTime(smtVisitor.getStartTime()), null, null, smtVisitor.getCompany(),
					null, null, smtVisitor.getSmsCode(), ParkNoticeTypeEnum.VISIT_APPLY_SUCCESS.getCode(),
					smtVisitor.getParkId());
			if (ObjectUtil.isNotNull(sendResult) && !sendResult.isSuccess()) {
				sendSmsFailNoticeSafely(smtVisitor, SmsTemplateEnum.VISIT_1001, sendResult.getMsg());
			}
		} catch (Exception e) {
			log.error("发送访客预约成功短信异常，visitorId={}，phone={}", smtVisitor.getId(), smtVisitor.getVisitorPhone(), e);
		}
	}

	private void sendReceptionistPassNoticeSafely(SmtVisitor smtVisitor) {
		try {
			Result<SendSmsVo> sendResult = sendMessage(smtVisitor.getReceptionistPhone(), smtVisitor.getVisitorName(),
					SmsTemplateEnum.VISIT_1006.getCode(), smtVisitor.getReceptionistName(),
					DateUtils.formatDateTime(smtVisitor.getStartTime()), null, null,
					smtVisitor.getCompany(), null, null, null,
					ParkNoticeTypeEnum.VISIT_APPLY_SUCCESS_NOTICE_HOST.getCode(), smtVisitor.getParkId());
			if (ObjectUtil.isNotNull(sendResult) && !sendResult.isSuccess()) {
				sendSmsFailNoticeSafely(smtVisitor, SmsTemplateEnum.VISIT_1006, sendResult.getMsg());
			}
		} catch (Exception e) {
			log.error("发送被访人预约成功短信异常，visitorId={}，phone={}", smtVisitor.getId(), smtVisitor.getReceptionistPhone(), e);
		}
	}

	private void sendSmsFailNoticeSafely(SmtVisitor smtVisitor, SmsTemplateEnum failedTemplate, String remark) {
		try {
			sendMessageError(smtVisitor.getReceptionistPhone(), SmsTemplateEnum.SMS_12001.getCode(),
					failedTemplate.getDesc(), remark, ParkNoticeTypeEnum.SMS_SEND_FAILD.getCode(), smtVisitor.getParkId());
		} catch (Exception e) {
			log.error("发送访客短信失败告警异常，visitorId={}，template={}", smtVisitor.getId(), failedTemplate.getCode(), e);
		}
	}

	@Override
	public void updateOaStatusTask() {
		List<SmtVisitor> applyList = pendingOaStatusList();
		if (CollUtil.isEmpty(applyList)) {
			return;
		}
		int changedCount = 0;
		for (SmtVisitor visitor : applyList) {
			if (syncOaStatus(visitor)) {
				changedCount++;
			}
		}
		log.info("访客OA审批状态同步结束，本批数量：{}，状态变更：{}", applyList.size(), changedCount);
	}

	private List<SmtVisitor> pendingOaStatusList() {
		Map<Long, SmtVisitor> visitorMap = new LinkedHashMap<>();
		for (SmtVisitor visitor : pendingOaStatusPage(true).getRecords()) {
			visitorMap.put(visitor.getId(), visitor);
		}
		List<SmtVisitor> oldestPage = pendingOaStatusPage(false).getRecords();
		for (SmtVisitor visitor : oldestPage) {
			visitorMap.put(visitor.getId(), visitor);
		}
		for (SmtVisitor visitor : pendingOaStatusRecheckList()) {
			visitorMap.put(visitor.getId(), visitor);
		}
		if (CollUtil.isNotEmpty(oldestPage)) {
			List<SmtVisitor> cursorPage = pendingOaStatusCursorPage().getRecords();
			if (CollUtil.isEmpty(cursorPage) && sharedCursor(OA_STATUS_CURSOR_KEY, oaStatusCursor) > 0) {
				updateSharedCursor(OA_STATUS_CURSOR_KEY, oaStatusCursor, 0L);
				cursorPage = pendingOaStatusCursorPage().getRecords();
			}
			advanceOaStatusCursor(cursorPage);
			for (SmtVisitor visitor : cursorPage) {
				visitorMap.put(visitor.getId(), visitor);
			}
		}
		return new ArrayList<>(visitorMap.values());
	}

	private List<SmtVisitor> pendingOaStatusRecheckList() {
		List<Long> recheckIds = pendingOaStatusRecheckIds();
		if (CollUtil.isEmpty(recheckIds)) {
			return Collections.emptyList();
		}
		Page<SmtVisitor> page = new Page<>(1, recheckIds.size());
		page.setSearchCount(false);
		List<SmtVisitor> records = this.page(page, Wrappers.<SmtVisitor>query().lambda()
				.eq(SmtVisitor::getStatus, VisitorStatusEnum.Status_2.getCode())
				.isNotNull(SmtVisitor::getProcessId)
				.gt(SmtVisitor::getEndTime, new Date())
				.in(SmtVisitor::getId, recheckIds)
				.orderByAsc(SmtVisitor::getId)).getRecords();
		removeFinishedOaStatusRecheckIds(recheckIds, records);
		return records;
	}

	private List<Long> pendingOaStatusRecheckIds() {
		List<Long> sharedIds = sharedRecheckIds(OA_STATUS_RECHECK_KEY, oaStatusRecheckIds);
		if (CollUtil.isNotEmpty(sharedIds)) {
			return sharedIds;
		}
		synchronized (oaStatusRecheckIds) {
			return new ArrayList<>(oaStatusRecheckIds);
		}
	}

	private void rememberPendingOaStatus(SmtVisitor visitor) {
		if (visitor == null || visitor.getId() == null) {
			return;
		}
		rememberSharedRecheckId(OA_STATUS_RECHECK_KEY, oaStatusRecheckIds, visitor.getId());
	}

	private void forgetPendingOaStatus(Long visitorId) {
		forgetSharedRecheckId(OA_STATUS_RECHECK_KEY, oaStatusRecheckIds, visitorId);
	}

	private void removeFinishedOaStatusRecheckIds(List<Long> candidateIds, List<SmtVisitor> records) {
		Set<Long> activeIds = CollUtil.isEmpty(records) ? Collections.emptySet() : records.stream()
				.map(SmtVisitor::getId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		for (Long candidateId : candidateIds) {
			if (!activeIds.contains(candidateId)) {
				forgetPendingOaStatus(candidateId);
			}
		}
	}

	private IPage<SmtVisitor> pendingOaStatusPage(boolean latestFirst) {
		Page<SmtVisitor> page = new Page<>(1, OA_STATUS_SYNC_PAGE_SIZE);
		page.setSearchCount(false);
		LambdaQueryWrapper<SmtVisitor> queryWrapper = Wrappers.<SmtVisitor>query().lambda()
				.eq(SmtVisitor::getStatus, VisitorStatusEnum.Status_2.getCode())
				.isNotNull(SmtVisitor::getProcessId)
				.gt(SmtVisitor::getEndTime, new Date());
		if (latestFirst) {
			queryWrapper.orderByDesc(SmtVisitor::getCreateTime)
					.orderByDesc(SmtVisitor::getId);
		} else {
			queryWrapper.orderByAsc(SmtVisitor::getCreateTime)
					.orderByAsc(SmtVisitor::getId);
		}
		return this.page(page, queryWrapper);
	}

	private IPage<SmtVisitor> pendingOaStatusCursorPage() {
		Page<SmtVisitor> page = new Page<>(1, OA_STATUS_SYNC_PAGE_SIZE);
		page.setSearchCount(false);
		LambdaQueryWrapper<SmtVisitor> queryWrapper = Wrappers.<SmtVisitor>query().lambda()
				.eq(SmtVisitor::getStatus, VisitorStatusEnum.Status_2.getCode())
				.isNotNull(SmtVisitor::getProcessId)
				.gt(SmtVisitor::getEndTime, new Date());
		long cursor = sharedCursor(OA_STATUS_CURSOR_KEY, oaStatusCursor);
		if (cursor > 0) {
			queryWrapper.gt(SmtVisitor::getId, cursor);
		}
		queryWrapper.orderByAsc(SmtVisitor::getId);
		return this.page(page, queryWrapper);
	}

	private void advanceOaStatusCursor(List<SmtVisitor> visitorList) {
		if (CollUtil.isEmpty(visitorList)) {
			return;
		}
		visitorList.stream()
				.map(SmtVisitor::getId)
				.filter(Objects::nonNull)
				.max(Long::compareTo)
				.ifPresent(cursor -> updateSharedCursor(OA_STATUS_CURSOR_KEY, oaStatusCursor, cursor));
	}

	private long sharedCursor(String redisKey, AtomicLong fallbackCursor) {
		if (stringRedisTemplate == null) {
			return fallbackCursor.get();
		}
		try {
			String cursorValue = stringRedisTemplate.opsForValue().get(redisKey);
			if (StrUtil.isBlank(cursorValue)) {
				return fallbackCursor.get();
			}
			return Long.parseLong(cursorValue);
		} catch (Exception e) {
			log.warn("读取访客OA同步游标失败，key={}", redisKey, e);
			return fallbackCursor.get();
		}
	}

	private void updateSharedCursor(String redisKey, AtomicLong fallbackCursor, Long cursor) {
		if (cursor == null) {
			return;
		}
		fallbackCursor.set(cursor);
		if (stringRedisTemplate == null) {
			return;
		}
		try {
			stringRedisTemplate.opsForValue().set(redisKey, cursor.toString(), SHARED_SYNC_STATE_KEEP_HOURS, TimeUnit.HOURS);
		} catch (Exception e) {
			log.warn("写入访客OA同步游标失败，key={}，cursor={}", redisKey, cursor, e);
		}
	}

	private List<Long> sharedRecheckIds(String redisKey, Set<Long> fallbackIds) {
		if (stringRedisTemplate == null) {
			return limitedLocalRecheckIds(fallbackIds);
		}
		try {
			Set<String> idValues = stringRedisTemplate.opsForZSet().rangeByScore(redisKey, 0,
					System.currentTimeMillis(), 0, OA_STATUS_RECHECK_BATCH_SIZE);
			if (CollUtil.isEmpty(idValues)) {
				return limitedLocalRecheckIds(fallbackIds);
			}
			List<Long> ids = idValues.stream()
					.map(this::parseLongSilently)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			if (CollUtil.isNotEmpty(ids)) {
				return ids;
			}
		} catch (Exception e) {
			log.warn("读取访客OA重查集合失败，key={}", redisKey, e);
		}
		return limitedLocalRecheckIds(fallbackIds);
	}

	private void rememberSharedRecheckId(String redisKey, Set<Long> fallbackIds, Long id) {
		if (id == null) {
			return;
		}
		rememberLocalRecheckId(fallbackIds, id);
		if (stringRedisTemplate == null) {
			return;
		}
		try {
			String idValue = id.toString();
			long now = System.currentTimeMillis();
			stringRedisTemplate.opsForZSet().add(redisKey, idValue, now);
			Long size = stringRedisTemplate.opsForZSet().zCard(redisKey);
			if (size != null && size > OA_STATUS_RECHECK_ID_LIMIT) {
				stringRedisTemplate.opsForZSet().removeRange(redisKey, OA_STATUS_RECHECK_ID_LIMIT, size - 1);
			}
			stringRedisTemplate.expire(redisKey, SHARED_SYNC_STATE_KEEP_HOURS, TimeUnit.HOURS);
		} catch (Exception e) {
			log.warn("写入访客OA重查集合失败，key={}，id={}", redisKey, id, e);
		}
	}

	private void forgetSharedRecheckId(String redisKey, Set<Long> fallbackIds, Long id) {
		if (id == null) {
			return;
		}
		synchronized (fallbackIds) {
			fallbackIds.remove(id);
		}
		if (stringRedisTemplate == null) {
			return;
		}
		try {
			stringRedisTemplate.opsForZSet().remove(redisKey, id.toString());
		} catch (Exception e) {
			log.warn("移除访客OA重查集合失败，key={}，id={}", redisKey, id, e);
		}
	}

	private List<Long> limitedLocalRecheckIds(Set<Long> fallbackIds) {
		synchronized (fallbackIds) {
			return fallbackIds.stream()
					.limit(OA_STATUS_RECHECK_BATCH_SIZE)
					.collect(Collectors.toList());
		}
	}

	private void rememberLocalRecheckId(Set<Long> fallbackIds, Long id) {
		synchronized (fallbackIds) {
			fallbackIds.add(id);
			while (fallbackIds.size() > OA_STATUS_RECHECK_ID_LIMIT) {
				Iterator<Long> iterator = fallbackIds.iterator();
				if (!iterator.hasNext()) {
					return;
				}
				iterator.next();
				iterator.remove();
			}
		}
	}

	private Long parseLongSilently(String value) {
		if (StrUtil.isBlank(value)) {
			return null;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	private boolean syncOaStatus(SmtVisitor visitor) {
		if (visitor == null || visitor.getId() == null || StrUtil.isBlank(visitor.getProcessId())) {
			return false;
		}
		WorkFlowLogDTO workFlowLogDTO;
		try {
			workFlowLogDTO = oaWorkflowService.query(visitor.getProcessId());
		} catch (Exception e) {
			log.warn("访客OA审批状态查询失败，id={}，processId={}", visitor.getId(), visitor.getProcessId(), e);
			rememberPendingOaStatus(visitor);
			return false;
		}
		Integer finalStatus = resolveOaFinalStatus(workFlowLogDTO);
		if (finalStatus == null) {
			rememberPendingOaStatus(visitor);
			return false;
		}
		forgetPendingOaStatus(visitor.getId());
		visitor.setStatus(finalStatus);
		if (!claimOaFinalStatus(visitor)) {
			log.info("访客OA审批状态已被其他任务处理，id={}，processId={}", visitor.getId(), visitor.getProcessId());
			return false;
		}
		try {
			this.updateHfStatus(visitor);
		} catch (Exception e) {
			log.error("访客OA审批通过后续处理失败，id={}，processId={}", visitor.getId(), visitor.getProcessId(), e);
			restorePendingOaStatusAfterPostApprovalFailure(visitor, finalStatus);
			rememberPendingOaStatus(visitor);
		}
		return true;
	}

	private Integer resolveOaFinalStatus(WorkFlowLogDTO workFlowLogDTO) {
		if (ObjectUtil.isNull(workFlowLogDTO) || !workFlowLogDTO.success()) {
			return null;
		}
		List<WorkFlowLogDataDTO> data = workFlowLogDTO.getResultdata();
		if (CollUtil.isEmpty(data)) {
			return null;
		}
		WorkFlowLogDataDTO dataDTO = data.get(data.size() - 1);
		if (OaFinalStatusEnum.CAUSE_3.getCode().toString().equals(dataDTO.getCURRENTNODETYPE())) {
			return VisitorStatusEnum.Status_0.getCode();
		}
		if (OaFinalStatusEnum.CAUSE_0.getCode().toString().equals(dataDTO.getCURRENTNODETYPE())) {
			return VisitorStatusEnum.Status_1.getCode();
		}
		return null;
	}

	private boolean claimOaFinalStatus(SmtVisitor visitor) {
		LambdaUpdateWrapper<SmtVisitor> updateWrapper = Wrappers.<SmtVisitor>lambdaUpdate()
				.eq(SmtVisitor::getId, visitor.getId())
				.eq(SmtVisitor::getStatus, VisitorStatusEnum.Status_2.getCode());
		if (visitor.getEndTime() != null && new Date().after(visitor.getEndTime())) {
			visitor.setStatus(VisitorStatusEnum.CAUSE_6.getCode());
			updateWrapper.set(SmtVisitor::getStatus, visitor.getStatus());
			return this.update(updateWrapper);
		}
		updateWrapper.set(SmtVisitor::getStatus, visitor.getStatus());
		if (VisitorStatusEnum.Status_0.getCode().equals(visitor.getStatus())) {
			if (StrUtil.isBlank(visitor.getSmsCode())) {
				visitor.setSmsCode(RandomUtil.randomNumbers(6));
			}
			updateWrapper.set(SmtVisitor::getSmsCode, visitor.getSmsCode());
		}
		return this.update(updateWrapper);
	}

	private void restorePendingOaStatusAfterPostApprovalFailure(SmtVisitor visitor, Integer finalStatus) {
		if (!VisitorStatusEnum.Status_0.getCode().equals(finalStatus) || visitor == null || visitor.getId() == null) {
			return;
		}
		boolean restored = this.update(Wrappers.<SmtVisitor>lambdaUpdate()
				.eq(SmtVisitor::getId, visitor.getId())
				.eq(SmtVisitor::getStatus, VisitorStatusEnum.Status_0.getCode())
				.set(SmtVisitor::getStatus, VisitorStatusEnum.Status_2.getCode()));
		if (restored) {
			visitor.setStatus(VisitorStatusEnum.Status_2.getCode());
		}
	}

}
