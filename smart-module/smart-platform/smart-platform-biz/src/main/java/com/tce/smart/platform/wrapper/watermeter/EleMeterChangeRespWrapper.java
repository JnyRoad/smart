package com.tce.smart.platform.wrapper.watermeter;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.watermeter.EleMeterChangeRespDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterChange;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Li.JiaJun
 * @since 2022/5/12 11:16
 */
@Component
@AllArgsConstructor
public class EleMeterChangeRespWrapper extends BaseWrapper<SmtEleMeterChange, EleMeterChangeRespDTO> {
	@Override
	protected EleMeterChangeRespDTO warp(SmtEleMeterChange model) {
		return BeanUtils.transform(EleMeterChangeRespDTO.class, model);
	}
}
