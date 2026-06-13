package com.tce.smart.platform.wrapper.dormitoryconfig;


import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAuthTypeRespDTO;
import com.tce.smart.platform.api.dto.resp.dormitoryconfig.DormitoryConfigListRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtOaAreaType;
import com.tce.smart.platform.core.entity.dormitoryconfig.SmtDormitoryConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description:
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author
 * @Date
 */
@Component
@AllArgsConstructor
public class DormitoryConfigListWrapper extends BaseWrapper<SmtDormitoryConfig, DormitoryConfigListRespDTO> {

    @Override
    protected DormitoryConfigListRespDTO warp(SmtDormitoryConfig bean) throws IOException {
		DormitoryConfigListRespDTO resp = BeanUtils.transform(DormitoryConfigListRespDTO.class, bean);
        return resp;
    }
}
