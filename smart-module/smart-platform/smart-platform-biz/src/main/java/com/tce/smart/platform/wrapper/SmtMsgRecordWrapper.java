package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.SmtMsgRecordRespDTO;
import com.tce.smart.platform.core.entity.SmtMsgRecord;
import com.tce.smart.platform.emun.StatusEmun;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Author: Liu.jihong
 * @Date: 2020/9/27 16:05
 */
@Component
public class SmtMsgRecordWrapper extends BaseWrapper<SmtMsgRecord, SmtMsgRecordRespDTO> {
	@Override
	protected SmtMsgRecordRespDTO warp(SmtMsgRecord smtMsgRecord) throws IOException {
		SmtMsgRecordRespDTO smtMsgRecordRespDTO = BeanUtil.toBean(smtMsgRecord, SmtMsgRecordRespDTO.class);
		smtMsgRecordRespDTO.setMsgStateName(StatusEmun.desc(smtMsgRecord.getMsgState()));
		return smtMsgRecordRespDTO;
	}
}
