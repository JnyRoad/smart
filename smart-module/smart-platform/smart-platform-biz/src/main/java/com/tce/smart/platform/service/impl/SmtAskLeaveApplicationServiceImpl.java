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
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.data.api.dto.msg.req.SendVacateReqDTO;
import com.tce.smart.data.api.dto.ehrview.LvwLcdLeavetypeDTO;
import com.tce.smart.data.api.dto.ehrview.LvwLeavetypeDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizLregleaveRegisterRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwLregLeaveAllRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAyearholidayRespDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwBizLregleaveRegisterService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwLregLeaveAllService;
import com.tce.smart.data.api.feign.ehrview.RemoteLvwAyearholidayService;
import com.tce.smart.data.api.feign.ehrview.RemoteLvwLcdLeavetypeService;
import com.tce.smart.data.api.feign.ehrview.RemoteLvwLeavetypeService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYscompService;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import com.tce.smart.platform.core.dto.AddAskLeavelApplicationDTO;
import com.tce.smart.platform.core.dto.AppMsgPushDTO;
import com.tce.smart.platform.core.dto.SearchLeaveDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.platform.core.entity.SmtAskLeaveApplication;
import com.tce.smart.platform.core.entity.SmtBreakoffApplication;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtAskLeaveApplicationMapper;
import com.tce.smart.platform.core.model.YearHoliday;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.*;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 请假申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtAskLeaveApplicationServiceImpl extends ServiceImpl<SmtAskLeaveApplicationMapper, SmtAskLeaveApplication> implements SmtAskLeaveApplicationService {
	private final SmtStaffService smtStaffService;
	private final RemoteDictService remoteDictService;
	private final SmtProcessRecordService smtProcessRecordService;
	private final RemoteOaWorkFlowService remoteOaWorkFlowService;
	private final RemoteLvwAyearholidayService remoteLvwAyearholidayService;
	private final RemoteOvwYscompService remoteOvwYscompService;
    private final IOAWorkflowService oaWorkflowService;
	private SmtImageService smtImageService;
	private ImageService imageService;
	private final RemoteLvwLcdLeavetypeService remoteLvwLcdLeavetypeService;
	private final IAppMsgPushService appMsgPushService;
	private final RemoteLvwLeavetypeService  remoteLvwLeavetypeService;
	private final RemoteEvwBizLregleaveRegisterService remoteEvwBizLregleaveRegisterService;
	private final RemoteEvwLregLeaveAllService remoteEvwLregLeaveAllService;


	/**
	 * 查询请假申请的列表
	 */
	public Page<SearchAskLeaveApplicationVO> getAskLeavePage(Page page, SmtAskLeaveApplication smtAskLeaveApplication) {
		//判断参数是否为空值
		if(StringUtils.isEmpty(smtAskLeaveApplication.getStaffBadge())){
			 throw new TCEException(ExceptionTypeEnum.ASK_LEAVE_STAFF_BADGE_NULL);
		}
		Page<SearchAskLeaveApplicationVO> askLeavePageList = this.baseMapper.getAskLeavePage(page,smtAskLeaveApplication);
			for (int i = 0; i < askLeavePageList.getRecords().size(); i++) {
				//判断请假类型是否为空
				if(!StringUtils.isEmpty(askLeavePageList.getRecords().get(i).getType())) {
					Result<LvwLcdLeavetypeDTO> result = remoteLvwLcdLeavetypeService.info(Integer.parseInt(askLeavePageList.getRecords().get(i).getType()),SecurityConstants.FROM_IN);
					if (CommonConstants.SUCCESS  == result.getCode()) {
						if(ObjectUtil.isNotNull(result.getData())) {
							//根据时长id获取时长单位描述
							Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.LEAVE_UNIT,result.getData().getXunit().toString(), SecurityConstants.FROM_IN);
							//判断是否为空
							if(ObjectUtil.isNotNull(findByType.getData())) {
								//根据字典表查询补卡原因类型数据
								askLeavePageList.getRecords().get(i).setUnit(findByType.getData().getLabel());
							}
						}
					}
				}
				//根据流程的id查询最新的请假状态值
				String processId = askLeavePageList.getRecords().get(i).getProcessId();
				if(!StringUtils.isEmpty(processId)) {
					askLeavePageList.getRecords().get(i).setRecordDesc(smtProcessRecordService.getStatus(processId));
				}
			}
		return askLeavePageList;
	}


	/**
	 * 添加请假申请
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void add(AddAskLeavelApplicationDTO addAskLeavelApplicationDTO) {
		//判断参数是否为空值
		if(StringUtils.isEmpty(addAskLeavelApplicationDTO)){
			 throw new TCEException(ExceptionTypeEnum.ASK_LEAVE_PARAMETER__ERROR);
		}
		//正则判断
		ExceptionTypeEnum exceptionType = AskLeaveCheck(addAskLeavelApplicationDTO);
		if(!exceptionType.equals(ExceptionTypeEnum.CHECK_SUCCESS)){
            throw new TCEException(exceptionType);
		}

		Result<List<EvwBizLregleaveRegisterRespDTO>> infoRegister = remoteEvwBizLregleaveRegisterService.info(addAskLeavelApplicationDTO.getStaffBadge(), addAskLeavelApplicationDTO.getStartDate(), addAskLeavelApplicationDTO.getEndDate());
		log.info("remoteEvwBizLregleaveRegisterService.info {}",infoRegister);
		List<EvwBizLregleaveRegisterRespDTO> registerData = infoRegister.getData();
		if(registerData.size()>0)
		{
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(),registerData.get(0).getBeginTime()+"已在嘉阳PC后台审批中，不能重复申请");
		}
		Result<List<EvwLregLeaveAllRespDTO>> infoAll = remoteEvwLregLeaveAllService.info(addAskLeavelApplicationDTO.getStaffBadge(), addAskLeavelApplicationDTO.getStartDate(), addAskLeavelApplicationDTO.getEndDate());
		log.info("remoteEvwLregLeaveAllService.info {}",infoAll);
		List<EvwLregLeaveAllRespDTO> allData = infoAll.getData();
		 if(allData.size()>0)
		 {
			 throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(),allData.get(0).getBeginTime()+"已在嘉阳PC后台历史记录归档，不能重复申请");
		 }

		//判断这个时间段是否有请并且没有没有被回退  lt小于 ge大于
		List<SmtAskLeaveApplication> askList = this.baseMapper.selectList(Wrappers.<SmtAskLeaveApplication> query().lambda()
				.eq(SmtAskLeaveApplication::getStaffBadge, addAskLeavelApplicationDTO.getStaffBadge())
				.ge(SmtAskLeaveApplication::getEndTime,DateUtils.parse(addAskLeavelApplicationDTO.getStartDate()))
				.lt(SmtAskLeaveApplication::getStartTime,DateUtils.parse(addAskLeavelApplicationDTO.getEndDate())));

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
				throw new TCEException(addAskLeavelApplicationDTO.getStartDate()+"-"+addAskLeavelApplicationDTO.getEndDate()+"时间段已经申请过请假，不能重复申请");
			    }
			}
		}



		//判断这个时间段是否有过调休，是否退回
		SimpleDateFormat sdf = new SimpleDateFormat("HH");
		Integer type=0;
		Integer startHour=Integer.parseInt(sdf.format(DateUtil.parse(addAskLeavelApplicationDTO.getStartDate())));
		Integer endHour=Integer.parseInt(sdf.format(DateUtil.parse(addAskLeavelApplicationDTO.getEndDate())));
		String error="";
		if(startHour>=12)
		{
			//下午
			type=3;
			error="下午";
		}else if(endHour<=12)
		{
			//上午
			type=2;
			error="上午";
		}
		else if(startHour<12 && endHour>12 )
		{
			//全天
			error="全天";
			type=1;
		}
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		SmtBreakoffApplication  breakOff=new SmtBreakoffApplication();
		breakOff.setStaffBadge(addAskLeavelApplicationDTO.getStaffBadge());
		breakOff.setType(type);
		Date parse=null;
		try {
			parse = sdfDate.parse(addAskLeavelApplicationDTO.getStartDate());
			breakOff.setRestTime(parse);
		} catch (ParseException e) {
			throw new TCEException("系统异常");
		}
		List<SmtBreakoffApplication> list = this.baseMapper.selectBreakoffApplication(breakOff);

		//判断这天是否调休过，是否被退回
		for (SmtBreakoffApplication smtBreakoffApplication : list) {
			if(!StringUtils.isEmpty(smtBreakoffApplication.getProcessId())) {
				List<FlowVO> flowList = new ArrayList<FlowVO> ();
				getOAProcessFlow(smtBreakoffApplication.getProcessId(),flowList);
				Integer back=0;
					  if(flowList.size()>0) {
					   for (FlowVO flowVO : flowList) {
						if(flowVO.getProcessDesc().equals("退出"))
						{
							back+=1;
						}
					}
				}
			if(back==0 )
			{
				throw new TCEException( sdfDate.format(parse)+"已申请调休，不能重复申请请假");
			}
		}
	}
		SmtAskLeaveApplication askLeaveApplication = getAskLeaveApplication(addAskLeavelApplicationDTO);
		//添加进入数据库
		boolean save = this.save(askLeaveApplication);
		if(save) {
			//调用请假申请 获取流程的id
			askLeaveApplication.setProcessId(getProcessId(addAskLeavelApplicationDTO,askLeaveApplication.getId()));
			//修改数据，添加流程id
			boolean updateById = this.updateById(askLeaveApplication);
			if(updateById) {
				getOAProcess(askLeaveApplication.getProcessId());
			}
		}

	}
	/**
	 * 获取审批流程id
	 */
	public String getProcessId(AddAskLeavelApplicationDTO addAskLeavelApplicationDTO,Integer id) {
		String processId = "";
		//请假审批表
		SendVacateReqDTO sendVacateAo = new SendVacateReqDTO();
		//获取员工的信息
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,addAskLeavelApplicationDTO.getStaffBadge()));
		sendVacateAo.setBadge(selectOne.getBadge());
		sendVacateAo.setName(selectOne.getName());
		sendVacateAo.setCompid(selectOne.getCompId());
		sendVacateAo.setDepid(selectOne.getDepId());
		sendVacateAo.setJobid(selectOne.getJobId());
		sendVacateAo.setJchenid(selectOne.getJcheId());
		if(Objects.nonNull(selectOne.getEId())) {
			sendVacateAo.setEid(selectOne.getEId().toString());
		} else {
			sendVacateAo.setEid("");
		}
		sendVacateAo.setSeqid(id.toString());  //主键自增的id
		sendVacateAo.setTWID(addAskLeavelApplicationDTO.getVacateType());
		SimpleDateFormat formatDay = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm:ss");
		sendVacateAo.setBegintime(DateUtils.format(DateUtils.parse(addAskLeavelApplicationDTO.getStartDate()), formatDay));
		sendVacateAo.setEndtime(DateUtils.format(DateUtils.parse(addAskLeavelApplicationDTO.getEndDate()), formatDay));
		sendVacateAo.setStarttime(DateUtils.format(DateUtils.parse(addAskLeavelApplicationDTO.getStartDate()), formatHour));
		sendVacateAo.setEnddate(DateUtils.format(DateUtils.parse(addAskLeavelApplicationDTO.getEndDate()), formatHour));
		//根据员工号查年假信息
	    Result<LvwAyearholidayRespDTO> resultYear = remoteLvwAyearholidayService.info(addAskLeavelApplicationDTO.getStaffBadge(), SecurityConstants.FROM_IN);
	    if (CommonConstants.SUCCESS  == resultYear.getCode()) {
			LvwAyearholidayRespDTO lvwAyearholidayVO = resultYear.getData();
		    YearHoliday yearHoliday = new YearHoliday(lvwAyearholidayVO == null ? 0 : ObjectUtil.isNull(lvwAyearholidayVO.getThisbalance()) ? 0 : lvwAyearholidayVO.getThisbalance());
		    sendVacateAo.setCyear(yearHoliday.getDayCount().toString());
	    }else {
		sendVacateAo.setCyear("0");
	    }

		//获取人事区域
		Result<OvwYscompRespDTO> resultComp = remoteOvwYscompService.getByCompId(selectOne.getCompId(), SecurityConstants.FROM_IN);
		OvwYscompRespDTO ovwYscompVO = resultComp.getData();
		sendVacateAo.setEzid(ovwYscompVO.getEzid().toString());
		//根据时长单位获取时长value描述
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.LEAVE_UNIT, SecurityConstants.FROM_IN);
		//判断是否为空
		if(findByType.getData().size()>0) {
			for (int i = 0; i < findByType.getData().size(); i++) {
				if(findByType.getData().get(i).getLabel().equals(addAskLeavelApplicationDTO.getUnit())) {
					log.info("------------------------");
					log.info(findByType.getData().get(i).getValue());
					sendVacateAo.setUnit(findByType.getData().get(i).getValue());
					break;
				}
			}
		}
		sendVacateAo.setFJ("");//"暂时未空附件值"
		sendVacateAo.setAmount(addAskLeavelApplicationDTO.getVacateCount());
		sendVacateAo.setDayoffreason(addAskLeavelApplicationDTO.getVacateDesc());
		sendVacateAo.setShift(addAskLeavelApplicationDTO.getClassName());
		sendVacateAo.setIn2(addAskLeavelApplicationDTO.getSecondEnter());
		sendVacateAo.setOut2(addAskLeavelApplicationDTO.getSecondOut());
		sendVacateAo.setIn4(addAskLeavelApplicationDTO.getFourthEnter());
		sendVacateAo.setOut4(addAskLeavelApplicationDTO.getFourthOut());
		sendVacateAo.setIn5(addAskLeavelApplicationDTO.getFifthEnter());
		sendVacateAo.setOut5(addAskLeavelApplicationDTO.getFifthOut());
		sendVacateAo.setRemark("");
		Result<LvwLcdLeavetypeDTO> resultType = remoteLvwLcdLeavetypeService.info(Integer.parseInt(addAskLeavelApplicationDTO.getVacateType()),SecurityConstants.FROM_IN);
		if (CommonConstants.SUCCESS  == resultType.getCode()) {
			if(ObjectUtil.isNotNull(resultType.getData())) {
				//根据id获取请假的描述
				sendVacateAo.setTEXT(resultType.getData().getRemark());
			}else {
				sendVacateAo.setTEXT("");
			}
		}
		//获取流程id
		log.info("remoteOaWorkFlowService.sendVacateAo param {}",sendVacateAo);
		Result<String> result = remoteOaWorkFlowService.sendVacateAo(sendVacateAo);
		log.info("remoteOaWorkFlowService.sendVacateAo result {}",result);
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
	 * 获取请假对象
	 */
	public SmtAskLeaveApplication getAskLeaveApplication(AddAskLeavelApplicationDTO addAskLeavelApplicationDTO) {
		SmtAskLeaveApplication smtAskLeaveApplication = new SmtAskLeaveApplication ();

		//根据员工编号查询员工的信息
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,addAskLeavelApplicationDTO.getStaffBadge()));
		smtAskLeaveApplication.setStaffId(selectOne.getId());
		smtAskLeaveApplication.setStaffBadge(addAskLeavelApplicationDTO.getStaffBadge());
		smtAskLeaveApplication.setStaffName(selectOne.getName());
		smtAskLeaveApplication.setType(Integer.valueOf(addAskLeavelApplicationDTO.getVacateType()));
		smtAskLeaveApplication.setStartTime(DateUtils.parse(addAskLeavelApplicationDTO.getStartDate()));
		smtAskLeaveApplication.setEndTime(DateUtils.parse(addAskLeavelApplicationDTO.getEndDate()));
		smtAskLeaveApplication.setDuration(addAskLeavelApplicationDTO.getVacateCount());
		smtAskLeaveApplication.setCause(addAskLeavelApplicationDTO.getVacateDesc());
		smtAskLeaveApplication.setClassName(addAskLeavelApplicationDTO.getClassName());
		smtAskLeaveApplication.setSecondEnter(addAskLeavelApplicationDTO.getSecondEnter());
		smtAskLeaveApplication.setSecondOut(addAskLeavelApplicationDTO.getSecondOut());
		smtAskLeaveApplication.setFourthEnter(addAskLeavelApplicationDTO.getFourthEnter());
		smtAskLeaveApplication.setFourthOut(addAskLeavelApplicationDTO.getFourthOut());
		smtAskLeaveApplication.setFifthEnter(addAskLeavelApplicationDTO.getFifthEnter());
		smtAskLeaveApplication.setFifthOut(addAskLeavelApplicationDTO.getFifthOut());
		smtAskLeaveApplication.setCreateTime(DateUtil.date());
		//获取图片id
		if(!StringUtils.isEmpty(addAskLeavelApplicationDTO.getPhoto())){
			smtAskLeaveApplication.setPhotoId(getPhotoId(0,addAskLeavelApplicationDTO.getPhoto()));
			//获取图片id
		}
		return smtAskLeaveApplication;
	}
	private ExceptionTypeEnum AskLeaveCheck(AddAskLeavelApplicationDTO addAskLeavelApplicationDTO){
		String vacateType = addAskLeavelApplicationDTO.getVacateType();
		String vacateCount = addAskLeavelApplicationDTO.getVacateCount();
//		String vacateDesc = addAskLeavelApplicationDTO.getVacateDesc();
		String className = addAskLeavelApplicationDTO.getClassName();
		String staffBadge = addAskLeavelApplicationDTO.getStaffBadge();
		String unit = addAskLeavelApplicationDTO.getUnit();

		if(Objects.isNull(vacateType)){
			return ExceptionTypeEnum.ASK_LEAVE_TYPE_ERROR;
		}
		if(Objects.isNull(vacateCount)){
			return ExceptionTypeEnum.ASK_LEAVE_VACATE_COUNT_ERROR;
		}
	/*	if(StringUtils.isEmpty(className)){
			return ExceptionTypeEnum.ASK_LEAVE_CLASS_NAME_NULL;
		}*/
		if(StringUtils.isEmpty(unit)){
			return ExceptionTypeEnum.ASK_UNIT_NULL;
		}
		if(className.length()>50)
		{
			return ExceptionTypeEnum.ASK_LEAVE_CLASS_NAME_ERROR;
		}
	/*	if(StringUtils.isEmpty(vacateDesc)){
			return ExceptionTypeEnum.ASK_LEAVE_DESC_ERROR;
		}*/
		if(StringUtils.isEmpty(staffBadge)){
			return ExceptionTypeEnum.ASK_LEAVE_STAFF_BADGE_NULL;
		}
/*		if(StringUtils.isEmpty(photo)){
			return ExceptionTypeEnum.ASK_LEAVE_PHOTO_ERROR;
		}*/
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,staffBadge));
		if(selectOne==null){
			return ExceptionTypeEnum.ASK_LEAVE_STAFF_BADGE_ERROR;
		}
/*		if(endDate.before(startDate)){
			return ExceptionTypeEnum.ASK_LEAVE_TIME_ERROR;
		}*/
		return ExceptionTypeEnum.CHECK_SUCCESS;
	}


	/**
	 * 请假的类型
	 */
	public List<SearchAskLeaveTypeVO> getAskTypeList() {
		List <SearchAskLeaveTypeVO> list = new ArrayList<SearchAskLeaveTypeVO>();
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.LEAVE_TYPE, SecurityConstants.FROM_IN);
		//判断集合是否为空
		if(findByType.getData().size()>0) {
			for (int i = 0; i < findByType.getData().size(); i++) {
				//根据字典表查询请假类型数据
				SearchAskLeaveTypeVO searchAskLeaveTypeVO = new SearchAskLeaveTypeVO ();
				Integer id=Integer.parseInt(findByType.getData().get(i).getValue());
				Result<LvwLeavetypeDTO> byId = remoteLvwLeavetypeService.getById(id,SecurityConstants.FROM_IN);
				if(byId.isSuccess())
				{
					if(byId.getData()!=null)
					{
						searchAskLeaveTypeVO.setVacateRemark(byId.getData().getRemark());
					}
				}
				searchAskLeaveTypeVO.setVacateCode(findByType.getData().get(i).getValue());
				searchAskLeaveTypeVO.setVacateName(findByType.getData().get(i).getLabel());
				list.add(searchAskLeaveTypeVO);
			}
		}
		return list;
	}

	@Override
	public SearchAskLeaveApplicationDetailVO getAskLeaveById(Integer id) {
		SearchAskLeaveApplicationDetailVO askLeaveApplicationDetail = new SearchAskLeaveApplicationDetailVO ();
		SmtAskLeaveApplication selectById = this.baseMapper.selectById(id);
		List<FlowVO> flowList = new ArrayList<FlowVO> ();
		//判断请假记录不为空
		if(!StringUtils.isEmpty(selectById)) {
			askLeaveApplicationDetail.setProcessId(selectById.getProcessId());
			EmployeeAskLeaveVO employee = new EmployeeAskLeaveVO();
			employee.setVacateType(selectById.getType());
			//根据类型查询该的类型描述
			Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.LEAVE_TYPE,selectById.getType().toString(), SecurityConstants.FROM_IN);
			//判断是否为空
			if(findByType.getData()!=null) {
					//根据字典表查询类型数据
				    employee.setVacateTypeDesc(findByType.getData().getLabel());
			}
			//根据类型查询该请假市场单位的描述
			Result<SysDict> findUnitByType = remoteDictService.findByValue(DictConstants.LEAVE_UNIT,selectById.getType().toString(), SecurityConstants.FROM_IN);
			//判断是否为空
			if(findUnitByType.getData()!=null) {
				//根据字典表查询时长单位描述
				employee.setUnit(findUnitByType.getData().getLabel());
			}
			employee.setVacateCount(selectById.getDuration());
			employee.setStartDate(selectById.getStartTime());
			employee.setEndDate(selectById.getEndTime());
			employee.setVacateDesc(selectById.getCause());
			employee.setClassName(selectById.getClassName());
			employee.setSecondEnter(selectById.getSecondEnter());
			employee.setSecondOut(selectById.getSecondOut());
			employee.setFourthEnter(selectById.getFourthEnter());
			employee.setFourthOut(selectById.getFourthOut());
			employee.setFifthEnter(selectById.getFifthEnter());
			employee.setFifthOut(selectById.getFifthOut());
			if(com.tce.smart.common.core.util.StringUtils.isNotEmpty(selectById.getPhotoId())) {
				employee.setPhoto(imageService.buildImageUrl(selectById.getPhotoId()));
			}
			SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,selectById.getStaffBadge()));
			if(selectOne!=null) {
				employee.setEmployeeId(selectOne.getId());
				employee.setEmployeeBadge(selectOne.getBadge());
				employee.setEmployeeName(selectOne.getName());
				employee.setBuName(selectOne.getCompName());
				employee.setDeptName(selectOne.getDepName());
				employee.setJobName(selectOne.getJobName());
			}
			askLeaveApplicationDetail.setEmployee(employee);
			//根据流程id获取流程
			if(!StringUtils.isEmpty(selectById.getProcessId())) {
				getOAProcessFlow(selectById.getProcessId(),flowList);
//				List<SmtProcessRecord> selectList = smtProcessRecordService.list(Wrappers.<SmtProcessRecord> query().lambda().eq(SmtProcessRecord::getProcessId, selectById.getProcessId()).orderByAsc(SmtProcessRecord::getRecordDate));
//				if(selectList.size()>0) {
//					for (int i = 0; i < selectList.size(); i++) {
//						FlowVO flowVO = new FlowVO ();
//				        if(StrUtil.isEmpty(selectList.get(i).getNodeName())) {
//				        	flowVO.setNodeName("");
//				        }else {
//				        	String[] nodeNames = selectList.get(i).getNodeName().split(" ");
//				        	if(nodeNames.length == 2) {
//				        		flowVO.setNodeName(nodeNames[1]);
//				        	}
//				        }
//						flowVO.setNodeState(selectList.get(i).getNodeState());
//						//查询流程的最新的状态数据
//						flowVO.setProcessDesc(NodeStatusEnum.nodeStatus(selectList.get(i).getStatementStatus()).getDesc());
//						flowVO.setProcessDate(selectList.get(i).getRecordDate());
//						flowList.add(flowVO);
//					}
//				}
			}
		}else {
		    throw new TCEException(ExceptionTypeEnum.ASK_LEAVE_ID_PARAMETER);
		}

		askLeaveApplicationDetail.setFlow(flowList);
		return askLeaveApplicationDetail;
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
	 * 根据图片base64获取图片的id
	 * @param photo
	 */
	public String getPhotoId(Integer parkId,String photo) {
		if(!StringUtils.isEmpty(photo)) {
			return  smtImageService.saveImage(parkId,photo, SmtImageEnum.TYPE_ASK_LEAVW_ATTACHMENT.getCode());
		}
		return null;
	}

	@Override
	public void approvalNotice(String badge, String code, Integer id) {
		//推送App消息
		AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
		appMsgPushDTO.setBadge(badge);
		appMsgPushDTO.setBussiessId(String.valueOf(id));
		appMsgPushDTO.setTemplateCode(code);
		if(code.equals(SmsTemplateEnum.APP_PUSH_10301.getCode())) {
			appMsgPushDTO.setExtraParam("attendanceStatus=" + MsgAttendanceEnum.MSG_1.getCode());
		}
		if(code.equals(SmsTemplateEnum.APP_PUSH_10303.getCode())) {
			appMsgPushDTO.setExtraParam("attendanceStatus=" + MsgAttendanceEnum.MSG_1.getCode());
		}
		appMsgPushService.pushAppMsg(appMsgPushDTO);
	}


	@Override
	public Page<SearchAskLeaveApplicationVO> getAskLeavePageList(Page page, SearchLeaveDTO searchLeaveDTO) {
		// TODO Auto-generated method stub
		Page<SearchAskLeaveApplicationVO> askLeavePageList = this.baseMapper.getAskLeavePageList(page,searchLeaveDTO);

		for (int i = 0; i < askLeavePageList.getRecords().size(); i++) {
			if(("-7").equals(askLeavePageList.getRecords().get(i).getProcessId())){
				askLeavePageList.getRecords().get(i).setProcessId("");
			}
			//判断请假类型是否为空
			if(!StringUtils.isEmpty(askLeavePageList.getRecords().get(i).getType())) {
				 Result<SysDict> findByType2 = remoteDictService.findByValue(DictConstants.LEAVE_TYPE,askLeavePageList.getRecords().get(i).getType() ,SecurityConstants.FROM_IN);
				if(ObjectUtil.isNotNull(findByType2.getData())) {
					askLeavePageList.getRecords().get(i).setTypeDesc(findByType2.getData().getLabel());
				}
			}
		}
	return askLeavePageList;
	}


	@Override
	public SearchAskLeaveApplicationDetailVO getAskLeaveByListId(Integer id) {
		// TODO Auto-generated method stub
		SearchAskLeaveApplicationDetailVO askLeaveApplicationDetail = new SearchAskLeaveApplicationDetailVO ();
		SmtAskLeaveApplication selectById = this.baseMapper.selectById(id);
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,selectById.getStaffBadge()));

		//判断请假记录不为空
		if(!StringUtils.isEmpty(selectById)) {
			askLeaveApplicationDetail.setProcessId(selectById.getProcessId());
			EmployeeAskLeaveVO employee = new EmployeeAskLeaveVO();

			employee.setVacateType(selectById.getType());
			//根据类型查询该的类型描述
			Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.LEAVE_TYPE,selectById.getType().toString(), SecurityConstants.FROM_IN);
			//判断是否为空
			if(findByType.getData()!=null) {
					//根据字典表查询类型数据
				    employee.setVacateTypeDesc(findByType.getData().getLabel());
			}
			Result<LvwLcdLeavetypeDTO> result = remoteLvwLcdLeavetypeService.info(selectById.getType(),SecurityConstants.FROM_IN);
			System.out.println("LvwLcdLeavetypeDTO result"+result);
			if (CommonConstants.SUCCESS  == result.getCode()) {
				if(ObjectUtil.isNotNull(result.getData())) {
					//根据时长id获取时长单位描述
					Result<SysDict> findByTypes = remoteDictService.findByValue(DictConstants.LEAVE_UNIT,result.getData().getXunit().toString(), SecurityConstants.FROM_IN);
					//判断是否为空
					System.out.println("findByTypes result"+findByTypes);
					if(ObjectUtil.isNotNull(findByTypes.getData())) {
						//根据字典表查询补卡原因类型数据
						employee.setUnit(findByTypes.getData().getLabel());
					}
				}
			}

			employee.setVacateCount(selectById.getDuration());
			employee.setStartDate(selectById.getStartTime());
			employee.setEndDate(selectById.getEndTime());
			employee.setVacateDesc(selectById.getCause());
			employee.setClassName(selectById.getClassName());
			employee.setSecondEnter(selectById.getSecondEnter());
			employee.setSecondOut(selectById.getSecondOut());
			employee.setFourthEnter(selectById.getFourthEnter());
			employee.setFourthOut(selectById.getFourthOut());
			employee.setFifthEnter(selectById.getFifthEnter());
			employee.setFifthOut(selectById.getFifthOut());
			employee.setProcessId(selectById.getProcessId());
			employee.setCreateDate(selectById.getCreateTime());
			if(com.tce.smart.common.core.util.StringUtils.isNotEmpty(selectById.getPhotoId())) {
				employee.setPhoto(imageService.buildImageUrl(selectById.getPhotoId()));
			}
			employee.setEmployeeBadge(selectById.getStaffBadge());
			employee.setEmployeeName(selectById.getStaffName());
			employee.setBuName(selectOne.getCompName());
			employee.setDeptName(selectOne.getDepName());
			employee.setJobName(selectOne.getJobName());
			askLeaveApplicationDetail.setEmployee(employee);
		}
		else {
			    throw new TCEException(ExceptionTypeEnum.ASK_LEAVE_ID_PARAMETER);
			}
			return askLeaveApplicationDetail;
	}
}
