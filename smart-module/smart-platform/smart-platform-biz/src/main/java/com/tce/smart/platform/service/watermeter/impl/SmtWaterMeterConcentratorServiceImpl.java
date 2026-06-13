package com.tce.smart.platform.service.watermeter.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterConcentratorAddDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterConcentratorQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterConcentratorUpdateDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeter;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterConcentrator;
import com.tce.smart.platform.core.mapper.watermeter.SmtWaterMeterConcentratorMapper;
import com.tce.smart.platform.emun.MeterStatusEnum;
import com.tce.smart.platform.helper.MeterHelper;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterConcentratorService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:41
 */
@Slf4j
@Service
public class SmtWaterMeterConcentratorServiceImpl extends ServiceImpl<SmtWaterMeterConcentratorMapper, SmtWaterMeterConcentrator> implements SmtWaterMeterConcentratorService {
	@Autowired
	private SmtWaterMeterService waterMeterService;
	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private MeterHelper meterHelper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean addConcentrator(WaterMeterConcentratorAddDTO dto) {
		SmtWaterMeterConcentrator waterMeterConcentrator = this.getOne(Wrappers.<SmtWaterMeterConcentrator>lambdaQuery()
				.eq(SmtWaterMeterConcentrator::getParkId, dto.getParkId())
				.eq(SmtWaterMeterConcentrator::getIp, dto.getIp()), false);
		if (waterMeterConcentrator != null) {
			throw new SmartException("该园区已存在相同IP的水表集中器");
		}
		SmtWaterMeterConcentrator concentrator = SmtWaterMeterConcentrator.builder()
				.ip(dto.getIp())
				.name(dto.getName())
				.port(dto.getPort())
				.isOnline(MeterStatusEnum.UNCONNECTED.getCode())
				.remark(dto.getRemark())
				.address(dto.getAddress())
				.build();
		SmtPark park = smtParkService.getById(dto.getParkId());
		if (Objects.isNull(park)) {
			throw new SmartException("园区不存在");
		}
		concentrator.setParkId(park.getId());
		concentrator.setParkName(park.getParkName());
		boolean save = this.save(concentrator);
		if (!save) {
			log.info("保存水表集中器失败：{}", concentrator);
			return Boolean.FALSE;
		}
		Boolean isOnline = meterHelper.checkOnline(EventEnum.METER_CHECK_ONLINE.getCode(), concentrator.getId(),
				concentrator.getParkId(), concentrator.getIp(), concentrator.getPort());
		if (isOnline) {
			concentrator.setIsOnline(MeterStatusEnum.ONLINE.getCode());
			return this.updateById(concentrator);
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean updateConcentrator(WaterMeterConcentratorUpdateDTO dto) {
		SmtWaterMeterConcentrator concentrator = this.getById(dto.getId());
		if (Objects.isNull(concentrator)) {
			throw new SmartException("水表集中器不存在");
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
		concentrator.setAddress(dto.getAddress());
		concentrator.setIsOnline(MeterStatusEnum.UNCONNECTED.getCode());
		Boolean isOnline = meterHelper.checkOnline(EventEnum.METER_CHECK_ONLINE.getCode(), concentrator.getId(),
				concentrator.getParkId(), concentrator.getIp(), concentrator.getPort());
		if (isOnline) {
			concentrator.setIsOnline(MeterStatusEnum.ONLINE.getCode());
		} else {
			concentrator.setIsOnline(MeterStatusEnum.OUTLINE.getCode());
		}
		return this.updateById(concentrator);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean delConcentrator(Long id) {
		if (waterMeterService.existWaterMeterByConcentratorId(id)) {
			throw new SmartException("集中器已关联水表，请先删除水表");
		}
		return this.removeById(id);
	}

	@Override
	public IPage<SmtWaterMeterConcentrator> getPage(Page page, WaterMeterConcentratorQueryDTO queryDto) {
		return this.page(page, Wrappers.<SmtWaterMeterConcentrator>lambdaQuery()
				.eq(Objects.nonNull(queryDto.getParkId()), SmtWaterMeterConcentrator::getParkId, queryDto.getParkId())
				.like(Objects.nonNull(queryDto.getName()), SmtWaterMeterConcentrator::getName, queryDto.getName())
				.eq(Objects.nonNull(queryDto.getStatus()), SmtWaterMeterConcentrator::getIsOnline, queryDto.getStatus())
				.like(Objects.nonNull(queryDto.getAddress()), SmtWaterMeterConcentrator::getAddress, queryDto.getAddress())
				.orderByDesc(SmtWaterMeterConcentrator::getCreateTime));
	}

	@Override
	public Boolean queryFile(Long id) {
		SmtWaterMeterConcentrator concentrator = this.getById(id);
		List<SmtWaterMeter> waterMeters = waterMeterService.getByConcentratorId(id);
		JSONArray jsonArray = new JSONArray();
		for (SmtWaterMeter meter : waterMeters) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("seq", meter.getSeq());
			jsonObject.put("port", meter.getPort());
			jsonObject.put("address", meter.getAddress());
			jsonArray.add(jsonObject);
		}
		return meterHelper.meterFile(EventEnum.WATER_METER_QUERY_FILE.getCode(), id, concentrator.getAddress(),
				concentrator.getParkId(), concentrator.getIp(), concentrator.getPort(), waterMeters.size(), jsonArray.toString());
	}

	@Override
	public SmtWaterMeterConcentrator getByIp(String ip) {
		return this.getOne(Wrappers.<SmtWaterMeterConcentrator>lambdaQuery()
				.eq(SmtWaterMeterConcentrator::getIp, ip));
	}
}
