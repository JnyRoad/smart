package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.OperateLogRespDTO;
import com.tce.smart.platform.core.entity.SmtOperateLog;
import com.tce.smart.platform.emun.operateLog.CodeEnum;
import com.tce.smart.platform.emun.operateLog.MeterOperateEnum;
import org.springframework.stereotype.Component;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 14:44
 */
@Component
public class OperateLogRespWrapper extends BaseWrapper<SmtOperateLog, OperateLogRespDTO> {

	@Override
	protected OperateLogRespDTO warp(SmtOperateLog model) {
		OperateLogRespDTO dto = BeanUtils.transform(OperateLogRespDTO.class, model);
		dto.setDesc(CodeEnum.desc(model.getCode()));
		if (CodeEnum.METER.getCode().equals(dto.getCode())) {
			dto.setActionDesc(MeterOperateEnum.desc(dto.getAction()));
		}
		return dto;
	}
}
