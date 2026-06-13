package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.api.dto.resp.commonsd.SdMeterreadDetailChangeDTO;
import com.tce.smart.platform.core.entity.SmtSdMeterreadDetailChange;
import com.tce.smart.platform.core.mapper.SmtSdMeterreadDetailChangeMapper;
import com.tce.smart.platform.service.SmtSdMeterreadDetailChangeService;
import com.tce.smart.platform.utils.NumberUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 17:29
 */
@Service
public class SmtSdMeterreadDetailChangeServiceImpl extends ServiceImpl<SmtSdMeterreadDetailChangeMapper, SmtSdMeterreadDetailChange> implements SmtSdMeterreadDetailChangeService {

	@Override
	public List<SdMeterreadDetailChangeDTO> getList(Date meterMonth, Integer roomId) {
		List<SmtSdMeterreadDetailChange> detailChanges = this.list(Wrappers.<SmtSdMeterreadDetailChange>lambdaQuery()
				.eq(SmtSdMeterreadDetailChange::getMeterMonth, meterMonth)
				.eq(SmtSdMeterreadDetailChange::getRoomId, roomId)
		);
		if (CollUtil.isNotEmpty(detailChanges)) {
			return detailChanges.stream().map(e -> {
				SdMeterreadDetailChangeDTO dto = new SdMeterreadDetailChangeDTO();
				dto.setCategoryId(e.getCategoryId());
				dto.setPreMonthNum(NumberUtils.doubleFormat(e.getPreMonthNum()));
				dto.setCurMonthNum(NumberUtils.doubleFormat(e.getCurMonthNum()));
				double use = e.getCurMonthNum() - e.getPreMonthNum();
				dto.setUse(use < 0 ? 0 : NumberUtils.doubleFormat(use));
				dto.setCreateTime(e.getCreateTime().plusDays(-1));
				return dto;
			}).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}
