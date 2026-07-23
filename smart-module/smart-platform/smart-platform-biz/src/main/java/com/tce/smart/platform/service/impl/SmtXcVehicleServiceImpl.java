package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.xcvehicle.req.XCVehicleAddDTO;
import com.tce.smart.data.api.feign.xcvehicle.RemoteXCVehicleService;
import com.tce.smart.platform.core.dto.SaveXCVehicleDTO;
import com.tce.smart.platform.core.dto.UpdateXCVehicleDTO;
import com.tce.smart.platform.core.dto.XcVehicleDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtXcVehicleMapper;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.SmtXcVehicleService;
import com.tce.smart.tool.constant.VehicleApplyConstants;
import com.tce.smart.tool.constant.VehicleConstants;
import com.tce.smart.tool.enums.DeleteStatusEnum;
import com.tce.smart.tool.enums.SmtImageEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 许昌车辆信息表
 *
 */
@Slf4j
@Service
public class SmtXcVehicleServiceImpl extends ServiceImpl<SmtXcVehicleMapper, SmtXcVehicle> implements SmtXcVehicleService {

	@Autowired
	private RemoteXCVehicleService remoteXCVehicleService;

	@Autowired
	private SmtStaffService smtStaffService;

	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;

	@Override
	public IPage getXcVehicle(Page page, XcVehicleDTO entity) {
		return this.page(page,new LambdaQueryWrapper<SmtXcVehicle>()
				.eq(StringUtils.isNotEmpty(entity.getVehiclePlate()),SmtXcVehicle::getVehiclePlate,entity.getVehiclePlate())
				.eq(Objects.nonNull(entity.getCardState()),SmtXcVehicle::getCardState,entity.getCardState())
				.between((Objects.nonNull(entity.getStartTime()) && Objects.nonNull(entity.getStartTime())),SmtXcVehicle::getCreateTime,entity.getStartTime(),entity.getEndTime())
				.eq(SmtXcVehicle::getIsDelete,DeleteStatusEnum.NOT_DELETE.getCode())
		);
	}

	@Transactional
	@Override
	public Boolean saveXCSmtVehicle(SaveXCVehicleDTO entity) {
		//判断车辆是否已经添加
		SmtXcVehicle vehicle = this.getOne(Wrappers.<SmtXcVehicle>query().lambda()
				.eq(SmtXcVehicle::getVehiclePlate, StrUtil.removeAll(entity.getVehiclePlate(), " ").toUpperCase())
				.eq(SmtXcVehicle::getIsDelete, VehicleConstants.UNDELETED));
		if(Objects.nonNull(vehicle)){
			throw new TCEException("车辆信息已存在");
		}
		vehicle = new SmtXcVehicle();
		BeanUtil.copyProperties(entity, vehicle);
		vehicle.setParkId(xcParkId);
		vehicle.setVehiclePlate(StrUtil.removeAll(entity.getVehiclePlate(), " ").toUpperCase());
		vehicle.setIsDelete(VehicleConstants.UNDELETED);
		vehicle.setCreateTime(LocalDateTime.now());
		vehicle.setOptUser(SecurityUtils.getUser().getUsername());
		//添加车辆记录
		this.save(vehicle);

		// 添加到车辆系统
		XCVehicleAddDTO xcVehicleAddDTO = new XCVehicleAddDTO();
		xcVehicleAddDTO.setUserName(entity.getContactsUser());
		xcVehicleAddDTO.setPhone(entity.getContactsPhone());
		xcVehicleAddDTO.setBadge(entity.getStaffBadge());
		xcVehicleAddDTO.setPlat(entity.getVehiclePlate());
		xcVehicleAddDTO.setCtId(entity.getCtId());
		xcVehicleAddDTO.setCarColor(entity.getVehicleColor());
		xcVehicleAddDTO.setFctCode(entity.getVehicleType());
		xcVehicleAddDTO.setStartDate(DateUtils.format(entity.getStartDate()));
		xcVehicleAddDTO.setEndDate(DateUtils.format(entity.getEndDate()));
		xcVehicleAddDTO.setCUser(vehicle.getOptUser());
		Result<Boolean> result = remoteXCVehicleService.saveVehicle(xcVehicleAddDTO, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if(!result.isSuccess()){
			throw new TCEException("添加许昌车辆失败");
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean xcUpdateById(UpdateXCVehicleDTO updateXCVehicleDTO) {
		SmtXcVehicle xcVehicle = this.getById(updateXCVehicleDTO.getId());
		if(Objects.isNull(xcVehicle)){
			throw new TCEException("车辆信息不存在");
		}
		BeanUtil.copyProperties(updateXCVehicleDTO, xcVehicle);
		xcVehicle.setVehiclePlate(StrUtil.removeAll(updateXCVehicleDTO.getVehiclePlate(), " ").toUpperCase());
		xcVehicle.setIsDelete(VehicleConstants.UNDELETED);
		xcVehicle.setUpdateTime(LocalDateTime.now());
		xcVehicle.setOptUser(SecurityUtils.getUser().getUsername());
		return this.updateById(xcVehicle);
	}

	@Transactional
	@Override
	public Boolean deleteVehicle(Long id) {
		SmtXcVehicle xcVehicle = this.getById(id);
		if(Objects.isNull(xcVehicle)){
			throw new TCEException("车辆信息不存在");
		}
		xcVehicle.setIsDelete(DeleteStatusEnum.IS_DELETE.getCode());
		xcVehicle.setUpdateTime(LocalDateTime.now());
		this.updateById(xcVehicle);

		Result<Boolean> result = remoteXCVehicleService.deleteVehicle(xcVehicle.getVehiclePlate(), SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if(!result.isSuccess()){
			throw new TCEException("删除许昌车辆失败");
		}
		return Boolean.TRUE;
	}
}
