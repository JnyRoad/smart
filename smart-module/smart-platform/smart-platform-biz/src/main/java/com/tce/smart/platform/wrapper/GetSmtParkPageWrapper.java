package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.SmtParkRespDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class GetSmtParkPageWrapper extends BaseWrapper<SmtPark, SmtParkRespDTO> {
	@Override
	protected SmtParkRespDTO warp(SmtPark smtPark) throws IOException {
		SmtParkRespDTO smtParkRespDTO = new SmtParkRespDTO();
		BeanUtil.copyProperties(smtPark, smtParkRespDTO);
		return smtParkRespDTO;
	}
}
