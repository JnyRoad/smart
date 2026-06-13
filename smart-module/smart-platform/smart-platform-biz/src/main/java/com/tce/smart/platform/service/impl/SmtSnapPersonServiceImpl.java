package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SnapVehicleConstants;
import com.tce.smart.common.core.constant.enums.AlarmType;
import com.tce.smart.common.core.constant.enums.ExceptionType;
import com.tce.smart.common.core.constant.enums.SmtSnapPersonEnum;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.RegexUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.msg.req.AppointmentMsgReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendSmsErrorReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.data.api.vo.msg.SendSmsVo;
import com.tce.smart.platform.api.dto.IscTemperatureDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.AreaDeviceSnapRespDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkVisitorRespDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.mapper.SmtDeviceAreaMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtSnapPersonMapper;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.platform.core.vo.GetDeviceVO;
import com.tce.smart.platform.core.vo.SearchSmtSnapPersonVO;
import com.tce.smart.platform.core.vo.SearchVisitorDeviceAnalysisVO;
import com.tce.smart.platform.core.vo.SmtSnapPersonDetailVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.WeChatMsgUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 人员抓拍记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmtSnapPersonServiceImpl extends ServiceImpl<SmtSnapPersonMapper, SmtSnapPerson> implements SmtSnapPersonService {


	private final SmtVisitorService smtVisitorService;
	private final SmtFellowVisitorService smtFellowVisitorService;
	private final SmtStaffService smtStaffService;
	private final RemoteSmsManageService remoteSmsManageService;
	private final SmtAlarmRecordService smtAlarmRecordService;
	private final ImageService smtImageService;
	private final SmtDeviceService smtDeviceService;
	private final SmtDeviceMapper smtDeviceMapper;
	private final SmtAdmittanceFellowService smtAdmittanceFellowService;
	private final SmtAdmittanceApplyService smtAdmittanceApplyService;
	private final SmtDeviceAreaMapper smtDeviceAreaMapper;

	private final SmtDeviceAuthorityRelationService deviceAuthorityRelationService;

	private final SmtDeviceTaskService smtDeviceTaskService;

	private final SmtNoticeSwitchService smtNoticeSwitchService;

	private final SmtParkService smtParkService;

	private final SmtMsgTemplateService smtMsgTemplateService;

	@Value("${smart.hf-park-id}")
	private Integer hfParkId;


	/**
	 * 根据id查询人员出入的信息
	 */
	@Override
	public SmtSnapPersonDetailVO getSnapPersonById(Integer id) {
		SmtSnapPersonDetailVO smtSnapPersonDetail = new SmtSnapPersonDetailVO();
		//根据人员id查询抓拍人的相关信息
		SmtSnapPerson smtSnapPerson = this.getById(id);
		//根据id判断是否为空
		if (smtSnapPerson != null) {
			//判断是否为外部人员
			if (smtSnapPerson.getPersonType().equals(SmtSnapPersonEnum.SNAP_PERSON_TYPE2.getType())) {
				//根据外部人员id查询是否为访客的id
				if (getVisitorByPersonId(smtSnapPerson.getPersonId())) {
					//根据人员的id查询抓拍的详细信息
					smtSnapPersonDetail = this.baseMapper.getSnapPersonVisotorById(smtSnapPerson);
					smtSnapPersonDetail.setPhoto(smtImageService.buildImageUrl(smtSnapPersonDetail.getPhotoId()));
					smtSnapPersonDetail.setEventTypeDesc(EventTypeEnum.desc(smtSnapPersonDetail.getEventType()));
					smtSnapPersonDetail.setSnapPhoto(getPhotoUrl(smtSnapPersonDetail));
				}
				//根据外部人员id查询是否为随行人员的id
				if (getVisitorFellowByPersonId(smtSnapPerson.getPersonId())) {
					//根据随行人员的id查询抓拍的详细信息
					smtSnapPersonDetail = this.baseMapper.getSnapPersonFellowVisotorById(smtSnapPerson);
					smtSnapPersonDetail.setPhoto(smtImageService.buildImageUrl(smtSnapPersonDetail.getPhotoId()));
					smtSnapPersonDetail.setEventTypeDesc(EventTypeEnum.desc(smtSnapPersonDetail.getEventType()));
					smtSnapPersonDetail.setSnapPhoto(getPhotoUrl(smtSnapPersonDetail));
				}
				//根据外部人员id查询是否为入厂申请人员id
				if (smtAdmittanceFellowService.isExistFellow(smtSnapPerson.getPersonId())) {
					smtSnapPersonDetail = this.baseMapper.getSnapPersonAdmittanceFellowById(smtSnapPerson);
					smtSnapPersonDetail.setPhoto(smtImageService.buildImageUrl(smtSnapPersonDetail.getPhotoId()));
					smtSnapPersonDetail.setEventTypeDesc(EventTypeEnum.desc(smtSnapPersonDetail.getEventType()));
					smtSnapPersonDetail.setSnapPhoto(getPhotoUrl(smtSnapPersonDetail));
				}
			}
			if (smtSnapPerson.getPersonType().equals(SmtSnapPersonEnum.SNAP_PERSON_TYPE1.getType())) {
				//根据人员的id查询抓拍的详细信息为内部人员
				smtSnapPersonDetail = this.baseMapper.getSnapPersonById(smtSnapPerson);
				smtSnapPersonDetail.setStaffStatusDesc(StaffStatusEnum.desc(smtSnapPersonDetail.getStaffStatus()));
				smtSnapPersonDetail.setEventTypeDesc(EventTypeEnum.desc(smtSnapPersonDetail.getEventType()));
				smtSnapPersonDetail.setPhoto(smtImageService.buildImageUrl(smtSnapPersonDetail.getPhotoId()));
				smtSnapPersonDetail.setSnapPhoto(getPhotoUrl(smtSnapPersonDetail));

			}

			if (ObjectUtil.isNotNull(smtSnapPersonDetail.getParkId())) {
				SmtPark byId = smtParkService.getById(smtSnapPersonDetail.getParkId());
				smtSnapPersonDetail.setParkName(byId.getParkName());
			}

			//体温
			smtSnapPersonDetail.setFaceTemperature(smtSnapPerson.getFaceTemperature());
			smtSnapPersonDetail.setIsNormal(1);

			//查询设备的阈值
			SmtDevice device = smtDeviceService.getById(smtSnapPerson.getDeviceId());

			if (EnableStatusEnum.ENABLE.getCode().equals(device.getThermalEnable())
					&& smtSnapPerson.getFaceTemperature() != null
					&& device.getThermalThreshold() != null
					&& smtSnapPerson.getFaceTemperature() > device.getThermalThreshold()) {
				smtSnapPersonDetail.setIsNormal(0);
			}
		}
		return smtSnapPersonDetail;
	}

	private String getPhotoUrl(SmtSnapPersonDetailVO smtSnapPersonDetail) {
		return smtImageService.buildImageUrl(smtSnapPersonDetail.getParkId(), smtSnapPersonDetail.getSnapPhotoId());
	}

	/**
	 * 根据personID查询判断访客是否存在
	 */

	private Boolean getVisitorByPersonId(Long personId) {
		Integer selectCount = smtVisitorService
				.count(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getId, personId));
        return selectCount > 0;
    }


	/**
	 * 根据personID查询判断随行人员是否存在
	 */

	private Boolean getVisitorFellowByPersonId(Long personId) {
		Integer selectFellowCount = smtFellowVisitorService
				.count(Wrappers.<SmtFellowVisitor>query().lambda().eq(SmtFellowVisitor::getId, personId));
        return selectFellowCount > 0;
    }

	/**
	 * 根据personID查询判断员工id是否存在
	 */

	private Boolean getStaffByPersonId(Long personId) {
		Integer selectCount = smtStaffService
				.count(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getId, personId));
        return selectCount > 0;
    }

	/**
	 * 查询抓拍的人员的数据
	 */
	@Override
	public IPage<SearchSmtSnapPersonVO> getSmtSnapPersonPage(Page page, SearchSnapPersonAccessDTO searchSnapPersonAccessDto, String snapTime) {
		if (StringUtils.isNotBlank(snapTime)) {
			searchSnapPersonAccessDto.setStartTime(snapTime.split(",")[0]);
			searchSnapPersonAccessDto.setEndTime(snapTime.split(",")[1]);
		}
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		IPage<SearchSmtSnapPersonVO> searchSmtSnapPersonVo = null;
		if (searchSnapPersonAccessDto.getPersonType().equals(SmtSnapPersonEnum.SNAP_PERSON_TYPE2.getType())) {
			//当抓拍的数据为外来访客时
			searchSmtSnapPersonVo = this.baseMapper.getSmtSnapVisitorPersonPage(page, searchSnapPersonAccessDto, parkIdList);
			if (searchSmtSnapPersonVo.getRecords().size() > 0) {
				for (int i = 0; i < searchSmtSnapPersonVo.getRecords().size(); i++) {
					Long personId = searchSmtSnapPersonVo.getRecords().get(i).getPersonId();
					//根据外部人员id查询是否为访客的id
					if (getVisitorByPersonId(personId)) {
						SmtVisitor selectOne = smtVisitorService.getOne(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getId, personId));
						searchSmtSnapPersonVo.getRecords().get(i).setCompany(selectOne.getCompany());
					}
					//根据外部人员id查询是否为随行人员的id
					if (getVisitorFellowByPersonId(personId)) {
						SmtFellowVisitor selectFellowOne = smtFellowVisitorService.getOne(Wrappers.<SmtFellowVisitor>query().lambda().eq(SmtFellowVisitor::getId, personId));
						SmtVisitor selectVisitorOne = smtVisitorService.getOne(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getId, selectFellowOne.getVisitorId()));
						searchSmtSnapPersonVo.getRecords().get(i).setCompany(selectVisitorOne.getCompany());
					}
					//根据外部人员id查询是否为入厂申请人员id
					if (smtAdmittanceFellowService.isExistFellow(personId)) {
						SmtAdmittanceFellow fellow = smtAdmittanceFellowService.getById(personId);
						SmtAdmittanceApply apply = smtAdmittanceApplyService.getById(fellow.getVisitorId());
						searchSmtSnapPersonVo.getRecords().get(i).setCompany(apply.getCompany());
					}
				}
			}
		} else {
			//默认查员工的抓拍信息
			searchSmtSnapPersonVo = this.baseMapper.getSmtSnapPersonPage(page, searchSnapPersonAccessDto, parkIdList);
		}

		if (CollectionUtil.isNotEmpty(searchSmtSnapPersonVo.getRecords())) {
			List<String> deviceIdList = searchSmtSnapPersonVo.getRecords().stream().map(SearchSmtSnapPersonVO::getDeviceId).collect(Collectors.toList());
			List<String> deviceIds = deviceIdList.stream().distinct().collect(Collectors.toList());
			Collection<SmtDevice> devices = smtDeviceService.listByIds(deviceIds);
			Map<String, List<SmtDevice>> deviceMap = devices.stream().collect(Collectors.groupingBy(SmtDevice::getId));
			for (SearchSmtSnapPersonVO vo : searchSmtSnapPersonVo.getRecords()) {
				List<SmtDevice> deviceList = deviceMap.get(vo.getDeviceId());
				vo.setIsNormal(1);
				if (CollectionUtil.isNotEmpty(deviceList)) {
					SmtDevice snapDevice = deviceList.get(0);
					//检查设备的阈值
					if (EnableStatusEnum.ENABLE.getCode().equals(snapDevice.getThermalEnable())
							&& vo.getFaceTemperature() != null
							&& snapDevice.getThermalThreshold() != null
							&& vo.getFaceTemperature() > snapDevice.getThermalThreshold()) {
						vo.setIsNormal(0);
					}
				}
			}
		}

		return searchSmtSnapPersonVo;
	}

	/**
	 * 发送短信通知
	 *
	 * @param number
	 * @param visitorName
	 * @param tempCode
	 * @param hostName
	 * @param appointmentDate
	 * @param realityDate
	 * @param deviceName
	 * @return
	 */
	public Result<SendSmsVo> sendMessage(String number, String visitorName, String tempCode, String hostName, String appointmentDate, String realityDate, String deviceName) {
		//给访客发送短信,调用短信发送接口
		AppointmentMsgReqDTO appointmentMsgAo = new AppointmentMsgReqDTO();
		appointmentMsgAo.setNumber(number);
		appointmentMsgAo.setVisitorName(visitorName);
		appointmentMsgAo.setTempCode(tempCode);
		appointmentMsgAo.setHostName(hostName);
		appointmentMsgAo.setAppointmentDate(appointmentDate);
		appointmentMsgAo.setRealityDate(realityDate);
		appointmentMsgAo.setDeviceName(deviceName);
		//log.info("remoteSmsManageService:"+remoteSmsManageService);
		Result<SendSmsVo> sendAppointmentSms = remoteSmsManageService.sendAppointmentSms(appointmentMsgAo);
		//log.info("remoteSmsManageService.sendAppointmentSms Result={} :"+sendAppointmentSms);
		return sendAppointmentSms;
	}

	/**
	 * 短信发送失败后，再次发送
	 *
	 * @param number
	 * @param tempCode
	 * @param tempNameError
	 * @param remark
	 */
	public void sendMessageError(String number, String tempCode, String tempNameError, String remark) {
		SendSmsErrorReqDTO sendSmsErrorAo = new SendSmsErrorReqDTO();
		sendSmsErrorAo.setPhoneNumber(number);
		sendSmsErrorAo.setTempCode(tempCode);
		sendSmsErrorAo.setTempNameError(tempNameError);
		sendSmsErrorAo.setRemark(remark);
		//log.info("remoteSmsManageService:"+remoteSmsManageService);
		Result sendSmsError = remoteSmsManageService.sendSmsError(sendSmsErrorAo);
		//log.info("remoteSmsManageService.sendSmsError Result={} :"+sendSmsError);
	}


	/**
	 * 添加人员抓拍的记录信息
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public Result<Boolean> addSnapPerson(SaveSnapPersonDTO saveSnapPersonDTO) {
		//判断参数是否为空值
		if (ObjectUtil.isNull(saveSnapPersonDTO)) {
			return new Result<>(Boolean.FALSE, "人员抓拍参数不能为空");
		}

		//人员抓拍正则判断
		ExceptionType exceptionType = snapPersonCheck(saveSnapPersonDTO);
		if (!exceptionType.equals(ExceptionType.CHECK_SUCCESS)) {
			return new Result<>(Boolean.FALSE, exceptionType.getMessage());
		}
		//判断卡片是否为空
		if (StringUtils.isEmpty(saveSnapPersonDTO.getCardNo()) && !saveSnapPersonDTO.getLetPass().equals(LetPassEnum.LET_PASS_1.getCode())) {
			SmtAlarmRecord smtAlarmRecord = new SmtAlarmRecord();
			smtAlarmRecord.setDeviceId(saveSnapPersonDTO.getDeviceId());
			smtAlarmRecord.setAlarmType(AlarmType.STRANGER.getCode());
			smtAlarmRecord.setSnapId(saveSnapPersonDTO.getSnapPhotoId());
			smtAlarmRecord.setAlarmTime(DateUtils.parse(saveSnapPersonDTO.getSnapTime()));
			return new Result<>(smtAlarmRecordService.saveSmtAlarmRecord(smtAlarmRecord));
		}

		SmtSnapPerson smtSnapPerson = new SmtSnapPerson();
		if (StringUtils.isNotEmpty(saveSnapPersonDTO.getCardNo())) {
			smtSnapPerson.setPersonId(Long.parseLong(saveSnapPersonDTO.getCardNo()));
		}

		smtSnapPerson.setSnapPhotoId(saveSnapPersonDTO.getSnapPhotoId());
		smtSnapPerson.setEventType(saveSnapPersonDTO.getEventType());
		smtSnapPerson.setDeviceId(saveSnapPersonDTO.getDeviceId());
		smtSnapPerson.setCreateTime(LocalDateTime.now());
		smtSnapPerson.setSnapTime(DateUtils.parse(saveSnapPersonDTO.getSnapTime()));
		smtSnapPerson.setFaceTemperature(saveSnapPersonDTO.getFaceTemperature());

		SmtDevice smtDevice = smtDeviceService.getById(smtSnapPerson.getDeviceId());
		smtSnapPerson.setParkId(smtDevice.getParkId());
		if (ObjectUtil.isNotNull(smtDevice) && ObjectUtil.isNotNull(smtDevice.getEventType())) {
			smtSnapPerson.setEventType(smtDevice.getEventType());
		}
		//根据设备id查询通道号和地点
		GetDeviceVO getDeviceVo = getDevice(smtSnapPerson.getDeviceId());
		smtSnapPerson.setChannelNo(getDeviceVo.getChannelNo());
		smtSnapPerson.setAreaId(getDeviceVo.getAreaId());
		smtSnapPerson.setAreaName(getDeviceVo.getAreaName());
		//判断人员的id是否当为访客的id
		if (getVisitorByPersonId(smtSnapPerson.getPersonId())) {
			SmtVisitor getSmtVisitor = getSmtVisitor(smtSnapPerson.getPersonId());
			smtSnapPerson.setPersonName(getSmtVisitor.getVisitorName());
			smtSnapPerson.setPhotoId(getSmtVisitor.getVisitorPhotoId());
			smtSnapPerson.setPersonPhone(getSmtVisitor.getVisitorPhone());
			smtSnapPerson.setPersonType(SnapVehicleConstants.VISITOR_MASTER);
			//首次进门，并是未到达的状态下发短信
			//查询此人进门的记录
			List<SmtSnapPerson> oneIn = this.list(Wrappers.<SmtSnapPerson>query().lambda().eq(SmtSnapPerson::getPersonId, smtSnapPerson.getPersonId())
					.eq(SmtSnapPerson::getEventType, EventTypeEnum.EVENT_TYPE_1.getCode())
					.eq(SmtSnapPerson::getPersonType, SmtSnapPersonEnum.SNAP_PERSON_TYPE2.getType()));

			if (CollUtil.isNotEmpty(oneIn)) {
				if (smtSnapPerson.getEventType().equals(VehicleEventTypEnum.IN.getCode()) && !getSmtVisitor.getStatus()
						.equals(SmtVisitorEnum.COME_STATUS.getType())) {
					getSmtVisitor.setStatus(SmtVisitorEnum.COME_STATUS.getType());
					smtVisitorService.updateById(getSmtVisitor);
					//访客到访通知,调用短信发送接口
					SmtNoticeSwitch noticeSwitch = smtNoticeSwitchService.getOne(Wrappers.<SmtNoticeSwitch>query().lambda()
							.eq(SmtNoticeSwitch::getSwitchCode, ParkNoticeTypeEnum.VISITOR_ARRIVE_REAL.getCode())
							.eq(SmtNoticeSwitch::getIsOn, 1)
							.eq(SmtNoticeSwitch::getParkId, getSmtVisitor.getParkId()));
					if (ObjectUtil.isNotNull(noticeSwitch)) {
						Result<SendSmsVo> sendMessage = sendMessage(getSmtVisitor.getReceptionistPhone(), getSmtVisitor.getVisitorName(), SmsTemplateEnum.VISIT_1004.getCode(), getSmtVisitor.getReceptionistName(), DateUtils.formatDateTime(getSmtVisitor.getStartTime()), saveSnapPersonDTO.getSnapTime(), getDeviceVo.getDeviceName());
						if (!sendMessage.isSuccess()) {
							//发送失败后，要发短息通知
							SmtNoticeSwitch noticeSwitchFail = smtNoticeSwitchService.getOne(Wrappers.<SmtNoticeSwitch>query().lambda()
									.eq(SmtNoticeSwitch::getSwitchCode, ParkNoticeTypeEnum.SMS_SEND_FAILD.getCode())
									.eq(SmtNoticeSwitch::getIsOn, 1)
									.eq(SmtNoticeSwitch::getParkId, getSmtVisitor.getParkId()));
							if (ObjectUtil.isNotNull(noticeSwitchFail)) {
								sendMessageError(getSmtVisitor.getReceptionistPhone(), SmsTemplateEnum.SMS_12001.getCode(), SmsTemplateEnum.VISIT_1004.getDesc(), sendMessage.getMsg());
							}
						}
					}
				}
			}
			//访客首次出门下发短信,除驻场人员外
			List<SmtSnapPerson> oneOut = this.list(Wrappers.<SmtSnapPerson>query().lambda().eq(SmtSnapPerson::getPersonId, smtSnapPerson.getPersonId()).eq(SmtSnapPerson::getEventType, EventTypeEnum.EVENT_TYPE_2.getCode()).eq(SmtSnapPerson::getPersonType, SmtSnapPersonEnum.SNAP_PERSON_TYPE2.getType()));
			//log.info("查询此人出门的记录: {}, {}", smtSnapPerson.getPersonId(), oneOut);
			if (Objects.isNull(oneOut) || oneOut.size() == 0) {
				if (smtSnapPerson.getEventType().equals(VehicleEventTypEnum.OUT.getCode()) && !getSmtVisitor.getCause().equals(VisitorEnum.CAUSE_5.getCode())) {
					//查询进门的记录
					getSmtVisitor.setStatus(VisitorStatusEnum.CAUSE_5.getCode());
					smtVisitorService.updateById(getSmtVisitor);
					//查询访客离开短信通知开关配置 该项开关默认情况下是没有配置的
					SmtNoticeSwitch noticeSwitch = smtNoticeSwitchService.getOne(Wrappers.<SmtNoticeSwitch>query().lambda()
							.eq(SmtNoticeSwitch::getSwitchCode, ParkNoticeTypeEnum.VISITOR_LEAVE.getCode())
							.eq(SmtNoticeSwitch::getParkId, getSmtVisitor.getParkId()));
					Integer isOn = 1;
					if (null == noticeSwitch || isOn.equals(noticeSwitch.getIsOn())) {
						sendMessage(getSmtVisitor.getReceptionistPhone(), getSmtVisitor.getVisitorName(), SmsTemplateEnum.VISIT_1007.getCode(), getSmtVisitor.getReceptionistName(), DateUtils.formatDateTime(getSmtVisitor.getStartTime()), saveSnapPersonDTO.getSnapTime(), getDeviceVo.getDeviceName());
					}
					//访客出门后删除闸机
					//查询访客人员设备权限
					//访客出门不删除权限
//					List<SmtDeviceAuthorityRelation> deviceAuthList = deviceAuthorityRelationService.getRelationAuth(getSmtVisitor.getParkId(),
//							BusinessAuthorityEnum.VISITOR_FACE.getCode(), DeviceAuthorityEnum.VISITOR);
//					saveVisitorCardDelTask(getSmtVisitor, deviceAuthList);
				}
			}

		/*	if(!getSmtVisitor.getStatementStatus().equals(SmtVisitorEnum.COME_STATUS.getType()))
			{
				getSmtVisitor.setStatementStatus(SmtVisitorEnum.COME_STATUS.getType());
				smtVisitorService.updateById(getSmtVisitor);
			}*/
		}
		//判断人员的id是否为随行人员的id
		else if (getVisitorFellowByPersonId(smtSnapPerson.getPersonId())) {
			SmtFellowVisitor getSmtFellowVisitor = getSmtFellowVisitor(smtSnapPerson.getPersonId());
			//根据随行人员查询访客的id根据访客的id查询访客的手机号
			SmtVisitor selectOne = smtVisitorService.getOne(Wrappers.<SmtVisitor>query().lambda().eq(SmtVisitor::getId, getSmtFellowVisitor.getVisitorId()));
			if (smtSnapPerson.getEventType().equals(VehicleEventTypEnum.OUT.getCode()) && !selectOne.getCause().equals(VisitorEnum.CAUSE_5.getCode())) {
				//非园区驻场的随行人员 离开时 应马上删除通行权限
				//查询访客人员设备权限
				List<SmtDeviceAuthorityRelation> deviceAuthList = deviceAuthorityRelationService.getRelationAuth(selectOne.getParkId(),
						BusinessAuthorityEnum.VISITOR_FACE.getCode(), DeviceAuthorityEnum.VISITOR);
				saveFlowCardDelTask(getSmtFellowVisitor, deviceAuthList);
			}
			smtSnapPerson.setPersonName(getSmtFellowVisitor.getFellowName());
			smtSnapPerson.setPhotoId(getSmtFellowVisitor.getFellowPhotoId());
			smtSnapPerson.setPersonPhone(selectOne.getVisitorPhone());
			smtSnapPerson.setPersonType(SnapVehicleConstants.VISITOR_MASTER);
		} else if (smtAdmittanceFellowService.isExistFellow(smtSnapPerson.getPersonId())) {
			SmtAdmittanceFellow fellow = smtAdmittanceFellowService.getById(smtSnapPerson.getPersonId());
			SmtAdmittanceApply apply = smtAdmittanceApplyService.getById(fellow.getVisitorId());
			//首次进门修改入厂申请到访状态
			if (VehicleEventTypEnum.IN.getCode().equals(smtSnapPerson.getEventType()) &&
					!SmtVisitorEnum.COME_STATUS.getType().equals(apply.getStatus())) {
				List<SmtSnapPerson> oneIn = this.list(Wrappers.<SmtSnapPerson>query().lambda().eq(SmtSnapPerson::getPersonId, smtSnapPerson.getPersonId())
						.eq(SmtSnapPerson::getEventType, EventTypeEnum.EVENT_TYPE_1.getCode())
						.eq(SmtSnapPerson::getPersonType, SmtSnapPersonEnum.SNAP_PERSON_TYPE2.getType()));
				if (CollUtil.isEmpty(oneIn)) {
					apply.setStatus(SmtVisitorEnum.COME_STATUS.getType());
					smtAdmittanceApplyService.updateById(apply);
					//发送短信
					SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_ADMITTANCE_10904.getCode());
					SmtDevice selectDeviceById = smtDeviceService.getById(smtSnapPerson.getDeviceId());
					String msg = template.getTempContent().replace("{来访单位}", apply.getCompany())
							.replace("{实际来访时间}", DateUtils.convert(LocalDateTime.now()))
							.replace("{刷脸的门}", selectDeviceById.getDeviceName())
							.replace("{访客姓名}", apply.getVisitorName());
					try {
						WeChatMsgUtil.sendMsg(apply.getReceptionistBadge(), msg, null, null);
					} catch (Exception e) {
						log.error("微信推送失败{}", e.getMessage());
					}
				}
			}
			if (VehicleEventTypEnum.OUT.getCode().equals(smtSnapPerson.getEventType()) &&
					!VisitorStatusEnum.CAUSE_5.getCode().equals(apply.getStatus())) {
				//访客首次出门下发短信
				List<SmtSnapPerson> oneOut = this.list(Wrappers.<SmtSnapPerson>query().lambda()
						.eq(SmtSnapPerson::getPersonId, smtSnapPerson.getPersonId())
						.eq(SmtSnapPerson::getEventType, EventTypeEnum.EVENT_TYPE_2.getCode())
						.eq(SmtSnapPerson::getPersonType, SmtSnapPersonEnum.SNAP_PERSON_TYPE2.getType()));
				if (CollUtil.isEmpty(oneOut)) {
					//查询进门的记录
					apply.setStatus(VisitorStatusEnum.CAUSE_5.getCode());
					smtAdmittanceApplyService.updateById(apply);
					SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_ADMITTANCE_10905.getCode());
					SmtDevice selectDeviceById = smtDeviceService.getById(smtSnapPerson.getDeviceId());
					String msg = template.getTempContent().replace("{来访单位}", apply.getCompany())
							.replace("{被访人姓名}", apply.getReceptionistName())
							.replace("{访客姓名}", apply.getVisitorName())
							.replace("{实际离开时间}", DateUtils.convert(LocalDateTime.now()))
							.replace("{刷脸的门}", selectDeviceById.getDeviceName());
					try {
						WeChatMsgUtil.sendMsg(apply.getReceptionistBadge(), msg, null, null);
					} catch (Exception e) {
						log.error("微信推送失败{}", e.getMessage());
					}
				}
			}
			smtSnapPerson.setPersonName(fellow.getFellowName());
			smtSnapPerson.setPhotoId(fellow.getFellowPhotoId());
			smtSnapPerson.setPersonPhone(apply.getVisitorPhone());
			smtSnapPerson.setPersonType(SnapVehicleConstants.VISITOR_MASTER);
		}
		//判断人员的id是否为员工的id
		else if (getStaffByPersonId(smtSnapPerson.getPersonId())) {
			SmtStaff getSmtStaff = getSmtStaff(smtSnapPerson.getPersonId());
			smtSnapPerson.setPersonName(getSmtStaff.getName());
			smtSnapPerson.setPhotoId(getSmtStaff.getFacePicId());
			smtSnapPerson.setPersonPhone(getSmtStaff.getPhone());
			smtSnapPerson.setPersonType(SnapVehicleConstants.STAFF_MASTER);
		}
		return new Result<>(this.save(smtSnapPerson));
	}

	@Override
	public Boolean checkTemperature(List<IscTemperatureDTO> dto){
		List<SmtSnapPerson> personList = this.list(Wrappers.<SmtSnapPerson>query()
				.lambda().isNull(SmtSnapPerson::getFaceTemperature).eq(SmtSnapPerson::getParkId, hfParkId)
				.isNotNull(SmtSnapPerson::getPersonId)
				.ge(SmtSnapPerson::getCreateTime,DateUtils.beginOfDay(new Date())));
		if(CollUtil.isEmpty(personList)) {
			return Boolean.FALSE;
		}
		dto.forEach(temp -> {
			SmtStaff staff = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getCertno, temp.getCertNo()), false);
			if(Objects.isNull(staff) || CollUtil.isEmpty(personList)) {
				return;
			}
			List<SmtSnapPerson> persons = personList.stream().filter(item -> item.getPersonId().equals(staff.getId())).collect(Collectors.toList());
			personList.forEach(personResult -> {
				personResult.setFaceTemperature(Double.valueOf(temp.getTemp()));
				this.updateById(personResult);
			});
			personList.removeAll(persons);
		});
		return Boolean.TRUE;
	}

	/**
	 * @Title:查询设备id通道和地点
	 * @Param :
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月18日 上午11:42:27
	 */
	private GetDeviceVO getDevice(String id) {
		SmtDevice smtDevice = new SmtDevice();
		smtDevice.setId(id);
		/*		GetDeviceVO getDeviceVo = smtDeviceMapper.getDeviceById(smtDevice);
		 */
		GetDeviceVO getDeviceVo = smtDeviceMapper.getDeviceById(id);
		return getDeviceVo;
	}

	/**
	 * @Title:根据访客的id查询访客
	 * @Param :
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月18日 上午11:42:27
	 */
	private SmtVisitor getSmtVisitor(Long PersonId) {
		SmtVisitor selectById = smtVisitorService.getById(PersonId);
		return selectById;
	}

	/**
	 * @Title:根据访客的id查询随行人员的
	 * @Param :
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月18日 上午11:42:27
	 */
	private SmtFellowVisitor getSmtFellowVisitor(Long PersonId) {
		SmtFellowVisitor selectById = smtFellowVisitorService.getById(PersonId);
		return selectById;
	}

	/**
	 * @Title:根据访客的id查询员工的
	 * @Param :
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月18日 上午11:42:27
	 */
	private SmtStaff getSmtStaff(Long PersonId) {
		SmtStaff selectById = smtStaffService.getById(PersonId);
		return selectById;
	}

	/**
	 * 人员抓拍的正则判断
	 *
	 * @param saveSnapPersonDTO
	 * @return
	 */
	private ExceptionType snapPersonCheck(SaveSnapPersonDTO saveSnapPersonDTO) {
		String snapPhotoId = saveSnapPersonDTO.getSnapPhotoId();
		String snapTime = saveSnapPersonDTO.getSnapTime();
		String deviceId = saveSnapPersonDTO.getDeviceId();
		Integer eventType = saveSnapPersonDTO.getEventType();

		if (Objects.isNull(eventType)) {
			return ExceptionType.SNAP_PERSON_EVENT_TYPE_EMPTY;
		}
		if (!RegexUtils.matchEventType(eventType.toString())) {
			return ExceptionType.SNAP_PERSON_EVENT_TYPE_ERROR;
		}
		if (StringUtils.isEmpty(snapPhotoId)) {
			return ExceptionType.SNAP_PHOTO_ID_EMPTY;
		}
		if (StringUtils.isEmpty(snapTime)) {
			return ExceptionType.SNAP_TIME_ERROR;
		}
		if (!RegexUtils.matchDate(snapTime)) {
			return ExceptionType.SNAP_TIME_ERROR;
		}
		if (StringUtils.isEmpty(deviceId)) {
			return ExceptionType.SNAP_DEVICE_ID_ERROR;
		}
		//判断设备id是否存在
		SmtDevice selectById = smtDeviceMapper.selectById(deviceId);
		if (ObjectUtil.isNull(selectById)) {
			return ExceptionType.SNAP_DEVICE_ID_ERROR;
		}
		return ExceptionType.CHECK_SUCCESS;
	}

	/**
	 * 添加访客卡片删除任务
	 *
	 * @param smtVisitor     访客信息
	 * @param deviceAuthList 设备权限列表
	 */
	private void saveVisitorCardDelTask(SmtVisitor smtVisitor, List<SmtDeviceAuthorityRelation> deviceAuthList) {
		DeviceTaskVO deviceTaskVO;
		for (int i = 0; i < deviceAuthList.size(); i++) {
			//查询是否已生成删除任务
			SmtDeviceTask deviceTask = smtDeviceTaskService.getOne(new LambdaQueryWrapper<SmtDeviceTask>()
					.eq(SmtDeviceTask::getCardNo, smtVisitor.getId())
					.eq(SmtDeviceTask::getDeviceCode, deviceAuthList.get(i).getDeviceId())
					.eq(SmtDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
					.eq(SmtDeviceTask::getServiceType, DeviceTaskServiceTypeEnum.CARD_VISITOR.getCode())
					.eq(SmtDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
			);
			if (null != deviceTask) {
				//访客预约已存在待处理的删除任务 访客出门后 把删除时间调整为当前
				deviceTask.setOverTime(DateUtils.currentSeconds());
				smtDeviceTaskService.updateById(deviceTask);
			} else {
				deviceTaskVO = new DeviceTaskVO();
				deviceTaskVO.setAction(DeviceTaskConstants.DEL);
				deviceTaskVO.setCardNo(smtVisitor.getId().toString());
				deviceTaskVO.setDeviceCode(deviceAuthList.get(i).getDeviceId());
				deviceTaskVO.setStartTime(DateUtils.currentSeconds());
				deviceTaskVO.setOverTime(DateUtils.currentSeconds());
				deviceTaskVO.setImageId(smtVisitor.getVisitorPhotoId());
				deviceTaskVO.setGeneral(smtVisitor.getVisitorName());
				deviceTaskVO.setServiceType(DeviceTaskServiceTypeEnum.CARD_VISITOR.getCode());
				smtDeviceTaskService.saveTask(deviceTaskVO);
			}
		}
	}

	/**
	 * 添加访客随行人员卡片删除任务
	 *
	 * @param fellowVisitor  访客随时人员信息
	 * @param deviceAuthList 设备权限列表
	 */
	private void saveFlowCardDelTask(SmtFellowVisitor fellowVisitor, List<SmtDeviceAuthorityRelation> deviceAuthList) {
		DeviceTaskVO deviceTaskVO;
		for (int i = 0; i < deviceAuthList.size(); i++) {
			//查询是否已生成删除任务
			SmtDeviceTask deviceTask = smtDeviceTaskService.getOne(new LambdaQueryWrapper<SmtDeviceTask>()
					.eq(SmtDeviceTask::getCardNo, fellowVisitor.getId())
					.eq(SmtDeviceTask::getDeviceCode, deviceAuthList.get(i).getDeviceId())
					.eq(SmtDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
					.eq(SmtDeviceTask::getServiceType, DeviceTaskServiceTypeEnum.CARD_VISITOR.getCode())
					.eq(SmtDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
			);
			if (null != deviceTask) {
				//访客预约随性人员已存在待处理的删除任务 随性人员出门后 把删除时间调整为当前
				deviceTask.setOverTime(DateUtils.currentSeconds());
				smtDeviceTaskService.updateById(deviceTask);
			} else {
				deviceTaskVO = new DeviceTaskVO();
				deviceTaskVO.setAction(DeviceTaskConstants.DEL);
				deviceTaskVO.setCardNo(fellowVisitor.getId().toString());
				deviceTaskVO.setDeviceCode(deviceAuthList.get(i).getDeviceId());
				deviceTaskVO.setStartTime(DateUtils.currentSeconds());
				deviceTaskVO.setOverTime(DateUtils.currentSeconds());
				deviceTaskVO.setImageId(fellowVisitor.getFellowPhotoId());
				deviceTaskVO.setGeneral(fellowVisitor.getFellowName());
				deviceTaskVO.setServiceType(DeviceTaskServiceTypeEnum.CARD_VISITOR.getCode());
				smtDeviceTaskService.saveTask(deviceTaskVO);
			}
		}
	}

	@Override
	public ParkVisitorRespDTO getVisitorInfo(Integer parkId) {
		List<SnapPersonStatisDTO> snapPersonStatisDTOList = this.baseMapper.getSnapPersonStatis(parkId);
		Map<Integer, List<SnapPersonStatisDTO>> map = snapPersonStatisDTOList.stream().collect(Collectors.groupingBy(SnapPersonStatisDTO::getAreaId));

		//各位置进出记录
		List<ParkVisitorRespDTO.InOutRecord> inOutRecords = new ArrayList<>();
		//总进入数
		Integer inTotalCount = 0;
		//总离开数
		Integer outTotalCount = 0;

		for (Map.Entry<Integer, List<SnapPersonStatisDTO>> entry : map.entrySet()) {
			ParkVisitorRespDTO.InOutRecord inOutRecord = new ParkVisitorRespDTO.InOutRecord();
			inOutRecord.setAreaName(entry.getValue().get(0).getAreaName());
			for (SnapPersonStatisDTO snapPersonStatisDTO : entry.getValue()) {
				if (snapPersonStatisDTO.getEventType().equals(VehicleEventTypEnum.IN.getCode())) {
					//进
					if (inOutRecord.getInCount() == null) {
						inOutRecord.setInCount(0);
					}
					inOutRecord.setInCount(inOutRecord.getInCount() + snapPersonStatisDTO.getCount());
					inTotalCount += snapPersonStatisDTO.getCount();
				} else if (snapPersonStatisDTO.getEventType().equals(VehicleEventTypEnum.OUT.getCode())) {
					//出
					if (inOutRecord.getOutCount() == null) {
						inOutRecord.setOutCount(0);
					}
					inOutRecord.setOutCount(inOutRecord.getOutCount() + snapPersonStatisDTO.getCount());
					outTotalCount += snapPersonStatisDTO.getCount();
				}
				inOutRecords.add(inOutRecord);
			}
		}
		return ParkVisitorRespDTO.builder()
				.inCount(inTotalCount)
				.outCount(outTotalCount)
				.inOutRecords(inOutRecords)
				.build();
	}

	@Override
	public List<AreaDeviceSnapRespDTO> getAreaDeviceSnapData(Integer parkId) {
		//List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		List<AreaDeviceDTO> areaDeviceDTO = smtDeviceAreaMapper.queryAreaDevice(parkId);
		Map<Integer, List<AreaDeviceDTO>> collect = areaDeviceDTO.stream().collect(Collectors.groupingBy(AreaDeviceDTO::getId));

		List<AreaDeviceSnapRespDTO> areaDeviceSnapRespDTOS = new ArrayList<>();
		//遍历区域设备信息
		for (Map.Entry<Integer, List<AreaDeviceDTO>> entry : collect.entrySet()) {
			AreaDeviceSnapRespDTO areaDeviceSnapRespDTO = AreaDeviceSnapRespDTO.builder()
					.areaName(entry.getValue().get(0).getAreaName())
					.build();
			List<AreaDeviceSnapRespDTO.SnapData> snapDataList = new ArrayList<>();
			for (AreaDeviceDTO areaDeviceDTO1 : entry.getValue()) {
				//使用以前的方法
				SearchVisitorDeviceAnalysisVO searchVisitorDeviceAnalysisVO = new SearchVisitorDeviceAnalysisVO();
				smtVisitorService.getSnapPersonLasted(searchVisitorDeviceAnalysisVO, areaDeviceDTO1.getDeviceId());

				//拷贝数据
				AreaDeviceSnapRespDTO.SnapData snapData = new AreaDeviceSnapRespDTO.SnapData();
				if (searchVisitorDeviceAnalysisVO.getSnapPhotoUrl() != null) {
					BeanUtils.copyProperties(searchVisitorDeviceAnalysisVO, snapData);
					snapDataList.add(snapData);
				}
			}
			areaDeviceSnapRespDTO.setSnapDataList(snapDataList);

			areaDeviceSnapRespDTOS.add(areaDeviceSnapRespDTO);
		}
		return areaDeviceSnapRespDTOS;
	}


	/**
	 * 人员卡片删除
	 *
	 * @param visitorId 访客预约编号
	 * @param deviceId 设备编号
	 * @return
	 */
/*	private CardDTO delCardInfo(String visitorId, String deviceId) {
		CardDTO cardDTO = new CardDTO();
		cardDTO.setCardNo(visitorId);
		cardDTO.setDeviceCode(deviceId);
		return cardDTO;
	}*/
	/*	*//**
	 * 根据图片id查询图片
	 * @param PhotoId
	 *//*
	public String getPhoto(String photoId) {
		if(!StringUtils.isEmpty(photoId)) {
			return  remoteBlobService.getBlob(photoId, SecurityConstants.FROM_IN).getData();
		}
		return null;
	}*/
}
