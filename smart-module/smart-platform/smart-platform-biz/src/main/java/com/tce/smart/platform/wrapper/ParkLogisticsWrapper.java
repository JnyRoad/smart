package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.SmtParkLogisticsDTO;
import com.tce.smart.platform.core.entity.SmtParkLogistics;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Title: ParkLogisticsWrapper
 * @Auther: guohongtai
 * @Date: 2020-10-15 21:40
 */
@Component
@AllArgsConstructor
public class ParkLogisticsWrapper extends BaseWrapper<SmtParkLogistics, SmtParkLogisticsDTO> {
	@Override
	protected SmtParkLogisticsDTO warp(SmtParkLogistics park) throws IOException {
		SmtParkLogisticsDTO dto = new SmtParkLogisticsDTO();
		BeanUtil.copyProperties(park, dto);
		return dto;
	}
}
