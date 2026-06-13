package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.SmtApplicationProcessRespDTO;
import com.tce.smart.platform.core.entity.SmtApplicationProcess;
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
public class GetApplicationProcessWrapper extends BaseWrapper<SmtApplicationProcess, SmtApplicationProcessRespDTO> {
	@Override
	protected SmtApplicationProcessRespDTO warp(SmtApplicationProcess smtApplicationProcess) throws IOException {
		SmtApplicationProcessRespDTO smtApplicationProcessRespDTO = new SmtApplicationProcessRespDTO();
		BeanUtil.copyProperties(smtApplicationProcess, smtApplicationProcessRespDTO);
		return smtApplicationProcessRespDTO;
	}
}
