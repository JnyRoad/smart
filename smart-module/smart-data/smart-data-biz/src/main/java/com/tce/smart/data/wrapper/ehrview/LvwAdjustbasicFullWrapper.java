package com.tce.smart.data.wrapper.ehrview;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAdjustbasicFullRespDTO;
import com.tce.smart.ehrview.core.entity.LvwAdjustbasic;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: LvwAttendYcxxVOWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class LvwAdjustbasicFullWrapper extends BaseWrapper<LvwAdjustbasic, LvwAdjustbasicFullRespDTO> {

	@Override
	protected LvwAdjustbasicFullRespDTO warp(LvwAdjustbasic lvwAdjustbasic) {
		LvwAdjustbasicFullRespDTO lvwAdjustbasicFullRespDTO = new LvwAdjustbasicFullRespDTO();
		BeanUtil.copyProperties(lvwAdjustbasic, lvwAdjustbasicFullRespDTO);
		return lvwAdjustbasicFullRespDTO;
	}
}
