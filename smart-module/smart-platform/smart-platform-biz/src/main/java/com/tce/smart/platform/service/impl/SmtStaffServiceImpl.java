package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.dto.UserDTO;
import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.admin.api.feign.RemoteUserService;
import com.tce.smart.algorithm.api.dto.req.CompareDTO;
import com.tce.smart.algorithm.api.dto.req.CompareImageDTO;
import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.api.enums.FaceTypeEnum;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.app.api.dto.AddIdCollectDto;
import com.tce.smart.app.api.entity.AppUserDevice;
import com.tce.smart.app.api.feign.RemoteAppDeviceService;
import com.tce.smart.app.api.feign.RemoteAppPerfectService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.LoginResult;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.HttpUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.consume.req.RsEmpSaveReqDto;
import com.tce.smart.data.api.dto.consume.req.UpdateHeadImageReqDTO;
import com.tce.smart.data.api.dto.ehrview.EvwCcdFlstandardDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsjobRespDTO;
import com.tce.smart.data.api.dto.temporary.req.SaveEPhotoReqDTO;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpPhotoService;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwCcdFlstandardService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsjobService;
import com.tce.smart.data.api.feign.temporary.RemoteEPhotoService;
import com.tce.smart.data.api.feign.temporary.RemoteEstaffRegisterService;
import com.tce.smart.data.api.feign.xcc6.RemoteXCRsEmpService;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.FaceSnapDTO;
import com.tce.smart.platform.api.dto.SmtStaffDTO;
import com.tce.smart.platform.api.dto.req.EmpHrReqDTO;
import com.tce.smart.platform.api.dto.req.TempStaffEditReqDTO;
import com.tce.smart.platform.api.dto.resp.DormitoryRoomDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffAccountRespDTO;
import com.tce.smart.platform.api.dto.resp.StaffLookupRespDTO;
import com.tce.smart.platform.api.dto.resp.StaffPartInfo;
import com.tce.smart.platform.api.dto.resp.StaffSelfCheckInProfileRespDTO;
import com.tce.smart.platform.core.ao.SmtAppStaffAuthSaveAO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.ext.SecurityPersonRelationExt;
import com.tce.smart.platform.core.enums.DeviceSyncEnum;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.service.*;
import com.tce.smart.platform.core.vo.*;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.remoteLock.ConnectLockService;
import com.tce.smart.platform.utils.StaffUtil;
import com.tce.smart.tool.constant.*;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.*;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 员工表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:42
 */
@Service
@Slf4j
public class SmtStaffServiceImpl extends ServiceImpl<SmtStaffMapper, SmtStaff> implements SmtStaffService {
	@Autowired
	private SmtApplicationService applicationService;
	@Autowired
	private SmtSecurityBuService smtSecurityBuService;
	@Autowired
	private SmtAppAuthService smtAppAuthService;
	@Autowired
	private SmtRecruitmentService recruitmentService;
	@Autowired
	private SmtStaffEmergencyService staffEmergencyService;
	@Autowired
	private SmtStaffWorkService smtStaffWorkService;
	@Autowired
	private SmtStaffEducationService smtStaffEducationService;
	@Autowired
	private SmtStaffFamilyService smtStaffFamilyService;
	@Autowired
	private ConnectLockService connectLockService;
	@Autowired
	private SmtStaffRelationService smtStaffRelationService;
	@Autowired
	private SmtOutDormitoryStaffMapper outDormitoryStaffMapper;
	@Autowired
	private SmtVehicleStaffService vsService;
	@Autowired
	private SmtExternalDeptService smtExternalDeptService;
	@Autowired
	private SmtVehicleService smtVehicleService;
	@Autowired
	private RemoteDictService remoteDictService;
	@Autowired
	private SmtDormitoryRoomMapper roomMapper;
	@Autowired
	private RemoteEstaffRegisterService staffRegister;
	@Autowired
	private ImageService imageService;
	@Autowired
	private SmtImageService smtImageService;

	@Autowired
	private SmtDeviceTaskService smtDeviceTaskService;

	@Autowired
	private SmtStaffDeviceAuthService smtStaffDeviceAuthService;

	@Autowired
	private SmtAppStaffAuthService appStaffAuthService;

	@Autowired
	private SmtApplicationEmailService emailService;
	@Autowired
	private SmtOrganizeRelationService smtOrganizeRelationService;

	@Autowired
	private SmtOrganizeRelationMapper smtOrganizeRelationMapper;

	@Autowired
	private RemoteAppDeviceService remoteAppDeviceService;

	@Autowired
	private SmtParkService smtParkService;

	@Autowired
	private RemoteOvwYsjobService remoteOvwYsjobService;

	@Autowired
	private RemoteUserService remoteUserService;

	@Autowired
	private RemoteRsEmpService remoteRsEmpService;

	@Autowired
	private RemoteXCRsEmpService remoteXCRsEmpService;

	@Autowired
	private RemoteRsEmpPhotoService remoteRsEmpPhotoService;

	@Autowired
	private RemoteAppPerfectService remoteAppPerfectService;

	@Autowired
	private RemoteEPhotoService remoteEPhotoService;

	@Autowired
	private SmtVehicleStaffService smtVehicleStaffService;

	@Autowired
	private SmtParkBuService smtParkBuService;

	@Autowired
	private SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;

	@Autowired
	private SmtDeviceAuthorityService smtDeviceAuthorityService;

	@Autowired
	private SmtStaffPhotoUploadRecordService staffPhotoUploadRecordService;

	@Autowired
	private RemoteDispatcherService remoteDispatcherService;

	@Autowired
	private RemoteAlgorithmService remoteAlgorithmService;

	@Autowired
	private SmtBusinessDeviceAuthService smtBusinessDeviceAuthService;

	@Autowired
	private SmtTaskDownRecordService smtTaskDownRecordService;

	@Autowired
	private SmtIscDownRecordService smtIscDownRecordService;

	@Autowired
	private SmtDeviceService smtDeviceService;

	@Autowired
	private RemoteEvwCcdFlstandardService remoteEvwCcdFlstandardService;

	@Autowired
	private SmtPreStaffMapper smtPreStaffMapper;

	@Autowired
	private SmtDormitoryApplyService smtDormitoryApplyService;

	@Lazy
	@Autowired
	private SmtDormitoryStaffService smtDormitoryStaffService;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private SmtStaffExtService smtStaffExtService;

	@Autowired
	private SmtParkVehicleLevelService smtParkVehicleLevelService;

	@Autowired
	private SmtJcheAuthService smtJcheAuthService;

	@Autowired
	private ISCPersonService iscPersonService;

	@Autowired
	private SmtIscStaffCardService smtIscStaffCardService;
	/**
	 * 人脸登陆比对阀值
	 */
	@Value("${spring.face.login-compare-value}")
	private String loginCompareValue;
	/**
	 * 人脸登陆比对阀值
	 */
	@Value("${spring.temp.auth}")
	private String tempAppAuth;

	@Value("${spring.yuto-secsytem.phone.update-url}")
	private String phoneUpdateUrl;

	@Value("${spring.yuto-secsytem.phone.update-token}")
	private String updateToken;

	@Value("${smart.sy-park-id:0}")
	private Integer syParkId;

	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final String key = "syncDhrImgTime";

	/**
	 * 添加员工
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result addStaff(ApplicationStaffDTO smtStaffReq) {
		// TODO Auto-generated method stub

		//从应聘和招聘信息中同步
		SmtPreStaff smtPreStaff = new SmtPreStaff();
		Long applicationId = smtStaffReq.getApplicationId();
		SmtApplication application = applicationService.getOne(Wrappers.<SmtApplication>query().lambda().eq(SmtApplication::getId, applicationId));
		SmtRecruitment recruitment = recruitmentService.getOne(Wrappers.<SmtRecruitment>query().lambda().eq(SmtRecruitment::getId, application.getRecruitId()));
		//获取工作邮箱
		SmtApplicationEmail email = emailService.getOne(Wrappers.<SmtApplicationEmail>query().lambda().eq(SmtApplicationEmail::getApplicationId, applicationId));
		if (email != null) {
			smtPreStaff.setEmail(email.getEmail());
		}
		smtPreStaff.setName(application.getName());
		String badge = "";
		//获取comp的简称
		try {
			badge = smtStaffExtService.createNewBadge(recruitment.getCompId());
		} catch (Exception e) {
			throw new TCEException("员工号生成异常");
		}
		smtPreStaff.setBadge(badge);
		//smtPreStaff.setApplicationId(applicationId);
		smtPreStaff.setJobId(recruitment.getJobId());
		smtPreStaff.setJobName(recruitment.getJobName());
		smtPreStaff.setCompId(recruitment.getCompId());
		smtPreStaff.setCompName(recruitment.getCompName());
		smtPreStaff.setDepId(recruitment.getDepId());
		smtPreStaff.setDepName(recruitment.getDepName());
		smtPreStaff.setJcheId(recruitment.getJcheId());
		smtPreStaff.setJcheName(recruitment.getJcheName());
		smtPreStaff.setWelfareLevel(recruitment.getWelfareLevel());
		smtPreStaff.setCertno(application.getCertno());
		smtPreStaff.setSex(application.getSex());
		smtPreStaff.setAge(application.getAge());
		smtPreStaff.setNation(application.getNation());
		smtPreStaff.setBirth(application.getBirth());
		smtPreStaff.setPhone(application.getPhone());
		smtPreStaff.setWechat(application.getWechat());
		smtPreStaff.setHomeAddress(application.getHomeAddress());
		smtPreStaff.setLiveAddress(application.getLiveAddress());
		smtPreStaff.setCertnoPicId(application.getCertnoPicId());
		smtPreStaff.setFacePicId(application.getFacePicId());
		smtPreStaff.setStatus(1); //0-离职 1-入职
		//smtPreStaff.setEmpType(EmpTypeEnum.TYPE1.getCode());
		smtPreStaff.setDormitoryStatus(0);//0-未住宿
		smtPreStaff.setCreateTime(DateUtil.date()); ////获取当前详细时间
		smtPreStaff.setPolice(application.getPolice());
		smtPreStaff.setValidDate(application.getValidDate());
		smtPreStaff.setValidDateFm(application.getValidDateFm());
		boolean insert = smtPreStaff.insert();


		if (insert) {
			log.info("新增员工成功:员工号:{},姓名{}", smtPreStaff.getBadge(), smtPreStaff.getName());
			if (ObjectUtil.isNull(smtPreStaff.getSeqId())) {
				smtPreStaff = smtPreStaffMapper.selectById(smtPreStaff.getId());
			}
			log.info("============new query smtPreStaff===========:" + smtPreStaff);
			if (ObjectUtil.isNull(smtPreStaff.getSeqId())) {
				Integer value = Integer.parseInt(smtPreStaff.getId().toString());
				smtPreStaff.setSeqId(value);
			}
			log.info("============new query smtPreStaff===========:" + smtPreStaff);
			//新入职员工人脸图片存到es
			saveStaffFace(smtPreStaff);
			//根据应聘ID查询园区ID
			SmtApplication smtApplication = applicationService.getSimpleInfo(smtStaffReq.getApplicationId());
			//同步数据到HR系统表中
			smtStaffExtService.addStaffToHR(smtPreStaff, application.getId(), recruitment.getLocal());
			//同步工作经验
			smtStaffExtService.addStaffWork(applicationId, smtPreStaff);
			//同步教育经验
			smtStaffExtService.addStaffEducation(applicationId, smtPreStaff);
			//同步家庭成员
			smtStaffExtService.addStaffFamily(smtPreStaff, applicationId);
			//同步人事关系
			smtStaffExtService.addStaffRelation(smtPreStaff, applicationId);
			//入职人员下发闸机
//            if(Objects.nonNull(smtStaff.getFacePicId())) {
//                addDeviceTask(smtStaff);
//            }
//            //添加员工默认app权限
//            appStaffAuthService.inintStaffAuth(smtStaff.getId());
		}
		return new Result<>(insert);
	}


	@Override
	public Result addStaffToHR(ApplicationStaffDTO smtStaffReq) {
		return smtStaffExtService.addStaffToHR(smtStaffReq);
	}

	/**
	 * 新入职员工将人脸图片存到es
	 *
	 * @param
	 */
	private void saveStaffFace(SmtPreStaff smtPreStaff) {
		// TODO Auto-generated method stub
		FaceSnapDTO faceInfo = new FaceSnapDTO();
		faceInfo.setIdentityCard(smtPreStaff.getCertno());
		faceInfo.setName(smtPreStaff.getName());
		faceInfo.setParkId(smtPreStaff.getParkId());
		faceInfo.setParkName("");
		faceInfo.setSex(smtPreStaff.getSex());
		faceInfo.setType(FaceSnapTypeEnum.STAFF.getCode());
		faceInfo.setPersonId(smtPreStaff.getId());
		faceInfo.setFaceId(smtPreStaff.getFacePicId());

		//TODO 目前不再操作ES 下面的代码先注释掉
		// //往Es里建索引
		// JSONObject jsonStr = JSONUtil.parseObj(faceInfo);
		// log.info("remote remoteFaceService.faceStorage param=[{}]",jsonStr.toString());
		// Result<FaceStorageResultDTO> faceStorage = remoteFaceService.faceStorage(faceInfo, SecurityConstants.FROM_IN);
		// log.info("remote remoteFaceService.faceStorage result=[{}]", faceStorage);
	}

	/**
	 * 我的宿舍
	 */
	@Override
	public MyDormitoryVO myDormitory(SmtStaff smtStaff) {
		SmtStaff selectOne = this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, smtStaff.getBadge()));
		if (Objects.isNull(selectOne)) {
			throw new TCEException("未找到员工信息");
		}
		MyDormitoryVO myDormitory = this.baseMapper.myDormitory(smtStaff);
		if (!Objects.isNull(myDormitory)) {
			SmtDormitoryStaff ds = new SmtDormitoryStaff();
			// 查询我的宿舍里，占用了多少个床位（parkid,dormitoryId,floorId,roomId）
			Integer usedCount = ds.selectCount(
					Wrappers.<SmtDormitoryStaff>query().lambda()
							.eq(SmtDormitoryStaff::getRoomId, myDormitory.getRoomId())
							.eq(SmtDormitoryStaff::getFloorId, myDormitory.getFloorId())
							.eq(SmtDormitoryStaff::getDormitoryId, myDormitory.getDormitoryId())
							.eq(SmtDormitoryStaff::getParkId, myDormitory.getParkId()));
			myDormitory.setUsedNum(usedCount);
			SmtDormitoryRoom selectByIdRoom = roomMapper.selectById(myDormitory.getRoomId());
			myDormitory.setBedToal(selectByIdRoom.getBedTotal());
		}
		return myDormitory;
	}


	/**
	 * 查询员工信息列表
	 */
	@Override
	public IPage<StaffListVO> getSmtStaffPage(Page page, SearchStaffDTO smtStaff) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (StrUtil.isNotEmpty(smtStaff.getBadges())) {
			smtStaff.setBadgeList(ToolUtils.splitBlankString(smtStaff.getBadges()));
		}
		IPage<StaffListVO> smtStaffPage = this.baseMapper.getSmtStaffPage(page, smtStaff, parkIdList);
		List<StaffListVO> records = smtStaffPage.getRecords();
		Set<String> compIds = records.stream().map(StaffListVO::getCompId).collect(Collectors.toSet());
		List<Long> compIdList = compIds.stream().map(Long::parseLong).collect(Collectors.toList());
		List<SmtOrganizeRelation> organizeRelations = smtOrganizeRelationService.list(new LambdaQueryWrapper<SmtOrganizeRelation>()
				.in(compIdList.size() > 0, SmtOrganizeRelation::getId, compIdList)
		);
		Map<Long, List<SmtOrganizeRelation>> orMaop = new HashMap<>();
		Map<Integer, List<SmtPark>> parkMap = new HashMap<>();
		if (CollectionUtil.isNotEmpty(organizeRelations)) {
			orMaop = organizeRelations.stream().collect(Collectors.groupingBy(SmtOrganizeRelation::getId));
			Set<Integer> parkIds = organizeRelations.stream().map(SmtOrganizeRelation::getParkId).collect(Collectors.toSet());
			List<SmtPark> smtParks = smtParkService.list(new LambdaQueryWrapper<SmtPark>().in(SmtPark::getId, parkIds));
			parkMap = smtParks.stream().collect(Collectors.groupingBy(SmtPark::getId));
		}

		for (StaffListVO staffListVO : records) {
			String staffId = staffListVO.getId();
			// 获取员工通关权限
			List<SmtDeviceAuthority> authList = smtDeviceAuthorityService.getByStaffId(staffId);
			if (CollectionUtil.isNotEmpty(authList)) {
				staffListVO.setDeviceAuth(authList.stream().map(SmtDeviceAuthority::getAuthorityName).collect(Collectors.joining(",")));
			}
			// 获取员工app权限
			List<SmtAppAuth> staffAppAuth = this.baseMapper.getStaffAppAuth(Long.parseLong(staffId));
			String auth = "";
			for (SmtAppAuth smtAppAuth : staffAppAuth) {
				auth += smtAppAuth.getAuthName() + ",";
			}

			if (!"".equals(auth)) {
				auth = auth.substring(0, auth.length() - 1);
			}
			staffListVO.setAppAuth(auth);
			List<SmtPark> parkList = new ArrayList<>();
			String parkName = "";
/*            if(staffListVO.getCompId().length() > 16) {
                Integer park = smtOrganizeRelationService.getByBu(Long.parseLong(staffListVO.getCompId())).getParkId();
                parkName = smtParkService.getById(park).getParkName();
            }else {*/
			parkList = smtParkBuService.getParkListByBu(Long.parseLong(staffListVO.getCompId()));
			for (SmtPark smtPark : parkList) {
				parkName += smtPark.getParkName() + ",";
			}

			if (!"".equals(parkName)) {
				parkName = parkName.substring(0, parkName.length() - 1);
			}
			if (StringUtils.isEmpty(parkName) && orMaop.size() > 0 && parkMap.size() > 0) {
				//可能是临时人员
				Integer parkId = orMaop.get(Long.parseLong(staffListVO.getCompId())).get(0).getParkId();
				parkName = parkMap.get(parkId).get(0).getParkName();
			}
			staffListVO.setParkName(parkName);
		}
		return smtStaffPage;
	}

	@Override
	public IPage<SecurityAllStaffListDTO> getStaffPage(Page page, SecurityPersonRelationExt reqDTO) {
		return this.baseMapper.getStaffPage(page, reqDTO);
	}

	/**
	 * 获取员工的详细信息
	 */
	@Override
	public Result getSmtStaffInfoById(String id) {
		StaffInfoVO staffInfoVO = new StaffInfoVO();
		SmtStaff selectById = this.baseMapper.selectById(id);
		staffInfoVO.setSmtStaff(selectById);
		//根据图片id去获取图片base64
		try {
			if (!Objects.isNull(selectById.getFacePicId())) {
				String facePicUrl = imageService.buildImageUrl(selectById.getFacePicId());
				staffInfoVO.setFacePic(facePicUrl);
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.info("获取图片异常");
		}
		List<SmtPark> parkList = smtParkBuService.getParkListByBu(Long.parseLong(selectById.getCompId()));
		String parkName = "";
		for (SmtPark smtPark : parkList) {
			parkName += smtPark.getParkName() + ",";
		}

		if (!"".equals(parkName)) {
			parkName = parkName.substring(0, parkName.length() - 1);
		}

		if (StringUtils.isBlank(parkName)) {
			//查询临时BU信息
			OrganizeRelationDTO orgRelation = smtOrganizeRelationMapper.getOrgRelation(Long.parseLong(selectById.getCompId()));
			parkName = orgRelation.getParkName();
		}

		staffInfoVO.setParkName(parkName);
		//获取紧急联系人
		List<SysDict> findByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN).data();

		List<SmtStaffEmergency> emergencyList = staffEmergencyService.list(Wrappers.<SmtStaffEmergency>query().lambda().eq(SmtStaffEmergency::getStaffId, id));
		if (emergencyList.size() > 0) {
			for (int j = 0; j < findByType.size(); j++) {
				if (emergencyList.get(0).getRelation().equals(findByType.get(j).getValue())) {
					emergencyList.get(0).setRelation(findByType.get(j).getLabel());
					break;
				}
			}
			staffInfoVO.setSmtStaffEmergency(emergencyList);
		}
		return new Result<>(staffInfoVO);
	}

	@Override
	public List<SmtStaffDTO> queryMobile(String mobile) {
		List<SmtStaffDTO> staffDTOS = new ArrayList<>();
		List<SmtStaff> selectByIds = this.baseMapper.selectList(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getPhone, mobile)
				.ne(SmtStaff::getStatus, StaffStatusEnum.STAFF_STATUS_QUIT.getCode())
				.ne(SmtStaff::getStatus, StaffStatusEnum.UNKNOWN.getCode())
		);
		for (SmtStaff staff : selectByIds) {
			SmtStaffDTO staffDTO = new SmtStaffDTO();
			staffDTO.setName(staff.getName());
			staffDTO.setBadge(staff.getBadge());
			staffDTO.setCertno(staff.getCertno());
			staffDTOS.add(staffDTO);
		}
		return staffDTOS;
	}

	@Override
	public StaffInfoVO getSmtStaffInfoByPhone(String phone) {
		return getSmtStaffInfoByPhone(phone, null);
	}

	@Override
	public StaffInfoVO getSmtStaffInfoByPhone(String phone, String name) {
		StaffInfoVO staffInfoVO = new StaffInfoVO();
		List<SmtStaff> selectByIds = this.baseMapper.selectList(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getPhone, phone)
				.eq(StringUtils.isNotEmpty(name), SmtStaff::getName, name)
		);
		log.info("手机号码查询员工: " + selectByIds);
		if (Objects.isNull(selectByIds) || selectByIds.size() < 1) {
			return staffInfoVO;
		}
		SmtStaff staff = selectByIds.get(0);
		staffInfoVO.setSmtStaff(staff);
		//根据图片id去获取图片base64
		try {
			if (!Objects.isNull(staff.getFacePicId())) {
				String facePicUrl = imageService.buildImageUrl(staff.getFacePicId());
				staffInfoVO.setFacePic(facePicUrl);
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.info("获取图片异常");
		}
		if (StrUtil.isNotEmpty(staff.getCompId())) {
			List<SmtPark> parkList = smtParkBuService.getParkListByBu(Long.parseLong(staff.getCompId()));
			String parkName = "";
			for (SmtPark smtPark : parkList) {
				parkName += smtPark.getParkName() + ",";
			}

			if (!"".equals(parkName)) {
				parkName = parkName.substring(0, parkName.length() - 1);
			}
			staffInfoVO.setParkName(parkName);
		}

		//获取紧急联系人
		List<SysDict> findByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN).data();

		List<SmtStaffEmergency> emergencyList = staffEmergencyService.list(Wrappers.<SmtStaffEmergency>query().lambda().eq(SmtStaffEmergency::getStaffId, staff.getId()));
		if (emergencyList.size() > 0) {
			for (int j = 0; j < findByType.size(); j++) {
				if (emergencyList.get(0).getRelation().equals(findByType.get(j).getValue())) {
					emergencyList.get(0).setRelation(findByType.get(j).getLabel());
					break;
				}
			}
			staffInfoVO.setSmtStaffEmergency(emergencyList);
		}
		return staffInfoVO;
	}

	/**
	 * 根据员工号获取基本信息
	 */
	@Override
	public StaffInfoVO getBaseinfoById(String badge) {
		// TODO Auto-generated method stub
		SmtStaff selectone = this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, badge));
		if (Objects.isNull(selectone)) {
			throw new TCEException("未找到员工信息");
		}

		StaffInfoVO staffInfoVO = new StaffInfoVO();
		staffInfoVO.setSmtStaff(selectone);
		if (Objects.nonNull(selectone.getDormitoryStatus())) {
			if (DormitoryStatusEnum.IS_INIT.getCode().equals(selectone.getDormitoryStatus())) {
				staffInfoVO.setDormitoryStateDesc("未分配");

				//查询内宿申请中记录
				List<SmtDormitoryApply> dormitoryApplyList = smtDormitoryApplyService.list(new LambdaQueryWrapper<SmtDormitoryApply>()
						.eq(SmtDormitoryApply::getStaffBadge, badge)
						.orderByDesc(SmtDormitoryApply::getCreateTime)
				);
				if (CollectionUtil.isEmpty(dormitoryApplyList)) {
					staffInfoVO.setApplyState(DormitoryApplyStatusEnum.NO_APPLYING.getCode());
					staffInfoVO.setApplyStateDesc(DormitoryApplyStatusEnum.NO_APPLYING.getDesc());
				} else {
					SmtDormitoryApply smtDormitoryApply = dormitoryApplyList.get(0);
					DormitoryApplyStatusEnum applyStatusEnum = DormitoryApplyStatusEnum.getEnmu(smtDormitoryApply.getStatus());
					staffInfoVO.setApplyState(applyStatusEnum.getCode());
					staffInfoVO.setApplyStateDesc(applyStatusEnum.getDesc());
				}

			} else if (DormitoryStatusEnum.NOT_INNER.getCode().equals(selectone.getDormitoryStatus())) {
				staffInfoVO.setDormitoryStateDesc("已分配");
			} else if (DormitoryStatusEnum.NOT_OUTER.getCode().equals(selectone.getDormitoryStatus())) {
				staffInfoVO.setDormitoryStateDesc("外宿");
			}

			staffInfoVO.setDormitoryState(selectone.getDormitoryStatus());
		} else {
			staffInfoVO.setDormitoryState(DormitoryStatusEnum.IS_INIT.getCode());
			staffInfoVO.setDormitoryStateDesc("未分配");
		}

		if (Objects.nonNull(selectone.getStatus())) {
			staffInfoVO.setStatus(selectone.getStatus());
			staffInfoVO.setStatusDes(StaffStatusEnum.desc(selectone.getStatus()));
		}
		if (Objects.nonNull(selectone.getEmpType())) {
			staffInfoVO.setEmpType(selectone.getEmpType());
			staffInfoVO.setEmpTypeDes(EmpTypeEnum.desc(selectone.getEmpType()));
		}

		String facePicId = selectone.getFacePicId();
		if (Objects.nonNull(facePicId)) {
			staffInfoVO.setFacePic(facePicId);
		}
		List<SmtPark> parkList = smtParkBuService.getParkListByBu(Long.parseLong(selectone.getCompId()));
		String parkName = "";
		for (SmtPark smtPark : parkList) {
			parkName += smtPark.getParkName() + ",";
		}

		if (!"".equals(parkName)) {
			parkName = parkName.substring(0, parkName.length() - 1);
		}
		staffInfoVO.setParkName(parkName);
		Boolean isHaveV = false;
//
		// 查询是否添加车辆
		List<SmtVehicleStaff> vehicleCount = vsService.list(
				Wrappers.<SmtVehicleStaff>query().lambda().eq(SmtVehicleStaff::getStaffId, selectone.getId()));
		if (vehicleCount.size() > 0) {
			for (SmtVehicleStaff smtVehicleStaff : vehicleCount) {
				SmtVehicle selectById = smtVehicleService.getById(smtVehicleStaff.getVehicleId());
				if (ObjectUtil.isNotNull(selectById)) {
					if (selectById.getIsDelete().equals(VehicleConstants.UNDELETED)) {
						isHaveV = true;
						break;
					}
				}
			}
		}
		if (isHaveV) {
			// 已绑定车辆
			staffInfoVO.setVehicleState(1);
			staffInfoVO.setVehicleStateDesc("已添加");
		} else {
			// 无绑定车辆
			staffInfoVO.setVehicleState(0);
			staffInfoVO.setVehicleStateDesc("未添加");
		}

		return staffInfoVO;
	}

	@Override
	public SmtStaff getByPhoneAndName(String phone, String name) {
		List<SmtStaff> selectByIds = this.baseMapper.selectList(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getPhone, phone)
				.eq(StringUtils.isNotEmpty(name), SmtStaff::getName, name)
		);
		if (CollUtil.isNotEmpty(selectByIds)) {
			return selectByIds.get(0);
		}
		return null;
	}

	/**
	 * 根据员工号获取员工的车辆
	 */
	@Override
	public IPage<SmtVehicle> getMyVehicle(Page page, String badge) {
		// TODO Auto-generated method stub

		return this.baseMapper.getMyVehicleByBadge(page, badge);
	}

	/**
	 * 员工申请车辆可以出入的园区
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result addVehiclePark(ApplyAuthDTO smtVehicleApply) {
		boolean result = false;

		SmtStaff staff = this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, smtVehicleApply.getBadge()));
		if (ObjectUtil.isNull(staff)) {
			return new Result(false, "未找到员工信息 ");
		}

		List<SmtParkVehicleLevel> list = smtParkVehicleLevelService.list(Wrappers.<SmtParkVehicleLevel>query().lambda()
				.eq(SmtParkVehicleLevel::getParkId, smtVehicleApply.getParkId()).eq(SmtParkVehicleLevel::getJcheId, staff.getJcheId()));
		if (CollUtil.isNotEmpty(list)) {
			return new Result(false, "您的职层不符合申请入园条件 ");
		}

		SmtVehicle selectone = smtVehicleService.getOne(Wrappers.<SmtVehicle>query().lambda()
				.eq(SmtVehicle::getVehiclePlate, smtVehicleApply.getPlateNumber())
				.eq(SmtVehicle::getIsDelete, VehicleConstants.UNDELETED));
		if (ObjectUtil.isNotNull(selectone)) {
			Integer jchebusinessCode = smtJcheAuthService.getJchebusinessCode(Integer.parseInt(staff.getJcheId()), smtVehicleApply.getParkId());
			SmtBusinessDeviceAuth businessDeviceAuth = smtBusinessDeviceAuthService.getOne(new LambdaQueryWrapper<SmtBusinessDeviceAuth>().eq(SmtBusinessDeviceAuth::getBusinessCode, jchebusinessCode)
					.eq(SmtBusinessDeviceAuth::getParkId, smtVehicleApply.getParkId()));
			//设置车辆的权限
			//selectone.setAuthorityId(businessDeviceAuth.getAuthId());
			//smtVehicleService.updateById(selectone);

			int total = smtVehicleService.getApplyVehicle(smtVehicleApply.getParkId(), StrUtil.removeAll(smtVehicleApply.getPlateNumber(), " ").toUpperCase(), VehicleConstants.UNDELETED, VehicleApplyConstants.REJECTED);
			if (total == 0) {
				SmtVehicleApply vehicleApply = new SmtVehicleApply();
				vehicleApply.setParkId(Integer.toString(smtVehicleApply.getParkId()));
				vehicleApply.setVehiclePlate(smtVehicleApply.getPlateNumber());
				vehicleApply.setVehicleId(selectone.getId());
				vehicleApply.setCreateTime(LocalDateTime.now());
				vehicleApply.setStatus(VehicleApplyConstants.APPROVAL);
				vehicleApply.setAuthorityId(businessDeviceAuth.getAuthId());
				log.info(vehicleApply.toString());
				result = vehicleApply.insert();
			} else {
				return new Result(false, "车辆已有入园权限，不能重复申请");
			}
		}
		return new Result<>(result);
	}

	/**
	 * 根据车辆id查询车辆出入园区的列表跟状态
	 */
	@Override
	public List<VehicleApplyVO> getVehiclePark(String vehiclePlate) {
		SmtVehicle vehicle = smtVehicleService.getOne(Wrappers.<SmtVehicle>query().lambda()
				.eq(SmtVehicle::getVehiclePlate, StrUtil.removeAll(vehiclePlate, " ").toUpperCase())
				.eq(SmtVehicle::getIsDelete, VehicleConstants.UNDELETED));
		return this.baseMapper.getVehiclePark(vehicle.getId());
	}

	@Override
	public VehicleParkDetailVO getVehicleParkById(Integer id) {
		VehicleParkDetailVO smtVehicle = this.baseMapper.getVehicleParkById(id);
		String driverLicenseId = "";
		driverLicenseId = smtImageService.getImageBase64ByCode(smtVehicle.getDriverLicenseId());
		smtVehicle.setDriverLicenseId(driverLicenseId);
		String drivingLicenseId = smtImageService.getImageBase64ByCode(smtVehicle.getDrivinglLicenseId());
		smtVehicle.setDrivinglLicenseId(drivingLicenseId);
		return smtVehicle;
	}

	@Override
	public Result updatePhone(SmtStaff smtStaff) {
		// TODO Auto-generated method stub
		return new Result<>(this.baseMapper.updatePhone(smtStaff));
	}

	@Override
	public SmtStaff getStaffByBadgeAll(String badge) {
		return this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getBadge, badge));
	}

	@Override
	public SmtStaff getStaffByBadge(String badge) {
		return this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getBadge, badge)
				.eq(SmtStaff::getStatus, StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode()));
	}


	@Override
	public SmtStaff getStaffByBadge(String badge, String compId) {
		return this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getCompId, compId)
				.eq(SmtStaff::getBadge, badge)
				.eq(SmtStaff::getStatus, StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode()));
	}

	@Override
	public List<SmtStaff> getStaffByBadges(List<String> badges, String compId) {
		return this.list(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getCompId, compId)
				.in(SmtStaff::getBadge, badges)
				.eq(SmtStaff::getStatus, StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode()));
	}


	@Override
	public IPage<StaffNODormitoryVO> quetyStaffNODormitory(Page page, SearchStaffDTO smtStaff) {
		// TODO Auto-generated method stub
		//查询入职状态的
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		smtStaff.setStatus(StaffStatusEnum.STAFF_STATUS_QUIT.getCode());
		smtStaff.setDormitoryStatus(DormitoryStatusEnum.IS_INIT.getCode());
		IPage<StaffNODormitoryVO> quetyStaffNODormitory = this.baseMapper.quetyStaffNODormitory(page, smtStaff, parkIdList);

		List<StaffNODormitoryVO> records = quetyStaffNODormitory.getRecords();
		for (StaffNODormitoryVO staffListVO : records) {

			List<SmtPark> parkList = smtParkBuService.getParkListByBu(Long.parseLong(staffListVO.getCompId()));
			String parkName = "";
			for (SmtPark smtPark : parkList) {
				parkName += smtPark.getParkName() + ",";
			}
			if (!parkName.equals("")) {
				parkName = parkName.substring(0, parkName.length() - 1);
			}

			if (StringUtils.isBlank(parkName)) {
				//查询临时BU信息
				OrganizeRelationDTO orgRelation = smtOrganizeRelationMapper.getOrgRelation(Long.parseLong(staffListVO.getCompId()));
				parkName = orgRelation.getParkName();
			}

			staffListVO.setParkName(parkName);
		}


		return quetyStaffNODormitory;
	}

	@Override
	public StaffInfoVO getSmtStaffInfoByBadge(String badge) {
		StaffInfoVO staffInfoVO = new StaffInfoVO();
		SmtStaff selectById = this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, badge));
		if (Objects.isNull(selectById)) {
			throw new TCEException("非员工入住，未找到员工信息");
		}

		staffInfoVO.setSmtStaff(selectById);
		if (StaffStatusEnum.UNKNOWN.getCode().equals(selectById.getStatus())) {
			return staffInfoVO;
		}
		List<SmtPark> parkList = smtParkBuService.getParkListByBu(Long.parseLong(selectById.getCompId()));
		String parkName = "";
		for (SmtPark smtPark : parkList) {
			parkName += smtPark.getParkName() + ",";
		}

		if (!"".equals(parkName)) {
			parkName = parkName.substring(0, parkName.length() - 1);
		}

		if (StringUtils.isBlank(parkName)) {
			//查询临时BU信息
			OrganizeRelationDTO orgRelation = smtOrganizeRelationMapper.getOrgRelation(Long.parseLong(selectById.getCompId()));
			if (Objects.nonNull(orgRelation)) {
				parkName = orgRelation.getParkName();
			}
		}

		staffInfoVO.setParkName(parkName);
		//根据图片id去获取图片base64
		String certnoPicId = selectById.getCertnoPicId();
		try {
			if (Objects.isNull(certnoPicId)) {
				String certnoPic = imageService.buildImageUrl(certnoPicId);
				staffInfoVO.setCertnoPic(certnoPic);
			}
		} catch (Exception e) {
			log.error("下载员工身份证照片异常", e);
		}
		//下载人脸照片
		String facePicId = selectById.getFacePicId();
		try {
			if (Objects.isNull(certnoPicId)) {
				String facePic = imageService.buildImageUrl(facePicId);
				staffInfoVO.setFacePic(facePic);
			}
		} catch (Exception e) {
			log.error("下载员工身份证照片异常", e);
		}
		//获取紧急联系人
		List<SmtStaffEmergency> emergencyList = staffEmergencyService.list(Wrappers.<SmtStaffEmergency>query().lambda().eq(SmtStaffEmergency::getStaffId, selectById.getId()));
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN);
		Map<String, String> emergencyType = new HashMap<>();
		List<SysDict> sysDicts = findByType.getData();
		sysDicts.forEach(sysDict -> {
			emergencyType.put(sysDict.getValue(), sysDict.getLabel());
		});
		emergencyList.forEach(e -> {
			String value = emergencyType.get(e.getRelation());
			e.setRelationDesc(value);
		});
		staffInfoVO.setSmtStaffEmergency(emergencyList);
		return staffInfoVO;
	}

	@Override
	public SmtStaff getSimpleSttaffByBadge(String badge) {
		SmtStaff smtStaff = this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, badge));
		if (Objects.isNull(smtStaff)) {
			log.error("未找到员工信息");
			return null;
		}
		return smtStaff;
	}

	@Override
	public List<StaffLookupRespDTO> searchStaffForAdmin(String badge, List<Integer> parkIds) {
		if (StrUtil.isBlank(badge) || CollectionUtil.isEmpty(parkIds)) {
			return Collections.emptyList();
		}

		List<SmtParkBu> parkBus = smtParkBuService.list(Wrappers.<SmtParkBu>query().lambda()
				.in(SmtParkBu::getParkId, parkIds));
		Set<String> visibleCompIds = parkBus.stream()
				.map(SmtParkBu::getCompId)
				.filter(StrUtil::isNotBlank)
				.collect(Collectors.toSet());
		// 临时员工通过组织关系绑定园区，需与常规 BU 关系一并纳入权限范围。
		smtOrganizeRelationService.list(Wrappers.<SmtOrganizeRelation>query().lambda()
				.in(SmtOrganizeRelation::getParkId, parkIds)).stream()
				.map(SmtOrganizeRelation::getCompId)
				.filter(StrUtil::isNotBlank)
				.forEach(visibleCompIds::add);
		if (visibleCompIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<SmtStaff> staffs = this.baseMapper.selectList(Wrappers.<SmtStaff>query().lambda()
				.like(SmtStaff::getBadge, badge.trim())
				.in(SmtStaff::getCompId, visibleCompIds)
				.last("and rownum <= 20"));
		return staffs.stream().limit(20).map(this::toStaffLookupResp).collect(Collectors.toList());
	}

	@Override
	public StaffSelfCheckInProfileRespDTO getCheckInProfileForBadge(String badge) {
		if (StrUtil.isBlank(badge)) {
			return null;
		}
		SmtStaff staff = this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getBadge, badge));
		if (staff == null) {
			return null;
		}

		StaffSelfCheckInProfileRespDTO response = new StaffSelfCheckInProfileRespDTO();
		response.setName(staff.getName());
		response.setProfileComplete(StrUtil.isNotBlank(staff.getName()) && StrUtil.isNotBlank(staff.getCertno()));
		response.setMaskedCertNo(maskCertNo(staff.getCertno()));
		return response;
	}

	@Override
	public InternalStaffAccountRespDTO getInternalAccountByBadge(String badge) {
		if (StrUtil.isBlank(badge)) {
			return null;
		}
		SmtStaff staff = this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getBadge, badge));
		if (staff == null) {
			return null;
		}

		InternalStaffAccountRespDTO response = new InternalStaffAccountRespDTO();
		response.setStaffId(staff.getId());
		response.setBadge(staff.getBadge());
		response.setName(staff.getName());
		response.setStatus(staff.getStatus());
		return response;
	}

	/** 将持久化实体显式投影为管理员查询响应，避免反射复制引入敏感字段。 */
	private StaffLookupRespDTO toStaffLookupResp(SmtStaff staff) {
		StaffLookupRespDTO response = new StaffLookupRespDTO();
		response.setStaffId(staff.getId());
		response.setBadge(staff.getBadge());
		response.setName(staff.getName());
		response.setDepartmentName(staff.getDepName());
		return response;
	}

	/** 身份证号仅显示末四位，短证件号不暴露任何原始字符。 */
	private String maskCertNo(String certNo) {
		if (StrUtil.isBlank(certNo)) {
			return null;
		}
		if (certNo.length() <= 4) {
			return "****";
		}
		StringBuilder masked = new StringBuilder(certNo.length());
		for (int index = 0; index < certNo.length() - 4; index++) {
			masked.append('*');
		}
		return masked.append(certNo.substring(certNo.length() - 4)).toString();
	}

	@Override
	public SmtStaff getSimpleSttaffById(String staffId) {
		return this.getById(staffId);
	}

	@Override
	public List<SmtStaff> getSimpleSttaffByIds(List<String> staffIds) {
		List<Long> ids = staffIds.stream().map(Long::parseLong).collect(Collectors.toList());
		return this.baseMapper.selectBatchIds(ids);
	}

	@Override
	public Result outDormitory(SmtStaff smtStaff) {
		// TODO Auto-generated method stub
		SmtOutDormitoryStaff selectOne = null;
		List<SmtOutDormitoryStaff> list = outDormitoryStaffMapper.selectList(Wrappers.<SmtOutDormitoryStaff>query().lambda().eq(SmtOutDormitoryStaff::getStaffBadge, smtStaff.getBadge()).eq(SmtOutDormitoryStaff::getIsDelete, 0).orderByDesc(SmtOutDormitoryStaff::getCreateTime));
		if (list.size() > 0) {
			selectOne = list.get(0);
		}
		return new Result<>(selectOne);
	}

	/**
	 * 员工添加车辆
	 */
	@Override
	public Result addVehicle(AddVehicleDTO addVehicleDTO) {
		if (addVehicleDTO == null) {
			return new Result<>(false, "车辆信息不能为空");
		}

		SmtStaff selectByBadge = this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, addVehicleDTO.getBadge()));
		if (Objects.isNull(selectByBadge)) {
			throw new TCEException("未找到员工信息");
		}

		//判断车辆是否已经添加
		int count = smtVehicleService.count(Wrappers.<SmtVehicle>query().lambda()
				.eq(SmtVehicle::getVehiclePlate, StrUtil.removeAll(addVehicleDTO.getPlateNumber(), " ").toUpperCase())
				.eq(SmtVehicle::getIsDelete, VehicleConstants.UNDELETED)
				.eq(SmtVehicle::getParkId, addVehicleDTO.getParkId()));
		if (count > 0) {
			return new Result(false, "员工车辆后台已录入，不需要再申请");
		}

		SaveVehicleDTO ve = new SaveVehicleDTO();
		ve.setVehiclePlate(addVehicleDTO.getPlateNumber());
		ve.setVehicleBrand(addVehicleDTO.getVehicleBrand());
		ve.setVehicleAscription(VehicleBelongTypeEnum.STAFF_VEHICLE.getCode());
		ve.setStaffId(selectByBadge.getId());
		ve.setDriverLicenseId(addVehicleDTO.getDrivingLicence());
		ve.setDrivinglLicenseId(addVehicleDTO.getCarDrivingLicence());
		ve.setParkId(addVehicleDTO.getParkId());
		//判断集合是否为空
		if (addVehicleDTO.getVehicleColor() != null) {
			ve.setVehicleColor(Integer.parseInt(addVehicleDTO.getVehicleColor()));
		}

		if (addVehicleDTO.getVehicleType() != null) {
			ve.setVehicleType(Integer.parseInt(addVehicleDTO.getVehicleType()));
		}
		/*ve.setParkId(selectByBadge.getParkId());*/  ///需要修改
		//APP端申请时 只添加一条车辆表的数据和车辆员工关联表的数据
		return smtVehicleService.saveSmtVehicleOnly(ve);
	}


	/**
	 * 生成员工二维码存于redis
	 * 30分钟失效
	 */
	@Override
	public Result<?> getQrcode(String badge) {
		// TODO Auto-generated method stub
		SmtStaff selectByBadge = this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, badge));

		String base64 = null;
		if (selectByBadge != null) {
			String staffId = selectByBadge.getId().toString();
			@SuppressWarnings("unchecked")
			ValueOperations<String, String> value = redisTemplate.opsForValue();
			try {
				if (value.get(staffId) != null) {
					base64 = value.get(staffId);
				} else {
					base64 = QRCodeUtils.wordsCreateQRCode(staffId);
					value.set(selectByBadge.getId().toString(), base64, 1800, TimeUnit.SECONDS);

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		QRCodeVO vo = new QRCodeVO();
		base64 = ImageUtils.changeFullBase64(base64, ImageUtils.IMAGE_TYPE_PNG);
		vo.setQrcode(base64);
		return new Result<>(vo);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void syncStaff(EmpHrVO empHr, SmtDormitoryStaffService dormitoryStaffService) {
		log.info("同步{}员工数据：{}", empHr.getBadge(), empHr);
		SmtStaff smtStaff = this.getBaseMapper().selectOne(
				Wrappers.<SmtStaff>query().lambda()
						.eq(SmtStaff::getBadge, empHr.getBadge())
		);
		if (Objects.isNull(smtStaff)) {
			//以工号没有查询到员工数据后 再以身份证号代替工号查询
			newStaffInfo(empHr,dormitoryStaffService);
		} else {
			//更新员工表数据
			StaffUtil.buildStaff(empHr, smtStaff);
			this.baseMapper.updateById(smtStaff);
			updateStaffLeaInfo(empHr, smtStaff.getId());
			//离职同步
			Boolean isDimission = StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(empHr.getStatus());
			//删除已离职的用户信息
			if (isDimission) {
				synDeleteUserInfo(smtStaff);
			}
			log.info("同步员工信息【修改】：{}", empHr.getName());
		}
	}

	public void newStaffInfo(EmpHrVO empHr,SmtDormitoryStaffService dormitoryStaffService){
		if(StringUtils.isEmpty(empHr.getCertno())){
			return;
		}
		List<SmtStaff> smtStaffs = this.baseMapper.selectList(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getBadge, empHr.getCertno())
				.orderByDesc(SmtStaff::getCreateTime)
		);
		boolean isAuth = false;
		SmtStaff smtStaff = null;
		if (CollUtil.isNotEmpty(smtStaffs) && !StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(empHr.getStatus())) {
			//更新最后一条记录 这里的原因是可能一个人一直未入职  但是用身份证入住了多次
			smtStaff = smtStaffs.get(0);
			//表示这是一条先入住 后入职的记录
			//更新这条员工数据
			StaffUtil.buildStaff(empHr, smtStaff);
			//设置该员工已入住
			smtStaff.setDormitoryStatus(DormitoryStatusEnum.NOT_INNER.getCode());
			connectLockService.updateLockPerson(smtStaff.getCertno(), smtStaff.getName(), smtStaff.getBadge(), smtStaff.getPhone());
			this.baseMapper.updateById(smtStaff);
			updateStaffLeaInfo(empHr, smtStaff.getId());
			//在先入住 后入职的情况下  如果同步过来的是一条离职记录 则不修改住宿信息
			dormitoryStaffService.updateDormitoryStaffTemp(smtStaff);
			isAuth = true;
			log.info("同步员工信息【先入住后入职】：{}", empHr.getName());
		} else {
			//表示这是一条新数据
			//添加员工数据
			smtStaff = new SmtStaff();
			StaffUtil.buildStaff(empHr, smtStaff);
			this.baseMapper.insert(smtStaff);
			log.info("同步dhr员工图片信息【新增】：{}", empHr.getBadge());
			if (!StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(empHr.getStatus())) {
				//在先入住 后入职的情况下  如果同步过来的是一条离职记录 则不修改住宿信息 不分配设备权限和APP权限
				dormitoryStaffService.updateDormitoryStaffTemp(smtStaff);
				log.info("同步dhr员工图片信息【新增】：{}", empHr.getBadge());
				isAuth = true;
			}
		}

		if (isAuth) {
			//分配设备权限和APP权限
			addStaffDeviceAuth(smtStaff);
			List<SmtPark> parkList = smtParkBuService.getParkListByBu(Long.parseLong(smtStaff.getCompId()));
			if (!CollUtil.isEmpty(parkList)) {
				//添加员工默认app权限
				appStaffAuthService.initStaffAuth(smtStaff.getId(), parkList.get(0).getId());
			} else {
				log.info("员工BU未关联园区,员工ID={},{}", smtStaff.getId(), smtStaff.getCompId());
			}
		}
	}

	private void updateStaffLeaInfo(EmpHrVO empHr, Long staffId) {
		if (Objects.isNull(empHr.getLeaDate()) || StrUtil.isBlank(empHr.getLeaType())) {
			// 由于从DHR同步数据时，对于已离职人员改为在职状态的数据，对应的LeaDate和leaType字段都会为空，因此需单独更新此字段
			log.info("更新员工[{}]离职字段，leaDate:{}, leaType:{}", empHr.getBadge(), empHr.getLeaDate(), empHr.getLeaType());
			try {
				this.baseMapper.updateStaffLeaveStatus(staffId, empHr.getLeaType(), empHr.getLeaDate());
			} catch (Exception e) {
				log.error("更新员工[{}]离职字段异常：", empHr.getBadge(), e);
			}
		}
	}

	@Override
	public SmtStaff getStaffIgnoreCase(String badge) {
		return this.baseMapper.getStaffIgnoreCase(badge);
	}

	@Override
	public Result<Boolean> perfectFace(StaffPerfectDTO perfectDTO) {
		//查询员工信息
		QueryWrapper<SmtStaff> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(SmtStaff::getBadge, perfectDTO.getBadge());
		SmtStaff queryRsPo = this.baseMapper.selectOne(queryWrapper);
		if (Objects.isNull(queryRsPo)) {
			throw new TCEException("获取到员工信息异常");
		}
		String certnoPicId = null;
		String facePicId = null;
		try {
			if (!StringUtil.isNullOrEmpty(perfectDTO.getCertnoPic())) {
				certnoPicId = smtImageService.saveImage(0, perfectDTO.getCertnoPic(), SmtImageEnum.TYPE_STAFF_IDCARD_FRONT.getCode());
				if (StringUtils.isBlank(certnoPicId)) {
					throw new TCEException("保存身份证图片异常");
				}
			}
			facePicId = smtImageService.saveImage(0, perfectDTO.getFacePic(), SmtImageEnum.TYPE_STAFF_FACE.getCode());
			if (StringUtils.isBlank(facePicId)) {
				throw new TCEException("保存人脸图片异常");
			}
		} catch (TCEException tce) {
			throw tce;
		} catch (Exception e) {
			log.error("保存图片异常", e);
			throw new TCEException("保存图片异常");
		}

		//更新人脸、身份证证照片信息
		SmtStaff updateSmtStaff = new SmtStaff();
		updateSmtStaff.setId(queryRsPo.getId());
		updateSmtStaff.setBadge(queryRsPo.getBadge());
		if (!StringUtil.isNullOrEmpty(certnoPicId)) {
			updateSmtStaff.setCertnoPicId(certnoPicId);
		}
		updateSmtStaff.setFacePicId(facePicId);
		//更新信息
		this.updateById(updateSmtStaff);

		Integer eid = queryRsPo.getEId();
		log.info("同步EHR照片图片前打印，员工Badge={},EID={}", queryRsPo.getBadge(), eid);
		if (Objects.nonNull(eid)) {
			try {
				//同步到EHR
				SaveEPhotoReqDTO saveEPhotoDto = new SaveEPhotoReqDTO();
				saveEPhotoDto.setEid(eid);
				saveEPhotoDto.setPhoto(perfectDTO.getFacePic());
				Result<Boolean> saveOrUpdatePhotoRs = remoteEPhotoService.saveOrUpdatePhoto(saveEPhotoDto);
				log.info("保存人事员工人脸图片信息:{}", saveOrUpdatePhotoRs);
			} catch (Exception e) {
				log.error("同步照片到EHR失败", e);
			}
		}

		//额外添加name，卡片下发需要personName，
		updateSmtStaff.setName(queryRsPo.getName());
		updateSmtStaff.setCompId(queryRsPo.getCompId());

		//下发闸机
		if (StringUtils.isEmpty(queryRsPo.getFacePicId())) {
			addDeviceTask(updateSmtStaff, DeviceTaskActionEnum.DOWN.getCode());
		} else {
			addDeviceTask(updateSmtStaff, DeviceTaskActionEnum.UPDATE.getCode());
		}
		return new Result<>(Boolean.TRUE);
	}

	@Override
	//@Transactional(rollbackFor = Exception.class)
	public void syncFaceImg() {
		LocalDateTime time = this.saveOrGetKey();
		log.info("dhr照片同步任务开始：{}", time);
		LocalDateTime now = LocalDateTime.now();
		// List<String> idCards = ToolUtils.readLastRemoteImgNameList(time);
		List<String> idCards = HuaweiOBSUtil.readLastRemoteImgNameList(time);
		if (CollUtil.isEmpty(idCards)) {
			log.info("dhr照片同步当前远程库没有更新的人员图片");
			return;
		}
		log.info("dhr照片同步远程库本次更新的图片数:{}", idCards.size());
		for (String certNo : idCards) {
			//查询员工信息
			List<SmtStaff> queryRsPos = this.list(Wrappers.<SmtStaff>query().lambda()
					.eq(SmtStaff::getCertno, certNo).orderByDesc(SmtStaff::getCreateTime));
			if (CollectionUtil.isEmpty(queryRsPos)) {
				continue;
			}

			// 过滤出在职员工
			List<SmtStaff> activeEmployees = queryRsPos.stream()
					.filter(employee -> employee.getStatus() != 0)
					.collect(Collectors.toList());
			if (CollectionUtil.isEmpty(activeEmployees)) {
				log.info("dhr照片同步人员已离职");
				continue;
			}

			//因可能存在身份证重复就数据，统一取第一条最新数据进行处理
			SmtStaff staff = queryRsPos.get(0);
			if (Integer.valueOf(0).equals(staff.getStatus())) {
				log.info("dhr照片同步人员已离职: {},{}", staff.getName(), staff.getBadge());
				continue;
			}
			if (StringUtils.isEmpty(staff.getCompId())) {
				log.info("dhr照片同步人员没有BU，{},{}", staff.getName(), staff.getBadge());
				continue;
			}

			SmtStaff updateSmtStaff = new SmtStaff();
			updateSmtStaff.setId(staff.getId());
			updateSmtStaff.setBadge(staff.getBadge());
			updateSmtStaff.setName(staff.getName());
			updateSmtStaff.setCompId(staff.getCompId());

			//获取人脸图片内容
			// String dhrImage = ToolUtils.readRemoteImgToBase64(certNo);
			String dhrImage = HuaweiOBSUtil.readRemoteImgToBase64(certNo);
			if (StringUtils.isEmpty(dhrImage)) {
				log.info("dhr照片同步-dhr图片获取失败");
				continue;
			}

			Integer handleType = DeviceTaskActionEnum.DOWN.getCode();
			if (StringUtils.isNotEmpty(staff.getFacePicId())) {
				handleType = DeviceTaskActionEnum.UPDATE.getCode();

				//获取图片base64内容
				String imageBase64ByCode = smtImageService.getImageBase64ByCode(staff.getFacePicId());
				String faceMd5 = SecureUtil.md5(imageBase64ByCode);

				String romoteMd5 = SecureUtil.md5(dhrImage);

				if (faceMd5.equals(romoteMd5)) {
					//人脸图片和远程图库的图片一致 则不变动
					continue;
				}
			}
			log.info("dhr照片同步-dhr：{}", dhrImage.substring(0, 20));
			//保存新人脸图
			String facePicId = smtImageService.saveImage(0, dhrImage,
					SmtImageEnum.TYPE_STAFF_FACE.getCode());
			if (StringUtils.isBlank(facePicId)) {
				log.error("dhr照片同步保存人脸图片异常");
				throw new TCEException("保存人脸图片异常");
			}
			//更新人脸、身份证证照片信息
			updateSmtStaff.setFacePicId(facePicId);
			this.updateById(updateSmtStaff);

			//下发闸机
			log.info("dhr照片同步-下发闸机：{}，下发方式：{}", updateSmtStaff, handleType);
			addDeviceTask(updateSmtStaff, handleType);
		}
		log.info("dhr照片同步任务结束：查询开始时间:{} 查询结束时间:{}", time,now);
		//数据处理完成后 更新最后时间
		String nowTime = formatter.format(now);
		ValueOperations<String, String> value = redisTemplate.opsForValue();
		value.set(key, nowTime);

		log.info("dhr照片同步任务结束：{}", LocalDateTime.now());
	}

	private LocalDateTime saveOrGetKey() {
		ValueOperations<String, String> value = redisTemplate.opsForValue();
		if (Objects.nonNull(value.get(key))) {
			String beforeTime = value.get(key);
			return LocalDateTime.parse(beforeTime, formatter);
		} else {
			//如果没有记录最开始时间 则以1970年为开始计算时间
			LocalDateTime now = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
			return now;
		}
	}

	@Override
	public Boolean checkPerfectInfo(String badge) {
		boolean isNeed = true;
		QueryWrapper<SmtStaff> queryWrapper = new QueryWrapper<SmtStaff>();
		queryWrapper.lambda().eq(SmtStaff::getBadge, badge);

		SmtStaff smtStaff = this.baseMapper.selectOne(queryWrapper);
		if (Objects.nonNull(smtStaff) && StringUtils.isBlank(smtStaff.getFacePicId())) {
			isNeed = false;
		}
		return isNeed;
	}

	@Override
	public Boolean faceSearchForCompare(StaffPerfectDTO perfectDTO) {
		if (StrUtil.isBlank(perfectDTO.getFacePic())) {
			throw new TCEException("未包含人脸信息");
		}
		if (StrUtil.isBlank(perfectDTO.getDeviceNo())) {
			throw new TCEException("设备信息为空");
		}
		String faceImage = perfectDTO.getFacePic().replaceAll("[\\t\\n\\r]", "");//替换换行符号
		return faceSearchForLogin(faceImage, perfectDTO.getDeviceNo()) != null;
	}

	@Override
	public SmtStaff faceSearchForLogin(String facePic, String deviceNo) {
		SmtStaff smtStaff = null;
		try {
			//查找当前设备登陆过的账户信息
			log.info("查询设备号:{}", deviceNo);
			log.info("人脸比对开始");
			List<AppUserDevice> appUserDeviceList = remoteAppDeviceService.queryByDeviceNo(deviceNo).data();
			//只取最近登录过的，优先已绑定的
			AppUserDevice appUserDevice = appUserDeviceList.get(0);
			log.info("查询员工号:{}, 设备ID:{}", appUserDevice.getBadge(), appUserDevice.getId());
			smtStaff = this.getSimpleSttaffByBadge(appUserDevice.getBadge());
			if (Objects.isNull(smtStaff)) {
				throw new SmartException("员工为空");
			}
			//查询员工关联的人脸图片
			String getImageBase64Rs = smtImageService.getImageBase64ByCode(smtStaff.getFacePicId());
			log.info("查询员工关联的人脸图片:工号：{},人脸图片ID:{}", smtStaff.getBadge(), smtStaff.getFacePicId());
			if (StringUtil.isNullOrEmpty(getImageBase64Rs)) {
				throw new TCEException("获取员工关联人脸图异常");
			}

			CompareDTO compareDTO = new CompareDTO();
			CompareImageDTO compareImageDTO1 = new CompareImageDTO();
			compareImageDTO1.setImageBase64(facePic);
			compareImageDTO1.setFaceType(FaceTypeEnum.LIVE.getType());

			CompareImageDTO compareImageDTO2 = new CompareImageDTO();
			compareImageDTO2.setImageBase64(getImageBase64Rs);
			compareImageDTO2.setFaceType(FaceTypeEnum.LIVE.getType());

			compareDTO.setCompareImageA(compareImageDTO1);
			compareDTO.setCompareImageB(compareImageDTO2);

			com.tce.smart.algorithm.api.dto.resp.CompareDTO imageComareRs = remoteAlgorithmService.compare(IdUtil.fastSimpleUUID().toUpperCase(), AlgorithmTypeEnum.COMPARE_FACEALL.getType(), compareDTO, SecurityConstants.FROM_IN).data();
			log.info("人像比对: 员工号:{} 相似度：{}", smtStaff.getBadge(), imageComareRs.getSimilarity());
			//小于阀值则认为不是本人
			if (-1 == (new BigDecimal(String.valueOf(imageComareRs.getSimilarity()))
					.compareTo(new BigDecimal(loginCompareValue)))) {
				throw new TCEException("人脸识别验证未通过,相识度[" + imageComareRs.getSimilarity() + "]");
			}

		} catch (TCEException tce) {
			throw tce;
		} catch (Exception e) {
			log.error("人脸识别验证未通过", e);
			throw new TCEException("人脸识别验证未通过");
		}

		return smtStaff;
	}

	/**
	 * 通过员工BU查询员工所属的园区列表
	 *
	 * @param buId
	 * @return
	 */
	private List<Integer> getStaffParkIdList(Long buId) {
		log.debug("查询员工园区权限：buId={}", buId);
		List<SmtPark> parkListByBu = smtParkBuService.getParkListByBu(buId);

		if (CollUtil.isEmpty(parkListByBu)) {
			log.warn("员工BU未关联任何园区：buId={}", buId);
			return new ArrayList<>();
		}

		List<Integer> parkIdList = new ArrayList<>();
		for (SmtPark smtPark : parkListByBu) {
			parkIdList.add(smtPark.getId());
		}

		log.debug("员工关联园区列表：buId={}, parkIds={}", buId, parkIdList);
		return parkIdList;
	}

	/**
	 * 添加员工权限 不下发人员卡片 因为目前还不存在员工人脸图片
	 *
	 * @param smtStaff
	 */
	private void addStaffDeviceAuth(SmtStaff smtStaff) {
		//通行权限业务类型
		Integer authType = BusinessAuthorityEnum.STAFF_FACE.getCode();
		//查询该员工所属的园区权限
		List<Integer> parkIdList = new ArrayList<>();
		String buId = smtStaff.getCompId();
		//外部企业buId为long型，不存于SmtParkBu表中
		if (smtStaff.getCompId().length() > 16) {
//			Integer park = smtOrganizeRelationService.getByBu(Long.parseLong(buId)).getParkId();
//			parkIdList.add(park);
			smtStaffExtService.authAccess(smtStaff);
			return;
		} else {
			parkIdList = getStaffParkIdList(Long.parseLong(smtStaff.getCompId()));
		}
		List<Integer> authIds;
		List<Integer> buSecurity = smtSecurityBuService.getRelationSecuritys(buId, parkIdList);
		//根据业务类型和员工所属的园区Id列表 查询当前配置的通关权限配置记录
		List<SmtBusinessDeviceAuth> mulDeviceAuth = smtBusinessDeviceAuthService.getMulDeviceAuth(parkIdList, authType);
		authIds = mulDeviceAuth.stream().map(SmtBusinessDeviceAuth::getAuthId).collect(Collectors.toList());
		if(CollUtil.isNotEmpty(buSecurity)) {
			authIds.addAll(buSecurity);
		}
		authIds = authIds.stream().distinct().collect(Collectors.toList());
		for (Integer authId : authIds) {
			//查询该员工是否已存在对应的通过权限
			int staffDevAuCount = smtStaffDeviceAuthService.count(Wrappers.<SmtStaffDeviceAuth>query()
					.lambda().eq(SmtStaffDeviceAuth::getStaffId, smtStaff.getId())
					.eq(SmtStaffDeviceAuth::getAuthId, authId));
			if (staffDevAuCount <= 0) {
				//添加员工通过权限
				SmtStaffDeviceAuth deviceAuth = new SmtStaffDeviceAuth();
				deviceAuth.setStaffId(smtStaff.getId());
				deviceAuth.setAuthId(authId);
				deviceAuth.setCreateTime(DateUtils.date());
				smtStaffDeviceAuthService.addAuth(deviceAuth);
				log.info("添加员工刷脸通行权限：staffId={},AuthId={}", smtStaff.getId(), authId);
			}
		}
	}

	/**
	 * 设备人员删除、添加任务操作
	 *
	 * @param smtStaff 员工信息
	 */
	@Override
	public void addDeviceTask(SmtStaff smtStaff, Integer action) {
		Integer authType = BusinessAuthorityEnum.STAFF_FACE.getCode();
		//添加员工人脸通行权限
		addStaffDeviceAuth(smtStaff);

		//查询该员工所属的园区权限
		List<Integer> parkIdList = new ArrayList<>();
		String buId = smtStaff.getCompId();
		//外部企业buId为long型，不存于SmtParkBu表中
		if (smtStaff.getCompId().length() > 16) {
			Integer park = smtOrganizeRelationService.getByBu(Long.parseLong(buId)).getParkId();
			parkIdList.add(park);
		} else {
			parkIdList = getStaffParkIdList(Long.parseLong(smtStaff.getCompId()));
		}

		//查询员工的设备权限记录
		List<SmtDeviceAuthorityRelation> deviceAuthList = smtDeviceAuthorityRelationService.getMulRelationAuth(smtStaff.getId(), parkIdList, authType, DeviceAuthorityEnum.STAFF);

		//推迟1分钟下发
		Long addStartSecond = DateUtil.currentSeconds() + 60;
		savePersonCardTask(action, addStartSecond, DeviceTaskConstants.maxTime, smtStaff, deviceAuthList);
	}

	/**
	 * 添加人员卡片下发任务
	 *
	 * @param actionType     下发类型
	 * @param startTime      生效时间
	 * @param endTime        失效时间
	 * @param smtStaff       员工信息
	 * @param deviceAuthList 设备权限列表
	 */
	@Override
	public void savePersonCardTask(Integer actionType, long startTime, long endTime, SmtStaff smtStaff, List<SmtDeviceAuthorityRelation> deviceAuthList) {
		DeviceTaskVO deviceTaskVO;
		if (CollectionUtil.isEmpty(deviceAuthList)) {
			return;
		}
		boolean isDeleteAction = DeviceTaskActionEnum.DEL.getCode().equals(actionType)
				|| DeviceTaskActionEnum.DELAY_DEL.getCode().equals(actionType);
		if (!isDeleteAction && StringUtils.isEmpty(smtStaff.getFacePicId())) {
			return;
		}
		for (int i = 0; i < deviceAuthList.size(); i++) {
			String deviceCode = deviceAuthList.get(i).getDeviceId();
			SmtDevice device = smtDeviceService.getById(deviceCode);
			Integer deviceActionType = personCardTaskAction(actionType, smtStaff, deviceCode, device);
			deviceTaskVO = new DeviceTaskVO();
			deviceTaskVO.setAction(deviceActionType);
			deviceTaskVO.setCardType(SmtVisitorEnum.CARD_TYPE_1.getType());
			deviceTaskVO.setImageId(smtStaff.getFacePicId());
			deviceTaskVO.setServiceType(personCardServiceType(deviceActionType, device));
			deviceTaskVO.setDeviceCode(deviceCode);
			deviceTaskVO.setCardNo(smtStaff.getId().toString());
			deviceTaskVO.setDeviceType(DeviceTaskConstants.CARD);
			deviceTaskVO.setStartTime(startTime);
			deviceTaskVO.setOverTime(endTime);

			if (Objects.nonNull(smtStaff.getBadge())) {
				deviceTaskVO.setGeneral(smtStaff.getBadge() + "-" + smtStaff.getName());
			} else {
				String badge = this.getById(smtStaff.getId()).getBadge();
				deviceTaskVO.setGeneral(badge + "-" + smtStaff.getName());
			}

			//添加下发设备任务
			smtDeviceTaskService.saveTask(deviceTaskVO);
		}
	}

	private Integer personCardServiceType(Integer actionType, SmtDevice device) {
		if (isPersonCardUpdateAction(actionType) && isIscDevice(device)) {
			return DeviceTaskConstants.UPDATE_FACE;
		}
		return DeviceTaskConstants.CARD;
	}

	private Integer personCardTaskAction(Integer actionType, SmtStaff smtStaff, String deviceCode, SmtDevice device) {
		if (!isPersonCardUpdateAction(actionType)) {
			return actionType;
		}
		if (hasPersonCardDownRecord(smtStaff, deviceCode, device)) {
			return actionType;
		}
		if (DeviceTaskActionEnum.DELAY_UPDATE.getCode().equals(actionType)) {
			return DeviceTaskActionEnum.DELAY_DOWN.getCode();
		}
		return DeviceTaskActionEnum.DOWN.getCode();
	}

	private boolean isPersonCardUpdateAction(Integer actionType) {
		return DeviceTaskActionEnum.UPDATE.getCode().equals(actionType)
				|| DeviceTaskActionEnum.DELAY_UPDATE.getCode().equals(actionType);
	}

	private boolean isIscDevice(SmtDevice device) {
		return Objects.nonNull(device) && DeviceSyncEnum.YES.getCode().equals(device.getIsSync());
	}

	private boolean hasPersonCardDownRecord(SmtStaff smtStaff, String deviceCode, SmtDevice device) {
		if (Objects.isNull(device) || Objects.isNull(device.getIsSync())) {
			return false;
		}
		String cardNo = smtStaff.getId().toString();
		if (isIscDevice(device)) {
			return Objects.nonNull(smtIscDownRecordService.getOne(new LambdaQueryWrapper<SmtIscDownRecord>()
					.eq(SmtIscDownRecord::getDeviceCode, deviceCode)
					.eq(SmtIscDownRecord::getCardNo, cardNo)
					.eq(SmtIscDownRecord::getDeviceType, DeviceTaskConstants.CARD)
					.eq(SmtIscDownRecord::getServiceType, DeviceTaskConstants.CARD_STAFF_IMPORT)));
		}
		return Objects.nonNull(smtTaskDownRecordService.getOne(new LambdaQueryWrapper<SmtTaskDownRecord>()
				.eq(SmtTaskDownRecord::getDeviceCode, deviceCode)
				.eq(SmtTaskDownRecord::getCardNo, cardNo)
				.eq(SmtTaskDownRecord::getDeviceType, DeviceTaskConstants.CARD)
				.eq(SmtTaskDownRecord::getServiceType, DeviceTaskConstants.CARD_STAFF_IMPORT)));
	}

	/**
	 * 离职员工信息同步删除
	 *
	 * @param smtStaff 同步员工信息
	 */
	@Override
	public void synDeleteUserInfo(SmtStaff smtStaff) {
		if (StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(smtStaff.getStatus())) {
			smtIscStaffCardService.removeStaffCardsByStaffId(smtStaff.getId());
			//临时工删除时 检查是否入住
			if (StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode().equals(smtStaff.getStatus())) {
				List<DormitoryRoomDetailRespDTO> reDor = smtDormitoryStaffService.getSimpleStaffRoomList(smtStaff.getBadge());
				if (CollUtil.isNotEmpty(reDor)) {
					throw new SmartException("该员工存在入住记录，请先退宿再删除");
				}
			}

			//删除C6
			Integer userId = null;
			if (null != SecurityUtils.getUser()) {
				userId = SecurityUtils.getUser().getId();
			} else {
				UserInfo userInfo = remoteUserService.info(smtStaff.getBadge(), SecurityConstants.FROM_IN).getData();
				if (null != userInfo) {
					userId = userInfo.getSysUser().getUserId();
				}
			}
			if (null != userId) {
				SmtOrganizeRelation organizeRelation = smtOrganizeRelationService.getByUserId(userId);
				if (null != organizeRelation) {
					leaveToC6(smtStaff, organizeRelation);
				}
			}

			SmtExternalDept smtExternalDept = smtExternalDeptService.getById(Long.valueOf(smtStaff.getDepId()));
			if (Objects.nonNull(smtExternalDept) && Objects.nonNull(smtExternalDept.getDirector()) && smtExternalDept.getDirector().equals(String.valueOf(smtStaff.getId()))) {
				if (smtExternalDeptService.deleteDirector(smtExternalDept.getId())) {
					List<SmtStaff> staffs = this.baseMapper.selectList(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getCompId, smtStaff.getCompId()));
					for (SmtStaff staff : staffs) {
						this.baseMapper.updateReportTo(staff.getId());
					}
				}
			}

			try {
				//删除系统用户
				Boolean userDelRs = remoteUserService.delUserForPlatform(smtStaff.getBadge(), SecurityConstants.FROM_IN).data();
				log.info("删除系统用户:工号：{} 结果:{}", smtStaff.getBadge(), userDelRs);
			} catch (Exception e) {
				log.error("离职同步删除登录信息失败,badge={}", smtStaff.getBadge(), e);
			}

			try {
				List<SmtStaffDeviceAuth> smtStaffDeviceAuths = smtStaffDeviceAuthService.list(Wrappers.<SmtStaffDeviceAuth>query()
						.lambda().eq(SmtStaffDeviceAuth::getStaffId, smtStaff.getId()));

				List<Integer> parkIds = new ArrayList<>();
				List<SmtPark> parkList = smtParkBuService.getParkListByBu(Long.parseLong(smtStaff.getCompId()));
				for (SmtPark park : parkList) {
					parkIds.add(park.getId());
				}

				if (parkIds.size() < 1) {
					SmtOrganizeRelation relation = smtOrganizeRelationService.getByBu(Long.valueOf(smtStaff.getCompId()));
					if (Objects.nonNull(relation)) {
						parkIds.add(relation.getParkId());
					}
				}

				//员工分配了通过权限
				if (CollUtil.isNotEmpty(smtStaffDeviceAuths) && parkIds.size() > 0) {
					smtStaffDeviceAuths.forEach(a -> {
						//设备权限表
						List<SmtDeviceAuthorityRelation> deviceAuthList = smtDeviceAuthorityRelationService
								.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>()
										.eq(SmtDeviceAuthorityRelation::getAuthorityId, a.getAuthId()));
						log.info("查询设备权限: " + deviceAuthList);
						//删除车辆权限
						deleteVehicle(smtStaff.getId(), parkIds);
						//删除人员权限
						savePersonCardTask(DeviceTaskConstants.DEL, DateUtil.currentSeconds(), DateUtil.currentSeconds(), smtStaff, deviceAuthList);
						//删除权限组
						smtStaffDeviceAuthService.remove(Wrappers.<SmtStaffDeviceAuth>query()
								.lambda().eq(SmtStaffDeviceAuth::getStaffId, smtStaff.getId()));
					});
					log.info("人员离职权限删除，staffId={}", smtStaff.getId());
				} else {
					log.info("查询园区结果: {}", parkIds);
				}
			} catch (Exception e) {
				log.error("离职同步删除人员门禁信息失败,badge={}", smtStaff.getBadge(), e);
			}
		}
	}

	// 删除车辆及权限
	private void deleteVehicle(long staffId, List<Integer> parkIds) {
		List<SmtVehicleStaff> list = smtVehicleStaffService.list(Wrappers.<SmtVehicleStaff>query().lambda().eq(SmtVehicleStaff::getStaffId, staffId));
		if (CollectionUtil.isNotEmpty(list)) {
			for (SmtVehicleStaff vehicle : list) {
				smtVehicleService.deleteVehicle(vehicle.getVehicleId(), parkIds);
			}
		}
	}

	@Override
	public boolean synStaffFaceImage(String badge) {
		boolean isSuccess = false;
		SmtStaff smtStaff = this.getSimpleSttaffByBadge(badge);
		if (Objects.isNull(smtStaff)) {
			return Boolean.FALSE;
		}
		String faceImgBase64 = null;
		try {
			faceImgBase64 = smtImageService.getImageBase64ByCode(smtStaff.getFacePicId());
		} catch (Exception e) {
			log.error("下载员工头像异常", e);
		}

		// 下载人脸照片失败退出
		if (StringUtil.isNullOrEmpty(faceImgBase64)) {
			return isSuccess;
		}

		try {
			// 同步到裕同C6表
			Result<Boolean> updateHeadImageRs = remoteRsEmpPhotoService.updateHeadImage(new UpdateHeadImageReqDTO(badge, faceImgBase64),
					SecurityConstants.FROM_IN);
			log.info("更新员工头像,同步到裕同C6表={},badge={}", updateHeadImageRs, badge);

			isSuccess = true;
		} catch (Exception e) {
			log.error("同步员工头像异常", e);
			// 失败记录到信息收集表
			AddIdCollectDto addIdCollectDto = new AddIdCollectDto();
			addIdCollectDto.setBadge(badge);
			addIdCollectDto.setFacePhoto(faceImgBase64);
			addIdCollectDto.setPhotoSyncFla(EmpImgSyncEnum.FAILD.getCode());

			Result<Boolean> saveFaceCollectRs = remoteAppPerfectService.saveFaceCollect(addIdCollectDto);
			log.info("收集员工信息-人脸={},badge={}", saveFaceCollectRs, badge);
		}

		return isSuccess;
	}

	@Override
	public Boolean syncIscPersonFace(String badge, Integer parkId, String imageId) {
		SmtStaff staff = this.getSimpleSttaffByBadge(badge);
		byte[] faceImg = null;
		String facePicId = StringUtils.isNotEmpty(imageId) ? imageId : staff == null ? null : staff.getFacePicId();
		if (StringUtils.isNotEmpty(facePicId)) {
			try {
				faceImg = smtImageService.getImageBinaryByCode(facePicId);
			} catch (Exception e) {
				log.error("读取员工ISC同步人脸图片异常，工号：{}，图片：{}", badge, facePicId, e);
			}
			if (faceImg == null || faceImg.length == 0) {
				return iscPersonService.updateISCPersonFace(badge, parkId, null, facePicId);
			}
		}
		return iscPersonService.updateISCPersonFace(badge, parkId, faceImg, facePicId);
	}

	@Override
	public Boolean retryFailedIscPersonFaceSync() {
		iscPersonService.retryFailedPersonFaceSync();
		return Boolean.TRUE;
	}

	@Override
	public List<CheckFacePicVO> checkFacePic(CheckFacePicDTO check) {
		// TODO Auto-generated method stub
		if (check.getFacePicUpLoad().size() <= 0) {
			throw new TCEException("请选择上传图片");
		}
		List<CheckFacePicVO> voList = new ArrayList<CheckFacePicVO>();
		for (CheckFacePicVO string : check.getFacePicUpLoad()) {
			CheckFacePicVO vo = new CheckFacePicVO();
			vo.setStaffBadge(string.getStaffBadge());
			vo.setStatus(2);
			SmtStaff one = this.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, string.getStaffBadge()).eq(Objects.nonNull(check.getCompId()), SmtStaff::getCompId, check.getCompId()));
			if (ObjectUtil.isNotNull(one)) {
				vo.setStaffName(one.getName());
				if (ObjectUtil.isNotNull(one.getFacePicId())) {
					vo.setStatus(0);
				} else {
					vo.setStatus(1);
				}
			}
			voList.add(vo);
		}
		return voList;
	}

	@Transactional
	@Override
	public String upload(CheckFacePicDTO check) {
		if (check.getFacePicUpLoad().size() <= 0) {
			throw new TCEException("请选择上传图片");
		}
		//上传成功数量
		int scuuCount = 0;
		String taskNum = UUID.randomUUID().toString();
		for (CheckFacePicVO checkFacePicVO : check.getFacePicUpLoad()) {
			SmtStaff staff = this.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, checkFacePicVO.getStaffBadge()));
			if (ObjectUtil.isNotNull(staff)) {
				Integer recordId = createStaffPhotoUploadRecord(staff);
				try {
					log.info("添加或修改人脸照片,并下发图片到设备");
					String facePicId = smtImageService.saveImage(0, checkFacePicVO.getFacePic(), SmtImageEnum.TYPE_STAFF_FACE.getCode());
					staff.setFacePicId(facePicId);
					//更新人脸图片
					this.updateById(staff);

					updatePersonCard(staff, checkFacePicVO.getFacePic(), facePicId, null, taskNum,null);
					//更新人脸并上传人脸到c6
					faceStorage(staff, facePicId, recordId, facePicId);
					scuuCount++;
				} catch (Exception e) {
					log.error("上传头像同步删除人员门禁信息失败,badge={}", staff.getBadge(), e);
				}
			}
		}
		return taskNum;
	}

	@Override
	public Integer createStaffPhotoUploadRecord(SmtStaff one) {
		SmtStaffPhotoUploadRecord record = new SmtStaffPhotoUploadRecord();
		one.setCreateTime(null);
		BeanUtil.copyProperties(one, record);
		record.setCreateTime(LocalDateTime.now());
		record.setStatus(0);
		record.setCreateUser(SecurityUtils.getUser().getUsername());
		if (record.insert()) {
			return record.getId();
		} else {
			throw new TCEException("上传人脸库失败");
		}
	}

	/**
	 * 添加或修改人脸卡片
	 *
	 * @param staff
	 * @param faceImage
	 * @param facePicId
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public String updatePersonCard(SmtStaff staff, String faceImage, String facePicId, List<SmtStaffDeviceAuth> staffDeviceAuths,
								   String taskNum, String applyBadge) {
		int count = 0;
		String remark = null;
		//查询员工的设备权限记录
		if (CollUtil.isEmpty(staffDeviceAuths)) {
			staffDeviceAuths = smtStaffDeviceAuthService.list(new LambdaQueryWrapper<SmtStaffDeviceAuth>()
					.eq(SmtStaffDeviceAuth::getStaffId, staff.getId()));
		}
		if (CollectionUtil.isNotEmpty(staffDeviceAuths)) {
			for (SmtStaffDeviceAuth staffDeviceAuth : staffDeviceAuths) {
				//查询权限配置对应的设备列表
				List<SmtDeviceAuthorityRelation> deviceAuthList = smtDeviceAuthorityRelationService
						.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>()
								.eq(SmtDeviceAuthorityRelation::getAuthorityId, staffDeviceAuth.getAuthId())
						);
				for (int i = 0; i < deviceAuthList.size(); i++) {
					String deviceCode = deviceAuthList.get(i).getDeviceId();
					DeviceTaskActionEnum deviceTaskActionEnum;
					SmtDevice device = smtDeviceService.getById(deviceCode);
					log.info("权限下发-设备信息:{}", device);
					if (Objects.isNull(device)) {
						continue;
					}
					if (Objects.isNull(device.getIsSync())) {
						continue;
					}
					//生成随机序列
					String sNo = UUID.randomUUID().toString().replaceAll("-", "");
					String cardNo = staff.getId().toString();
					if (DeviceSyncEnum.YES.getCode().equals(device.getIsSync())) {
						//ISC设备
							SmtIscDownRecord smtIscDownRecord = smtIscDownRecordService.getOne(new LambdaQueryWrapper<SmtIscDownRecord>()
									.eq(SmtIscDownRecord::getDeviceCode, deviceCode)
									.eq(SmtIscDownRecord::getCardNo, cardNo)
									.eq(SmtIscDownRecord::getDeviceType, DeviceTaskConstants.CARD)
									.eq(SmtIscDownRecord::getServiceType, DeviceTaskConstants.CARD_STAFF_IMPORT)
							);
						if (null == smtIscDownRecord) {
							//生成下发任务
							deviceTaskActionEnum = DeviceTaskActionEnum.DOWN;

						} else {
							//生成修改任务
							deviceTaskActionEnum = DeviceTaskActionEnum.UPDATE;
						}
					} else {
						//非ISC设备
						//查询是否存在下发记录
						SmtTaskDownRecord taskDownRecord = smtTaskDownRecordService.getOne(new LambdaQueryWrapper<SmtTaskDownRecord>()
								.eq(SmtTaskDownRecord::getDeviceCode, deviceCode)
								.eq(SmtTaskDownRecord::getCardNo, cardNo)
								.eq(SmtTaskDownRecord::getDeviceType, DeviceTaskConstants.CARD)
								.eq(SmtTaskDownRecord::getServiceType, DeviceTaskConstants.CARD_STAFF_IMPORT)
						);

						if (null == taskDownRecord) {
							//生成下发任务
							deviceTaskActionEnum = DeviceTaskActionEnum.DOWN;
						} else {
							//生成修改任务
							deviceTaskActionEnum = DeviceTaskActionEnum.UPDATE;
						}
					}
						//保存下发任务
						String taskId = addDeviceTask(deviceCode, cardNo, staff.getBadge() + SymbolConstants.MINUS + staff.getName(),
								facePicId, DeviceTaskStatusEnum.INIT.getCode(), sNo, deviceTaskActionEnum, applyBadge, device);
					// 如果taskNum不为空
					if (StringUtils.isNotEmpty(taskNum)) {
						// 如果taskId是数字
						if (RegexUtils.matchNumber(taskId)) {
							// 创建设备任务详情实体类
							SmtDeviceTaskDetail deviceTaskDetail = SmtDeviceTaskDetail.builder()
									.status(DeviceTaskStatusEnum.INIT.getCode())
									.action(deviceTaskActionEnum.getCode())
									.taskId(taskId)
									.badge(staff.getBadge()).name(staff.getName())
									.createTime(LocalDateTime.now())
									.taskListId(taskNum).build();
							// 插入设备任务详情
							deviceTaskDetail.insert();
						} else {
							// 创建设备任务详情实体类
							SmtDeviceTaskDetail deviceTaskDetail = SmtDeviceTaskDetail.builder()
									.status(DeviceTaskStatusEnum.FAIL.getCode())
									.action(deviceTaskActionEnum.getCode())
									.remark(taskId)
									.badge(staff.getBadge()).name(staff.getName())
									.createTime(LocalDateTime.now())
									.taskListId(taskNum).build();
							// 插入设备任务详情
							deviceTaskDetail.insert();
						}
					}
				}
			}
		} else {
			remark = "人员设备权限为空";
			if (StringUtils.isNotEmpty(taskNum)) {
				SmtDeviceTaskDetail deviceTaskDetail = SmtDeviceTaskDetail.builder()
						.action(DeviceTaskActionEnum.DOWN.getCode())
						.status(DeviceTaskStatusEnum.CANCEL.getCode())
						.remark(remark)
						.badge(staff.getBadge()).name(staff.getName())
						.createTime(LocalDateTime.now()).taskListId(taskNum).build();
				deviceTaskDetail.insert();
			}
		}


		log.info("更改卡片的count:" + count);

		return remark;
	}

	/**
	 * 保存下发任务
	 *
	 * @param deviceCode
	 * @param cardNo
	 * @param general
	 * @param facePicId
	 * @param status
	 */
	private String addDeviceTask(String deviceCode, String cardNo, String general, String facePicId, Integer status,
								 String serialNo, DeviceTaskActionEnum taskActionEnum, String applyBadge, SmtDevice device) {
		DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
		deviceTaskVO.setAction(taskActionEnum.getCode());
		deviceTaskVO.setServiceType(personCardServiceType(taskActionEnum.getCode(), device));
		deviceTaskVO.setDeviceCode(deviceCode);
		deviceTaskVO.setCardNo(cardNo);
		deviceTaskVO.setCardType(SmtVisitorEnum.CAR_CARD_TYPE_1.getType());
		deviceTaskVO.setGeneral(general);
		deviceTaskVO.setImageId(facePicId);
		deviceTaskVO.setDeviceType(DeviceTaskConstants.CARD);
		deviceTaskVO.setOverTime(DeviceTaskConstants.maxTime);
		deviceTaskVO.setStartTime(DateUtil.currentSeconds());
		deviceTaskVO.setStatus(status);
		deviceTaskVO.setSerialNo(serialNo);
		deviceTaskVO.setApplyBadge(applyBadge);
		return smtDeviceTaskService.saveTask(deviceTaskVO);
	}

	@Override
	public void faceStorage(SmtStaff staff, String facePicId, Integer recordId, String faceImage) {

		staff.setFacePicId(facePicId);
		boolean updateById = staff.updateById();
		SmtStaffPhotoUploadRecord byId = staffPhotoUploadRecordService.getById(recordId);
		byId.setStatus(1);
		byId.setFacePicId(facePicId);
		byId.updateById();
		if (updateById && EmpTypeEnum.TYPE3.getCode().equals(staff.getEmpType())) {
			Result<Boolean> updateHeadImageRs = remoteRsEmpPhotoService.updateHeadImage(new UpdateHeadImageReqDTO(staff.getBadge(), faceImage),
					SecurityConstants.FROM_IN);
			log.info("上传派遣员工头像,同步到裕同C6表={},badge={}", updateHeadImageRs, staff.getBadge());
		}
		log.info("更新状态【{}】，员工类型【{}】", updateById, staff.getEmpType());
	}


	@Override
	public ToC6ePhoto toC6ePhoto(ToC6ePhoto toC6ePhoto) {
		// TODO Auto-generated method stub

		if (ObjectUtil.isNull(toC6ePhoto)) {
			throw new TCEException("缺少员工号参数empNo");
		}
		if (ObjectUtil.isNull(toC6ePhoto.getEmpNo())) {
			throw new TCEException("缺少员工号参数");
		}

		SmtStaff one = this.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, toC6ePhoto.getEmpNo()));
		if (ObjectUtil.isNull(one)) {
			throw new TCEException("此员工不存在");
		}
		String photoId = one.getFacePicId();
		String blob = smtImageService.getImageBase64ByCode(photoId);
		if (!StringUtil.isNullOrEmpty(blob)) {
			toC6ePhoto.setPhoto(blob);
		} else {
			throw new TCEException("获取员工图片失败");
		}
		return toC6ePhoto;
	}

	@Override
	public IPage<SmtStaff> getTempList(Page page, TempStaffEditReqDTO reqDTO) {
		Integer userId = SecurityUtils.getUser().getId();
		if (Objects.nonNull(reqDTO.getStatus()) && reqDTO.getStatus().equals(StaffStatusEnum.STAFF_STATUS_IN.getCode())) {
			reqDTO.setStatus(StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode());
		}
		List<String> badges = new ArrayList<>();
		SmtOrganizeRelation organizeRelation = smtOrganizeRelationService.getByUserId(userId);
		if (Objects.isNull(organizeRelation)) {
			throw new SmartException("您未关联企业，无法查看人员信息");
		}
		Long deptId = reqDTO.getDepId();
		Long buId = organizeRelation.getId();
		//判断传入工号是否存在数据
		if (StrUtil.isNotBlank(reqDTO.getBadges())) {
			badges = ToolUtils.splitStr(reqDTO.getBadges());
			List<String> useBadges = new ArrayList<>();
			useBadges.addAll(badges);
			List<SmtStaff> staffs = this.list(Wrappers.<SmtStaff>query().lambda()
					.eq(Objects.nonNull(reqDTO.getStatus()), SmtStaff::getStatus, reqDTO.getStatus())
					.eq(SmtStaff::getCompId, buId)
					.in(CollUtil.isNotEmpty(badges), SmtStaff::getBadge, badges));
			List<String> staffBadges = staffs.stream().map(SmtStaff::getBadge).collect(Collectors.toList());
			useBadges.removeAll(staffBadges);
			if (CollUtil.isNotEmpty(useBadges)) {
				throw new SmartException("输入工号：" + StrUtil.join(SymbolConstants.COMMA, useBadges) + "在该BU不存在或未离职");
			}
		}

		IPage<SmtStaff> queryRsPo = this.page(page, Wrappers.<SmtStaff>query().lambda()
				.eq(Objects.nonNull(buId), SmtStaff::getCompId, Objects.nonNull(buId) ? buId.toString() : null)
				.eq(Objects.nonNull(reqDTO.getStatus()), SmtStaff::getStatus, reqDTO.getStatus())
				.like(StringUtils.isNotBlank(reqDTO.getBadge()), SmtStaff::getBadge, reqDTO.getBadge())
				.like(StringUtils.isNotBlank(reqDTO.getName()), SmtStaff::getName, reqDTO.getName())
				.in(CollUtil.isNotEmpty(badges), SmtStaff::getBadge, badges)
				.isNotNull(Objects.nonNull(reqDTO.getIsFace()) && reqDTO.getIsFace(), SmtStaff::getFacePicId)
				.isNull(Objects.nonNull(reqDTO.getIsFace()) && !reqDTO.getIsFace(), SmtStaff::getFacePicId)
				.eq(Objects.nonNull(deptId), SmtStaff::getDepId, Objects.nonNull(deptId) ? deptId.toString() : null)
				.orderByDesc(SmtStaff::getCreateTime).orderByDesc(SmtStaff::getId));
		return queryRsPo;
	}

	@Transactional
	@Override
	public Boolean updateTemporaryStaff(TempStaffEditReqDTO tempStaff) {
		SmtStaff smtStaff = BeanUtils.transform(SmtStaff.class, tempStaff);
		SmtStaff reStaff = this.getSimpleSttaffByBadge(tempStaff.getBadge());
		if (Objects.nonNull(reStaff) && !reStaff.getId().equals(smtStaff.getId())) {
			throw new SmartException("该工号已存在！");
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		try {
			smtStaff.setCreateTime(df.parse(tempStaff.getEntryTime()));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (!reStaff.getPhone().equals(tempStaff.getPhone())) {
			//手机号已修改 更新登录用户表信息
			// 修改sys_user 表数据
			UserDTO needUpdate = new UserDTO();
			needUpdate.setUsername(tempStaff.getBadge());
			needUpdate.setPhone(tempStaff.getPhone());
			needUpdate.setRole(SecurityUtils.getRoles());
			Result<Boolean> booleanResult = remoteUserService.updateUserInfo(needUpdate,SecurityConstants.FROM_IN);
			if (!booleanResult.isSuccess()) {
				throw new TCEException("修改登录信息异常");
			}
		}

		smtStaff.setDepId(tempStaff.getDepId().toString());
		SmtExternalDept dept = smtExternalDeptService.getById(tempStaff.getDepId());
		smtStaff.setCompId(String.valueOf(dept.getCompId()));
		if (StringUtils.isNotEmpty(dept.getDirector())) {
			SmtStaff staff = this.getById(dept.getDirector());
			if (Objects.nonNull(staff)) {
				smtStaff.setReportTo(staff.getBadge());
			}
		}
		SysDict dict = remoteDictService.findByValue(DictConstants.JOB_LEVEL, tempStaff.getJcheId(), SecurityConstants.FROM_IN).data();
		if (Objects.isNull(dict)) {
			throw new SmartException("获取级层字典表失败");
		}
		smtStaff.setJcheName(dict.getLabel());
		Boolean flag = false;
		if (reStaff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode())) {
			//员工原状态是离职，修改为在职,原权限已删除，现为新增
			flag = true;
		}
		if (StrUtil.isEmpty(reStaff.getFacePicId())) {
			//员工原本无人脸图，权限也为新增
			flag = true;
		}
		if (tempStaff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_IN.getCode())) {
			smtStaff.setStatus(StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode());
		}
		this.updateById(smtStaff);
		if (tempStaff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode())) {
			//离职处理
			smtStaff.setFacePicId(reStaff.getFacePicId());
			this.synDeleteUserInfo(smtStaff);
			return true;
		}
		return this.updateTempStaffAuth(tempStaff, flag);
	}

	/**
	 * 员工信息更新全新
	 *
	 * @param tempStaff
	 * @param flag
	 * @return
	 */
	public Boolean updateTempAuth(SmtStaff tempStaff, Boolean flag) {
		//更新人脸照片
		if (StringUtils.isNotEmpty(tempStaff.getFacePicId())) {
			this.sendTempStaffDevice(tempStaff, flag);
		}
		//更新app权限
		return this.updateAppAuth(tempStaff.getId());
	}

	public Boolean updateTempStaffAuth(TempStaffEditReqDTO tempStaff, Boolean flag) {
		//更新人脸照片
		if (StringUtils.isNotEmpty(tempStaff.getFaceImg())) {
			this.editFaceImg(tempStaff, flag);
		}
		//更新app权限
		return this.updateAppAuth(tempStaff.getId());
	}

	private Boolean updateAppAuth(Long staffId) {
		SmtAppAuth smtAppAuth = smtAppAuthService.getOne(Wrappers.<SmtAppAuth>query().lambda().eq(SmtAppAuth::getAuthName, tempAppAuth));
		if (Objects.isNull(smtAppAuth)) {
			throw new SmartException("未配置默认合作企业APP权限");
		}
		Integer[] auth = new Integer[]{smtAppAuth.getId()};
		//更新app权限
		SmtAppStaffAuthSaveAO appStaffAuthSaveAO = new SmtAppStaffAuthSaveAO();
		appStaffAuthSaveAO.setAuthId(auth);
		appStaffAuthSaveAO.setStaffId(String.valueOf(staffId));
		appStaffAuthService.updateStaffAuth(appStaffAuthSaveAO);
		return Boolean.TRUE;
	}

	private Boolean editFaceImg(TempStaffEditReqDTO tempStaff, Boolean flag) {
		//查询员工信息
		SmtStaff queryRsPo = this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, tempStaff.getBadge()));
		if (Objects.isNull(queryRsPo)) {
			throw new TCEException("获取到员工信息异常");
		}
		String facePicId = null;
		String faceImg = tempStaff.getFaceImg();
		try {
			if (!StringUtil.isNullOrEmpty(faceImg)) {
				facePicId = smtImageService.saveImage(0, faceImg, SmtImageEnum.TYPE_STAFF_FACE.getCode());
				if (StringUtils.isBlank(facePicId)) {
					throw new TCEException("保存人脸图片异常");
				}
			}
		} catch (TCEException tce) {
			throw tce;
		} catch (Exception e) {
			log.error("保存图片异常", e);
			throw new TCEException("保存图片异常");
		}

		//更新人脸、身份证证照片信息
		SmtStaff updateSmtStaff = new SmtStaff();
		updateSmtStaff.setId(queryRsPo.getId());
		updateSmtStaff.setCompId(queryRsPo.getCompId());
		updateSmtStaff.setBadge(queryRsPo.getBadge());
		updateSmtStaff.setFacePicId(facePicId);
		updateSmtStaff.setName(queryRsPo.getName());
		//更新信息
		this.updateById(updateSmtStaff);
		return this.sendTempStaffDevice(updateSmtStaff, flag);
	}

	private Boolean sendTempStaffDevice(SmtStaff updateSmtStaff, Boolean flag) {

		//下发闸机
		if (StringUtils.isEmpty(updateSmtStaff.getFacePicId())) {
			return Boolean.FALSE;
		}
		if (flag) {
			addDeviceTask(updateSmtStaff, DeviceTaskActionEnum.DOWN.getCode());
		} else {
			addDeviceTask(updateSmtStaff, DeviceTaskActionEnum.UPDATE.getCode());
		}

		return Boolean.TRUE;
	}

	@Override
	public Boolean saveTemporaryStaff(TempStaffEditReqDTO tempStaff, SmtDormitoryStaffService dormitoryStaffService) {
		Integer userId = SecurityUtils.getUser().getId();
		SmtOrganizeRelation organizeRelation = smtOrganizeRelationService.getByUserId(userId);
		if (Objects.isNull(organizeRelation)) {
			throw new SmartException("您尚未登记企业");
		}
		return this.insertTempStaff(tempStaff, organizeRelation, dormitoryStaffService);
	}

	@Transactional(rollbackFor = Exception.class)
	public Boolean insertTempStaff(TempStaffEditReqDTO tempStaff, SmtOrganizeRelation organizeRelation, SmtDormitoryStaffService dormitoryStaffService) {
		SmtStaff staff = BeanUtils.transform(SmtStaff.class, tempStaff);
		SmtStaff reStaff = this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, tempStaff.getBadge()));
		if (Objects.nonNull(reStaff)) {
			throw new SmartException("该工号已存在！");
		}

		SysDict dict = remoteDictService.findByValue(DictConstants.JOB_LEVEL, tempStaff.getJcheId(), SecurityConstants.FROM_IN).data();
		if (Objects.isNull(dict)) {
			throw new SmartException("获取级层字典表失败");
		}
		staff.setJcheName(dict.getLabel());
		staff.setCompId(String.valueOf(organizeRelation.getId()));
		staff.setCompName(organizeRelation.getCompName());
		try {
			//可能没有对应的福利层级
			Result<EvwCcdFlstandardDTO> flstandardDTOResult = remoteEvwCcdFlstandardService.getById(tempStaff.getJcheId(), null, SecurityConstants.FROM_IN);
			staff.setWelfareLevel(flstandardDTOResult.getData().getCode());
		} catch (Exception e) {
		}
		if (RegexUtils.matchCento(tempStaff.getCertno())) {
			Integer idNOToAge = StaffUtil.idNoToAge(tempStaff.getCertno());
			staff.setAge(idNOToAge);
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		try {
			staff.setCreateTime(df.parse(tempStaff.getEntryTime()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		staff.setDepId(tempStaff.getDepId().toString());
		SmtExternalDept dept = smtExternalDeptService.getById(tempStaff.getDepId());
		if (StringUtils.isNotEmpty(dept.getDirector())) {
			SmtStaff smtStaff = this.getById(dept.getDirector());
			if (Objects.nonNull(smtStaff)) {
				staff.setReportTo(smtStaff.getBadge());
			}
		}
		if (tempStaff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_IN.getCode())) {
			staff.setStatus(StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode());
		}

		//查询临时人员组织是否在排除列表中
		Boolean wasExcludeOrg = smtOrganizeRelationService.wasExcludeOrg(dept.getCompId());

		//使用身份证查询员工信息
		SmtStaff smtStaff1 = null;
		if (StringUtils.isNotEmpty(tempStaff.getCertno()) && !wasExcludeOrg) {
			smtStaff1 = this.getOne(new LambdaQueryWrapper<SmtStaff>()
					.eq(SmtStaff::getBadge, tempStaff.getCertno())
					.eq(SmtStaff::getStatus, StaffStatusEnum.UNKNOWN.getCode())
			);
		}

		if (smtStaff1 != null) {
			//先入住 后添加的情况
			staff.setId(smtStaff1.getId());

			//查询是否存在入住记录
			SmtDormitoryStaff dormitoryStaff = dormitoryStaffService.getOne(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getStaffBadge, staff.getCertno()));
			if (dormitoryStaff != null) {
				//更新入住信息
				dormitoryStaffService.updateDormitoryStaffTemp(staff);
				//设置为已入住
				staff.setDormitoryStatus(DormitoryStatusEnum.NOT_INNER.getCode());
			}

			staff.setBadge(tempStaff.getBadge());

			this.updateById(staff);

		} else {
			staff.insert();
		}

		if (tempStaff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode())) {
			//如果是已离职 直接写入员工表 后续的添加权限等操作不再执行
			return true;
		}
		//添加员工权限
		this.addStaffDeviceAuth(staff);
		tempStaff.setId(staff.getId());
		saveToC6(staff, organizeRelation);
		//更新APP权限
		return this.updateTempStaffAuth(tempStaff, true);

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean batchUpdateTempStatus(List<String> tempStaffs) {
		Integer userId = SecurityUtils.getUser().getId();
		SmtOrganizeRelation organizeRelation = smtOrganizeRelationService.getByUserId(userId);
		if (Objects.isNull(organizeRelation)) {
			throw new SmartException("您尚未登记企业");
		}
		for (String staffId : tempStaffs) {
			SmtStaff staff = this.getById(Long.parseLong(staffId));
			staff.setStatus(StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode());
			this.updateById(staff);

			//添加员工权限
			this.addStaffDeviceAuth(staff);
			//恢复c6状态
			intoC6(staff, organizeRelation);
			//下发设备权限与更新APP权限
			updateTempAuth(staff, true);
		}
		return Boolean.TRUE;
	}

	/**
	 * 离职C6
	 *
	 * @param staff
	 * @param organizeRelation
	 */
	public void leaveToC6(SmtStaff staff, SmtOrganizeRelation organizeRelation) {
		if (TempCompTypeEnum.PAI_QIAN.getCode().equals(organizeRelation.getCompType())) {
			//如果是派遣工 添加到C6
			//通过派遣工部门Id查询对应的C6部门编号
			SmtExDeptC6 exDeptC6 = new SmtExDeptC6();
			exDeptC6 = exDeptC6.selectOne(new LambdaQueryWrapper<SmtExDeptC6>()
					.eq(SmtExDeptC6::getDId, staff.getDepId())
			);
			if (null != exDeptC6) {
				if (syParkId.equals(organizeRelation.getParkId())) {
					//石岩C6
					remoteRsEmpService.leaveEmp(RsEmpSaveReqDto.builder()
							.empNo(staff.getBadge())
							.build(), SecurityConstants.FROM_IN);
				} else if (xcParkId.equals(organizeRelation.getParkId())) {
					//许昌C6
					remoteXCRsEmpService.leaveEmp(RsEmpSaveReqDto.builder()
							.empNo(staff.getBadge())
							.build(), SecurityConstants.FROM_IN);
				}
			}
		}
	}

	/**
	 * C6离职转在职
	 *
	 * @param staff
	 * @param organizeRelation
	 */
	public void intoC6(SmtStaff staff, SmtOrganizeRelation organizeRelation) {
		if (TempCompTypeEnum.PAI_QIAN.getCode().equals(organizeRelation.getCompType())) {
			SmtExDeptC6 exDeptC6 = new SmtExDeptC6();
			exDeptC6 = exDeptC6.selectOne(new LambdaQueryWrapper<SmtExDeptC6>()
					.eq(SmtExDeptC6::getDId, staff.getDepId())
			);
			if (null != exDeptC6) {
				if (syParkId.equals(organizeRelation.getParkId())) {
					//石岩C6
					remoteRsEmpService.intoEmp(RsEmpSaveReqDto.builder()
							.empNo(staff.getBadge())
							.build(), SecurityConstants.FROM_IN);
				} else if (xcParkId.equals(organizeRelation.getParkId())) {
					//许昌C6
					remoteXCRsEmpService.intoEmp(RsEmpSaveReqDto.builder()
							.empNo(staff.getBadge())
							.build(), SecurityConstants.FROM_IN);
				}
			}
		}
	}

	@Override
	public void saveToC6(SmtStaff staff, SmtOrganizeRelation organizeRelation) {
		if (TempCompTypeEnum.PAI_QIAN.getCode().equals(organizeRelation.getCompType())) {
			log.info("派遣工同步到C6: {}", JSONUtil.toJsonStr(staff));
			//如果是派遣工 添加到C6
			//通过派遣工部门Id查询对应的C6部门编号
			SmtExDeptC6 exDeptC6 = new SmtExDeptC6();
			exDeptC6 = exDeptC6.selectOne(new LambdaQueryWrapper<SmtExDeptC6>()
					.eq(SmtExDeptC6::getDId, staff.getDepId())
			);
			if (null != exDeptC6) {
				if (syParkId.equals(organizeRelation.getParkId())) {
					//推送到石岩C6
					remoteRsEmpService.saveEmp(RsEmpSaveReqDto.builder()
							.empNo(staff.getBadge())
							.empName(staff.getName())
							.empSex(null == staff.getSex() ? 0 : staff.getSex() + 1)            //园区系统 0.男 1.女   C6 1.男 2.女
							.empIDNo(staff.getCertno())
							.dptNo(exDeptC6.getC6DptNo())
							.grpDate(staff.getCreateTime())
							.build(), SecurityConstants.FROM_IN);
				} else if (xcParkId.equals(organizeRelation.getParkId())) {
					//推送到许昌C6
					remoteXCRsEmpService.saveEmp(RsEmpSaveReqDto.builder()
							.empNo(staff.getBadge())
							.empName(staff.getName())
							.empSex(null == staff.getSex() ? 0 : staff.getSex() + 1)            //园区系统 0.男 1.女   C6 1.男 2.女
							.empIDNo(staff.getCertno())
							.dptNo(exDeptC6.getC6DptNo())
							.grpDate(staff.getCreateTime())
							.build(), SecurityConstants.FROM_IN);
				}

			}
		}
	}

	private Boolean setAuth(SmtStaff staff) {
		//添加员工权限
		addStaffDeviceAuth(staff);
		List<SmtPark> parkList = smtParkBuService.getParkListByBu(Long.parseLong(staff.getCompId()));
		if (CollUtil.isEmpty(parkList)) {
			log.info("员工BU未关联园区,员工ID={},{}", staff.getId(), staff.getCompId());
			return Boolean.FALSE;
		}
		//添加员工默认app权限
		appStaffAuthService.initStaffAuth(staff.getId(), parkList.get(0).getId());
		return Boolean.TRUE;
	}


	@SuppressWarnings("unused")
	@Override
	public Boolean register(StaffRegisterDTO staffRegisterDTO) {
		// TODO Auto-generated method stub

		log.info("staffRegisterDTO:" + staffRegisterDTO);

		if (ObjectUtil.isNull(staffRegisterDTO)) {
			throw new TCEException("员工信息为空，请重新选择");
		}
		if (staffRegisterDTO.getStaffList().size() <= 0) {
			throw new TCEException("员工信息为空，请重新选择");
		}
		if (ObjectUtil.isNull(staffRegisterDTO.getJobId())) {
			throw new TCEException("岗位信息为空，请重新选择岗位");
		}

		Result<OvwYsjobRespDTO> byDeptName = remoteOvwYsjobService.getByDeptName(Integer.parseInt(staffRegisterDTO.getJobId()), SecurityConstants.FROM_IN);
		if (byDeptName.isSuccess()) {
			if (byDeptName.getData() != null) {
				OvwYsjobRespDTO ovwYsjob = byDeptName.getData();
				staffRegisterDTO.setJcheId(ovwYsjob.getJchenID().toString());
				staffRegisterDTO.setJcheName(ovwYsjob.getJchenName());
				staffRegisterDTO.setFlcc(ovwYsjob.getFlCJ());
			}
		}

		List<SmtStaffRegister> list = staffRegisterDTO.getStaffList();
        /*String errorCento = "";
        for (SmtStaffRegister staffRegister : list) {
            String cento = staffRegister.getCertno();
            if (!RegexUtils.matchCento(cento)) {
                errorCento = errorCento + cento + ",";
            }
        }
        if (!"".equals(errorCento)) {
            errorCento = errorCento.substring(0, errorCento.length() - 1);
            throw new TCEException("证件号'" + errorCento + "'不合法，请核对后重新登记");
        }
        for (SmtStaffRegister staffRegister : list) {
            String cento = staffRegister.getCertno();
            SmtStaff one = this.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getCertno, cento).ne(SmtStaff::getStatus, StaffStatusEnum.STAFF_STATUS_QUIT.getCode()));
            if (ObjectUtil.isNotNull(one)) {
                errorCento += cento + ",";
            }
        }
        if (!"".equals(errorCento)) {
            errorCento = errorCento.substring(0, errorCento.length() - 1);
            throw new TCEException("证件号为'" + errorCento + "'的员工已入职,请核对后重新登记");
        }*/
		SmtPreStaff preStaff = null;
		for (SmtStaffRegister staffRegister : list) {
			preStaff = new SmtPreStaff();
			String createNewBadge = smtStaffExtService.createNewBadge(staffRegisterDTO.getCompId());
			//String createNewBadge = staffRegister.getCertno();
			if (RegexUtils.matchCento(staffRegister.getCertno())) {
				Integer idNOToAge = StaffUtil.idNoToAge(staffRegister.getCertno());
				preStaff.setAge(idNOToAge);
			}
			preStaff.setBadge(createNewBadge);
			preStaff.setBirth(staffRegister.getBirth());
			preStaff.setCertno(staffRegister.getCertno());
			preStaff.setCompId(staffRegisterDTO.getCompId());
			preStaff.setCompName(staffRegisterDTO.getCompName());
			preStaff.setCreateTime(DateUtil.date());
			preStaff.setDepId(staffRegisterDTO.getDepId());
			preStaff.setDepName(staffRegisterDTO.getDepName());
			preStaff.setHomeAddress(staffRegister.getHomeAddress());
			preStaff.setJcheId(staffRegisterDTO.getJcheId());
			preStaff.setJcheName(staffRegisterDTO.getJcheName());
			preStaff.setJobId(staffRegisterDTO.getJobId());
			preStaff.setJobName(staffRegisterDTO.getJobName());
			preStaff.setNation(staffRegister.getNation());
			preStaff.setName(staffRegister.getName());
			preStaff.setStatus(StaffStatusEnum.STAFF_STATUS_IN.getCode());
			preStaff.setPolice(staffRegister.getPolice());
			preStaff.setSex(staffRegister.getSex());
			preStaff.setWelfareLevel(staffRegisterDTO.getFlcc());
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			try {
				preStaff.setValidDate(dateFormat.parse(staffRegister.getValidDate()));
				preStaff.setValidDateFm(dateFormat.parse(staffRegister.getValidDateFm()));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//staff.insert(); //不再像本库存数据，直接存入中间表，并备份到已入职一份

			//addToApplication(staff,staffRegisterDTO);
			smtStaffExtService.addStaffToHR(preStaff, null, null);
		}
		return true;
	}

	//将快速入职的员工向招聘-已入职备份
	public void addToApplication(SmtStaff staff, StaffRegisterDTO staffRegisterDTO) {
		SmtApplication application = new SmtApplication();
		application.setAge(staff.getAge());
		application.setApplyDate(DateUtil.date());
		application.setBirth(staff.getBirth());
		application.setCertno(staff.getCertno());
		application.setCertnoPicId(staff.getCertnoPicId());
		application.setCreateTime(DateUtil.date());
		application.setFacePicId(staff.getFacePicId());
		application.setHomeAddress(staff.getHomeAddress());
		application.setName(staff.getName());
		application.setSex(staff.getSex());
		application.setPhone(staff.getPhone());
		application.setPolice(staff.getPolice());
		application.setValidDate(staff.getValidDate());
		application.setValidDateFm(staff.getValidDateFm());
		application = BeanUtils.transform(SmtApplication.class, staff);
		application.setStatus(ApplicationStatusEnum.ENTRY_DONE.getCode());
		application.setIsDelete(0);
		application.setParkId(staffRegisterDTO.getParkId());
		application.insert();
	}


	@Override
	public Result getAuthInfo(String id) {
		return this.smtStaffExtService.getAuthInfo(id);
	}

	@Override
	public Page<StaffListVO> getTOStaffPage(Page page, SearchToStaffDTO searchToStaffDTO) {
		// TODO Auto-generated method stub
		Page<StaffListVO> result = this.baseMapper.getTOStaffPage(page, searchToStaffDTO);
		return result;
	}

	@Override
	public Result getToStaffInfoById(String id) {
		// TODO Auto-generated method stub
		StaffInfoVO staffInfoVO = new StaffInfoVO();
		SmtStaff selectById = this.baseMapper.selectById(id);
		staffInfoVO.setSmtStaff(selectById);
		//根据图片id去获取图片base64
		try {
			if (!Objects.isNull(selectById.getFacePicId())) {
				String facePicUrl = imageService.buildImageUrl(selectById.getFacePicId());
				staffInfoVO.setFacePic(facePicUrl);
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.info("获取图片异常");
		}
		List<SmtPark> parkList = smtParkBuService.getParkListByBu(Long.parseLong(selectById.getCompId()));
		String parkName = "";
		for (SmtPark smtPark : parkList) {
			parkName += smtPark.getParkName() + ",";
		}

		if (!parkName.equals("")) {
			parkName = parkName.substring(0, parkName.length() - 1);
		}
		staffInfoVO.setParkName(parkName);
		//获取紧急联系人
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN);

		//获取教育经验
		List<SmtStaffEducation> educations = smtStaffEducationService.list(Wrappers.<SmtStaffEducation>query().lambda().eq(SmtStaffEducation::getStaffId, id));
		Result<List<SysDict>> findEdutionByType = remoteDictService.findByType(DictConstants.EDUCATION_TYPE, SecurityConstants.FROM_IN);
		Result<List<SysDict>> findDegreeByType = remoteDictService.findByType(DictConstants.DEGREE_TYPE, SecurityConstants.FROM_IN);

		for (SmtStaffEducation smtStaffEducation : educations) {
			for (int j = 0; j < findEdutionByType.getData().size(); j++) {
				String value = findEdutionByType.getData().get(j).getValue();
				if (value.equals(smtStaffEducation.getEducation())) {
					smtStaffEducation.setEducation(findEdutionByType.getData().get(j).getLabel());
					break;
				}
			}
			for (int j = 0; j < findDegreeByType.getData().size(); j++) {
				String value = findDegreeByType.getData().get(j).getValue();
				if (value.equals(smtStaffEducation.getDegree())) {
					smtStaffEducation.setDegree(findDegreeByType.getData().get(j).getLabel());
					break;
				}
			}
		}
		staffInfoVO.setEducation(educations);
		//获取工作经验
		List<SmtStaffWork> works = smtStaffWorkService.list(Wrappers.<SmtStaffWork>query().lambda().eq(SmtStaffWork::getStaffId, id));
		staffInfoVO.setWork(works);
		//家庭成员
		List<SmtStaffFamily> family = smtStaffFamilyService.list(Wrappers.<SmtStaffFamily>query().lambda().eq(SmtStaffFamily::getStaffId, id));
		for (SmtStaffFamily smtStaffFamily : family) {
			for (int j = 0; j < findByType.getData().size(); j++) {
				if (smtStaffFamily.getRelation().equals(findByType.getData().get(j).getValue())) {
					smtStaffFamily.setRelation(findByType.getData().get(j).getLabel());
					break;
				}
			}
		}
		staffInfoVO.setFamily(family);
		//获取人事关系
		List<SmtStaffRelation> relations = smtStaffRelationService.list(Wrappers.<SmtStaffRelation>query().lambda().eq(SmtStaffRelation::getStaffId, id));
		for (SmtStaffRelation smtStaffRelation : relations) {
			for (int j = 0; j < findByType.getData().size(); j++) {
				if (smtStaffRelation.getRelation().equals(findByType.getData().get(j).getValue())) {
					smtStaffRelation.setRelation(findByType.getData().get(j).getLabel());
					break;
				}
			}
		}
		staffInfoVO.setRelation(relations);
		return new Result<>(staffInfoVO);
	}

	@Override
	public List<SmtPark> getStaffPark(String staffBadge) {
		// TODO Auto-generated method stub
		SmtStaff one = this.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, staffBadge));
		if (ObjectUtil.isNull(one)) {
			throw new TCEException("员工信息不存在");
		}
		List<SmtPark> parkList = new ArrayList<>();
		if (one.getStatus() == 4) {
			SmtOrganizeRelation organizeRelation = smtOrganizeRelationService.getByBu(Long.valueOf(one.getCompId()));
			if (Objects.nonNull(organizeRelation)) {
				SmtPark smtPark = smtParkService.getById(organizeRelation.getParkId());
				parkList.add(smtPark);
			}
		} else {
			parkList = smtParkBuService.getParkListByBu(Long.parseLong(one.getCompId()));
		}
		return parkList;
	}

	@Override
	public List<SmtStaff> getNewStaff() {
		return baseMapper.getNewStaff();
	}

	@Override
	public List<SmtStaff> getSeniorStaff() {
		return baseMapper.getSeniorStaff();
	}

	@Override
	public List<SmtStaff> getSeniorRechargeStaff() {
		return baseMapper.getSeniorRechargeStaff();
	}

	@Override
	public List<StaffListVO> remoteSyncStaffInfo(Integer parkId, String createTime) {
		return baseMapper.remoteSyncStaffInfo(parkId, createTime);
	}

	@Override
	public List<StaffPartInfo> getStaffInfo(String staffBadge) {
		if ("undefined".equals(staffBadge)) {
			throw new SmartException("请输入工号");
		}
		//取10条
		List<SmtStaff> staffList = baseMapper.getStaffLikeBadge(staffBadge);
		List<StaffPartInfo> result = new ArrayList<>();
		for (SmtStaff smtStaff : staffList) {
			result.add(StaffPartInfo.builder()
					.badge(smtStaff.getBadge())
					.info(MessageFormat.format("{0} {1} {2}", smtStaff.getName(), smtStaff.getBadge(), smtStaff.getDepName()))
					.build()
			);
		}
		return result;
	}

	@Override
	public IPage<EmpHrReqDTO> getStaffList(Page page) {
		IPage records = this.page(page, Wrappers.<SmtStaff>query().lambda()
				.ne(SmtStaff::getStatus, StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode()));
		List<EmpHrReqDTO> empHrReqDTOS = new ArrayList<>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (Object obj : records.getRecords()) {
			try {
				SmtStaff smtStaff = (SmtStaff) obj;
				log.info(smtStaff.toString());
				if (smtStaff.getJobId() != null) {
					if (smtStaff.getCompId() != null && smtStaff.getCompId().length() <= 10) {
						EmpHrReqDTO empHrReqDTO = new EmpHrReqDTO();
						BeanUtils.copyProperties(smtStaff, empHrReqDTO);
						empHrReqDTO.setEid(smtStaff.getEId());
						empHrReqDTO.setPqcompany(smtStaff.getPqcompany());
						empHrReqDTO.setFlcj(smtStaff.getWelfareLevel());
						empHrReqDTO.setGender(smtStaff.getSex());
						empHrReqDTO.setJobid(smtStaff.getJobId());
						empHrReqDTO.setCompID(Integer.parseInt(smtStaff.getCompId()));
						empHrReqDTO.setDepid(Integer.parseInt(smtStaff.getDepId()));
						empHrReqDTO.setCompname(smtStaff.getCompName());
						empHrReqDTO.setJobname(smtStaff.getJobName());
						empHrReqDTO.setEmail(smtStaff.getEmail());
						try {
							if (StrUtil.isNotBlank(smtStaff.getBirth())) {
								empHrReqDTO.setBirthDay(dateFormat.parse(smtStaff.getBirth()));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						try {
							empHrReqDTO.setJoindate(smtStaff.getCreateTime());
						} catch (Exception e) {
							e.printStackTrace();
						}
						empHrReqDTO.setDepname(smtStaff.getDepName());
						empHrReqDTO.setJchenID(Integer.parseInt(smtStaff.getJcheId()));
						empHrReqDTO.setJchenName(smtStaff.getJcheName());
						empHrReqDTO.setLeaType(smtStaff.getLeaType());
						empHrReqDTOS.add(empHrReqDTO);
					}
				}
			} catch (Exception e){
				log.error("getStaffList查询员工数据异常",e);
				throw new SmartException("getStaffList查询员工数据异常");
			}
		}
		records.setRecords(empHrReqDTOS);
		return records;
	}

	@Override
	public Boolean updateStaffPhone(Long staffId, String newPhone) {
		SmtStaff staff = this.getById(staffId);
		if (null == staff) {
			throw new TCEException("员工不存在");
		} else if (StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(staff.getStatus())) {
			throw new TCEException("员工已离职");
		}

		if (StaffStatusEnum.STAFF_STATUS_IN.getCode().equals(staff.getStatus())
				|| StaffStatusEnum.STAFF_STATUS_TTRY.getCode().equals(staff.getStatus())
				|| StaffStatusEnum.STAFF_STATUS_PRACTICE.getCode().equals(staff.getStatus())
		) {
			//裕同正式员工 需修改EHR里存储的手机号码
			Map<String, String> param = new HashMap<>();
			param.put("UserName", staff.getBadge());
			param.put("NewPhone", newPhone);
			param.put("TokenID", updateToken);
			String newUri = UriComponentsBuilder.fromHttpUrl(phoneUpdateUrl)
					.replaceQuery(HttpUtil.toParams(param))
					.build(true)
					.toString();
			HttpResponse response = HttpUtils.createGet(newUri).execute();
			log.info("修改EHR中手机号，响应({})", response.body());
			LoginResult result = HttpUtils.parse(response, LoginResult.class);
			if (!(null != result && result.getType().equals(1) && result.getErrorcode().equals(0))) {
				throw new TCEException("修改EHR中手机号码失败");
			}
		}
		//修改智慧园区系统内员工手机号
		staff.setPhone(newPhone);
		this.updateById(staff);

		// 修改sys_user 表数据
		UserDTO needUpdate = new UserDTO();
		needUpdate.setUsername(staff.getBadge());
		needUpdate.setPhone(newPhone);
		needUpdate.setRole(SecurityUtils.getRoles());
		remoteUserService.updateUserInfo(needUpdate,SecurityConstants.FROM_IN);
		return true;
	}

	@Override
	public SmtStaff getStffNoQuitByBadge(String staffBadge) {
		SmtStaff staff = this.getOne(new LambdaQueryWrapper<SmtStaff>()
				.eq(SmtStaff::getBadge, staffBadge)
		);
		if (null == staff) {
			throw new com.tce.smart.tool.exception.TCEException("员工不存在");
		} else if (StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(staff.getStatus())) {
			throw new com.tce.smart.tool.exception.TCEException("员工已离职");
		}
		return staff;
	}

	@Override
	public String getEmpCard(String empNo) {
		String localCardNo = smtIscStaffCardService.getFirstActiveCardNoByBadge(empNo);
		if (StrUtil.isNotBlank(localCardNo)) {
			return localCardNo;
		}
		return remoteRsEmpService.getEmpCard(empNo, SecurityConstants.FROM_IN).getData();
	}
}
