package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.data.api.dto.ehrview.OvwYsCallOwanceDetailsDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsRespDTO;
import com.tce.smart.data.api.dto.msg.req.SendCallowanceCancelReqDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwEmphrYsService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsCallOwanceDetailsService;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.platform.core.dto.AppMsgPushDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.platform.core.entity.SmtCallowanceCancelRecord;
import com.tce.smart.platform.core.entity.SmtOutDormitoryStaff;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtCallowanceCancelRecordMapper;
import com.tce.smart.platform.core.vo.CallowanceCancelInfoVO;
import com.tce.smart.platform.core.vo.CallowanceDetailVO;
import com.tce.smart.platform.core.vo.CallowanceOutDormitoryVO;
import com.tce.smart.platform.core.vo.FlowVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.DormitoryConstans;
import com.tce.smart.tool.enums.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class SmtCallowanceCancelRecordServiceImpl extends ServiceImpl<SmtCallowanceCancelRecordMapper, SmtCallowanceCancelRecord> implements SmtCallowanceCancelRecordService {


	@Autowired
	private RemoteEvwEmphrYsService evwEmphrYsService;

	@Autowired
	private SmtOutDormitoryStaffService outDormitoryStaffService;

	@Autowired
	private RemoteOvwYsCallOwanceDetailsService ovwYsCallOwanceDetailsService;

	@Autowired
	private  RemoteOaWorkFlowService remoteOaWorkFlowService;

	private final IOAWorkflowService oaWorkflowService;

	private final SmtProcessRecordService smtProcessRecordService;

	private final IAppMsgPushService appMsgPushService;

	private final SmtStaffService  smtStaffService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result save(String badge,String backDate, Integer type) {
		// TODO Auto-generated method stub

		//查询是否有未审批的单子
		List<SmtCallowanceCancelRecord> list = this.list(Wrappers.<SmtCallowanceCancelRecord>query().lambda()
				.eq(SmtCallowanceCancelRecord::getBadge, badge)
				.eq(SmtCallowanceCancelRecord::getXType, type)
				.orderByDesc(SmtCallowanceCancelRecord::getCreateTime));
		if(list.size()>0)
		{

			Boolean refuse=false;
			for (SmtCallowanceCancelRecord record : list) {

//				List<SmtProcessRecord> selectList = smtProcessRecordService.list(Wrappers.<SmtProcessRecord> query().lambda().eq(SmtProcessRecord::getProcessId, record.getProcessId()).orderByDesc(SmtProcessRecord::getRecordDate));
//				if(selectList.size()>0) {
//					//查询流程的最新的状态数据
//					if(selectList.get(0).getStatementStatus().equals(ApplicationEnum.RECORD_STATUS_c.getCode())) {
//						throw new TCEException("您的取消外宿补贴正在审批中，无需重复申请");
//					}
//				}
				List<FlowVO> flowList = new ArrayList<FlowVO> ();
				if(!StringUtils.isEmpty(record.getProcessId())) {
					//根据流程id获取流程
					getOAProcessFlow(record.getProcessId(),flowList);

					for (FlowVO flowVO : flowList) {

						if(flowVO.getProcessDesc().equals("退出")  )
						{
							refuse=true;
						}

						if(flowVO.getProcessDesc().equals(ApplicationEnum.RECORD_STATUS_13.getDesc()))
						{
							refuse=true;
							break;
						}
					}
					if(!refuse)
					{
						for (FlowVO flowVO : flowList) {

                            if (flowVO.getProcessDesc().indexOf("归档") > -1) {
                                refuse = true;
                                break;
                            }
						}
					}

				}
			}
			if(!refuse)
			{
				throw new TCEException("您的取消外宿补贴正在审批中，无需重复申请");
			}
		}

	    Result<EvwEmphrYsRespDTO> info = evwEmphrYsService.info(badge,  SecurityConstants.FROM_IN);
		if(ObjectUtil.isNull(info)||ObjectUtil.isNull(info.getData()))
		{
			throw new TCEException("未找到员工信息");
		}
		EvwEmphrYsRespDTO ehr = info.getData();
		SmtOutDormitoryStaff outDormitoryStaff = outDormitoryStaffService.getOne(Wrappers.<SmtOutDormitoryStaff>query().lambda()
				.eq(SmtOutDormitoryStaff::getStaffBadge, badge)
				.eq(SmtOutDormitoryStaff::getStatus,1)
				.eq(SmtOutDormitoryStaff::getIsDelete, 0));
		/*if(ObjectUtil.isNull(outDormitoryStaff))
		{
			throw new TCEException("您之前没有申请过外宿补贴，不需要取消");
		}*/

		Result<OvwYsCallOwanceDetailsDTO> callOwanceDetails = ovwYsCallOwanceDetailsService.getInfo(badge, 11);
		log.info("ovwYsCallOwanceDetailsService.getInfo result{}",callOwanceDetails);
		if(ObjectUtil.isNull(callOwanceDetails) ||  ObjectUtil.isNull(callOwanceDetails.getData()))
		{
			if(ObjectUtil.isNotNull(outDormitoryStaff))
			{
				outDormitoryStaff.setIsDelete(1);
				outDormitoryStaff.updateById();
			}
			throw new TCEException("您之前没有申请过外宿补贴，不需要取消");
		}

		OvwYsCallOwanceDetailsDTO ovwYsCallOwanceDetails = callOwanceDetails.getData();
		SmtCallowanceCancelRecord  record=new SmtCallowanceCancelRecord();
		record.setBadge(badge);
		record.setName(ehr.getName());
		record.setCompId(ehr.getCompID().toString());
		record.setJobId(ehr.getJobid());
		record.setDepId(ehr.getDepid().toString());
		SimpleDateFormat formatDay = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat formatMonth = new SimpleDateFormat("yyyy-MM");
		record.setStartTime(DateUtils.parse( DateUtil.format(ovwYsCallOwanceDetails.getBegindate(),formatDay), formatMonth));
		if(ObjectUtil.isNotNull(ovwYsCallOwanceDetails.getEnddate()))
		{
			record.setEndTime(DateUtils.parse(DateUtil.format(ovwYsCallOwanceDetails.getEnddate(),formatDay), formatMonth));
		}
		record.setBackDate(DateUtils.parse(backDate,formatMonth));
		record.setCreateTime(DateUtil.date());
		record.setAmount(ovwYsCallOwanceDetails.getAmount().toString());
		record.setXType(11);
		record.setIfCancel(1);
		record.setPzid(ehr.getPzid());

		if(ObjectUtil.isNotNull(callOwanceDetails)&&ObjectUtil.isNotNull(callOwanceDetails.getData()))
		{
			OvwYsCallOwanceDetailsDTO data = callOwanceDetails.getData();
			record.setAppid(data.getId());
		}
		boolean insert = record.insert();
		//如果存在外宿补贴，则要进行撤销
		if(ObjectUtil.isNotNull(callOwanceDetails)&&ObjectUtil.isNotNull(callOwanceDetails.getData()))
		{
			String processId="";
			SendCallowanceCancelReqDTO sendCallowanceCancelAo=createCallowanceCancel(record,ehr,callOwanceDetails.getData());
			log.info("remoteOaWorkFlowService.sendCallowanceCancel param{}:"+sendCallowanceCancelAo);
			Result<String> result = remoteOaWorkFlowService.sendCallowanceCancel(sendCallowanceCancelAo);
			log.info("remoteOaWorkFlowService.sendCallowanceCancel result{}:"+result);

			if (result.isSuccess()) {
				if(ObjectUtil.isNotNull(result.getData())) {
					if("-7".equals(result.getData())){
						throw new com.tce.smart.tool.exception.TCEException("获取不到OA审批人员，请联系OA管理处理后再试");
					}
					processId = result.getData();
				}else {
					this.removeById(record.getId());
					throw new TCEException("OA流程提交异常");
				}
			}else
			{
				this.removeById(record.getId());
				throw new TCEException("OA流程提交异常");
			}
			record.setProcessId(processId);
			this.updateById(record);
			getOAProcess(processId);
		}


		if(ObjectUtil.isNotNull(outDormitoryStaff))
		{
			outDormitoryStaff.setIsDelete(1);
			outDormitoryStaff.updateById();
		}

		return new Result<>(true);
	}

	private SendCallowanceCancelReqDTO createCallowanceCancel(SmtCallowanceCancelRecord record, EvwEmphrYsRespDTO ehr, OvwYsCallOwanceDetailsDTO detail) {
		// TODO Auto-generated method stub
		SendCallowanceCancelReqDTO ao=new SendCallowanceCancelReqDTO();
		if(Objects.nonNull(ehr.getEId())){
			ao.setEID(ehr.getEId());
		} else {
			ao.setEID(0);
		}
		SimpleDateFormat formatDay = new SimpleDateFormat("yyyy-MM-dd");
		ao.setAmount(Double.parseDouble(record.getAmount()));
		ao.setAPPENDDATE( DateUtils.format(detail.getEnddate(),formatDay)==null?"":DateUtils.format(detail.getEnddate(),formatDay));
		ao.setBegindate(DateUtils.format(record.getStartTime(),formatDay));
		ao.setBackdate(DateUtils.format(record.getBackDate(),formatDay));
		ao.setAPPID(detail.getId());
		ao.setBadge(record.getBadge());
		ao.setCompid(record.getCompId());
		ao.setDepid(record.getDepId());
		ao.setJobid(record.getJobId());
		ao.setIFCANCEL(record.getIfCancel());
		ao.setCOMPUTATIONRULE(detail.getComputationrule());
		ao.setName(record.getName());
		ao.setPZID(ehr.getPzid());
		ao.setREGBY(ehr.getUserid());
		ao.setRemark(record.getRemark()==null?"":record.getRemark());
		ao.setXtype(record.getXType());
		ao.setStatus(ehr.getStatus());
		ao.setJoindate(DateUtils.format(ehr.getJoindate(),formatDay));
		ao.setFLCJ(ehr.getFlcj());
		return ao;
	}

	public void getOAProcess(String processId) {
		WorkFlowLogDTO workFlowLogDTO = oaWorkflowService.query(processId);
	if(ObjectUtil.isNotNull(workFlowLogDTO) && workFlowLogDTO.success()) {
		List<WorkFlowLogDataDTO> flowRecords = workFlowLogDTO.getResultdata();
		    if(CollectionUtils.isNotEmpty(flowRecords)){
		        flowRecords.forEach(flowRecord->saveProcessRecord(processId, flowRecord));
		    }
	}
	}

	private void saveProcessRecord(String processId,WorkFlowLogDataDTO process) {
		SmtProcessRecord processRecord = smtProcessRecordService.getOne(Wrappers.<SmtProcessRecord>query().lambda()
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
			processRocord.setRemark(process.getREMARK());
			processRocord.setStaffBadge(process.getWORKCODE());
			processRocord.setStaffName(process.getLASTNAME());
			processRocord.setStatus(process.getLOGTYPE());
			smtProcessRecordService.save(processRocord);
	    }
	}

	@Override
	public void approvalNotice(String staffBadge, String code, String id, boolean flag) {
		// TODO Auto-generated method stub
		if(flag)
		{
			//当外宿取消审批通过后，删除外宿信息，并修改员工的住宿状态
			SmtOutDormitoryStaff outDormitoryStaff = outDormitoryStaffService.getOne(Wrappers.<SmtOutDormitoryStaff>query().lambda().eq(SmtOutDormitoryStaff::getStaffBadge, staffBadge).eq(SmtOutDormitoryStaff::getIsDelete, 0));
			if(ObjectUtil.isNotNull(outDormitoryStaff))
			{
				outDormitoryStaff.setIsDelete(1);
				boolean deleteById = outDormitoryStaff.updateById();
			}
			SmtStaff staff = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, staffBadge));
			if(ObjectUtil.isNotNull(staff))
			{
				staff.setDormitoryStatus(DormitoryStatusEnum.IS_INIT.getCode());
				staff.updateById();
			}
		}

		AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
		appMsgPushDTO.setBadge(staffBadge);
		appMsgPushDTO.setBussiessId(String.valueOf(id));
		appMsgPushDTO.setTemplateCode(code);
		appMsgPushService.pushAppMsg(appMsgPushDTO);

	}



	@Override
	public Result get(Integer id) {
			SmtCallowanceCancelRecord record = this.getById(id);
			SmtStaff staff = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, record.getBadge()));
			CallowanceDetailVO vo = new CallowanceDetailVO();
			BeanUtil.copyProperties(record, vo);

			SimpleDateFormat formatDay= new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat formatMoth= new SimpleDateFormat("yyyy-MM");
			vo.setJoinDate( DateUtil.format(record.getCreateTime(),formatDay));
			vo.setBackDate( DateUtil.format(record.getBackDate(),formatMoth));
			vo.setStartTime( DateUtil.format(record.getStartTime(),formatMoth));
			vo.setEndTime( DateUtil.format(record.getEndTime(),formatMoth));
			vo.setCompName(staff.getCompName());
			vo.setJobName(staff.getJobName());
			vo.setDepName(staff.getDepName());
			String allowancename = DormitoryConstans.allowanceName;
			if(record.getXType() == 10){
				allowancename = "外餐补贴";
			}
			vo.setAllowanceType(allowancename);
			vo.setJoinDate(DateUtil.format(staff.getCreateTime(), formatDay));
			vo.setStaffStatus(StaffStatusEnum.desc(staff.getStatus()));
			//根据流程id获取流程
			List<FlowVO> flowList = new ArrayList<FlowVO> ();
			if(!StringUtils.isEmpty(vo.getProcessId())) {
				getOAProcessFlow(vo.getProcessId(),flowList);
			}
			vo.setFlow(flowList);
			return new Result<>(vo);
	}

	//获取流程数据
		public void getOAProcessFlow(String processId,List<FlowVO> list) {
			WorkFlowLogDTO workFlowLogDTO = oaWorkflowService.query(processId);
		if(ObjectUtil.isNotNull(workFlowLogDTO) && workFlowLogDTO.success()) {
			List<WorkFlowLogDataDTO> flowRecords = workFlowLogDTO.getResultdata();
			    if(CollectionUtils.isNotEmpty(flowRecords)){
			        flowRecords.forEach(flowRecord->getProcessRecord(list, flowRecord));
			    }
		}
		}

		private void getProcessRecord(List<FlowVO> list,WorkFlowLogDataDTO process) {
		    FlowVO flowVO = new FlowVO ();
		    if(StrUtil.isEmpty(process.getNODENAME())) {
	             flowVO.setNodeName("");
	            }else {
	             String[] nodeNames = process.getNODENAME().split(" ");
	             if(nodeNames.length == 2) {
	              flowVO.setNodeName(nodeNames[1]);
	             }else if(nodeNames.length == 1) {
		 flowVO.setNodeName(nodeNames[0]);
	             }
	            }
			String dateTime = process.getOPERATEDATE() + " " + process.getOPERATETIME();
			if(StrUtil.isNotBlank(process.getOPERATEDATE()) && StrUtil.isNotBlank(process.getOPERATETIME())) {
				flowVO.setProcessDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
			}
			flowVO.setProcessDesc(ApplicationEnum.desc(process.getLOGTYPE()));
			flowVO.setRemark(process.getREMARK()==null?"":process.getREMARK());
			flowVO.setCreateUser(process.getLASTNAME()==null?"":process.getLASTNAME());
			list.add(flowVO);
	}

		@Override
		public Result getInfo(String badge) {
			// TODO Auto-generated method stub
			List<SmtCallowanceCancelRecord> list = this.list(Wrappers.<SmtCallowanceCancelRecord>query().lambda()
					.eq(SmtCallowanceCancelRecord::getBadge, badge)
					.orderByDesc(SmtCallowanceCancelRecord::getCreateTime));
			if(list.size()>0)
			{
				List<CallowanceCancelInfoVO> voList=new ArrayList<>();
				for (SmtCallowanceCancelRecord record : list) {
					CallowanceCancelInfoVO vo = new CallowanceCancelInfoVO();
					vo.setName(record.getName());
					vo.setId(record.getId());
					SimpleDateFormat formatDay= new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat formatMoth= new SimpleDateFormat("yyyy-MM");
					vo.setCreateTime( DateUtil.format(record.getCreateTime(),formatDay));
					vo.setBackDate( DateUtil.format(record.getBackDate(),formatMoth));
					vo.setAllowanceType(record.getXType());
					String allowancename = DormitoryConstans.allowanceName;
					if(record.getXType() == 10){
						allowancename = "外餐补贴";
					}
					vo.setAllowanceTypeName(allowancename);
//					List<SmtProcessRecord> selectList = smtProcessRecordService.list(Wrappers.<SmtProcessRecord> query().lambda().eq(SmtProcessRecord::getProcessId, record.getProcessId()).orderByDesc(SmtProcessRecord::getRecordDate));
//					if(selectList.size()>0) {
//						//查询流程的最新的状态数据
//						if(selectList.get(0).getStatementStatus().equals(ApplicationEnum.RECORD_STATUS_e.getCode()) || selectList.get(0).getStatementStatus().equals(ApplicationEnum.RECORD_STATUS_0.getCode())) {
//							vo.setProcessResult(ApplicationEnum.RECORD_STATUS_11.getDesc());
//						}else if(selectList.get(0).getStatementStatus().equals(ApplicationEnum.RECORD_STATUS_3.getCode())){
//							vo.setProcessResult(ApplicationEnum.RECORD_STATUS_12.getDesc());
//					    }else {
//					    	vo.setProcessResult("申请中");
//						}
//					}
					List<FlowVO> flowList = new ArrayList<FlowVO> ();
					if(!StringUtils.isEmpty(record.getProcessId())) {
						//根据流程id获取流程
						getOAProcessFlow(record.getProcessId(),flowList);
						Boolean refuse=false;
						vo.setProcessResult("已申请");
						for (FlowVO flowVO : flowList) {

							if(flowVO.getProcessDesc().equals("退出"))
							{
								refuse=true;
								vo.setProcessResult("已回退");
								break;
							}
							if(flowVO.getProcessDesc().equals(ApplicationEnum.RECORD_STATUS_13.getDesc()))
							{
								refuse=true;
								vo.setProcessResult(OutDormitoryStatusEnum.IS_REVOKE.getDesc());
								break;
							}
						}
						if(!refuse)
						{
							for (FlowVO flowVO : flowList) {

								if(flowVO.getProcessDesc().indexOf("归档")>-1)
								{
									vo.setProcessResult("已同意");
									break;
								}
							}
						}

					}
					voList.add(vo);
				}
				return new Result<>(voList);
			}
			return new Result<>();
		}

		@Override
		public Result getOutDormitory(String badge, Integer type) {
			// TODO Auto-generated method stub
			Result<OvwYsCallOwanceDetailsDTO> callOwanceDetails = ovwYsCallOwanceDetailsService.getInfo(badge, type);
			log.info("ovwYsCallOwanceDetailsService.getInfo result{}",callOwanceDetails);
			if(ObjectUtil.isNull(callOwanceDetails) || ObjectUtil.isNull(callOwanceDetails.getData()))
			{
				throw new TCEException("您之前没有申请过外宿补贴，不需要取消");
			}
			OvwYsCallOwanceDetailsDTO data = callOwanceDetails.getData();
			CallowanceOutDormitoryVO vo=new  CallowanceOutDormitoryVO();
			SmtStaff staff = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, badge));
			SimpleDateFormat formatDay= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat formatMoth= new SimpleDateFormat("yyyy-MM");
			vo.setJoinDate(DateUtil.format(staff.getCreateTime(), formatDay));
			vo.setStartTime( DateUtil.format(DateUtil.parse(DateUtil.format(data.getBegindate(),formatDay)),formatMoth));
			vo.setStaffStatus(StaffStatusEnum.desc(staff.getStatus()));
			String allowancename = DormitoryConstans.allowanceName;
			if(type == 10){
				allowancename = "外餐补贴";
			}
			vo.setAllowanceType(allowancename);
			vo.setAmount(data.getAmount().toString());
			return new Result<>(vo);
		}


		@Override
		public Result getCallowanceDetail(String badge, Integer type) {
			// TODO Auto-generated method stub
			//存在外宿补贴
			Result<OvwYsCallOwanceDetailsDTO> callOwanceDetails = ovwYsCallOwanceDetailsService.getInfo(badge, type);
			log.info("ovwYsCallOwanceDetailsService.getInfo result{}",callOwanceDetails);
			return callOwanceDetails;
		}
}
