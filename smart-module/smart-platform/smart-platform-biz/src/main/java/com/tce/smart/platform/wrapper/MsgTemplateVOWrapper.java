package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;
import com.tce.smart.platform.core.vo.MsgTemplateVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class MsgTemplateVOWrapper extends BaseWrapper<SmtMsgTemplate, MsgTemplateVO> {
    @Override
    protected MsgTemplateVO warp(SmtMsgTemplate smtMsgTemplate) throws IOException {
		MsgTemplateVO msgTemplateVO = new MsgTemplateVO();
        BeanUtil.copyProperties(smtMsgTemplate, msgTemplateVO);
        return msgTemplateVO;
    }
}
