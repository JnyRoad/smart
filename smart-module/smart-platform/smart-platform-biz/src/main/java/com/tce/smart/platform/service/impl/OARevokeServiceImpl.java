package com.tce.smart.platform.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.service.IOARevokeService;
import com.tce.smart.platform.service.SmtProcessRecordService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.constant.ResultStatusConstants;
import com.tce.smart.tool.enums.ApplicationEnum;
import com.tce.smart.tool.enums.NodeStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * oa撤销服务实现类
 * @author fushiping
 * @date 2020/8/3 10:38
 **/
@Slf4j
@Service
public class OARevokeServiceImpl implements IOARevokeService {

	@Autowired
	private RemoteOaWorkFlowService remoteOaWorkFlowService;
	@Autowired
	private SmtProcessRecordService smtProcessRecordService;
	@Autowired
	private SmtStaffService smtStaffService;


	@Override
	public Boolean revokeProcess(Integer processId, String badege, String status) {
		if(!status.equals(ApplicationEnum.RECORD_STATUS_10.getDesc())) {
			throw  new SmartException("该审批状态已不允许撤销");
		}
		Result<Boolean> result = remoteOaWorkFlowService.sendOaRevoke(processId, badege);
		if(!result.getMessage().equals(ResultStatusConstants.SUCCESS)) {
			throw  new SmartException("OA撤销请求返回失败");
		}
		if(result.getData().equals(Boolean.FALSE)) {
			throw  new SmartException("该OA流程状态已不允许撤销");
		}
		if(result.getData().equals(Boolean.TRUE)) {
			this.saveProcessRecord(Integer.toString(processId), badege);
		}
		return null;
	}

	private void saveProcessRecord(String processId, String badege) {
		SmtProcessRecord processRecord = smtProcessRecordService.getOne(Wrappers.<SmtProcessRecord>query().lambda()
				.eq(SmtProcessRecord::getProcessId, processId)
				.eq(SmtProcessRecord::getStaffBadge, badege)
				.eq(SmtProcessRecord::getStatus, NodeStatusEnum.REVOKE.getCode()));
		//1、判重
		if(ObjectUtil.isNull(processRecord)) {
			SmtProcessRecord processRocord = new SmtProcessRecord();
			processRocord.setCreatTime(DateUtil.date());
			processRocord.setNodeName(ApplicationEnum.RECORD_STATUS_13.getDesc());
			processRocord.setProcessId(processId);
			processRocord.setRecordDate(DateUtil.date());
			processRocord.setStaffBadge(badege);
			SmtStaff staff = smtStaffService.getById(badege);
			processRocord.setStaffName(staff.getName());
			processRocord.setStatus(ApplicationEnum.RECORD_STATUS_13.getCode());
			smtProcessRecordService.save(processRocord);
		}
	}
}
