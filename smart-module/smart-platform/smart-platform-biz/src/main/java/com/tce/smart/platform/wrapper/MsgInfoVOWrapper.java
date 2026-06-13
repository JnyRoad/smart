package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.vo.MsgInfoVO;
import com.tce.smart.platform.core.entity.SmtMsgRecord;
import com.tce.smart.tool.enums.SmsRecordSateEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class MsgInfoVOWrapper extends BaseWrapper<SmtMsgRecord, MsgInfoVO> {
    @Override
    protected MsgInfoVO warp(SmtMsgRecord smtMsgRecord) throws IOException {
		MsgInfoVO msgInfoRespDTO = new MsgInfoVO();
        BeanUtil.copyProperties(smtMsgRecord, msgInfoRespDTO);
        msgInfoRespDTO.setMsgStateName(SmsRecordSateEnum.desc(msgInfoRespDTO.getMsgState()).getDesc());
        return msgInfoRespDTO;
    }
}
