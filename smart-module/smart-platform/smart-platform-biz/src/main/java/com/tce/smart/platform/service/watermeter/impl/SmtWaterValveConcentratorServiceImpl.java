package com.tce.smart.platform.service.watermeter.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.platform.api.dto.req.watermeter.WaterValveConcentratorAddDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterValveConcentratorQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterValveConcentratorUpdateDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterValveConcentrator;
import com.tce.smart.platform.core.mapper.watermeter.SmtWaterValveConcentratorMapper;
import com.tce.smart.platform.emun.MeterStatusEnum;
import com.tce.smart.platform.helper.MeterHelper;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterValveService;
import com.tce.smart.platform.service.watermeter.SmtWaterValveConcentratorService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:41
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtWaterValveConcentratorServiceImpl extends ServiceImpl<SmtWaterValveConcentratorMapper, SmtWaterValveConcentrator> implements SmtWaterValveConcentratorService {

	private final SmtParkService smtParkService;

	private final MeterHelper meterHelper;

	private final SmtWaterMeterValveService meterValveService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean addConcentrator(WaterValveConcentratorAddDTO dto) {
		SmtWaterValveConcentrator valveConcentrator = this.getOne(Wrappers.<SmtWaterValveConcentrator>lambdaQuery()
				.eq(SmtWaterValveConcentrator::getParkId, dto.getParkId())
				.eq(SmtWaterValveConcentrator::getIp, dto.getIp()), false);
		if (valveConcentrator != null) {
			throw new SmartException("该园区已存在相同IP的外置阀门");
		}
		SmtWaterValveConcentrator concentrator = SmtWaterValveConcentrator.builder()
				.ip(dto.getIp())
				.name(dto.getName())
				.port(dto.getPort())
				.isOnline(MeterStatusEnum.UNCONNECTED.getCode())
				.remark(dto.getRemark())
				.build();
		SmtPark park = smtParkService.getById(dto.getParkId());
		if (Objects.isNull(park)) {
			throw new SmartException("园区不存在");
		}
		concentrator.setParkId(park.getId());
		concentrator.setParkName(park.getParkName());
		boolean save = this.save(concentrator);
		if (!save) {
			log.info("保存外置阀门集中器失败：{}", concentrator);
			return Boolean.FALSE;
		}
		Boolean isOnline = meterHelper.checkOnline(EventEnum.VALVE_CHECK_ONLINE.getCode(), concentrator.getId(),
				concentrator.getParkId(), concentrator.getIp(), concentrator.getPort());
		if (isOnline) {
			concentrator.setIsOnline(MeterStatusEnum.ONLINE.getCode());
			return this.updateById(concentrator);
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean updateConcentrator(WaterValveConcentratorUpdateDTO dto) {
		SmtWaterValveConcentrator concentrator = this.getById(dto.getId());
		if(Objects.isNull(concentrator)) {
			throw new SmartException("外置阀门集中器不存在");
		}
		if (StrUtil.isNotBlank(dto.getIp())) {
			concentrator.setIp(dto.getIp());
		}
		if (StrUtil.isNotBlank(dto.getPort())) {
			concentrator.setPort(dto.getPort());
		}
		if (StrUtil.isNotBlank(dto.getName())) {
			concentrator.setName(dto.getName());
		}
		concentrator.setRemark(dto.getRemark());
		concentrator.setIsOnline(MeterStatusEnum.UNCONNECTED.getCode());
		Boolean isOnline = meterHelper.checkOnline(EventEnum.VALVE_CHECK_ONLINE.getCode(), concentrator.getId(),
				concentrator.getParkId(), concentrator.getIp(), concentrator.getPort());
		if (isOnline) {
			concentrator.setIsOnline(MeterStatusEnum.ONLINE.getCode());
			return this.updateById(concentrator);
		}
		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean delConcentrator(Long id) {
		if (meterValveService.existWaterMeterByConcentratorId(id)) {
			throw new SmartException("集中器已关联阀门，请先删除阀门");
		}
		return this.removeById(id);
	}

	@Override
	public IPage<SmtWaterValveConcentrator> getPage(Page page, WaterValveConcentratorQueryDTO queryDto) {
		return this.page(page, Wrappers.<SmtWaterValveConcentrator>lambdaQuery()
				.like(Objects.nonNull(queryDto.getName()), SmtWaterValveConcentrator::getName, queryDto.getName())
				.eq(Objects.nonNull(queryDto.getStatus()), SmtWaterValveConcentrator::getIsOnline, queryDto.getStatus())
				.orderByDesc(SmtWaterValveConcentrator::getCreateTime));
	}
}
