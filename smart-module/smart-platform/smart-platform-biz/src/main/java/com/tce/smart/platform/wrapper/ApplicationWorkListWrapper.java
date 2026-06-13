package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.SmtApplicationWorkRespDTO;
import com.tce.smart.platform.core.entity.SmtApplicationWork;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-platform
 * @ClassName: ApplicationWorkListWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class ApplicationWorkListWrapper extends BaseWrapper<SmtApplicationWork, SmtApplicationWorkRespDTO> {
	@Override
	protected SmtApplicationWorkRespDTO warp(SmtApplicationWork model) throws IOException {
		return BeanUtils.toBean(model, SmtApplicationWorkRespDTO.class);
	}
}
