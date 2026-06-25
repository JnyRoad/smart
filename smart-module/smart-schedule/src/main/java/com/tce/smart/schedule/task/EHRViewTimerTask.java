package com.tce.smart.schedule.task;

import cn.hutool.core.date.DateUtil;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.ehrview.core.entity.EvwEcdLeaintent;
import com.tce.smart.ehrview.core.entity.EvwEcdLeavereason;
import com.tce.smart.ehrview.core.entity.EvwEcdLeavetype;
import com.tce.smart.ehrview.core.service.IEvwEcdLeaintentService;
import com.tce.smart.ehrview.core.service.IEvwEcdLeavereasonService;
import com.tce.smart.ehrview.core.service.IEvwEcdLeavetypeService;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.feign.*;
import com.tce.smart.platform.api.feign.admittance.RemoteAdmittanceTaskService;
import com.tce.smart.platform.api.feign.manage.RemoteAttendanceSignService;
import com.tce.smart.platform.api.feign.manage.RemoteEhrSetUpService;
import com.tce.smart.platform.api.feign.manage.RemoteStaffRechargeService;
import com.tce.smart.platform.api.feign.news.RemoteNewsInfoService;
import com.tce.smart.schedule.config.TaskJob;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.TimerTaskEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: ViewTimerTask
 * @Author jinbo
 * @Date 2019/5/5
 */
@Slf4j
@Component
@EnableScheduling
public class EHRViewTimerTask {
	private static final long ADMITTANCE_UPDATE_OA_LOCK_MINUTES = 5L;

	@Autowired
	private RemoteDictService remoteDictService;
	@Autowired
	private RemoteNewsInfoService remoteNewsInfoService;
	@Autowired
	private IEvwEcdLeaintentService iEvwEcdLeaintentService;
	@Autowired
	private IEvwEcdLeavetypeService iEvwEcdLeavetypeService;
	@Autowired
	private RemoteVisitorTaskService remoteVisitorTaskService;
	@Autowired
	private RemoteDormitoryService remoteDormitoryService;
	@Autowired
	private IEvwEcdLeavereasonService iEvwEcdLeavereasonService;
	@Autowired
	private RemoteAttendanceApplicationService remoteAttendanceApplicationService;
	@Autowired
	private RemoteOutDormitoryStaffService remoteOutDormitoryStaffService;
	@Autowired
	private RemoteRecruitmentService remoteRecruitmentService;
	@Autowired
	private RemoteEhrSetUpService remoteEhrSetUpService;
	@Autowired
	private RemoteStaffRechargeService remoteStaffRechargeService;
	@Autowired
	private RemoteWageSignService remoteWageSignService;
	@Autowired
	private RemoteAttendanceSignService remoteAttendanceSignService;
	@Autowired
	private RemoteParkService remoteParkService;
	@Autowired
	private RemoteAdmittanceTaskService remoteAdmittanceTaskService;
	@Autowired
	private ISwitchService iSwitchService;
	@Autowired
	private TaskJob taskJob;

	/**
	 * 每1小时执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 60)
	public void leaveReasonTask() {
		if (taskJob.getLeaveReason() && iSwitchService.process(TimerTaskEnum.LEAVE_REASON)) {
			log.info("Task leaveReasonTask start time:" + DateUtil.date());
			List<EvwEcdLeavereason> evwEcdLeaveReasonList = iEvwEcdLeavereasonService.list();
			if (CollectionUtils.isNotEmpty(evwEcdLeaveReasonList)) {
				evwEcdLeaveReasonList.forEach(e -> remoteDictService.saveDict(DictConstants.LEAVE_APPLICATION_REASON,
						e.getTitle(), String.valueOf(e.getId()), SecurityConstants.FROM_IN));
			}
			log.info("Task leaveReasonTask end time:" + DateUtil.date());
		}
	}

	/**
	 * 每1小时执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 60)
	public void leaveTypeTask() {
		if (taskJob.getLeaveType() && iSwitchService.process(TimerTaskEnum.LEAVE_TYPE)) {
			log.info("Task leaveTypeTask start time:" + DateUtil.date());
			List<EvwEcdLeavetype> evwEcdLeaveTypeList = iEvwEcdLeavetypeService.list();
			if (CollectionUtils.isNotEmpty(evwEcdLeaveTypeList)) {
				evwEcdLeaveTypeList.forEach(e -> remoteDictService.saveDict(DictConstants.LEAVE_APPLICATION_TYPE,
						e.getTitle(), String.valueOf(e.getId()), SecurityConstants.FROM_IN));
			}
			log.info("Task leaveTypeTask end time:" + DateUtil.date());
		}
	}

	/**
	 * 每1小时执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 60)
	public void leaintentTask() {
		if (taskJob.getLeaveLeaintent() && iSwitchService.process(TimerTaskEnum.LEAINTENT)) {
			log.info("Task leaintentTask start time:" + DateUtil.date());
			List<EvwEcdLeaintent> evwEcdLeaintentList = iEvwEcdLeaintentService.list();
			if (CollectionUtils.isNotEmpty(evwEcdLeaintentList)) {
				evwEcdLeaintentList.forEach(e -> remoteDictService.saveDict(DictConstants.LEAINTENT_TYPE, e.getTitle()
						, String.valueOf(e.getId()), SecurityConstants.FROM_IN));
			}
			log.info("Task leaintentTask end time:" + DateUtil.date());
		}
	}

	/**
	 * 访客通知 每5分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 5)
	public void visitorTask() {
		if (taskJob.getVisitorType() && iSwitchService.process(TimerTaskEnum.VISITOR_TYPE)) {
			log.info("Task visitorTask start time:" + DateUtil.date());
			Result<List<SmtParkDTO>> result = remoteParkService.getParkList(SecurityConstants.FROM_IN);
			if (result.isSuccess()) {
				List<SmtParkDTO> parkDTOList = result.getData();
				for (SmtParkDTO smtParkDTO : parkDTOList) {
					remoteVisitorTaskService.visitorOverTime(smtParkDTO.getId(), SecurityConstants.FROM_IN);
					remoteVisitorTaskService.visitorRemind(smtParkDTO.getId(), SecurityConstants.FROM_IN);
				}
			}
			log.info("Task visitorTask end time:" + DateUtil.date());
		}
	}

	/**
	 * 访客通知 每5分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 5)
	public void admittanceTask() {
		if (taskJob.getAdmittanceRemind() && iSwitchService.process(TimerTaskEnum.ADMITTANCE_REMIND)) {
			log.info("Task admittanceTask start time:" + DateUtil.date());
			remoteAdmittanceTaskService.visitorRemind(SecurityConstants.FROM_IN);
			log.info("Task visitorTask end time:" + DateUtil.date());
		}
	}

	/**
	 * 入厂申请OA审批状态更新 每2分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 2)
	public void admittanceUpdateOaTask() {
		if (!taskJob.getAdmittanceUpdateOa()) {
			return;
		}
		String lockToken = iSwitchService.acquire(TimerTaskEnum.ADMITTANCE_UPDATE_OA,
				ADMITTANCE_UPDATE_OA_LOCK_MINUTES, TimeUnit.MINUTES);
		if (lockToken == null) {
			return;
		}
		try {
			log.info("Task admittance_update_oa_task start time:" + DateUtil.date());
			remoteAdmittanceTaskService.updateOaStatusTask(SecurityConstants.FROM_IN);
			log.info("Task admittance_update_oa_task end time:" + DateUtil.date());
		} finally {
			iSwitchService.release(TimerTaskEnum.ADMITTANCE_UPDATE_OA, lockToken);
		}
	}

	/**
	 * 访客通知 每8分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 8)
	public void admittanceComeTask() {
		if (taskJob.getAdmittanceComeOntime() && iSwitchService.process(TimerTaskEnum.ADMITTANCE_COME_ONTIME)) {
			log.info("Task admittanceTask start time:" + DateUtil.date());
			remoteAdmittanceTaskService.visitorComeOnTime(SecurityConstants.FROM_IN);
			log.info("Task visitorTask end time:" + DateUtil.date());
		}
	}

	@Scheduled(fixedDelay = 1000 * 60 * 6)
	public void admittanceOverTask() {
		if (taskJob.getAdmittanceOvertime() && iSwitchService.process(TimerTaskEnum.ADMITTANCE_OVERTIME)) {
			log.info("Task admittanceTask start time:" + DateUtil.date());
			remoteAdmittanceTaskService.visitorOverTime(SecurityConstants.FROM_IN);
			remoteAdmittanceTaskService.overTimeNoLeave(SecurityConstants.FROM_IN);
			log.info("Task visitorTask end time:" + DateUtil.date());
		}
	}

	/**
	 * 每2小时执行一次
	 */
	@Scheduled(fixedRate = 1000 * 60 * 60 * 2)
	public void visitorNoLeaveTask() {
		if (taskJob.getVisitorNoLeave() && iSwitchService.process(TimerTaskEnum.VISITOR_NO_LEAVE)) {
			log.info("Task visitorNoLeaveTask start time:" + DateUtil.date());
			Result<List<SmtParkDTO>> result = remoteParkService.getParkList(SecurityConstants.FROM_IN);
			if (result.isSuccess()) {
				List<SmtParkDTO> parkDTOList = result.getData();
				for (SmtParkDTO smtParkDTO : parkDTOList) {
					remoteVisitorTaskService.overTimeNoLeave(smtParkDTO.getId(), SecurityConstants.FROM_IN);
				}
			}
			log.info("Task visitorNoLeaveTask end time:" + DateUtil.date());
		}
	}

	/**
	 * 每1小时执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 60)
	public void newStaffRecharge() {
		if (taskJob.getNewStaffRecharge() && iSwitchService.process(TimerTaskEnum.NEW_STAFF_RECHARGE)) {
			log.info("新员工充值名单同步任务:" + DateUtil.date());
			remoteStaffRechargeService.syncNewStaff(SecurityConstants.FROM_IN);
			log.info("新员工充值名单同步任务:" + DateUtil.date());
		}
	}

	/**
	 * 每10分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 10)
	public void newsCheck() {
		if (taskJob.getNewsTerminalCheck() && iSwitchService.process(TimerTaskEnum.NEWS_TERMINAL_CHECK)) {
			log.info("终端消息发布检查任务:" + DateUtil.date());
			remoteNewsInfoService.check(SecurityConstants.FROM_IN);
			log.info("终端消息发布检查任务:" + DateUtil.date());
		}
	}


	/**
	 * 每天3点执行一次
	 */
	@Scheduled(cron = "0 0 03 * * ?")
	public void seniorStaffRecharge() {
		if (taskJob.getSeniorStaffRecharge() && iSwitchService.process(TimerTaskEnum.SENIOR_STAFF_RECHARGE)) {
			log.info("在职员工充值名单同步任务:" + DateUtil.date());
			remoteStaffRechargeService.syncSeniorStaff(SecurityConstants.FROM_IN);
			log.info("在职员工充值名单同步任务:" + DateUtil.date());
		}
	}


	/**
	 * 每天02:00分执行，清除撤销外宿审批的员工的外宿记录
	 */
	@Scheduled(cron = "0 0 02 * * ?")
	public void refreshOutDormitory() {
		if (taskJob.getOutdormitoryType() && iSwitchService.process(TimerTaskEnum.OUTDORMITORY_TYPE)) {
			log.info("Task refreshOutDormitory start time:" + DateUtil.date());
			remoteOutDormitoryStaffService.refreshOutDormitory(SecurityConstants.FROM_IN);
			log.info("Task refreshOutDormitory end time:" + DateUtil.date());
		}
	}

	/**
	 * 每天23点执行一次
	 */
	@Scheduled(cron = "${task.ehr.dealyQuit}")
	public void dealyQuit() {
		if (taskJob.getDormitoryDealyQuit() && iSwitchService.process(TimerTaskEnum.DORMITORY_DEALY_QUIT)) {
			log.info("退宿申请延迟退宿:" + DateUtil.date());
			remoteDormitoryService.dealyQuit(SecurityConstants.FROM_IN);
			log.info("退宿申请延迟退宿:" + DateUtil.date());
		}
	}

	/**
	 * 每天10:00执行，推送访客信息给指定人员email
	 */
	@Scheduled(cron = "${task.ehr.emailPush}")
	public void pushVisitorToEmail() {
		if (taskJob.getPushVisitorEmail() && iSwitchService.process(TimerTaskEnum.PUSH_VISITOR_EMAIL)) {
			log.info("推送访客信息给指定人员email:" + DateUtil.date());
			remoteVisitorTaskService.toEmail(SecurityConstants.FROM_IN);
			log.info("推送访客信息给指定人员email:" + DateUtil.date());
		}
	}

	/**
	 * 每天14:00执行，考勤消息通知
	 */
	@Scheduled(cron = "0 00 14 * * ?")
	public void attendanceNotice() {
		if (taskJob.getReplaceType() && iSwitchService.process(TimerTaskEnum.REPLACE_TYPE)) {
			log.info("Task replaceErrorTask start time:" + DateUtil.date());
			remoteAttendanceApplicationService.patchErrorPushMsg(SecurityConstants.FROM_IN);
			log.info("Task replaceErrorTask end time:" + DateUtil.date());
		}
	}

	/**
	 * 每天06:00执行，更新招聘岗位时间
	 */
	@Scheduled(cron = "0 00 06 * * ?")
	public void refreshRecruit() {
		if (taskJob.getRefreshRecruit() && iSwitchService.process(TimerTaskEnum.REFRESH_RECRUIT)) {
			log.info("Task refreshRecruit start time:" + DateUtil.date());
			remoteRecruitmentService.refreshRecruitmentById(SecurityConstants.FROM_IN);
			log.info("Task refreshRecruit end time:" + DateUtil.date());
		}
	}


	/**
	 * 每周一零点执行，更新组织信息
	 */
	@Scheduled(cron = "0 0 0 ? * MON")
	public void refreshComp() {
		if (taskJob.getRefreshComp() && iSwitchService.process(TimerTaskEnum.REFRESH_COMP)) {
			log.info("Task refreshComp start time:" + DateUtil.date());
			remoteRecruitmentService.refreshComp(SecurityConstants.FROM_IN);
			log.info("Task refreshComp end time:" + DateUtil.date());
		}
	}

	/**
	 * 每月一日零点十分执行，更新工资签收单员工信息
	 */
	@Scheduled(cron = "${task.ehr.wageSignTaskTime}")
	public void genWageSign() {
		if (taskJob.getWageSignInfo() && iSwitchService.process(TimerTaskEnum.WAGE_SIGN_INFO)) {
			log.info("Task genWageSign start time:" + DateUtil.date());
			//员工工资签单列表初始化
			remoteWageSignService.syncTask(SecurityConstants.FROM_IN);
			log.info("Task genWageSign end time:" + DateUtil.date());
		}
	}

	/**
	 * 每月一日零点五十分执行，更新考勤确认员工信息
	 */
	@Scheduled(cron = "0 50 0 16 * ?")
	public void genAttendanceSign() {
		if (taskJob.getAttendanceSignInfo() && iSwitchService.process(TimerTaskEnum.ATTENDANCE_SIGN_INFO)) {
			log.info("Task genAttendanceSign start time:" + DateUtil.date());
			//员工考勤签单列表初始化
			remoteAttendanceSignService.syncTask(SecurityConstants.FROM_IN);
			log.info("Task genAttendanceSign end time:" + DateUtil.date());
		}
	}


	/**
	 * 每天十点执行，工资签收提醒与考勤汇总确认提醒任务
	 */
	@Scheduled(cron = "${task.ehr.noticeTaskTime}")
	public void sendNotice() {
		if (taskJob.getEhrSetSendMsg() && iSwitchService.process(TimerTaskEnum.EHR_SET_SEND_MSG)) {
			log.info("Task sendNotice start time:" + DateUtil.date());
			remoteEhrSetUpService.sendMsg(SecurityConstants.FROM_IN);
			remoteEhrSetUpService.autoConfirm(SecurityConstants.FROM_IN);
			log.info("Task sendNotice end time:" + DateUtil.date());
		}
	}
}
