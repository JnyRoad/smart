package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.msg.req.SendOutDormitoryReqDTO;
import com.tce.smart.data.api.dto.ehrview.CvwCcdAllowRuleDTO;
import com.tce.smart.data.api.dto.ehrview.CvwCcdAllowanceDTO;
import com.tce.smart.data.api.dto.ehrview.EvwCcdFlstandardDTO;
import com.tce.smart.data.api.dto.ehrview.OvwYsCallOwanceDetailsDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsCallOwanceCancelAllRespDTO;
import com.tce.smart.data.api.feign.ehrview.*;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.platform.api.dto.SmtOutDormitoryStaffDTO;
import com.tce.smart.platform.api.dto.req.SearchOutDormitoryReqDTO;
import com.tce.smart.platform.api.dto.resp.AllowanceStatusRespDTO;
import com.tce.smart.platform.api.dto.resp.DormitoryRoomDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.DormitorySituationRespDTO;
import com.tce.smart.platform.conf.DormitoryConfiguration;
import com.tce.smart.platform.core.dto.AppMsgPushDTO;
import com.tce.smart.platform.core.dto.SearchOutDormitoryDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtOutDormitoryStaffMapper;
import com.tce.smart.platform.core.vo.AllowanceInfoVO;
import com.tce.smart.platform.core.vo.FlowVO;
import com.tce.smart.platform.core.vo.OutDormitoryDetailVO;
import com.tce.smart.platform.core.vo.SearchOutDormitoryVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.remoteLock.ConnectLockService;
import com.tce.smart.tool.constant.DormitoryConstans;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 员工宿舍信息表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmtOutDormitoryStaffServiceImpl extends ServiceImpl<SmtOutDormitoryStaffMapper, SmtOutDormitoryStaff> implements SmtOutDormitoryStaffService {
	private final SmtStaffService staffService;

	private final RemoteCvwCcdAllowanceService remoteCvwCcdAllowanceService;
	private final RemoteCvwCcdAllowRuleService remoteCvwCcdAllowRuleService;
	private final RemoteEvwCcdFlstandardService remoteEvwCcdFlstandardService;
	private final RemoteOaWorkFlowService remoteOaWorkFlowService;
	private final SmtProcessRecordService smtProcessRecordService;
	private final RemoteEvwEmphrYsService remoteEvwEmphrYsService;
	private final IAppMsgPushService appMsgPushService;
	private final IOAWorkflowService oaWorkflowService;
	private final ConnectLockService connectLockService;
	private final SmtDormitoryStaffService dormitoryStaffService;
	private final RemoteOvwYsCallOwanceCancelService remoteOvwYsCallOwanceCancelService;
	private final RemoteOvwYsCallOwanceDetailsService ovwYsCallOwanceDetailsService;
	private final SmtDormitoryOutRemarkService smtDormitoryOutRemarkService;

	@Autowired
	private DormitoryConfiguration conf;

	@Value("${smart.sy-park-id:0}")
	private Integer syParkId;

	@Override
	public AllowanceStatusRespDTO status(String staffBadge) {
		Result<EvwEmphrYsRespDTO> info = remoteEvwEmphrYsService.info(staffBadge, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (!info.getData().getStatus().equals(1) || !info.getData().getEmpType().equals(1)) {
			throw new TCEException("只有在职的正式工，才可申请");
		}
		// 0,"审批中" 1,"已审批" 2,"已回退" 3,"已撤销" -1"待申请"
		AllowanceStatusRespDTO respDTO = new AllowanceStatusRespDTO();
		List<SmtOutDormitoryStaff> accommodations = this.baseMapper.selectList(Wrappers.<SmtOutDormitoryStaff>query().lambda()
				.eq(SmtOutDormitoryStaff::getStaffBadge, staffBadge)
				.eq(SmtOutDormitoryStaff::getAllowanceType, "外宿补贴").orderByDesc(SmtOutDormitoryStaff::getCreateTime));
		if (Objects.isNull(accommodations) || accommodations.size() < 1) respDTO.setAccommodationStatus(-1);
		else respDTO.setAccommodationStatus(accommodations.get(0).getStatus());

		List<SmtOutDormitoryStaff> meals = this.baseMapper.selectList(Wrappers.<SmtOutDormitoryStaff>query().lambda()
				.eq(SmtOutDormitoryStaff::getStaffBadge, staffBadge)
				.eq(SmtOutDormitoryStaff::getAllowanceType, "外餐补贴").orderByDesc(SmtOutDormitoryStaff::getCreateTime));
		if (Objects.isNull(meals) || meals.size() < 1) respDTO.setMealStatus(-1);
		else respDTO.setMealStatus(meals.get(0).getStatus());
		return respDTO;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<Boolean> addOutDormitory(SmtOutDormitoryStaff apply) {
		// TODO Auto-generated method stubb
/*		if (apply.getOutAddress() == null || apply.getOutAddress().equals("")) {
			return new Result<>(Boolean.FALSE, "外宿地址不可为空，申请失败");
		}*/
		if (apply.getStaffBadge() == null || apply.getStaffBadge().equals("")) {
			return new Result<>(Boolean.FALSE, "员工号不能为空，申请失败");
		}
		Result<EvwEmphrYsRespDTO> info = remoteEvwEmphrYsService.info(apply.getStaffBadge(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		log.info("remote  remoteEvwEmphrYsService info Result=[]" + info);
		if (!info.getData().getStatus().equals(1) || !info.getData().getEmpType().equals(1)) {
			return new Result<>(Boolean.FALSE, "只有在职的正式工，才可申请");
		}

		//25号之后提交的，补贴开始时间要下下个月，25号之前的是 补贴时间下个月
		if (ObjectUtil.isNotNull(apply.getStartTime()) && !apply.getStartTime().equals("")) {
			Calendar date = Calendar.getInstance();
			Integer day = date.get(Calendar.DAY_OF_MONTH);
			Integer month = date.get(Calendar.MONDAY);
			Integer startMonth = DateUtil.parseDate(apply.getStartTime()).getMonth();
			Integer startDay = DateUtil.parseDate(apply.getStartTime()).getDate();

			//月份差
			int subMonth = (DateUtil.parseDate(apply.getStartTime()).year() - date.get(Calendar.YEAR)) * 12 + (DateUtil.parseDate(apply.getStartTime()).month() - date.get(Calendar.MONTH));

			if (day < 25) {
				//25号之前的是 补贴时间下个月
				if (subMonth < 1 || startDay != 1) {
					date.set(Calendar.MONTH, date.get(Calendar.MONTH) + 1);
					return new Result<>(Boolean.FALSE, "25号之前申请的外宿，补贴开始时间应从下个月开始，正确时间为" + date.get(Calendar.YEAR) + "-" + (date.get(Calendar.MONTH) + 1));
				}
			} else if (day >= 25) {
				//25号之后的是 补贴时间下下个月
				if (subMonth < 2 || startDay != 1) {
					date.set(Calendar.MONTH, date.get(Calendar.MONTH) + 2);
					return new Result<>(Boolean.FALSE, "25号之后申请的外宿，补贴开始时间应从下下个月开始，正确时间为" + date.get(Calendar.YEAR) + "-" + (date.get(Calendar.MONTH) + 1));
				}
			}
		}

		apply.setCreateTime(DateUtil.date());
		//0未审批，1-已审批
		apply.setStatus(OutDormitoryStatusEnum.NOT_APPROVAL.getCode());
		apply.setIsDelete(OutDormitoryStatusEnum.NOT_APPROVAL.getCode());
		this.baseMapper.insert(apply);
		//调用流程接口，获取到流程id
		String processId = "";
		SendOutDormitoryReqDTO sendOutDormitoryAo = CreateSendOutDormitoryAo(apply);
		log.info("======sendOutDormitory params ======:" + sendOutDormitoryAo);
		Result<String> result = remoteOaWorkFlowService.sendOutDormitory(sendOutDormitoryAo);
		log.info("======remoteOaWorkFlowService result ======:" + result);
		if (result.isSuccess()) {
			if (ObjectUtil.isNotNull(result.getData())) {

				processId = result.getData();
				if ("-7".equals(processId)) {
					throw new TCEException("获取不到OA审批人员，请联系OA管理处理后再试");
				}
			} else {
				this.removeById(apply.getId());
				return new Result<>(Boolean.FALSE, "OA流程提交异常");
			}
		} else {
			this.removeById(apply.getId());
			return new Result<>(Boolean.FALSE, "OA流程提交异常");
		}
		apply.setProcessId(processId);
		this.baseMapper.updateById(apply);
		//获取oa审批流程
		getOAProcess(processId);
		return new Result<>(true);
	}


	@Override
	public void getOAProcess(String processId) {
		WorkFlowLogDTO workFlowLogDTO = oaWorkflowService.query(processId);
		if (ObjectUtil.isNotNull(workFlowLogDTO) && workFlowLogDTO.success()) {
			List<WorkFlowLogDataDTO> flowRecords = workFlowLogDTO.getResultdata();
			if (CollectionUtils.isNotEmpty(flowRecords)) {
				flowRecords.forEach(flowRecord -> saveProcessRecord(processId, flowRecord));
			}
		}
	}

	private void saveProcessRecord(String processId, WorkFlowLogDataDTO process) {
		SmtProcessRecord processRecord = smtProcessRecordService.getOne(Wrappers.<SmtProcessRecord>query().lambda()
				.eq(SmtProcessRecord::getProcessId, processId)
				.eq(SmtProcessRecord::getStaffBadge, process.getWORKCODE())
				.ne(SmtProcessRecord::getStatus, NodeStatusEnum.FINISHED.getCode())
				.ne(SmtProcessRecord::getStatus, NodeStatusEnum.NOT_FINISHED.getCode()));
		//1、判重
		if (ObjectUtil.isNull(processRecord)) {
			SmtProcessRecord processRocord = new SmtProcessRecord();
			processRocord.setCreatTime(DateUtil.date());
			processRocord.setNodeName(process.getNODENAME());
			processRocord.setProcessId(processId);
			String dateTime = process.getOPERATEDATE() + " " + process.getOPERATETIME();
			if (StrUtil.isNotBlank(process.getOPERATEDATE()) && StrUtil.isNotBlank(process.getOPERATETIME())) {
				processRocord.setRecordDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
			}
			processRocord.setRemark(process.getREMARK());
			processRocord.setStaffBadge(process.getWORKCODE());
			processRocord.setStaffName(process.getLASTNAME());
			processRocord.setStatus(process.getLOGTYPE());
			smtProcessRecordService.save(processRocord);
		}
	}


	private SendOutDormitoryReqDTO CreateSendOutDormitoryAo(SmtOutDormitoryStaff apply) {
		// TODO Auto-generated method stub
		SendOutDormitoryReqDTO sendOutDormitoryAo = new SendOutDormitoryReqDTO();
		SmtStaff selectOne = staffService.getOne(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getBadge, apply.getStaffBadge()));
		if (selectOne == null) {
			throw new TCEException("员工不存在");
		}
		sendOutDormitoryAo.setBadge(apply.getStaffBadge());
		sendOutDormitoryAo.setCompid(selectOne.getCompId());
		sendOutDormitoryAo.setDepid(selectOne.getDepId());
		sendOutDormitoryAo.setJobid(selectOne.getJobId());
		sendOutDormitoryAo.setJoindate(DateUtils.format(selectOne.getCreateTime(), "yyyy-MM-dd"));
		sendOutDormitoryAo.setFLCJ(selectOne.getWelfareLevel());
		sendOutDormitoryAo.setEXPLAIN(apply.getExplain());
	/*	String statusDescs=StaffStatusEnum.desc(selectOne.getStatementStatus());
		Integer statementStatus=EvwEmphrYsEnum.code(statusDescs);
		*/
		log.info("员工状态：" + selectOne.getStatus());
		sendOutDormitoryAo.setStatus(selectOne.getStatus());
		sendOutDormitoryAo.setName(selectOne.getName());
		sendOutDormitoryAo.setSeqid(apply.getId());
		Double amout = Double.valueOf(apply.getAmount());
		sendOutDormitoryAo.setAmount(amout);
		sendOutDormitoryAo.setBegindate(apply.getStartTime());

		if (StringUtil.isNullOrEmpty(apply.getEndTime())) {
			sendOutDormitoryAo.setAPPenddate("");
		} else {
			sendOutDormitoryAo.setAPPenddate(apply.getEndTime());
		}
		if (ObjectUtil.isNotNull(selectOne.getEId())) {
			sendOutDormitoryAo.setEID(selectOne.getEId().toString());
		} else {
			sendOutDormitoryAo.setEID("");
		}
		Result<CvwCcdAllowRuleDTO> byTitle = remoteCvwCcdAllowRuleService.getByTitle(apply.getComputaionRule(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (!byTitle.isSuccess()) {
			JSONObject errorInfoOjb = JSONUtil.parseObj(byTitle.getMsg());
			String errInfo = StringUtils.isNotBlank(errorInfoOjb.getStr("msg")) ? errorInfoOjb.getStr("msg")
					: errorInfoOjb.getStr("message");
			throw new TCEException(errInfo);
		}
		CvwCcdAllowRuleDTO data = byTitle.getData();
		sendOutDormitoryAo.setCOMPUTATIONRULE(data.getId());
		Result<CvwCcdAllowanceDTO> byName = remoteCvwCcdAllowanceService.getByName(apply.getAllowanceType(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (!byName.isSuccess()) {
			JSONObject errorInfoOjb = JSONUtil.parseObj(byName.getMsg());
			String errInfo = StringUtils.isNotBlank(errorInfoOjb.getStr("msg")) ? errorInfoOjb.getStr("msg")
					: errorInfoOjb.getStr("message");
			throw new TCEException(errInfo);
		}
		CvwCcdAllowanceDTO data2 = byName.getData();
		sendOutDormitoryAo.setXtype(data2.getId());

		Result<EvwEmphrYsRespDTO> info = remoteEvwEmphrYsService.info(apply.getStaffBadge(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		log.info("======remoteEvwEmphrYsService result{} ======:" + info);
		if (!info.isSuccess()) {
			JSONObject errorInfoOjb = JSONUtil.parseObj(info.getMsg());
			String errInfo = StringUtils.isNotBlank(errorInfoOjb.getStr("msg")) ? errorInfoOjb.getStr("msg")
					: errorInfoOjb.getStr("message");
			throw new TCEException(errInfo);
		}
		if (info.getData() != null) {
			sendOutDormitoryAo.setPZID(info.getData().getPzid());
		}
		return sendOutDormitoryAo;
	}


	@Override
	public Result getAllowance(String staffBadge, Integer type) {
		if (StringUtils.isBlank(staffBadge)) {
			throw new TCEException("员工号不能为空");
		}
		SmtStaff selectOne = staffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, staffBadge));
		if (Objects.isNull(selectOne)) {
			throw new TCEException("员工不存在");
		}
		String allowancename = DormitoryConstans.allowanceName;
		if (type == 10) {
			allowancename = "外餐补贴";
		}
		Result<CvwCcdAllowanceDTO> byName = remoteCvwCcdAllowanceService.getByName(allowancename, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		CvwCcdAllowanceDTO data = byName.getData();
		log.info("remote  remoteCvwCcdAllowanceService getByName Result=[]" + byName);
		Integer computationRule = data.getComputationRule();
		Result<CvwCcdAllowRuleDTO> byId = remoteCvwCcdAllowRuleService.getById(computationRule.toString(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		log.info("remote  remoteCvwCcdAllowRuleService getById Result=[]" + byId);
		String title = byId.getData().getTitle();
		String jcheId = selectOne.getJcheId();
		//根据员工号查询pzid
		Result<EvwEmphrYsRespDTO> info = remoteEvwEmphrYsService.info(staffBadge, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		log.info("remote  remoteEvwEmphrYsService info Result=[]" + info);
		Result<EvwCcdFlstandardDTO> byId2 = remoteEvwCcdFlstandardService.getById(jcheId, info.getData().getPzid(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		log.info("remote  remoteEvwCcdFlstandardService getById Result=[]" + byId2);
		AllowanceInfoVO vo = new AllowanceInfoVO();
		if (type == 10) {
			vo.setAmount(byId2.getData().getStandard());
		} else {
			vo.setAmount(byId2.getData().getStandard1());
		}
		vo.setAllowanceTypeName(allowancename);
		vo.setAllowanceType(type);
		vo.setComputaionRule(title);
		return new Result<>(vo);
	}

	@Override
	public Result getOutDormitoryInfo(String staffBadge, Integer type) {
		if (StringUtils.isBlank(staffBadge)) {
			throw new TCEException("员工号不能为空");
		}

		SmtStaff selectOne = staffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, staffBadge));
		if (Objects.isNull(selectOne)) {
			throw new TCEException("员工不存在");
		}
//		String allowancename = DormitoryConstans.allowanceName;
//		if(type == 10){
//			allowancename = "外餐补贴";
//		}
		List<SmtOutDormitoryStaff> list = this.baseMapper.selectList(Wrappers.<SmtOutDormitoryStaff>query().lambda()
				.eq(SmtOutDormitoryStaff::getStaffBadge, staffBadge).eq(SmtOutDormitoryStaff::getIsDelete, 0)
				.orderByDesc(SmtOutDormitoryStaff::getCreateTime));
		//查询流程结果
		List<SmtOutDormitoryStaffDTO> voList = new ArrayList<>();
		if (list.size() > 0) {
			for (SmtOutDormitoryStaff vo : list) {
				String processId = vo.getProcessId();
				List<FlowVO> flowList = new ArrayList<>();
				if (!StringUtils.isEmpty(processId)) {
					//根据流程id获取流程
					getOAProcessFlow(vo.getProcessId(), flowList);
					Boolean refuse = false;
					for (FlowVO flowVO : flowList) {
						if (flowVO.getProcessDesc().equals(ApplicationEnum.RECORD_STATUS_3.getDesc())) {
							refuse = true;
							vo.setStatus(OutDormitoryStatusEnum.IS_REFUSEL.getCode());
							break;
						} else if (flowVO.getProcessDesc().equals(ApplicationEnum.RECORD_STATUS_13.getDesc())) {
							refuse = true;
							vo.setStatus(OutDormitoryStatusEnum.IS_REVOKE.getCode());
							break;
						}
					}

					if (!refuse) {
						for (FlowVO flowVO : flowList) {
							if (flowVO.getProcessDesc().equals("批准")) {
								vo.setStatus(OutDormitoryStatusEnum.IS_APPROVAL.getCode());
								break;
							}
						}
					}
				}
				SmtOutDormitoryStaffDTO dto = new SmtOutDormitoryStaffDTO();
				BeanUtils.copyProperties(vo, dto);
				dto.setName(selectOne.getName());
				if (dto.getAllowanceType().equals(DormitoryConstans.allowanceName)) {
					dto.setType(11);
				} else {
					dto.setType(10);
				}
				voList.add(dto);
			}
		}
		return new Result<>(voList);
	}


	@Override
	public void approvalNotice(String staffBadge, String code, Integer id, boolean flag) {
		//推送App消息
		SmtOutDormitoryStaff byId = this.getById(id);
		if (flag) {
			byId.setStatus(OutDormitoryStatusEnum.IS_APPROVAL.getCode());
			byId.updateById();
			//当外宿审批通过后，若存在内宿信息，要将内宿信息删除并更换为外宿信息，
			SmtStaff staffOne = staffService.getOne(Wrappers.<SmtStaff>query().lambda()
					.eq(SmtStaff::getBadge, staffBadge));
			if (ObjectUtil.isNotNull(staffOne)) {
				staffOne.setDormitoryStatus(2);
				staffOne.updateById();
				SmtDormitoryStaff dormitoryStaff = dormitoryStaffService.getOne(Wrappers.<SmtDormitoryStaff>query().lambda()
						.eq(SmtDormitoryStaff::getStaffBadge, staffBadge));
				if (ObjectUtil.isNotNull(dormitoryStaff)) {
					boolean deleteById = dormitoryStaff.deleteById();
					if (deleteById) {
						//生成退宿记录
						addDormitoryHistory(dormitoryStaff, DormitoryHisotryTypeEnum.QUTI_DORMITORY.getCode());

					}
				}
			}
		} else {
			byId.setStatus(OutDormitoryStatusEnum.IS_REFUSEL.getCode());
			byId.updateById();
		}
		AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
		appMsgPushDTO.setBadge(staffBadge);
		appMsgPushDTO.setBussiessId(String.valueOf(id));
		appMsgPushDTO.setTemplateCode(code);
		if (byId.getAllowanceType().equals("外餐补贴")) {
			appMsgPushDTO.setExtraParam("type=10");
		} else if (byId.getAllowanceType().equals("外宿补贴")) {
			appMsgPushDTO.setExtraParam("type=11");
		}
		appMsgPushService.pushAppMsg(appMsgPushDTO);
	}

	private void addDormitoryHistory(SmtDormitoryStaff dormitoryStaff, Integer type) {
		addDormitoryHistory(dormitoryStaff, type, DormitoryStatisFlagnum.STATIS);
	}

	private void addDormitoryHistory(SmtDormitoryStaff dormitoryStaff, Integer type, DormitoryStatisFlagnum flagnum) {
		SmtDormitoryStaffHistory history = new SmtDormitoryStaffHistory();
		history.setBedId(dormitoryStaff.getBedId());
		history.setBedNumber(dormitoryStaff.getBedNumber());
		history.setTime(DateUtils.date());
		history.setCreateTime(DateUtils.date());
		history.setStatisFlag(flagnum.getCode());
		history.setDormitoryId(dormitoryStaff.getDormitoryId());
		history.setDormitoryName(dormitoryStaff.getDormitoryName());
		history.setDormitoryTypeId(dormitoryStaff.getDormitoryTypeId());
		history.setDormitoryTypeName(dormitoryStaff.getDormitoryTypeName());
		history.setFloorId(dormitoryStaff.getFloorId());
		history.setFloorName(dormitoryStaff.getFloorName());
		history.setInTime(dormitoryStaff.getCreateTime());
		history.setParkId(dormitoryStaff.getParkId());
		history.setParkName(dormitoryStaff.getParkName());
		history.setRoomId(dormitoryStaff.getRoomId());
		history.setRoomName(dormitoryStaff.getRoomName());
		history.setStaffBadge(dormitoryStaff.getStaffBadge());
		history.setStaffSex(dormitoryStaff.getStaffSex());
		history.setStaffId(dormitoryStaff.getStaffId());
		history.setStaffName(dormitoryStaff.getStaffName());
		history.setType(type);
		history.setIsStaff(dormitoryStaff.getIsStaff());
		history.setDfId(dormitoryStaff.getId());
		history.setCompName(dormitoryStaff.getCompName());
		history.setDepName(dormitoryStaff.getDepName());
		history.setJobName(dormitoryStaff.getJobName());
		try {
			history.setOptUser(SecurityUtils.getUser().getUsername());
		}catch (Exception e){}
		history.setInOptUser(dormitoryStaff.getOptUser());
		history.setInCreateTime(dormitoryStaff.getCreateTime());
		history.insert();
		if (!DormitoryHisotryTypeEnum.IN_DORMITORY.getCode().equals(type)) {
			smtDormitoryOutRemarkService.updateDorStaffId(dormitoryStaff.getId(), history.getId());
		}
		try {
			connectLockService.sendLockData(dormitoryStaff, type, null);
		} catch (Exception e) {
			log.error(e.getMessage());
	}
	}

	@Override
	public Result<OutDormitoryDetailVO> getOutDormitoryInfoDetail(Integer id) {
		SmtOutDormitoryStaff selectOne = this.baseMapper.selectById(id);
		if (ObjectUtil.isNull(selectOne)) {
			throw new TCEException("该员工没有外宿信息");
		}
		SmtStaff staffOne = staffService.getOne(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getBadge, selectOne.getStaffBadge()));

		//如果被退回，则返回空
		OutDormitoryDetailVO vo = new OutDormitoryDetailVO();
		vo.setStaffBadge(selectOne.getStaffBadge());
		vo.setAllowanceType(selectOne.getAllowanceType());
		vo.setAmount(selectOne.getAmount());
		vo.setComputaionRule(selectOne.getComputaionRule());
		vo.setEndTime(selectOne.getEndTime());
		vo.setExplain(selectOne.getExplain() == null ? "" : selectOne.getExplain());
		vo.setOutAddress(selectOne.getOutAddress());
		vo.setRemark(selectOne.getRemark() == null ? "" : selectOne.getRemark());
		vo.setStartTime(selectOne.getStartTime());
		vo.setCreateTime(selectOne.getCreateTime());
		vo.setCompName(staffOne.getCompName());
		vo.setDepName(staffOne.getDepName());
		vo.setName(staffOne.getName());
		vo.setProcessId(selectOne.getProcessId());

		List<FlowVO> flowList = new ArrayList<FlowVO>();
		//根据流程id获取流程
		if (!StringUtils.isEmpty(selectOne.getProcessId())) {
			getOAProcessFlow(selectOne.getProcessId(), flowList);
		}
		vo.setFlow(flowList);
		return new Result<>(vo);
	}


	//获取流程数据
	@Override
	public void getOAProcessFlow(String processId, List<FlowVO> list) {
		WorkFlowLogDTO workFlowLogDTO = oaWorkflowService.query(processId);
		if (ObjectUtil.isNotNull(workFlowLogDTO) && workFlowLogDTO.success()) {
			List<WorkFlowLogDataDTO> flowRecords = workFlowLogDTO.getResultdata();
			if (CollectionUtils.isNotEmpty(flowRecords)) {
				flowRecords.forEach(flowRecord -> getProcessRecord(list, flowRecord));
			}
		}
	}


	private void getProcessRecord(List<FlowVO> list, WorkFlowLogDataDTO process) {
		FlowVO flowVO = new FlowVO();
		if (StrUtil.isEmpty(process.getNODENAME())) {
			flowVO.setNodeName("");
		} else {
			String[] nodeNames = process.getNODENAME().split(" ");
			if (nodeNames.length == 2) {
				flowVO.setNodeName(nodeNames[1]);
			} else if (nodeNames.length == 1) {
				flowVO.setNodeName(nodeNames[0]);
			}
		}
		String dateTime = process.getOPERATEDATE() + " " + process.getOPERATETIME();
		if (StrUtil.isNotBlank(process.getOPERATEDATE()) && StrUtil.isNotBlank(process.getOPERATETIME())) {
			flowVO.setProcessDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
		}
		flowVO.setProcessDesc(ApplicationEnum.desc(process.getLOGTYPE()));
		flowVO.setRemark(process.getREMARK() == null ? "" : process.getREMARK());
		flowVO.setCreateUser(process.getLASTNAME() == null ? "" : process.getLASTNAME());
		list.add(flowVO);
	}


	@Override
	public void refreshOutDormitory() {
		String allowancename = DormitoryConstans.allowanceName;
		Result<CvwCcdAllowanceDTO> byName = remoteCvwCcdAllowanceService.getByName(allowancename, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		CvwCcdAllowanceDTO data = byName.getData();
		log.info("remote  remoteCvwCcdAllowanceService getByName Result=[]" + byName);
		Integer computationRule = data.getComputationRule();
		List<SmtOutDormitoryStaff> selectList = this.baseMapper.selectList(Wrappers.<SmtOutDormitoryStaff>query().lambda()
				.eq(SmtOutDormitoryStaff::getIsDelete, 0).eq(SmtOutDormitoryStaff::getStatus, OutDormitoryStatusEnum.IS_APPROVAL.getCode()));

		for (SmtOutDormitoryStaff oStaff : selectList) {
			SmtStaff selectOne = staffService.getOne(Wrappers.<SmtStaff>query().lambda()
					.eq(SmtStaff::getBadge, oStaff.getStaffBadge()));
			Result<List<OvwYsCallOwanceCancelAllRespDTO>> info = remoteOvwYsCallOwanceCancelService.getInfo(oStaff.getStaffBadge(), computationRule, oStaff.getStartTime(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			if (info.isSuccess()) {
				if (ObjectUtil.isNotNull(info.getData())) {
					if (info.getData().size() > 0) {
						oStaff.setIsDelete(1);
						oStaff.updateById();
						selectOne.setDormitoryStatus(DormitoryStatusEnum.IS_INIT.getCode());
						selectOne.updateById();
					}
				}
			}
		}

	}


	@Override
	public IPage<SearchOutDormitoryVO> getOutDormitoryPageList(Page page, SearchOutDormitoryReqDTO searchOutDormitoryReqDTO) {
		// TODO Auto-generated method stub
		SearchOutDormitoryDTO dto = new SearchOutDormitoryDTO();
		BeanUtil.copyProperties(searchOutDormitoryReqDTO, dto);
		IPage<SearchOutDormitoryVO> pageResut = this.baseMapper.getOutDormitoryPageList(page, dto);
		return pageResut;
	}


	@Override
	public OutDormitoryDetailVO getOutDormitoryDetailById(Integer id) {
		// TODO Auto-generated method stub
		SmtOutDormitoryStaff selectOne = this.baseMapper.selectById(id);
		if (ObjectUtil.isNull(selectOne)) {
			throw new TCEException("该外宿信息不存在");
		}
		OutDormitoryDetailVO vo = new OutDormitoryDetailVO();
		vo.setProcessId(selectOne.getProcessId());
		vo.setCreateTime(selectOne.getCreateTime());
		vo.setStaffBadge(selectOne.getStaffBadge());
		SmtStaff staff = staffService.getOne(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getBadge, selectOne.getStaffBadge()));
		vo.setName(staff.getName());
		vo.setAllowanceType(selectOne.getAllowanceType());
		vo.setAmount(selectOne.getAmount());
		vo.setComputaionRule(selectOne.getComputaionRule());
		vo.setEndTime(selectOne.getEndTime());
		vo.setExplain(selectOne.getExplain() == null ? "" : selectOne.getExplain());
		vo.setOutAddress(selectOne.getOutAddress());
		vo.setRemark(selectOne.getRemark() == null ? "" : selectOne.getRemark());
		vo.setStartTime(selectOne.getStartTime());
		vo.setCompName(staff.getCompName());
		vo.setDepName(staff.getDepName());
		vo.setJobName(staff.getJobName());
		List<FlowVO> flowList = new ArrayList<FlowVO>();
		//根据流程id获取流程
		if (!StringUtils.isEmpty(selectOne.getProcessId())) {
			getOAProcessFlow(selectOne.getProcessId(), flowList);
		}
		vo.setFlow(flowList);
		return vo;
	}


	@Override
	public Integer getDormitroySet() {
		// TODO Auto-generated method stub
		return conf.getDatenum();
	}


	@Override
	public Result<OutDormitoryDetailVO> outRoomApplyDetailById(String recordId) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public DormitorySituationRespDTO getOvwYsCallOwanceDetails(String staffBadge, Integer type,
															   Integer parkId, String nowDor, SmtDormitoryStaffService smtDormitoryStaffService) {
		DormitorySituationRespDTO situation = new DormitorySituationRespDTO();
		situation.setIsPass(Boolean.TRUE);
		List<DormitoryRoomDetailRespDTO> staffRooms = smtDormitoryStaffService.getStaffRoomInfoList(staffBadge);
		//查询已有宿舍
		if (CollectionUtils.isNotEmpty(staffRooms)) {
			if (syParkId.equals(parkId)) {
				//允许入住的宿舍楼
				List<String> dor = new ArrayList<String>() {{
					add("A栋");
					add("A栋一单元");
					add("A栋二单元");
					add("A栋三单元");
				}};
				List<DormitoryRoomDetailRespDTO> syRoom = staffRooms.stream().filter(s -> s.getParkId().equals(syParkId) &&
						!dor.contains(s.getDormitoryName())).collect(Collectors.toList());
				if(CollUtil.isNotEmpty(syRoom) && !dor.contains(nowDor)) {
					List<String> syRooms = new ArrayList<>();
					syRoom.forEach(sy -> {
						syRooms.add(sy.getDormitoryName() + "-" + sy.getRoomName());
					});
					String errorStr = StringUtils.join(SymbolConstants.COMMA, syRooms);
					situation.setErrorDor("已在" + errorStr + "入住，无法重复入住");
					situation.setIsPass(Boolean.FALSE);
				}
			}
			List<String> rooms = new ArrayList<>();
			staffRooms.forEach(staffRoom -> {
				rooms.add(staffRoom.getDormitoryName() + "-" + staffRoom.getRoomName());
			});
			situation.setRooms(rooms);
		}
		//查询外宿状态
		Result<OvwYsCallOwanceDetailsDTO> callOwanceDetails = ovwYsCallOwanceDetailsService.getInfo(staffBadge, type, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (Objects.isNull(callOwanceDetails.getData())) {
			situation.setIsOutDormitory(OneOrZeroEnum.ZERO.getCode());
			return situation;
		}
		situation.setIsOutDormitory(OneOrZeroEnum.ONE.getCode());
		return situation;
	}

}
