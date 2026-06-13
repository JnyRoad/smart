package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SnapVehicleConstants;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.msg.req.GuardMsgReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.platform.core.dto.AddSnapVehicleDTO;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.dto.LogisticsAppointmentDTO;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtLogisticsAppointment;
import com.tce.smart.platform.core.entity.SmtParkLogistics;
import com.tce.smart.platform.core.mapper.SmtDeviceAuthorityRelationMapper;
import com.tce.smart.platform.core.mapper.SmtLogisticsAppointmentMapper;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtLogisticsAppointmentService;
import com.tce.smart.platform.service.SmtParkLogisticsService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.LogisticsAppointmentConstants;
import com.tce.smart.tool.enums.BusinessAuthorityEnum;
import com.tce.smart.tool.enums.DeviceAuthorityEnum;
import com.tce.smart.tool.enums.SmsTemplateEnum;
import com.tce.smart.tool.enums.VehicleBelongTypeEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流车预约信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:27
 */
@Service
@Slf4j
@AllArgsConstructor
public class SmtLogisticsAppointmentServiceImpl extends ServiceImpl<SmtLogisticsAppointmentMapper, SmtLogisticsAppointment> implements SmtLogisticsAppointmentService {
	private final SmtLogisticsAppointmentMapper logisticsAppointmentMapper;
	private final SmtDeviceAuthorityRelationMapper smtDeviceAuthorityRelationMapper;
	private final SmtDeviceTaskService smtDeviceTaskService;
	private final RemoteSmsManageService remoteSmsManageService;
	private final SmtParkLogisticsService smtParkLogisticsService;
	private final SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;
	private final SmtParkService smtParkService;

	/**
	 * 获取物流车预约统计信息
	 *
	 * @param smtLogisticsAppointment 车辆人员信息
	 * @return 返回保存结果
	 */
	@Override
	public IPage getLogisticsAppointment(Page page, SmtLogisticsAppointment smtLogisticsAppointment,List<Integer> parkIds) {
		return logisticsAppointmentMapper.getLogisticsAppointment(page, smtLogisticsAppointment,parkIds);
	}

	@Override
	public boolean saveLogisticsAppointment(LogisticsAppointmentDTO logisticsAppointmentDTO) {
		boolean result = false;
		SmtLogisticsAppointment entity = new SmtLogisticsAppointment();
		logisticsAppointmentDTO.setStatus(LogisticsAppointmentConstants.BOOKED);
		logisticsAppointmentDTO.setCreateTime(LocalDateTime.now());
		BeanUtil.copyProperties(logisticsAppointmentDTO,entity);
		SmtLogisticsAppointment logisticsAppointment = this.baseMapper.selectOne(Wrappers.<SmtLogisticsAppointment>query().lambda().eq(SmtLogisticsAppointment::getPlanCode, logisticsAppointmentDTO.getPlanCode()));
		if(ObjectUtil.isNull(logisticsAppointment)) {
			SmtParkLogistics smtParkLogistics = smtParkLogisticsService.getOne(Wrappers.<SmtParkLogistics>query().lambda().eq(SmtParkLogistics::getCompanyId,logisticsAppointmentDTO.getCompanyId()));
			if(ObjectUtil.isNotNull(smtParkLogistics)){
				entity.setParkId(smtParkLogistics.getParkId());
				result = this.save(entity);
				DeviceTaskVO deviceTaskVO = null;
				if(result) {
					List<SmtDeviceAuthorityRelation> selectList = smtDeviceAuthorityRelationService
							.getRelationAuth(logisticsAppointmentDTO.getParkId(), BusinessAuthorityEnum.LOGISTICS_APPOINTMENT.getCode(), DeviceAuthorityEnum.LOGISTICS_APPOINTMENT);
					for (int i = 0; i < selectList.size(); i++) {
						//注册物流车预约信息
						deviceTaskVO = new DeviceTaskVO();
						deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
						deviceTaskVO.setServiceType(DeviceTaskConstants.CAR_GUARD);
						deviceTaskVO.setDeviceCode(selectList.get(i).getDeviceId());
						deviceTaskVO.setCardNo(entity.getId().toString());
						deviceTaskVO.setStatus(0);
						deviceTaskVO.setCardType(SmtVisitorEnum.CAR_CARD_TYPE_1.getType());
						deviceTaskVO.setGeneral(logisticsAppointmentDTO.getVehiclePlate());
						deviceTaskVO.setDeviceType(DeviceTaskConstants.CAR);
						deviceTaskVO.setOverTime(logisticsAppointmentDTO.getEndTime().getTime()/1000);
						deviceTaskVO.setStartTime(DateUtil.offsetHour(logisticsAppointmentDTO.getStartTime(), -6).getTime()/1000);
						smtDeviceTaskService.saveTask(deviceTaskVO);
					}
					String park = smtParkService.getById(logisticsAppointmentDTO.getParkId()).getParkName();
					String[] phones = logisticsAppointmentDTO.getDriverPhone().split("/");
                    for (String string : phones) {
                        if (StrUtil.isNotBlank(string) && string.length() == 11) {
                            sendMessage(string, logisticsAppointmentDTO.getDriverName(), DateUtil.format(logisticsAppointmentDTO.getStartTime(), DatePattern.NORM_DATE_PATTERN), SmsTemplateEnum.GUARD_11001.getCode(), park, logisticsAppointmentDTO.getVehiclePlate());
                        }
                    }
                }
			}

		}
	    return result;
	}

	/**
	 * 发送短信通知
	 * @param number 手机号
	 * @param name 用户名
	 * @param date 来访时间
	 * @param tempCode 编码
	 */
	public void sendMessage(String number, String name, String date, String tempCode, String parkName, String plat) {
		GuardMsgReqDTO guradMsgAo = new GuardMsgReqDTO();
		guradMsgAo.setNumber(number);
		guradMsgAo.setVisitorName(name);
		guradMsgAo.setParkName(parkName);
		guradMsgAo.setPlat(plat);
		guradMsgAo.setTempCode(tempCode);
		guradMsgAo.setAppointmentDate(date);
		Result<?> result = remoteSmsManageService.sendGuardSms(guradMsgAo);
		log.debug("短消息发送结果：{}",result);
	}


	/**
	 * 车辆抓拍记录物流车车辆信息补充
	 * @param entity 抓拍车辆信息
	 */
	@Override
	public void logisticsAppointmentHandle(AddSnapVehicleDTO entity) {
		if(StrUtil.isNotBlank(entity.getCardNo())) {
			SmtLogisticsAppointment logisticsAppointmentList  = logisticsAppointmentMapper.queryByVehicleID(entity.getCardNo());
			if(ObjectUtil.isNotNull(logisticsAppointmentList)) {
				//修改状态
				if(entity.getEventType().equals(SnapVehicleConstants.DIRECTION_IN)) {
					//修改为已到达
					logisticsAppointmentList.setStatus(LogisticsAppointmentConstants.ARRIVED);
					logisticsAppointmentList.setArrivalTime(entity.getSnapTime());
				}else if(entity.getEventType().equals(SnapVehicleConstants.DIRECTION_OUT)) {
					//修改为已离开
					logisticsAppointmentList.setLeaveTime(entity.getSnapTime());
					logisticsAppointmentList.setStatus(LogisticsAppointmentConstants.ALREADY_LEFT);
				}
				// 对应的物流车信息则补全信息 并更新预约信息
				entity.setVehicleAscription(VehicleBelongTypeEnum.LOGISTICS_VEHICLE.getCode());
				entity.setDriverId(logisticsAppointmentList.getId());
				entity.setDriverName(logisticsAppointmentList.getDriverName());
				entity.setDriverPhone(logisticsAppointmentList.getDriverPhone());
				entity.setDriverType(VehicleBelongTypeEnum.LOGISTICS_VEHICLE.getCode());
				 //根据ID修改
				this.update(logisticsAppointmentList,Wrappers.<SmtLogisticsAppointment>query().lambda().eq(SmtLogisticsAppointment::getId, logisticsAppointmentList.getId()));
			}
		}
	}

	/**
	 * 手动进厂
	 * @param id 物流车预约ID
	 * @return
	 */
	@Override
	public boolean manualEnter(Long id) {
		SmtLogisticsAppointment logisticsAppointment = this.getById(id);
		logisticsAppointment.setArrivalTime(DateUtil.date());
		logisticsAppointment.setStatus(LogisticsAppointmentConstants.ARRIVED);
		return this.updateById(logisticsAppointment);
	}

	/**
	 * 返回预约
	 * @param id 物流车预约ID
	 * @return
	 */
	@Override
	public boolean goOrder(Long id) {
		SmtLogisticsAppointment logisticsAppointment = this.getById(id);
		logisticsAppointment.setStatus(LogisticsAppointmentConstants.BOOKED);
		return this.updateById(logisticsAppointment);
	}

	/**
	 * 手动离厂
	 * @param id 物流车预约ID
	 * @return
	 */
	@Override
	public boolean manualLeave(Long id) {
	    SmtLogisticsAppointment entity = this.getById(id);
	    entity.setLeaveTime(DateUtil.date());
		entity.setStatus(LogisticsAppointmentConstants.ALREADY_LEFT);
		boolean result = this.updateById(entity);
	    return result;
	}

	/**
	 * 取消预约
	 * @param id 物流车预约ID
	 * @return
	 */
	@Override
	public boolean cancelOrder(Long id) {
		SmtLogisticsAppointment entity = this.getById(id);
		entity.setStatus(LogisticsAppointmentConstants.CANCEL);
		boolean result = this.updateById(entity);
		DeviceTaskVO deviceTaskVO = null;
	    if(result) {
		List<SmtDeviceAuthorityRelation> selectList = smtDeviceAuthorityRelationService
					.getRelationAuth(entity.getParkId(), BusinessAuthorityEnum.LOGISTICS_APPOINTMENT.getCode(), DeviceAuthorityEnum.LOGISTICS_APPOINTMENT);
		    for (int i = 0; i < selectList.size(); i++) {
				//注销物流车预约信息,
				deviceTaskVO = new DeviceTaskVO();
			deviceTaskVO.setAction(DeviceTaskConstants.DEL);
				deviceTaskVO.setCardNo(entity.getId().toString());
				deviceTaskVO.setDeviceCode(selectList.get(i).getDeviceId());
			smtDeviceTaskService.saveTask(deviceTaskVO);
			}
	    }
		return result;

	}

	/**
	 * 返回在厂
	 * @param id 物流车预约ID
	 * @return
	 */
	@Override
	public boolean goIn(Long id) {
		SmtLogisticsAppointment entity = this.getById(id);
		entity.setLeaveTime(DateUtil.date());
		entity.setStatus(LogisticsAppointmentConstants.ARRIVED);
		boolean result = this.updateById(entity);
	    return result;

	}

	@Override
	public boolean updateStatus() {
		return logisticsAppointmentMapper.updateStatus() > 0;
	}
}