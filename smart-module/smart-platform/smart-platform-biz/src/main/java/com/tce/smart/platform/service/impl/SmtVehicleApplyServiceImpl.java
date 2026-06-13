package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.dto.VehicleApplyDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtParkMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.mapper.SmtVehicleApplyMapper;
import com.tce.smart.platform.core.mapper.SmtVehicleMapper;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.vo.VehicleApplyDetailVO;
import com.tce.smart.platform.core.vo.VehicleVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.VehicleApplyConstants;
import com.tce.smart.tool.enums.*;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 车辆信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:02
 */
@Slf4j
@Service
public class SmtVehicleApplyServiceImpl extends ServiceImpl<SmtVehicleApplyMapper, SmtVehicleApply>
		implements SmtVehicleApplyService {
	@Autowired
	private SmtVehicleMapper vehicleMapper;
	@Autowired
	private SmtParkMapper parkMapper;
	@Autowired
	private SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;
	@Autowired
	private SmtDeviceTaskService smtDeviceTaskService;
	@Autowired
	private ImageService imageService;
	@Autowired
	private SmtStaffMapper staffMapper;
	@Autowired
	private SmtVehicleStaffService vehicleStaffService;
	@Autowired
	private SmtJcheAuthService smtJcheAuthService;
	@Autowired
	private SmtParkService smtParkService;

	@Autowired
	private SmtVehicleService smtVehicleService;

	/**
	 * 查询入园申请信息
	 *
	 * @param page   分页
	 * @param entity 查询条件
	 * @return 返回结果集
	 */
	@Override
	public IPage getVehicleApply(Page page, VehicleApplyDTO entity) {
		return this.baseMapper.getVehicleApply(page, entity);
	}

	/**
	 * 查询入园申请详情信息
	 *
	 * @param id 入园申请车辆ID
	 * @return 返回结果
	 */
	@Override
	public VehicleApplyDetailVO getVehicleApplyDetail(Long id) {
		SmtVehicleApply vehicleApply = this.getById(id);
		VehicleApplyDetailVO vehicleApplyDetail = new VehicleApplyDetailVO();
		List<Integer> parks = splitStringToInteger(vehicleApply.getParkId());
		List<SmtPark> parkList = smtParkService.list(Wrappers.<SmtPark>query().lambda().in(SmtPark::getId, parks));
		String parkName = "";
		for (SmtPark smtPark : parkList) {
			parkName += smtPark.getParkName() + ",";
		}
		if (!parkName.equals("")) {
			parkName = parkName.substring(0, parkName.length() - 1);
		}
		vehicleApplyDetail.setParkName(parkName);
		SmtPark park = parkMapper.selectById(vehicleApply.getParkId());
		vehicleApplyDetail.setParkName(ObjectUtil.isNotNull(park) ? park.getParkName() : "");
		BeanUtil.copyProperties(vehicleApply, vehicleApplyDetail);
		VehicleVO vehicleVO = vehicleMapper.getDetail(vehicleApply.getVehicleId());
		if (StrUtil.isNotBlank(vehicleVO.getDriverLicenseId())) {
			vehicleVO.setDriverLicenseId(imageService.buildImageUrl(vehicleVO.getDriverLicenseId()));
		}
		if (StrUtil.isNotBlank(vehicleVO.getDrivinglLicenseId())) {
			vehicleVO.setDrivinglLicenseId(imageService.buildImageUrl(vehicleVO.getDrivinglLicenseId()));
		}
		if (StrUtil.isNotBlank(vehicleVO.getFacePicId())) {
			vehicleVO.setFacePicId(imageService.buildImageUrl(vehicleVO.getFacePicId()));
		}

		vehicleVO.setVehicleTypeName(VehicleTypeEnum.desc(vehicleVO.getVehicleType()));
		vehicleVO.setVehicleColorName(VehicleColorEnum.desc(vehicleVO.getVehicleColor()));
		vehicleApplyDetail.setVehicle(vehicleVO);
		vehicleApplyDetail.setReason(vehicleApply.getStatus().equals(VehicleApplyConstants.REJECTED) ? vehicleApply.getReason() : "无");
		return vehicleApplyDetail;
	}

	/**
	 * 更新入园申请信息
	 */
	@Transactional
	@Override
	public boolean updateStatus(SmtVehicleApply entity) {
		SmtVehicleApply vehicleApply = this.getById(entity.getId());
		SmtVehicle smtVehicle = smtVehicleService.getById(vehicleApply.getVehicleId());
		entity.setApprover(SecurityUtils.getUser().getUsername());
		entity.setUpdateTime(LocalDateTime.now());
		boolean result = this.updateById(entity);
		if (entity.getStatus().equals(VehicleApplyConstants.APPROVED)) {
			//同意
			//根据车辆权限下发
			register(smtVehicle.getId(), smtVehicle.getVehiclePlate(), vehicleApply.getAuthorityId());
		} else if (entity.getStatus().equals(VehicleApplyConstants.REJECTED)) {
			//拒绝
			//把车辆表数据修改为已删除
			smtVehicle.setIsDelete(DeleteStatusEnum.IS_DELETE.getCode());
			smtVehicleService.updateById(smtVehicle);
		}
		return result;
	}


	/**
	 * 下发权限
	 *
	 * @param cardNo
	 * @param vehiclePlate
	 * @param authorityId
	 */
	private void register(Long cardNo, String vehiclePlate, Integer authorityId) {
		List<SmtDeviceAuthorityRelation> selectList = smtDeviceAuthorityRelationService.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>()
				.eq(SmtDeviceAuthorityRelation::getAuthorityId, authorityId));
		DeviceTaskVO deviceTaskVO = null;
		for (int i = 0; i < selectList.size(); i++) {
			//下发车辆信息,
			deviceTaskVO = new DeviceTaskVO();
			deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
			deviceTaskVO.setServiceType(DeviceTaskConstants.CAR_STAFF);
			deviceTaskVO.setDeviceCode(selectList.get(i).getDeviceId());
			deviceTaskVO.setCardNo(cardNo.toString());
			deviceTaskVO.setCardType(SmtVisitorEnum.CAR_CARD_TYPE_1.getType());
			deviceTaskVO.setGeneral(vehiclePlate);
			deviceTaskVO.setDeviceType(DeviceTaskConstants.CAR);
			deviceTaskVO.setOverTime(DeviceTaskConstants.maxTime);
			deviceTaskVO.setStartTime(DateUtil.currentSeconds());
			smtDeviceTaskService.saveTask(deviceTaskVO);
		}
	}

	private void cancellation(Long cardNo, Integer authorityId) {
		List<SmtDeviceAuthorityRelation> selectList = smtDeviceAuthorityRelationService.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>()
				.eq(SmtDeviceAuthorityRelation::getAuthorityId, authorityId));
		DeviceTaskVO deviceTaskVO = null;
		for (int i = 0; i < selectList.size(); i++) {
			// 注销车辆信息,
			deviceTaskVO = new DeviceTaskVO();
			deviceTaskVO.setAction(DeviceTaskConstants.DEL);
			deviceTaskVO.setCardNo(cardNo.toString());
			deviceTaskVO.setDeviceCode(selectList.get(i).getDeviceId());
			smtDeviceTaskService.saveTask(deviceTaskVO);
		}
	}

	/**
	 * 注册车辆信息封装
	 *
	 * @param cardNo
	 * @param parkId
	 * @param vehiclePlate
	 */
	private void register(Long cardNo, Integer parkId, String vehiclePlate, DeviceAuthorityEnum code, Integer businessCode) {
		List<SmtDeviceAuthorityRelation> selectList = smtDeviceAuthorityRelationService.getRelationAuth(parkId, businessCode, code);
		DeviceTaskVO deviceTaskVO = null;
		for (int i = 0; i < selectList.size(); i++) {
			//下发车辆信息,
			deviceTaskVO = new DeviceTaskVO();
			deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
			deviceTaskVO.setServiceType(DeviceTaskConstants.CAR_STAFF);
			deviceTaskVO.setDeviceCode(selectList.get(i).getDeviceId());
			deviceTaskVO.setCardNo(cardNo.toString());
			deviceTaskVO.setCardType(SmtVisitorEnum.CAR_CARD_TYPE_1.getType());
			deviceTaskVO.setGeneral(vehiclePlate);
			deviceTaskVO.setDeviceType(DeviceTaskConstants.CAR);
			deviceTaskVO.setOverTime(DeviceTaskConstants.maxTime);
			deviceTaskVO.setStartTime(DateUtil.currentSeconds());
			smtDeviceTaskService.saveTask(deviceTaskVO);
		}
	}

	/**
	 * 注销车辆信息
	 *
	 * @param cardNo
	 * @param parkId
	 */
	private void cancellation(Long cardNo, Integer parkId, DeviceAuthorityEnum code, Integer businessCode) {
		List<SmtDeviceAuthorityRelation> selectList = smtDeviceAuthorityRelationService.getRelationAuth(parkId, businessCode, code);
		DeviceTaskVO deviceTaskVO = null;
		for (int i = 0; i < selectList.size(); i++) {
			// 注销车辆信息,
			deviceTaskVO = new DeviceTaskVO();
			deviceTaskVO.setAction(DeviceTaskConstants.DEL);
			deviceTaskVO.setCardNo(cardNo.toString());
			deviceTaskVO.setDeviceCode(selectList.get(i).getDeviceId());
			smtDeviceTaskService.saveTask(deviceTaskVO);
		}
	}

	public List<Integer> splitStringToInteger(String idsStr) {
		List<Integer> returnList = new ArrayList<>();
		if (!StringUtil.isNullOrEmpty(idsStr)) {
			int[] idsArray = StringUtils.splitToInt(idsStr, ",");
			returnList.addAll(IntStream.of(idsArray).boxed().collect(Collectors.toList()));
		}

		return returnList;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeAuthToDevice(List<Integer> ids, List<String> deviceIds) {
		if (CollectionUtil.isEmpty(deviceIds)) {
			return this.removeByIds(ids);
		}
		for (Integer id : ids) {
			SmtVehicleApply vehicleApply = getById(id);
			SmtVehicle vehicle = smtVehicleService.getById(vehicleApply.getVehicleId());
			smtDeviceTaskService.addDeviceDelTaskImmed(deviceIds, vehicle.getId().toString(),
					vehicle.getVehiclePlate(), DeviceTaskConstants.CAR_STAFF, DeviceTaskActionEnum.DELAY_DEL.getCode(),
					SmtVisitorEnum.CAR_CARD_TYPE_1, DeviceTaskConstants.CAR, null);
		}
		this.removeByIds(ids);
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeAuthToDevice(SmtVehicleApply vehicleApply, List<String> deviceIds) {
		SmtVehicle vehicle = smtVehicleService.getById(vehicleApply.getVehicleId());
		if (vehicle == null) {
			log.info("车辆{}信息为空", vehicleApply.getId());
			return false;
		}
		smtDeviceTaskService.addDeviceDelTaskImmed(deviceIds, vehicle.getId().toString(),
				vehicle.getVehiclePlate(), DeviceTaskConstants.CAR_STAFF, DeviceTaskActionEnum.DELAY_DEL.getCode(),
				SmtVisitorEnum.CAR_CARD_TYPE_1, DeviceTaskConstants.CAR, null);

		return this.removeById(vehicleApply.getId());
	}

	@Override
	public Boolean removeByAuthId(Integer authId) {
		return remove(Wrappers.<SmtVehicleApply>lambdaQuery().eq(SmtVehicleApply::getAuthorityId, authId));
	}
}
