package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.ExceptionType;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.consume.resp.WorkTimeDetailDTO;
import com.tce.smart.data.api.dto.ehrview.EvwCcdFlstandardDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAyearholidayRespDTO;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpService;
import com.tce.smart.data.api.feign.consume.RemoteTxEmpCardService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwCcdFlstandardService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwEmphrYsService;
import com.tce.smart.data.api.feign.ehrview.RemoteLvwAyearholidayService;
import com.tce.smart.platform.api.dto.req.LeaveHandoverSubmitReqDTO;
import com.tce.smart.platform.api.dto.resp.WorkDetailDTO;
import com.tce.smart.platform.core.ao.LeaveApplyAO;
import com.tce.smart.platform.core.dto.AppMsgPushDTO;
import com.tce.smart.platform.core.dto.LeaveApplicationDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtLeaveApplicationMapper;
import com.tce.smart.platform.core.mapper.SmtProcessRecordMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.model.YearHoliday;
import com.tce.smart.platform.core.service.SmtLeaveApplicationService;
import com.tce.smart.platform.core.vo.LeaveApplicationRecordDetailVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.ToolUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.util.ObjectUtil.isNotNull;

/**
 * 离职申请表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
@Service
@Slf4j
public class LeaveApplicationServiceImpl implements ILeaveApplicationService {

	@Autowired
	private SmtLeaveApplicationService smtLeaveApplicationService;

	@Autowired
    private SmtLeaveApplicationMapper leaveApplicationMapper;

	@Autowired
    private  RemoteEvwEmphrYsService remoteEvwEmphrYsService;

	@Autowired
    private RemoteDictService remoteDictService;

	@Autowired
    private RemoteLvwAyearholidayService remoteLvwAyearholidayService;

	@Autowired
    private  SmtProcessRecordMapper processRecordMapper;

	@Autowired
    private SmtLeaveHandoverService leaveHandoverService;

	@Autowired
    private IOAWorkflowService oaWorkflowService;

	@Autowired
    private IAppMsgPushService appMsgPushService;

	@Autowired
	private SmtLeaveApplicationMapper smtLeaveApplicationMapper;

	@Autowired
	private ApproveListService approveListService;

	@Autowired
	private SmtStaffMapper staffMapper;

	@Autowired
	private RemoteEvwCcdFlstandardService evwCcdFlstandardService;

	@Autowired
	private RemoteRsEmpService remoteRsEmpService;

	@Autowired
	private RemoteTxEmpCardService remoteTxEmpCardService;

	@Autowired
	private SmtLbejConfigService smtLbejConfigService;

	@Override
	@Transactional(rollbackFor=Exception.class)
	public Result saveLeaveApplication(LeaveApplicationDTO leaveApplicationDTO) {
		SmtLeaveApplication leaveApplication = new SmtLeaveApplication();
		BeanUtil.copyProperties(leaveApplicationDTO, leaveApplication);
		Result<EvwEmphrYsRespDTO> result = remoteEvwEmphrYsService.info(leaveApplicationDTO.getBadge(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		//log.info("=====result=======remoteEvwEmphrYsService.info======{}",result);

		if(!result.isSuccess() || ObjectUtil.isNull(result.getData())) {
			return new Result("获取员工信息异常");
		}

		EvwEmphrYsRespDTO evwEmphrYsVO;

		evwEmphrYsVO = result.getData();
		leaveApplication.setCompId(evwEmphrYsVO.getCompID());
		leaveApplication.setCompName(evwEmphrYsVO.getCompname());
		leaveApplication.setDepId(evwEmphrYsVO.getDepid());
		leaveApplication.setDepName(evwEmphrYsVO.getDepname());
		leaveApplication.setJchenId(evwEmphrYsVO.getJchenID());
		leaveApplication.setJchenName(evwEmphrYsVO.getJchenName());
		leaveApplication.setJobId(evwEmphrYsVO.getJobid());
		leaveApplication.setJobName(evwEmphrYsVO.getJobname());
		leaveApplication.setName(evwEmphrYsVO.getName());
		leaveApplication.setJoinTime(evwEmphrYsVO.getJoindate());
		leaveApplication.setEzid(evwEmphrYsVO.getEzid());//人事区域
		leaveApplication.setCreateTime(LocalDateTime.now());
		leaveApplication.setLeaveTime(leaveApplicationDTO.getLeaveTime());
		leaveApplication.setApproveStatus(ApproveListStateEnum.PENDING.getCode());
		leaveApplication.setEid(evwEmphrYsVO.getEId());
		boolean save = smtLeaveApplicationService.save(leaveApplication);
		if(save) {
			// 调用离职申请接口,获取流程编号
			LeaveApplyAO leaveApplyAO =  this.leaveApplyAO(leaveApplication);
			String processId = oaWorkflowService.leaveApply(leaveApplyAO);
			if(StrUtil.isBlank(processId)) {
				return new Result(false,"OA流程提交异常");
			}else {
				this.getOAProcess(processId);
			}
			//log.info("=====result=======processId======{}",processId);
			// 补全流程编号

			leaveApplication.setProcessId(processId);
			save = smtLeaveApplicationService.updateById(leaveApplication);
			// 写入数据库
			if(save && leaveApplication.getLeaveStatus().equals(LeaveApplicationEnum.NORMAL.getCode())){
			    return leaveHandoverService.initLeaveHandover(leaveApplication);
	        }
		}

		return new Result(ExceptionType.SERVER_ERROR);
	}

	@Override
	public void getOAProcess(String processId) {
		WorkFlowLogDTO workFlowLogDTO = oaWorkflowService.query(processId);
		//log.info("OA离职流程查询结果:({})", JSONUtil.toJsonStr(workFlowLogDTO));
	if(ObjectUtil.isNotNull(workFlowLogDTO) && workFlowLogDTO.success()) {
		List<WorkFlowLogDataDTO> flowRecords = workFlowLogDTO.getResultdata();
		    if(CollectionUtils.isNotEmpty(flowRecords)){
		        flowRecords.forEach(flowRecord->saveProcessRecord(processId, flowRecord));
		    }
	}

	}
	private void saveProcessRecord(String processId,WorkFlowLogDataDTO process) {
		if(!process.getLOGTYPE().equals(NodeStatusEnum.INTERVENTION.getCode())) {
			SmtProcessRecord processRecord = processRecordMapper.selectOne(Wrappers.<SmtProcessRecord>query().lambda()
					.eq(SmtProcessRecord::getProcessId, processId)
					.eq(SmtProcessRecord::getStaffBadge, process.getWORKCODE())
					.ne(SmtProcessRecord::getStatus, NodeStatusEnum.FINISHED.getCode())
					.ne(SmtProcessRecord::getStatus, NodeStatusEnum.NOT_FINISHED.getCode()));
			//1、判重
		    if(ObjectUtil.isNull(processRecord)) {
			SmtProcessRecord processRocord = new SmtProcessRecord();
				processRocord.setCreatTime(DateUtil.date());
				processRocord.setNodeName(process.getNODENAME());
				processRocord.setProcessId(processId);
				String dateTime = process.getOPERATEDATE() + " " + process.getOPERATETIME();
				if(StrUtil.isNotBlank(process.getOPERATEDATE()) && StrUtil.isNotBlank(process.getOPERATETIME())) {
					processRocord.setRecordDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
				}
				processRocord.setRemark(htmlHandle(process.getREMARK()));
				processRocord.setStaffBadge(process.getWORKCODE());
				processRocord.setStaffName(process.getLASTNAME());
				processRocord.setStatus(process.getLOGTYPE());
				processRecordMapper.insert(processRocord);
		    }else {
			if(processRecord.getStatus().equals(NodeStatusEnum.APPROVER.getCode())) {
				SmtProcessRecord processRocord = new SmtProcessRecord();
				processRocord.setId(processRecord.getId());
				String dateTime = process.getOPERATEDATE() + " " + process.getOPERATETIME();
					if(StrUtil.isNotBlank(process.getOPERATEDATE()) && StrUtil.isNotBlank(process.getOPERATETIME())) {
						processRocord.setRecordDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
					}
					processRocord.setStatus(process.getLOGTYPE());
					processRocord.setRemark(htmlHandle(process.getREMARK()));
					processRecordMapper.updateById(processRocord);
			}

		    }
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

	private LeaveApplyAO leaveApplyAO(SmtLeaveApplication leaveApplication) {
		LeaveApplyAO leaveApplyAO = new LeaveApplyAO();
		leaveApplyAO.setApplyTime(leaveApplication.getCreateTime());
		leaveApplyAO.setBadge(leaveApplication.getBadge());
		leaveApplyAO.setLeaitent(leaveApplication.getLeaveStatus());
		leaveApplyAO.setLeaveReason(leaveApplication.getLeaveReason());
		leaveApplyAO.setLeaveTime(leaveApplication.getLeaveTime());
		leaveApplyAO.setLeaveType(leaveApplication.getLeaveType());
		leaveApplyAO.setYearHoliday(leaveApplication.getYearHoliday());
		leaveApplyAO.setId(leaveApplication.getId());
		return leaveApplyAO;
	}

	@Override
	public List<SysDict> getLeaveType() {
		Result result = remoteDictService.findByType(DictConstants.LEAVE_APPLICATION_TYPE, SecurityConstants.FROM_IN);
		List<SysDict> list = (List<SysDict>) result.getData();
		return list;
	}

	@Override
	public List<SysDict> getLeaveReason() {
	    Result result = remoteDictService.findByType(DictConstants.LEAVE_APPLICATION_REASON, SecurityConstants.FROM_IN);
        List<SysDict> list = (List<SysDict>) result.getData();
        return list;
	}

	@Override
	public YearHoliday getYearHoliday(String badge) {
	    Result<LvwAyearholidayRespDTO> result = remoteLvwAyearholidayService.info(badge, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		LvwAyearholidayRespDTO lvwAyearholidayVO = result.getData();
	    YearHoliday yearHoliday = new YearHoliday(ObjectUtil.isNull(lvwAyearholidayVO) ? Double.valueOf(0) : ObjectUtil.isNull(lvwAyearholidayVO.getThisbalance()) ? Double.valueOf(0) : lvwAyearholidayVO.getThisbalance());
		return yearHoliday;
	}

	@Override
    public void sysnProcessRecord() {
		log.info("离职记录同步");
		List<SmtLeaveApplication> list = smtLeaveApplicationService.list(Wrappers.<SmtLeaveApplication>query().lambda().eq(SmtLeaveApplication::getApproveStatus, LeaveApplicationStatusEnum.PENDING.getCode()));
		if(CollectionUtils.isNotEmpty(list)){
			list.forEach(leaveApplication->getOAProcess(leaveApplication.getProcessId()));
		}
    }

	@Override
    public boolean endLeaveApplication(String processId) {
		 SmtLeaveApplication leaveApplication = smtLeaveApplicationMapper.selectOne(Wrappers.<SmtLeaveApplication>query().lambda().eq(SmtLeaveApplication::getProcessId, processId));
		 int num = leaveApplicationMapper.updateStatus(LeaveApplicationStatusEnum.APPROVED.getCode(), processId);
		 if(num > 0) {
			//推送App消息
				AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
				appMsgPushDTO.setBadge(leaveApplication.getBadge());
				appMsgPushDTO.setBussiessId(leaveApplication.getProcessId());
				appMsgPushDTO.setTemplateCode(SmsTemplateEnum.APP_PUSH_2303.getCode());
				appMsgPushDTO.setExtraParam("leaveStatus=" + leaveApplication.getLeaveStatus() + "||" + "approveStatus=" +  LeaveApplicationStatusEnum.APPROVED.getCode());
				boolean flg = appMsgPushService.pushAppMsg(appMsgPushDTO);
				System.out.println(flg);
		 }
        return num > 0;
    }


	@Override
    public boolean failLeaveApplication(String processId) {
		 SmtLeaveApplication leaveApplication = smtLeaveApplicationMapper.selectOne(Wrappers.<SmtLeaveApplication>query().lambda().eq(SmtLeaveApplication::getProcessId, processId));
		 int num = leaveApplicationMapper.updateStatus(LeaveApplicationStatusEnum.REJECTED.getCode(), processId);
		 if(num > 0) {
			//推送App消息
				AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
				appMsgPushDTO.setBadge(leaveApplication.getBadge());
				appMsgPushDTO.setBussiessId(leaveApplication.getProcessId());
				appMsgPushDTO.setTemplateCode(SmsTemplateEnum.APP_PUSH_2306.getCode());
				appMsgPushDTO.setExtraParam("leaveStatus=" + leaveApplication.getLeaveStatus() + "||" + "approveStatus=" +  LeaveApplicationStatusEnum.REJECTED.getCode());
				boolean flg = appMsgPushService.pushAppMsg(appMsgPushDTO);
				System.out.println(flg);
		 }
        return num > 0;
    }

	@Override
	public WorkDetailDTO getWorkDetail() {
		String userName = SecurityUtils.getUser().getUsername();
		Date searchDate = new Date();
		int today = DateUtil.dayOfMonth(searchDate);
		if(today < 6){
			//当前日期小于6 查询日期为上月1日
			searchDate = ToolUtils.setCalDate(ToolUtils.getCalDate(searchDate, Calendar.MONTH,-1),Calendar.DAY_OF_MONTH,1);
		}
		Result<List<WorkTimeDetailDTO>> empWorkDetail = remoteRsEmpService.getEmpWorkDetail(userName, searchDate,SecurityConstants.FROM_IN);
		WorkDetailDTO workDetailDTO = new WorkDetailDTO();
		List<WorkDetailDTO.WorkTimeDetail> workTimeDetails = new ArrayList<>();
		int normalTotal = empWorkDetail.getData().stream().collect(Collectors.summingInt(WorkTimeDetailDTO::getResultTotalNormalWorktime));
		workDetailDTO.setNormalTotal(normalTotal);
		empWorkDetail.getData().forEach(item -> {
			WorkDetailDTO.WorkTimeDetail workTimeDetail = new WorkDetailDTO.WorkTimeDetail();
			BeanUtils.copyProperties(item,workTimeDetail);
			workTimeDetails.add(workTimeDetail);
		});
		workDetailDTO.setWorkTimeDetails(workTimeDetails);
		return workDetailDTO;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean setWorkConnect() {
		String username = SecurityUtils.getUser().getUsername();
		//查询申请记录
		SmtLeaveApplication smtLeaveApplication = leaveApplicationMapper.selectOne(new LambdaQueryWrapper<SmtLeaveApplication>()
				.eq(SmtLeaveApplication::getApplyBadge, username)
				.eq(SmtLeaveApplication::getApproveStatus, LeaveApplicationStatusEnum.PENDING.getCode())
		);
		if(null != smtLeaveApplication){
			throw new TCEException("没有申请记录");
		}

		//修改离职申请记录状态为开始交接
		smtLeaveApplication.setApproveStatus(LeaveApplicationStatusEnum.START.getCode());
		//防并发
		int update = leaveApplicationMapper.update(smtLeaveApplication, new LambdaQueryWrapper<SmtLeaveApplication>()
				.eq(SmtLeaveApplication::getApproveStatus, LeaveApplicationStatusEnum.PENDING.getCode())
		);
		if(update != 1){
			throw new TCEException("状态异常");
		}

		//查询离职交接项
		List<SmtLeaveHandover> items = leaveHandoverService.list(Wrappers.<SmtLeaveHandover>query().lambda().eq(SmtLeaveHandover::getApplicationId,smtLeaveApplication.getId()));
		Set<String> qrrBadges = items.stream().map(SmtLeaveHandover::getQrr).collect(Collectors.toSet());
		//添加审批人待审批记录
		for(String qrrBadge : qrrBadges){
			ApproveList approveList = new ApproveList();
			approveList.setBusinessId(smtLeaveApplication.getId().toString());
			approveList.setApproveName(smtLeaveApplication.getName() + "提交的离职申请");
			approveList.setApproveType(ApproveListTypeConstants.LEAVE);
			approveList.setApproveBadge(qrrBadge);
			approveList.setApproveState(ApproveListStateEnum.PENDING.getCode());
			approveListService.saveApproveList(approveList);
		}
		return true;
	}

	@Override
	public LeaveApplicationRecordDetailVO getApproveItem(Integer id) {
		String username = SecurityUtils.getUser().getUsername();
		//查询申请记录
		SmtLeaveApplication leaveApplication = leaveApplicationMapper.selectById(id);
		if(null == leaveApplication){
			throw new TCEException("申请记录不存在");
		}

		//查询员工信息
		SmtStaff staff = staffMapper.selectOne(Wrappers.<SmtStaff> query().lambda()
				.eq(SmtStaff::getBadge, leaveApplication.getBadge()));


		//查询交接记录
		List<SmtLeaveHandover> leaveHandovers = leaveHandoverService.list(new LambdaQueryWrapper<SmtLeaveHandover>()
				.eq(SmtLeaveHandover::getApplicationId, id)
				.eq(SmtLeaveHandover::getJjr, username)
		);

		leaveHandovers.forEach(item -> {
			SmtLbejConfig lbejConfig = smtLbejConfigService.getOne(new LambdaQueryWrapper<SmtLbejConfig>()
					.eq(SmtLbejConfig::getItemId, item.getJjItemId())
					.eq(SmtLbejConfig::getDepId, staff.getDepId())
			);
			if(null != lbejConfig){
				//该交接项在伙食费配置列表中 计算伙食费
				BigDecimal calMealFee = calMealFee(staff, null, null);
				item.setJe(calMealFee.setScale(2, RoundingMode.HALF_UP).doubleValue());
			}
		});

		LeaveApplicationRecordDetailVO detailVO = LeaveApplicationRecordDetailVO.builder()
				.id(leaveApplication.getId())
				.badge(leaveApplication.getBadge())
				.name(leaveApplication.getName())
				.items(leaveHandovers)
				.build();
		detailVO.setCompName(staff.getCompName());
		detailVO.setDepName(staff.getDepName());
		detailVO.setJobName(staff.getJobName());


		Result<SysDict> sysDict = remoteDictService.findByValue(DictConstants.LEAVE_APPLICATION_REASON, leaveApplication.getLeaveReason()+"", SecurityConstants.FROM_IN);
		if(isNotNull(sysDict) && isNotNull(sysDict.getData())){
			detailVO.setLeaveReasonDesc(sysDict.getData().getLabel());
		}
		sysDict = remoteDictService.findByValue(DictConstants.LEAVE_APPLICATION_TYPE, leaveApplication.getLeaveType()+"", SecurityConstants.FROM_IN);
		if(isNotNull(sysDict) && isNotNull(sysDict.getData())){
			detailVO.setLeaveTypeDesc(sysDict.getData().getLabel());
		}

		return detailVO;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean submitItem(LeaveHandoverSubmitReqDTO submitReqDTO) {

		String username = SecurityUtils.getUser().getUsername();

		//查询离职申请记录
		SmtLeaveApplication leaveApplication = leaveApplicationMapper.selectById(submitReqDTO.getApplicationId());
		if(null == leaveApplication){
			throw new TCEException("离职申请记录不存在");
		} else if(!LeaveApplicationStatusEnum.PENDING.getCode().equals(leaveApplication.getApproveStatus())){
			throw new TCEException("状态异常");
		}

		Set<Integer> itemIds = submitReqDTO.getItems().stream().map(LeaveHandoverSubmitReqDTO.HandoverItem::getItemId).collect(Collectors.toSet());
		//查询交接项
		List<SmtLeaveHandover> leaveHandovers = leaveHandoverService.list(new LambdaQueryWrapper<SmtLeaveHandover>()
				.eq(SmtLeaveHandover::getApplicationId, submitReqDTO.getApplicationId())
				.eq(SmtLeaveHandover::getBadge, username)
				.in(SmtLeaveHandover::getJjItemId,itemIds)
		);
		if(leaveHandovers.size() != itemIds.size()){
			throw new TCEException("交接项错误");
		}

		for(int i = 0;i < submitReqDTO.getItems().size();i++){
			SmtLeaveHandover leaveHandover = new SmtLeaveHandover();
			leaveHandover.setJjClosed(LeaveHandoverEnum.ABNORMAL.getCode());
			leaveHandover.setJjClosedTime(new Date());

			leaveHandoverService.update(leaveHandover,new LambdaUpdateWrapper<SmtLeaveHandover>()
					.eq(SmtLeaveHandover::getApplicationId,submitReqDTO.getApplicationId())
					.eq(SmtLeaveHandover::getJjItemId,submitReqDTO.getItems().get(i).getItemId())
			);
		}
		return true;
	}

	/**
	 * 计算伙食费
	 * @return
	 */
	@Override
	public BigDecimal calMealFee(SmtStaff staff,Date startDate,Date endDate){
		//充值基数
		Result<EvwCcdFlstandardDTO> flstandardResult = evwCcdFlstandardService.getById(staff.getJcheId(), staff.getPzid(),SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		BigDecimal flstandard = new BigDecimal(flstandardResult.get().getStandard());
		//正班天数
		Result<Double> normalWorkDaysResult = remoteRsEmpService.getNormalWorkDays(staff.getBadge(), startDate, endDate,SecurityConstants.FROM_IN);
		Double normalWorkDays = normalWorkDaysResult.data();
		//实际出勤工时
		WorkDetailDTO workDetail = getWorkDetail();
		Integer normalTotal = workDetail.getNormalTotal();
		//实际充值金额
		Result<Double> actPutMoneyResult = remoteTxEmpCardService.getActPutMoney(staff.getBadge(), startDate, endDate,SecurityConstants.FROM_IN);
		Double actPutMoney = actPutMoneyResult.data();
		//公司充值剩余金额
		Result<Double> compBalanceResult = remoteTxEmpCardService.getCompBalance(staff.getBadge(),SecurityConstants.FROM_IN);
		Double compBalance = compBalanceResult.data();
		//个人充值剩余金额
		Result<Double> personalBalanceResult = remoteTxEmpCardService.getPersonalBalance(staff.getBadge(),SecurityConstants.FROM_IN);
		Double personalBalance = personalBalanceResult.data();
		personalBalance = (null == personalBalance ? 0 : personalBalance);

		return flstandard
				.divide(new BigDecimal(normalWorkDays),4, RoundingMode.HALF_UP)
				.multiply(new BigDecimal(normalTotal))
				.divide(new BigDecimal(8),4, RoundingMode.HALF_UP)
				.subtract(new BigDecimal(actPutMoney))
				.add(new BigDecimal(compBalance))
				.add(new BigDecimal(personalBalance));
	}

}
