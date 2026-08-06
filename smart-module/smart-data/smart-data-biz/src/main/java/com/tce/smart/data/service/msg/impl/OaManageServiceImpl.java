package com.tce.smart.data.service.msg.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.businesstrip.core.entity.FormTableMain182;
import com.tce.smart.businesstrip.core.entity.FormTableMain182Dt1;
import com.tce.smart.businesstrip.core.entity.FormTableMain182Dt2;
import com.tce.smart.businesstrip.core.service.FormTableMain182Dt1Service;
import com.tce.smart.businesstrip.core.service.FormTableMain182Dt2Service;
import com.tce.smart.businesstrip.core.service.FormTableMain182Service;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.api.dto.msg.resp.OaStaffLookupRespDTO;
import com.tce.smart.data.api.model.LeaveDetailTable;
import com.tce.smart.data.api.vo.msg.QueryOaStaffRespVo;
import com.tce.smart.data.service.msg.IOaManageService;
import com.tce.smart.data.webservice.client.WorkflowServicePortType;
import com.tce.smart.data.webservice.databinding.*;
import com.tce.smart.data.webservice.newservice.RequestInfo;
import com.tce.smart.data.webservice.newservice.RequestService;
import com.tce.smart.data.webservice.newservice.RequestServicePortType;
import com.tce.smart.tool.constant.NumberConstants;
import com.tce.smart.tool.constant.WorkFlowTypeConstants;
import com.tce.smart.tool.enums.WorkFlowTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.*;

/**
 *
 * @ClassName AppOaManagerServiceImpl.java
 * @Author mingkai.wu
 * @Date 2019-04-29 09:42
 * @Description Oa消息管理服务实现类
 */
@Service
@Slf4j
public class OaManageServiceImpl implements IOaManageService {

	@Autowired
	private WorkflowServicePortType workflowServicePortType;
	@Autowired
	private FormTableMain182Service formTableMain182Service;
	@Autowired
	private FormTableMain182Dt1Service formTableMain182Dt1Service;
	@Autowired
	private FormTableMain182Dt2Service formTableMain182Dt2Service;

	@Value("${spring.oa.staff.query-url}")
	private String queryOaStaff ;
	@Value("${spring.oa.staff.query-token}")
	private String queryOaStaffToken;

	@Override
	public Object processFlowOver(FlowOverReqDTO flowOverAo) {
		log.info("收到消息{}", flowOverAo);
		return null;
	}

	@Override
	public String sendVacate(SendVacateReqDTO sendVacateAo) {
		// 流程编号
		String processId = createWorkFLow(WorkFlowTypeConstants.VACATE_WORKFLOW_ID, sendVacateAo);

		return processId;
	}

	@Override
	public String sendRest(SendRestReqDTO sendRestAo) {
		// 流程编号
		String processId = createWorkFLow(WorkFlowTypeConstants.REST_WORKFLOW_ID, sendRestAo);

		return processId;
	}

	@Override
	public String sendExtrawork(SendExtraworkReqDTO sendExtraworkAO) {
		// 流程编号
		String processId = createWorkFLow(WorkFlowTypeConstants.EXTRAWORK_WORKFLOW_ID, sendExtraworkAO);

		return processId;
	}

	@Override
	public String sendAttendancePatchk(SendAttendancePatchkAo sendAttendancePatchkAo) {
		// 流程编号
		String processId = createWorkFLow(WorkFlowTypeConstants.ATTENDANCE_PATCH_WORKFLOW_ID, sendAttendancePatchkAo);

		return processId;
	}

	private <T> String createWorkFLow(String workFlowType, MainBaseTableReqDTO<T> mainBaseTableAo) {
		return this.createWorkFLow(workFlowType, mainBaseTableAo,null);
	}

	private <T> String createWorkFLow(String workFlowType, MainBaseTableReqDTO<T> mainBaseTableAo, List<LeaveDetailTable> detailBaseTableAos) {
		String processId = null;
		if (StringUtils.isBlank(workFlowType) || Objects.isNull(mainBaseTableAo)) {
			throw new TCEException("请求参数为空");
		}

		// 查询OA系统员工信息
		QueryOaStaffRespVo queryOaStaffRespVo = processOcrService(mainBaseTableAo.getBadge());
		if (Objects.isNull(queryOaStaffRespVo)) {
			throw new TCEException("获取OA系统员工信息异常");
		}

		WorkFlowOaTableReqDTO workFlowOaTableAo = new WorkFlowOaTableReqDTO();
		workFlowOaTableAo.setSQR(String.valueOf(queryOaStaffRespVo.getID()));// OA系统员工id
		workFlowOaTableAo.setGS(String.valueOf(queryOaStaffRespVo.getSUBCOMPANYID1()));// OA系统员工公司
		workFlowOaTableAo.setBM(String.valueOf(queryOaStaffRespVo.getDEPARTMENTID()));// OA系统员部门
		workFlowOaTableAo.setGW(String.valueOf(queryOaStaffRespVo.getJOBTITLE()));// OA系统员岗位

		try {
			processId = sendToOaWorkFlow(queryOaStaffRespVo.getID(), workFlowType, workFlowOaTableAo, mainBaseTableAo, detailBaseTableAos);
		} catch (RemoteException e) {
			log.error("与OA系统通行异常", e);
			throw new TCEException("与OA系统通行异常");
		}

		return processId;
	}

	@Override
	public OaStaffLookupRespDTO getOAInfoByBadge(String badge) {
		QueryOaStaffRespVo oaStaff = processOcrService(badge);
		if (Objects.isNull(oaStaff)) {
			return null;
		}
		OaStaffLookupRespDTO lookupRespDTO = new OaStaffLookupRespDTO();
		lookupRespDTO.setId(oaStaff.getID());
		lookupRespDTO.setName(oaStaff.getLASTNAME());
		return lookupRespDTO;
	}

	private QueryOaStaffRespVo processOcrService(String badge) {
		QueryOaStaffRespVo queryOaStaffRespVo = null;

		if (StringUtils.isNotEmpty(badge)) {

			try {
				HttpEntity<String> requestEntity = new HttpEntity<>(null, null);
			     ResponseEntity<String> exchange = new RestTemplate().exchange(queryOaStaff + "?WorkCode=" + badge+ "&TokenID=" + queryOaStaffToken,
						 HttpMethod.GET, requestEntity, String.class);
			     String responseEntity = exchange.getBody();

				log.debug("OA 员工信息查询已收到上游响应");
				if (Objects.isNull(responseEntity)) {
					throw new TCEException("OA系统员工信息查询异常");
				}

				JSONObject respObject = JSONUtil.parseObj(responseEntity);
				if (0 != respObject.getInt("errorcode")) {
					throw new TCEException("询OA系统员工信息查失败");
				} else {
					JSONArray jsonSONArray = respObject.getJSONArray("resultdata");
					if (Objects.nonNull(jsonSONArray)) {
						queryOaStaffRespVo = JSONUtil.toBean(JSONUtil.parseObj(jsonSONArray.get(0)),
								QueryOaStaffRespVo.class);
					}
				}
			} catch (Exception e) {
				log.error("查询OA系统员工信息失败", e);
				throw new TCEException("查询OA系统员工信息失败");
			}

		}
		return queryOaStaffRespVo;
	}


	/**
	 * 构造创建工作段消息体
	 *
	 * @param oaUserId          Oa系统申请人编号
	 * @param workflowId        工作流编号：请假 34901，加班 35021，调休 35141，补卡 35101，离职 34461
	 * @param workFlowOaTableAo OA系统审批字段
	 * @param mainBaseTableAo   EHR表字段
	 * @return 审批流程编号
	 * @throws RemoteException 服务通讯异常
	 */
	private <T,K> String sendToOaWorkFlow(Integer oaUserId, String workflowId, WorkFlowOaTableReqDTO workFlowOaTableAo,
										MainBaseTableReqDTO<T> mainBaseTableAo, List<K> detailBaseTableAos) throws RemoteException {

		WorkflowRequestInfo workflowRequestInfo = new WorkflowRequestInfo();// 工作流程请求信息

		// 请求id
		String requestId = mainBaseTableAo.getBadge() + '-' + System.currentTimeMillis() +
                '-' + RandomUtil.randomInt(3);

		String workName = WorkFlowTypeEnum.desc(workflowId);
		//区分外宿和外餐标题后，将错误的外餐code替换为正确的
		if(workflowId.equals(WorkFlowTypeEnum.OUTDORMITORY_FOOD_WORKFLOW_ID.getCode())) {
			workflowId = WorkFlowTypeEnum.OUTDORMITORY_WORKFLOW_ID.getCode();
		}
		// 请求标题
		String workFlowName = "tce" + '-' + mainBaseTableAo.getName() + '-' +
                mainBaseTableAo.getBadge() + workName;

		workflowRequestInfo.setCanView(true);// 显示
		workflowRequestInfo.setCanEdit(true);// 可编辑
		workflowRequestInfo.setRequestId(requestId);// 请求id
		workflowRequestInfo.setRequestName(workFlowName);// 请求标题
		workflowRequestInfo.setRequestLevel("0");// 请求重要级别
		workflowRequestInfo.setCreatorId(String.valueOf(oaUserId));

		WorkflowBaseInfo workflowBaseInfo = new WorkflowBaseInfo();// 工作流信息
		workflowBaseInfo.setWorkflowId(workflowId);// 流程ID-请假
		workflowBaseInfo.setWorkflowName(workFlowName);// 流程名称
		workflowBaseInfo.setWorkflowTypeName(workFlowName);// 流程类型名称
		workflowRequestInfo.setWorkflowBaseInfo(workflowBaseInfo);// 工作流信息

		/**************** main table start *************/
		// EHR审批需要字段
		List<WorkflowRequestTableField> tableFieldList = new ArrayList<WorkflowRequestTableField>();

		setTableField(tableFieldList, new BeanMap(workFlowOaTableAo));
		setTableField(tableFieldList, new BeanMap(mainBaseTableAo));

		WorkflowRequestTableRecord[] workflowRequestTableRecord = new WorkflowRequestTableRecord[] {
				new WorkflowRequestTableRecord(null,
						tableFieldList.toArray(new WorkflowRequestTableField[tableFieldList.size()])) };// 主表字段


		WorkflowMainTableInfo workflowMainTableInfo = new WorkflowMainTableInfo(workflowRequestTableRecord, null);// 主表

		workflowRequestInfo.setWorkflowMainTableInfo(workflowMainTableInfo);
		/**************** main table end *************/

		/**************** detail table start *************/
		if(ObjectUtil.isNotNull(detailBaseTableAos)){
			List<WorkflowRequestTableRecord> workflowRequestDetailTableRecords = new ArrayList<WorkflowRequestTableRecord>();
			detailBaseTableAos.forEach(detatilBase -> this.detailTable(workflowRequestDetailTableRecords, detatilBase));
			WorkflowRequestTableRecord[] workflowRequestTableRecordArray =workflowRequestDetailTableRecords.toArray(new WorkflowRequestTableRecord[workflowRequestDetailTableRecords.size()]);
			WorkflowDetailTableInfo workflowDetailTableInfo = new WorkflowDetailTableInfo(null, null, null, workflowRequestTableRecordArray);// 详情表
			WorkflowDetailTableInfo[] workflowDetailTableInfos = new WorkflowDetailTableInfo[] {workflowDetailTableInfo};
			workflowRequestInfo.setWorkflowDetailTableInfos(workflowDetailTableInfos);
		}
		/**************** detail table end *************/

		// 发送OA创建审批
		log.info("====doCreateWorkflowRequest[{}]=======request={}", requestId, JSONUtil.toJsonStr(workflowRequestInfo));
		String response = workflowServicePortType.doCreateWorkflowRequest(workflowRequestInfo, oaUserId);
		log.info("====doCreateWorkflowRequest[{}]=======response={}", requestId, response);

		return response;
	}

	/**
	 * 构造创建工作段消息体 -- 多张明细表的情况
	 *
	 * @param oaUserId          Oa系统申请人编号
	 * @param workflowId        工作流编号：请假 34901，加班 35021，调休 35141，补卡 35101，离职 34461
	 * @param workFlowOaTableAo OA系统审批字段
	 * @param mainBaseTableAo   EHR表字段
	 * @return 审批流程编号
	 * @throws RemoteException 服务通讯异常
	 */
	private <T,K> String sendToOaWorkFlowMore(Integer oaUserId, String workflowId, WorkFlowOaTableReqDTO workFlowOaTableAo,
										  MainBaseTableReqDTO<T> mainBaseTableAo, WorkflowDetailTableInfo[] workflowDetailTableInfos, String visitorName) throws RemoteException {

		WorkflowRequestInfo workflowRequestInfo = new WorkflowRequestInfo();// 工作流程请求信息

		// 请求id
		String requestId = mainBaseTableAo.getBadge() + '-' + System.currentTimeMillis() +
                '-' + RandomUtil.randomInt(3);

		String workName = WorkFlowTypeEnum.desc(workflowId);
		String workFlowName;
		if(WorkFlowTypeEnum.SECURITY_AUTH_APPLY_ID.getCode().equals(workflowId)) {
			workFlowName = "XCAJ02-许昌裕同保密权限申请表-" + mainBaseTableAo.getName() + '-' +
                    DateUtils.convert("yyyy/MM/dd", LocalDateTime.now());
		}else if(WorkFlowTypeEnum.ENTRY_FACTORY_APPLY_ID.getCode().equals(workflowId)){
			workFlowName = visitorName;
		} else if (WorkFlowTypeEnum.RELEASE_APPLY_ID.getCode().equals(workflowId)) {
			workFlowName = "XCAJ03-许昌裕同放行条-" + mainBaseTableAo.getName() + '-' +
                    DateUtils.convert("yyyy/MM/dd", LocalDateTime.now());
		} else {
			workFlowName = "tce" + '-' + mainBaseTableAo.getName() + '-' +
                    mainBaseTableAo.getBadge() + workName;
		}
		workflowRequestInfo.setCanView(true);// 显示
		workflowRequestInfo.setCanEdit(true);// 可编辑
		workflowRequestInfo.setRequestId(requestId);// 请求id
		workflowRequestInfo.setRequestName(workFlowName);// 请求标题
		workflowRequestInfo.setRequestLevel("0");// 请求重要级别
		workflowRequestInfo.setCreatorId(String.valueOf(oaUserId));

		WorkflowBaseInfo workflowBaseInfo = new WorkflowBaseInfo();// 工作流信息
		workflowBaseInfo.setWorkflowId(workflowId);// 流程ID-请假
		workflowBaseInfo.setWorkflowName(workFlowName);// 流程名称
		workflowBaseInfo.setWorkflowTypeName(workFlowName);// 流程类型名称
		workflowRequestInfo.setWorkflowBaseInfo(workflowBaseInfo);// 工作流信息

		/**************** main table start *************/
		// EHR审批需要字段
		List<WorkflowRequestTableField> tableFieldList = new ArrayList<WorkflowRequestTableField>();

		setTableField(tableFieldList, new BeanMap(workFlowOaTableAo));
		setTableField(tableFieldList, new BeanMap(mainBaseTableAo));

//		printOARequestLog(tableFieldList, "主表数据");

		WorkflowRequestTableRecord[] workflowRequestTableRecord = new WorkflowRequestTableRecord[] {
				new WorkflowRequestTableRecord(null,
						tableFieldList.toArray(new WorkflowRequestTableField[tableFieldList.size()])) };// 主表字段


		WorkflowMainTableInfo workflowMainTableInfo = new WorkflowMainTableInfo(workflowRequestTableRecord, null);// 主表

		workflowRequestInfo.setWorkflowMainTableInfo(workflowMainTableInfo);
		/**************** main table end *************/

		/**************** detail table start *************/
		if(CollectionUtils.isNotEmpty(workflowDetailTableInfos)){
//			Arrays.stream(workflowDetailTableInfos).forEach(field ->
//					Arrays.stream(field.getWorkflowRequestTableRecords()).forEach(record ->
//							printOARequestLog(Arrays.asList(record.getWorkflowRequestTableFields()), "详情表数据")));
			workflowRequestInfo.setWorkflowDetailTableInfos(workflowDetailTableInfos);
		}
		/**************** detail table end *************/

		// 发送OA创建审批
		log.info("====doCreateWorkflowRequest[{}]=======request={}", requestId, JSONUtil.toJsonStr(workflowRequestInfo));
		String response = workflowServicePortType.doCreateWorkflowRequest(workflowRequestInfo, oaUserId);
		log.info("====doCreateWorkflowRequest[{}]=======response={}", requestId, response);

		return response;
	}

	private void printOARequestLog(List<WorkflowRequestTableField> tableFieldList, String title) {
		tableFieldList.forEach(field -> log.info("{}：FieldName:{},FieldValue:{},FieldType:{}", title, field.getFieldName(), field.getFieldValue(), field.getFieldType()));
	}

	private <T> void detailTable(List<WorkflowRequestTableRecord> workflowRequestDetailTableRecords, T detatilBase) {
		List<WorkflowRequestTableField> detailtableFieldList = new ArrayList<WorkflowRequestTableField>();
		setTableField(detailtableFieldList, new BeanMap(detatilBase));
		workflowRequestDetailTableRecords.add(new WorkflowRequestTableRecord(null,
				detailtableFieldList.toArray(new WorkflowRequestTableField[detailtableFieldList.size()])));
	}

	private void setTableField(List<WorkflowRequestTableField> tableFieldList, Map<?, ?> fieldMap) {
		if (CollectionUtils.isEmpty(fieldMap)) {
			throw new TCEException("WorkflowRequestTableField 字段为空,请检查");
		}
		List<String> list = new ArrayList<String>() {{
			add("xgzj");
			add("fjsc");
			add("zjfj");
			add("fkzp");
			add("zjzp");

			add("xcm2");
			add("jkm2");
			add("zjzp2");
			add("fkzp2");
		}};
		WorkflowRequestTableField workflowRequestTableField = null;
		for (Object key : fieldMap.keySet()) {
			workflowRequestTableField = new WorkflowRequestTableField();
			String name = String.valueOf(key);
			String value = String.valueOf(fieldMap.get(key));
			workflowRequestTableField.setFieldName(name);// 文档
			workflowRequestTableField.setFieldValue(value);
			workflowRequestTableField.setView(true);
			workflowRequestTableField.setEdit(true);
			if (workflowRequestTableField.getFieldValue() != null && list.contains(name)) {
				workflowRequestTableField.setFieldType(workflowRequestTableField.getFieldValue().startsWith("https")
						? "https:" : workflowRequestTableField.getFieldValue().startsWith("http") ? "http:" : null);
			}
			tableFieldList.add(workflowRequestTableField);
		}

	}

	@Override
	public String sendOutDormitoryAo(SendOutDormitoryReqDTO sendOutDormitoryAo) {
		String code = WorkFlowTypeEnum.OUTDORMITORY_WORKFLOW_ID.getCode();
		if(sendOutDormitoryAo.getXtype().equals(10)) {
			code = WorkFlowTypeEnum.OUTDORMITORY_FOOD_WORKFLOW_ID.getCode();
		}
		return createWorkFLow(code, sendOutDormitoryAo);
}

	@Override
	public String sendLeaveApplication(SendLeaveApplicationReqDTO sendLeaveApplicationAo) {
		return createWorkFLow(WorkFlowTypeEnum.DIMISSION_WORKFLOW_ID.getCode(), sendLeaveApplicationAo.getMainTable(),sendLeaveApplicationAo.getDetailTable());
	}


	@Override
	public String sendCallowanceCancel(SendCallowanceCancelReqDTO sendCallowanceCancelAo) {
		return createWorkFLow(WorkFlowTypeEnum.CALLOWANCE_CANCEL_ID.getCode(), sendCallowanceCancelAo);
	}

	@Override
	public boolean sendOaRevoke(Integer processId, String badge) {
		//获得撤销接口
		RequestServicePortType requestServicePortType = this.getPortType();
		//查询OA系统员工信息
		QueryOaStaffRespVo oaStaff = this.processOcrService(badge);
		if(Objects.nonNull(oaStaff)) {
			Boolean result = requestServicePortType.nextNodeByReject(processId, oaStaff.getID(), "TCE-TEST-申请人撤回");
			//Boolean result = requestServicePortType.nextNodeByReject(processId, oaStaff.getID(), "TCE-TEST-申请人撤回");
			return result;
		}
		throw new SmartException("获取OA系统员工信息失败");
	}

	/**
	 * 获得oa撤销接口
	 * @return
	 */
	private RequestServicePortType getPortType() {
		try {
			return new RequestService(new URL("http://10.0.20.69/services/RequestService?wsdl")).getRequestServiceHttpPort();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}



	@Override
	public String sendSecurityareaVisit(SendSecurityAreaVisitReqDTO sendSecurityAreaVisitReqDTO) {
		// 流程编号
		String processId = "";

		SecurityAreaVisitMainReqDTO securityAreaVisitMainReqDTO = sendSecurityAreaVisitReqDTO.getSecurityAreaVisitMainReqDTO();

		// 查询OA系统员工信息
		QueryOaStaffRespVo queryOaStaffRespVo = processOcrService(securityAreaVisitMainReqDTO.getBadge());
		if (Objects.isNull(queryOaStaffRespVo)) {
			throw new TCEException("获取OA系统员工信息异常");
		}

		WorkFlowOaTableReqDTO workFlowOaTableAo = new WorkFlowOaTableReqDTO();
		workFlowOaTableAo.setSQR(String.valueOf(queryOaStaffRespVo.getID()));// OA系统员工id
		workFlowOaTableAo.setGS(String.valueOf(queryOaStaffRespVo.getSUBCOMPANYID1()));// OA系统员工公司
		workFlowOaTableAo.setBM(String.valueOf(queryOaStaffRespVo.getDEPARTMENTID()));// OA系统员部门
		workFlowOaTableAo.setGW(String.valueOf(queryOaStaffRespVo.getJOBTITLE()));// OA系统员岗位

		securityAreaVisitMainReqDTO.setShenqr(String.valueOf(queryOaStaffRespVo.getID()));
		securityAreaVisitMainReqDTO.setShenqrbm(String.valueOf(queryOaStaffRespVo.getDEPARTMENTID()));

		//List<LeaveDetailTable>

		try {
			processId = sendToOaWorkFlow(queryOaStaffRespVo.getID(), WorkFlowTypeEnum.SECURITY_AREA_VISIT_ID.getCode(), workFlowOaTableAo, securityAreaVisitMainReqDTO, sendSecurityAreaVisitReqDTO.getSecurityAreaVisitDetailReqDTOs());
		} catch (RemoteException e) {
			log.error("与OA系统通信异常", e);
			throw new TCEException("与OA系统通信异常");
		}

		return processId;
	}

	@Override
	public String sendSecurityAuthApply(SendSecurityAuthApplyReqDTO sendSecurityAuthApplyReqDTO) {
		// 流程编号
		String processId;
		SecurityAuthApplyMainReqDTO securityAuthApplyMainReqDTO = sendSecurityAuthApplyReqDTO.getSecurityAuthApplyMainReqDTO();
		// 查询OA系统员工信息
		QueryOaStaffRespVo queryOaStaffRespVo = processOcrService(securityAuthApplyMainReqDTO.getBadge());
		if (Objects.isNull(queryOaStaffRespVo)) {
			throw new TCEException("获取OA系统员工信息异常");
		}
		WorkFlowOaTableReqDTO workFlowOaTableAo = getWorkFlowOaTableAo(queryOaStaffRespVo);
		securityAuthApplyMainReqDTO.setSqr(String.valueOf(queryOaStaffRespVo.getID()));
		securityAuthApplyMainReqDTO.setSqbm(String.valueOf(queryOaStaffRespVo.getDEPARTMENTID()));
		securityAuthApplyMainReqDTO.setJobid(String.valueOf(queryOaStaffRespVo.getJOBTITLE()));
		securityAuthApplyMainReqDTO.setDepid(String.valueOf(queryOaStaffRespVo.getDEPARTMENTID()));
		if(CollectionUtils.isNotEmpty(sendSecurityAuthApplyReqDTO.getSecurityAuthApplyDetailReqDTOs())) {
			for(SecurityAuthApplyDetailReqDTO person : sendSecurityAuthApplyReqDTO.getSecurityAuthApplyDetailReqDTOs()) {
				QueryOaStaffRespVo personOa = processOcrService(person.getSqrgh());
				if (Objects.isNull(queryOaStaffRespVo)) {
					throw new TCEException("获取OA系统员工信息异常");
				}
				person.setSqrbm(String.valueOf(personOa.getDEPARTMENTID()));
				person.setSqrzw(String.valueOf(personOa.getJOBTITLE()));
				person.setSqrxm(String.valueOf(personOa.getID()));
			}
		}
		try {
			List<WorkflowDetailTableInfo> workflowDetailTableInfoList = new ArrayList<>();
			WorkflowDetailTableInfo tableInfo1 = getWorkflowDetailTableInfo(sendSecurityAuthApplyReqDTO.getSecurityAuthApplyDetailReqDTOs());
			WorkflowDetailTableInfo tableInfo2 = getWorkflowDetailTableInfo(sendSecurityAuthApplyReqDTO.getSecurityAuthApplyDetailAreaReqDTOS());
			if(Objects.nonNull(tableInfo1)) {
				workflowDetailTableInfoList.add(tableInfo1);
			}
			if(Objects.nonNull(tableInfo2)) {
				workflowDetailTableInfoList.add(tableInfo2);
			}
			WorkflowDetailTableInfo[] workflowDetailTableInfos = new WorkflowDetailTableInfo[workflowDetailTableInfoList.size()];
			workflowDetailTableInfoList.toArray(workflowDetailTableInfos);
			processId = sendToOaWorkFlowMore(queryOaStaffRespVo.getID(), WorkFlowTypeEnum.SECURITY_AUTH_APPLY_ID.getCode(),
					workFlowOaTableAo, securityAuthApplyMainReqDTO, workflowDetailTableInfos, null);
		} catch (RemoteException e) {
			log.error("与OA系统通信异常", e);
			throw new TCEException("与OA系统通信异常");
		}

		return processId;
	}

	@Override
	public String sendEntryFactoryApply(SendEntryFactoryApplyReqDTO sendEntryFactoryApplyReqDTO) {
		// 流程编号
		String processId;
		EntryFactoryApplyMainReqDTO entryFactoryApplyMainReqDTO = sendEntryFactoryApplyReqDTO.getEntryFactoryApplyMainReqDTO();
		// 查询OA系统员工信息
		QueryOaStaffRespVo queryOaStaffRespVo = processOcrService(entryFactoryApplyMainReqDTO.getBadge());
		if (Objects.isNull(queryOaStaffRespVo)) {
			throw new TCEException("获取OA系统员工信息异常");
		}
		String visitorName = sendEntryFactoryApplyReqDTO.getEntryFactoryApplyLongDetailReqDTOs().get(0).getXm();
		//默认由ehr01账号申请
		WorkFlowOaTableReqDTO workFlowOaTableAo = new WorkFlowOaTableReqDTO();
		workFlowOaTableAo.setSQR("3041254");
		workFlowOaTableAo.setGS("901");
		workFlowOaTableAo.setBM("33742");
		workFlowOaTableAo.setGW("37682");
		entryFactoryApplyMainReqDTO.setSqr("3041254");
		entryFactoryApplyMainReqDTO.setSqbm("33742");
		entryFactoryApplyMainReqDTO.setJobid("37682");
		entryFactoryApplyMainReqDTO.setDepid("33742");
/*		WorkFlowOaTableReqDTO workFlowOaTableAo = new WorkFlowOaTableReqDTO();
		workFlowOaTableAo.setSQR(String.valueOf(queryOaStaffRespVo.getID()));
		workFlowOaTableAo.setGS(String.valueOf(queryOaStaffRespVo.getSUBCOMPANYID1()));
		workFlowOaTableAo.setBM(String.valueOf(queryOaStaffRespVo.getDEPARTMENTID()));
		workFlowOaTableAo.setGW(String.valueOf(queryOaStaffRespVo.getJOBTITLE()));
		entryFactoryApplyMainReqDTO.setSqr(String.valueOf(queryOaStaffRespVo.getID()));
		entryFactoryApplyMainReqDTO.setSqbm(String.valueOf(queryOaStaffRespVo.getDEPARTMENTID()));
		entryFactoryApplyMainReqDTO.setJobid(String.valueOf(queryOaStaffRespVo.getJOBTITLE()));
		entryFactoryApplyMainReqDTO.setDepid(String.valueOf(queryOaStaffRespVo.getDEPARTMENTID()));*/

		sendEntryFactoryApplyReqDTO.getEntryFactoryApplyShortDetailReqDTOs().get(0).setGhao(String.valueOf(queryOaStaffRespVo.getID()));
		sendEntryFactoryApplyReqDTO.getEntryFactoryApplyShortDetailReqDTOs().get(0).setZhiww(String.valueOf(queryOaStaffRespVo.getJOBTITLE()));
		entryFactoryApplyMainReqDTO.setQyjdr(String.valueOf(queryOaStaffRespVo.getID()));
		try {
			List<WorkflowDetailTableInfo> workflowDetailTableInfoList = new ArrayList<>();
			WorkflowDetailTableInfo tableInfo1 = getWorkflowDetailTableInfo(sendEntryFactoryApplyReqDTO.getEntryFactoryApplyShortDetailReqDTOs());
			WorkflowDetailTableInfo tableInfo2 = getWorkflowDetailTableInfo(sendEntryFactoryApplyReqDTO.getEntryFactoryApplyLongDetailReqDTOs());
			WorkflowDetailTableInfo tableInfo3 = getWorkflowDetailTableInfo(sendEntryFactoryApplyReqDTO.getEntryFactoryApplyCarDetailReqDTOs());
			if(Objects.nonNull(tableInfo1)) {
				workflowDetailTableInfoList.add(tableInfo1);
			}
			if(Objects.nonNull(tableInfo2)) {
				workflowDetailTableInfoList.add(tableInfo2);
			}
			if(Objects.nonNull(tableInfo3)) {
				workflowDetailTableInfoList.add(tableInfo3);
			}
			WorkflowDetailTableInfo[] workflowDetailTableInfos = new WorkflowDetailTableInfo[workflowDetailTableInfoList.size()];
			workflowDetailTableInfoList.toArray(workflowDetailTableInfos);
			//判断是否为车辆预约
			StringBuilder workFlowName = new StringBuilder();
			if(StrUtil.isEmpty(sendEntryFactoryApplyReqDTO.getEntryFactoryApplyMainReqDTO().getSqjrqy())) {
				workFlowName = new StringBuilder("XCWL-货车预约申请-").append(visitorName)
						.append('-').append(entryFactoryApplyMainReqDTO.getName())
						.append('-').append(DateUtils.convert("yyyy/MM/dd",LocalDateTime.now()));
			}else {
				workFlowName = new StringBuilder("XCAJ01-许昌裕同访客申请单-").append(visitorName)
						.append('-').append(entryFactoryApplyMainReqDTO.getName())
						.append('-').append(DateUtils.convert("yyyy/MM/dd",LocalDateTime.now()));
			}
			processId = sendToOaWorkFlowMore(queryOaStaffRespVo.getID(), WorkFlowTypeEnum.ENTRY_FACTORY_APPLY_ID.getCode(),
					workFlowOaTableAo, entryFactoryApplyMainReqDTO, workflowDetailTableInfos, workFlowName.toString());
		} catch (RemoteException e) {
			log.error("与OA系统通信异常", e);
			throw new TCEException("与OA系统通信异常");
		}

		return processId;
	}

	@Override
	public String sendVisitApply(SendVisitApplyReqDTO sendVisitApplyReqDTO) {
		// 流程编号
		String processId;
		VisitApplyMainReqDTO visitApplyMainReqDTO = sendVisitApplyReqDTO.getVisitApplyMainReqDTO();
		// 查询OA系统员工信息
		QueryOaStaffRespVo queryOaStaffRespVo = processOcrService(visitApplyMainReqDTO.getBadge());
		if (Objects.isNull(queryOaStaffRespVo)) {
			throw new TCEException("获取OA系统员工信息异常");
		}
		//默认由ehr01账号申请
		WorkFlowOaTableReqDTO workFlowOaTableAo = new WorkFlowOaTableReqDTO();
		workFlowOaTableAo.setSQR("3041254");
		workFlowOaTableAo.setGS("901");
		workFlowOaTableAo.setBM("33742");
		workFlowOaTableAo.setGW("37682");
		visitApplyMainReqDTO.setSqrxm("3041254");
		visitApplyMainReqDTO.setSqrgh("ehr01");
		visitApplyMainReqDTO.setSqrbm("33742");
		visitApplyMainReqDTO.setJobid("37682");
		visitApplyMainReqDTO.setDepid("33742");
		visitApplyMainReqDTO.setName(queryOaStaffRespVo.getLASTNAME());
		visitApplyMainReqDTO.setCompid(queryOaStaffRespVo.getSUBCOMPANYID1().toString());
		visitApplyMainReqDTO.setBfr(queryOaStaffRespVo.getID().toString());
		try {
			List<WorkflowDetailTableInfo> workflowDetailTableInfoList = new ArrayList<>();
			WorkflowDetailTableInfo tableInfo1 = getWorkflowDetailTableInfo(sendVisitApplyReqDTO.getVisitApplyPersonReqDTOS());
			if(Objects.nonNull(tableInfo1)) {
				workflowDetailTableInfoList.add(tableInfo1);
			}
			log.info("【合肥访客提交附表】:{}", workflowDetailTableInfoList);
			log.info("【合肥访客提交主表】:{}", visitApplyMainReqDTO);
			WorkflowDetailTableInfo[] workflowDetailTableInfos = new WorkflowDetailTableInfo[workflowDetailTableInfoList.size()];
			workflowDetailTableInfoList.toArray(workflowDetailTableInfos);
			processId = sendToOaWorkFlowMore(queryOaStaffRespVo.getID(), WorkFlowTypeEnum.HF_VISIT_ID.getCode(),
					workFlowOaTableAo, visitApplyMainReqDTO, workflowDetailTableInfos, null);
		} catch (RemoteException e) {
			log.error("与OA系统通信异常", e);
			throw new TCEException("与OA系统通信异常");
		}

		return processId;
	}

	@Override
	public String sendReleaseApply(SendReleaseApplyReqDTO sendReleaseApplyReqDTO) {
		// 流程编号
		String processId;
		ReleaseApplyMainReqDTO releaseApplyMainReqDTO = sendReleaseApplyReqDTO.getReleaseApplyMainReqDTO();
		// 查询OA系统员工信息
		QueryOaStaffRespVo queryOaStaffRespVo = processOcrService(releaseApplyMainReqDTO.getBadge());
		if (Objects.isNull(queryOaStaffRespVo)) {
			throw new TCEException("获取OA系统员工信息异常");
		}
		WorkFlowOaTableReqDTO workFlowOaTableAo = getWorkFlowOaTableAo(queryOaStaffRespVo);

		releaseApplyMainReqDTO.setSqr(String.valueOf(queryOaStaffRespVo.getID()));
		releaseApplyMainReqDTO.setSqbm(String.valueOf(queryOaStaffRespVo.getDEPARTMENTID()));

		try {
			List<WorkflowDetailTableInfo> workflowDetailTableInfoList = new ArrayList<>();
			WorkflowDetailTableInfo tableInfo1 = getWorkflowDetailTableInfo(sendReleaseApplyReqDTO.getReleaseApplyPersonDetailReqDTOs());
			WorkflowDetailTableInfo tableInfo2 = getWorkflowDetailTableInfo(sendReleaseApplyReqDTO.getReleaseApplyThingDetailReqDTOs());
			if(Objects.nonNull(tableInfo1)) {
				workflowDetailTableInfoList.add(tableInfo1);
			}
			if(Objects.nonNull(tableInfo2)) {
				workflowDetailTableInfoList.add(tableInfo2);
			}
			WorkflowDetailTableInfo[] workflowDetailTableInfos = new WorkflowDetailTableInfo[workflowDetailTableInfoList.size()];
			workflowDetailTableInfoList.toArray(workflowDetailTableInfos);
			processId = sendToOaWorkFlowMore(queryOaStaffRespVo.getID(), WorkFlowTypeEnum.RELEASE_APPLY_ID.getCode(),
					workFlowOaTableAo, releaseApplyMainReqDTO, workflowDetailTableInfos, null);
		} catch (RemoteException e) {
			log.error("与OA系统通信异常", e);
			throw new TCEException("与OA系统通信异常");
		}

		return processId;
	}

	@Override
	public Boolean sendWriteBackReturnTime(SendWriteBackReturnTimeReqDTO reqDTO) {
		FormTableMain182 formTableMain182 = formTableMain182Service.getByRequestId(reqDTO.getRequestid());
		if(Objects.nonNull(formTableMain182)) {
			// 是否返厂：0、是；1、否
			if(NumberConstants.ZERO.equals(formTableMain182.getSffc())) {
				// 放行事项 == 人员放行，人员放行（仅出差使用）
				if(0 == formTableMain182.getFxsx() || 7 == formTableMain182.getFxsx()) {
					List<FormTableMain182Dt1> dt1List = formTableMain182Dt1Service.getByMainId(formTableMain182.getId());
					if(CollectionUtils.isNotEmpty(dt1List)) {
						for (FormTableMain182Dt1 dt1 : dt1List) {
							dt1.setFcrq(reqDTO.getFcrq());
							dt1.setFcsj(reqDTO.getFcsj());
						}
						formTableMain182Dt1Service.updateFcsj(dt1List);
					}
				} else {
					List<FormTableMain182Dt2> dt2List = formTableMain182Dt2Service.getByMainId(formTableMain182.getId());
					if(CollectionUtils.isNotEmpty(dt2List)) {
						for (FormTableMain182Dt2 dt2 : dt2List) {
							dt2.setWpfcrq(reqDTO.getFcrq());
							dt2.setWpfcsj(reqDTO.getFcsj());
						}
						formTableMain182Dt2Service.updateFcsj(dt2List);
					}
				}
			}
			return Boolean.TRUE;
		} else {
			log.error("申请单{}不存在", reqDTO.getRequestid());
			return Boolean.FALSE;
		}
	}

	@Override
	public Boolean sendSecurityApproval(SendSecurityApprovalReqDTO sendSecurityApprovalReqDTO) {
		// 发送OA创建审批
		//获得撤销接口
		RequestServicePortType requestServicePortType = this.getPortType();
		RequestInfo requestInfo = new RequestInfo();
		int requestId = Integer.parseInt(sendSecurityApprovalReqDTO.getRequestid());
		int userId = Integer.parseInt(sendSecurityApprovalReqDTO.getUserid());
		Boolean response = requestServicePortType.nextNodeBySubmit(requestInfo, requestId, userId, sendSecurityApprovalReqDTO.getRemark());
		log.info("====nextNodeBySubmit[{}]=======response={}", sendSecurityApprovalReqDTO.getRequestid(), response);
		return response;
	}

	private WorkFlowOaTableReqDTO getWorkFlowOaTableAo(QueryOaStaffRespVo queryOaStaffRespVo) {
		WorkFlowOaTableReqDTO workFlowOaTableAo = new WorkFlowOaTableReqDTO();
		// OA系统员工id
		workFlowOaTableAo.setSQR(String.valueOf(queryOaStaffRespVo.getID()));
		// OA系统员工公司
		workFlowOaTableAo.setGS(String.valueOf(queryOaStaffRespVo.getSUBCOMPANYID1()));
		// OA系统员部门
		workFlowOaTableAo.setBM(String.valueOf(queryOaStaffRespVo.getDEPARTMENTID()));
		// OA系统员岗位
		workFlowOaTableAo.setGW(String.valueOf(queryOaStaffRespVo.getJOBTITLE()));
		return workFlowOaTableAo;
	}

	private <K> WorkflowDetailTableInfo getWorkflowDetailTableInfo(List<K> detailBaseTableAos) {
		if(Objects.nonNull(detailBaseTableAos)) {
			List<WorkflowRequestTableRecord> workflowRequestDetailTableRecords = new ArrayList<WorkflowRequestTableRecord>();
			detailBaseTableAos.forEach(detatilBase -> this.detailTable(workflowRequestDetailTableRecords, detatilBase));
			WorkflowRequestTableRecord[] workflowRequestTableRecordArray =workflowRequestDetailTableRecords.toArray(new WorkflowRequestTableRecord[workflowRequestDetailTableRecords.size()]);
			// 详情表
			return new WorkflowDetailTableInfo(null, null, null, workflowRequestTableRecordArray);
		} else {
			return null;
		}
	}
}
