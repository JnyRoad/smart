package com.tce.smart.platform.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.msg.req.SendAttendancePatchkAo;
import com.tce.smart.data.api.dto.attendance.resp.KQCardDetailsRespDTO;
import com.tce.smart.data.api.dto.attendance.resp.KQShiftDetailsRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwAcardlostAllRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizLcardlostRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAttendYcxxFullRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAttendYcxxSimpleRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import com.tce.smart.data.api.feign.attendance.RemoteKQCardDetailsService;
import com.tce.smart.data.api.feign.attendance.RemoteKQShiftDetailsService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwAcardlostAllService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwBizLcardlostService;
import com.tce.smart.data.api.feign.ehrview.RemoteLvwAttendYcxxService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYscompService;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.platform.api.dto.req.PatchStatisticsReqDTO;
import com.tce.smart.platform.api.dto.resp.PatchStatisticsRespDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.entity.SmtReplaceApplication;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtReplaceApplicationMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.platform.core.vo.*;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.RegexUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


/**
 * 考勤\补卡申请表

 *
 * @author 梁圆
 * @date 2019-04-13 18:19:37
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtReplaceApplicationServiceImpl extends ServiceImpl<SmtReplaceApplicationMapper, SmtReplaceApplication> implements SmtReplaceApplicationService {
	@Autowired
	private  RemoteDictService remoteDictService;

	@Autowired
	private SmtStaffService smtStaffService;

	@Autowired
	private SmtProcessRecordService smtProcessRecordService;

	@Autowired
	private RemoteKQCardDetailsService remoteKQCardDetailsService;

	@Autowired
	private RemoteLvwAttendYcxxService remoteLvwAttendYcxxService;

	@Autowired
	private RemoteKQShiftDetailsService remoteKQShiftDetailsService;

	@Autowired
	private RemoteOaWorkFlowService remoteOaWorkFlowService;

	@Autowired
    private IOAWorkflowService oaWorkflowService;

	@Autowired
    private IAppMsgPushService appMsgPushService;

	@Autowired
	private RemoteOvwYscompService remoteOvwYscompService;

	@Autowired
	private SmtMsgTemplateService smtMsgTemplateService;
	@Autowired
	private ImageService imageService;
	@Autowired
	private SmtImageService smtImageService;

	@Autowired
	private RemoteEvwBizLcardlostService remoteEvwBizLcardlostService;

	@Autowired
	private RemoteEvwAcardlostAllService remoteEvwAcardlostAllService;



	/**
	 * 获取补卡原因列表
	 */
	public List<SearchPatchCardReasonVO> getPatchCardReason() {

		List <SearchPatchCardReasonVO> list = new ArrayList<SearchPatchCardReasonVO>();
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.REPLACE_REASON, SecurityConstants.FROM_IN);
		//判断集合是否为空
		if(findByType.getData().size()>0) {
			for (int i = 0; i < findByType.getData().size(); i++) {
				//根据字典表查询补卡原因类型数据
				SearchPatchCardReasonVO searchAskLeaveTypeVO = new SearchPatchCardReasonVO ();
				searchAskLeaveTypeVO.setReasonCode(findByType.getData().get(i).getValue());
				searchAskLeaveTypeVO.setReasonName(findByType.getData().get(i).getLabel());
				list.add(searchAskLeaveTypeVO);

			}
		}
		return list;
	}

	/**
	 * 添加补卡申请
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void add(AddReplaceApplicationDTO addReplaceApplicationDTO) {

		//判断参数是否为空值
		if(StringUtils.isEmpty(addReplaceApplicationDTO)){
			throw new TCEException(ExceptionTypeEnum.REPLACE_NULL);
		}
		//正则判断
		ExceptionTypeEnum exceptionType = ReplaceCheck(addReplaceApplicationDTO);
		if(!exceptionType.equals(ExceptionTypeEnum.CHECK_SUCCESS)){
			throw new TCEException(exceptionType);
		}

		//判断补卡是否已经存在，存在则不让补
//		List<SmtReplaceApplication> smtReplaceApplicationList = this.list(Wrappers.<SmtReplaceApplication> query().lambda()
//				.eq(SmtReplaceApplication::getStaffBadge,addReplaceApplicationDTO.getStaffBadge()).eq(SmtReplaceApplication::getStartTime, addReplaceApplicationDTO.getPatchDate()));
//		if(smtReplaceApplicationList.size()>0)
//		{
//			throw new TCEException(addReplaceApplicationDTO.getPatchDate()+"已经申请过补卡，不能重复申请");
//		}

//		Result<List<EvwBizLcardlostRespDTO>> infoLost = remoteEvwBizLcardlostService.info(addReplaceApplicationDTO.getStaffBadge(), addReplaceApplicationDTO.getPatchDate());
//		List<EvwBizLcardlostRespDTO> dataLost = infoLost.getData();
//		log.info("dataLost:{}", dataLost);
//		if(dataLost.size()>0)
//		{
//			for (EvwBizLcardlostRespDTO evwBizLcardlost : dataLost) {
//				log.info("lcard_lost_data: {}", evwBizLcardlost);
//				//当Formstate=1 、3、4时，可以申请
//				if(evwBizLcardlost.getFormState() != null && (evwBizLcardlost.getFormState().equals(0) ||
//						evwBizLcardlost.getFormState().equals(2) || evwBizLcardlost.getFormState().equals(5) ||
//						evwBizLcardlost.getFormState().equals(6)))
//				{
//					throw new TCEException(addReplaceApplicationDTO.getPatchDate()+"已在嘉阳PC后台审批中，不能重复申请");
//				}
//			}
//		}
//		Result<List<EvwAcardlostAllRespDTO>> infoAll = remoteEvwAcardlostAllService.info(addReplaceApplicationDTO.getStaffBadge(), addReplaceApplicationDTO.getPatchDate());
//		if(infoAll.isSuccess() && Objects.nonNull(infoAll.getData()))
//		{
//			List<EvwAcardlostAllRespDTO> dataAll = infoAll.getData();
//			 if(dataAll.size()>0)
//			 {
//				 throw new TCEException(addReplaceApplicationDTO.getPatchDate()+"已在嘉阳PC后台历史记录归档，不能重复申请");
//			 }
//		}
		/*
		SmtReplaceApplication one = this.getOne(Wrappers.<SmtReplaceApplication> query().lambda().eq(SmtReplaceApplication::getStaffBadge, addReplaceApplicationDTO.getStaffBadge()).
				eq(SmtReplaceApplication::getStartTime,addReplaceApplicationDTO.getPatchDate()));*/
		//往补卡的实体类赋值
		SmtReplaceApplication replaceApplication = getReplaceApplication(addReplaceApplicationDTO);

/*		boolean save =false;
		if(one!=null) {
			//throw new TCEException("当前时间补卡已申请");
			log.info("修改补卡申请");
			replaceApplication.setId(one.getId());
			save=this.updateById(replaceApplication);
		}*/

		//添加进入数据库
		boolean save = this.save(replaceApplication);
		if(save) {
			//调用补卡申请 获取流程的id
			String processId="";
			try {
				 processId = getProcessId(addReplaceApplicationDTO, replaceApplication.getId());
			}catch (Exception e){
				throw new TCEException(e.getMessage());
			}
			replaceApplication.setProcessId(processId);
			//修改数据，添加流程id
			boolean updateById = this.updateById(replaceApplication);
			if(updateById) {
				getOAProcess(replaceApplication.getProcessId());
			}
		}

	}

	/**
	 * 获取审批流程id
	 */
	public String getProcessId(AddReplaceApplicationDTO addReplaceApplicationDTO,Integer id) {
		String processId = "";
		//补卡审批表
		SendAttendancePatchkAo sendAttendancePatchkAo = new SendAttendancePatchkAo ();
		//获取员工的信息
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,addReplaceApplicationDTO.getStaffBadge()));
		sendAttendancePatchkAo.setEmpNoList(selectOne.getBadge());
		sendAttendancePatchkAo.setBadge(selectOne.getBadge());
		sendAttendancePatchkAo.setName(selectOne.getName());
		sendAttendancePatchkAo.setCompid(selectOne.getCompId());
		sendAttendancePatchkAo.setDepid(selectOne.getDepId());
		sendAttendancePatchkAo.setJobid(selectOne.getJobId());
		sendAttendancePatchkAo.setJchenid(selectOne.getJcheId());
		if(ObjectUtil.isNotNull(selectOne.getEId())) {
			sendAttendancePatchkAo.setEid(selectOne.getEId().toString());
		} else {
			sendAttendancePatchkAo.setEid("");
		}
		sendAttendancePatchkAo.setKQSTARTDATE(addReplaceApplicationDTO.getPatchDate());
	/*	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sendAttendancePatchkAo.setKQSTARTDATE(DateUtils.format(DateUtils.parseDate(addReplaceApplicationDTO.getPatchDate()), format));
*/		sendAttendancePatchkAo.setKQINTIME2(addReplaceApplicationDTO.getSecondEnter());
		sendAttendancePatchkAo.setKQOUTTIME2(addReplaceApplicationDTO.getSecondOut());
		if(!StringUtils.isEmpty(addReplaceApplicationDTO.getSecondOutCover())) {
			sendAttendancePatchkAo.setOUT2(Integer.parseInt(addReplaceApplicationDTO.getSecondOutCover()));
		}else{
			sendAttendancePatchkAo.setOUT2(null);
		}
		sendAttendancePatchkAo.setKQINTIME4(addReplaceApplicationDTO.getFourthEnter());
		sendAttendancePatchkAo.setKQOUTTIME4(addReplaceApplicationDTO.getFourthOut());
		if(!StringUtils.isEmpty(addReplaceApplicationDTO.getFourthEnterCover())) {
			sendAttendancePatchkAo.setIN4(Integer.parseInt(addReplaceApplicationDTO.getFourthEnterCover()));
		}else{
			sendAttendancePatchkAo.setIN4(null);
		}
		if(!StringUtils.isEmpty(addReplaceApplicationDTO.getFourthOutCover())) {
			sendAttendancePatchkAo.setOUT4(Integer.parseInt(addReplaceApplicationDTO.getFourthOutCover()));
		}else
		{
			sendAttendancePatchkAo.setOUT4(null);
		}
		sendAttendancePatchkAo.setKQINTIME5(addReplaceApplicationDTO.getFifthEnter());
		sendAttendancePatchkAo.setKQOUTTIME5(addReplaceApplicationDTO.getFifthOut());
		if(com.tce.smart.common.core.util.StringUtils.isNotEmpty(addReplaceApplicationDTO.getFifthEnterCover())) {
			sendAttendancePatchkAo.setIN5(Integer.parseInt(addReplaceApplicationDTO.getFifthEnterCover()));
		}else
		{
			sendAttendancePatchkAo.setIN5(null);
		}
		if(com.tce.smart.common.core.util.StringUtils.isNotEmpty(addReplaceApplicationDTO.getFifthOutCover())) {
			sendAttendancePatchkAo.setOut5(Integer.parseInt(addReplaceApplicationDTO.getFifthOutCover()));
		}else
		{
			sendAttendancePatchkAo.setOut5(null);
		}

		//获取班次
		Result<KQShiftDetailsRespDTO> resultShift = remoteKQShiftDetailsService.info(selectOne.getBadge(),addReplaceApplicationDTO.getPatchDate(), SecurityConstants.FROM_IN);
		if (CommonConstants.SUCCESS  == resultShift.getCode()) {
			if(ObjectUtil.isNotNull(resultShift.getData())) {
				sendAttendancePatchkAo.setShift(resultShift.getData().getRunName());
				sendAttendancePatchkAo.setStdIn2(resultShift.getData().getRun2StartTime());
				sendAttendancePatchkAo.setStdOt2(resultShift.getData().getRun2EndTime());
				sendAttendancePatchkAo.setStdIn4(resultShift.getData().getRun4StartTime());
				sendAttendancePatchkAo.setStdOt4(resultShift.getData().getRun4EndTime());
				sendAttendancePatchkAo.setStdIn5(resultShift.getData().getRun5StartTime());
				sendAttendancePatchkAo.setStdOt5(resultShift.getData().getRun5EndTime());
			}
		}
		sendAttendancePatchkAo.setReason(addReplaceApplicationDTO.getPatchReason());
		sendAttendancePatchkAo.setREMARKS(addReplaceApplicationDTO.getRemark());
		sendAttendancePatchkAo.setFJ("");
		sendAttendancePatchkAo.setERRMSG("提示信息");
		//获取人事区域
		Result<OvwYscompRespDTO> resultComp = remoteOvwYscompService.getByCompId(selectOne.getCompId(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		OvwYscompRespDTO ovwYscompVO = resultComp.getData();
		sendAttendancePatchkAo.setEzid(ovwYscompVO.getEzid().toString());
		sendAttendancePatchkAo.setSeqid(id.toString());
		//获取流程id


		log.info("remoteOaWorkFlowService.param:"+sendAttendancePatchkAo);
		Result<String> result = remoteOaWorkFlowService.sendRest(sendAttendancePatchkAo);
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
	  * 获取补卡
	 * @param addReplaceApplicationDTO
	 * @return
	 */
	private SmtReplaceApplication getReplaceApplication(AddReplaceApplicationDTO addReplaceApplicationDTO) {
		SmtReplaceApplication smtReplaceApplication = new SmtReplaceApplication ();
		//根据员工编号查询员工的信息
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,addReplaceApplicationDTO.getStaffBadge()));
		smtReplaceApplication.setStaffId(selectOne.getId());
		smtReplaceApplication.setStaffBadge(addReplaceApplicationDTO.getStaffBadge());
		smtReplaceApplication.setStaffName(selectOne.getName());
		smtReplaceApplication.setWorkMonth(addReplaceApplicationDTO.getPatchMonth());
		smtReplaceApplication.setStartTime(addReplaceApplicationDTO.getPatchDate());
		smtReplaceApplication.setCause(Integer.parseInt(addReplaceApplicationDTO.getPatchReason()));
		smtReplaceApplication.setCreateTime(DateUtil.date());
		smtReplaceApplication.setStartDateTwo(addReplaceApplicationDTO.getSecondEnter());
		smtReplaceApplication.setEndDateTwo(addReplaceApplicationDTO.getSecondOut());

		if(!StringUtils.isEmpty(addReplaceApplicationDTO.getSecondOutCover())) {
			smtReplaceApplication.setEndTwoCover(Integer.parseInt(addReplaceApplicationDTO.getSecondOutCover()));
		}
		smtReplaceApplication.setStartDateFour(addReplaceApplicationDTO.getFourthEnter());
		smtReplaceApplication.setEndDateFour(addReplaceApplicationDTO.getFourthOut());
		if(!StringUtils.isEmpty(addReplaceApplicationDTO.getFourthEnterCover())) {
			smtReplaceApplication.setStartFourCover(Integer.parseInt(addReplaceApplicationDTO.getFourthEnterCover()));
		}
		if(!StringUtils.isEmpty(addReplaceApplicationDTO.getFourthOutCover())) {
			smtReplaceApplication.setEndFourCover(Integer.parseInt(addReplaceApplicationDTO.getFourthOutCover()));
		}
		smtReplaceApplication.setStartDateFive(addReplaceApplicationDTO.getFifthEnter());
		smtReplaceApplication.setEndDateFive(addReplaceApplicationDTO.getFifthOut());
		if(!StringUtils.isEmpty(addReplaceApplicationDTO.getFifthEnterCover())) {
			smtReplaceApplication.setStartFiveCover(Integer.parseInt(addReplaceApplicationDTO.getFifthEnterCover()));
		}
		if(!StringUtils.isEmpty(addReplaceApplicationDTO.getFifthOutCover())) {
			smtReplaceApplication.setEndFiveCover(Integer.parseInt(addReplaceApplicationDTO.getFifthOutCover()));
		}
		smtReplaceApplication.setRemark(addReplaceApplicationDTO.getRemark());
		if(!StringUtils.isEmpty(addReplaceApplicationDTO.getPhoto())){
		smtReplaceApplication.setPhotoId(getPhotoId(0,addReplaceApplicationDTO.getPhoto()));
		}
		return smtReplaceApplication;
	}
	/**
	 * 根据图片base64获取图片的id
	 * @param  photo
	 */
	public String getPhotoId(Integer parkId,String photo) {
		if (!StringUtils.isEmpty(photo)) {
			try {
				return smtImageService.saveImage(parkId,photo, SmtImageEnum.TYPE_REPLACE_APPLICATION.getCode());
			} catch (Exception e) {
				log.error("下载照片异常", e);
			}
		}
		return null;
	}
	/**
	 *正则判断
	 * @param addReplaceApplicationDTO
	 * @return
	 */
	private ExceptionTypeEnum ReplaceCheck(AddReplaceApplicationDTO addReplaceApplicationDTO) {
		String photo = addReplaceApplicationDTO.getPhoto();
		if(!StringUtils.isEmpty(photo)){
			addReplaceApplicationDTO.setPhoto(photo.substring(0,(photo.length() > 100 ? 10 : photo.length())));
		}

		log.info("addReplaceApplicationDTO:"+addReplaceApplicationDTO);
		addReplaceApplicationDTO.setPhoto(photo);
		String staffBadge = addReplaceApplicationDTO.getStaffBadge();
		String patchMonth = addReplaceApplicationDTO.getPatchMonth();
		String patchDate = addReplaceApplicationDTO.getPatchDate();
//		String secondEnter = addReplaceApplicationDTO.getSecondEnter();
//		String secondOut = addReplaceApplicationDTO.getSecondOut();
//		String secondOutCover = addReplaceApplicationDTO.getSecondOutCover();
//		String fourthEnter = addReplaceApplicationDTO.getFourthEnter();
//		String fourthOut = addReplaceApplicationDTO.getFourthOut();
//		String fourthEnterCover = addReplaceApplicationDTO.getFourthEnterCover();
//		String fourthOutCover = addReplaceApplicationDTO.getFourthOutCover();
		String fifthEnter=addReplaceApplicationDTO.getFifthEnter();
		String fifthout=addReplaceApplicationDTO.getFifthOut();
		if(StringUtils.isEmpty(staffBadge)){
			return ExceptionTypeEnum.REPLACE_STAFF_BADGE_NULL;
		}
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,staffBadge));
		if(selectOne==null){
			return ExceptionTypeEnum.REPLACE_STAFF_BADGE_PARAMETER;
		}
		if(StringUtils.isEmpty(patchMonth)){
			return ExceptionTypeEnum.REPLACE_PATCH_MONTH_NULL;
		}
		if(StringUtils.isEmpty(patchDate)){
			return ExceptionTypeEnum.REPLACE_PATCH_DATE_NULL;
		}

		if(Objects.nonNull(fifthEnter) ||Objects.nonNull(fifthout))
		{
			if(!fifthEnter.equals("") || !fifthout.equals("") )
			{
				throw new TCEException("5入5出为加班卡，不能进行系统补卡，请与部门文员确认后线下补卡");
			}
		}
		return ExceptionTypeEnum.CHECK_SUCCESS;
	}

	/**
	 * 获取补卡记录分页列表
	 */
	public Page<SearchReplaceApplicationVO> getSmtReplaceApplicationPage(Page page,
			SmtReplaceApplication smtReplaceApplication) {
		//判断参数是否为空值
		if(StringUtils.isEmpty(smtReplaceApplication.getStaffBadge())){
			throw new TCEException(ExceptionTypeEnum.REPLACE_STAFF_BADGE_NULL);
		}
		Page<SearchReplaceApplicationVO> selectPage = this.baseMapper.getSmtReplaceApplicationPage(page, smtReplaceApplication);
		//判断是否为空
		if(selectPage.getSize()>0) {
			for (int i = 0; i < selectPage.getRecords().size(); i++) {
				//根据流程的id查询最新的补卡状态值
				String processId = selectPage.getRecords().get(i).getProcessId();
				if(!StringUtils.isEmpty(processId)) {
					selectPage.getRecords().get(i).setRecordDesc(smtProcessRecordService.getStatus(processId));
				}
				//根据补卡原因id获取补卡的原因描述
				Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.REPLACE_REASON,selectPage.getRecords().get(i).getCause().toString(), SecurityConstants.FROM_IN);
				//判断是否为空
				if(findByType.getData()!=null) {
					//根据字典表查询补卡原因类型数据
					selectPage.getRecords().get(i).setPatchReasonDesc(findByType.getData().getLabel());
				}
				//根据补卡开始时间查询考勤的异常信息，根据考勤的异常信息判断缺卡几次，获取班次
				Integer missPatchCount =0;
				//获取班次名称
				String classDesc ="";
				//根据时间查询考勤异常数据，调用考勤异常接口
/*				Result<LvwAttendYcxxVO> info = remoteLvwAttendYcxxService.info("019541", "2017-09-15", selectPage.getRecords().get(i).getPatchDate(), SecurityConstants.FROM_IN);
*/				Result<LvwAttendYcxxSimpleRespDTO> info = remoteLvwAttendYcxxService.info(smtReplaceApplication.getStaffBadge(), selectPage.getRecords().get(i).getPatchDate(), selectPage.getRecords().get(i).getPatchDate(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
				//查班次
				Result<KQShiftDetailsRespDTO> result = remoteKQShiftDetailsService.info(smtReplaceApplication.getStaffBadge(), selectPage.getRecords().get(i).getPatchDate(), SecurityConstants.FROM_IN);

				if(info.getCode() == CommonConstants.SUCCESS) {
					//判断异常是否有数据，如果没有则查询班次的信息
					if(!checkObjAllFieldsIsNull(info.getData())) {
						if(StringUtils.isEmpty(info.getData().getIn1())) {
							missPatchCount++;
						}if(StringUtils.isEmpty(info.getData().getIn2())) {
							missPatchCount++;
						}if(StringUtils.isEmpty(info.getData().getIn3()) && com.tce.smart.common.core.util.StringUtils.isNotEmpty(result.getData().getRun5StartTime())) {
							missPatchCount++;
						}if(StringUtils.isEmpty(info.getData().getOut1())) {
							missPatchCount++;
						}if(StringUtils.isEmpty(info.getData().getOut2())) {
							missPatchCount++;
						}if(StringUtils.isEmpty(info.getData().getOut3())  && com.tce.smart.common.core.util.StringUtils.isNotEmpty(result.getData().getRun5EndTime())) {
							missPatchCount++;
						}
						classDesc  = info.getData().getShift();
					}else {
						//获取班次信息,查看打卡次数  没做
/*						Result<KQShiftDetails> infoShift = remoteKQShiftDetailsService.info("103192",selectPage.getRecords().get(i).getPatchDate(),"",SecurityConstants.FROM_IN);
*/						Result<KQShiftDetailsRespDTO> infoShift = remoteKQShiftDetailsService.info(smtReplaceApplication.getStaffBadge(),selectPage.getRecords().get(i).getPatchDate(),SecurityConstants.FROM_IN);
						//判断班次的信息是否查询正确
						if(infoShift.getCode() == CommonConstants.SUCCESS) {
							if(!Objects.isNull(infoShift.getData())) {
/*								missPatchCount=6;
*/								classDesc  = infoShift.getData().getRunName();
							}
						}else {
							throw new TCEException(infoShift.getCode(),info.getMsg());
						}
					}
				}else {
					throw new TCEException(info.getCode(),info.getMsg());
				}

				selectPage.getRecords().get(i).setClassDesc(classDesc);
				selectPage.getRecords().get(i).setMissPatchCount(missPatchCount);
			}
		}
		return selectPage;
	}

	//获取补卡详情
	public ReplaceApplicationDetailVO getSmtReplaceApplicationDetail( Integer id) {

		ReplaceApplicationDetailVO vo=new  ReplaceApplicationDetailVO();
		//获取到补卡信息
		SmtReplaceApplication selectById = this.baseMapper.selectById(id);
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,selectById.getStaffBadge()));
		vo.setStartTime(selectById.getStartTime());
		vo.setRemark(selectById.getRemark()==null?"":selectById.getRemark());
		vo.setWorkMonth(selectById.getWorkMonth());
		//根据补卡原因id获取补卡的原因描述
		Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.REPLACE_REASON,selectById.getCause().toString(), SecurityConstants.FROM_IN);
		//判断是否为空
		if(findByType.getData()!=null) {
			//根据字典表查询补卡原因类型数据
			vo.setCauseDesc(findByType.getData().getLabel());
		}else
		{
			vo.setCauseDesc("");
		}
		if( !Objects.isNull(selectById.getPhotoId()))
		{
			String facePicUrl = imageService.buildImageUrl(selectById.getPhotoId());
			vo.setPhotoUrl(facePicUrl);
		}else
		{
			vo.setPhotoUrl("");
		}

		List<FlowVO> flowList = new ArrayList<FlowVO> ();
		//根据流程id获取流程
		if(!StringUtils.isEmpty(selectById.getProcessId())) {
			getOAProcessFlow(selectById.getProcessId(),flowList);
		}
		vo.setFlow(flowList);

	/*	String secondEnter = "";
		String secondOut = "";
		String fourthEnter = "";
		String fourthOut = "";
		String fifthEnter = "";
		String fifthOut = "";*/
		//获取班次名称
		//根据时间查询考勤异常数据，调用考勤异常接口
		/*Result<LvwAttendYcxxVO> info = remoteLvwAttendYcxxService.info(selectById.getStaffBadge(), selectById.getStartTime(), selectById.getStartTime(),SecurityConstants.FROM_IN);
		//查询补卡记录
		Result<LvwAcardlost> lvwAcardlost = remoteLvwAcardlostService.getByBadge(selectById.getStaffBadge(),selectById.getStartTime(),SecurityConstants.FROM_IN);
		log.info("remoteLvwAttendYcxxService.result"+info);
		log.info("remoteLvwAcardlostService.result"+lvwAcardlost);

		//查班次
		Result<KQShiftDetails> result= remoteKQShiftDetailsService.info(selectById.getStaffBadge(), selectById.getStartTime(), SecurityConstants.FROM_IN);
		*/
		/*if(lvwAcardlost.isSuccess() &&!StringUtils.isEmpty(lvwAcardlost.getData()))
		{
					secondEnter = lvwAcardlost.getData().getKqintime2();
					vo.setSecondEnter(secondEnter);
					fourthEnter = result.getData().getRun4StartTime();
					vo.setFourthEnter(fourthEnter);
					fifthEnter = result.getData().getRun5StartTime();
					vo.setFifthEnter(fifthEnter);
					secondOut = result.getData().getRun2EndTime();
					vo.setSecondOut(secondOut);
					fourthOut = result.getData().getRun4EndTime();
					vo.setFourthOut(fourthOut);
					fifthOut = result.getData().getRun5EndTime();
					vo.setFifthOut(fifthOut);
				log.info("vo:"+vo);
		}else{
			if(info.getCode() == CommonConstants.SUCCESS) {
				//判断异常是否有数据，如果没有则查询班次的信息
				if(!checkObjAllFieldsIsNull(info.getData())) {
					if(StringUtils.isEmpty(info.getData().getIn1())) {
						secondEnter = result.getData().getRun2StartTime();
						vo.setSecondEnter(secondEnter);
					}if(StringUtils.isEmpty(info.getData().getIn2())) {
						fourthEnter = result.getData().getRun4StartTime();
						vo.setFourthEnter(fourthEnter);
					}if(StringUtils.isEmpty(info.getData().getIn3()) && com.tce.smart.common.core.util.StringUtils.isNotEmpty(result.getData().getRun5StartTime())) {
						fifthEnter = result.getData().getRun5StartTime();
						vo.setFifthEnter(fifthEnter);
					}if(StringUtils.isEmpty(info.getData().getOut1())) {
						secondOut = result.getData().getRun2EndTime();
						vo.setSecondOut(secondOut);
					}if(StringUtils.isEmpty(info.getData().getOut2())) {
						fourthOut = result.getData().getRun4EndTime();
						vo.setFourthOut(fourthOut);
					}if(StringUtils.isEmpty(info.getData().getOut3())  && com.tce.smart.common.core.util.StringUtils.isNotEmpty(result.getData().getRun5EndTime())) {
						fifthOut = result.getData().getRun5EndTime();
						vo.setFifthOut(fifthOut);
					}
				}
			}
		}*/
		if(!StringUtils.isEmpty(selectById.getEndTwoCover()))
		{
			vo.setSecondOutCover(selectById.getEndTwoCover().toString());
		}
		if(!StringUtils.isEmpty(selectById.getStartFourCover()))
		{
			vo.setFourthEnterCover(selectById.getStartFourCover().toString());
		}
		if(!StringUtils.isEmpty(selectById.getEndFourCover()))
		{
			vo.setFourthOutCover(selectById.getEndFourCover().toString());
		}
		if(!StringUtils.isEmpty(selectById.getStartFiveCover()))
		{
			vo.setFifthEnterCover(selectById.getStartFiveCover().toString());
		}
		if(!StringUtils.isEmpty(selectById.getEndFiveCover()))
		{
			vo.setFifthOutCover(selectById.getEndFiveCover().toString());
		}
		vo.setSecondEnter(selectById.getStartDateTwo());
		vo.setFourthEnter(selectById.getStartDateFour());
		vo.setFifthEnter(selectById.getStartDateFive());
		vo.setSecondOut(selectById.getEndDateTwo());
		vo.setFourthOut(selectById.getEndDateFour());
		vo.setFifthOut(selectById.getEndDateFive());
		vo.setCreateTime(selectById.getCreateTime());
		vo.setProcessId(selectById.getProcessId());
		vo.setEmployeeBadge(selectById.getStaffBadge());
		vo.setEmployeeName(selectById.getStaffName());
		vo.setBuName(selectOne.getCompName());
		vo.setDepName(selectOne.getDepName());
		vo.setJobName(selectOne.getJobName());
		log.info("vo:"+vo);
		return vo;

	}

	/**
	 * 获取补卡的信息
	 */
	public SearchPatchVO getPatchApplication(SearchPatchDTO searchPatchDTO) {
		SearchPatchVO searchPatchVO = new SearchPatchVO ();
		//判断补卡员工号是否为空值
		if(StringUtils.isEmpty(searchPatchDTO.getStaffBadge())){
			throw new TCEException(ExceptionTypeEnum.REPLACE_STAFF_BADGE_NULL);
		}
		//判断补卡日期是否为空值
		if(StringUtils.isEmpty(searchPatchDTO.getPatchDate())){
			throw new TCEException(ExceptionTypeEnum.REPLACE_PATCH_DATE_NULL);
		}

		Result<LvwAttendYcxxSimpleRespDTO> info = remoteLvwAttendYcxxService.info(searchPatchDTO.getStaffBadge(), searchPatchDTO.getPatchDate(), searchPatchDTO.getPatchDate(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if(info.isSuccess())
		{
			if(ObjectUtil.isNotNull(info.getData()))
			{
				LvwAttendYcxxSimpleRespDTO data = info.getData();
				if(ObjectUtil.isNull(data.getIn1()))
				{
					//二进没有打卡点时，返回二进班次的卡点
					searchPatchVO.setSecondEnter(data.getStdIn2());
				}
				if(ObjectUtil.isNull(data.getOut1()))
				{
					//二出没有打卡点时，返回二出班次的卡点
					searchPatchVO.setSecondOut(data.getStdOt2());
				}
				if(ObjectUtil.isNull(data.getIn2()))
				{
					//4进没有打卡点时，返回4进班次的卡点
					searchPatchVO.setFourthEnter(data.getStdIn4());
				}
				if(ObjectUtil.isNull(data.getOut2()))
				{
					//4出没有打卡点时，返回4出班次的卡点
					searchPatchVO.setFourthOut(data.getStdOt4());
				}
			}
		}

		return searchPatchVO;
	}

	/**
	 *获取出勤的信息
	 */
	public List<SearchAttendanceVO> getAttendance(SearchAttendanceDTO searchAttendanceDTO) {
		List<SearchAttendanceVO> list = new ArrayList<> ();
		//判断员工号是否为空值
		if(StringUtils.isEmpty(searchAttendanceDTO.getStaffBadge())){
			throw new TCEException(ExceptionTypeEnum.REPLACE_STAFF_BADGE_NULL);
		}
		//判断年月份是否为空
		if(Objects.isNull(searchAttendanceDTO.getQueryDay())){
			throw new TCEException(ExceptionTypeEnum.REPLACE_YEAR_MONTH_NULL);
		}
		if(!RegexUtils.matchYearMonth(searchAttendanceDTO.getQueryDay())) {
			throw new TCEException(ExceptionTypeEnum.REPLACE_YEAR_MONTH_PARAMETER);
		}
		//如果传过来的月份比当前的月份大则直接返回值
		int compare_date = compare_date(searchAttendanceDTO.getQueryDay(),getNowDate());
		int compare_dates = 0;
		//判断员工入职时间 和传过来的时间判断
		log.info("------------------");
		log.info("------------------"+ searchAttendanceDTO);
	SmtStaff selectOneStaff = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,searchAttendanceDTO.getStaffBadge()));
		if(ObjectUtil.isNotNull(selectOneStaff)) {
			SimpleDateFormat formatDay = new SimpleDateFormat("yyyy-MM");
			compare_dates = compare_date(searchAttendanceDTO.getQueryDay(),DateUtils.format(selectOneStaff.getCreateTime(), formatDay));
		}
		//判断传入日期等于或者在当前年月份
		if(compare_date!=-1 || compare_dates!=-1) {
			//for循环传入时间的月份每天的日期，到截止当天日期
			DateFormat parser = new SimpleDateFormat("yyyy-MM");
			int yearParam = DateUtils.parse(searchAttendanceDTO.getQueryDay(), parser).year();
			int monthParam = DateUtils.parse(searchAttendanceDTO.getQueryDay(), parser).month()+1;
			List<String> dayByMonth = getMonthFullDay(yearParam,monthParam);
			for (int i = 0; i < dayByMonth.size(); i++) {
				SearchAttendanceVO searchAttendanceVO = new SearchAttendanceVO ();
				//判断获取的时间是否等于当前的时间，跳出循环
				if(dayByMonth.get(i).equals(getNowDateDay())) {
					break;
				}
				SimpleDateFormat formatDay = new SimpleDateFormat("yyyy-MM-dd");
				//时间做比较
				if(ObjectUtil.isNotNull(selectOneStaff)) {
				int compare_dateDay = compare_dateDay(dayByMonth.get(i),DateUtils.format(selectOneStaff.getCreateTime(), formatDay));
				if(compare_dateDay!=-1 && compare_dateDay!=0) {
					continue;
				}
				}
				//判断是否有考勤异常的数据，如果有则为异常没有则正常
				Result<LvwAttendYcxxSimpleRespDTO> info = remoteLvwAttendYcxxService.info(searchAttendanceDTO.getStaffBadge(), dayByMonth.get(i),dayByMonth.get(i), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
				log.info("remoteLvwAttendYcxxService.info-Result={}"+info);
				//判断异常数据是否存在
				searchAttendanceVO.setIsRecord(String.valueOf(AttendanceEnum.Is_CHECK_STATE.getCode()));

				if(info.getCode().equals(CommonConstants.SUCCESS) &&ObjectUtil.isNotNull(info) ) {
					if(ObjectUtil.isNotNull(info.getData()))
					{
						if(("缺卡").equals(info.getData().getType())) {
							searchAttendanceVO.setCheckState(String.valueOf(AttendanceEnum.Not_CHECK_STATE.getCode()));
						}else {
							searchAttendanceVO.setCheckState(String.valueOf(AttendanceEnum.Is_CHECK_STATE.getCode()));
						}
							/*//判断是否补卡,如果补卡了则正常显示
								List<SmtReplaceApplication> selectOne = this.baseMapper.selectList(
										Wrappers.<SmtReplaceApplication>query().lambda()
										.eq(SmtReplaceApplication::getStaffBadge, searchAttendanceDTO.getStaffBadge())
										.eq(SmtReplaceApplication::getStartTime,dayByMonth.get(i)).orderByDesc(SmtReplaceApplication::getCreateTime)
										);
								//判断是否已经补卡,并且是否已经审批通过
								if(selectOne.size()>0) {
									searchAttendanceVO.setIsRecord(String.valueOf(AttendanceEnum.Is_CHECK_STATE.getCode()));
									//判断是否通过
									if(!StringUtils.isEmpty(selectOne.get(0).getProcessId())) {
										List<SmtProcessRecord> selectList = smtProcessRecordService.list(Wrappers.<SmtProcessRecord> query().lambda().eq(SmtProcessRecord::getProcessId, selectOne.get(0).getProcessId()).orderByDesc(SmtProcessRecord::getRecordDate));
										if(selectList.size()>0) {
											//查询流程的最新的状态数据
											if(selectList.get(0).getStatementStatus().equals(ApplicationEnum.RECORD_STATUS_e.getCode()) || selectList.get(0).getStatementStatus().equals(ApplicationEnum.RECORD_STATUS_0.getCode())) {
												searchAttendanceVO.setCheckState(String.valueOf(AttendanceEnum.Is_CHECK_STATE.getCode()));
											}else if(selectList.get(0).getStatementStatus().equals(ApplicationEnum.RECORD_STATUS_3.getCode()))
											{
												searchAttendanceVO.setIsRecord(String.valueOf(AttendanceEnum.Not_CHECK_STATE.getCode()));
											}
										}
									}
								}*/

					} else {
						searchAttendanceVO.setCheckState(String.valueOf(AttendanceEnum.Is_CHECK_STATE.getCode()));
					}
				}
				searchAttendanceVO.setMonthlyDay(String.valueOf(getDay(dayByMonth.get(i))));
				searchAttendanceVO.setFullDate(dayByMonth.get(i));
				list.add(searchAttendanceVO);
			}
		}
		return list;
	}


	   /**
     * 判断对象中属性值是否全为空
     *
     * @param object
     * @return
     */
    public static boolean checkObjAllFieldsIsNull(Object object) {
        if (null == object) {
            return true;
        }

        try {
            for (Field f : object.getClass().getDeclaredFields()) {
                f.setAccessible(true);

                if (f.get(object) != null && !StringUtils.isEmpty(f.get(object).toString())) {
                    return false;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
	//根据年月获取每天的日期
	public List<String> getMonthFullDay(int year , int month){
		SimpleDateFormat dateFormatYYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
		List<String> fullDayList = new ArrayList<>(32);
		// 获得当前日期对象
		Calendar cal = Calendar.getInstance();
		cal.clear();// 清除信息
		cal.set(Calendar.YEAR, year);
		//  1月从0开始
		cal.set(Calendar.MONTH, month-1 );
		// 当月1号
		cal.set(Calendar.DAY_OF_MONTH,1);
		int count = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		for (int j = 1; j <= count ; j++) {
			fullDayList.add(dateFormatYYYYMMDD.format(cal.getTime()));
			cal.add(Calendar.DAY_OF_MONTH,1);
		}
		return fullDayList;
	}
	//获取指定时间的Day
	private Integer getDay(String time) {
		SimpleDateFormat dateFormatYYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = dateFormatYYYYMMDD.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Calendar now = Calendar.getInstance();
		now.setTime(date);

		int day = now.get(Calendar.DAY_OF_MONTH);
		return day;
	}
	//获取当前的日期年和月
	private  String getNowDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		Date d = new Date();
		return sdf.format(d);
	}
	//获取当前的日期年和月和day
	private  String getNowDateDay() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();
		return sdf.format(d);
	}

	//比较两个日期的大小
	public  int compare_date(String DATE1, String DATE2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM");
		try {
			Date dt1 = df.parse(DATE1);
			Date dt2 = df.parse(DATE2);
			if (dt1.getTime() < dt2.getTime()) {
				return 1;
			} else if (dt1.getTime() > dt2.getTime()) {
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}
	//比较两个日期年月日的大小
	public  int compare_dateDay(String DATE1, String DATE2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date dt1 = df.parse(DATE1);
			Date dt2 = df.parse(DATE2);
			if (dt1.getTime() < dt2.getTime()) {
				return 1;
			} else if (dt1.getTime() > dt2.getTime()) {
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}

	/**
	 * 获取考勤的详细数据
	 */
	public SearchAttendanceDetailVO getAttendanceDetail(SearchAttendanceDTO searchAttendanceDTO) {
/*		searchAttendanceDTO.setStaffBadge("050982");
*/		//判断补卡员工号是否为空值
		if(StringUtils.isEmpty(searchAttendanceDTO.getStaffBadge())){
			throw new TCEException(ExceptionTypeEnum.REPLACE_STAFF_BADGE_NULL);
		}
		//判断补卡日期是否为空值
		if(StringUtils.isEmpty(searchAttendanceDTO.getQueryDay())){
			throw new TCEException(ExceptionTypeEnum.REPLACE_YEAR_MONTH_NULL);
		}
		//正则判断年月日
		if(!RegexUtils.matchYearMonthDay(searchAttendanceDTO.getQueryDay())) {
			throw new TCEException(ExceptionTypeEnum.REPLACE_YEAR_MONTH_DAY_PARAMETER);
		}
		//根据时间判断打卡的次数
		Integer totalPunchCount = 0;
		String secondEnter = "";
		String secondOut = "";
		String fourthEnter = "";
		String fourthOut = "";
		String fifthEnter = "";
		String fifthOut = "";

		//没做  根据班次或者考勤异常获取员工号和员工姓名
		String employeeBadge = "";
		String employeeName = "";
		String classDesc = "";
		//根据时间查询考勤异常数据，调用考勤异常接口
		Result<LvwAttendYcxxSimpleRespDTO> info = remoteLvwAttendYcxxService.info(searchAttendanceDTO.getStaffBadge(),searchAttendanceDTO.getQueryDay(),searchAttendanceDTO.getQueryDay(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if(info.getCode()==CommonConstants.SUCCESS.intValue()) {

			//判断是否补卡,如果补卡了则正常显示
//			Integer selectCount = smtReplaceApplicationMapper.selectCount(
//					Wrappers.<SmtReplaceApplication>query().lambda()
//					.eq(SmtReplaceApplication::getStaffBadge, searchAttendanceDTO.getStaffBadge())
//					.eq(SmtReplaceApplication::getStartTime,searchAttendanceDTO.getQueryDay())
//					);
			//判断异常是否有数据，如果没有则查询班次的信息，或者已经补卡了
//			if(checkObjAllFieldsIsNull(info.getData()) || selectCount>0) {
//							Result<KQShiftDetails> infoShift = remoteKQShiftDetailsService.info(searchAttendanceDTO.getStaffBadge(),searchAttendanceDTO.getQueryDay(),SecurityConstants.FROM_IN);
//								//判断班次的信息是否查询正确
//								if(infoShift.getCode()==CommonConstants.SUCCESS.intValue()) {
//									if(!checkObjAllFieldsIsNull(infoShift.getData())) {
//										if(!StringUtils.isEmpty(infoShift.getData().getRun2StartTime())) {
//											secondEnter = infoShift.getData().getRun2StartTime();
//											totalPunchCount++;
//										}if(!StringUtils.isEmpty(infoShift.getData().getRun4StartTime())) {
//											fourthEnter = infoShift.getData().getRun4StartTime();
//											totalPunchCount++;
//										}if(!StringUtils.isEmpty(infoShift.getData().getRun5StartTime())) {
//											fifthEnter = infoShift.getData().getRun5StartTime();
//											totalPunchCount++;
//										}if(!StringUtils.isEmpty(infoShift.getData().getRun2EndTime())) {
//											secondOut = infoShift.getData().getRun2EndTime();
//											totalPunchCount++;
//										}if(!StringUtils.isEmpty(infoShift.getData().getRun4EndTime())) {
//											fourthOut = infoShift.getData().getRun4EndTime();
//											totalPunchCount++;
//										}if(!StringUtils.isEmpty(infoShift.getData().getRun5EndTime())) {
//											fifthOut = infoShift.getData().getRun5EndTime();
//											totalPunchCount++;
//										}
//										employeeBadge = infoShift.getData().getEmpNo();
//										employeeName =  infoShift.getData().getEmpname();
//										classDesc =  infoShift.getData().getRunName();
//									}
//								}else {
//									throw new TCEException(infoShift.getCode(),infoShift.getMsg());
//								}
//
//			}else {
			if(ObjectUtil.isNotNull(info.getData())) {
				if (!StringUtils.isEmpty(info.getData().getIn1())) {
					secondEnter = info.getData().getIn1();
					totalPunchCount++;
				}
				if (!StringUtils.isEmpty(info.getData().getIn2())) {
					fourthEnter = info.getData().getIn2();
					totalPunchCount++;
				}
				if (!StringUtils.isEmpty(info.getData().getIn3())) {
					fifthEnter = info.getData().getIn3();
					totalPunchCount++;
				}
				if (!StringUtils.isEmpty(info.getData().getOut1())) {
					secondOut = info.getData().getOut1();
					totalPunchCount++;
				}
				if (!StringUtils.isEmpty(info.getData().getOut2())) {
					fourthOut = info.getData().getOut2();
					totalPunchCount++;
				}
				if (!StringUtils.isEmpty(info.getData().getOut3())) {
					fifthOut = info.getData().getOut3();
					totalPunchCount++;
				}
				employeeBadge = info.getData().getBadge();
				employeeName = info.getData().getName();
				classDesc = info.getData().getShift();
//			}
			}
		}else {
			throw new TCEException(info.getCode(),info.getMsg());
		}
		Integer comparisonTime = 0;
		Integer comparisonTime1 = 0;
		Integer comparisonTime2 = 0;
		Integer comparisonTime3 = 0;
		///判断两个时间段是否都有时间 ，如果都有则取两个时间段的出勤时间（分钟）
		if(!StringUtils.isEmpty(secondEnter) && !StringUtils.isEmpty(secondOut) ) {
			comparisonTime1 = comparisonTime(secondEnter,secondOut);
		}if(!StringUtils.isEmpty(fourthEnter) && !StringUtils.isEmpty(fourthOut) ) {
			comparisonTime2 = comparisonTime(fourthEnter,fourthOut);
		}if(!StringUtils.isEmpty(fifthEnter) && !StringUtils.isEmpty(fifthOut) ) {
			comparisonTime3 = comparisonTime(fifthEnter,fifthOut);
		}
		//获取全部打卡时间段的时间的毫秒数
		comparisonTime = comparisonTime1+comparisonTime2+comparisonTime3;
         SearchAttendanceDetailVO searchAttendanceDetailVO = new SearchAttendanceDetailVO ();
		 searchAttendanceDetailVO.setEmployeeBadge(employeeBadge);
		 searchAttendanceDetailVO.setEmployeeName(employeeName);
		 searchAttendanceDetailVO.setWeekInfo(dayForWeek(searchAttendanceDTO.getQueryDay()));
		 searchAttendanceDetailVO.setTotalPunchCount(String.valueOf(totalPunchCount));
		 searchAttendanceDetailVO.setTotalHourCount(String.valueOf(getHour(comparisonTime).intValue()));
		 searchAttendanceDetailVO.setTotalMinCount(String.valueOf(getMinutes(comparisonTime).intValue()));
		 searchAttendanceDetailVO.setSecondEnter(secondEnter);
		 searchAttendanceDetailVO.setSecondOut(secondOut);
		 searchAttendanceDetailVO.setFourthEnter(fourthEnter);
		 searchAttendanceDetailVO.setFourthOut(fourthOut);
		 searchAttendanceDetailVO.setFifthEnter(fifthEnter);
		 searchAttendanceDetailVO.setFifthOut(fifthOut);
		 searchAttendanceDetailVO.setDateInfo(searchAttendanceDTO.getQueryDay());
		 searchAttendanceDetailVO.setClassDesc(classDesc);
		 return searchAttendanceDetailVO;
	}
	/**
	 *获取考勤的详细数据
	 */
	public AttendanceSuccessDetailVO getAttendanceSuccessDetail(SearchAttendanceDTO searchAttendanceDTO) {
		/*		searchAttendanceDTO.setStaffBadge("050982");
		 */		//判断补卡员工号是否为空值
		if(StringUtils.isEmpty(searchAttendanceDTO.getStaffBadge())){
			throw new TCEException(ExceptionTypeEnum.REPLACE_STAFF_BADGE_NULL);
		}
		//判断补卡日期是否为空值
		if(StringUtils.isEmpty(searchAttendanceDTO.getQueryDay())){
			throw new TCEException(ExceptionTypeEnum.REPLACE_YEAR_MONTH_NULL);
		}
		//正则判断年月日
		if(!RegexUtils.matchYearMonthDay(searchAttendanceDTO.getQueryDay())) {
			throw new TCEException(ExceptionTypeEnum.REPLACE_YEAR_MONTH_DAY_PARAMETER);
		}
		//没做  根据班次或者考勤异常获取员工号和员工姓名
		String employeeId = "";
		String employeeName = "";
		String classDesc = "";
				Result<KQShiftDetailsRespDTO> infoShift = remoteKQShiftDetailsService.info(searchAttendanceDTO.getStaffBadge(),searchAttendanceDTO.getQueryDay(),SecurityConstants.FROM_IN);
				//判断班次的信息是否查询正确
				if(infoShift.getCode()==CommonConstants.SUCCESS) {
					if(!checkObjAllFieldsIsNull(infoShift.getData())) {
						employeeId = infoShift.getData().getEmpNo();
						employeeName =  infoShift.getData().getEmpname();
						classDesc =  infoShift.getData().getRunName();
					}
				}else {
					throw new TCEException(infoShift.getCode(),infoShift.getMsg());
				}
				 List<KQCardDetailsRespDTO> list = new ArrayList<> ();
				 List<String> listString = new ArrayList<String> ();
				//查询打卡信息
				Result<List<KQCardDetailsRespDTO>> result = remoteKQCardDetailsService.info(searchAttendanceDTO.getStaffBadge(),searchAttendanceDTO.getQueryDay(),SecurityConstants.FROM_IN);
				log.info("======================");
				log.info(result.toString());
				if (CommonConstants.SUCCESS == result.getCode()) {
					list = result.getData();
					if(list.size()>0) {
						for (int i = 0; i < list.size(); i++) {
							listString.add(list.get(i).getKqtime());
						}
					}
				}
		AttendanceSuccessDetailVO attendanceSuccessDetailVO = new AttendanceSuccessDetailVO ();
		attendanceSuccessDetailVO.setKqTime(listString);
		attendanceSuccessDetailVO.setEmployeeBadge(employeeId);
		attendanceSuccessDetailVO.setEmployeeName(employeeName);
		attendanceSuccessDetailVO.setWeekInfo(dayForWeek(searchAttendanceDTO.getQueryDay()));
		attendanceSuccessDetailVO.setTotalPunchCount(String.valueOf(list.size()));
		attendanceSuccessDetailVO.setDateInfo(searchAttendanceDTO.getQueryDay());
		attendanceSuccessDetailVO.setClassDesc(classDesc);
		return attendanceSuccessDetailVO;
	}

	/**
	 * 判断当前日期是星期几
	 *
	 * @param pTime 修要判断的时间
	 * @return dayForWeek 判断结果
	 * @Exception 发生异常
	 */
	public String dayForWeek(String pTime) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(format.parse(pTime));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int dayForWeek = 0;
		if(c.get(Calendar.DAY_OF_WEEK) == 1){
			dayForWeek = 7;
		}else{
			dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
		}
		switch (dayForWeek) {
		case 1:
			return "星期一";
		case 2:
			return "星期二";
		case 3:
			return "星期三";
		case 4:
			return "星期四";
		case 5:
			return "星期五";
		case 6:
			return "星期六";
		case 7:
			return "星期天";
		}
		return "";
	}

	/**
	 * 获取两个时间段的毫秒数
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static Integer comparisonTime(String startTime,String endTime) {
		//把当前时间和要比较的时间转换为Date类型，目的在于得到这两个时间的毫秒值
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		//转换成date类型
		Date start = null;
		try {
			start = sdf.parse(startTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date end = null;
		try {
			end = sdf.parse(endTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//获得这两个时间的毫秒值后进行处理。
		Long diff = end.getTime() - start.getTime();

		return diff.intValue();
	}
	/**
	 * 根据毫秒获取小时
	 * @return
	 */
	public static Integer getHour(Integer time) {
		Integer hours = (time % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		return hours;
	}
	/**
	 * 根据毫秒获取分钟
	 * @return
	 */
	public static Integer getMinutes(Integer time) {
		Integer minutes = (time % (1000 * 60 * 60)) / (1000 * 60);
		return minutes;
	}

	/**
	 * 获取补卡次数
	 */
	public PatchCountVO getPatchCount(SearchPatchDTO searchPatchDTO) {
		PatchCountVO patchCountVO = new PatchCountVO ();
		if(StringUtils.isEmpty(searchPatchDTO.getStaffBadge())){
			throw new TCEException(ExceptionTypeEnum.REPLACE_STAFF_BADGE_NULL);
		}
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,searchPatchDTO.getStaffBadge()));
		if(selectOne==null){
			throw new TCEException(ExceptionTypeEnum.REPLACE_STAFF_BADGE_PARAMETER);
		}

		if(StringUtils.isEmpty(searchPatchDTO.getPatchDate())){
			throw new TCEException(ExceptionTypeEnum.REPLACE_PATCH_MONTH_NULL);
		}
//		Integer selectCount = this.baseMapper.selectCount(
//				Wrappers.<SmtReplaceApplication>query().lambda()
//				.eq(SmtReplaceApplication::getStaffBadge, searchPatchDTO.getStaffBadge())
//				.ge(SmtReplaceApplication::getCreateTime, DateUtil.beginOfMonth(DateUtils.parse(searchPatchDTO.getPatchDate())))
//				.le(SmtReplaceApplication::getCreateTime, DateUtil.endOfMonth(DateUtils.parse(searchPatchDTO.getPatchDate())))
//				);

		Integer patchCount=0;
		List<SmtReplaceApplication> list = this.list(Wrappers.<SmtReplaceApplication>query().lambda()
				.eq(SmtReplaceApplication::getStaffBadge, searchPatchDTO.getStaffBadge())
				.ge(SmtReplaceApplication::getCreateTime, DateUtil.beginOfMonth(DateUtils.parse(searchPatchDTO.getPatchDate())))
				.le(SmtReplaceApplication::getCreateTime, DateUtil.endOfMonth(DateUtils.parse(searchPatchDTO.getPatchDate()))));
		for (SmtReplaceApplication smtReplaceApplication : list) {
			Result<List<EvwBizLcardlostRespDTO>> infoLost = remoteEvwBizLcardlostService.info(smtReplaceApplication.getStaffBadge(), smtReplaceApplication.getStartTime());
			log.info("remoteEvwBizLcardlostService.info {}",infoLost);
			List<EvwBizLcardlostRespDTO> dataLost = infoLost.getData();
			if(dataLost.size()>0)
			{
				for (EvwBizLcardlostRespDTO evwBizLcardlost : dataLost) {
					if(evwBizLcardlost.getFormState() != null && evwBizLcardlost.getFormState().equals(4))
					{
						patchCount++;
					}
				}
			}
		}
		patchCountVO.setPatchCount(patchCount.toString());
		return patchCountVO;
	}

	@Override
	public List<FlowVO> getInfoFlow(Integer id) {
		List<FlowVO> list = new ArrayList<FlowVO> ();
		SmtReplaceApplication selectOne = this.baseMapper.selectOne(Wrappers.<SmtReplaceApplication> query().lambda()
				.eq(SmtReplaceApplication::getId,id)
				);
		if(ObjectUtil.isNotNull(selectOne)) {
			getOAProcessFlow(selectOne.getProcessId(),list);

		}
		return list;
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


		/**
		 * 考勤异常消息推送
		 */
		public void patchErrorPushMsg() {

			log.info("考勤异常消息推送");
			//查询昨天异常
			Result<List<LvwAttendYcxxFullRespDTO>> result = remoteLvwAttendYcxxService.infoAll(DateUtils.formatDateTime(DateUtils.offsetDay(DateUtils.date(), -1)), DateUtils.now(),SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
				//转换接过来的值
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				if (CommonConstants.SUCCESS  == result.getCode()) {
					List<LvwAttendYcxxFullRespDTO> list = result.getData();
					log.info("异常考勤个数："+list);
					for (LvwAttendYcxxFullRespDTO lvwAttendYcxx : list) {
						int staffCount = smtStaffService.count(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,lvwAttendYcxx.getBadge()));
						if(staffCount>0) {
							//根据异常的员工号和时间查询是否已经补卡，如果已经补卡则不推送消息
							Integer count = this.baseMapper.selectCount(Wrappers.<SmtReplaceApplication> query().lambda()
									.eq(SmtReplaceApplication::getStaffBadge,lvwAttendYcxx.getBadge())
									.eq(SmtReplaceApplication::getStartTime,DateUtils.format(lvwAttendYcxx.getAttenddate(), format)));
							if(count<=0) {
								//判断是够已经推送过消息，如果未推送消息则推送考勤异常消息
								log.info("考勤异常员工："+lvwAttendYcxx.getBadge());
								approvalNotice(lvwAttendYcxx.getBadge(), lvwAttendYcxx.getAttenddate());
							}
						}
					}
				}
		}

		public void approvalNotice(String badge, Date date) {
			//推送App消息
			Calendar ca = Calendar.getInstance();
			ca.setTime(date);
			String day = ca.get(Calendar.DAY_OF_MONTH)+"";
			int months=ca.get(Calendar.MONTH)+1;
			String month = months+"";
			AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
			appMsgPushDTO.setBadge(badge);
			appMsgPushDTO.setTemplateCode(SmsTemplateEnum.APP_PUSH_10302.getCode());
			appMsgPushDTO.setExtraParam("attendanceStatus=" + MsgAttendanceEnum.MSG_2.getCode());
			SmtMsgTemplate smtMsgTemplate = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.APP_PUSH_10302.getCode());
			String content=smtMsgTemplate.getTempContent();
			content=content.replace("{月}", month).replace("{日}", day);
			appMsgPushDTO.setContent(content);
			log.info("推送消息："+appMsgPushDTO);
			Boolean pushAppMsg = appMsgPushService.pushAppMsg(appMsgPushDTO);
			log.info("推送消息结果："+pushAppMsg);
		}

		@Override
		public Page<SearchReplaceApplicationVO> getSmtReplaceApplicationPageList(Page page,
				SearchReplaceDTO searchReplaceDTO) {
			// TODO Auto-generated method stub
			Page<SearchReplaceApplicationVO> selectPage = this.baseMapper.getSmtReplaceApplicationPageList(page, searchReplaceDTO);
			//判断是否为空
			if(selectPage.getSize()>0) {
				for (int i = 0; i < selectPage.getRecords().size(); i++) {

					//根据补卡原因id获取补卡的原因描述
					Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.REPLACE_REASON,selectPage.getRecords().get(i).getCause().toString(), SecurityConstants.FROM_IN);
					//判断是否为空
					if(findByType.getData() != null) {
						//根据字典表查询补卡原因类型数据
						selectPage.getRecords().get(i).setPatchReasonDesc(findByType.getData().getLabel());
					}
				}
			}
			return selectPage;
		}

		@Override
		public IPage<PatchStatisticsVo> patchCountStatistics(Page page, PatchStatisticsReqDTO reqDTO) {
			PatchStatisticsDTO dto = BeanUtils.transform(PatchStatisticsDTO.class, reqDTO);
			List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
			dto.setParkIds(parkIds);
			return this.baseMapper.patchStatistics(page, dto);
		}

}
