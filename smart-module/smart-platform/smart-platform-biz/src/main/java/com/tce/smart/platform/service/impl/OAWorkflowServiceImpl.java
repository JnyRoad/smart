package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.HttpUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.data.api.dto.msg.req.SendLeaveApplicationReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendLeaveMainTableReqDTO;
import com.tce.smart.data.api.dto.ehrview.EvwEappraisDTO;
import com.tce.smart.data.api.dto.ehrview.resp.YsLeaveRespDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwEmphrYsService;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.data.api.model.LeaveDetailTable;
import com.tce.smart.platform.core.ao.LeaveApplyAO;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.model.LeaveMainTable;
import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.tool.constant.IFPYConstants;
import com.tce.smart.tool.constant.LeaveApplicationConstants;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.ZoneId;
import java.util.*;

/**
 *
 * @ClassName AppOaManagerService.java
 * @Author mingkai.wu
 * @Date 2019-04-29 09:43
 * @Description
 */
@Slf4j
@Service
public class OAWorkflowServiceImpl implements IOAWorkflowService {
	private static final int OA_WORKFLOW_LOG_TIMEOUT_MILLIS = 5000;

	@Value("${spring.oa.process.request.log-token}")
	private String logToken;
	@Value("${spring.oa.process.request.log-url}")
	private String logUrl;
	@Autowired
	private RemoteOaWorkFlowService remoteOaWorkFlowService;

	@Autowired
	private RemoteEvwEmphrYsService remoteEvwEmphrYsService;

	@Override
	public String leaveApply(LeaveApplyAO leaveApplyAO) {
		String badge = leaveApplyAO.getBadge();

		SendLeaveMainTableReqDTO sendLeaveMainTableAo = new SendLeaveMainTableReqDTO();
		sendLeaveMainTableAo.setSeqid(leaveApplyAO.getId());
		sendLeaveMainTableAo.setIsEndCon(1);
		LeaveMainTable leaveMainTable = new LeaveMainTable();
		List<LeaveDetailTable> leaveDetailTableList = new ArrayList<LeaveDetailTable>();

		leaveMainTable.setLeavetype(leaveApplyAO.getLeaveType()+"");
		leaveMainTable.setLeavereason(leaveApplyAO.getLeaveReason()+"");
		leaveMainTable.setLEAINTENT(LeaveApplicationConstants.ACTIVE+"");
		leaveMainTable.setApplyDate(DateUtil.format(Date.from(leaveApplyAO.getApplyTime().atZone(ZoneId.systemDefault()).toInstant()), "yyyy-MM-dd"));
		leaveMainTable.setLeavedate(DateUtil.format(leaveApplyAO.getLeaveTime(), "yyyy-MM-dd"));
		leaveMainTable.setYEARDAY(leaveApplyAO.getYearHoliday()+"");
		leaveMainTable.setType(leaveApplyAO.getLeaitent());

		fillLeaveTable(badge, sendLeaveMainTableAo, leaveMainTable, leaveDetailTableList);
		return leaveOfApply(badge, sendLeaveMainTableAo, leaveMainTable, leaveDetailTableList);
	}

	private void fillLeaveTable(String badge, SendLeaveMainTableReqDTO sendLeaveMainTableAo, LeaveMainTable leaveMainTable, List<LeaveDetailTable> leaveDetailTableList) {
		Result<YsLeaveRespDTO> remoteResult = remoteEvwEmphrYsService.leave(badge, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (!remoteResult.isSuccess()) {
			log.warn("查询员工信息失败：Badge = {}", badge);
			return;
		}

		if (ObjectUtil.isNotNull(remoteResult.getData())) {
			YsLeaveRespDTO evwEmphrYs = remoteResult.getData();

			sendLeaveMainTableAo.setBadge(badge);
			sendLeaveMainTableAo.setCompid(evwEmphrYs.getCompId() + "");
			sendLeaveMainTableAo.setDepid(evwEmphrYs.getDepId() + "");
			sendLeaveMainTableAo.setJobid(evwEmphrYs.getJobId());
			sendLeaveMainTableAo.setName(evwEmphrYs.getName());

			leaveMainTable.setBadge(badge);
			leaveMainTable.setName(evwEmphrYs.getName());
			leaveMainTable.setCompid(evwEmphrYs.getCompId() + "");
			leaveMainTable.setDepid(evwEmphrYs.getDepId() + "");
			leaveMainTable.setJobid(evwEmphrYs.getJobId());
			leaveMainTable.setJchenid(evwEmphrYs.getJchenId() + "");
			leaveMainTable.setJoindate(DateUtil.format(evwEmphrYs.getJoinDate(), "yyyy-MM-dd"));
			leaveMainTable.setJxianid(evwEmphrYs.getJxianId() + "");
			leaveMainTable.setIFPY(IFPYConstants.FALSE);
			leaveMainTable.setISCGPY(evwEmphrYs.getIsCgpy());
			leaveMainTable.setEzid(evwEmphrYs.getEzId() + "");
			leaveMainTable.setEID(evwEmphrYs.getEId() + "");

			leaveMainTable.setNEWCOMPANY("");
			leaveMainTable.setNEWJOB("");
			leaveMainTable.setNEWSALARY("");
			if (CollectionUtils.isNotEmpty(remoteResult.getData().getEvwEapprais())) {
				leaveMainTable.setIFPY(IFPYConstants.TRUE);

				List<EvwEappraisDTO> evwEappraisList = remoteResult.getData().getEvwEapprais();
				LeaveDetailTable leaveDetailTable = null;
				for (EvwEappraisDTO element : evwEappraisList) {
					leaveDetailTable = new LeaveDetailTable();

					leaveDetailTable.setBadge(StringUtils.isNotBlank(element.getBadge()) ? element.getBadge() : "");
					leaveDetailTable.setName(StringUtils.isNotBlank(element.getName()) ? element.getName() : "");
					leaveDetailTable.setCompid(StringUtils.isNotBlank(element.getCompID()) ? element.getCompID() : "");
					leaveDetailTable.setDepid(StringUtils.isNotBlank(element.getDepID()) ? element.getDepID() : "");
					leaveDetailTable.setJobid(StringUtils.isNotBlank(element.getJobID()) ? element.getJobID() : "");
					leaveDetailTable.setJchenid(StringUtils.isNotBlank(element.getJchenid()) ? element.getJchenid() : "");
					leaveDetailTable.setJoindate(StringUtils.isNotBlank(element.getJoindate()) ? element.getJoindate() : "");
					leaveDetailTable.setPrize(StringUtils.isNotBlank(element.getPrize()) ? element.getPrize() : "");
					leaveDetailTable.setEffectdate(StringUtils.isNotBlank(element.getEffectdate()) ? element.getEffectdate() : "");

					leaveDetailTableList.add(leaveDetailTable);
				}
			}else {
				LeaveDetailTable leaveDetailTable = new LeaveDetailTable();
				leaveDetailTable.setBadge("");
				leaveDetailTable.setName("");
				leaveDetailTable.setCompid("");
				leaveDetailTable.setDepid("");
				leaveDetailTable.setJobid("");
				leaveDetailTable.setJchenid("");
				leaveDetailTable.setJoindate("");
				leaveDetailTable.setPrize("");
				leaveDetailTable.setEffectdate("");
				leaveDetailTableList.add(leaveDetailTable);
			}
		}

	}

	/**
	 * 发送离职申请
	 * @param badge badge
	 * @param sendLeaveMainTableAo sendLeaveMainTableAo
	 * @param leaveMainTable leaveMainTable
	 * @param leaveDetailTableList leaveDetailTableList
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public String leaveOfApply(String badge, SendLeaveMainTableReqDTO sendLeaveMainTableAo, LeaveMainTable leaveMainTable, List<LeaveDetailTable> leaveDetailTableList) {

		List<LeaveMainTable> LeaveMainTableList = new ArrayList<>();
		LeaveMainTableList.add(leaveMainTable);
		List<LeaveDetailTable> LeaveDetailTableList = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(leaveDetailTableList)) {
			LeaveDetailTableList = leaveDetailTableList;
		}

		SendLeaveApplicationReqDTO sendLeaveApplicationAo = new SendLeaveApplicationReqDTO();
		BeanUtil.copyProperties(leaveMainTable, sendLeaveMainTableAo);
		sendLeaveApplicationAo.setMainTable(sendLeaveMainTableAo);
		sendLeaveApplicationAo.setDetailTable(LeaveDetailTableList);

		log.info("===========leaveOfApply======sendLeaveApplicationAo====={}",JSONUtil.toJsonPrettyStr(sendLeaveApplicationAo));

		Result<String> result = remoteOaWorkFlowService.sendLeaveApplication(sendLeaveApplicationAo);
		if(Objects.nonNull(result) && result.isSuccess()){
			if("-7".equals(result.getData())){
				throw new TCEException("获取不到OA审批人员，请联系OA管理处理后再试");
			}
			return result.getData();
		}
		log.warn("向OA发送离职申请审批：{} - {}", result.getCode(), result.getMsg());
		return null;
	}

	@Override
	public WorkFlowLogDTO query(String requestId) {
		Map<String, String> param = new HashMap<>();
		param.put("requestid", requestId);
		param.put("TokenID", logToken);
		String newUri = UriComponentsBuilder.fromHttpUrl(logUrl).replaceQuery(HttpUtil.toParams(param)).build(true).toString();
		HttpResponse response = HttpUtils.createGet(newUri).timeout(OA_WORKFLOW_LOG_TIMEOUT_MILLIS).execute();
		return HttpUtils.parse(response, WorkFlowLogDTO.class);
	}
}
