package com.tce.smart.platform.wrapper.admittance;


import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAuthTypeRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtOaAreaType;
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
public class AdmittanceAuthTypeListWrapper extends BaseWrapper<SmtOaAreaType, AdmittanceAuthTypeRespDTO> {

    @Override
    protected AdmittanceAuthTypeRespDTO warp(SmtOaAreaType bean) throws IOException {
		AdmittanceAuthTypeRespDTO resp = new AdmittanceAuthTypeRespDTO();
		resp.setAreaTypeId(bean.getId());
		resp.setAreaTypeName(bean.getTypeName());
		resp.setAreaOaId(bean.getTypeValue());
        return resp;
    }
}
