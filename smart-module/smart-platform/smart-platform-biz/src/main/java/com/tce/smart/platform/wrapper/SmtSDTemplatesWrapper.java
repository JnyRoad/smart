package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.req.SmtSdTemplatesReqDTO;
import com.tce.smart.platform.core.entity.SmtAlarmRecord;
import com.tce.smart.platform.core.entity.SmtSdTemplates;
import com.tce.smart.platform.core.vo.AlarmRecordVO;

import java.io.IOException;

/**
 * @description: SmtSDTemplatesWrapper
 * @date: 2020-07-07 13:41
 * @author: wuling
 * @version: 1.0
 */
public class SmtSDTemplatesWrapper extends BaseWrapper<SmtSdTemplates, SmtSdTemplatesReqDTO> {

	@Override
	protected SmtSdTemplatesReqDTO warp(SmtSdTemplates smtSdTemplates) throws IOException {
		SmtSdTemplatesReqDTO smtSdTemplatesReqDTO = new SmtSdTemplatesReqDTO();
		BeanUtil.copyProperties(smtSdTemplates, smtSdTemplatesReqDTO);
		return smtSdTemplatesReqDTO;
	}
}
