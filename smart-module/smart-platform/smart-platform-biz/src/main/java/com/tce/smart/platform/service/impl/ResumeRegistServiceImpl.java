package com.tce.smart.platform.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.SexType;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.msg.req.SendSmsCodeMsgReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.platform.api.dto.CardDTO;
import com.tce.smart.platform.api.dto.FaceSnapDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.DicContentVO;
import com.tce.smart.platform.core.vo.OcrReadCardImgVO;
import com.tce.smart.platform.service.IOcrService;
import com.tce.smart.platform.service.ResumeRegistService;
import com.tce.smart.platform.service.SmtApplicationEducationService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.constant.RedisKeyConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ResumeRegistServiceImpl  implements ResumeRegistService  {

	private static final Integer MESSAGE_LENGTH = 6;

	//@Autowired
	//private RemoteFaceService remoteFaceService;

	@Autowired
	private SmtApplicationProcessMapper processMapper;


	@Autowired
	private SmtApplicationEmailMapper emailMapper;

	@Autowired
	private RemoteDictService remoteDictService;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private SmtRecruitmentMapper recruitmentMapper;

	@Autowired
	private RemoteSmsManageService remoteSmsManageService;

	@Autowired
	private SmtApplicationMapper mapper;

	@Autowired
	private SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;

	@Autowired
	private SmtApplicationEducationService educationService;

	@Autowired
	private SmtApplicationWorkMapper workMapper;

	@Autowired
	private SmtApplicationEmergencyMapper emergencyMapper;

	@Autowired
	private SmtApplicationFamilyMapper familyMapper;

	@Autowired
	private IOcrService ocrService;

	@Autowired
	private SmtImageService smtImageService;

	@Autowired
	private SmtDeviceTaskService smtDeviceTaskService;

	@Autowired
	private SmtDeviceAuthorityRelationMapper smtDeviceAuthorityRelationMapper;


	@Value("${spring.face.compare-value}")
	private String compareValue;

	/**
	 * 通过其他应聘网站录入招聘数据，证件识别
	 */
	@Override
	public OcrReadCardImgVO readCardImg(OcrReadCardImgDTO ocrReadCardImgDTO) {
		// TODO Auto-generated method stub

		if(ObjectUtil.isNull(ocrReadCardImgDTO.getId()))
		{
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "未获取到岗位信息");
		}

		SmtRecruitment selectById = recruitmentMapper.selectById(ocrReadCardImgDTO.getId());
		if(ObjectUtil.isNull(selectById))
		{
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "岗位信息不存在");
		}
		OcrReadCardImgVO ocrReadCardImgVO;
		//读取身份证正面信息
		OcrIdCardDTO frontInfo = ocrService.readIdCardFontImg(ocrReadCardImgDTO.getIdCardFrontImg());
		log.info("身份证正面信息识别完成");
		//, JSONUtil.toJsonStr(frontInfo)
		if(Objects.isNull(frontInfo) ||Objects.isNull(frontInfo.getName()) )
		{
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "识别身份证正面失败，请正对拍摄");
		}
		//读取身份证背面信息
		OcrIdCardDTO backInfo = ocrService.readIdCardBackImg(ocrReadCardImgDTO.getIdCardBackImg());
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

		ocrReadCardImgVO = new OcrReadCardImgVO();
		ocrReadCardImgVO.setRecruitId(ocrReadCardImgDTO.getId().toString());
		ocrReadCardImgVO.setJcheName(selectById.getJcheName());
		ocrReadCardImgVO.setName(frontInfo.getName());
		ocrReadCardImgVO.setIdentification(frontInfo.getIdentityCard());
		ocrReadCardImgVO.setGender(frontInfo.getGender());
		ocrReadCardImgVO.setEthnicity(frontInfo.getEthnicity());
		ocrReadCardImgVO.setBirthday(frontInfo.getBirthday());
		ocrReadCardImgVO.setAddress(frontInfo.getAddress());
		ocrReadCardImgVO.setSignOrg(frontInfo.getSignOrg());
		ocrReadCardImgVO.setValidityDate(frontInfo.getValidityDate());

		return ocrReadCardImgVO;
	}

	@Override
	@Transactional(rollbackFor=Exception.class)
	public String saveCardInfo(OcrReadCardImgVO ocrReadCardImgVo) {
		// TODO Auto-generated method stub
		SaveWechatApplicationDTO saveApplicationDTO = new SaveWechatApplicationDTO();
		saveApplicationDTO.setName(ocrReadCardImgVo.getName());
		saveApplicationDTO.setBirth(ocrReadCardImgVo.getBirthday());
		saveApplicationDTO.setCertno(ocrReadCardImgVo.getIdentification());
		saveApplicationDTO.setCertnoPicture(ocrReadCardImgVo.getCardFrontImg());
		saveApplicationDTO.setNation(ocrReadCardImgVo.getEthnicity());
		saveApplicationDTO.setRecruitId(Integer.parseInt(ocrReadCardImgVo.getRecruitId()));
		saveApplicationDTO.setPolice(ocrReadCardImgVo.getSignOrg());
		saveApplicationDTO.setSex(SexType.code(ocrReadCardImgVo.getGender()));
		saveApplicationDTO.setHomeAddress(ocrReadCardImgVo.getAddress());
		saveApplicationDTO.setStatus(ApplicationStatusEnum.EDIT_ING.getCode());
		saveApplicationDTO.setMaritalStatus(ocrReadCardImgVo.getMaritalStatus());
		if (StringUtils.isNotBlank(ocrReadCardImgVo.getValidityDate())) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			String[] validityDateArray = ocrReadCardImgVo.getValidityDate().split("-");
			if (validityDateArray.length != 2) {
				throw new TCEException("解析身份证有效期异常");
			}

			try {
				saveApplicationDTO.setValidDateFm(dateFormat.parse(validityDateArray[0]));
				if(validityDateArray[1].indexOf("长期")>-1)
				{
					saveApplicationDTO.setValidDateChar(validityDateArray[1]);

				}else
				{
					saveApplicationDTO.setValidDate(dateFormat.parse(validityDateArray[1]));
				}
			} catch (ParseException e) {
				throw new TCEException("解析身份证有效期异常");
			}
		}
		Result result = addWechatAppliction(saveApplicationDTO);
		Object applicationId = result.getData();
		if (Objects.isNull(applicationId)) {
			log.error("remote addWechatAppliction result{}", result.getCode(), result.getMsg());
			throw new TCEException(result.getMsg());
		}
		SmtApplicationEmail selectOne = emailMapper.selectOne(Wrappers.<SmtApplicationEmail> query().lambda().eq(SmtApplicationEmail::getApplicationId, Long.parseLong(applicationId.toString())));
		if(selectOne==null)
		{
			SmtApplicationEmail em=new SmtApplicationEmail();
			em.setApplicationId(Long.parseLong(applicationId.toString()));
			em.setEmail(ocrReadCardImgVo.getEmail());
			emailMapper.insert(em);
		}
		else
		{
			selectOne.setEmail(ocrReadCardImgVo.getEmail());
			emailMapper.updateById(selectOne);
		}
		return applicationId.toString();
	}

	public Result addWechatAppliction(SaveWechatApplicationDTO saveApplicationDTO) {
		SmtApplication appliction = new SmtApplication();

		if (Objects.isNull(saveApplicationDTO)) {
			return Result.fail(CommonConstants.FAIL, "应聘信息为空");
		}

		if (Objects.isNull(saveApplicationDTO.getRecruitId())) {
			return Result.fail(CommonConstants.FAIL, "岗位ID为空");
		}

		SmtRecruitment smtRecruitment = recruitmentMapper.selectById(saveApplicationDTO.getRecruitId());
		if (Objects.isNull(smtRecruitment)) {
			return Result.fail(CommonConstants.FAIL, "未找到岗位信息");
		}

		SmtApplication queryEntityRs = getByIdCardNo(saveApplicationDTO.getCertno(), saveApplicationDTO.getRecruitId());
		if (Objects.nonNull(queryEntityRs)) {
			if (queryEntityRs.getStatus().equals(ApplicationStatusEnum.EDIT_ING.getCode())) {
				//处于编辑状态更新应聘人员的信息
				String certnoPicId = smtImageService.saveImage(saveApplicationDTO.getParkId(), saveApplicationDTO.getCertnoPicture(), SmtImageEnum.TYPE_JOB_APPLY_IDCARD_FRONT.getCode());
				queryEntityRs.setCertnoPicId(certnoPicId);
				BeanUtils.copyProperties(saveApplicationDTO, queryEntityRs);
				queryEntityRs.setRecruitId(saveApplicationDTO.getRecruitId());
				queryEntityRs.setParkId(smtRecruitment.getParkId());
				queryEntityRs.setIsDelete(DeleteStatusEnum.NOT_DELETE.getCode("否"));
				queryEntityRs.setAge(idNOToAge(saveApplicationDTO.getCertno()));
				queryEntityRs.setStatus(ApplicationStatusEnum.EDIT_ING.getCode());
				queryEntityRs.setHomeAddress(saveApplicationDTO.getHomeAddress());
				queryEntityRs.setMaritalStatus(saveApplicationDTO.getMaritalStatus());
				queryEntityRs.setPhone(saveApplicationDTO.getPhone());
				mapper.updateById(queryEntityRs);
				return new Result<>(queryEntityRs.getId());
			} else
				return Result.fail(CommonConstants.FAIL, "应聘信息已存在");
		}
		// 根据图片获取图片id
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
		appliction.setMaritalStatus(saveApplicationDTO.getMaritalStatus());
		mapper.insert(appliction);

		return new Result<>(appliction.getId());
	}

	public SmtApplication getByIdCardNo(String certno,Integer recruitId) {
		if(com.tce.smart.common.core.util.StringUtils.isNotBlank(certno) && Objects.nonNull(recruitId)){
			 @SuppressWarnings("unused")

			List<SmtApplication> list = mapper.selectList(Wrappers.<SmtApplication>query().lambda()
					.eq(SmtApplication::getCertno, certno)
					.eq(SmtApplication::getRecruitId, recruitId).orderByDesc(SmtApplication::getCreateTime));
			if(list.size()>0)
			{
				return list.get(0);
			}
			else
			{
				return null;
			}

		}
		return null;
	}

	@Override
	public Integer addFaceImg(AddJobFaceDTO addJobFaceDTO) {
		// TODO Auto-generated method stub
		SmtApplication selectById = mapper.selectById(Long.parseLong(addJobFaceDTO.getApplicationId()));
		if(ObjectUtil.isNull(selectById))
		{
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "应聘人员信息不存在");
		}

		/*String certnoPicId = selectById.getCertnoPicId();
		//获取身份证照片base64字符
		String certnoPic = blobService.getBlob(certnoPicId, SecurityConstants.FROM_IN).getData();

		FaceCompareDTO faceCompareDTO = new FaceCompareDTO();
		faceCompareDTO.setBase64Face1(addJobFaceDTO.getFacePhoto());
		faceCompareDTO.setBase64Face2(certnoPic);
		// 远程调用人证照片比对接口
		Result<?> faceCompareRs = remoteFaceService.twoFaceComparison(faceCompareDTO, SecurityConstants.FROM_IN);
		log.info("remote twoFaceComparison result=[{}]", faceCompareRs);

		if(!faceCompareRs.isSuccess()){
			throw new TCEException(faceCompareRs.getCode(), faceCompareRs.getMsg());
		}

		if (Objects.isNull(faceCompareRs)) {
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "人证比对服务异常");
		}
		if(Double.parseDouble(faceCompareRs.getData().toString())<Double.parseDouble(compareValue))
		{
			log.info("比对结果是："+faceCompareRs.getData());
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "人证比对失败，请重新拍照");
		}*/

		SaveWechatApplicationDTO saveFaceDto = new SaveWechatApplicationDTO();
		saveFaceDto.setApplicatioId(Long.parseLong(addJobFaceDTO.getApplicationId()));
		saveFaceDto.setFacePicture(addJobFaceDTO.getFacePhoto());

		//存储到数据库
		String result = smtImageService.saveImage(0,addJobFaceDTO.getFacePhoto(), SmtImageEnum.TYPE_JOB_APPLY_FACE.getCode());
		//判断是否成功
		if(StringUtils.isNotEmpty(result)) {
			selectById.setFacePicId(result);
		}else {
			throw new TCEException(ExceptionTypeEnum.VISITOR_PHOTO_ERROR);
		}
		return mapper.updateById(selectById);
	}

	//投递
	@Override
	public Result deliveryThird(long applicationId ) {
		// TODO Auto-generated method stub
		SmtApplication selectById = mapper.selectById(applicationId);
		if(selectById.getStatus().equals(ApplicationStatusEnum.DELIVER_DONE.getCode()))
		{
			return new Result<>(true);
		}
		//TODO 这里的设置状态和备注不一致 业务测试时再根据业务修改
		selectById.setStatus(ApplicationStatusEnum.ENTRY_TO_DO.getCode());
		selectById.setApplyDate(DateUtils.date());
		selectById.setInterviewTime(DateUtils.date());
		//更新为投递状态
		int updateById = mapper.updateById(selectById);
		if(updateById>0)
		{
			register(selectById);
			addApplicationProcess(ApplicationStatusEnum.ENTRY_TO_DO.getCode(),applicationId,selectById.getParkId(),selectById.getName(),null);
		}
		return new Result<>(true);
	}

	private void register(SmtApplication application)
	{
		//面试时间开始后一小时
		Long maxTime = DateUtils.offsetHour(application.getInterviewTime(), +1).getTime()/1000;
		//面试时间开始前一小时
		Long startTime = DateUtils.offsetHour(application.getInterviewTime(), -1).getTime()/1000;
		List<SmtDeviceAuthorityRelation> selectList = smtDeviceAuthorityRelationService
				.getRelationAuth(application.getParkId(), BusinessAuthorityEnum.STAFF_FACE.getCode(), DeviceAuthorityEnum.STAFF);
		log.info("DeviceAuthorityListCount:"+selectList.size());
		DeviceTaskVO deviceTaskVO = null;
	    for (int i = 0; i < selectList.size(); i++) {
		deviceTaskVO = new DeviceTaskVO();
		deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
			deviceTaskVO.setImageId(application.getFacePicId());
			deviceTaskVO.setCardType(SmtVisitorEnum.CARD_TYPE_1.getType());
			deviceTaskVO.setGeneral(application.getName());
			deviceTaskVO.setDeviceCode(selectList.get(i).getDeviceId());
			deviceTaskVO.setCardNo(application.getId().toString());
			deviceTaskVO.setStatus(DeviceTaskConstants.FAIL);
		deviceTaskVO.setDeviceType(DeviceTaskConstants.CARD);
		deviceTaskVO.setStartTime(startTime);
		deviceTaskVO.setOverTime(maxTime);
		smtDeviceTaskService.saveTask(deviceTaskVO);
	    }
	}

/*	private CardDTO saveCardInfo(SmtApplication application, Integer type, String facePicId, String deviceId) {
		// TODO Auto-generated method stub

		CardDTO cardInfo = new CardDTO();
		cardInfo.setCardNo(application.getId().toString());
		cardInfo.setCardType(type);
		cardInfo.setFaceImage(application.getFacePicId());
		//通行前后一小时
		CardValid validTime = new CardValid();
		validTime.setStartTime(DateUtils.offsetHour(application.getInterviewTime(), -1).getTime()/1000);
		validTime.setEndTime( DateUtils.offsetHour(application.getInterviewTime(), +1).getTime()/1000);
		cardInfo.setValidTime(validTime);
		cardInfo.setDeviceCode(deviceId);
		cardInfo.setPersonName(application.getName());
		return cardInfo;
	}*/

	/**
	 * 添加应聘流程表
	 * @param status 应聘桩体
	 * @param applicationId 应聘id
	 * @param parkId 园区id
	 * @param refuse
	 * @return
	 */
	public Result addApplicationProcess(Integer status,Long applicationId,Integer parkId,String user, String refuse)
	{

		SmtApplicationProcess process=new SmtApplicationProcess();
		//默认投递
		process.setStatus(status);
		process.setApplicationId(applicationId);
		process.setCreateTime(DateUtil.date());
		process.setCreateUserName(user); //从缓存中获取
		process.setRemark(refuse);
		return new Result<>(processMapper.insert(process));

	}


	@Override
	public List<DicContentVO> getEducationType() {
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.EDUCATION_TYPE, SecurityConstants.FROM_IN);

		List<DicContentVO> typeList = new ArrayList<DicContentVO>();
		DicContentVO type = null;
		if (findByType.getData().size() > 0) {
			for (int j = 0; j < findByType.getData().size(); j++) {
				type = new DicContentVO();
				type.setTypeCode(findByType.getData().get(j).getValue());
				type.setTypeName(findByType.getData().get(j).getLabel());
				typeList.add(type);
			}
		}
		return typeList;
	}

	@Override
	public List<DicContentVO> getDegreeType() {
		// TODO Auto-generated method stub
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.DEGREE_TYPE, SecurityConstants.FROM_IN);

		List<DicContentVO> typeList = new ArrayList<DicContentVO>();
		DicContentVO type = null;
		if (findByType.getData().size() > 0) {
			for (int j = 0; j < findByType.getData().size(); j++) {
				type = new DicContentVO();
				type.setTypeCode(findByType.getData().get(j).getValue());
				type.setTypeName(findByType.getData().get(j).getLabel());
				typeList.add(type);
			}
		}
		return typeList;
	}

	@Override
	public List<DicContentVO> relationList() {
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN);
		List<DicContentVO> relationList=new ArrayList<DicContentVO>();
		if(findByType.getData().size()>0) {
			for (int j = 0; j < findByType.getData().size(); j++) {
				DicContentVO re = new DicContentVO();
				re.setTypeCode(findByType.getData().get(j).getValue());
				re.setTypeName(findByType.getData().get(j).getLabel());
				relationList.add(re);
			}
		}
		return relationList;
	}


	@Override
	public Boolean bindMobile(VerifySmsCodeDTO verifySmsCodeAo) {

		//校验短信验证码
		String redisKey = RedisKeyConstants.SMAT_APP_WECHAT_SMSCODE + verifySmsCodeAo.getMobile();
		String smsCodeObject = stringRedisTemplate.opsForValue().get(redisKey);
		if (StringUtils.isEmpty(smsCodeObject)) {
			log.error("获取短信验证码异常");
			throw new TCEException("短信码已失效");
		}

		JSONObject smsCodeJson = (JSONObject) JSONUtil.parse(smsCodeObject);
		if (!smsCodeJson.get("smsCode").equals(verifySmsCodeAo.getSmsCode())) {
			throw new TCEException("短信码无效");
		}
		SmtApplication selectById = mapper.selectById(Long.parseLong(verifySmsCodeAo.getApplicationId()));
		if(selectById==null)
		{
			throw new TCEException("应聘人员不存在");
		}
		selectById.setPhone(verifySmsCodeAo.getMobile());
		mapper.updateById(selectById);
		return Boolean.TRUE;
	}

	@Override
	public Boolean sendSmsCode(String mobile) {

		String smsCode = RandomUtil.randomNumbers(MESSAGE_LENGTH);// 短信验证码
		Map<String, Object> smsCodeMap = new HashMap<>();
		smsCodeMap.put("smsCode", smsCode);
		String redisKey = RedisKeyConstants.SMAT_APP_WECHAT_SMSCODE + mobile;
		stringRedisTemplate.opsForValue().set(redisKey, JSONUtil.toJsonStr(smsCodeMap), 600, TimeUnit.SECONDS);// 10分钟失效
		SendSmsCodeMsgReqDTO codeMsg = new SendSmsCodeMsgReqDTO();
		codeMsg.setName(mobile);
		codeMsg.setNumber(mobile);
		codeMsg.setSmsCode(smsCode);
		codeMsg.setTempCode(SmsTemplateEnum.SMSCODE_4001.getCode());
		log.debug("发送短信验证码，手机号：{}, 验证码：{}", mobile, smsCode);
		// 调用feign接口发送短信验证码
		Result result = remoteSmsManageService.sendSmsCode(codeMsg);
		if (!result.isSuccess()) {
			String errorMsg = result.getMsg();

			log.debug("发送短信验证码失败，手机号：{},", mobile);
			throw new TCEException(result.getCode(), errorMsg);
		}

		log.debug("发送短信验证码成功，手机号：{}, 验证码：{}", mobile, smsCode);
		return Boolean.TRUE;

	}

	public  Integer idNOToAge(String idNo){
		 String birthTimeString = idNo.substring(6,10)+"-"+idNo.substring(10,12)+"-"+idNo.substring(12,14);
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
	public Result saveEducationThird(ApplicationEducationDTO applicationEducationDTO) {
		// TODO Auto-generated method stub
		if(ObjectUtil.isNull(applicationEducationDTO.getApplicationId()))
		{
			throw new TCEException("应聘人员唯一标识不能为空");
		}
		SmtApplication selectById = mapper.selectById(Long.parseLong(applicationEducationDTO.getApplicationId()));
		if(ObjectUtil.isNull(selectById))
		{
			throw new TCEException("应聘人员不存在");
		}


		List<SmtApplicationEducation> eduList= educationService.list(Wrappers.<SmtApplicationEducation> query().lambda().eq(SmtApplicationEducation::getApplicationId,Long.parseLong(applicationEducationDTO.getApplicationId())));
		for (SmtApplicationEducation smtApplicationEducation : eduList) {
			if(smtApplicationEducation.getIsHighDegreeType().equals(1) && applicationEducationDTO.getIsHighDegreeType().equals(1))
			{
				throw new TCEException("最高学位已存在");
			}


			if(smtApplicationEducation.getIsHighEduType().equals(1) && applicationEducationDTO.getIsHighEduType().equals(1))
			{
				throw new TCEException("最高学历已存在");
			}
		}

		//int delete = educationMapper.delete(Wrappers.<SmtApplicationEducation> query().lambda().eq(SmtApplicationEducation::getApplicationId,Long.parseLong(applicationEducationDTO.getApplicationId())));
		SmtApplicationEducation edu=new SmtApplicationEducation();
		edu.setApplicationId(Long.parseLong(applicationEducationDTO.getApplicationId()));
		edu.setStartTime(applicationEducationDTO.getStartTime());
		edu.setEndTime(applicationEducationDTO.getEndTime());
		edu.setSchoolName(applicationEducationDTO.getSchoolName());
		edu.setEducation(applicationEducationDTO.getEducation());
		edu.setDegree(applicationEducationDTO.getDegree());
		edu.setMajor(applicationEducationDTO.getMajor());
		edu.setGradType(applicationEducationDTO.getGradType());
		edu.setIsHighDegreeType(applicationEducationDTO.getIsHighDegreeType());
		edu.setIsHighEduType(applicationEducationDTO.getIsHighEduType());
		return new Result<>(edu.insert());
	}

	@Override
	public Result saveApplicationEmergencyThird(ApplicationEmergencyDTO emergencyDTO) {
		// TODO Auto-generated method stub

		if(ObjectUtil.isNull(emergencyDTO.getApplicationId()))
		{
			throw new TCEException("应聘人员唯一标识不能为空");
		}
		SmtApplication selectById = mapper.selectById(Long.parseLong(emergencyDTO.getApplicationId()));
		if(ObjectUtil.isNull(selectById))
		{
			throw new TCEException("应聘人员不存在");
		}
		SmtApplicationEmergency select = emergencyMapper.selectOne(Wrappers.<SmtApplicationEmergency>query().lambda().eq(SmtApplicationEmergency::getApplicationId,Long.parseLong(emergencyDTO.getApplicationId() )));
		if(select!=null)
		{
			select.setEmergencyName(emergencyDTO.getEmergencyName());
			select.setRelation(emergencyDTO.getRelation());
			select.setTelephont(emergencyDTO.getPhone());
			return new Result<>(emergencyMapper.updateById(select));
		}else
		{
			SmtApplicationEmergency emergency=new SmtApplicationEmergency();
			emergency.setApplicationId(Long.parseLong(emergencyDTO.getApplicationId()));
			emergency.setEmergencyName(emergencyDTO.getEmergencyName());
			emergency.setRelation(emergencyDTO.getRelation());
			emergency.setTelephont(emergencyDTO.getPhone());
			return new Result<>(emergencyMapper.insert(emergency));

		}

	}

	@Override
	public Result saveThird(ApplicationFamilyDTO applicationFamilyDTO) {
		// TODO Auto-generated method stub

		if(ObjectUtil.isNull(applicationFamilyDTO.getApplicationId()))
		{
			throw new TCEException("应聘人员唯一标识不能为空");
		}
		SmtApplication selectById = mapper.selectById(Long.parseLong(applicationFamilyDTO.getApplicationId()));
		if(ObjectUtil.isNull(selectById))
		{
			throw new TCEException("应聘人员不存在");
		}
		//int delete = familyMapper.delete(Wrappers.<SmtApplicationFamily> query().lambda().eq(SmtApplicationFamily::getApplicationId, Long.parseLong(applicationFamilyDTO.getApplicationId())));
		SmtApplicationFamily fa=new SmtApplicationFamily();
		fa.setApplicationId(Long.parseLong(applicationFamilyDTO.getApplicationId()));
		fa.setCompany(applicationFamilyDTO.getCompany());
		fa.setBirth(applicationFamilyDTO.getBirth());
		fa.setCompany(applicationFamilyDTO.getCompany());
		fa.setJob(applicationFamilyDTO.getJob());
		fa.setName(applicationFamilyDTO.getName());
		fa.setPhone(applicationFamilyDTO.getPhone());
		fa.setRelation(applicationFamilyDTO.getRelation());
		fa.setSex(applicationFamilyDTO.getSex());
		return new Result<>(familyMapper.insert(fa));
	}


	@Override
	public Result addApplicationWorkThird(ApplicationWorkDTO applicationWorkDTO) {
		// TODO Auto-generated method stub
		SmtApplicationWork smtApplicationWork=new SmtApplicationWork();


		if(ObjectUtil.isNull(applicationWorkDTO.getApplicationId()))
		{
			throw new TCEException("应聘人员唯一标识不能为空");
		}
		SmtApplication selectById = mapper.selectById(Long.parseLong(applicationWorkDTO.getApplicationId()));
		if(ObjectUtil.isNull(selectById))
		{
			throw new TCEException("应聘人员不存在");
		}
		// workMapper.delete(Wrappers.<SmtApplicationWork> query().lambda().eq(SmtApplicationWork::getApplicationId, Long.parseLong(applicationWorkDTO.getApplicationId())));
		 smtApplicationWork.setApplicationId( Long.parseLong(applicationWorkDTO.getApplicationId()));
		 smtApplicationWork.setCompany(applicationWorkDTO.getCompany());
		 smtApplicationWork.setEndTime(applicationWorkDTO.getEndTime());
		 smtApplicationWork.setStartTime(applicationWorkDTO.getStartTime());
		 smtApplicationWork.setJobName(applicationWorkDTO.getJobName());
		 smtApplicationWork.setPersonLiable(applicationWorkDTO.getPersonLiable());
		 smtApplicationWork.setPhone(applicationWorkDTO.getPhone());
		 return new Result<>(smtApplicationWork.insert());
	}

	@Override
	public Result updateThird(ApplicationFamilyDTO applicationFamilyDTO) {
		// TODO Auto-generated method stub


		SmtApplicationFamily fa = familyMapper.selectById(applicationFamilyDTO.getId());
		fa.setCompany(applicationFamilyDTO.getCompany());
		fa.setBirth(applicationFamilyDTO.getBirth());
		fa.setCompany(applicationFamilyDTO.getCompany());
		fa.setJob(applicationFamilyDTO.getJob());
		fa.setName(applicationFamilyDTO.getName());
		fa.setPhone(applicationFamilyDTO.getPhone());
		fa.setRelation(applicationFamilyDTO.getRelation());
		fa.setSex(applicationFamilyDTO.getSex());
		return new Result<>(familyMapper.updateById(fa));
	}

	@Override
	public Result updateApplicationWorkThird(ApplicationWorkDTO applicationWorkDTO) {
		// TODO Auto-generated method stub

		SmtApplicationWork smtApplicationWork=workMapper.selectById(applicationWorkDTO.getId());
		 smtApplicationWork.setCompany(applicationWorkDTO.getCompany());
		 smtApplicationWork.setEndTime(applicationWorkDTO.getEndTime());
		 smtApplicationWork.setStartTime(applicationWorkDTO.getStartTime());
		 smtApplicationWork.setJobName(applicationWorkDTO.getJobName());
		 smtApplicationWork.setPersonLiable(applicationWorkDTO.getPersonLiable());
		 smtApplicationWork.setPhone(applicationWorkDTO.getPhone());
		 return new Result<>(smtApplicationWork.updateById());
	}

	@Override
	public Result updateApplicationeEducation(ApplicationEducationDTO applicationEducationDTO) {
		// TODO Auto-generated method stub


		List<SmtApplicationEducation> eduList= educationService.list(Wrappers.<SmtApplicationEducation> query().lambda().eq(SmtApplicationEducation::getApplicationId,Long.parseLong(applicationEducationDTO.getApplicationId())));
		for (SmtApplicationEducation smtApplicationEducation : eduList) {
			if(smtApplicationEducation.getIsHighDegreeType().equals(1) && applicationEducationDTO.getIsHighDegreeType().equals(1))
			{
				if(!smtApplicationEducation.getId().equals(applicationEducationDTO.getId())){
					throw new TCEException("最高学位已存在");
				}
			}

			if(smtApplicationEducation.getIsHighEduType().equals(1) && applicationEducationDTO.getIsHighEduType().equals(1))
			{
				if(!smtApplicationEducation.getId().equals(applicationEducationDTO.getId())){
					throw new TCEException("最高学历已存在");
				}
			}
		}

		SmtApplicationEducation edu= educationService.getById(applicationEducationDTO.getId());
		edu.setStartTime(applicationEducationDTO.getStartTime());
		edu.setEndTime(applicationEducationDTO.getEndTime());
		edu.setSchoolName(applicationEducationDTO.getSchoolName());
		edu.setEducation(applicationEducationDTO.getEducation());
		edu.setDegree(applicationEducationDTO.getDegree());
		edu.setMajor(applicationEducationDTO.getMajor());
		edu.setGradType(applicationEducationDTO.getGradType());
		edu.setIsHighDegreeType(applicationEducationDTO.getIsHighDegreeType());
		edu.setIsHighEduType(applicationEducationDTO.getIsHighEduType());
		return new Result<>(edu.updateById());
	}

	@Override
	public Result getByApplicationIdThird(String applicationId) {
		// TODO Auto-generated method stub

		SmtApplicationEmergency selectOne = emergencyMapper.selectOne(Wrappers.<SmtApplicationEmergency> query().lambda().eq(SmtApplicationEmergency::getApplicationId, Long.parseLong(applicationId)));

		if(selectOne!=null) {
			ApplicationEmergencyDTO dto=new ApplicationEmergencyDTO();
			dto.setApplicationId(applicationId);
			dto.setApplicationId(selectOne.getApplicationId().toString());
			dto.setEmergencyName(selectOne.getEmergencyName());
			dto.setPhone(selectOne.getTelephont());
			dto.setRelation(selectOne.getRelation());
			dto.setId(selectOne.getId());
			return new Result<>(dto);
		}
		return new Result<>();

	}

	@Override
	public List<DicContentVO> emergencyRelationList() {
		// TODO Auto-generated method stub
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.EMERGENCY_REALTION__TYPE, SecurityConstants.FROM_IN);

		List<DicContentVO> typeList = new ArrayList<DicContentVO>();
		DicContentVO type = null;
		if (findByType.getData().size() > 0) {
			for (int j = 0; j < findByType.getData().size(); j++) {
				type = new DicContentVO();
				type.setTypeCode(findByType.getData().get(j).getValue());
				type.setTypeName(findByType.getData().get(j).getLabel());
				typeList.add(type);
			}
		}
		return typeList;
	}
}
