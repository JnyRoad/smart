package com.tce.smart.platform.service.watermeter.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterChangeQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterUpdateDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterChange;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterConcentrator;
import com.tce.smart.platform.core.mapper.watermeter.SmtEleMeterChangeMapper;
import com.tce.smart.platform.service.watermeter.SmtEleMeterChangeService;
import com.tce.smart.platform.service.watermeter.SmtEleMeterConcentratorService;
import com.tce.smart.platform.utils.NumberUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author Li.JiaJun
 * @since 2022/5/12 10:54
 */
@Slf4j
@Service
public class SmtEleMeterChangeServiceImpl extends ServiceImpl<SmtEleMeterChangeMapper, SmtEleMeterChange> implements SmtEleMeterChangeService {

	@Autowired
	private SmtEleMeterConcentratorService concentratorService;

	@Override
	public IPage<SmtEleMeterChange> getPage(Page page, EleMeterChangeQueryDTO dto) {
		return this.page(page, Wrappers.<SmtEleMeterChange>lambdaQuery()
				.like(Objects.nonNull(dto.getBeforeAddress()), SmtEleMeterChange::getBeforeAddress, dto.getBeforeAddress())
				.like(Objects.nonNull(dto.getAfterAddress()), SmtEleMeterChange::getAfterAddress, dto.getAfterAddress())
				.ge(Objects.nonNull(dto.getStartTime()), SmtEleMeterChange::getCreateTime, NumberUtils.convertTime(dto.getStartTime()))
				.le(Objects.nonNull(dto.getEndTime()), SmtEleMeterChange::getCreateTime, NumberUtils.convertTime(dto.getEndTime()))
				.orderByDesc(SmtEleMeterChange::getCreateTime)
		);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addRecord(EleMeterUpdateDTO beforeDto, EleMeterUpdateDTO dto) {
		String personName = SecurityUtils.getUser().getUsername();
		SmtEleMeterConcentrator concentrator = concentratorService.getById(beforeDto.getConcentratorId());
		SmtEleMeterConcentrator afterConcentrator = concentratorService.getById(dto.getConcentratorId());
		SmtEleMeterChange meterChange = SmtEleMeterChange.builder()
				.eleMeterId(beforeDto.getId())
				.beforeMeterId(dto.getId())
				.beforeAddress(beforeDto.getAddress())
				.beforePort(beforeDto.getPort())
				.beforeSeq(beforeDto.getSeq())
				.beforeRatio(beforeDto.getRatio())
				.beforeConcentrator(concentrator.getName())
				.afterAddress(dto.getAddress())
				.afterPort(dto.getPort())
				.afterSeq(dto.getSeq())
				.afterRatio(dto.getRatio())
				.afterConcentrator(afterConcentrator.getName())
				.createUserName(personName)
				.build();
		this.save(meterChange);
	}
}
