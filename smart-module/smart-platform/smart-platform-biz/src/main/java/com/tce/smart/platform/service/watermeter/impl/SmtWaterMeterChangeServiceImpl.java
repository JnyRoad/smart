package com.tce.smart.platform.service.watermeter.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterChangeQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterUpdateDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterChange;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterConcentrator;
import com.tce.smart.platform.core.mapper.watermeter.SmtWaterMeterChangeMapper;
import com.tce.smart.platform.emun.DownChannelEnum;
import com.tce.smart.platform.emun.LargeClassEnum;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterChangeService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterConcentratorService;
import com.tce.smart.platform.utils.NumberUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author Li.JiaJun
 * @since 2022/5/12 10:55
 */
@Slf4j
@Service
public class SmtWaterMeterChangeServiceImpl extends ServiceImpl<SmtWaterMeterChangeMapper, SmtWaterMeterChange> implements SmtWaterMeterChangeService {

	@Autowired
	private SmtWaterMeterConcentratorService concentratorService;

	@Override
	public IPage<SmtWaterMeterChange> getPage(Page page, WaterMeterChangeQueryDTO dto) {
		return this.page(page, Wrappers.<SmtWaterMeterChange>lambdaQuery()
				.like(Objects.nonNull(dto.getBeforeAddress()), SmtWaterMeterChange::getBeforeAddress, dto.getBeforeAddress())
				.like(Objects.nonNull(dto.getAfterAddress()), SmtWaterMeterChange::getAfterAddress, dto.getAfterAddress())
				.ge(Objects.nonNull(dto.getStartTime()), SmtWaterMeterChange::getCreateTime, NumberUtils.convertTime(dto.getStartTime()))
				.le(Objects.nonNull(dto.getEndTime()), SmtWaterMeterChange::getCreateTime, NumberUtils.convertTime(dto.getEndTime()))
				.orderByDesc(SmtWaterMeterChange::getCreateTime)
		);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addRecord(Long concentratorId, Long meterId, String beforeAddress, String beforePort,
						  String largeClass, Integer seq, WaterMeterUpdateDTO dto) {
		String personName = SecurityUtils.getUser().getUsername();
		SmtWaterMeterConcentrator concentrator = concentratorService.getById(concentratorId);
		SmtWaterMeterConcentrator afterConcentrator = concentratorService.getById(dto.getConcentratorId());
		SmtWaterMeterChange meterChange = SmtWaterMeterChange.builder()
				.waterMeterId(meterId)
				.beforeMeterId(dto.getId())
				.beforeAddress(beforeAddress)
				.beforePort(DownChannelEnum.desc(Integer.parseInt(beforePort)))
				.beforeLargeClass(LargeClassEnum.desc(Integer.parseInt(largeClass)))
				.beforeSeq(seq)
				.beforeConcentrator(concentrator.getName())
				.afterAddress(dto.getAddress())
				.afterPort(DownChannelEnum.desc(Integer.parseInt(dto.getPort())))
				.afterLargeClass(LargeClassEnum.desc(Integer.parseInt(dto.getLargeClass())))
				.afterSeq(dto.getSeq())
				.afterConcentrator(afterConcentrator.getName())
				.createUserName(personName)
				.build();
		this.save(meterChange);
	}
}
