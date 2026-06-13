package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.MeterreadConfigRespDTO;
import com.tce.smart.platform.core.entity.SmtAlarmRecord;
import com.tce.smart.platform.core.entity.SmtMeterreadConfig;
import com.tce.smart.platform.core.vo.AlarmRecordVO;
import com.tce.smart.platform.service.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName:
 * @Author
 * @Date
 */
@Component
@AllArgsConstructor
public class MeterreadConfigWrapper extends BaseWrapper<SmtMeterreadConfig, MeterreadConfigRespDTO> {

    @Override
    protected MeterreadConfigRespDTO warp(SmtMeterreadConfig bean) throws IOException {
		MeterreadConfigRespDTO meterreadConfigRespDTO = BeanUtils.transform(MeterreadConfigRespDTO.class, bean);
        return meterreadConfigRespDTO;
    }
}
