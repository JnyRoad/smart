package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.app.ao.fore.ApplicationAo;
import com.tce.smart.app.ao.fore.OperationAo;
import com.tce.smart.app.emun.ApplicationOPType;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.fore.ApplicationService;
import com.tce.smart.app.vo.fore.*;
import com.tce.smart.common.core.constant.PaginationConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.SexType;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.*;
import com.tce.smart.platform.api.dto.req.ApplicationListReqDTO;
import com.tce.smart.platform.api.dto.req.UpApplicationListReqDTO;
import com.tce.smart.platform.api.dto.resp.ApplicationListRespDTO;
import com.tce.smart.platform.api.dto.resp.FaceApplicationRespDTO;
import com.tce.smart.platform.api.dto.resp.SmtApplicationProcessRespDTO;
import com.tce.smart.platform.api.feign.RemoteApplicationService;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.ApplicationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 招聘管理接口实现
 *
 * @author qipei
 */
@Service
@AllArgsConstructor
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

	private RemoteApplicationService service;

	private final RemoteDictService remoteDictService;

	private final AppCommService appCommService;

	/**
	 * 获取简历列表
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Result getApplicationList(Map<String, Object> params, ApplicationAo application) {
		String badge = SecurityUtils.getUser().getUsername();
		log.info("remote getApplicationList ApplicationListDTO=[{}]", application);
		ApplicationListReqDTO appDto = new ApplicationListReqDTO();
		if (StringUtils.isNotEmpty(application.getJobId())) {
			appDto.setJobId(application.getJobId());
		}

		appDto.setStaffBadge(badge);
		appDto.setAgeEnd(application.getAgeEnd());
		appDto.setAgeOrder(application.getAgeOrder());
		appDto.setAgeStart(application.getAgeStart());
		appDto.setApplicantMobile(application.getApplicantMobile());
		appDto.setApplicantName(application.getApplicantName());
		appDto.setDeliverOrder(application.getDeliverOrder());
		appDto.setGender(application.getGender());
		appDto.setApplyState(application.getApplyState());
		appDto.setParkId(application.getParkId());

		Result<Page<ApplicationListRespDTO>> result = service.getSmtApplictionList(MapUtil.getInt(params, PaginationConstants.CURRENT),
				MapUtil.getInt(params, PaginationConstants.SIZE), appDto, SecurityConstants.FROM_IN);

		if (result.isSuccess() && Objects.nonNull(result.getData())) {
			//List<ApplicationListRespDTO> voList = (List<ApplicationListRespDTO>)result.getData();
			IPage<ApplicationListRespDTO> pageList = result.getData();
			for (ApplicationListRespDTO elemnet : pageList.getRecords()) {
				//获取图片URL
				if (elemnet.getApplicantPhoto() != null) {
					elemnet.setApplicantPhoto(appCommService.buildHqImageUrl(elemnet.getApplicantPhoto()));
				}
			}

		}

		log.info("remote getSmtApplictionList result=[{}]", result);
		return result;
	}

	/**
	 * 获取简历详细信息
	 */
	@Override
	public ApplicationDetailVo getApplicationDetail(String applicationId) {
		String id = JSONUtil.parseObj(applicationId).get("applicationId").toString();
		Result<ApplicationInfoRespDTO> result = service.getApplicationById(id, SecurityConstants.FROM_IN);
		log.info("remote getById result=[{}]", result);
		if (!result.isSuccess()) {
			if(StringUtils.isNotEmpty(result.getMsg())) {
				JSONObject errorInfoOjb = JSONUtil.parseObj(result.getMsg());
				String errInfo = StringUtils.isNotBlank(errorInfoOjb.getStr("msg")) ? errorInfoOjb.getStr("msg")
						: errorInfoOjb.getStr("message");
				throw new TCEException(errInfo);
			}else{
				throw new TCEException("查询异常");
			}
		}

		ApplicationInfoRespDTO info = result.getData();
		ApplicationDetailVo vo = new ApplicationDetailVo();
		SmtRecruitmentDTO re = info.getRecruitment();
		SmtApplicationDTO app = info.getApplication();
		vo.setApplicationId(app.getId().toString());
		vo.setApplicantName(app.getName());
		vo.setApplicantAge(app.getAge());
		vo.setApplicantEducation(re.getEducation() == null ? "" : re.getEducation());
		vo.setApplicantGender(SexType.desc(app.getSex()));
		vo.setApplicantMobile(app.getPhone());
		vo.setApplicantNation(app.getNation());
		//查询学历
		vo.setApplicantEducation(info.getApplicantEducation() == null ? "" : info.getApplicantEducation());
		//投递时间
		vo.setApplyDate(DateUtils.format(app.getApplyDate(), DateUtils.DATE_FORMAT));
		//图片地址
		String certnoPicUrl = appCommService.buildHqImageUrl(info.getFacePic());
		vo.setApplicantPhoto(certnoPicUrl);
		vo.setJobName(re.getJobName());
		vo.setApplicationJche(re.getJcheName());
		vo.setJobAddress(info.getParkName());
		vo.setComputerLevel(re.getCompRequire() == null ? "" : re.getCompRequire());
		vo.setLanguage(re.getReqLanguage() == null ? "" : re.getReqLanguage());
		vo.setJobDept(re.getDepName());
		vo.setWorkAge(re.getWorkYear() == null ? 0 : re.getWorkYear());
		vo.setApplicantAddress(info.getParkName());
		List<SmtApplicationEducationDTO> education = info.getApplicationEducation();
		//教育
		List<EducationVo> educationVoList = getEducation(education);
		vo.setEducationHis(educationVoList);
		List<SmtApplicationWorkDTO> work = info.getApplicationWork();
		List<WorkVo> workVoList = getWork(work);
		vo.setWorkHis(workVoList);
		return vo;
	}

	/**
	 * 重构应聘者的工作经验列表
	 *
	 * @param work
	 * @return
	 */
	private List<WorkVo> getWork(List<SmtApplicationWorkDTO> work) {
		List<WorkVo> workVoList = new ArrayList<>();
		WorkVo workvo = null;
		for (SmtApplicationWorkDTO wo : work) {
			workvo = new WorkVo();
			workvo.setCompanyName(wo.getCompany());
			workvo.setProver(wo.getPersonLiable());
			workvo.setJobName(wo.getJobName());
			workvo.setProverMobile(wo.getPhone());
			workvo.setStartTime(wo.getStartTime());
			workvo.setEndTime(wo.getEndTime());
			workVoList.add(workvo);
		}
		return workVoList;
	}

	/**
	 * 重构教育的经验列表
	 *
	 * @param education
	 * @return
	 */
	private List<EducationVo> getEducation(List<SmtApplicationEducationDTO> education) {
		List<EducationVo> educationVoList = new ArrayList<>();
		EducationVo evo = null;
		for (SmtApplicationEducationDTO ed : education) {
			evo = new EducationVo();
			evo.setSchoolName(ed.getSchoolName());
			evo.setMajor(StringUtils.isNotBlank(ed.getMajor()) ? ed.getMajor() : "");

			//查询学历
			String eduStr = "";
			try {
				if (StringUtils.isNotBlank(ed.getEducation())) {
					Result<SysDict> result = remoteDictService.findByValue(DictConstants.EDUCATION_TYPE, String.valueOf(ed.getEducation()), SecurityConstants.FROM_IN);
					eduStr = result.getData().getDescription();
				}
			} catch (Exception e) {
				log.error("查询学历字典异常", e);
			}
			evo.setEducation(eduStr);
			evo.setStartTime(ed.getStartTime());
			evo.setEndTime(ed.getEndTime());
			educationVoList.add(evo);
		}
		return educationVoList;
	}


	/**
	 * 获取所有的发布的招聘的岗位
	 */
	@Override
	public Result getJobsiftList(Integer parkId) {
		// TODO Auto-generated method stub
		Result jobList = service.JobList(parkId,SecurityConstants.FROM_IN);
		log.info("remote JobList result=[{}]", jobList);
		return jobList;
	}


	/**
	 * 查询应聘的状态
	 */
	@Override
	public Result getOtptypeList() {
		// 查询应聘状态字典表
//		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.APPLICATION_STATUS, SecurityConstants.FROM_IN);
		List<Map<String, Object>> typeList = ApplicationStatusEnum.getAppDispaylist();

		//排序
		Collections.sort(typeList,
				(Map<String, Object> map1, Map<String, Object> map2) -> Integer.parseInt(map1.get("order") + "") - Integer.parseInt(map2.get("order") + ""));

		List<ApplicationStatusVo> statusVoList = new ArrayList<>();
		ApplicationStatusVo vo = null;
		for (int j = 0; j < typeList.size(); j++) {
			vo = new ApplicationStatusVo();
			vo.setApplyState(Integer.valueOf(typeList.get(j).get("code").toString()));
			vo.setApplyStateDesc(typeList.get(j).get("desc").toString());
			statusVoList.add(vo);
		}
		return new Result<>(statusVoList);
	}

	/**
	 * 查询某应聘的流程
	 */
	@Override
	public Result getRecord(String applicationId) {
		// TODO Auto-generated method stub
		String idOb = JSONUtil.parseObj(applicationId).get("applicationId").toString();
		Long id = Long.parseLong(idOb);
		Result<List<SmtApplicationProcessRespDTO>> result = service.getApplicationProcess(id, SecurityConstants.FROM_IN);
		log.info("remote getApplicationProcess result=[{}]", result);
		List<SmtApplicationProcessRespDTO> list = result.getData();


		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.APPLICATION_STATUS, SecurityConstants.FROM_IN);
		List<RecordVo> recordVoList = new ArrayList<>();
		RecordVo vo = null;
		for (SmtApplicationProcessRespDTO pro : list) {
			vo = new RecordVo();
			vo.setOptUser(pro.getCreateUserName() == null ? "" : pro.getCreateUserName());
			vo.setOptDate(pro.getCreateTime());
			vo.setOptDesc(pro.getRemark() == null ? "" : pro.getRemark());
			if (findByType.getData().size() > 0) {
				for (int j = 0; j < findByType.getData().size(); j++) {
					if (findByType.getData().get(j).getValue().equals(String.valueOf(pro.getStatus()))) {
						vo.setOptName(findByType.getData().get(j).getLabel());
						break;
					}
				}
			}
			recordVoList.add(vo);
		}
		return new Result<>(recordVoList);
	}

	/**
	 * 根据人脸搜索简历
	 */
	@Override
	public Result getFaceList(String face) {
		Result<FaceApplicationRespDTO> result = service.getByface(face, SecurityConstants.FROM_IN);
		log.info("remote getByface result=[{}]", result);
		if (!result.isSuccess()) {
			throw new TCEException("人脸搜索简历异常");
		}
		if (result.getData() != null) {
			FaceApplicationRespDTO data = result.getData();
			data.setApplicantPhoto(appCommService.buildHqImageUrl(data.getApplicantPhoto()));
			return new Result<>(data);
		}
		return new Result<>();

	}

	/**
	 * 筛选简历
	 */
	@Override
	public Result operationApplication(OperationAo operationAo) {
		if (StringUtils.isEmpty(operationAo.getApplicationId())) {
			throw new TCEException("应聘id为空");
		}

		UpApplicationListReqDTO dto = new UpApplicationListReqDTO();
		List<Long> ids = new ArrayList<>();
		String[] operationItem = operationAo.getApplicationId().split("\\|");
		for (int i = 0; i < operationItem.length; i++) {

			Long id = Long.parseLong(operationItem[i]);
			ids.add(id);
		}

		dto.setIds(ids);
		// 获取用户名
		String userName = SecurityUtils.getUser().getUsername();
		dto.setCreateUserNme(userName); // 获取登录的用户
		//拒绝原因
		String operationDesc = null;
		if (ApplicationOPType.REFUSE.getType().equals(operationAo.getOperationType())) {
			operationDesc = operationAo.getOperationDesc();
		}
		dto.setRefuseReason(operationDesc);

		//面试、复试、重新邀请、入职时间
		Date interviewTime = null;
		if (ApplicationOPType.INVITE.getType().equals(operationAo.getOperationType())
				|| ApplicationOPType.RE_FACE.getType().equals(operationAo.getOperationType())
				|| ApplicationOPType.RE_INVITE.getType().equals(operationAo.getOperationType())
				|| ApplicationOPType.ENROLL.getType().equals(operationAo.getOperationType())) {

			try {
				interviewTime = DateUtils.parse(operationAo.getAppointTime(),
						DateUtils.DEFAULT_MINUTE_DATE_TIME_FORMAT);
			} catch (Exception e) {
				log.error("时间转换异常", e);
				throw new TCEException("约定时间格式无效");
			}
		}

		dto.setInterviewTime(interviewTime);
		dto.setCreateUserNme(SecurityUtils.getUser().getUsername());
		// 转换应聘状态码
		Integer status = changeAppplicationState(operationAo.getOperationType());
		dto.setStatus(status);

		Result result = service.updateApplicationList(dto, SecurityConstants.FROM_IN);

		log.info("remote updateApplicationList result=[{}]", result);
		return result;
	}

	/**
	 * 转换应聘状态码
	 *
	 * @param operationType HR面试简历操作类型
	 * @return 简历状态状态
	 */
	private Integer changeAppplicationState(Integer operationType) {
		// 1-面试邀请，2-拒绝，3-复试，4-加入人才库，5-重新邀请，6-录取
		Integer status = null;
		switch (ApplicationOPType.type(operationType)) {
			case INVITE://面试邀请
				status = ApplicationStatusEnum.INVITE_DONE.getCode();//已邀请
				break;
			case REFUSE://拒绝
				status = ApplicationStatusEnum.REFUSE_DONE.getCode();//已拒绝;
				break;
			case RE_FACE://复试
				status = ApplicationStatusEnum.REFACE_TO_DO.getCode();//待面试
				break;
			case STORE://加入人才库
				status = ApplicationStatusEnum.STORE_DONE.getCode();//已入库
				break;
			case RE_INVITE://重新邀请
				status = ApplicationStatusEnum.INVITE_DONE.getCode();//已邀请
				break;
			case ENROLL://录取
				status = ApplicationStatusEnum.ENTRY_TO_DO.getCode();//待入职
				break;
			case ENTRY://入职
				status = ApplicationStatusEnum.ENTRY_DONE.getCode();//已入职
				break;
		}
		return status;
	}


}
