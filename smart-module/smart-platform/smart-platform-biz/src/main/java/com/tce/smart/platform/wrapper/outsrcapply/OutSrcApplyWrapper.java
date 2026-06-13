package com.tce.smart.platform.wrapper.outsrcapply;

import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.outsrcapply.SmtOutSrcApplyRespDTO;
import com.tce.smart.platform.core.entity.SmtOutSrcApply;
import com.tce.smart.platform.emun.OutSrcApplyStatusEnum;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/3 14:13
 */
@Component
public class OutSrcApplyWrapper extends BaseWrapper<SmtOutSrcApply, SmtOutSrcApplyRespDTO> {

	@Override
	protected SmtOutSrcApplyRespDTO warp(SmtOutSrcApply model) throws IOException {
		return SmtOutSrcApplyRespDTO.builder()
				.applyId(model.getId())
				.compName(model.getCompName())
				.applyNum(model.getApplyNum())
				.applyTime(DateUtils.format(model.getCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT))
				.statusDesc(OutSrcApplyStatusEnum.desc(model.getStatus()))
				.build();
	}
}
