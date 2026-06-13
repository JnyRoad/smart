package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.SmtParkRespDTO;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.vo.MsgTemplateVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class ParkRespDTOWrapper extends BaseWrapper<SmtPark, SmtParkRespDTO> {
    @Override
    protected SmtParkRespDTO warp(SmtPark park) throws IOException {
		SmtParkRespDTO dto = new SmtParkRespDTO();
        BeanUtil.copyProperties(park, dto);
        return dto;
    }
}
