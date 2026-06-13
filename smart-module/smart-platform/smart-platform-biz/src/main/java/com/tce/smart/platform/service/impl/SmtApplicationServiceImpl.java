package com.tce.smart.platform.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.SexType;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.RegexUtils;
import com.tce.smart.data.api.dto.msg.req.RecruitMsgReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendEmailReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteEmailManagerService;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.platform.api.dto.FaceSnapDTO;
import com.tce.smart.platform.api.dto.SmtApplicationDTO;
import com.tce.smart.platform.api.dto.SmtRecruitmentDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtApplicationEducationMapper;
import com.tce.smart.platform.core.mapper.SmtApplicationMapper;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtEmailReceiveService;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.*;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.ImageUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 应聘表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:24
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtApplicationServiceImpl extends ServiceImpl<SmtApplicationMapper, SmtApplication> implements SmtApplicationService {
	@Autowired
	private SmtApplicationMapper mapper;

	@Autowired
	private SmtRecruitmentService recruitmentService;

	@Autowired
	private SmtApplicationEducationService educationService;
	@Autowired
	private SmtApplicationEducationMapper educationMapper;

	@Autowired
	private SmtApplicationWorkService workService;

	@Autowired
	private SmtApplicationEmergencyService emergencyService;

	@Autowired
	private SmtApplicationRelationService relationService;

	@Autowired
	private SmtApplicationFamilyService familyService;

	@Autowired
	private SmtImageService smtImageService;

	@Autowired
	private SmtParkService parkService;

	//@Autowired
	//private RemoteFaceService faceService;

	@Autowired
	private SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;

	@Autowired
	private RemoteSmsManageService remoteSmsManageService;

	@Autowired
	private RemoteDictService remoteDictService;

	@Autowired
	private SmtDeviceTaskService smtDeviceTaskService;

	@Autowired
	private SmtAppStaffAuthService appStaffAuthService;

	@Autowired
	private SmtApplicationEmailService emailService;

	@Autowired
	private RemoteEmailManagerService remoteEmailManagerService;

	@Autowired
	private SmtEmailReceiveService receiveService;

	@Autowired
	private SmtStaffService staffService;

	@Autowired
	private SmtParkBuService smtParkBuService;

	/**
	 * 添加应聘
	 *
	 * @param applictionDto applictionDto
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result addAppliction(AddOrUpApplicationDTO applictionDto) {
		SmtApplication appliction = applictionDto.getSmtApplication();
		checkApplication(appliction);
		//根据图片获取图片id
		String certnoPicId = smtImageService.saveImage(appliction.getParkId(), applictionDto.getCertnoPicture(), SmtImageEnum.TYPE_JOB_APPLY_IDCARD_FRONT.getCode());
		String facePicId = smtImageService.saveImage(appliction.getParkId(), applictionDto.getFacePicture(), SmtImageEnum.TYPE_JOB_APPLY_FACE.getCode());
		//根据身份证照片获取基本信息
		appliction.setCertnoPicId(certnoPicId);
		appliction.setFacePicId(facePicId);
		Integer status = ApplicationStatusEnum.DELIVER_DONE.getCode();
		appliction.setStatus(status);
		appliction.setIsDelete(DeleteStatusEnum.NOT_DELETE.getCode("否"));
		appliction.insert();
		return addApplicationProcess(0, appliction.getId(), appliction.getParkId(), applictionDto.getCreateUserName(), applictionDto.getSmtApplication().getRefuseReason());
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<Long> addWechatAppliction(SaveWechatApplicationDTO saveApplicationDTO) {
		SmtApplication appliction = new SmtApplication();
		if (Objects.isNull(saveApplicationDTO)) {
			return Result.fail(CommonConstants.FAIL, "应聘信息为空");
		}
		if (Objects.isNull(saveApplicationDTO.getRecruitId())) {
			return Result.fail(CommonConstants.FAIL, "岗位ID为空");
		}
		SmtRecruitment smtRecruitment = recruitmentService.getById(saveApplicationDTO.getRecruitId());
		if (Objects.isNull(smtRecruitment)) {
			return Result.fail(CommonConstants.FAIL, "未找到岗位信息");
		}
		SmtApplication queryEntityRs=null;
		try {
			 queryEntityRs = this.getByIdCardNo(saveApplicationDTO.getCertno(),
					saveApplicationDTO.getRecruitId());
		}catch (Exception e){
			throw new TCEException(e.getMessage());
		}
		SmtStaff queryEntitySt = this.getStaffByRs(saveApplicationDTO.getCertno(), saveApplicationDTO.getRecruitId());
		//非离职状体提示已存在
		if (Objects.nonNull(queryEntitySt)) {
			if (!queryEntitySt.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode())) {
				return Result.fail(CommonConstants.FAIL, "应聘信息已存在");
			}
		}
		//非编辑状态
		if (Objects.nonNull(queryEntityRs)) {
			//处于编辑状态更新应聘人员的信息
			if (queryEntityRs.getStatus().equals(ApplicationStatusEnum.EDIT_ING.getCode())) {
				// 保存图片
				String certnoPicId = smtImageService.saveImage(saveApplicationDTO.getParkId(), saveApplicationDTO.getCertnoPicture(), SmtImageEnum.TYPE_JOB_APPLY_IDCARD_FRONT.getCode());
				queryEntityRs.setCertnoPicId(certnoPicId);
				BeanUtils.copyProperties(saveApplicationDTO, queryEntityRs);
				queryEntityRs.setRecruitId(saveApplicationDTO.getRecruitId());
				queryEntityRs.setParkId(smtRecruitment.getParkId());
				queryEntityRs.setIsDelete(DeleteStatusEnum.NOT_DELETE.getCode("否"));
				queryEntityRs.setAge(idNOToAge(saveApplicationDTO.getCertno()));
				queryEntityRs.setStatus(ApplicationStatusEnum.EDIT_ING.getCode());
				queryEntityRs.setHomeAddress(saveApplicationDTO.getHomeAddress());
				this.updateById(queryEntityRs);
				return new Result<>(queryEntityRs.getId());
			} else {
				return Result.fail(CommonConstants.FAIL, "应聘信息已存在");
			}
		}
		// 保存图片
		String certnoPicId = smtImageService.saveImage(saveApplicationDTO.getParkId(), saveApplicationDTO.getCertnoPicture(), SmtImageEnum.TYPE_JOB_APPLY_IDCARD_FRONT.getCode());
		appliction.setCertnoPicId(certnoPicId);
		BeanUtils.copyProperties(saveApplicationDTO, appliction);
		appliction.setRecruitId(saveApplicationDTO.getRecruitId());
		appliction.setParkId(smtRecruitment.getParkId());
		appliction.setIsDelete(DeleteStatusEnum.NOT_DELETE.getCode("否"));
		appliction.setAge(idNOToAge(saveApplicationDTO.getCertno()));
		appliction.setStatus(ApplicationStatusEnum.EDIT_ING.getCode());
		appliction.setHomeAddress(saveApplicationDTO.getHomeAddress());
		appliction.setCreateTime(DateUtil.date());
		this.save(appliction);
		return new Result<>(appliction.getId());
	}

	private SmtStaff getStaffByRs(String certno, Integer recruitId) {
		// TODO Auto-generated method stub
		if (com.tce.smart.common.core.util.StringUtils.isNotBlank(certno) && Objects.nonNull(recruitId)) {
			SmtRecruitment selectById = recruitmentService.getById(recruitId);
			@SuppressWarnings("unused")
			SmtStaff selectOne = staffService.getOne(Wrappers.<SmtStaff>query().lambda()
					.eq(SmtStaff::getCertno, certno)
					.eq(SmtStaff::getCompId, selectById.getCompId())
					.eq(SmtStaff::getJobId, selectById.getJobId())
					.eq(SmtStaff::getJcheId, selectById.getJcheId())
					.eq(SmtStaff::getDepId, selectById.getDepId())
			);
			return selectOne;
		}
		return null;
	}

	@Override
	public Result<Boolean> addMobileFromWechat(SaveWechatApplicationDTO saveApplicationDTO) {
		String phone = saveApplicationDTO.getPhone();
		if (com.tce.smart.common.core.util.StringUtils.isBlank(phone)) {
			throw new TCEException("手机号为空");
		}
		SmtApplication appliction = new SmtApplication();
		appliction.setId(saveApplicationDTO.getApplicatioId());
		appliction.setPhone(phone);
		super.saveOrUpdate(appliction);
		return new Result<>(Boolean.TRUE);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<?> addFaceFromWechat(SaveWechatApplicationDTO saveApplicationDTO) {
		String facePicture = saveApplicationDTO.getFacePicture();
		if (com.tce.smart.common.core.util.StringUtils.isBlank(facePicture)) {
			throw new TCEException("手机号为空");
		}
		//保存图片
		String facePicId = smtImageService.saveImage(saveApplicationDTO.getParkId(), saveApplicationDTO.getFacePicture(), SmtImageEnum.TYPE_JOB_APPLY_FACE.getCode());
		SmtApplication application = new SmtApplication();
		application.setId(saveApplicationDTO.getApplicatioId());
		application.setFacePicId(facePicId);
		super.saveOrUpdate(application);
		return new Result<>(Boolean.TRUE);
	}

	@Override
	public SmtApplication getSimpleInfo(Long id) {
		return super.getById(id);
	}

	/**
	 * 修改应聘
	 *
	 * @param applicationDto
	 * @return
	 */
	@Override
	public Result updateApplicationById(AddOrUpApplicationDTO applicationDto) {

		SmtApplication application = applicationDto.getSmtApplication();
		//修改应聘基本表
		checkApplication(application);
		SmtApplication applicationDb = this.getById(application.getId());
		if (Objects.isNull(applicationDb)) {
			throw new TCEException("数据异常");
		}

		//根据图片获取图片id
		if (applicationDto.getCertnoPicture() != null && !applicationDto.getCertnoPicture().equals("")) {
			if (Objects.nonNull(applicationDb.getCertnoPicId())) {
				smtImageService.updateByCode(applicationDb.getCertnoPicId(), applicationDto.getCertnoPicture());
			} else {
				smtImageService.saveImage(application.getParkId(), applicationDto.getCertnoPicture(), SmtImageEnum.TYPE_JOB_APPLY_IDCARD_FRONT.getCode());
			}
		}

		if (applicationDto.getFacePicture() != null && !applicationDto.getFacePicture().equals("")) {
			if (Objects.nonNull(applicationDb.getCertnoPicId())) {
				smtImageService.updateByCode(applicationDb.getFacePicId(), applicationDto.getFacePicture());
			} else {
				smtImageService.saveImage(application.getParkId(), applicationDto.getFacePicture(), SmtImageEnum.TYPE_JOB_APPLY_FACE.getCode());
			}
		}
		//0-已投递1-已拒绝 2-已邀请3-待入职/4待复试，暂定/5-已入职/6-已入库
		if (Objects.nonNull(application.getStatus())) {
			if (ApplicationStatusEnum.INVITE_DONE.getCode().equals(application.getStatus())) {
				//邀请面试
				if (Objects.isNull(application.getInterviewTime())) {
					return new Result<>(Boolean.FALSE, "面试时间不能为空");
				}
			} else if (ApplicationStatusEnum.ENTRY_DONE.getCode().equals(application.getStatus())) {
				log.info("******入职流程开始********");
				//入职
				ApplicationStaffDTO dto = new ApplicationStaffDTO();
				dto.setApplicationId(application.getId());
				staffService.addStaffToHR(dto);
			}
			//向流程添加表
			SmtApplication selectById = this.getById(application.getId());
			addApplicationProcess(application.getStatus(), application.getId(), selectById.getParkId(), applicationDto.getCreateUserName(), application.getRefuseReason());
		}
		return new Result<>(this.updateById(application));
	}

	@Override
	public Result removeApplicationById(Long id) {
		Integer educationCount =
				educationService.count(Wrappers.<SmtApplicationEducation>query().lambda().eq(SmtApplicationEducation::getApplicationId, id));
		if (educationCount > 0) {
			return new Result<>(Boolean.FALSE, "该应聘已绑定教育经验，删除失败");
		}
		Integer workCount =
				workService.count(Wrappers.<SmtApplicationWork>query().lambda().eq(SmtApplicationWork::getApplicationId, id));
		if (workCount > 0) {
			return new Result<>(Boolean.FALSE, "该应聘已绑定工作经验，删除失败");
		}
		//修改删除状态
		SmtApplication selectById = this.getById(id);
		//是否删除;0：未删；1：已删，默认是0
		selectById.setIsDelete(DeleteStatusEnum.IS_DELETE.getCode("是"));
		return new Result<>(selectById.updateById());
	}

	@Override
	public IPage<ApplicationVO> getSmtApplictionPage(Page page, ApplicationDTO applicationDTO, String rangTime, String ageRang, List<Integer> parkIds) {
		if (StringUtils.isNotBlank(rangTime)) {
			applicationDTO.setStartTime(rangTime.split(",")[0]);
			applicationDTO.setEndTime(rangTime.split(",")[1]);
		}
		if (StringUtils.isNotBlank(ageRang)) {
			if (ageRang.split(",").length > 0) {
				applicationDTO.setStartAge(Integer.parseInt(ageRang.split(",")[0]));
			}
			if (ageRang.split(",").length > 1 && !ageRang.split(",")[1].equals("")) {
				applicationDTO.setEndAge(Integer.parseInt(ageRang.split(",")[1]));
			}
		}
		IPage<ApplicationVO> pageInfo = mapper.getSmtApplictionPage(page, applicationDTO, parkIds);
		return pageInfo;
	}

	@Override
	public ApplicationInfoVO getApplictionById(String id) {
		ApplicationInfoVO appVo = new ApplicationInfoVO();
		//获取基本信息
		SmtApplication application = this.getById(id);
		if (application == null) {
			throw new TCEException("该应聘消息不存在");
		}
		SmtApplicationDTO smtApplicationDTO=new SmtApplicationDTO();
		BeanUtils.copyProperties(application, smtApplicationDTO);
		appVo.setApplication(smtApplicationDTO);
		Integer parkId = application.getParkId();
		SmtPark park = parkService.getById(parkId);
		if (park == null) {
			throw new TCEException("该应聘园区不存在");
		}
		appVo.setParkName(park.getParkName());
//		try {
//			String certnoPicUrl = imageService.buildImageUrl(application.getCertnoPicId());
		appVo.setCertnoPic(application.getCertnoPicId());
//			String facePicUrl = imageService.buildImageUrl(application.getFacePicId());
		appVo.setFacePic(application.getFacePicId());
//		} catch (Exception e) {
//			// TODO: handle exception
//			log.info("获取图片地址失败");
//		}
		//获取招聘信息
		SmtRecruitment recruitment = recruitmentService.getById(application.getRecruitId());
		SmtRecruitmentDTO smtRecruitmentDTO=new SmtRecruitmentDTO();
		BeanUtils.copyProperties(recruitment, smtRecruitmentDTO);
		appVo.setRecruitment(smtRecruitmentDTO);
		//获取教育经验
		List<SmtApplicationEducation> educations = educationService.list(Wrappers.<SmtApplicationEducation>query().lambda().eq(SmtApplicationEducation::getApplicationId, id));
		appVo.setApplicationEducation(educations);
		//获取工作经验
		List<SmtApplicationWork> works = workService.list(Wrappers.<SmtApplicationWork>query().lambda().eq(SmtApplicationWork::getApplicationId, id));
		appVo.setApplicationWork(works);
		List<SmtApplicationEmergency> emergencys = emergencyService.list(Wrappers.<SmtApplicationEmergency>query().lambda().eq(SmtApplicationEmergency::getApplicationId, id));
		appVo.setApplicationEmergency(emergencys);
		//获取人事关系
		List<SmtApplicationRelation> relations = relationService.list(Wrappers.<SmtApplicationRelation>query().lambda().eq(SmtApplicationRelation::getApplicationId, id));
		appVo.setApplicationRelation(relations);
		String maxEduStr = "";
		try {
			Integer maxEdu = educationMapper.getMaxEdu(id);
			Result<SysDict> result = remoteDictService.findByValue(DictConstants.EDUCATION_TYPE, String.valueOf(maxEdu), SecurityConstants.FROM_IN);
			maxEduStr = result.getData().getDescription();
			appVo.setApplicantEducation(maxEduStr == null ? "" : maxEduStr);
		} catch (Exception e) {
			appVo.setApplicantEducation("");
			log.error("查询学历字典异常", e);
		}
		return appVo;
	}


	public Result checkApplication(SmtApplication application) {
		if (!RegexUtils.matchName(application.getName())) {
			return new Result<>(Boolean.FALSE, "姓名只允许汉字、字母与数字的组合，最长为30个字符");
		}
		if (!RegexUtils.matchPhone(application.getPhone())) {
			return new Result<>(Boolean.FALSE, "请输入正确的手机号");
		}
		return new Result<>(true);
	}


	/**
	 * 添加应聘流程表
	 *
	 * @param status        应聘桩体
	 * @param applicationId 应聘id
	 * @param parkId        园区id
	 * @param refuse
	 * @return
	 */
	public Result addApplicationProcess(Integer status, Long applicationId, Integer parkId, String user, String refuse) {
		SmtApplicationProcess process = new SmtApplicationProcess();
		//默认投递
		process.setStatus(status);
		process.setApplicationId(applicationId);
		process.setCreateTime(DateUtil.date());
		process.setCreateUserName(user); //从缓存中获取
		process.setRemark(refuse);
		return new Result<>(process.insert());
	}

	/**
	 * 查询应聘流程
	 */
	@Override
	public List<SmtApplicationProcess> getApplicationProcess(Long id) {
		// TODO Auto-generated method stub
		return mapper.getApplicationProcess(id);
	}


	/**
	 * 批量修改应聘状态
	 */
	@SuppressWarnings("unused")
	@Override
	public Result<Boolean> updateApplicationList(UpApplicationListDTO application) {
		List<Long> applicationIds = application.getIds();
		SmtApplicationProcess process = new SmtApplicationProcess();
		SmtStaff selectOneStaff = staffService.getOne(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getBadge, application.getCreateUserNme()));
		for (Long long1 : applicationIds) {
			SmtApplication selectById = this.getById(long1);
			if (selectById == null) {
				throw new TCEException("该工号的员工不存在");
			}

			SmtRecruitment recruitById = recruitmentService.getById(selectById.getRecruitId());
			if (selectById != null) {
				// 邀请面试,复试，待入职
				if (ApplicationStatusEnum.INVITE_DONE.getCode().equals(application.getStatus())
						|| ApplicationStatusEnum.ENTRY_TO_DO.getCode().equals(application.getStatus())
						|| ApplicationStatusEnum.REFACE_TO_DO.getCode().equals(application.getStatus())) {

					if (Objects.isNull(application.getInterviewTime())) {
						return new Result<>(Boolean.FALSE, "时间不能为空");
					}

					selectById.setInterviewTime(application.getInterviewTime());
				} else if (ApplicationStatusEnum.REFUSE_DONE.getCode().equals(application.getStatus())) {//已拒绝
					selectById.setRefuseReason(application.getRefuseReason());
				} else if (ApplicationStatusEnum.ENTRY_DONE.getCode().equals(application.getStatus())) {//已入职
					// 入职
					ApplicationStaffDTO dto = new ApplicationStaffDTO();
					dto.setApplicationId(selectById.getId());
					staffService.addStaffToHR(dto);
				}
				selectById.setStatus(application.getStatus());
				// 修改应聘主表
				this.updateById(selectById);
				// 添加流程表
				process.setStatus(application.getStatus());
				process.setApplicationId(long1);
				process.setCreateTime(DateUtil.date());
				if (ObjectUtil.isNull(selectOneStaff)) {
					process.setCreateUserName(application.getCreateUserNme()); // 从权限或缓存中获取
				} else {
					process.setCreateUserName(application.getCreateUserNme() + "-" + selectOneStaff.getName());
				}
				process.setParkId(selectById.getParkId());
				process.setRemark(application.getRefuseReason());
				process.insert();
				// 面试，复试，待入职发送
				if (ApplicationStatusEnum.INVITE_DONE.getCode().equals(application.getStatus())
						|| ApplicationStatusEnum.ENTRY_TO_DO.getCode().equals(application.getStatus())
						|| ApplicationStatusEnum.REFACE_TO_DO.getCode().equals(application.getStatus())) {
					//面试，待复试，待入职注册下发
					register(selectById);
					try {
						if (!ApplicationStatusEnum.ENTRY_DONE.getCode().equals(application.getStatus())) {
							sendMessage(recruitById, selectById, application);
						}
					} catch (Exception e) {
						log.error("发送面试邀约短信异常", e);
					}
				}

			}
		}
		return new Result<>(Boolean.TRUE);
	}


	private void register(SmtApplication application) {
		Long maxTime = DateUtils.offsetHour(application.getInterviewTime(), +1).getTime() / 1000;
		List<SmtDeviceAuthorityRelation> selectList = smtDeviceAuthorityRelationService
				.getRelationAuth(application.getParkId(), BusinessAuthorityEnum.STAFF_FACE.getCode(), DeviceAuthorityEnum.INTERVIEWER);
		log.info("DeviceAuthorityListCount:" + selectList.size());
		DeviceTaskVO deviceTaskVO = null;
		for (int i = 0; i < selectList.size(); i++) {
			deviceTaskVO = new DeviceTaskVO();
			deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
			deviceTaskVO.setServiceType(DeviceTaskConstants.CARD_RECRUIT);
			deviceTaskVO.setCardNo(application.getId().toString());
			deviceTaskVO.setDeviceCode(selectList.get(i).getDeviceId());
			deviceTaskVO.setGeneral(application.getName());
			deviceTaskVO.setImageId(application.getFacePicId());
			deviceTaskVO.setCardType(SmtVisitorEnum.CARD_TYPE_1.getType());
			deviceTaskVO.setStatus(DeviceTaskConstants.FAIL);
			deviceTaskVO.setDeviceType(DeviceTaskConstants.CARD);
			deviceTaskVO.setStartTime(DateUtils.offsetHour(application.getInterviewTime(), -1).getTime() / 1000);
			deviceTaskVO.setOverTime(DateUtils.offsetHour(application.getInterviewTime(), +1).getTime() / 1000);
			smtDeviceTaskService.saveTask(deviceTaskVO);
		}
	}

	/**
	 * 发送短信通知
	 *
	 * @param recruitById recruitById
	 * @param selectById  selectById
	 * @param application application
	 */
	public void sendMessage(SmtRecruitment recruitById, SmtApplication selectById, UpApplicationListDTO application) {
		//给应聘发送短信,调用短信发送接口
		//0-已投递1-已拒绝 2-已邀请3-待入职/4待复试，暂定/5-已入职/6-已入库
		SmtPark park = parkService.getById(recruitById.getParkId());
		SimpleDateFormat weekFM = new SimpleDateFormat("E");
		SimpleDateFormat dateFm = new SimpleDateFormat("HH:mm");
		SimpleDateFormat dateDay = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		SmtApplicationEmail appEmail = emailService.getOne(Wrappers.<SmtApplicationEmail>query().lambda().eq(SmtApplicationEmail::getApplicationId, selectById.getId()));
		SendEmailReqDTO sendEmailAo = new SendEmailReqDTO();
		if (appEmail != null) {
			sendEmailAo.setInbox(appEmail.getEmail());
			sendEmailAo.setTempCode(SmsTemplateEnum.EMAIN_2201.getCode());
			sendEmailAo.setParkId(recruitById.getParkId());
			Map<String, String> param = new HashMap<>();
			param.put("姓名", selectById.getName());
			param.put("岗位", recruitById.getJobName());
			param.put("面试时间", dateTime.format(application.getInterviewTime()));
			param.put("面试地址",park.getParkAddress());
			param.put("园区地址",park.getParkAddress());
			param.put("联系方式",recruitById.getRelation());
			param.put("园区名称",park.getParkName());
			param.put("岗位职责", recruitById.getJobCotent());
			param.put("周几", weekFM.format(application.getInterviewTime()));
			param.put("BU", recruitById.getCompName());
			param.put("部门", recruitById.getDepName());
			param.put("通知时间", dateDay.format(DateUtils.date()));
			param.put("入职时间", dateTime.format(application.getInterviewTime()));
			param.put("电话",selectById.getPhone());
			sendEmailAo.setParam(param);
		}

		RecruitMsgReqDTO recruitMsgAo = new RecruitMsgReqDTO();
		recruitMsgAo.setNumber(selectById.getPhone());
		recruitMsgAo.setApplicantName(selectById.getName());
		recruitMsgAo.setParkAddress(park.getParkAddress());
		recruitMsgAo.setParkPhone(park.getParkPhone());

		if (ApplicationStatusEnum.INVITE_DONE.getCode().equals(application.getStatus())) {//已邀请
			recruitMsgAo.setFaceTime(DateUtils.formatDateTime(application.getInterviewTime()));
			recruitMsgAo.setTempCode(SmsTemplateEnum.RECRUIT_2001.getCode());
			if (ObjectUtil.isNotNull(sendEmailAo.getInbox())) {
				//给面试人员发送邮件
				log.info("=======remoteSmsManageService.sendRecruitSms  params==========={}", sendEmailAo);
				Result<?> sendEmail = remoteEmailManagerService.sendEmail(sendEmailAo);
				log.info("=======remoteEmailManagerService.sendEmail result==========={}", sendEmail);
			}
		} else if (ApplicationStatusEnum.REFACE_TO_DO.getCode().equals(application.getStatus())) {//待复试
			recruitMsgAo.setFaceAgainTime(DateUtils.formatDateTime(application.getInterviewTime()));
			recruitMsgAo.setTempCode(SmsTemplateEnum.RECRUIT_2002.getCode());
		} else if (ApplicationStatusEnum.ENTRY_TO_DO.getCode().equals(application.getStatus())) {//待入职
			//获取url链接地址
			Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.LINK_URL, SecurityConstants.FROM_IN);
			recruitMsgAo.setBuName(recruitById.getCompName());
			recruitMsgAo.setDeptName(recruitById.getDepName());
			recruitMsgAo.setJobName(recruitById.getJobName());
			recruitMsgAo.setEntryDate(dateDay.format(application.getInterviewTime()));
			recruitMsgAo.setEntryTime(dateFm.format(application.getInterviewTime()));
			recruitMsgAo.setEntryWeek(weekFM.format(application.getInterviewTime()).replace("星期", ""));
			if (findByType.getData().size() > 0) {
				recruitMsgAo.setLinkUrl(findByType.getData().get(0).getLabel() + selectById.getId());
			}
			recruitMsgAo.setTempCode(SmsTemplateEnum.RECRUIT_2003.getCode());
			//给录取人员发送邮件
			sendEmailAo.setTempCode(SmsTemplateEnum.EMAIN_6001.getCode());
			sendEmailAo.setParkId(recruitById.getParkId());
			log.info("=======remoteSmsManageService.sendRecruitSms  params==========={}", sendEmailAo);
			Result<?> sendEmail = remoteEmailManagerService.sendEmail(sendEmailAo);
			log.info("=======remoteEmailManagerService.sendEmail result==========={}", sendEmail);
			//给内部行政员工发邮件
			sendEmailToHR(recruitById, selectById);
		}
		//发送短信通知
		Result<?> smsSendResult = remoteSmsManageService.sendRecruitSms(recruitMsgAo);
		log.info("=======remoteSmsManageService.sendRecruitSms==========={}", smsSendResult);

	}

	//给内部行政员工发邮件
	public void sendEmailToHR(SmtRecruitment recruitById, SmtApplication selectById) {
		//获取hr邮箱
		SimpleDateFormat weekFM = new SimpleDateFormat("E");
		SimpleDateFormat dateFm = new SimpleDateFormat("HH:mm");
		SimpleDateFormat dateDay = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String templateCode = SmsTemplateEnum.EMAIN_5001.getCode();
		SmtPark park = parkService.getById(recruitById.getParkId());
		Result<List<SmtEmailReceive>> byCode = receiveService.getByCode(templateCode, recruitById.getParkId());
		if (byCode.isSuccess()) {
			if (byCode.getData() != null) {
				List<SmtEmailReceive> data = byCode.getData();
				for (SmtEmailReceive smtEmailReceive : data) {
					SendEmailReqDTO sendEmailAo = new SendEmailReqDTO();
					sendEmailAo.setInbox(smtEmailReceive.getEmail());
					sendEmailAo.setTempCode(SmsTemplateEnum.EMAIN_5001.getCode());
					Map<String, String> param = new HashMap<>();
					param.put("姓名", selectById.getName());
					param.put("岗位", recruitById.getJobName());
					param.put("面试时间", dateTime.format(selectById.getInterviewTime()));
					param.put("面试地址",park.getParkAddress());
					param.put("园区地址",park.getParkAddress());
					param.put("联系方式",recruitById.getRelation());
					param.put("园区名称",park.getParkName());
					param.put("岗位职责", recruitById.getJobCotent());
					param.put("周几", weekFM.format(selectById.getInterviewTime()));
					param.put("BU", recruitById.getCompName());
					param.put("部门", recruitById.getDepName());
					param.put("通知时间", dateDay.format(DateUtils.date()));
					param.put("入职时间", dateTime.format(selectById.getInterviewTime()));
					param.put("电话",selectById.getPhone());
					sendEmailAo.setParam(param);
					sendEmailAo.setParkId(recruitById.getParkId());
					log.info("=======remoteSmsManageService.sendRecruitSms  params==========={}", sendEmailAo);
					Result<?> sendEmail = remoteEmailManagerService.sendEmail(sendEmailAo);
					log.info("=======remoteEmailManagerService.sendEmail result==========={}", sendEmail);
				}
			}
		}
	}

	/**
	 * 根据人脸图片搜索应聘信息
	 */
	@Override
	public Result<FaceApplicationVO> getByface(String facePhoto) {
		String photo = JSONUtil.parseObj(facePhoto).get("facePhoto").toString();
		String base64Face = photo;
		Result<FaceSnapDTO> faceSearch = null;
		try {
			//TODO 图片搜索
			//faceSearch = faceService.faceSearch(base64Face, SecurityConstants.FROM_IN);
		} catch (Exception e) {
			log.error("人脸搜索简历异常", e);
			throw new TCEException("人脸搜索简历异常");
		}
		log.info("faceSearch result FaceSnapDTO:" + faceSearch);
		//根基人员id查询简历结果
		if (faceSearch.getData() != null) {
			FaceSnapDTO smtFace = JSONUtil.toBean(JSONUtil.parseObj(faceSearch.getData()), FaceSnapDTO.class);
			Long applicationId = smtFace.getPersonId();
			FaceApplicationVO vo = mapper.queryFaceApplication(applicationId);
			if (vo != null) {
				//smtFace.getFaceBase64()
				//String buildImageUrl = imageService.buildImageUrl(smtFace.getFaceId());
				vo.setApplicantPhoto(smtFace.getFaceId());
				if (vo.getApplicantGender().equals(SexType.WOMAN.getCode().toString())) {
					vo.setApplicantGender(SexType.WOMAN.getDesc());
				} else if (vo.getApplicantGender().equals(SexType.MAN.getCode().toString())) {
					vo.setApplicantGender(SexType.MAN.getDesc());
				}
				String maxEduStr = "";
				try {
					Integer maxEdu = educationMapper.getMaxEdu(applicationId.toString());
					if (maxEdu != null) {
						Result<SysDict> result = remoteDictService.findByValue(DictConstants.EDUCATION_TYPE, String.valueOf(maxEdu), SecurityConstants.FROM_IN);
						maxEduStr = result.getData().getDescription();
						vo.setApplicantEducation(maxEduStr == null ? "" : maxEduStr);
					}
				} catch (Exception e) {
					vo.setApplicantEducation("");
					log.error("查询学历字典异常", e);
				}
			}
			return new Result<FaceApplicationVO>(vo);
		} else {
			return new Result<>();
		}
	}


	/**
	 * 根据应聘id获取简历信息
	 */
	@Override
	public Result getApplicationResume(Long id) {
		// TODO Auto-generated method stub
		//SmtApplicationResume resume= mapper.getApplicationResume(id);
		return null;
	}


	@Override
	public IPage<ApplicationListVO> getSmtApplictionList(Page page, ApplicationListDTO applicationDTO) {
		// TODO Auto-generated method stub
		//获取可以查得职层权限id
		List<String> staffRecruitAuthLeve = appStaffAuthService.getStaffRecruitAuthLeve(applicationDTO.getStaffBadge());
		if (staffRecruitAuthLeve == null) {
			staffRecruitAuthLeve = new ArrayList<>();
			staffRecruitAuthLeve.add("-1");
		}
		//查询该账户可以查看得园区
		SmtStaff oneStaff = staffService.getOne(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getBadge, applicationDTO.getStaffBadge()));
		List<SmtPark> parkListByBu = smtParkBuService.getParkListByBu(Long.parseLong(oneStaff.getCompId()));
		List<Integer> parkIds = new ArrayList<>();
		for (SmtPark park : parkListByBu) {
			parkIds.add(park.getId());
		}

		IPage<ApplicationListVO> pageInfo = mapper.getSmtApplictionList(page, applicationDTO, staffRecruitAuthLeve, parkIds);

		for (int i = 0; i < pageInfo.getRecords().size(); i++) {
			pageInfo.getRecords().get(i).setApplicantGender(SexType.desc(Integer.parseInt(pageInfo.getRecords().get(i).getApplicantGender())));
			//查询最高学历
			String maxEduStr = "";
			try {
				Integer maxEdu = educationMapper.getMaxEdu(pageInfo.getRecords().get(i).getApplicationId());
				if (maxEdu != null) {
					Result<SysDict> result = remoteDictService.findByValue(DictConstants.EDUCATION_TYPE, String.valueOf(maxEdu), SecurityConstants.FROM_IN);
					maxEduStr = result.getData().getDescription();
				}
			} catch (Exception e) {
				log.error("查询学历字典异常", e);
			}
			pageInfo.getRecords().get(i).setApplicantEducation(maxEduStr);
		}
		return pageInfo;
	}


	@Override
	public List<JobVO> getJobList(Integer parkId, String jobName) {
		// TODO Auto-generated method stub
		SmtRecruitment sm = new SmtRecruitment();
		//查询正在招聘中的岗位
		List<SmtRecruitment> list = recruitmentService.list(Wrappers.<SmtRecruitment>query().lambda()
				.eq(SmtRecruitment::getParkId, parkId)
				.like(StringUtils.isNotBlank(jobName), SmtRecruitment::getJobName, jobName));
		List<JobVO> jobVoList = new ArrayList<>();
		JobVO jobVo = null;
		for (SmtRecruitment re : list) {
			jobVo = new JobVO();
			jobVo.setJobId(re.getJobId());
			jobVo.setJobName(re.getJobName());
			jobVoList.add(jobVo);
		}
		return jobVoList;
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public SmtApplication getByIdCardNo(String certno, Integer recruitId) {
		if (com.tce.smart.common.core.util.StringUtils.isNotBlank(certno) && Objects.nonNull(recruitId)) {
			int count = this.count(Wrappers.<SmtApplication>lambdaQuery()
					.eq(SmtApplication::getCertno, certno)
					.eq(SmtApplication::getRecruitId, recruitId).ne(SmtApplication::getStatus, -1));
			if (count > 0) {
				throw new TCEException("不能重复投递简历");
			}
			@SuppressWarnings("unused")
			List<SmtApplication> selectList = baseMapper.selectList(Wrappers.<SmtApplication>query().lambda()
					.eq(SmtApplication::getCertno, certno)
					.eq(SmtApplication::getRecruitId, recruitId).orderByDesc(SmtApplication::getCreateTime));
			if(selectList.size()>0)
			{
				return selectList.get(0);
			}
		}
		return null;
	}

	/**
	 * 投递
	 */
	@Override
	public Result<Boolean> delivery(Long applicationId, Integer maritalStatus) {
		// TODO Auto-generated method stub
		SmtApplication selectById = this.getById(applicationId);
		if (selectById.getStatus().equals(ApplicationStatusEnum.DELIVER_DONE.getCode())) {
			return new Result<>(true);
		}
		if (selectById.getFacePicId() != null) {
			FaceSnapDTO faceInfo = new FaceSnapDTO();
			faceInfo.setIdentityCard(selectById.getCertno());
			faceInfo.setName(selectById.getName());
			faceInfo.setParkId(selectById.getParkId());
			faceInfo.setParkName("");
			faceInfo.setSex(selectById.getSex());
			faceInfo.setType(FaceSnapTypeEnum.Application.getCode());//应聘者
			faceInfo.setPersonId(applicationId);
			faceInfo.setFaceId(selectById.getFacePicId());
			//往Es里建索引
			log.info("remote remoteFaceService.faceStorage request, personId={}", applicationId);
			//Result faceStorage = faceService.faceStorage(faceInfo,  SecurityConstants.FROM_IN);
			//log.info("remote remoteFaceService.faceStorage result=[{}]", faceStorage);
			//FaceStorageResultDTO smtVehicless = JSONUtil.toBean(JSONUtil.parseObj(faceStorage.getData()), FaceStorageResultDTO.class);
		}
		selectById.setStatus(ApplicationStatusEnum.DELIVER_DONE.getCode());
		selectById.setApplyDate(DateUtils.date());
		selectById.setMaritalStatus(maritalStatus);
		//更新为投递状态
		this.updateById(selectById);
		addApplicationProcess(ApplicationStatusEnum.DELIVER_DONE.getCode(), applicationId, selectById.getParkId(), selectById.getName(), null);
		return new Result<>(true);
	}


	public Integer idNOToAge(String IdNO) {
		String birthTimeString = IdNO.substring(6, 10) + "-" + IdNO.substring(10, 12) + "-" + IdNO.substring(12, 14);
		// 先截取到字符串中的年、月、日
		String[] strs = birthTimeString.trim().split("-");
		int selectYear = Integer.parseInt(strs[0]);
		int selectMonth = Integer.parseInt(strs[1]);
		int selectDay = Integer.parseInt(strs[2]);
		// 得到当前时间的年、月、日
		Calendar cal = Calendar.getInstance();
		int yearNow = cal.get(Calendar.YEAR);
		int monthNow = cal.get(Calendar.MONTH) + 1;
		int dayNow = cal.get(Calendar.DATE);
		// 用当前年月日减去生日年月日
		int yearMinus = yearNow - selectYear;
		int monthMinus = monthNow - selectMonth;
		int dayMinus = dayNow - selectDay;
		int age = yearMinus;
		if (yearMinus < 0) {// 选了未来的年份
			age = 0;
		} else if (yearMinus == 0) {// 同年的，要么为1，要么为0
			if (monthMinus < 0) {// 选了未来的月份
				age = 0;
			} else if (monthMinus == 0) {// 同月份的
				if (dayMinus < 0) {// 选了未来的日期
					age = 0;
				} else if (dayMinus >= 0) {
					age = 1;
				}
			} else if (monthMinus > 0) {
				age = 1;
			}
		} else if (yearMinus > 0) {
			if (monthMinus < 0) {// 当前月>生日月
			} else if (monthMinus == 0) {// 同月份的，再根据日期计算年龄
				if (dayMinus < 0) {
				} else if (dayMinus >= 0) {
					age = age + 1;
				}
			} else if (monthMinus > 0) {
				age = age + 1;
			}
		}
		return age;
	}

	@Override
	public Result getProcess(String id) {
		// TODO Auto-generated method stub
		return new Result<>(mapper.getApplicationProcess(Long.parseLong(id)));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public Result getApplictionInfoById(String id) {
		// TODO Auto-generated method stub
		SmtApplicationDetailVO appVo = new SmtApplicationDetailVO();
		//获取基本信息
		SmtApplication application = this.getById(id);
		if (application == null) {
			throw new TCEException("该应聘消息不存在");
		}
		appVo.setApplication(application);
		Integer parkId = application.getParkId();
		SmtPark park = parkService.getById(parkId);
		if (park == null) {
			throw new TCEException("该应聘园区不存在");
		}
		appVo.setParkName(park.getParkName());
		try {
			/*String certnoPicUrl = imageService.buildImageUrl(application.getCertnoPicId());
			appVo.setCertnoPic(certnoPicUrl);
			String facePicUrl = imageService.buildImageUrl(application.getFacePicId());
			appVo.setFacePic(facePicUrl);*/
			SmtImage certnoImage = smtImageService.getByCode(application.getCertnoPicId());
			if (Objects.nonNull(certnoImage)) {
				if (ObjectUtil.isNotNull(certnoImage.getImageSmall())) {
					appVo.setCertnoPic(ImageUtils.encodeImage(certnoImage.getImageSmall()));
				} else {
					appVo.setCertnoPic(ImageUtils.encodeImage(certnoImage.getImage()));
				}
			}

			SmtImage faceImage = smtImageService.getByCode(application.getCertnoPicId());
			if (Objects.nonNull(faceImage)) {
				if (ObjectUtil.isNotNull(certnoImage.getImageSmall())) {
					appVo.setFacePic(ImageUtils.encodeImage(certnoImage.getImageSmall()));
				} else {
					appVo.setFacePic(ImageUtils.encodeImage(certnoImage.getImage()));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.info("获取图片地址失败");
		}
		String maxEduStr = "";
		try {
			Integer maxEdu = educationMapper.getMaxEdu(id);
			if (ObjectUtil.isNotNull(maxEdu)) {
				Result<SysDict> result = remoteDictService.findByValue(DictConstants.EDUCATION_TYPE,
						String.valueOf(maxEdu), SecurityConstants.FROM_IN);
				maxEduStr = result.getData().getDescription();
			}
			appVo.setApplicantEducation(maxEduStr == null ? "" : maxEduStr);
		} catch (Exception e) {
			appVo.setApplicantEducation("");
			log.error("查询学历字典异常", e);
		}

		//获取招聘信息
		SmtRecruitment recruitment = recruitmentService.getById(application.getRecruitId());
		appVo.setRecruitment(recruitment);
		//获取教育经验
		List<ApplicationEducationVO> eduVo = getEducationDe(id);
		appVo.setApplicationEducation(eduVo);
		//获取工作经验
		List<SmtApplicationWork> works =
				workService.list(Wrappers.<SmtApplicationWork>query().lambda().eq(SmtApplicationWork::getApplicationId
						, id));
		appVo.setApplicationWork(works);
		List<RelationVO> emergencyList = getEmergencyDe(id);
		appVo.setApplicationEmergency(emergencyList);
		//获取人事关系
		List<OrgrelationVO> relationList = getOrgrelationDe(id);
		appVo.setApplicationRelation(relationList);
		//获取家庭成员
		List<FamilyMemberVO> familyList = getFamilyDe(id);
		appVo.setApplicationFamilyMember(familyList);
		appVo.setApplyDate(application.getApplyDate());
		//获取工作邮箱
		SmtApplicationEmail selectOne =
				emailService.getOne(Wrappers.<SmtApplicationEmail>query().lambda().eq(SmtApplicationEmail::getApplicationId, id));
		if (selectOne != null) {
			appVo.setApplicantEmail(selectOne.getEmail());
		}
		return new Result<>(appVo);
	}

	private List<FamilyMemberVO> getFamilyDe(String id) {
		// TODO Auto-generated method stub
		List<SmtApplicationFamily> familys = familyService.list(Wrappers.<SmtApplicationFamily>query().lambda().eq(SmtApplicationFamily::getApplicationId, id));
		Result<List<SysDict>> findRelationByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN);
		List<FamilyMemberVO> falist = new ArrayList<>();
		for (SmtApplicationFamily fa : familys) {
			FamilyMemberVO vo = new FamilyMemberVO();
			vo.setEmergencyPhone(fa.getPhone());
			vo.setFamilyBirthday(fa.getBirth());
			vo.setFamilyCompany(fa.getCompany());
			vo.setFamilyGender(fa.getSex());
			vo.setFamilyJob(fa.getJob());
			vo.setFamilyName(fa.getName());
			vo.setRelationType(Integer.parseInt(fa.getRelation()));
			if (findRelationByType.getData().size() > 0) {
				for (int j = 0; j < findRelationByType.getData().size(); j++) {
					String value = findRelationByType.getData().get(j).getValue();
					if (value.equals(fa.getRelation())) {
						vo.setRelationTypeDesc(findRelationByType.getData().get(j).getLabel());
						break;
					}
				}
			}
			falist.add(vo);
		}
		return falist;
	}

	//获取紧急联系人，描述
	private List<RelationVO> getEmergencyDe(String id) {
		List<SmtApplicationEmergency> emergencys = emergencyService.list(Wrappers.<SmtApplicationEmergency>query().lambda().eq(SmtApplicationEmergency::getApplicationId, id));
		Result<List<SysDict>> findRelationByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN);
		List<RelationVO> emergencyList = new ArrayList<>();
		for (SmtApplicationEmergency em : emergencys) {
			RelationVO vo = new RelationVO();
			vo.setEmergencyName(em.getEmergencyName());
			vo.setEmergencyPhone(em.getTelephont());
			vo.setRelationType(Integer.parseInt(em.getRelation()));
			if (findRelationByType.getData().size() > 0) {
				for (int j = 0; j < findRelationByType.getData().size(); j++) {
					String value = findRelationByType.getData().get(j).getValue();
					if (value.equals(em.getRelation())) {
						vo.setRelationTypeDesc(findRelationByType.getData().get(j).getLabel());
						break;
					}
				}
			}
			emergencyList.add(vo);
		}
		return emergencyList;
	}

	//获取教育经验，描述信息
	private List<ApplicationEducationVO> getEducationDe(String id) {
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.EDUCATION_TYPE, SecurityConstants.FROM_IN);
		Result<List<SysDict>> findDegreeByType = remoteDictService.findByType(DictConstants.DEGREE_TYPE, SecurityConstants.FROM_IN);
		List<SmtApplicationEducation> educations = educationService.list(Wrappers.<SmtApplicationEducation>query().lambda().eq(SmtApplicationEducation::getApplicationId, id));
		List<ApplicationEducationVO> eduVo = new ArrayList<>();
		for (SmtApplicationEducation smt : educations) {
			ApplicationEducationVO vo = new ApplicationEducationVO();
			vo.setEducationHisId(smt.getApplicationId().toString());
			vo.setSchoolName(smt.getSchoolName());
			vo.setDegree(smt.getDegree());
			vo.setEducation(smt.getEducation());
			vo.setMajor(smt.getMajor());
			vo.setStartTime(smt.getStartTime());
			vo.setEndTime(smt.getEndTime());
			vo.setIsHighDegreeType(smt.getIsHighDegreeType());
			vo.setIsHighEduType(smt.getIsHighEduType());
			vo.setEducationDesc("");
			vo.setDegreeDesc("");
			if (findByType.getData().size() > 0) {
				for (int j = 0; j < findByType.getData().size(); j++) {
					String value = findByType.getData().get(j).getValue();
					if (value.equals(smt.getEducation())) {
						vo.setEducationDesc(findByType.getData().get(j).getLabel());
						break;
					}
				}
			}
			if (findDegreeByType.getData().size() > 0) {
				for (int j = 0; j < findDegreeByType.getData().size(); j++) {
					String value = findDegreeByType.getData().get(j).getValue();
					if (value.equals(smt.getDegree())) {
						vo.setDegreeDesc(findDegreeByType.getData().get(j).getLabel());
						break;
					}
				}
			}
			eduVo.add(vo);
		}
		return eduVo;
	}

	//人事关系重构
	private List<OrgrelationVO> getOrgrelationDe(String id) {
		Result<List<SysDict>> findRelationByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN);
		List<SmtApplicationRelation> relations = relationService.list(Wrappers.<SmtApplicationRelation>query().lambda().eq(SmtApplicationRelation::getApplicationId, id));
		List<OrgrelationVO> relationList = new ArrayList<>();
		for (SmtApplicationRelation re : relations) {
			OrgrelationVO vo = new OrgrelationVO();
			vo.setOrgPersonBu(re.getCompName());
			vo.setOrgPersonDept(re.getDeptName());
			vo.setOrgPersonGender(re.getSex());
			vo.setRelationType(Integer.parseInt(re.getRelation()));
			vo.setRelationTypeDesc("");
			vo.setOrgPersonSection(re.getClassName());
			vo.setOrgPersonName(re.getName());
			vo.setJobName(re.getJobName());
			vo.setRelationDetail(re.getRelationDetail());
			if (findRelationByType.getData().size() > 0) {
				for (int j = 0; j < findRelationByType.getData().size(); j++) {
					String value = findRelationByType.getData().get(j).getValue();
					if (value.equals(re.getRelation())) {
						vo.setRelationTypeDesc(findRelationByType.getData().get(j).getLabel());
						break;
					}
				}
			}
			relationList.add(vo);
		}
		return relationList;
	}

	@Override
	public Result updateApplicationToStaff(UpApplicationListDTO application) {
		// TODO Auto-generated method stub
		List<Long> applicationIds = application.getIds();
		for (Long long1 : applicationIds) {
			SmtApplication selectById = this.getById(long1);
			if (selectById == null) {
				throw new TCEException("该应聘人员不存在");
			}

			SmtRecruitment recruitById = recruitmentService.getById(selectById.getRecruitId());
			if (selectById != null) {
				if (ApplicationStatusEnum.ENTRY_DONE.getCode().equals(application.getStatus())) {//已入职
					// 入职
					ApplicationStaffDTO dto = new ApplicationStaffDTO();
					dto.setApplicationId(selectById.getId());
					staffService.addStaffToHR(dto);
				}
			}
		}
		return new Result<>(Boolean.TRUE);
	}

}
