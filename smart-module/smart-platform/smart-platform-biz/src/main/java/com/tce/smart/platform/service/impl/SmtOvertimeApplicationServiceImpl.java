package com.tce.smart.platform.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.data.api.dto.msg.req.SendExtraworkReqDTO;
import com.tce.smart.data.api.feign.attendance.RemoteKQShiftDetailsService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwBizAregotRegisterService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwLergotAllService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYscompService;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.data.api.dto.attendance.resp.KQShiftDetailsRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizAregotRegisterRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwLergotAllRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import com.tce.smart.platform.core.dto.AddOverTimeApplicationDTO;
import com.tce.smart.platform.core.dto.SearchOverTimeDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.platform.core.entity.SmtOvertimeApplication;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtOvertimeApplicationMapper;
import com.tce.smart.platform.core.vo.*;
import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.platform.service.SmtOvertimeApplicationService;
import com.tce.smart.platform.service.SmtProcessRecordService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.ApplicationEnum;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.enums.NodeStatusEnum;
import com.tce.smart.tool.enums.OverTimeEnum;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.RegexUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 加班申请表

 *
 * @author 梁圆
 * @date 2019-04-13 18:20:11
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtOvertimeApplicationServiceImpl extends ServiceImpl<SmtOvertimeApplicationMapper, SmtOvertimeApplication> implements SmtOvertimeApplicationService {
	private final RemoteDictService remoteDictService;
	private final SmtStaffService smtStaffService;
	private final SmtProcessRecordService smtProcessRecordService;
	private final RemoteOaWorkFlowService remoteOaWorkFlowService;
	private final RemoteOvwYscompService remoteOvwYscompService;
    private final IOAWorkflowService oaWorkflowService;
    private final RemoteKQShiftDetailsService remoteKQShiftDetailsService;

    private final RemoteEvwBizAregotRegisterService remoteEvwBizAregotRegisterService;

    private final RemoteEvwLergotAllService  remoteEvwLergotAllService;

	/**
	 * 加班类型
	 */
	public List<SearchOverTimeTypeVO> getOverTypeList() {
		List <SearchOverTimeTypeVO> list = new ArrayList<SearchOverTimeTypeVO>();
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.OVER_TIME_TYPE, SecurityConstants.FROM_IN);
		//判断集合是否为空
		if(findByType.getData().size()>0) {
			for (int i = 0; i < findByType.getData().size(); i++) {
				//根据字典表查询班别类型数据
				SearchOverTimeTypeVO searchOverTimeTypeVO = new SearchOverTimeTypeVO ();
				searchOverTimeTypeVO.setExtraworkType(findByType.getData().get(i).getValue());
				searchOverTimeTypeVO.setExtraworkTypeName(findByType.getData().get(i).getLabel());
				list.add(searchOverTimeTypeVO);
			}
		}
		return list;
	}
	/**
	 * 班别类别
	 */
	public List<SearchOverClassTimeTypeVO> getOverClassTypeList() {
		List <SearchOverClassTimeTypeVO> list = new ArrayList<SearchOverClassTimeTypeVO>();
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.EXTRA_WORK_CLASS_TYPE, SecurityConstants.FROM_IN);
		//判断集合是否为空
		if(findByType.getData().size()>0) {
			for (int i = 0; i < findByType.getData().size(); i++) {
				//根据字典表查询班别类型数据
				SearchOverClassTimeTypeVO searchOverClassTimeTypeVO = new SearchOverClassTimeTypeVO ();
				searchOverClassTimeTypeVO.setExtraworkClassCode(findByType.getData().get(i).getValue());
				searchOverClassTimeTypeVO.setRestName(findByType.getData().get(i).getLabel());
				list.add(searchOverClassTimeTypeVO);
			}
		}
		return list;
	}
	/**
	 * 添加加班申请
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void save(AddOverTimeApplicationDTO addOverApplicationDTO) {
		//判断参数是否为空值
		if(StringUtils.isEmpty(addOverApplicationDTO)){
			 throw new TCEException(ExceptionTypeEnum.ASK_LEAVE_PARAMETER__ERROR);
		}
		//正则判断
		ExceptionTypeEnum exceptionType = AskOverTimeCheck(addOverApplicationDTO);
		if(!exceptionType.equals(ExceptionTypeEnum.CHECK_SUCCESS)){
            throw new TCEException(exceptionType);
		}
		Result<List<EvwBizAregotRegisterRespDTO>> infoRegister = remoteEvwBizAregotRegisterService.info(addOverApplicationDTO.getStaffBadge(), addOverApplicationDTO.getExtraworkDate());
		log.info("remoteEvwBizAregotRegisterService.info {}",infoRegister);
		List<EvwBizAregotRegisterRespDTO> dataRegister = infoRegister.getData();
		if(dataRegister.size()>0)
		{
			for (EvwBizAregotRegisterRespDTO evwBizAregotRegister : dataRegister) {
				//当Formstate=1 、3、4时，可以申请
				if(evwBizAregotRegister.getFORMSTATE().equals(0)|| evwBizAregotRegister.getFORMSTATE().equals(2)||evwBizAregotRegister.getFORMSTATE().equals(5)||evwBizAregotRegister.getFORMSTATE().equals(6))
				{
					throw new TCEException(addOverApplicationDTO.getExtraworkDate()+"已在嘉阳PC后台审批中，不能重复申请");
				}
			}
			throw new TCEException(addOverApplicationDTO.getExtraworkDate()+"已在嘉阳PC后台审批中，不能重复申请");
		}
		Result<List<EvwLergotAllRespDTO>> infoAll = remoteEvwLergotAllService.info(addOverApplicationDTO.getStaffBadge(), addOverApplicationDTO.getExtraworkDate());
		log.info("remoteEvwLergotAllService.info {}",infoAll);
		List<EvwLergotAllRespDTO> dataAll= infoAll.getData();
		if(dataAll.size()>0)
		{
			throw new TCEException(addOverApplicationDTO.getExtraworkDate()+"已在嘉阳PC后台历史记录归档，不能重复申请");
		}

		SmtOvertimeApplication overTimeApplicationDTO = getOvertimeApplication(addOverApplicationDTO);
		boolean save = this.save(overTimeApplicationDTO);
		if(save) {
		//调用请假申请 获取流程的id
		overTimeApplicationDTO.setProcessId(getProcessId(addOverApplicationDTO,overTimeApplicationDTO.getId()));
		//修改数据，添加流程id
		boolean updateById = this.updateById(overTimeApplicationDTO);
		if(updateById) {
			getOAProcess(overTimeApplicationDTO.getProcessId());
		}
		}
	}

	/**
	 * 获取审批流程id
	 */
	public String getProcessId(AddOverTimeApplicationDTO addOverApplicationDTO,Integer id) {
		String processId = "";
		//加班审批表
		SendExtraworkReqDTO sendExtraworkAo = new SendExtraworkReqDTO();
		//获取员工的信息
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,addOverApplicationDTO.getStaffBadge()));
		sendExtraworkAo.setBadge(selectOne.getBadge());
		sendExtraworkAo.setName(selectOne.getName());
		sendExtraworkAo.setCompid(selectOne.getCompId());
		sendExtraworkAo.setDepid(selectOne.getDepId());
		sendExtraworkAo.setJobid(selectOne.getJobId());
		if(Objects.nonNull(selectOne.getEId())) {
			sendExtraworkAo.setEid(selectOne.getEId().toString());
		} else {
			sendExtraworkAo.setEid("");
		}
		sendExtraworkAo.setTWID(addOverApplicationDTO.getExtraworkClassCode());
		sendExtraworkAo.setAmount(addOverApplicationDTO.getExtraworkCount());
		sendExtraworkAo.setOTTerm(addOverApplicationDTO.getExtraworkDate());
		sendExtraworkAo.setOTTYPE(addOverApplicationDTO.getExtraworkType());
		sendExtraworkAo.setOT2TYPENAME(addOverApplicationDTO.getExtraworkType());
		sendExtraworkAo.setOT4TYPENAME(addOverApplicationDTO.getExtraworkType());
		sendExtraworkAo.setOT5TYPENAME(addOverApplicationDTO.getExtraworkType());
		sendExtraworkAo.setOT2STARTTIME(addOverApplicationDTO.getStartDate2());
		sendExtraworkAo.setOT2ENDTIME(addOverApplicationDTO.getEndDate2());
		sendExtraworkAo.setOT4STARTTIME(addOverApplicationDTO.getStartDate4());
		sendExtraworkAo.setOT4ENDTIME(addOverApplicationDTO.getEndDate4());
		sendExtraworkAo.setOT5STARTTIME(addOverApplicationDTO.getStartDate5());
		sendExtraworkAo.setOT5ENDTIME(addOverApplicationDTO.getEndDate5());
		sendExtraworkAo.setIscc(addOverApplicationDTO.getIsTravelExtrawork());
		sendExtraworkAo.setFJ("");
		sendExtraworkAo.setReason(addOverApplicationDTO.getExtraworkDesc());
		sendExtraworkAo.setRemark(addOverApplicationDTO.getExtraworkDesc());
		//获取人事区域
		Result<OvwYscompRespDTO> resultComp = remoteOvwYscompService.getByCompId(selectOne.getCompId(), SecurityConstants.FROM_IN);
		OvwYscompRespDTO ovwYscompVO = resultComp.getData();
		sendExtraworkAo.setEzid(ovwYscompVO.getEzid().toString());
		sendExtraworkAo.setJchenid(selectOne.getJcheId());
		sendExtraworkAo.setSeqid(id.toString());
		//获取流程id
		log.info("remoteOaWorkFlowService.param:"+sendExtraworkAo);
		Result<String> result = remoteOaWorkFlowService.sendExtrawork(sendExtraworkAo);
		log.info("remoteOaWorkFlowService.result:"+result);
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
	 * 获取加班对象
	 * @param addOverApplicationDTO
	 * @return
	 */
	private SmtOvertimeApplication getOvertimeApplication(AddOverTimeApplicationDTO addOverApplicationDTO) {
		SmtOvertimeApplication overtimeApplication = new SmtOvertimeApplication ();
		//根据员工编号查询员工的信息
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,addOverApplicationDTO.getStaffBadge()));
		overtimeApplication.setStaffId(selectOne.getId());
		overtimeApplication.setStaffBadge(addOverApplicationDTO.getStaffBadge());
		overtimeApplication.setStaffName(selectOne.getName());
		overtimeApplication.setWorkTime(addOverApplicationDTO.getExtraworkDate());
		overtimeApplication.setWorkType(Integer.valueOf(addOverApplicationDTO.getExtraworkType()));
		overtimeApplication.setWorkClassCode(Integer.valueOf(addOverApplicationDTO.getExtraworkClassCode()));
		overtimeApplication.setDuration(addOverApplicationDTO.getExtraworkCount());
		overtimeApplication.setCause(addOverApplicationDTO.getExtraworkDesc());
		overtimeApplication.setCreateTime(DateUtil.date());
		overtimeApplication.setIsTravelWork(Integer.valueOf(addOverApplicationDTO.getIsTravelExtrawork()));
		overtimeApplication.setStartDateTwo(addOverApplicationDTO.getStartDate2());
		overtimeApplication.setEndDateTwo(addOverApplicationDTO.getEndDate2());
		overtimeApplication.setStartDateFour(addOverApplicationDTO.getStartDate4());
		overtimeApplication.setEndDateFour(addOverApplicationDTO.getEndDate4());
		overtimeApplication.setStartDateFive(addOverApplicationDTO.getStartDate5());
		overtimeApplication.setEndDateFive(addOverApplicationDTO.getEndDate5());
		return overtimeApplication;
	}
	private ExceptionTypeEnum AskOverTimeCheck(AddOverTimeApplicationDTO addOverApplicationDTO){
		String extraworkClassCode = addOverApplicationDTO.getExtraworkClassCode();
		String ExtraworkType = addOverApplicationDTO.getExtraworkType();
		String isTravelExtrawork = addOverApplicationDTO.getIsTravelExtrawork();
		String extraworkCount = addOverApplicationDTO.getExtraworkCount();
		String staffBadge = addOverApplicationDTO.getStaffBadge();
		String extraworkDate = addOverApplicationDTO.getExtraworkDate();

		if(StringUtils.isEmpty(extraworkClassCode)){
			return ExceptionTypeEnum.OVER_TIME_EXTRA_WORK_CLASS_CODE_NULL;
		}
		if(StringUtils.isEmpty(ExtraworkType)){
			return ExceptionTypeEnum.OVER_TIME_EXTRA_WORK_TYPE_NULL;
		}
		if(StringUtils.isEmpty(isTravelExtrawork)){
			return ExceptionTypeEnum.OVER_TIME_IS_TRAVEL_WORK_NULL;
		}
		if(StringUtils.isEmpty(extraworkCount)){
			return ExceptionTypeEnum.OVER_TIME_EXTA_WORK_COUNT_NULL;
		}
		if(StringUtils.isEmpty(extraworkDate)){
			return ExceptionTypeEnum.OVER_TIME_EXTA_WORK_COUNT_NULL;
		}
		if(!RegexUtils.matchYearMonthDay(extraworkDate)){
			return ExceptionTypeEnum.OVER_TIME_YEAR_MONTH_DAY_PARAMETER;
		}
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,staffBadge));
		if(selectOne==null){
			return ExceptionTypeEnum.OVER_TIME_STAFF_BADGE_PARAMETER;
		}
		return ExceptionTypeEnum.CHECK_SUCCESS;
	}
	/**
	 * 加班分页查询记录
	 */
	public Page<SearchOverTimeApplicationVO> getOvertimeApplicationPage(Page page,SmtOvertimeApplication smtOvertimeApplication) {
		//判断参数是否为空值
		if(StringUtils.isEmpty(smtOvertimeApplication.getStaffBadge())){
			 throw new TCEException(ExceptionTypeEnum.OVER_TIME_STAFF_BADGE_ERROR);
		}
		Page<SearchOverTimeApplicationVO> overTimeApplicationList = this.baseMapper.getOvertimeApplicationPage(page,smtOvertimeApplication);
		for (int i = 0; i < overTimeApplicationList.getRecords().size(); i++) {
			//根据流程的id查询最新的请假状态值
			String processId = overTimeApplicationList.getRecords().get(i).getProcessId();
			if(!StringUtils.isEmpty(processId)) {
				overTimeApplicationList.getRecords().get(i).setRecordDesc(smtProcessRecordService.getStatus(processId));
			}
			//根据类型查询加班的类型描述
			Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.OVER_TIME_TYPE,overTimeApplicationList.getRecords().get(i).getWorkType().toString(), SecurityConstants.FROM_IN);
			//判断是否为空
			if(findByType.getData()!=null) {
					//根据字典表查询加班类型数据
				overTimeApplicationList.getRecords().get(i).setExtraworkTypeName(findByType.getData().getLabel());
			}
		}
	return overTimeApplicationList;
}

	/**
	 * 根据id获取详情
	 */
	public SearchOverTimeApplicationDetailVO getOverTimeById(Integer id) {
		SearchOverTimeApplicationDetailVO overTimeApplicationDetail = new SearchOverTimeApplicationDetailVO ();
		SmtOvertimeApplication selectById = this.baseMapper.selectById(id);

		List<FlowVO> flowList = new ArrayList<FlowVO> ();

		//判断加班记录不为空
		if(!StringUtils.isEmpty(selectById)) {
			EmployeeOverTimeVO employee = new EmployeeOverTimeVO();
			overTimeApplicationDetail.setProcessId(selectById.getProcessId());
			//根据类型查询该加班的类型描述
			Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.OVER_TIME_TYPE,selectById.getWorkType().toString(), SecurityConstants.FROM_IN);
			//判断是否为空
			if(findByType.getData()!=null) {
				//根据字典表查询调休类型数据
				employee.setExtraworkTypeDesc(findByType.getData().getLabel());
			}
			//根据类型查询该加班班别的类型描述
			Result<SysDict> findByTypeClass = remoteDictService.findByValue(DictConstants.EXTRA_WORK_CLASS_TYPE,selectById.getWorkClassCode().toString(), SecurityConstants.FROM_IN);
			//判断是否为空
			if(findByTypeClass.getData()!=null) {
				//根据字典表查询调休类型数据
				employee.setExtraworkClassDesc(findByTypeClass.getData().getLabel());
			}
			employee.setExtraworkDate(selectById.getWorkTime());
			employee.setExtraworkCount(selectById.getDuration());
			employee.setIsTravelExtrawork(OverTimeEnum.desc(selectById.getIsTravelWork()));
			employee.setExtraworkDesc(selectById.getCause());
			employee.setStartDate2(selectById.getStartDateTwo());
			employee.setEndDate2(selectById.getEndDateTwo());
			employee.setStartDate4(selectById.getStartDateFour());
			employee.setEndDate4(selectById.getEndDateFour());
			employee.setStartDate5(selectById.getStartDateFive());
			employee.setEndDate5(selectById.getEndDateFive());
			SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,selectById.getStaffBadge()));
			if(selectOne!=null) {
				employee.setEmployeeId(selectOne.getId().toString());
				employee.setEmployeeBadge(selectOne.getBadge());
				employee.setEmployeeName(selectOne.getName());
				employee.setBuName(selectOne.getCompName());
				employee.setDeptName(selectOne.getDepName());
				employee.setJobName(selectOne.getJobName());
			}
			overTimeApplicationDetail.setEmployee(employee);
			//根据流程id获取流程
			if(!StringUtils.isEmpty(selectById.getProcessId())) {
				getOAProcessFlow(selectById.getProcessId(),flowList);
/*				List<SmtProcessRecord> selectList = smtProcessRecordService.list(Wrappers.<SmtProcessRecord> query().lambda().eq(SmtProcessRecord::getProcessId, selectById.getProcessId()).orderByAsc(SmtProcessRecord::getRecordDate));
				if(selectList.size()>0) {
					for (int i = 0; i < selectList.size(); i++) {
						FlowVO flowVO = new FlowVO ();
				        if(StrUtil.isEmpty(selectList.get(i).getNodeName())) {
					flowVO.setNodeName("");
				        }else {
					String[] nodeNames = selectList.get(i).getNodeName().split(" ");
					if(nodeNames.length == 2) {
						flowVO.setNodeName(nodeNames[1]);
					}
				        }
						flowVO.setNodeState(selectList.get(i).getNodeState());
						//查询流程的最新的状态数据
						flowVO.setProcessDesc(NodeStatusEnum.nodeStatus(selectList.get(i).getStatementStatus()).getDesc());
						flowVO.setProcessDate(selectList.get(i).getRecordDate());
						flowList.add(flowVO);
					}
				}*/
			}
		}else {
		    throw new TCEException(ExceptionTypeEnum.OVER_TIME_ID_PARAMETER);
		}

		overTimeApplicationDetail.setFlow(flowList);
		return overTimeApplicationDetail;
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
	@Override
	public Page<SearchOverTimeApplicationVO> getOvertimeApplicationPageList(Page page,
			SearchOverTimeDTO searchLeaveDTO) {
			Page<SearchOverTimeApplicationVO> overTimeApplicationList = this.baseMapper.getOvertimeApplicationPageList(page,searchLeaveDTO);
			for (int i = 0; i < overTimeApplicationList.getRecords().size(); i++) {
				//根据类型查询加班的类型描述
				Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.OVER_TIME_TYPE,overTimeApplicationList.getRecords().get(i).getWorkType().toString(), SecurityConstants.FROM_IN);
				//判断是否为空
				if(findByType.getData()!=null) {
						//根据字典表查询加班类型数据
					overTimeApplicationList.getRecords().get(i).setExtraworkTypeName(findByType.getData().getLabel());
				}
				Result<SysDict> findByTypeClass = remoteDictService.findByValue(DictConstants.EXTRA_WORK_CLASS_TYPE,overTimeApplicationList.getRecords().get(i).getWorkClassCode().toString(), SecurityConstants.FROM_IN);
				System.out.println("findByTypeClass:"+findByTypeClass);
				//判断是否为空
				if(findByTypeClass.getData()!=null) {
					//根据字典表查询调休类型数据
					overTimeApplicationList.getRecords().get(i).setWorkClassCodeDesc(findByTypeClass.getData().getLabel());
				}
			}
		return overTimeApplicationList;
	}
	@Override
	public SearchOverTimeApplicationDetailVO getOverTimeByListId(Integer id) {
		// TODO Auto-generated method stub
		SearchOverTimeApplicationDetailVO overTimeApplicationDetail = new SearchOverTimeApplicationDetailVO ();
		SmtOvertimeApplication selectById = this.baseMapper.selectById(id);
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,selectById.getStaffBadge()));
		//判断加班记录不为空
		if(!StringUtils.isEmpty(selectById)) {
			overTimeApplicationDetail.setProcessId(selectById.getProcessId());
			EmployeeOverTimeVO employee = new EmployeeOverTimeVO();

			//根据类型查询该加班的类型描述
			Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.OVER_TIME_TYPE,selectById.getWorkType().toString(), SecurityConstants.FROM_IN);
			//判断是否为空
			if(findByType.getData()!=null) {
				//根据字典表查询调休类型数据
				employee.setExtraworkTypeDesc(findByType.getData().getLabel());
			}
			//根据类型查询该加班班别的类型描述
			Result<SysDict> findByTypeClass = remoteDictService.findByValue(DictConstants.EXTRA_WORK_CLASS_TYPE,selectById.getWorkClassCode().toString(), SecurityConstants.FROM_IN);
			//判断是否为空
			if(findByTypeClass.getData()!=null) {
				//根据字典表查询调休类型数据
				employee.setExtraworkClassDesc(findByTypeClass.getData().getLabel());
			}
			employee.setExtraworkDate(selectById.getWorkTime());
			employee.setExtraworkCount(selectById.getDuration());
			employee.setIsTravelExtrawork(OverTimeEnum.desc(selectById.getIsTravelWork()));
			employee.setExtraworkDesc(selectById.getCause());
			employee.setStartDate2(selectById.getStartDateTwo());
			employee.setEndDate2(selectById.getEndDateTwo());
			employee.setStartDate4(selectById.getStartDateFour());
			employee.setEndDate4(selectById.getEndDateFour());
			employee.setStartDate5(selectById.getStartDateFive());
			employee.setEndDate5(selectById.getEndDateFive());
			employee.setEmployeeBadge(selectById.getStaffBadge());
			employee.setEmployeeName(selectById.getStaffName());
			employee.setProcessId(selectById.getProcessId());
			employee.setCreateDate(selectById.getCreateTime());
			employee.setBuName(selectOne.getCompName());
			employee.setDeptName(selectOne.getDepName());
			employee.setJobName(selectOne.getJobName());
			overTimeApplicationDetail.setEmployee(employee);

			Result<KQShiftDetailsRespDTO> result = remoteKQShiftDetailsService.info(selectById.getStaffBadge(), selectById.getWorkTime(), SecurityConstants.FROM_IN);

			Console.log("KQShiftDetailsRespDTO:"+result);

			if (CommonConstants.SUCCESS  == result.getCode()) {
				if(ObjectUtil.isNotNull(result.getData())) {
					employee.setClassDesc(result.getData().getRunName());
					employee.setSecondEnter(result.getData().getRun2StartTime());
					employee.setSecondOut(result.getData().getRun2EndTime());
					employee.setFourthEnter(result.getData().getRun4StartTime());
					employee.setFourthOut(result.getData().getRun4EndTime());
					employee.setFifthEnter(result.getData().getRun5StartTime());
					employee.setFifthOut(result.getData().getRun5EndTime());
				}
			}

		}else {
		    throw new TCEException(ExceptionTypeEnum.OVER_TIME_ID_PARAMETER);
		}

		return overTimeApplicationDetail;
	}
}
