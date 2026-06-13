package com.tce.smart.platform.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.SmartEvent;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.ao.WorkFlowRecordAO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.securityarea.SmtSecurityAreaOrder;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.core.service.SmtLeaveApplicationService;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthApplyService;
import com.tce.smart.tool.enums.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: SmartEventListener
 * @Author jinbo
 * @Date 2019/5/10
 */
@Slf4j
@Component
public class LeaveApplicationListener implements ApplicationListener<SmartEvent> {

	@Autowired
	private SmtLeaveApplicationService smtLeaveApplicationService;

	@Autowired
	private SmtProcessRecordService smtProcessRecordService;

	@Autowired
	private SmtAskLeaveApplicationService smtAskLeaveApplicationService;

	@Autowired
	private SmtOvertimeApplicationService smtOvertimeApplicationService;

	@Autowired
	private SmtReplaceApplicationService smtReplaceApplicationService;

	@Autowired
	private SmtBreakoffApplicationService breakoffApplicationService;

	@Autowired
	private SmtOutDormitoryStaffService outDormitoryStaffService;

	@Autowired
	private SmtCallowanceCancelRecordService callowanceCancelRecordService;

	@Autowired
	private ILeaveApplicationService leaveApplicationService;

	@Autowired
	private SmtSecurityAreaOrderService smtSecurityAreaOrderService;

	@Autowired
	private SmtSecurityAuthApplyService smtSecurityAuthApplyService;

	@Autowired
	private SmtArticlesReleaseService smtArticlesReleaseService;

	@Autowired
	private SmtAdmittanceApplyService smtAdmittanceApplyService;

	@Autowired
	private SmtVisitorService smtVisitorService;

    @Override
    public void onApplicationEvent(SmartEvent event) {
	String message = JSONUtil.toJsonStr(event.getMessage());
	log.info("收到OA审批消息：{}" + message);
		// 由于OA系统回调地址只能配置一个，因此对于所有的OA回调消息，都往大岭山同步发送一份
		// since 2021-09-28 26:29
		// start
		String url = "http://smartapp.szyuto.com:8080/platform/oa/workflow/over";
		try {
			HttpUtil.post(url, message);
		} catch (Exception ignored) {
		}
		// end

	WorkFlowAO workFlowAO = (WorkFlowAO) event.getMessage();
	final String processId = workFlowAO.getRequestid();
	//离职
	SmtLeaveApplication leaveApplication = smtLeaveApplicationService.getOne(Wrappers.<SmtLeaveApplication>query()
				.lambda().eq(SmtLeaveApplication::getProcessId, processId));
	if(ObjectUtil.isNotNull(leaveApplication)) {
		List<WorkFlowRecordAO> flowRecords = workFlowAO.getFlowRecord();
		boolean flag = true; //是否回退
		    if(CollectionUtils.isNotEmpty(flowRecords)){
			for (WorkFlowRecordAO flowRecord : flowRecords) {
				if(flag) {
				flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
			}
			processRecord(processId, flowRecord);
				}
		    }
		    log.info("流程编号【{}】审批完成",processId);
		    if(flag) {
				leaveApplicationService.endLeaveApplication(processId);
		    }else {
				leaveApplicationService.failLeaveApplication(processId);
		    }
	}

	//请假
	SmtAskLeaveApplication askLeaveApplication = smtAskLeaveApplicationService.getOne(Wrappers.<SmtAskLeaveApplication>query().lambda().eq(SmtAskLeaveApplication::getProcessId, processId));
	if(ObjectUtil.isNotNull(askLeaveApplication)) {
		List<WorkFlowRecordAO> flowRecords = workFlowAO.getFlowRecord();
		boolean flag = true; //是否回退
		    if(CollectionUtils.isNotEmpty(flowRecords)){
			for (WorkFlowRecordAO flowRecord : flowRecords) {
				if(flag) {
				flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
			}
			processRecord(processId, flowRecord);
				}
		    }
		    log.info("流程编号【{}】审批完成",processId);
		    if(flag) {
		    smtAskLeaveApplicationService.approvalNotice(askLeaveApplication.getStaffBadge(),SmsTemplateEnum.APP_PUSH_7301.getCode(),askLeaveApplication.getId());
		    }else {
			smtAskLeaveApplicationService.approvalNotice(askLeaveApplication.getStaffBadge(),SmsTemplateEnum.APP_PUSH_7302.getCode(),askLeaveApplication.getId());
		    }
		    }
	//加班
	SmtOvertimeApplication overtimeApplication = smtOvertimeApplicationService.getOne(Wrappers.<SmtOvertimeApplication>query().lambda().eq(SmtOvertimeApplication::getProcessId, processId));
	if(ObjectUtil.isNotNull(overtimeApplication)) {
		boolean flag = true; //是否回退
		List<WorkFlowRecordAO> flowRecords = workFlowAO.getFlowRecord();
		    if(CollectionUtils.isNotEmpty(flowRecords)){
			for (WorkFlowRecordAO flowRecord : flowRecords) {
				if(flag) {
				flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
			}
			processRecord(processId, flowRecord);
				}
		    }
		log.info("流程编号【{}】审批完成",processId);
		 if(flag) {
			 smtAskLeaveApplicationService.approvalNotice(overtimeApplication.getStaffBadge(),SmsTemplateEnum.APP_PUSH_9301.getCode(),overtimeApplication.getId());
		 }else {
			 smtAskLeaveApplicationService.approvalNotice(overtimeApplication.getStaffBadge(),SmsTemplateEnum.APP_PUSH_9302.getCode(),overtimeApplication.getId());
		    }
		}
	//补卡
	SmtReplaceApplication replaceApplication = smtReplaceApplicationService.getOne(Wrappers.<SmtReplaceApplication>query().lambda().eq(SmtReplaceApplication::getProcessId, processId));
	if(ObjectUtil.isNotNull(replaceApplication)) {
		List<WorkFlowRecordAO> flowRecords = workFlowAO.getFlowRecord();
		boolean flag = true; //是否回退
		    if(CollectionUtils.isNotEmpty(flowRecords)){
			for (WorkFlowRecordAO flowRecord : flowRecords) {
				if(flag) {
				flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
			}
			processRecord(processId, flowRecord);
				}
		    }
		log.info("流程编号【{}】审批完成",processId);
		 if(flag) {
			 smtAskLeaveApplicationService.approvalNotice(replaceApplication.getStaffBadge(),SmsTemplateEnum.APP_PUSH_10301.getCode(),replaceApplication.getId());
		 }else {
			 smtAskLeaveApplicationService.approvalNotice(replaceApplication.getStaffBadge(),SmsTemplateEnum.APP_PUSH_10303.getCode(),replaceApplication.getId());
		 }
	}
	//调休
	SmtBreakoffApplication breakoffApplication = breakoffApplicationService.getOne(Wrappers.<SmtBreakoffApplication>query().lambda().eq(SmtBreakoffApplication::getProcessId, processId));
	if(ObjectUtil.isNotNull(breakoffApplication)) {
		boolean flag = true; //是否回退
		List<WorkFlowRecordAO> flowRecords = workFlowAO.getFlowRecord();
		    if(CollectionUtils.isNotEmpty(flowRecords)){
			for (WorkFlowRecordAO flowRecord : flowRecords) {
				if(flag) {
				flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
			}
			processRecord(processId, flowRecord);
				}
		    }
		log.info("流程编号【{}】审批完成",processId);
		 if(flag) {
			 smtAskLeaveApplicationService.approvalNotice(breakoffApplication.getStaffBadge(),SmsTemplateEnum.APP_PUSH_8301.getCode(),breakoffApplication.getId());
		 }else {
			 smtAskLeaveApplicationService.approvalNotice(breakoffApplication.getStaffBadge(),SmsTemplateEnum.APP_PUSH_8302.getCode(),breakoffApplication.getId());

		 }
	}

	//申请外宿
	SmtOutDormitoryStaff outDormitoryStaff = outDormitoryStaffService.getOne(Wrappers.<SmtOutDormitoryStaff>query().lambda().eq(SmtOutDormitoryStaff::getProcessId, processId));
	if(ObjectUtil.isNotNull(outDormitoryStaff)) {
		boolean flag = true; //是否回退
		List<WorkFlowRecordAO> flowRecords = workFlowAO.getFlowRecord();
		    if(CollectionUtils.isNotEmpty(flowRecords)){
			for (WorkFlowRecordAO flowRecord : flowRecords) {
				if(flag) {
				flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
			}
			processRecord(processId, flowRecord);
				}
		    }
		   log.info("流程编号【{}】审批完成",processId);
			 if(flag) {
				 //审批完成
				 if(outDormitoryStaff.getAllowanceType().equals("外宿补贴"))
					outDormitoryStaffService.approvalNotice(outDormitoryStaff.getStaffBadge(),SmsTemplateEnum.APP_PUSH_6301.getCode(),outDormitoryStaff.getId(),flag);
				 else
					outDormitoryStaffService.approvalNotice(outDormitoryStaff.getStaffBadge(),SmsTemplateEnum.APP_PUSH_6305.getCode(),outDormitoryStaff.getId(),flag);
			 }else {
				 //审批回退
				 if(outDormitoryStaff.getAllowanceType().equals("外宿补贴"))
					outDormitoryStaffService.approvalNotice(outDormitoryStaff.getStaffBadge(),SmsTemplateEnum.APP_PUSH_6302.getCode(),outDormitoryStaff.getId(),flag);
				 else
					outDormitoryStaffService.approvalNotice(outDormitoryStaff.getStaffBadge(),SmsTemplateEnum.APP_PUSH_6306.getCode(),outDormitoryStaff.getId(),flag);
			 }
	}
	//外宿补贴撤销
	SmtCallowanceCancelRecord callowanceCancelRecord = callowanceCancelRecordService.getOne(Wrappers.<SmtCallowanceCancelRecord>query().lambda().eq(SmtCallowanceCancelRecord::getProcessId, processId));
	if(ObjectUtil.isNotNull(callowanceCancelRecord)) {
		boolean flag = true; //是否回退
		List<WorkFlowRecordAO> flowRecords = workFlowAO.getFlowRecord();

		log.info("callowanceCancelRecordService-flowRecords:"+flowRecords);
		    if(CollectionUtils.isNotEmpty(flowRecords)){
			//直接判断最后一个节点
			WorkFlowRecordAO lastAo = flowRecords.get(flowRecords.size()-1);
				log.info("lastAo：",lastAo);
			if(lastAo.getLogtype().equals(NodeStatusEnum.RETURN.getCode()))
			{
				flag=false;
			}
			for (WorkFlowRecordAO flowRecord : flowRecords) {
				/*if(flag) {
				flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
			}*/
			processRecord(processId, flowRecord);
				}
		    }
		   log.info("流程编号【{}】审批完成",processId);
			 if(flag) {
				 //审批完成
				callowanceCancelRecordService.approvalNotice(callowanceCancelRecord.getBadge(),SmsTemplateEnum.APP_PUSH_6303.getCode(),callowanceCancelRecord.getId().toString(),flag);
			 }else {
				 //审批回退
				callowanceCancelRecordService.approvalNotice(callowanceCancelRecord.getBadge(),SmsTemplateEnum.APP_PUSH_6304.getCode(),callowanceCancelRecord.getId().toString(),flag);

			 }
	}

	//保密区预约
		SmtSecurityAreaOrder smtSecurityAreaOrder = smtSecurityAreaOrderService.getOne(Wrappers.<SmtSecurityAreaOrder>query().lambda().eq(SmtSecurityAreaOrder::getProcessId, processId));
		if(ObjectUtil.isNotNull(smtSecurityAreaOrder)) {
			boolean flag = true; //是否回退
			List<WorkFlowRecordAO> flowRecords = workFlowAO.getFlowRecord();
			if(CollectionUtils.isNotEmpty(flowRecords)){
				for (WorkFlowRecordAO flowRecord : flowRecords) {
					if(flag) {
						flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
					}
					processRecord(processId, flowRecord);
				}
			}
			log.info("流程编号【{}】审批完成",processId);
			if(flag) {
				//审批通过
				SecurityAreaVisitStatusEnum statusEnum = SecurityAreaVisitStatusEnum.PASSED;
			}else {
				//审批回退
				SecurityAreaVisitStatusEnum statusEnum = SecurityAreaVisitStatusEnum.RETURNED;
			}
			smtSecurityAreaOrderService.update(SmtSecurityAreaOrder.builder().status(SecurityAreaVisitStatusEnum.PASSED.getCode()).build(),Wrappers.<SmtSecurityAreaOrder>update().lambda().eq(SmtSecurityAreaOrder::getProcessId,processId));
		}

		//保密区权限申请
		SmtSecurityAuthApply authApply = smtSecurityAuthApplyService.getByProcessId(processId);
		if(Objects.nonNull(authApply)) {
			log.info("保密区权限申请收到OA推送【{}】",processId);
			//是否回退
			boolean flag = true;
			List<WorkFlowRecordAO> flowRecords = workFlowAO.getFlowRecord();
			if(CollectionUtils.isNotEmpty(flowRecords)){
				for (WorkFlowRecordAO flowRecord : flowRecords) {
					if(flag) {
						flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
					}
					processRecord(processId, flowRecord);
				}
			}
			log.info("流程编号【{}】审批完成",processId);
			if(flag) {
				//审批通过
				authApply.setOaStatus(ApproveListStateEnum.AGREE.getCode());
			}else {
				//审批回退
				authApply.setOaStatus(ApproveListStateEnum.REFUSE.getCode());
			}
			smtSecurityAuthApplyService.updateStatus(authApply);
		}

		//入厂申请
		SmtAdmittanceApply admittanceApply = smtAdmittanceApplyService.getByProcessId(processId);
		if(ObjectUtil.isNotNull(admittanceApply) && VisitorStatusEnum.Status_2.equals(admittanceApply.getStatus())) {
			log.info("入厂申请：流程编号【{}】审批开始",processId);
			//是否回退
			boolean flag = true;
			List<WorkFlowRecordAO> flowRecords = workFlowAO.getFlowRecord();
			if(CollectionUtils.isNotEmpty(flowRecords)){
				for (WorkFlowRecordAO flowRecord : flowRecords) {
					if(flag) {
						flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
					}
					processRecord(processId, flowRecord);
				}
			}
			if(flag) {
				//审批通过
				admittanceApply.setStatus(VisitorStatusEnum.Status_0.getCode());
			}else {
				//审批回退
				admittanceApply.setStatus(VisitorStatusEnum.Status_1.getCode());
			}
			smtAdmittanceApplyService.updateStatus(admittanceApply);
			log.info("入厂申请：流程编号【{}】审批完成",processId);
		}

		//HF访客预约
		SmtVisitor visitor = smtVisitorService.getOne(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getProcessId, processId));
		if(ObjectUtil.isNotNull(visitor) && VisitorStatusEnum.Status_2.equals(visitor.getStatus())) {
			log.info("合肥访客预约：流程编号【{}】审批开始",processId);
			//是否回退
			boolean flag = true;
			List<WorkFlowRecordAO> flowRecords = workFlowAO.getFlowRecord();
			if(CollectionUtils.isNotEmpty(flowRecords)){
				for (WorkFlowRecordAO flowRecord : flowRecords) {
					if(flag) {
						flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
					}
					processRecord(processId, flowRecord);
				}
			}
			if(flag) {
				//审批通过
				visitor.setStatus(VisitorStatusEnum.Status_0.getCode());
			}else {
				//审批回退
				visitor.setStatus(VisitorStatusEnum.Status_1.getCode());
			}
			smtVisitorService.updateHfStatus(visitor);
			log.info("合肥访客预约：流程编号【{}】审批完成",processId);
		}

		// 办公区物品放行
		SmtArticlesRelease articlesRelease = smtArticlesReleaseService.getOne(new LambdaQueryWrapper<SmtArticlesRelease>().eq(SmtArticlesRelease::getProcessId, processId));
		if (ObjectUtil.isNotNull(articlesRelease)) {
			List<WorkFlowRecordAO> flowRecords = workFlowAO.getFlowRecord();
			if(CollectionUtils.isNotEmpty(flowRecords)){
				for (WorkFlowRecordAO flowRecord : flowRecords) {
					processRecord(processId, flowRecord);
				}
			}
			// 是否回退
			boolean noReturn = "1".equals(workFlowAO.getStatus());
			log.info("物品放行流程编号【{}】审批完成", processId);
			// 归档
			if(noReturn) {
				//审批通过
				articlesRelease.setStatus(ArticlesReleaseStatusEnum.APPROVED.getCode());
				smtArticlesReleaseService.updateById(articlesRelease);
				// 发送APP消息通知
				callowanceCancelRecordService.approvalNotice(articlesRelease.getBadge(),SmsTemplateEnum.SMS_RELEASE_10605.getCode(),articlesRelease.getId().toString(), noReturn);
			}else {
				//审批回退
				articlesRelease.setStatus(ArticlesReleaseStatusEnum.APPROVAL_FAILED.getCode());
				smtArticlesReleaseService.updateById(articlesRelease);
				// 发送APP消息通知
				callowanceCancelRecordService.approvalNotice(articlesRelease.getBadge(),SmsTemplateEnum.SMS_RELEASE_10606.getCode(),articlesRelease.getId().toString(), noReturn);
			}
		}
    }


	private void processRecord(String processId, WorkFlowRecordAO process){
		if(!process.getLogtype().equals(NodeStatusEnum.INTERVENTION.getCode())) {
			SmtProcessRecord processRecord = smtProcessRecordService.getOne(Wrappers.<SmtProcessRecord>query().lambda()
					.eq(SmtProcessRecord::getProcessId, processId)
					.eq(SmtProcessRecord::getStaffBadge, process.getWorkcode())
					.ne(SmtProcessRecord::getStatus, NodeStatusEnum.FINISHED.getCode())
					.ne(SmtProcessRecord::getStatus, NodeStatusEnum.NOT_FINISHED.getCode()));
			//1、判重
		    if(ObjectUtil.isNull(processRecord)) {
			SmtProcessRecord processRocord = new SmtProcessRecord();
				processRocord.setCreatTime(DateUtil.date());
				processRocord.setNodeName(process.getNodename());
				processRocord.setProcessId(processId);
				String dateTime = process.getOperatedate() + " " + process.getOperatetime();
				if(StrUtil.isNotBlank(process.getOperatedate()) && StrUtil.isNotBlank(process.getOperatetime())) {
					processRocord.setRecordDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
				}
				processRocord.setRemark(htmlHandle(process.getRemark()));
				processRocord.setStaffBadge(process.getWorkcode());
				processRocord.setStaffName(process.getLastname());
				processRocord.setStatus(process.getLogtype());
				smtProcessRecordService.save(processRocord);
		    }else {
			if(processRecord.getStatus().equals(NodeStatusEnum.APPROVER.getCode())) {
				SmtProcessRecord processRocord = new SmtProcessRecord();
				processRocord.setId(processRecord.getId());
				String dateTime = process.getOperatedate() + " " + process.getOperatetime();
					if(StrUtil.isNotBlank(process.getOperatedate()) && StrUtil.isNotBlank(process.getOperatetime())) {
						processRocord.setRecordDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
					}
					processRocord.setStatus(process.getLogtype());
					processRocord.setRemark(htmlHandle(process.getRemark()));
					boolean flag = smtProcessRecordService.updateById(processRocord);
					log.info("flag:"+flag);
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
}
