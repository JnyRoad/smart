package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.algorithm.api.dto.resp.FaceFeaturesDTO;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.app.ao.fore.EmployeeUpdateAo;
import com.tce.smart.app.ao.fore.PerfectInfoAo;
import com.tce.smart.app.ao.fore.RoomApplyAo;
import com.tce.smart.app.dto.fore.OcrIdCardDto;
import com.tce.smart.app.entity.AppIdentityCollect;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppIdentityCollectService;
import com.tce.smart.app.service.fore.DeviceManageService;
import com.tce.smart.app.service.fore.EmployeeService;
import com.tce.smart.app.vo.fore.*;
import com.tce.smart.app.vo.wechat.RelationTypeVO;
import com.tce.smart.common.core.constant.PaginationConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.consume.req.UpdateHeadImageReqDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsRespDTO;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpPhotoService;
import com.tce.smart.data.api.feign.dhrview.RemoteYutoDhrYsService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwEmphrYsService;
import com.tce.smart.platform.api.dto.SmtOutDormitoryStaffDTO;
import com.tce.smart.platform.api.dto.SmtStaffDTO;
import com.tce.smart.platform.api.dto.req.InDormitoryReqDTO;
import com.tce.smart.platform.api.dto.req.StaffEmergencyReqDTO;
import com.tce.smart.platform.api.dto.req.StaffPerfectReqDTO;
import com.tce.smart.platform.api.dto.resp.MyDormitoryRespDTO;
import com.tce.smart.platform.api.dto.resp.StaffInfoRespDTO;
import com.tce.smart.platform.api.feign.RemoteCallowanceCancelRecordService;
import com.tce.smart.platform.api.feign.RemoteOutDormitoryStaffService;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.ImageUtils;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 员工信息接口实现
 *
 * @author qipei
 */
@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

	@Autowired
	private RemoteStaffService remoteStaff;

	@Autowired
	private RemoteDictService remoteDictService;

	@Autowired
	private RemoteOutDormitoryStaffService outDormitoryStaffService;

	@Autowired
	private AppCommService appCommService;

	@Autowired
	private RemoteAlgorithmService remoteAlgorithmService;

	@Autowired
	private DeviceManageService deviceManageService;

	@Autowired
	private AppIdentityCollectService identityCollectService;

	@Autowired
	private RemoteRsEmpPhotoService remoteRsEmpPhotoService;

	@Autowired
	private RemoteEvwEmphrYsService remoteEvwEmphrYsService;

	@Autowired
	private RemoteCallowanceCancelRecordService remoteCallowanceCancelRecordService;

	@Autowired
	private RemoteYutoDhrYsService remoteYutoDhrYsService;

	/**
	 * 根据员工号获取员工基本信息
	 */
	@Override
	public EmployeeVo getBaseinfo(String badge) {
		// 获取员工号
		if (StringUtils.isBlank(badge)) {
			badge = SecurityUtils.getUser().getUsername();
		}
		// 远程调用获取员工基本信息
		StaffInfoRespDTO staff = remoteStaff.getBaseinfoByBadge(badge, SecurityConstants.FROM_IN).data();

		EmployeeVo employeeVo = new EmployeeVo();
		employeeVo.setEmployeeBadge(staff.getSmtStaff().getBadge());
		employeeVo.setEmployeeName(staff.getSmtStaff().getName());
		employeeVo.setMobile(staff.getSmtStaff().getPhone());
		employeeVo.setEmployeeSex(staff.getSmtStaff().getSex());
		employeeVo.setBuName(staff.getSmtStaff().getCompName());
		employeeVo.setDeptName(staff.getSmtStaff().getDepName());
		employeeVo.setJobName(staff.getSmtStaff().getJobName());
		employeeVo.setIsSecurityGuard(employeeVo.getJobName() != null && employeeVo.getJobName().contains("保安") ? 0 : 1);
		employeeVo.setEmployeePhoto(appCommService.buildHqImageUrl(staff.getSmtStaff().getFacePicId()));
		employeeVo.setEntryDate(staff.getSmtStaff().getCreateTime());
		employeeVo.setParkName(staff.getParkName());
		employeeVo.setDormitoryState(Objects.nonNull(staff.getDormitoryState()) ? String.valueOf(staff.getDormitoryState()) : null);
		employeeVo.setDormitoryStateDesc(staff.getDormitoryStateDesc());
		employeeVo.setVehicleState(staff.getVehicleState().toString());
		employeeVo.setVehicleStateDesc(staff.getVehicleStateDesc());
		employeeVo.setStatus(staff.getStatus());
		employeeVo.setJcheName(staff.getSmtStaff().getJcheName());
		employeeVo.setStatusDes(staff.getStatusDes());
		employeeVo.setEmployeeCardNo(staff.getSmtStaff().getCertno());
		employeeVo.setEmpType(staff.getEmpType());
		employeeVo.setEmpTypeDes(staff.getEmpTypeDes());
		employeeVo.setApplyState(staff.getApplyState());
		employeeVo.setWelfareLevel(staff.getSmtStaff().getWelfareLevel());
		employeeVo.setApplyStateDesc(staff.getApplyStateDesc());
		Result<String> attribute = remoteYutoDhrYsService.getProperties(employeeVo.getEmployeeBadge(), SecurityConstants.FROM_IN);
		if(Objects.nonNull(attribute.getData())) {
			employeeVo.setEmpAttribute(attribute.getData());
		}
		return employeeVo;
	}

	/**
	 * 根据员工号获取员工的所有信息
	 */
	@Override
	public EmployeeInfoVo getFullinfo(String badge) {
		// TODO Auto-generated method stub
		// 获取员工号
		if (StringUtils.isBlank(badge)) {
			badge = SecurityUtils.getUser().getUsername();
		}
		StaffInfoRespDTO staffVo = remoteStaff.getFullByBadge(badge, SecurityConstants.FROM_IN).data();
		List<SysDict> findByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN).data();

		SmtStaffDTO staff = staffVo.getSmtStaff();
		EmployeeInfoVo emp = new EmployeeInfoVo();
		emp.setEmployeeId(staff.getBadge());
		emp.setEmployeeName(staff.getName());
		emp.setIdentification(staff.getCertno());
		emp.setEmployeePhoto(ImageUtils.changeFullBase64(staffVo.getFacePic()));
		emp.setBuName(staff.getCompName());
		emp.setDeptName(staff.getDepName());
		emp.setJobName(staff.getJobName());
		emp.setJobLeve(staff.getJcheName());
		emp.setMobile(staff.getPhone());
		emp.setEmail(staff.getEmail());
		emp.setEmpType(staff.getEmpType());
		emp.setParkName(staffVo.getParkName());
		emp.setEmpTypeDes("");
		if (Objects.nonNull(emp.getEmpType())) {
			emp.setEmpTypeDes(EmpTypeEnum.desc(emp.getEmpType()));
		}
		emp.setJobLevelflag(staff.getWelfareLevel());
		if (CollectionUtils.isNotEmpty(staffVo.getSmtStaffEmergency())) {
			if (CollectionUtils.isNotEmpty(findByType)) {
				for (SysDict sysDict : findByType) {
					if (staffVo.getSmtStaffEmergency().get(0).getRelation().equals(sysDict.getValue())) {
						emp.setRelation(sysDict.getLabel());
						break;
					}
				}
			}
			emp.setEmergencyName(staffVo.getSmtStaffEmergency().get(0).getEmergencyName());
			emp.setEmergencyPhone(staffVo.getSmtStaffEmergency().get(0).getTelephont());
		}
		emp.setEntryDate(staff.getCreateTime());
		return emp;
	}

	@Override
	public Result relationUpdate(EmployeeUpdateAo relationAo) {
		// TODO Auto-generated method stub
		String relationType = "";
		List<SysDict> findByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN).data();
		if (CollectionUtils.isNotEmpty(findByType)) {
			for (SysDict sysDict : findByType) {
				if (relationAo.getRelation().equals(sysDict.getLabel())) {
					relationType = sysDict.getValue();
					break;
				}
			}
		}
		if (StringUtils.isEmpty(relationType)) {
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "此关系不存在");
		}
		// 获取员工号
		String badge = SecurityUtils.getUser().getUsername();
		StaffEmergencyReqDTO em = new StaffEmergencyReqDTO();
		em.setBadge(badge);
		em.setEmergencyName(relationAo.getEmergencyName());
		em.setEmergencyPhone(relationAo.getEmergencyPhone());
		em.setRelation(relationType);
		Result result = remoteStaff.updateByBadge(em, SecurityConstants.FROM_IN);
		log.info("修改紧急联系人: Badge={}, Relation={}, EmergencyName={}, Result={}", badge, em.getRelation(), em.getEmergencyName(), result.isSuccess());
		return result;
	}

	/**
	 * 内宿申请
	 */
	@Override
	public Result roomApply(RoomApplyAo roomApplyVo) {
		String badge = SecurityUtils.getUser().getUsername();
		InDormitoryReqDTO inDormitoryDTO = new InDormitoryReqDTO();
		inDormitoryDTO.setStaffBadge(badge);
		inDormitoryDTO.setBedType(roomApplyVo.getBedType());
		inDormitoryDTO.setParkId(roomApplyVo.getParkId());
		Result result = remoteStaff.addInDormitory(inDormitoryDTO, SecurityConstants.FROM_IN);
		log.info("员工申请内宿: Badge={}, Result={}", badge, result);
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
		return result;
	}

	/**
	 * 员工内宿信息
	 */
	@Override
	public RoomDetailVo roomDetail() {
		// TODO Auto-generated method stub
		String badge = SecurityUtils.getUser().getUsername(); // 获取员工号
		SmtStaffDTO staff = new SmtStaffDTO();
		staff.setBadge(badge);
		MyDormitoryRespDTO apply = remoteStaff.myDormitory(staff, SecurityConstants.FROM_IN).data();

		RoomDetailVo vo = new RoomDetailVo();
		vo.setBuildingName(apply.getDormitoryName());
		vo.setFloor(apply.getFloorName().toString());
		vo.setRoom(apply.getRoomName().toString());
		vo.setBedName(apply.getBedNumber() + "号床");
		vo.setMaxBed(apply.getBedToal().toString());
		vo.setOccupancy(apply.getUsedNum().toString());
		vo.setApplyStateDes("已分配");
		vo.setRoomType("内宿");
		vo.setParkId(apply.getParkId());
		vo.setParkName(apply.getParkName());
		return vo;
	}

	/**
	 * 获取员工二维码
	 */
	@Override
	public Result qrcode() {
		// 获取员工号
		String badge = SecurityUtils.getUser().getUsername();
		return remoteStaff.getQrcode(badge, SecurityConstants.FROM_IN);
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
	public Result outRoomApply(SmtOutDormitoryStaffDTO outDormitory) {
		if (StringUtils.isBlank(outDormitory.getStaffBadge())) {
			String badge = SecurityUtils.getUser().getUsername();
			outDormitory.setStaffBadge(badge);
		}
		Result result = outDormitoryStaffService.addOutDormitory(outDormitory, SecurityConstants.FROM_IN);
		log.info("申请外宿: Badge={}, Result={}", outDormitory.getStaffBadge(), result.isSuccess());
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
		return result;
	}

	@Override
	public Result getAllowance(String staffBadge, Integer type) {
		if (StringUtils.isBlank(staffBadge)) {
			staffBadge = SecurityUtils.getUser().getUsername();
		}
		Result result = outDormitoryStaffService.getAllowance(staffBadge, type, SecurityConstants.FROM_IN);
		log.info("获取补贴信息: Badge={}, Result={}", staffBadge, result.isSuccess());
		return result;
	}

	@Override
	public Result outRoomDetaol(String staffBadge, Integer type) {
		if (StringUtils.isBlank(staffBadge)) {
			staffBadge = SecurityUtils.getUser().getUsername();
		}
		Result<List<SmtOutDormitoryStaffDTO>> result = outDormitoryStaffService.getOutDormitoryInfo(staffBadge, type, SecurityConstants.FROM_IN);
		log.info("外宿信息: Badge={}, Result={}", staffBadge, result.isSuccess());

		OutDormitoryVo vo = new OutDormitoryVo();
		if (result.isSuccess()) {
			List<SmtOutDormitoryStaffDTO> list = result.getData();
			if (CollectionUtils.isNotEmpty(list)) {
				vo.setOutAddress(list.get(0).getOutAddress());
				if (type == 11) {
					vo.setDormitoryType("外宿");
				} else {
					vo.setDormitoryType("外餐");
				}
				vo.setStatusDes(OutDormitoryStatusEnum.desc(list.get(0).getStatus()));
			}
		}
		return new Result<>(vo);
	}

	@Override
	public Boolean updateFacePhoto(PerfectInfoAo perfectInfoAo) {
		if (Objects.isNull(perfectInfoAo)) {
			throw new TCEException("用户信息为空");
		}

		if (StringUtil.isNullOrEmpty(perfectInfoAo.getFacePhoto())) {
			throw new TCEException("人脸照片为空");
		}

		if (StringUtil.isNullOrEmpty(perfectInfoAo.getDeviceNo())) {
			throw new TCEException("设备编号为空");
		}
		String facePhoto = perfectInfoAo.getFacePhoto();
		FaceFeaturesDTO faceFeaturesDTO = remoteAlgorithmService.getFaceFeatures(facePhoto, SecurityConstants.FROM_IN).data();
		if (!StringUtil.isNullOrEmpty(faceFeaturesDTO.getFaceFeature())) {
			// 更新人脸、身份证照片信息
			String badge = SecurityUtils.getUser().getUsername();
			StaffPerfectReqDTO perfectDTO = new StaffPerfectReqDTO();
			perfectDTO.setBadge(badge);
			perfectDTO.setFacePic(facePhoto);

			// 更新员工人脸信息
			Result result = remoteStaff.perfectFace(perfectDTO);
			log.info("更新员工人脸信息: Result={}", result.isSuccess());
			if (!result.isSuccess()) {
				throw new TCEException("更新员工人脸信息失败: " + result.getMessage());
			}
			// 更新此设备为绑定状态
			deviceManageService.bindDevice(badge, perfectInfoAo.getDeviceNo());
			String staffId = SecurityUtils.getUser().getUsername();
			OcrIdCardDto ocrIdCardDto = new OcrIdCardDto();
			ocrIdCardDto.setStaffId(staffId);
			ocrIdCardDto.setFacePhoto(facePhoto);
			// 保存人脸信息
			Integer perfectId = identityCollectService.insertOrUpdate(ocrIdCardDto);

			// 同步到裕同C6表
			//updateRemoteC6(perfectId, new UpdateHeadImageReqDTO(badge, facePhoto));

		} else {
			throw new TCEException("未检测到人脸，请重新上传");
		}
		return true;
	}

	@Override
	public Boolean syncPhotoToYuto() {
		Page<AppIdentityCollect> page = new Page<>();
		//分页查询员工最近更新、未同步的的人脸信息
		identityCollectService.getLatestPhoto(page);
		//批量同步到C6
		this.batchSyncPhtoToC6(page.getRecords());
		while (page.hasNext()) {
			page.setCurrent(page.getCurrent() + 1);
			identityCollectService.getLatestPhoto(page);
			this.batchSyncPhtoToC6(page.getRecords());
		}
		return Boolean.TRUE;
	}

	/**
	 * 批量同步人脸图片到C6
	 *
	 * @param listPo
	 */
	private void batchSyncPhtoToC6(List<AppIdentityCollect> listPo) {
		listPo.forEach(entity -> updateRemoteC6(entity.getId(), new UpdateHeadImageReqDTO(entity.getStaffId(), entity.getFaceImage())));
	}

	/**
	 * 同步到裕同C6表
	 *
	 * @param perfectId   信息完善iD
	 * @param headImageAo 人脸图片信息
	 */
	private void updateRemoteC6(Integer perfectId, UpdateHeadImageReqDTO headImageAo) {
		try {
			Result<Boolean> updatePhotoRs = remoteRsEmpPhotoService.updateHeadImage(headImageAo, SecurityConstants.FROM_IN);
			String syncPhotoState = updatePhotoRs.isSuccess() ? EmpImgSyncEnum.SUCCESS.getCode() : EmpImgSyncEnum.FAILD.getCode();
			// 更新同步状态
			identityCollectService.updatePhtoSync(perfectId, syncPhotoState);
		} catch (Exception e) {
			log.error("同步员工人脸信息到C6异常: {}", e.getMessage(), e);
		}
	}

	@Override
	public EmployeeSalayType getSalayType(String badge) {
		EmployeeSalayType employeeSalayType = new EmployeeSalayType();
		if (ObjectUtil.isNull(badge)) {
			throw new TCEException("员工号缺失");
		}
		//临时人员不查询薪资
		StaffInfoRespDTO staff = remoteStaff.getBaseinfoByBadge(badge, SecurityConstants.FROM_IN).data();
		if(Objects.nonNull(staff)) {
			if (staff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode())) {
				return null;
			}
		}
		EvwEmphrYsRespDTO data = remoteEvwEmphrYsService.info(badge, SecurityConstants.FROM_IN).get("员工信息不存在");
		employeeSalayType.setBadge(badge);
		employeeSalayType.setSalaryTypeName(data.getSalarytypeName());
		return employeeSalayType;
	}

	@Override
	public Result outRoomApplyDetail(Integer id) {
		Result result = outDormitoryStaffService.outRoomApplyDetail(id, SecurityConstants.FROM_IN);
		log.info("获取外宿审批详情: StaffBadge={}, Result={}", id, result.isSuccess());
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
		return result;
	}

	@Override
	public Result addCallowanceCancel(String staffBadge, String backDate, Integer type) {
		if (StringUtils.isBlank(staffBadge)) {
			staffBadge = SecurityUtils.getUser().getUsername();
		}
		Result result = remoteCallowanceCancelRecordService.save(staffBadge, backDate, type, SecurityConstants.FROM_IN);
		log.info("外宿补贴撤销申请: StaffBadge={}, Result={}", staffBadge, result.isSuccess());
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
		return result;
	}

	@Override
	public Result callowanceCancelList(String staffBadge) {
		if (StringUtils.isBlank(staffBadge)) {
			staffBadge = SecurityUtils.getUser().getUsername();
		}
		Result result = remoteCallowanceCancelRecordService.get(staffBadge, SecurityConstants.FROM_IN);
		log.info("外宿补贴撤销列表查询: StaffBadge={}, Result={}", staffBadge, result.isSuccess());
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
		return result;
	}

	@Override
	public Result callowanceCancelDetail(Integer id) {
		Result result = remoteCallowanceCancelRecordService.detail(id, SecurityConstants.FROM_IN);
		log.info("外宿补贴撤销记录详情: Id={}, Result={}", id, result.isSuccess());
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
		return result;
	}

	@Override
	public Result callowanceUInfo(String staffBadge, Integer type) {
		if (StringUtils.isBlank(staffBadge)) {
			staffBadge = SecurityUtils.getUser().getUsername();
		}
		Result result = remoteCallowanceCancelRecordService.getOutDormitory(staffBadge, type, SecurityConstants.FROM_IN);
		log.info("查询员工外宿补贴信息: Badge={}, Result={}", staffBadge, result.isSuccess());
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
		return result;
	}

	@Override
	public Result callowanceDetail(String staffBadge, Integer type) {
		if (StringUtils.isBlank(staffBadge)) {
			staffBadge = SecurityUtils.getUser().getUsername();
		}
		Result result = remoteCallowanceCancelRecordService.callowanceDetail(staffBadge, type, SecurityConstants.FROM_IN);
		log.info("查询外宿补贴详情: Badge={}, Result={}", staffBadge, result.isSuccess());
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
		return result;
	}

	@Override
	public Result getFreeBed(Integer parkId, String staffBadge) {
		if (StringUtils.isBlank(staffBadge)) {
			staffBadge = SecurityUtils.getUser().getUsername();
		}
		Result result = remoteStaff.getJcheFreeBed(parkId, staffBadge, SecurityConstants.FROM_IN);
		log.info("根据员工号查询空余床位: ParkId={}, Badge={}, Result={}", parkId, staffBadge, result.isSuccess());
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
		return result;
	}

	@Override
	public Result<Integer> getDormitorySet() {
		Result<Integer> result = outDormitoryStaffService.getDormitroySet(SecurityConstants.FROM_IN);
		log.info("查询外宿补贴开始时间的配置时间: Result={}", result.isSuccess());
		if (!result.isSuccess()) {
			throw new TCEException("获取外宿补贴设置失败");
		}
		return result;
	}

	@Override
	public Result outRoomList(Map<String, Object> params, String staffBadge) {
		if (StringUtils.isBlank(staffBadge)) {
			staffBadge = SecurityUtils.getUser().getUsername();
		}
		Result result = outDormitoryStaffService.getOutDormitoryPageList(
				MapUtil.getInt(params, PaginationConstants.CURRENT),
				MapUtil.getInt(params, PaginationConstants.SIZE),
				staffBadge,
				SecurityConstants.FROM_IN);
		log.info("查询员工外宿列表: Badge={}, Result={}", staffBadge, result.isSuccess());
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
		return result;
	}

	@Override
	public Result outRoomListDetail(Integer id) {
		Result result = outDormitoryStaffService.getOutDormitoryDetailById(id, SecurityConstants.FROM_IN);
		log.info("查询员工外宿详情: Id={}, Result={}", id, result.isSuccess());
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
		return result;
	}


	@Override
	public Result outRoomDetailById(String recordId) {
		Result result = outDormitoryStaffService.outRoomDetailById(recordId, SecurityConstants.FROM_IN);
		log.info("查询房间详情: RecordId={}, Result={}", recordId, result.isSuccess());
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
		return result;
	}

}
