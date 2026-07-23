package com.tce.smart.platform.service.impl;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.data.api.dto.msg.req.SendRestReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.platform.core.dto.AddBreakOffApplicationDTO;
import com.tce.smart.platform.core.dto.SearchBreakOffDTO;
import com.tce.smart.platform.core.dto.SearchPatchDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.platform.core.entity.SmtAskLeaveApplication;
import com.tce.smart.platform.core.entity.SmtBreakoffApplication;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.model.SearchBreakoffApplicationDetail;
import com.tce.smart.platform.core.vo.EmployeeBreakOffVO;
import com.tce.smart.platform.core.vo.FlowVO;
import com.tce.smart.platform.core.vo.SearchBreakOffTypeVO;
import com.tce.smart.platform.core.vo.SearchBreakoffApplicationVO;
import com.tce.smart.platform.core.mapper.SmtBreakoffApplicationMapper;
import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.platform.service.SmtAskLeaveApplicationService;
import com.tce.smart.platform.service.SmtBreakoffApplicationService;
import com.tce.smart.platform.service.SmtProcessRecordService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.ApplicationEnum;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.enums.NodeStatusEnum;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYscompService;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 调休申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:30:08
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtBreakoffApplicationServiceImpl extends ServiceImpl<SmtBreakoffApplicationMapper, SmtBreakoffApplication> implements SmtBreakoffApplicationService {
	private final SmtStaffService smtStaffService;
	private final RemoteDictService remoteDictService;
	private final SmtProcessRecordService smtProcessRecordService;
    private final IOAWorkflowService oaWorkflowService;
	private final RemoteOaWorkFlowService remoteOaWorkFlowService;
	private final RemoteOvwYscompService remoteOvwYscompService;

	private final SmtAskLeaveApplicationService  smtAskLeaveApplicationService;

	/**
	 * 添加调休申请
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveBreakoffApplication(AddBreakOffApplicationDTO addBreakoffApplicationDTO) {
		//判断参数是否为空值
		if(StringUtils.isEmpty(addBreakoffApplicationDTO)){
			 throw new TCEException(ExceptionTypeEnum.BREAK_OFF_PARAMETER);
		}
		//正则判断
		ExceptionTypeEnum exceptionType = BreakoffCheck(addBreakoffApplicationDTO);
		if(!exceptionType.equals(ExceptionTypeEnum.CHECK_SUCCESS)){
            throw new TCEException(exceptionType);
		}
		if(Double.parseDouble(addBreakoffApplicationDTO.getTermCount()) < Double.parseDouble(addBreakoffApplicationDTO.getRestCount()))
		{
			 throw new TCEException("出勤日期只有"+addBreakoffApplicationDTO.getTermCount()+"天，调休类型不能选择全天，请选择上下午");
		}
		//判断这天是否调休过，是否被退回
		List<SmtBreakoffApplication> list = this.baseMapper.selectList(
					Wrappers.<SmtBreakoffApplication> query().lambda()
					.eq(SmtBreakoffApplication::getStaffBadge, addBreakoffApplicationDTO.getStaffBadge())
					.eq(SmtBreakoffApplication::getRestTime,DateUtils.parse(addBreakoffApplicationDTO.getRestDate()))
					);

		for (SmtBreakoffApplication smtBreakoffApplication : list) {
			if(!StringUtils.isEmpty(smtBreakoffApplication.getProcessId())) {
				List<FlowVO> flowList = new ArrayList<FlowVO> ();
				getOAProcessFlow(smtBreakoffApplication.getProcessId(),flowList);
				log.info("【调休OA返回：】{}", flowList);
				Integer back=0;
			    if(flowList.size()>0) {
				for (FlowVO flowVO : flowList) {
						if(flowVO.getProcessDesc().equals("退出"))
						{
							back+=1;
						}
					}
			    }
			    if(back==0 && (smtBreakoffApplication.getType().equals(Integer.parseInt(addBreakoffApplicationDTO.getRestType())) || smtBreakoffApplication.getType()==1 ||Integer.parseInt(addBreakoffApplicationDTO.getRestType())==1) )
			    {

				throw new TCEException(addBreakoffApplicationDTO.getRestDate()+"已申请过调休，不能重复申请");
			    }
			}
		}

		//判断这个时间段是否有请假，并且没有没有被回退  lt小于 ge大于
		String start="",end="";
		String error="";
		if(addBreakoffApplicationDTO.getRestType().equals("1"))
		{
			error="全天";
			start=addBreakoffApplicationDTO.getRestDate()+" 08:00:00";
			end=addBreakoffApplicationDTO.getRestDate()+" 17:30:00";

		}else if (addBreakoffApplicationDTO.getRestType().equals("2"))
		{
			error="上午";
			start=addBreakoffApplicationDTO.getRestDate()+" 08:00:00";
			end=addBreakoffApplicationDTO.getRestDate()+" 12:00:00";

		}
		else if (addBreakoffApplicationDTO.getRestType().equals("3"))
		{
			error="下午";
			start=addBreakoffApplicationDTO.getRestDate()+" 13:30:00";
			end=addBreakoffApplicationDTO.getRestDate()+" 17:30:00";

		}
		List<SmtAskLeaveApplication> askList = smtAskLeaveApplicationService.list(Wrappers.<SmtAskLeaveApplication> query().lambda()
						.eq(SmtAskLeaveApplication::getStaffBadge, addBreakoffApplicationDTO.getStaffBadge())
						.ge(SmtAskLeaveApplication::getEndTime,DateUtils.parse(start))
						.lt(SmtAskLeaveApplication::getStartTime,DateUtils.parse(end)));

		for (SmtAskLeaveApplication smtAskLeaveApplication : askList) {

			if(!StringUtils.isEmpty(smtAskLeaveApplication.getProcessId())) {
				List<FlowVO> flowList = new ArrayList<FlowVO> ();
				getOAProcessFlow(smtAskLeaveApplication.getProcessId(),flowList);
				Integer back=0;
					  if(flowList.size()>0) {
					   for (FlowVO flowVO : flowList) {
						if(flowVO.getProcessDesc().equals("退出"))
						{
									back+=1;
						}
					}
				 }
				if(back==0)
				{
					 throw new TCEException(addBreakoffApplicationDTO.getRestDate()+"的"+error+"已申请过请假，不能再申请调休");
				}
			}
		}

		SmtBreakoffApplication breakoffApplication = getBreakoffApplication(addBreakoffApplicationDTO);
		//添加进入数据库
		boolean save = this.save(breakoffApplication);
		if(save) {
			String str="-7";
			String processId = getProcessId(addBreakoffApplicationDTO, breakoffApplication.getId());
			if(str.equals(processId)){
				throw new TCEException("获取不到OA审批人员，请联系OA管理处理后再试");
			}
			//调用调休申请 获取流程的id
			breakoffApplication.setProcessId(processId);
			//修改数据，添加流程id
			boolean updateById = this.updateById(breakoffApplication);
			if(updateById) {
				getOAProcess(breakoffApplication.getProcessId());
			}
		}
	}
	/**
	 * 获取审批流程id
	 */
	public String getProcessId(AddBreakOffApplicationDTO addBreakoffApplicationDTO,Integer id) {
		String processId = "";
		//调休审批表
		SendRestReqDTO sendRestAo = new SendRestReqDTO();
		//获取员工的信息
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,addBreakoffApplicationDTO.getStaffBadge()));
		sendRestAo.setBadge(selectOne.getBadge());
		sendRestAo.setName(selectOne.getName());
		sendRestAo.setCompid(selectOne.getCompId());
		sendRestAo.setDepid(selectOne.getDepId());
		sendRestAo.setJchenid(selectOne.getJcheId());
		if(!Objects.isNull(selectOne.getEId())) {
			sendRestAo.setEid(selectOne.getEId().toString());
		} else {
			sendRestAo.setEid("");
		}
		sendRestAo.setJobid(selectOne.getJobId());
		sendRestAo.setTwid(addBreakoffApplicationDTO.getRestType());
		sendRestAo.setTermid(addBreakoffApplicationDTO.getTermId());
		sendRestAo.setOLDBEGINTIME(addBreakoffApplicationDTO.getTerm());
		sendRestAo.setPERIOD(addBreakoffApplicationDTO.getRestAbleCount());
		sendRestAo.setOLDAMOUNT(addBreakoffApplicationDTO.getRestCount());
		sendRestAo.setBEGINTIME(addBreakoffApplicationDTO.getRestDate());
		sendRestAo.setRemark(addBreakoffApplicationDTO.getVacateDesc());
		sendRestAo.setSeqid(id.toString());
		//时间长度
		sendRestAo.setAmount(addBreakoffApplicationDTO.getRestCount());
		//获取人事区域
		Result<OvwYscompRespDTO> resultComp = remoteOvwYscompService.getByCompId(selectOne.getCompId(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		OvwYscompRespDTO ovwYscompVO = resultComp.getData();
		sendRestAo.setEzid(ovwYscompVO.getEzid().toString());
		sendRestAo.setUnit("1");
		log.info("---------------------");
		log.info(sendRestAo.toString());
		//获取流程id
		Result<String> result = remoteOaWorkFlowService.sendRest(sendRestAo);
		if (CommonConstants.SUCCESS  == result.getCode()) {
			if(ObjectUtil.isNotNull(result.getData())) {

				processId = result.getData();
				if("-7".equals(processId)){
					throw new TCEException("获取不到OA审批人员，请联系OA管理处理后再试");
				}
			}else {
				this.removeById(id);
				throw new TCEException("OA流程提交异常");

			}
		}else {
			this.removeById(id);
			throw new TCEException("申请失败，请重新操作");
		}
		return processId;
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
	/**
	 * 正则判断
	 * @param addBreakoffApplicationDTO
	 * @return
	 */
	private ExceptionTypeEnum BreakoffCheck(AddBreakOffApplicationDTO addBreakoffApplicationDTO){
		String vacateType = addBreakoffApplicationDTO.getRestType();
		String restCount = addBreakoffApplicationDTO.getRestCount();
/*		String vacateDesc = addBreakoffApplicationDTO.getVacateDesc();
*/		String staffBadge = addBreakoffApplicationDTO.getStaffBadge();
		String termId = addBreakoffApplicationDTO.getTermId();
		String term = addBreakoffApplicationDTO.getTerm();

		if(StringUtils.isEmpty(termId)){
			return ExceptionTypeEnum.BREAK_OFF_TYPE_TERMID;
		}
		if(StringUtils.isEmpty(term)){
			return ExceptionTypeEnum.BREAK_OFF_TYPE_WORK;
		}
		if(StringUtils.isEmpty(vacateType)){
			return ExceptionTypeEnum.BREAK_OFF_TYPE_PARAMETER;
		}
		if(StringUtils.isEmpty(restCount)){
			return ExceptionTypeEnum.BREAK_OFF_REST_COUNT_NULL;
		}
		if(StringUtils.isEmpty(staffBadge)){
			return ExceptionTypeEnum.ASK_LEAVE_STAFF_BADGE_NULL;
		}
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,staffBadge));
		if(selectOne==null){
			return ExceptionTypeEnum.BREAK_OFF_STAFF_BADGE_PARAMETER;
		}
		return ExceptionTypeEnum.CHECK_SUCCESS;
	}
	/**
	 * 获取调休
	 */
	public SmtBreakoffApplication getBreakoffApplication(AddBreakOffApplicationDTO addBreakoffApplicationDTO) {
		SmtBreakoffApplication smtBreakoffApplication = new SmtBreakoffApplication ();

		//根据员工编号查询员工的信息
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,addBreakoffApplicationDTO.getStaffBadge()));
		smtBreakoffApplication.setStaffId(selectOne.getId());
		smtBreakoffApplication.setStaffBadge(addBreakoffApplicationDTO.getStaffBadge());
		smtBreakoffApplication.setStaffName(selectOne.getName());
		smtBreakoffApplication.setType(Integer.valueOf(addBreakoffApplicationDTO.getRestType()));
		smtBreakoffApplication.setWorkTime(DateUtils.parse(addBreakoffApplicationDTO.getTerm()));
		smtBreakoffApplication.setRestTime(DateUtils.parse(addBreakoffApplicationDTO.getRestDate()));
		smtBreakoffApplication.setRestCount(addBreakoffApplicationDTO.getRestCount());
		smtBreakoffApplication.setRestAbleCount(addBreakoffApplicationDTO.getRestAbleCount());
		smtBreakoffApplication.setCause(addBreakoffApplicationDTO.getVacateDesc());
		smtBreakoffApplication.setCreateTime(DateUtil.date());
		return smtBreakoffApplication;
	}

	//获取调休记录分页列表
	public Page<SearchBreakoffApplicationVO> getSmtBreakoffApplicationPage(Page page,SmtBreakoffApplication smtBreakoffApplication) {
		//判断参数是否为空值
		if(StringUtils.isEmpty(smtBreakoffApplication.getStaffBadge())){
			 throw new TCEException(ExceptionTypeEnum.BREAK_OFF_STAFF_BADGE_ERROE);
		}
		Page<SearchBreakoffApplicationVO> smtBreakoffApplicationList = this.baseMapper.getSmtBreakoffApplicationPage(page,smtBreakoffApplication);
		for (int i = 0; i < smtBreakoffApplicationList.getRecords().size(); i++) {
			SearchBreakoffApplicationVO application = smtBreakoffApplicationList.getRecords().get(i);
			//根据调休的类型查询字典表调休的状态
			Integer type = application.getType();

			//根据类型那个查询该调休的类型描述
			Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.BREAK_OFF_TYPE,type.toString(), SecurityConstants.FROM_IN);
			//判断是否为空
			if(findByType.getData()!=null) {
					//根据字典表查询调休类型数据
				application.setRecordTypeDesc(findByType.getData().getLabel());
			}
			//根据流程的id查询最新调休状态值
			String processId = application.getProcessId();
			if(!StringUtils.isEmpty(processId)) {
				application.setRestDesc(smtProcessRecordService.getStatus(processId));
			}
		}
		return smtBreakoffApplicationList;
	}

	/**
	 * 根据id获取调休的信息
	 */
	@Override
	public SearchBreakoffApplicationDetail getBreakoffApplicationById(Integer id) {
		SearchBreakoffApplicationDetail breakoffApplicationDetail = new SearchBreakoffApplicationDetail ();
		SmtBreakoffApplication selectById = this.baseMapper.selectById(id);
		if(Objects.isNull(selectById)) {
			throw new SmartException("调休记录为空");
		}
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,selectById.getStaffBadge()));

		//判断调休记录不为空
		if(!StringUtils.isEmpty(selectById)) {
			breakoffApplicationDetail.setProcessId(selectById.getProcessId());
			EmployeeBreakOffVO employee = new EmployeeBreakOffVO();

			//根据类型查询该调休的类型描述
			Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.BREAK_OFF_TYPE,selectById.getType().toString(), SecurityConstants.FROM_IN);
			//判断是否为空
			if(findByType.getData()!=null) {
					//根据字典表查询调休类型数据
				    employee.setVacateTypeDesc(findByType.getData().getLabel());
			}
			employee.setRestDate(selectById.getRestTime());
			employee.setWorkDate(selectById.getWorkTime());
			employee.setRestCount(selectById.getRestCount());
			employee.setRestAbleCount(selectById.getRestAbleCount());
			employee.setRestDesc(selectById.getCause());
			employee.setCreateTime(selectById.getCreateTime());
			employee.setStaffBadge(selectById.getStaffBadge());
			employee.setStaffName(selectById.getStaffName());
			employee.setProcessId(selectById.getProcessId());
			employee.setBuName(selectOne.getCompName());
			employee.setDepName(selectOne.getDepName());
			employee.setJobName(selectOne.getJobName());
			breakoffApplicationDetail.setEmployee(employee);
			List<FlowVO> flowList = new ArrayList<FlowVO> ();
			//根据流程id获取流程
			if(!StringUtils.isEmpty(selectById.getProcessId())) {
				getOAProcessFlow(selectById.getProcessId(),flowList);
			}
			breakoffApplicationDetail.setFlow(flowList);
		}else {
		    throw new TCEException(ExceptionTypeEnum.BREAK_OFF_ID_PARAMETER);
		}
		return breakoffApplicationDetail;
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
	/**
	 * 调休类型
	 */
	public List<SearchBreakOffTypeVO> getBreakOffTypeList() {
		List <SearchBreakOffTypeVO> list = new ArrayList<SearchBreakOffTypeVO>();
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.BREAK_OFF_TYPE, SecurityConstants.FROM_IN);
		//判断集合是否为空
		if(findByType.getData().size()>0) {
			for (int i = 0; i < findByType.getData().size(); i++) {
				//根据字典表查询调休类型数据
				SearchBreakOffTypeVO searchBreakOffTypeVO = new SearchBreakOffTypeVO ();
				searchBreakOffTypeVO.setRestCode(findByType.getData().get(i).getValue());
				searchBreakOffTypeVO.setRestName(findByType.getData().get(i).getLabel());
				list.add(searchBreakOffTypeVO);
			}
		}
		return list;
	}

	/**
	 * 获取可调休的天数
	 */
	@Override
	public List<SmtBreakoffApplication> getRestCountList(SearchPatchDTO searchPatchDTO) {
		List <SmtBreakoffApplication> list = new ArrayList<SmtBreakoffApplication>();

		//判断员工号是否为空值
		if(StringUtils.isEmpty(searchPatchDTO.getStaffBadge())){
			throw new TCEException(ExceptionTypeEnum.REPLACE_STAFF_BADGE_NULL);
		}
		//判断年月份是否为空
		if(Objects.isNull(searchPatchDTO.getPatchDate())){
			throw new TCEException(ExceptionTypeEnum.REPLACE_YEAR_MONTH_NULL);
		}

		 list = this.baseMapper.selectList(
				Wrappers.<SmtBreakoffApplication> query().lambda()
				.eq(SmtBreakoffApplication::getStaffBadge, searchPatchDTO.getStaffBadge())
				.eq(SmtBreakoffApplication::getWorkTime,DateUtils.parse(searchPatchDTO.getPatchDate()))
				);
			//判断是否已经退回
		 for (int j = 0; j < list.size(); j++) {
				if(!StringUtils.isEmpty(list.get(j).getProcessId())) {
					List<FlowVO> flowList = new ArrayList<FlowVO> ();
					getOAProcessFlow(list.get(j).getProcessId(),flowList);
				    if(flowList.size()>0) {
					for (FlowVO flowVO : flowList) {
							if(flowVO.getProcessDesc().equals("退出"))
							{
								list.remove(j);
								j--;
							}

						}
				    }
				}
		 }

		return list;
	}

	@Override
	public Page<SearchBreakoffApplicationVO> getSmtBreakoffApplicationPageList(Page page,
			SearchBreakOffDTO searchBreakOffDTO) {
		// TODO Auto-generated method stub
		Page<SearchBreakoffApplicationVO> smtBreakoffApplicationList = this.baseMapper.getSmtBreakoffApplicationPageList(page,searchBreakOffDTO);
		for (int i = 0; i < smtBreakoffApplicationList.getRecords().size(); i++) {
			if(("-7").equals(smtBreakoffApplicationList.getRecords().get(i).getProcessId())){
				smtBreakoffApplicationList.getRecords().get(i).setProcessId("");
			}
			//根据调休的类型查询字典表调休的状态
			Integer type = smtBreakoffApplicationList.getRecords().get(i).getType();

			//根据类型那个查询该调休的类型描述
			Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.BREAK_OFF_TYPE,type.toString(), SecurityConstants.FROM_IN);
			//判断是否为空
			if(findByType.getData()!=null) {
					//根据字典表查询调休类型数据
				    smtBreakoffApplicationList.getRecords().get(i).setRecordTypeDesc(findByType.getData().getLabel());
			}

		}
		return smtBreakoffApplicationList;
	}


}
