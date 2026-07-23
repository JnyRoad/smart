package com.tce.smart.app.service.wechat.impl;

import com.icbc.api.internal.util.codec.Base64;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.algorithm.api.dto.req.CompareDTO;
import com.tce.smart.algorithm.api.dto.req.CompareImageDTO;
import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.api.enums.FaceTypeEnum;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.app.ao.fore.*;
import com.tce.smart.app.ao.wechat.*;
import com.tce.smart.app.dto.fore.OcrIdCardDto;
import com.tce.smart.app.service.AppSmsService;
import com.tce.smart.app.service.IOcrService;
import com.tce.smart.app.service.wechat.JobService;
import com.tce.smart.app.vo.fore.DicContentVo;
import com.tce.smart.app.vo.fore.EmployeeVo;
import com.tce.smart.app.vo.wechat.*;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.SexType;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.util.UUIDUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.*;
import com.tce.smart.platform.api.dto.req.ApplicationEmergencyReqDTO;
import com.tce.smart.platform.api.dto.req.SaveWechatApplicationReqDTO;
import com.tce.smart.platform.api.dto.resp.*;
import com.tce.smart.platform.api.feign.*;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.ApplicationStatusEnum;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 招聘信息
 *
 * @author mingkai.wu
 * @date 2019-05-13 08:37:17
 */
@Service
@Slf4j
public class JobServiceImpl implements JobService {

	@Value("${spring.face.compare-value}")
	private Double compareValue;

	@Autowired
	private RemoteRecruitmentService remoteRecruit;

	@Autowired
	private RemoteStaffInternalService remoteStaffInternalService;

	@Autowired
	private RemoteDictService remoteDictService;

	@Autowired
	private RemoteParkService remoteParkService;

	@Autowired
	private AppSmsService appSmsService;

	@Autowired
	private RemoteApplicationService remoteApplicationService;

	@Autowired
	private IOcrService ocrService;

	@Autowired
	private RemoteSmtImageService remoteSmtImageService;

	@Autowired
	private RemoteAlgorithmService remoteAlgorithmService;

	@Autowired
	private RemoteStaffService remoteStaff;


	@SuppressWarnings({"rawtypes"})
	@Override
	public List<SmtParkDTO> getParkList() {
		// 调用远程获取园区列表
		return remoteParkService.getParkList(SecurityConstants.FROM_IN).data();
	}


	/**
	 * 岗位详情
	 */
	@Override
	public JobDetailVo getJobDetail(String jobId) {
		// 招聘岗位id
		Integer id = Integer.parseInt(jobId);
		log.info("查询岗位详情, JobId={}", jobId);
		RecruitmentRespDTO recruitmentRespDTO = remoteRecruit.getById(id, SecurityConstants.FROM_IN).data();

		JobDetailVo recruitDetailVo = new JobDetailVo();
		recruitDetailVo.setRecruitId(recruitmentRespDTO.getId());
		recruitDetailVo.setJobName(recruitmentRespDTO.getJobName());
		recruitDetailVo.setJobCount(recruitmentRespDTO.getRecruitNum());
		recruitDetailVo.setJobAddress(recruitmentRespDTO.getParkName());
		recruitDetailVo.setJobDept(recruitmentRespDTO.getDepName());
		recruitDetailVo.setJobWage(recruitmentRespDTO.getSalaryStart() + "-" + recruitmentRespDTO.getSalaryEnd());
		recruitDetailVo.setJobDesc(recruitmentRespDTO.getJobCotent());
		recruitDetailVo.setValidityDate(recruitmentRespDTO.getEndTime());
		recruitDetailVo.setPublishDate(DateUtils.format(recruitmentRespDTO.getCreateTime(), DateUtils.DATE_FORMAT));
		recruitDetailVo.setJcheName(recruitmentRespDTO.getJcheName());
		/*recruitDetailVo.getJobNecess().getAge();*/
		recruitDetailVo.setParkLatitude(recruitmentRespDTO.getParkLatitude());
		recruitDetailVo.setParkLongitude(recruitmentRespDTO.getParkLongitude());
		// 语言
		//Result<SysDict> languageDict = remoteDictService.findById(recruitmentRespDTO.getReqLanguage(), SecurityConstants.FROM_IN);
		//if (CommonConstants.SUCCESS == languageDict.getCode()) {
		recruitDetailVo.getJobNecess().setLanguage(recruitmentRespDTO.getReqLanguage());
		recruitDetailVo.getJobNecess().setEducation(recruitmentRespDTO.getEducation());
		recruitDetailVo.getJobNecess().setWorkYear(recruitmentRespDTO.getWorkYear().toString());
		recruitDetailVo.getJobNecess().setMajor(recruitmentRespDTO.getMajor());
		recruitDetailVo.setRelation(recruitmentRespDTO.getRelation());
		//}
		// 计算机等级
		//Result<SysDict> compRequireDict = remoteDictService.findById(recruitmentRespDTO.getCompRequire(),
		//SecurityConstants.FROM_IN);
		//if (CommonConstants.SUCCESS == compRequireDict.getCode()) {
		recruitDetailVo.getJobNecess().setComputers(recruitmentRespDTO.getCompRequire());
		//}
		// 年龄要求
		recruitDetailVo.getJobNecess().setAge(recruitmentRespDTO.getAgeStart() + "-" + recruitmentRespDTO.getAgeEnd());

		return recruitDetailVo;
	}

	@Override
	public OcrReadCardImgVo readCardImg(OcrReadCardImgAo ocrReadCardImgAo, String openId) {
		OcrReadCardImgVo ocrReadCardImgVo;
		//读取身份证正面信息
		OcrIdCardDto frontInfo = ocrService.readIdCardFontImg(ocrReadCardImgAo.getIdCardFrontImg());
		log.info("身份证正面信息识别完成");
		//, JSONUtil.toJsonStr(frontInfo)
		if (Objects.isNull(frontInfo) || Objects.isNull(frontInfo.getName())) {
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "识别身份证正面失败，请正对拍摄");
		}

		//读取身份证背面信息
		OcrIdCardDto backInfo = ocrService.readIdCardBackImg(ocrReadCardImgAo.getIdCardBackImg());
		log.info("身份证反面信息识别完成");
		//, JSONUtil.toJsonStr(backInfo)
	/*	if(Objects.isNull(backInfo) ||Objects.isNull(backInfo.getValidityDate()) )
		{
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "识别身份证背面失败，请正对拍摄");
		}*/
		frontInfo.setSignOrg(backInfo.getSignOrg());
		frontInfo.setSignDate(backInfo.getSignDate());
		frontInfo.setValidityDate(backInfo.getValidityDate());
		frontInfo.setValidityEndDate(backInfo.getValidityEndDate());
		/*SaveWechatApplicationReqDTO saveApplicationDTO = new SaveWechatApplicationReqDTO();
		saveApplicationDTO.setName(frontInfo.getName());
		saveApplicationDTO.setBirth(frontInfo.getBirthday());
		saveApplicationDTO.setCertno(frontInfo.getIdentityCard());
		saveApplicationDTO.setCertnoPicture(ocrReadCardImgAo.getIdCardFrontImg());
		saveApplicationDTO.setNation(frontInfo.getEthnicity());
		saveApplicationDTO.setRecruitId(ocrReadCardImgAo.getRecruitId());
		saveApplicationDTO.setPolice(frontInfo.getSignOrg());
		saveApplicationDTO.setSex(SexType.code(frontInfo.getGender()));
		saveApplicationDTO.setWechat(openId);
		saveApplicationDTO.setHomeAddress(frontInfo.getAddress());
		saveApplicationDTO.setStatementStatus(ApplicationStatusEnum.EDIT_ING.getCode());

		parseCardDate(frontInfo, saveApplicationDTO);
		//发送feign添加应聘信息
		Result result = remoteApplicationService.addWechatAppliction(saveApplicationDTO, SecurityConstants.FROM_IN);


		Object applicationId = result.getData();
		if (Objects.isNull(applicationId)) {
			log.error("remote remoteApplicationService result{}", result.getCode(), result.getMsg());
			throw new TCEException(result.getMsg());
		}*/

		ocrReadCardImgVo = new OcrReadCardImgVo();
		/*ocrReadCardImgVo.setApplicationId(applicationId.toString());*/
		ocrReadCardImgVo.setRecruitId(ocrReadCardImgAo.getRecruitId().toString());
		ocrReadCardImgVo.setJcheName(ocrReadCardImgAo.getJcheName());
		ocrReadCardImgVo.setName(frontInfo.getName());
		ocrReadCardImgVo.setIdentification(frontInfo.getIdentityCard());
		ocrReadCardImgVo.setGender(frontInfo.getGender());
		ocrReadCardImgVo.setEthnicity(frontInfo.getEthnicity());
		ocrReadCardImgVo.setBirthday(frontInfo.getBirthday());
		ocrReadCardImgVo.setAddress(frontInfo.getAddress());
		ocrReadCardImgVo.setSignOrg(frontInfo.getSignOrg());
		ocrReadCardImgVo.setValidityDate(frontInfo.getValidityDate());
		ocrReadCardImgVo.setCardFrontImg(ocrReadCardImgAo.getIdCardFrontImg());

		return ocrReadCardImgVo;

	}

	@Override
	public String saveCardInfo(OcrReadCardImgVo ocrReadCardImgAo, String openId) {
		// TODO Auto-generated method stub
		SaveWechatApplicationReqDTO saveApplicationDTO = new SaveWechatApplicationReqDTO();
		saveApplicationDTO.setName(ocrReadCardImgAo.getName());
		saveApplicationDTO.setBirth(ocrReadCardImgAo.getBirthday());
		saveApplicationDTO.setCertno(ocrReadCardImgAo.getIdentification());
		saveApplicationDTO.setCertnoPicture(ocrReadCardImgAo.getCardFrontImg());
		saveApplicationDTO.setNation(ocrReadCardImgAo.getEthnicity());
		saveApplicationDTO.setRecruitId(Integer.parseInt(ocrReadCardImgAo.getRecruitId()));
		saveApplicationDTO.setPolice(ocrReadCardImgAo.getSignOrg());
		saveApplicationDTO.setSex(SexType.code(ocrReadCardImgAo.getGender()));
		saveApplicationDTO.setWechat(openId);
		saveApplicationDTO.setHomeAddress(ocrReadCardImgAo.getAddress());
		saveApplicationDTO.setStatus(ApplicationStatusEnum.EDIT_ING.getCode());
		if (StringUtils.isNotBlank(ocrReadCardImgAo.getValidityDate())) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			String[] validityDateArray = ocrReadCardImgAo.getValidityDate().split("-");
			if (validityDateArray.length != 2) {
				throw new TCEException("解析身份证有效期异常");
			}

			try {
				saveApplicationDTO.setValidDateFm(dateFormat.parse(validityDateArray[0]));
				if (validityDateArray[1].contains("长期")) {
					saveApplicationDTO.setValidDateChar(validityDateArray[1]);

				} else {
					saveApplicationDTO.setValidDate(dateFormat.parse(validityDateArray[1]));
				}
			} catch (ParseException e) {
				throw new TCEException("解析身份证有效期异常");
			}
		}

		//添加应聘信息
		String result = "";
		try {
			result = remoteApplicationService.addWechatAppliction(saveApplicationDTO, SecurityConstants.FROM_IN)
					.data()
					.toString();
		} catch (Exception e) {
			throw new TCEException(e.getMessage());
		}
		return result;
	}

	@Override
	public Boolean bindMobile(VerifySmsCodeAo verifySmsCodeAo) {
		//校验短信验证码
		appSmsService.verifySmsCode(verifySmsCodeAo.getMobile(), verifySmsCodeAo.getSmsCode());
		SaveWechatApplicationReqDTO saveApplicationDTO = new SaveWechatApplicationReqDTO();
		saveApplicationDTO.setApplicatioId(Long.parseLong(verifySmsCodeAo.getApplicationId()));
		saveApplicationDTO.setPhone(verifySmsCodeAo.getMobile());
		Result result = remoteApplicationService.addMobileFromWechat(saveApplicationDTO, SecurityConstants.FROM_IN);

		if (!result.isSuccess()) {
			throw new TCEException("短信发送服务异常:" + result.getMessage());
		}
		return Boolean.TRUE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EducationRespDTO> getEducationHis(String applicationId) {
		return remoteApplicationService.getSmtApplicationEducationList(applicationId, SecurityConstants.FROM_IN).data();
	}

	@Override
	public List getWorkHis(String applicationId) {
		List<SmtApplicationWorkRespDTO> list = remoteApplicationService.getSmtApplicationWorkList(applicationId, SecurityConstants.FROM_IN).data();
		List<ApplicationWorkVo> voList = new ArrayList<>();
		ApplicationWorkVo vo;
		for (SmtApplicationWorkRespDTO app : list) {
			vo = new ApplicationWorkVo();
			vo.setWorkHisId(app.getId().toString());
			vo.setProver(app.getPersonLiable());
			vo.setProverMobile(app.getPhone());
			vo.setEndTime(app.getEndTime());
			vo.setJobName(app.getJobName());
			vo.setCompanyName(app.getCompany());
			vo.setStartTime(app.getStartTime());
			voList.add(vo);
		}
		return voList;
	}

	@Override
	public Boolean addFaceImg(AddJobFaceAo addJobFaceAo) {
		// 获取身份证照片信息
		// 对比人脸、身份证照片
		if (StringUtils.isEmpty(addJobFaceAo.getApplicationId())) {
			throw new TCEException("应聘者id为空");
		}
		SmtApplicationRespDTO smtApplication = remoteApplicationService.getSimpleInfo(Long.parseLong(addJobFaceAo.getApplicationId()), SecurityConstants.FROM_IN).data();
		String certnoPicId = smtApplication.getCertnoPicId();
		//获取身份证照片base64字符
		String certnoPic = remoteSmtImageService.getImageBase64ByCode(certnoPicId, SecurityConstants.FROM_IN).data();
		// 远程调用人证照片比对接口
		CompareDTO compareDTO = new CompareDTO();
		CompareImageDTO compareImageA = new CompareImageDTO();
		compareImageA.setImageBase64(addJobFaceAo.getFacePhoto());
		compareImageA.setFaceType(FaceTypeEnum.LIVE.getType());
		compareImageA.setIsCard(0);
		byte[] bytesFace = Base64.decodeBase64(addJobFaceAo.getFacePhoto());
		log.info("人证比对人脸照片={}kb", bytesFace.length / 1024);
		CompareImageDTO compareImageB = new CompareImageDTO();
		compareImageB.setImageBase64(certnoPic);
		compareImageB.setFaceType(FaceTypeEnum.CERT.getType());
		compareImageB.setIsCard(1);
		byte[] bytesCer = Base64.decodeBase64(certnoPic);
		log.info("人证比对证件照照片={}kb", bytesCer.length / 1024);
		compareDTO.setCompareImageA(compareImageA);
		compareDTO.setCompareImageB(compareImageB);
		com.tce.smart.algorithm.api.dto.resp.CompareDTO compare = remoteAlgorithmService.compare(
				UUIDUtils.create(),
				AlgorithmTypeEnum.COMPARE_FACEALL.getType(),
				compareDTO,
				SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)
				.data();

		if (compare.getSimilarity() < compareValue) {
			log.info("人证比对结果是：" + compare);
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "人证比对失败，请重新拍照");
		}
		SaveWechatApplicationReqDTO saveFaceDTO = new SaveWechatApplicationReqDTO();
		saveFaceDTO.setApplicatioId(Long.parseLong(addJobFaceAo.getApplicationId()));
		saveFaceDTO.setFacePicture(addJobFaceAo.getFacePhoto());
		// 添加人脸照片信息
		Result addFaceRs = remoteApplicationService.addFaceFromWechat(saveFaceDTO, SecurityConstants.FROM_IN);
		if (!addFaceRs.isSuccess()) {
			throw new TCEException("添加人脸照片信息: " + addFaceRs.getMsg());
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean submitApplication(String applicationId, Integer maritalStatus) {
		Result result = remoteApplicationService.delivery(Long.parseLong(applicationId), maritalStatus, SecurityConstants.FROM_IN);
		if (!result.isSuccess()) {
			throw new TCEException("添加应聘者人脸信息失败");
		}
		return Boolean.TRUE;
	}

	@Override
	public Result addEducationHis(EducationHisAo educationAo) {
		// TODO Auto-generated method stub
		//删除该application的学历
		Result<Integer> deleteEducationList = remoteApplicationService.deleteEducationList(educationAo.getApplicationId(), SecurityConstants.FROM_IN);
		log.info("删除学历信息, ApplicationId={}, Result={}", educationAo.getApplicationId(), deleteEducationList.isSuccess());
		if (deleteEducationList.isSuccess()) {
			SmtApplicationEducationDTO edu;
			List<ApplicationEducationAo> educationHis = educationAo.getEducationHis();
			for (ApplicationEducationAo ao : educationHis) {
				edu = new SmtApplicationEducationDTO();
				edu.setApplicationId(Long.parseLong(educationAo.getApplicationId()));
				edu.setDegree(ao.getDegree());
				edu.setEducation(ao.getEducation());
				edu.setSchoolName(ao.getSchoolName());
				edu.setMajor(ao.getMajor());
				edu.setStartTime(ao.getStartTime());
				edu.setEndTime(ao.getEndTime());
				edu.setGradType(ao.getGradType());
				edu.setIsHighDegreeType(ao.getIsHighDegreeType());
				edu.setIsHighEduType(ao.getIsHighEduType());
				Result result = remoteApplicationService.addApplicationeEducation(edu, SecurityConstants.FROM_IN);
				log.info("写入学历信息, Education={}, Degree={}, Result={}", edu.getEducation(), edu.getDegree(), result.isSuccess());
			}
		}
		return new Result<>(true);
	}

	@Override
	public Result addWorkHis(WorkHisAo workAo) {
		//删除该application的学历
		Result deleteWorkList = remoteApplicationService.deleteApplicationWorkList(workAo.getApplicationId(), SecurityConstants.FROM_IN);
		log.info("删除工作经历信息, ApplicationId={}, Result={}", workAo.getApplicationId(), deleteWorkList.isSuccess());
		if (deleteWorkList.isSuccess()) {
			SmtApplicationWorkDTO work;
			List<ApplicationWorkAo> workHis = workAo.getWorkHis();
			for (ApplicationWorkAo ao : workHis) {
				work = new SmtApplicationWorkDTO();
				work.setApplicationId(Long.parseLong(workAo.getApplicationId()));
				work.setCompany(ao.getCompanyName());
				work.setEndTime(ao.getEndTime());
				work.setJobName(ao.getJobName());
				work.setPersonLiable(ao.getProver());
				work.setPhone(ao.getProverMobile());
				work.setStartTime(ao.getStartTime());

				Result result = remoteApplicationService.addApplicationWork(work, SecurityConstants.FROM_IN);
				log.info("写入工作经历信息完成 success={}", result.isSuccess());
			}
		}
		return new Result<>(true);
	}

	/**
	 * 解析身份证有效期时间
	 *
	 * @param frontInfo
	 * @param saveApplicationDTO
	 */
	private void parseCardDate(OcrIdCardDto frontInfo, SaveWechatApplicationReqDTO saveApplicationDTO) {
		if (StringUtils.isNotBlank(frontInfo.getValidityDate())) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			String[] validityDateArray = frontInfo.getValidityDate().split("-");
			if (validityDateArray.length != 2) {
				throw new TCEException("解析身份证有效期异常");
			}
			try {
				saveApplicationDTO.setValidDateFm(dateFormat.parse(validityDateArray[0]));
				saveApplicationDTO.setValidDate(dateFormat.parse(validityDateArray[1]));
			} catch (ParseException e) {
				throw new TCEException("解析身份证有效期异常");
			}
		}
	}

	@Override
	public List<DicContentVo> getDegreeType() {
		// TODO Auto-generated method stub
		List<SysDict> findByType = remoteDictService.findByType(DictConstants.DEGREE_TYPE, SecurityConstants.FROM_IN).data();
		List<DicContentVo> typeList = new ArrayList<>();
		DicContentVo type;
		if (CollectionUtils.isNotEmpty(findByType)) {
			for (SysDict sysDict : findByType) {
				type = new DicContentVo();
				type.setTypeCode(sysDict.getValue());
				type.setTypeName(sysDict.getLabel());
				typeList.add(type);
			}
		}
		return typeList;
	}

	@Override
	public List<DicContentVo> getEducationType() {
		List<SysDict> findByType = remoteDictService.findByType(DictConstants.EDUCATION_TYPE, SecurityConstants.FROM_IN).data();
		List<DicContentVo> typeList = new ArrayList<>();
		DicContentVo type;
		if (CollectionUtils.isNotEmpty(findByType)) {
			for (SysDict sysDict : findByType) {
				type = new DicContentVo();
				type.setTypeCode(sysDict.getValue());
				type.setTypeName(sysDict.getLabel());
				typeList.add(type);
			}
		}
		return typeList;
	}

	@Override
	public Result attachmentSubmit(MultipartFile file, String applicationId) {
		// TODO Auto-generated method stub
		SmtApplicationResumeDTO res = new SmtApplicationResumeDTO();
		res.setResumeName(file.getOriginalFilename());
		byte[] bytes;
		try {
			bytes = file.getBytes();
			res.setResume(bytes);
			res.setApplicationId(Long.parseLong(applicationId));
			return remoteApplicationService.save(res, SecurityConstants.FROM_IN);
		} catch (IOException e) {
			log.error("远程接口请求失败: {}", e.getMessage(), e);
		}
		return null;
	}

	@Override
	public void applicationRelationUpdate(ApplicationEmergencyReqDTO applicationEmergencyReqDTO) {
		// TODO Auto-generated method stub
		Result<Integer> result = remoteApplicationService.updateByIdApplicationEmergency(applicationEmergencyReqDTO, SecurityConstants.FROM_IN);
		log.info("修改紧急联系人完成 applicationId={} success={}", applicationEmergencyReqDTO.getApplicationId(), result.isSuccess());
	}

	@Override
	public List<RelationTypeVO> relationList() {
		List<SysDict> findByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN).data();
		List<RelationTypeVO> relationList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(findByType)) {
			for (SysDict sysDict : findByType) {
				RelationTypeVO re = new RelationTypeVO();
				re.setRelationType(sysDict.getValue());
				re.setRelationTypeDesc(sysDict.getLabel());
				relationList.add(re);
			}
		}
		return relationList;
	}


	@Override
	public Result relationAdd(RelationAo relationAo) {
		String applicationId = relationAo.getApplicationId();
		if (StringUtils.isNotEmpty(applicationId)) {
			SmtApplicationEmergencyDTO applicationEmergency = new SmtApplicationEmergencyDTO();
			applicationEmergency.setApplicationId(Long.parseLong(applicationId));
			applicationEmergency.setRelation(relationAo.getRelationType());
			applicationEmergency.setEmergencyName(relationAo.getEmergencyName());
			applicationEmergency.setTelephont(relationAo.getEmergencyPhone());
			Result<Boolean> result = remoteApplicationService.addApplicationEmergency(applicationEmergency, SecurityConstants.FROM_IN);
			log.info("新增紧急联系人完成 applicationId={} success={}", applicationEmergency.getApplicationId(), result.isSuccess());
			return result;
		} else {
			log.error("员工id不能为空");
		}
		return null;
	}

	@Override
	public void familySave(FamilyMemberAddAO familyMemberAddAO) {
		String applicationId = familyMemberAddAO.getApplicationId();
		Result deleteFamily = remoteApplicationService.removeFamilyByApplicationId(Long.parseLong(applicationId), SecurityConstants.FROM_IN);
		log.info("删除家庭成员完成 applicationId={} success={}", applicationId, deleteFamily.isSuccess());
		if (StringUtils.isNotEmpty(applicationId)) {
			List<FamilyMemberAO> listFamilyMember = familyMemberAddAO.getFamilyMember();
			if (CollectionUtils.isNotEmpty(listFamilyMember)) {
				listFamilyMember.forEach(familyMember -> familySave(familyMember, applicationId));
			}
		} else {
			log.error("员工id不能为空");
		}
	}

	/**
	 * 将AO 转换成SmtStaffFamily调用远程接口
	 *
	 * @param familyMemberAO
	 */
	private void familySave(FamilyMemberAO familyMemberAO, String applicationId) {
		SmtApplicationFamilyDTO smtApplicationFamily = new SmtApplicationFamilyDTO();
		//smtStaffFamily.setBadge("1");
		smtApplicationFamily.setApplicationId(Long.parseLong(applicationId));
		smtApplicationFamily.setRelation(null != familyMemberAO.getRelationType() ? familyMemberAO.getRelationType().toString() : null);
		smtApplicationFamily.setName(familyMemberAO.getFamilyName());
		String familyGender = familyMemberAO.getFamilyGender();
		smtApplicationFamily.setSex(null != familyGender ? Integer.parseInt(familyGender) : 0);
		smtApplicationFamily.setBirth(familyMemberAO.getFamilyBirthday());
		smtApplicationFamily.setCompany(familyMemberAO.getFamilyCompany());
		smtApplicationFamily.setJob(familyMemberAO.getFamilyJob());
		smtApplicationFamily.setPhone(familyMemberAO.getEmergencyPhone());
		//调用接口存储
		Result<Boolean> result = remoteApplicationService.addApplicationFamily(smtApplicationFamily, SecurityConstants.FROM_IN);
		log.info("新增家庭成员: ApplicationId={}, Result={}", applicationId, result.isSuccess());
	}

	@Override
	public void orgrelationSave(OrgrelationAddAO orgrelationAddAO) {
		String applicationId = orgrelationAddAO.getApplicationId();
		if (StringUtils.isNotEmpty(applicationId)) {
			Result result = remoteApplicationService.removeRelationByApplicationId(Long.parseLong(applicationId), SecurityConstants.FROM_IN);
			log.info("删除人事关系: ApplicationId={}, Result={}", applicationId, result.isSuccess());
			if (CollectionUtils.isNotEmpty(orgrelationAddAO.getOrgrelation())) {
				orgrelationAddAO.getOrgrelation().forEach(orgrelation -> orgrelationSave(orgrelation, applicationId));
			}
		} else {
			log.error("员工id不能为空");
		}
	}

	/**
	 * 将AO转换为SmtStaffRelation
	 *
	 * @param orgrelation
	 */
	private void orgrelationSave(OrgrelationAO orgrelation, String applicationId) {
		SmtApplicationRelationDTO smtApplicationRelation = new SmtApplicationRelationDTO();
		//smtStaffRelation.setBadge("1");
		smtApplicationRelation.setApplicationId(Long.parseLong(applicationId));
		smtApplicationRelation.setRelation(null != orgrelation.getRelationType() ? orgrelation.getRelationType().toString() : null);
		smtApplicationRelation.setCompName(orgrelation.getOrgPersonBu());
		smtApplicationRelation.setName(orgrelation.getOrgPersonName());
		smtApplicationRelation.setDeptName(orgrelation.getOrgPersonDept());
		smtApplicationRelation.setClassName(orgrelation.getOrgPersonSection());
		smtApplicationRelation.setSex(Integer.parseInt(orgrelation.getOrgPersonGender()));
		smtApplicationRelation.setBadge(orgrelation.getOrgPersonEid());
		smtApplicationRelation.setRelationDetail(orgrelation.getRelationDetail());
		smtApplicationRelation.setJobName(orgrelation.getOrgPersonJob());
		Result result = remoteApplicationService.addApplicationRelation(smtApplicationRelation, SecurityConstants.FROM_IN);
		log.info("添加人事关系: ApplicationId={}, Result={}", applicationId, result.isSuccess());
	}

	@Override
	public RelationVo relationsGet(String applicationId) {
		RelationVo relationVo = null;
		SmtApplicationEmergencyDTO smtApplicationEmergency = remoteApplicationService.getApplicationEmergency(applicationId, SecurityConstants.FROM_IN).data();
		if (null != smtApplicationEmergency) {
			String desc = "";
			List<SysDict> findByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN).data();
			if (CollectionUtils.isNotEmpty(findByType)) {
				for (SysDict sysDict : findByType) {
					if (sysDict.getValue().equals(smtApplicationEmergency.getRelation())) {
						desc = sysDict.getLabel();
						break;
					}
				}
			}
			relationVo = RelationVo
					.builder()
					.relationType(Integer.parseInt(smtApplicationEmergency.getRelation()))
					.relationTypeDesc(desc)
					.emergencyName(smtApplicationEmergency.getEmergencyName())
					.emergencyPhone(smtApplicationEmergency.getTelephont())
					.build();
		}
		return relationVo;
	}

	@Override
	public List<FamilyMemberVO> familyGet(String applicationId) {
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN);
		List<SmtApplicationFamilyDTO> list = remoteApplicationService.getApplicationFamily(applicationId, SecurityConstants.FROM_IN).data();
		final List<FamilyMemberVO> data = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(list)) {
			list.forEach(item -> familyConvert(item, data, findByType));
		}
		return data;
	}

	/**
	 * 转换家庭成员数据并加入list
	 *
	 * @param smtApplicationFamily smtApplicationFamily
	 * @param listVo               listVo
	 * @param findByType           findByType
	 */
	private void familyConvert(SmtApplicationFamilyDTO smtApplicationFamily, List<FamilyMemberVO> listVo, Result<List<SysDict>> findByType) {
		String relationDesc = "";
		List<SysDict> data = findByType.getData();
		for (SysDict sysDict : data) {
			if (sysDict.getValue().equals(smtApplicationFamily.getRelation())) {
				relationDesc = sysDict.getLabel();
				break;
			}
		}
		FamilyMemberVO familyVo = FamilyMemberVO
				.builder()
				.familyMemberId(smtApplicationFamily.getId())
				.familyName(smtApplicationFamily.getName())
				.relationType(null != smtApplicationFamily.getRelation() ? Integer.parseInt(smtApplicationFamily.getRelation()) : 0)
				.relationTypeDesc(relationDesc)
				.familyCompany(smtApplicationFamily.getCompany())
				.familyGender(smtApplicationFamily.getSex())
				.familyGenderDesc(smtApplicationFamily.getSex() == 0 ? "男" : "女")
				.familyJob(smtApplicationFamily.getJob())
				.emergencyPhone(smtApplicationFamily.getPhone())
				.familyBirthday(smtApplicationFamily.getBirth())
				.build();
		listVo.add(familyVo);
	}

	@Override
	public List<OrgrelationVo> orgrelationGet(String applicationId) {
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN);
		List<SmtApplicationRelationDTO> list = remoteApplicationService.getApplicationRelation(applicationId, SecurityConstants.FROM_IN).data();
		final List<OrgrelationVo> data = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(list)) {
			list.forEach(item -> orgrelationConvert(item, data, findByType));
		}
		return data;
	}

	/**
	 * 转换人事关系数据并加入list
	 *
	 * @param smtApplicationRelation smtApplicationRelation
	 * @param listVo                 listVo
	 * @param findByType             findByType
	 */
	private void orgrelationConvert(SmtApplicationRelationDTO smtApplicationRelation, List<OrgrelationVo> listVo, Result<List<SysDict>> findByType) {

		String relationDesc = "";
		List<SysDict> data = findByType.getData();
		for (SysDict sysDict : data) {
			if (sysDict.getValue().equals(smtApplicationRelation.getRelation())) {
				relationDesc = sysDict.getLabel();
				break;
			}
		}
		OrgrelationVo orgrelationVo = OrgrelationVo
				.builder()
				.orgPersonName(smtApplicationRelation.getName())
				.orgrelationId(smtApplicationRelation.getId())
				.relationType(null != smtApplicationRelation.getRelation() ? Integer.parseInt(smtApplicationRelation.getRelation()) : 0)
				.relationTypeDesc(relationDesc)
				.orgPersonBu(smtApplicationRelation.getCompName())
				.orgPersonSection(smtApplicationRelation.getClassName())
				.orgPersonDept(smtApplicationRelation.getDeptName())
				.orgPersonGender(smtApplicationRelation.getSex())
				.build();
		listVo.add(orgrelationVo);
	}

	@Override
	public SmtApplicationEmailDTO emailGet(String application) {
		Long applicationId = Long.parseLong(application);
		return remoteApplicationService.getSmtApplicationEmailList(applicationId, SecurityConstants.FROM_IN).data();
	}

	@Override
	public void emailAdd(ApplicationEmailAo email) {
		SmtApplicationEmailDTO appEmail = new SmtApplicationEmailDTO();
		appEmail.setApplicationId(Long.parseLong(email.getApplicationId()));
		appEmail.setEmail(email.getEmail());
		Result result = remoteApplicationService.addApplicationEmailList(appEmail, SecurityConstants.FROM_IN);
		log.info("添加邮箱完成 applicationId={} success={}", email.getApplicationId(), result.isSuccess());
	}

	@Override
	public Result emailUpdate(ApplicationEmailAo email) {
		SmtApplicationEmailDTO appEmail = new SmtApplicationEmailDTO();
		appEmail.setApplicationId(Long.parseLong(email.getApplicationId()));
		appEmail.setEmail(email.getEmail());
		Result result = remoteApplicationService.updateApplicationEmailList(appEmail, SecurityConstants.FROM_IN);
		log.info("修改邮箱完成 applicationId={} success={}", email.getApplicationId(), result.isSuccess());
		return result;
	}

	@Override
	public List<JobListRespDTO> getJobList(SmtRecruitmentDTO smtRecruitment) {
		return remoteRecruit.getJobList(smtRecruitment, SecurityConstants.FROM_IN).data();
	}


	@Override
	public EmployeeVo getBaseinfo(String badge) {
		// TODO Auto-generated method stub
		// 获取员工号
		badge = requireSelfBadge(badge);
		Result<InternalStaffSelfProfileRespDTO> profileResult = remoteStaffInternalService.getSelfProfile(badge,
				SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, "self-profile");
		if (!profileResult.isSuccess() || profileResult.getData() == null) {
			throw new TCEException("获取员工信息异常");
		}
		InternalStaffSelfProfileRespDTO staff = profileResult.getData();

		EmployeeVo employeeVo = new EmployeeVo();
		employeeVo.setEmployeeName(staff.getName());
		employeeVo.setMobile(staff.getPhone());
		employeeVo.setBuName(staff.getCompName());
		employeeVo.setDeptName(staff.getDepName());
		employeeVo.setEntryDate(staff.getCreateTime());
		employeeVo.setDormitoryState(Objects.nonNull(staff.getDormitoryState()) ? String.valueOf(staff.getDormitoryState()) : null);
		employeeVo.setDormitoryStateDesc(staff.getDormitoryStateDesc());
		employeeVo.setVehicleState(staff.getVehicleState().toString());
		employeeVo.setVehicleStateDesc(staff.getVehicleStateDesc());
		employeeVo.setEmployeeSex(staff.getSex());
		employeeVo.setEmployeeCardNo(staff.getCertno());
		employeeVo.setJobName(staff.getJobName());
		employeeVo.setJcheName(staff.getJcheName());
		return employeeVo;
	}

	/** 微信端个人资料仅允许读取当前认证员工。 */
	private String requireSelfBadge(String requestedBadge) {
		String currentBadge = SecurityUtils.getUser().getUsername();
		if (StringUtils.isBlank(currentBadge)) {
			throw new TCEException("当前登录员工信息缺失");
		}
		if (StringUtils.isNotBlank(requestedBadge) && !currentBadge.equals(requestedBadge)) {
			throw new TCEException("无权查询其他员工资料");
		}
		return currentBadge;
	}



	@Override
	public List<RelationTypeVO> emergencyRelationList() {
		// TODO Auto-generated method stub
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.EMERGENCY_REALTION__TYPE, SecurityConstants.FROM_IN);
		List<RelationTypeVO> relationList=new ArrayList<RelationTypeVO>();
		if(findByType.getData().size()>0) {
			for (int j = 0; j < findByType.getData().size(); j++) {
				RelationTypeVO re = new RelationTypeVO();
				re.setRelationType(findByType.getData().get(j).getValue());
				re.setRelationTypeDesc(findByType.getData().get(j).getLabel());
				relationList.add(re);
			}
		}
		return relationList;
	}

}
