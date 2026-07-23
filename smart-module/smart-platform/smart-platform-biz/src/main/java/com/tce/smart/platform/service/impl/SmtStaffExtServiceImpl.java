package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.SexType;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.consume.req.RsEmpSaveReqDto;
import com.tce.smart.data.api.dto.ehrview.EvwCcdFlstandardDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsdepRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsjobRespDTO;
import com.tce.smart.data.api.dto.temporary.req.*;
import com.tce.smart.data.api.dto.temporary.resp.OcompanyRespDTO;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwCcdFlstandardService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwEmphrYsService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsdepService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsjobService;
import com.tce.smart.data.api.feign.temporary.*;
import com.tce.smart.platform.api.dto.req.TempStaffEditReqDTO;
import com.tce.smart.platform.core.dto.ApplicationStaffDTO;
import com.tce.smart.platform.core.dto.StaffBadgeDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtPreStaffMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.model.DeviceTree;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.StaffDeviceAuthInfoVO;
import com.tce.smart.platform.core.vo.StaffDeviceAuthListVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.utils.StaffUtil;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.DormitoryStatusEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import com.tce.smart.tool.enums.TempCompTypeEnum;
import com.tce.smart.tool.util.RegexUtils;
import com.tce.smart.tool.util.ToolUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sunfujian
 * @since 2021/9/8 9:17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmtStaffExtServiceImpl extends ServiceImpl<SmtStaffMapper, SmtStaff> implements SmtStaffExtService {

	private final SmtStaffService smtStaffService;
	private final SmtStaffDeviceAuthService smtStaffDeviceAuthService;
	private final SmtDeviceAuthorityService smtDeviceAuthorityService;
	private final SmtBusinessDeviceAuthService smtBusinessDeviceAuthService;
	private final SmtAreaService smtAreaService;
	private final RemoteDictService remoteDictService;
	private final SmtApplicationRelationService applicationRelationService;
	private final RemoteOcompanyService remoteCompanyService;
	private final RemoteEbgeJavoidanceService ebgeJavoidanceService;
	private final SmtApplicationFamilyService appFamilyService;
	private final RemoteEbgFamilyRegisterService familyReg;
	private final SmtApplicationService applicationService;
	private final RemoteOvwYsjobService remoteOvwYsjobService;
	private final SmtImageService smtImageService;
	private final RemoteOvwYsdepService remoteOvwYsdepService;
	private final RemoteEvwEmphrYsService remoteEvwEmphrYsService;
	private final SmtApplicationEmergencyService applicationEmergentyService;
	private final RemoteEstaffRegisterService staffRegister;
	private final SmtRecruitmentService recruitmentService;
	private final SmtApplicationEmailService emailService;
	private final SmtPreStaffMapper smtPreStaffMapper;
	private final SmtApplicationWorkService workService;
	private final RemoteEbgWorkingRegisterService workRegister;
	private final SmtApplicationEducationService educationService;
	private final RemoteEbgEducationRegisterService eduReg;
	private final SmtOrganizeRelationService smtOrganizeRelationService;
	private final SmtExternalDeptService smtExternalDeptService;
	private final RemoteEvwCcdFlstandardService remoteEvwCcdFlstandardService;
	private final SmtAppAuthService smtAppAuthService;
	private final SmtAppStaffAuthService appStaffAuthService;
	private final SmtOrganizeAccessService organizeAccessService;
	private final RemoteRsEmpService remoteRsEmpService;

	/**
	 * 人脸登陆比对阀值
	 */
	@Value("${spring.temp.auth}")
	private String tempAppAuth;

	@Override
	public Result getAuthInfo(String id) {
		List<SmtStaffDeviceAuth> deviceAuthList = smtStaffDeviceAuthService.list(Wrappers.<SmtStaffDeviceAuth>query().lambda().eq(SmtStaffDeviceAuth::getStaffId, id));
		if (CollectionUtil.isEmpty(deviceAuthList)) {
			//没有员工设备权限
			throw new TCEException("没有设备权限");
		}
		List<StaffDeviceAuthInfoVO> authInfoVOList = new ArrayList<>();
		for (SmtStaffDeviceAuth auth : deviceAuthList) {
			StaffDeviceAuthInfoVO vo = new StaffDeviceAuthInfoVO();
			SmtDeviceAuthority byId = smtDeviceAuthorityService.getById(auth.getAuthId());
			vo.setAuthName(byId.getAuthorityName());
			vo.setTypeName("人员");
			vo.setRemark(byId.getRemark());
			List<StaffDeviceAuthListVO> list = this.baseMapper.getStaffDeviceStafff(auth.getId());

			List<DeviceTree> park = new ArrayList<>();
			List<DeviceTree> pArea = new ArrayList<>();
			List<DeviceTree> area = new ArrayList<>();
			List<DeviceTree> device;
			for (StaffDeviceAuthListVO staffDeviceAuthListVO : list) {
				DeviceTree parkTree = new DeviceTree();
				parkTree.setLabel(staffDeviceAuthListVO.getParkName());
				if (!park.contains(parkTree)) {
					park.add(parkTree);
				}
			}
			for (StaffDeviceAuthListVO staffDeviceAuthListVO : list) {
				DeviceTree areaTree = new DeviceTree();

				SmtArea smtArea = smtAreaService.getById(staffDeviceAuthListVO.getPId());
				areaTree.setLabel(smtArea.getAreaName());

				for (DeviceTree deviceTree : park) {
					if (deviceTree.getLabel().equals(staffDeviceAuthListVO.getParkName())) {
						if (!pArea.contains(areaTree)) {
							pArea.add(areaTree);
						}
					}
					deviceTree.setChildren(pArea);
				}
			}
			for (DeviceTree deviceTree : pArea) {
				area = new ArrayList<>();
				for (StaffDeviceAuthListVO staffDeviceAuthListVO : list) {
					DeviceTree areaTree = new DeviceTree();
					areaTree.setLabel(staffDeviceAuthListVO.getAreaName());
					SmtArea smtArea = smtAreaService.getById(staffDeviceAuthListVO.getPId());
					if (deviceTree.getLabel().equals(smtArea.getAreaName())) {
						if (!area.contains(areaTree)) {
							area.add(areaTree);
						}
					}
					deviceTree.setChildren(area);
				}
			}
			for (DeviceTree deviceTree : area) {
				device = new ArrayList<>();
				for (StaffDeviceAuthListVO staffDeviceAuthListVO : list) {
					DeviceTree areaTree = new DeviceTree();
					areaTree.setLabel(staffDeviceAuthListVO.getDeviceName());
					if (deviceTree.getLabel().equals(staffDeviceAuthListVO.getAreaName())) {
						if (!device.contains(areaTree)) {
							device.add(areaTree);
						}
					}
					deviceTree.setChildren(device);
				}
			}
			vo.setChildren(park);
			authInfoVOList.add(vo);
		}
		return new Result<>(authInfoVOList);
	}

	@Override
	public String createNewBadge(String compId) {
		String comp = "yt";
		String badge = "";
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.COMP_ABBR, SecurityConstants.FROM_IN);
		if (findByType.getData().size() > 0) {
			for (int j = 0; j < findByType.getData().size(); j++) {
				if (findByType.getData().get(j).getValue().equals(compId)) {
					comp = findByType.getData().get(j).getLabel();
					break;
				}
			}
		}
		StaffBadgeDTO badgeDto = new StaffBadgeDTO();
		badgeDto.setCompId(compId);
		badgeDto.setCompAbbr(comp);  //正式上线后去掉test
		try {
			SmtStaff lastStaff = this.baseMapper.SelectLastStaff(badgeDto);
			if (lastStaff == null) {
				badgeDto.setCompAbbr(comp);
				lastStaff = this.baseMapper.SelectLastStaff(badgeDto);
			}
			badge = StaffUtil.getNewBadge(lastStaff, comp);
			log.info("生成员工号:" + badge);

		} catch (Exception e) {
			// TODO: handle exception
			throw new TCEException("员工号生成异常" + e.getMessage());
		}
		return badge;
	}

	@Override
	public void addStaffRelation(SmtPreStaff preStaff, Long applicationId) {
		EbgeJavoidanceRegisterReqDTO register = null;
		List<SmtApplicationRelation> selectList = applicationRelationService.list(Wrappers.<SmtApplicationRelation>query().lambda().eq(SmtApplicationRelation::getApplicationId, applicationId));
		SmtStaffRelation staffRelation = null;

		for (SmtApplicationRelation fm : selectList) {
			register = new EbgeJavoidanceRegisterReqDTO();
			staffRelation = new SmtStaffRelation();
			staffRelation.setBadge(fm.getBadge());
			staffRelation.setClassName(fm.getClassName());
			staffRelation.setCompName(fm.getCompName());
			staffRelation.setDeptName(fm.getDeptName());
			staffRelation.setName(fm.getName());
			staffRelation.setRelation(fm.getRelation());
			staffRelation.setStaffId(preStaff.getId());
			staffRelation.setSex(fm.getSex());
			staffRelation.setJobName(fm.getJobName());
			staffRelation.setRelationDetail(fm.getRelationDetail());
			staffRelation.insert();
			register.setBadge(preStaff.getBadge());
			register.setCompid(Integer.parseInt(preStaff.getCompId()));
			register.setDepid(Integer.parseInt(preStaff.getDepId()));
			register.setJobid(preStaff.getJobId());
			register.setEID(preStaff.getEId());
			Result<OcompanyRespDTO> byComId = remoteCompanyService.getByComId(Integer.parseInt(preStaff.getCompId()),
					SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			if (byComId.isSuccess()) {
				if (byComId.getData() != null) {
					register.setEzid(byComId.getData().getEZID());
				}
			}
			register.setJchenID(Integer.parseInt(preStaff.getJcheId()));
			register.setJobid(preStaff.getJobId());
			register.setName(preStaff.getName());
			register.setRelativesBadge(fm.getBadge());
			register.setStatus(preStaff.getStatus());
			SmtStaff reOne = this.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, fm.getBadge()));
			if (reOne != null) {
				register.setREJchenID(Integer.parseInt(reOne.getJcheId()));
				register.setRelativesCompid(Integer.parseInt(reOne.getCompId()));
				register.setRelativesJobid(Integer.parseInt(reOne.getJobId()));
				register.setRelativesGX(Integer.parseInt(fm.getRelation()));
				register.setRelativesDepid(Integer.parseInt(reOne.getDepId()));
				register.setQsgx(fm.getRelationDetail());
			}
			register.setSeqid(preStaff.getSeqId());
			Result save = ebgeJavoidanceService.save(register, SecurityConstants.FROM_IN,
					SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			log.info("保持亲属关系:{}", save);
		}
	}

	@Override
	public void addStaffFamily(SmtPreStaff preStaff, Long applicationId) {
		List<SmtApplicationFamily> selectList = appFamilyService.list(Wrappers.<SmtApplicationFamily>query().lambda().eq(SmtApplicationFamily::getApplicationId, applicationId));
		for (SmtApplicationFamily fm : selectList) {
			EbgFamilyRegisterReqDTO fmRe = new EbgFamilyRegisterReqDTO();
			SmtStaffFamily staffFa = new SmtStaffFamily();
			staffFa.setBirth(fm.getBirth());
			staffFa.setCompany(fm.getCompany());
			staffFa.setJob(fm.getJob());
			staffFa.setName(fm.getName());
			staffFa.setPhone(fm.getPhone());
			staffFa.setRelation(fm.getRelation());
			staffFa.setSex(fm.getSex());
			staffFa.setStaffId(preStaff.getId());
			staffFa.insert();

			fmRe.setFname(fm.getName());
			fmRe.setBirthday(DateUtil.parseDate(fm.getBirth()));
			fmRe.setCompany(fm.getCompany());
			fmRe.setGender(fm.getSex());
			fmRe.setJob(fm.getJob());
			fmRe.setRemark(fm.getPhone());
			fmRe.setSeqid(preStaff.getSeqId());
			//获取code
			fmRe.setRelation(Integer.parseInt(fm.getRelation()));

			Result<Boolean> feignRs = familyReg.save(fmRe, SecurityConstants.FROM_IN,
					SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			log.info("保存家庭背景:{}", feignRs);
		}
	}

	@Override
	public void addStaffToHR(SmtPreStaff preStaff, Long applicationId, Integer local) {
		EstaffRegisterReqDTO estaff = new EstaffRegisterReqDTO();
		SmtApplication application = null;
		if (Objects.nonNull(applicationId)) {
			application = applicationService.getById(applicationId);
		}
		Result<OcompanyRespDTO> byComId = remoteCompanyService.getByComId(Integer.parseInt(preStaff.getCompId()),
				SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		log.info("获取公司信息:{}", byComId);
		OvwYsjobRespDTO ovwYsjob = remoteOvwYsjobService.getByDeptName(Integer.parseInt(preStaff.getJobId()), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED).data();

		estaff.setBadge(preStaff.getBadge());
		estaff.setName(preStaff.getName());
		estaff.setCompID(Integer.parseInt(preStaff.getCompId()));
		estaff.setDepID(Integer.parseInt(preStaff.getDepId()));
		estaff.setJobID(preStaff.getJobId());
		estaff.setJQunID(ovwYsjob.getJQunID());
		estaff.setJZuID(ovwYsjob.getJZuID());
		estaff.setJZongID(ovwYsjob.getJZongID());
		estaff.setJobtype(ovwYsjob.getJobType());
		estaff.setJchenID(ovwYsjob.getJchenID());
		estaff.setJXianID(ovwYsjob.getJXianID());
		estaff.setFlcj(ovwYsjob.getFlCJ());
		estaff.setEmpKind(ovwYsjob.getEmpkind());

		//9-网络招聘
		estaff.setJoinType(9);
		//1-在职/2-试用/3-实习/4-离职

		estaff.setStatus(2);
		//1-正式工/2-实习生/9-派遣工/10-临时工A类/13-临时工B类/14-自招挂安联/15-兼职/16-退休返聘
		estaff.setEmpType(1);
		//1-劳动合同/7-聘用协议/8-实习协议/9-退休返聘协议
		estaff.setConType(1);
		//默认裕同科技
		//SmtRecruitmentSetting recruitmentSetting = smtRecruitmentSettingService.getOne(Wrappers.<SmtRecruitmentSetting> query().lambda().eq(SmtRecruitmentSetting::getWorkCompId, staff.getCompId()));
		estaff.setContract(40);
		if (ObjectUtil.isNotNull(local)) {
			estaff.setContract(local);
		}
		//1-固定期合同/-非固定期合同/3-以完成一定工作任务为期限的劳动合同/4-实习协议
		estaff.setConProperty(1);
		estaff.setJoinDate(preStaff.getCreateTime());

		estaff.setPhone(preStaff.getPhone());
		estaff.setConNo(preStaff.getBadge());
		estaff.setCountry(41);//默认为中华人民共和国，标识传41
		estaff.setCertType(1);//1-身份证
		estaff.setCertNo(preStaff.getCertno());
		estaff.setPolice(preStaff.getPolice());
		estaff.setBirthday(DateUtil.parse(preStaff.getBirth()));
		estaff.setValidDate(preStaff.getValidDate());
		estaff.setValiddatefm(preStaff.getValidDateFm());
		estaff.setResidentaddress(preStaff.getHomeAddress());
		estaff.setNation(0);
		String facePic = null;
		if (ObjectUtil.isNotNull(preStaff.getFacePicId())) {
			try {
				byte[] bytes1 = smtImageService.getImageBinaryByCode(preStaff.getFacePicId());
             /*   stream = new ByteArrayInputStream(bytes1);
                Blob blob = Hibernate.createBlob(stream);*/
				estaff.setEmpPhoto(bytes1);
			} catch (Exception e) {
				log.error("下载员工头像异常", e);
			}
		}
		//0-男 1-女
		if (preStaff.getSex().equals(SexType.MAN.getCode())) {
			estaff.setGender(1);
		} else if (preStaff.getSex().equals(SexType.WOMAN.getCode())) {
			estaff.setGender(2);
		}
		estaff.setWorkbegindate(preStaff.getCreateTime());

		if (byComId.isSuccess()) {
			if (byComId.getData() != null) {
				estaff.setEzid(byComId.getData().getEZID());
			}
		}
		estaff.setType(1); //1-新员工，2-老员工复职
		estaff.setSalarytype(3); //计费类别编码，1-计时/2-计件/3-月薪/4-月薪1
		estaff.setPerSonEmail(preStaff.getEmail());
		estaff.setSeqID(preStaff.getSeqId());

		if (ObjectUtil.isNotNull(preStaff.getNation())) {
			String nation = preStaff.getNation() + "族";
			Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.NATION_TYPE, SecurityConstants.FROM_IN);
			if (findByType.getData().size() > 0) {
				for (int j = 0; j < findByType.getData().size(); j++) {
					if (findByType.getData().get(j).getLabel().equals(nation)) {
						estaff.setNation(Integer.parseInt(findByType.getData().get(j).getValue()));
						break;
					}
				}
			}
		} else {
			estaff.setNation(1);
		}

		Result<List<OvwYsdepRespDTO>> parentDep = remoteOvwYsdepService.getParentDep(Integer.parseInt(preStaff.getDepId()), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		log.info("获取部门信息：{}", parentDep);

		if (parentDep.isSuccess()) {
			if (parentDep.getData() != null) {
				List<OvwYsdepRespDTO> data = parentDep.getData();
				for (OvwYsdepRespDTO ovwYsdepVO : data) {
					if (ovwYsdepVO.getDepGrade().equals("1")) {
						estaff.setDepOne(ovwYsdepVO.getDepid());
					} else if (ovwYsdepVO.getDepGrade().equals("2")) {
						estaff.setDepTwo(ovwYsdepVO.getDepid());
					} else if (ovwYsdepVO.getDepGrade().equals("3")) {
						estaff.setDepThree(ovwYsdepVO.getDepid());
					}

				}

			}
		}
		SimpleDateFormat dateDay = new SimpleDateFormat("yyyy-MM-dd");
		estaff.setPrac(false);
		//默认标识：12-东莞，岗位属于大岭山园区发布的，工作地点都传东莞
		estaff.setWorkCity(12);
		estaff.setProb(true);
		//1-入职体检/2-职业健康体检/3-未成年员工体检/5-年度职业健康体检/6-年度体检
		estaff.seteTjtypte(1);
		estaff.seteTjdate(DateUtils.parse(dateDay.format(DateUtils.date())));
		estaff.seteTjenddate(DateUtils.parse(dateDay.format(DateUtils.date())));
		estaff.setConBeginDate(DateUtils.parse(dateDay.format(DateUtils.date())));
		estaff.setConEndDate(DateUtils.offsetMonth(DateUtils.parse(dateDay.format(DateUtils.date())), 3));
		estaff.setMarriage(Objects.nonNull(application) ? application.getMaritalStatus() : null);
		estaff.setTradebegindate(DateUtils.parse(dateDay.format(DateUtils.date())));
		//CONTERM合同期（月），默认3
		estaff.setConTerm(3);
		estaff.seteName(StaffUtil.getPinYin(preStaff.getName()));

		//员工、技工、班组长、职员、课长=3 其他=6
		if (preStaff.getJcheName().contains("技工") || preStaff.getJcheName().contains("职员") || preStaff.getJcheName().contains("课长") || preStaff.getJcheName().contains("员工") || preStaff.getJcheName().contains("班组长")) {
			estaff.setProbTerm(3);
			estaff.setProbEndDate(DateUtils.offsetMonth(DateUtils.parse(dateDay.format(DateUtils.date())), 3));
		} else {
			estaff.setProbTerm(6);
			estaff.setProbEndDate(DateUtils.offsetMonth(DateUtils.parse(dateDay.format(DateUtils.date())), 6));
		}

		estaff.setRegDate(DateUtils.parse(dateDay.format(DateUtils.date())));
		estaff.setJobBeginTime(DateUtils.parse(dateDay.format(DateUtils.date())));
		String director = null;
		OvwYsdepRespDTO ovwYsdepRespDTO = remoteOvwYsdepService.getByDepId(Integer.parseInt(preStaff.getDepId()), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED).data();
		if (ObjectUtil.isNotNull(ovwYsdepRespDTO)) {
			director = ovwYsdepRespDTO.getDirector();
			log.info("获取部门信息:部门ID:{} 部门名称:{}", ovwYsdepRespDTO.getDepid(), ovwYsdepRespDTO.getDepname());
		}
		if (director != null) {
			EvwEmphrYsRespDTO info = remoteEvwEmphrYsService.info(director, SecurityConstants.FROM_IN).data();
			estaff.setReportto(info.getEId());
		}

		if (ObjectUtil.isNotNull(applicationId)) {
			//获取紧急联系人
			List<SmtApplicationEmergency> emergencyList = applicationEmergentyService.list(Wrappers.<SmtApplicationEmergency>query().lambda().eq(SmtApplicationEmergency::getApplicationId, applicationId));
			if (emergencyList.size() > 0) {
				SmtStaffEmergency em = new SmtStaffEmergency();
				em.setStaffId(preStaff.getId());
				em.setEmergencyName(emergencyList.get(0).getEmergencyName());
				em.setTelephont(emergencyList.get(0).getTelephont());
				em.setRelation(emergencyList.get(0).getRelation());
				em.insert();
				estaff.setEmergencyName(em.getEmergencyName());
				estaff.setRelation(Integer.parseInt(em.getRelation()));
				estaff.setTelephone(em.getTelephont());
			}
		}
		Result<?> feignRs = staffRegister.save(estaff);
		log.info("保持入职信息:{}", feignRs);
	}

	@Transactional
	@Override
	public Result addStaffToHR(ApplicationStaffDTO smtStaffReq) {
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
		/*    smtStaff.setParkId(application.getParkId());*/
		smtPreStaff.setName(application.getName());
		String badge = "";
		//获取comp的简称
		badge = createNewBadge(recruitment.getCompId());
		smtPreStaff.setBadge(badge);
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

		//添加到入职员工表中，只做记录
		smtPreStaff.insert();

		log.info("============smtPreStaff===========:" + smtPreStaff);
		if (ObjectUtil.isNull(smtPreStaff.getSeqId())) {
			smtPreStaff = smtPreStaffMapper.selectById(smtPreStaff.getId());
		}
		log.info("============new query smtPreStaff===========:" + smtPreStaff);
		if (ObjectUtil.isNull(smtPreStaff.getSeqId())) {
			Integer value = Integer.parseInt(smtPreStaff.getId().toString());
			smtPreStaff.setSeqId(value);
		}
		log.info("============new query smtPreStaff===========:" + smtPreStaff);

		//同步数据到HR系统表中
		addStaffToHR(smtPreStaff, application.getId(), recruitment.getLocal());
		//同步工作经验
		addStaffWork(applicationId, smtPreStaff);
		//同步教育经验
		addStaffEducation(applicationId, smtPreStaff);
		//同步家庭成员
		addStaffFamily(smtPreStaff, applicationId);
		//同步人事关系
		addStaffRelation(smtPreStaff, applicationId);
		return new Result<>(true);
	}


	@Override
	public void addStaffWork(Long applicationId, SmtPreStaff preStaff) {
		List<SmtApplicationWork> selectList = workService.list(
				Wrappers.<SmtApplicationWork>query().lambda().eq(SmtApplicationWork::getApplicationId, applicationId));
		SmtStaffWork staffWork = null;

		EbgWorkingRegisterReqDTO re = null;
		for (SmtApplicationWork work : selectList) {
			staffWork = new SmtStaffWork();
			staffWork.setStaffId(preStaff.getId());
			staffWork.setCompany(work.getCompany());
			staffWork.setStartTime(work.getStartTime());
			staffWork.setEndTime(work.getEndTime());
			staffWork.setJobName(work.getJobName());
			staffWork.setPersonLiable(work.getPersonLiable());
			staffWork.setPhone(work.getPhone());
			staffWork.insert();

			re = new EbgWorkingRegisterReqDTO();
			re.setBegindate(DateUtil.parseDate(work.getStartTime()));
			re.setEnddate(DateUtil.parseDate(work.getEndTime()));
			re.setCompany(work.getCompany());
			re.setJob(work.getJobName());
			re.setReference(work.getPersonLiable());
			re.setTel(work.getPhone());
			re.setSeqID(preStaff.getSeqId());

			Result<?> feignRs = workRegister.save(re, SecurityConstants.FROM_IN,
					SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			log.info("保存工作经历:{}", feignRs);
		}
	}

	@Override
	public void addStaffEducation(Long applicationId, SmtPreStaff preStaff) {
		// TODO Auto-generated method stub
		List<SmtApplicationEducation> selectList = educationService.list(Wrappers.<SmtApplicationEducation>query()
				.lambda().eq(SmtApplicationEducation::getApplicationId, applicationId));
		Integer degreeInt = null;
		Integer educationInt = null;
		Integer one = 1;
		for (SmtApplicationEducation education : selectList) {
			SmtStaffEducation staffEducation = new SmtStaffEducation();
			staffEducation.setStaffId(preStaff.getId());
			staffEducation.setStartTime(education.getStartTime());
			staffEducation.setEndTime(education.getEndTime());
			staffEducation.setSchoolName(education.getSchoolName());
			staffEducation.setMajor(education.getMajor());
			staffEducation.setEducation(education.getEducation());
			staffEducation.setDegree(education.getDegree());
			staffEducation.setGradType(1);
			staffEducation.setIsHighDegreeType(education.getIsHighDegreeType());
			staffEducation.setIsHighEduType(education.getIsHighEduType());
			staffEducation.insert();
			EbgEducationRegisterReqDTO edu = new EbgEducationRegisterReqDTO();
			edu.setBeginDate(DateUtil.parseDate(education.getStartTime()));
			edu.setEndDate(DateUtil.parseDate(education.getEndTime()));
			edu.setSchoolName(education.getSchoolName());
			edu.setDegreeName(education.getDegree());
			edu.setMajor(education.getMajor());
//            // 暂定。需要改为字典
			edu.setGradType(1);
//            edu.setStudyType(1);
			edu.setSeqID(preStaff.getSeqId());
			//学位
			if (StringUtils.isNotBlank(education.getDegree())) {
				degreeInt = Integer.parseInt(education.getDegree());
			}
			edu.setDegreeType(degreeInt);

			//学历
			if (StringUtils.isNotBlank(education.getEducation())) {
				educationInt = Integer.parseInt(education.getEducation());
			}
			edu.setEduType(educationInt);
			edu.setIsHighDegreeType(one.equals(education.getIsHighDegreeType()));
			edu.setIsHighEdutype(one.equals(education.getIsHighEduType()));

			Result<Boolean> feignRs = eduReg.save(edu, SecurityConstants.FROM_IN,
					SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			log.info("保存教育经历:{}", feignRs);
		}
	}

	@Override
	public Boolean saveBatchTemporaryStaff(List<TempStaffEditReqDTO> tempStaffs, SmtDormitoryStaffService smtDormitoryStaffService) {
		long startTime = System.currentTimeMillis();
		if (CollectionUtil.isEmpty(tempStaffs)) {
			throw new SmartException("员工信息为空，请重新选择");
		}
		Integer userId = SecurityUtils.getUser().getId();
		SmtOrganizeRelation organizeRelation = smtOrganizeRelationService.getByUserId(userId);
		if (Objects.isNull(organizeRelation)) {
			throw new SmartException("您尚未登记企业");
		}
		List<SysDict> dict = remoteDictService.findByType(DictConstants.JOB_LEVEL, SecurityConstants.FROM_IN).data();
		if (CollUtil.isEmpty(dict)) {
			throw new SmartException("获取级层字典表失败");
		}
		List<String> dictStr = dict.stream().map(SysDict::getLabel).collect(Collectors.toList());
		List<String> depts = smtExternalDeptService.getList().stream().map(SmtExternalDept::getDeptName).collect(Collectors.toList());
		if (CollUtil.isEmpty(depts)) {
			throw new SmartException("企业：" + organizeRelation.getCompName() + " 尚未登记部门，无法导入");
		}
		String errorCento = "";
		String errorCento2 = "";
		String errorDept = "";
		String errorJche = "";
		for (TempStaffEditReqDTO staff : tempStaffs) {
			if (!depts.contains(staff.getDepName())) {
				errorDept = errorDept + staff.getDepName() + ",";
			}
			if (!dictStr.contains(staff.getJcheName())) {
				errorJche = errorJche + staff.getJcheName() + ",";
			}
            /*String cento = staff.getCertno();
            if (!RegexUtils.matchCento(cento)) {
                errorCento = errorCento + cento + ",";
            }
            SmtStaff one = this.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getCertno, cento)
                    .ne(SmtStaff::getStatus, StaffStatusEnum.STAFF_STATUS_QUIT.getCode()));
            if (ObjectUtil.isNotNull(one)) {
                errorCento2 += cento + ",";
            }*/
		}
		if (!"".equals(errorCento)) {
			errorCento = errorCento.substring(0, errorCento.length() - 1);
			throw new SmartException("证件号'" + errorCento + "'不合法，请核对后重新登记");
		}
/*        if (!"".equals(errorCento2)) {
            errorCento2 = errorCento2.substring(0, errorCento2.length() - 1);
            throw new SmartException("证件号为'" + errorCento2 + "'的员工已入职,请核对后重新登记");
        }*/
		if (!"".equals(errorDept)) {
			errorDept = errorDept.substring(0, errorDept.length() - 1);
			throw new SmartException("部门‘" + errorDept + " '尚未登记在" + organizeRelation.getCompName() + "企业下");
		}
		if (!"".equals(errorJche)) {
			errorJche = errorJche.substring(0, errorJche.length() - 1);
			throw new SmartException("级层‘" + errorJche + "不存在");
		}
		SmtStaff staff = null;

		Map<String, String> jcheFL = new HashMap<>();

		for (TempStaffEditReqDTO tempStaff : tempStaffs) {

			staff = BeanUtils.transform(SmtStaff.class, tempStaff);
			SmtExternalDept dept = smtExternalDeptService.getByName(tempStaff.getDepName(), organizeRelation.getId());
			staff.setSex(tempStaff.getSex());
			if (RegexUtils.matchCento(tempStaff.getCertno())) {
				Integer idNOToAge = StaffUtil.idNoToAge(tempStaff.getCertno());
				staff.setAge(idNOToAge);
				staff.setSex(ToolUtils.getGenderByIdCard(tempStaff.getCertno()).getCode());
			}
			if (StringUtils.isNotEmpty(dept.getDirector())) {
				staff.setReportTo(this.getById(dept.getDirector()).getBadge());
			}
			staff.setCompId(String.valueOf(organizeRelation.getId()));
			staff.setCompName(organizeRelation.getCompName());
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			try {
				staff.setCreateTime(df.parse(tempStaff.getEntryTime()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			staff.setDispatch(tempStaff.getDispatch());
			staff.setDepId(dept.getId().toString());
			staff.setDepName(tempStaff.getDepName());
			SysDict jche = dict.stream().filter(d -> d.getLabel().equals(tempStaff.getJcheName())).collect(Collectors.toList()).get(0);
			staff.setJcheId(jche.getValue());
			staff.setJcheName(jche.getLabel());
			try {
				if (jcheFL.containsKey(tempStaff.getJcheId())) {
					staff.setWelfareLevel(jcheFL.get(tempStaff.getJcheId()));
				} else {
					//可能没有对应的福利层级
					Result<EvwCcdFlstandardDTO> flstandardDTOResult = remoteEvwCcdFlstandardService.getById(tempStaff.getJcheId(), null, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
					staff.setWelfareLevel(flstandardDTOResult.getData().getCode());
					jcheFL.put(tempStaff.getJcheId(), staff.getWelfareLevel());
				}
			} catch (Exception e) {
			}
			staff.setStatus(StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode());

			SmtStaff reStaff = this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, tempStaff.getBadge()));
			if (reStaff != null) {
				//员工信息已存在 更新
				this.update(staff, Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, tempStaff.getBadge()));
			} else {
				//使用身份证查询员工信息
				SmtStaff smtStaff1 = null;
				Boolean excludeOrg = smtOrganizeRelationService.wasExcludeOrg(dept.getCompId());
				if (StringUtils.isNotEmpty(tempStaff.getCertno()) && !excludeOrg) {
					smtStaff1 = this.getOne(new LambdaQueryWrapper<SmtStaff>().eq(SmtStaff::getBadge, tempStaff.getCertno()));
				}

				if (smtStaff1 != null) {
					//先入住 后添加的情况
					staff.setId(smtStaff1.getId());

					//查询是否存在入住记录
					SmtDormitoryStaff dormitoryStaff = smtDormitoryStaffService.getOne(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getStaffBadge, staff.getCertno()));
					if (dormitoryStaff != null) {
						//更新入住信息
						smtDormitoryStaffService.updateDormitoryStaffTemp(staff);
						//设置为已入住
						staff.setDormitoryStatus(DormitoryStatusEnum.NOT_INNER.getCode());
					}
					this.updateById(staff);
				} else {
					staff.insert();
				}
				// 通过园区下发权限
//				this.addStaffDeviceAuth(staff);
				// 通过外包企业下发权限
				this.authAccess(staff);
				SmtAppAuth smtAppAuth = smtAppAuthService.getOne(Wrappers.<SmtAppAuth>query().lambda().eq(SmtAppAuth::getAuthName, tempAppAuth));
				if (Objects.nonNull(smtAppAuth)) {
					SmtAppStaffAuth smtAppStaffAuth = new SmtAppStaffAuth();
					smtAppStaffAuth.setStaffId(staff.getId());
					smtAppStaffAuth.setAuthId(smtAppAuth.getId());
					smtAppStaffAuth.setCreate_time(DateUtils.date());
					// 添加员工权限
					appStaffAuthService.save(smtAppStaffAuth);
				}
			}
			smtStaffService.saveToC6(staff, organizeRelation);
		}

		long endTime = System.currentTimeMillis();
		log.error("批量导入员工执行时长：" + (endTime - startTime)/1000 + "s");
		return true;
	}

	/**
	 * 下发门禁权限给下级员工
	 * @param staff
	 * @return
	 */
	@Override
	public Boolean authAccess(SmtStaff staff) {
		Long compId = Long.parseLong(staff.getCompId());
		List<Integer> deviceAuthIds = organizeAccessService.getDeviceAuthId(compId);
		List<SmtStaffDeviceAuth> staffId = smtStaffDeviceAuthService.list(Wrappers.<SmtStaffDeviceAuth>query()
						.lambda().eq(SmtStaffDeviceAuth::getStaffId, staff.getId()));
		if(CollUtil.isNotEmpty(staffId)) {
			List<Integer> authIds = staffId.stream().map(SmtStaffDeviceAuth::getAuthId).collect(Collectors.toList());
			deviceAuthIds.addAll(authIds);
			deviceAuthIds = deviceAuthIds.stream().distinct().collect(Collectors.toList());
		}
		if(CollectionUtils.isNotEmpty(deviceAuthIds)) {
			// 更新员工门禁权限
			List<Long> staffIds = new ArrayList<>();
			staffIds.add(staff.getId());
			smtStaffDeviceAuthService.updateAuth(staffIds, deviceAuthIds);
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean saveBatchTemporaryStaff(List<TempStaffEditReqDTO> tempStaffs, SmtOrganizeRelation organizeRelation, SmtDormitoryStaffService smtDormitoryStaffService) {
		long startTime = System.currentTimeMillis();
		if (CollectionUtil.isEmpty(tempStaffs)) {
			throw new SmartException("员工信息为空，请重新选择");
		}
		List<SysDict> dict = remoteDictService.findByType(DictConstants.JOB_LEVEL, SecurityConstants.FROM_IN).data();
		if (CollUtil.isEmpty(dict)) {
			throw new SmartException("获取级层字典表失败");
		}
		List<String> dictStr = dict.stream().map(SysDict::getLabel).collect(Collectors.toList());
		List<String> depts = smtExternalDeptService.getList(organizeRelation.getId()).stream().map(SmtExternalDept::getDeptName).collect(Collectors.toList());
		if (CollUtil.isEmpty(depts)) {
			throw new SmartException("企业：" + organizeRelation.getCompName() + " 尚未登记部门，无法导入");
		}
		String errorDept = "";
		String errorJche = "";
		for (TempStaffEditReqDTO staff : tempStaffs) {
			if (!depts.contains(staff.getDepName())) {
				errorDept = errorDept + staff.getDepName() + ",";
			}
			if (!dictStr.contains(staff.getJcheName())) {
				errorJche = errorJche + staff.getJcheName() + ",";
			}
		}
		if (!"".equals(errorDept)) {
			errorDept = errorDept.substring(0, errorDept.length() - 1);
			throw new SmartException("部门‘" + errorDept + " '尚未登记在" + organizeRelation.getCompName() + "企业下");
		}
		if (!"".equals(errorJche)) {
			errorJche = errorJche.substring(0, errorJche.length() - 1);
			throw new SmartException("级层‘" + errorJche + "不存在");
		}
		SmtStaff staff;
		Map<String, String> jcheFL = new HashMap<>();

		for (TempStaffEditReqDTO tempStaff : tempStaffs) {
			staff = BeanUtils.transform(SmtStaff.class, tempStaff);
			SmtExternalDept dept = smtExternalDeptService.getByName(tempStaff.getDepName(), organizeRelation.getId());
			if (RegexUtils.matchCento(tempStaff.getCertno())) {
				Integer idNOToAge = StaffUtil.idNoToAge(tempStaff.getCertno());
				staff.setAge(idNOToAge);
			}
			if (StringUtils.isNotEmpty(dept.getDirector())) {
				staff.setReportTo(this.getById(dept.getDirector()).getBadge());
			}
			staff.setCompId(String.valueOf(organizeRelation.getId()));
			staff.setCompName(organizeRelation.getCompName());
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			try {
				staff.setCreateTime(df.parse(tempStaff.getEntryTime()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			staff.setSex(tempStaff.getSex());
			staff.setDepId(dept.getId().toString());
			staff.setDepName(tempStaff.getDepName());
			SysDict jche = dict.stream().filter(d -> d.getLabel().equals(tempStaff.getJcheName())).collect(Collectors.toList()).get(0);
			staff.setJcheId(jche.getValue());
			staff.setJcheName(jche.getLabel());
			try {
				if (jcheFL.containsKey(tempStaff.getJcheId())) {
					staff.setWelfareLevel(jcheFL.get(tempStaff.getJcheId()));
				} else {
					//可能没有对应的福利层级
					Result<EvwCcdFlstandardDTO> flstandardDTOResult = remoteEvwCcdFlstandardService.getById(tempStaff.getJcheId(), null, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
					staff.setWelfareLevel(flstandardDTOResult.getData().getCode());
					jcheFL.put(tempStaff.getJcheId(), staff.getWelfareLevel());
				}
			} catch (Exception e) {
			}
			staff.setStatus(StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode());

			SmtStaff reStaff = this.baseMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, tempStaff.getBadge()));
			if (reStaff != null) {
				//员工信息已存在 更新
				this.update(staff, Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, tempStaff.getBadge()));
			} else {
				//使用身份证查询员工信息
				SmtStaff smtStaff1 = null;
				Boolean excludeOrg = smtOrganizeRelationService.wasExcludeOrg(dept.getCompId());
				if (StringUtils.isNotEmpty(tempStaff.getCertno()) && !excludeOrg) {
					smtStaff1 = this.getOne(new LambdaQueryWrapper<SmtStaff>().eq(SmtStaff::getBadge, tempStaff.getCertno()));
				}
				if (smtStaff1 != null) {
					//先入住 后添加的情况
					staff.setId(smtStaff1.getId());
					//查询是否存在入住记录
					SmtDormitoryStaff dormitoryStaff = smtDormitoryStaffService.getOne(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getStaffBadge, staff.getCertno()));
					if (dormitoryStaff != null) {
						//更新入住信息
						smtDormitoryStaffService.updateDormitoryStaffTemp(staff);
						//设置为已入住
						staff.setDormitoryStatus(DormitoryStatusEnum.NOT_INNER.getCode());
					}
					this.updateById(staff);
				} else {
					staff.insert();
				}
				// 通过外包企业下发权限
				this.authAccess(staff);
				SmtAppAuth smtAppAuth = smtAppAuthService.getOne(Wrappers.<SmtAppAuth>query().lambda().eq(SmtAppAuth::getAuthName, tempAppAuth));
				if (Objects.nonNull(smtAppAuth)) {
					SmtAppStaffAuth smtAppStaffAuth = new SmtAppStaffAuth();
					smtAppStaffAuth.setStaffId(staff.getId());
					smtAppStaffAuth.setAuthId(smtAppAuth.getId());
					smtAppStaffAuth.setCreate_time(DateUtils.date());
					// 添加员工权限
					appStaffAuthService.save(smtAppStaffAuth);
				}
			}
			smtStaffService.saveToC6(staff, organizeRelation);
		}
		long endTime = System.currentTimeMillis();
		log.error("批量导入员工执行时长：" + (endTime - startTime)/1000 + "s");
		return true;
	}
}
