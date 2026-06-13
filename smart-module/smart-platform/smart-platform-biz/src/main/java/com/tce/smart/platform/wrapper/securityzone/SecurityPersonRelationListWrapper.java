package com.tce.smart.platform.wrapper.securityzone;


import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityPersonRelationRespDTO;
import com.tce.smart.platform.core.dto.SecurityPersonRelationDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: SecurityPersonRelationListWrapper
 * @Author fushiping
 * @Date
 */
@Component
@AllArgsConstructor
public class SecurityPersonRelationListWrapper extends BaseWrapper<SecurityPersonRelationDTO, SecurityPersonRelationRespDTO> {

    @Override
    protected SecurityPersonRelationRespDTO warp(SecurityPersonRelationDTO bean) throws IOException {
		SecurityPersonRelationRespDTO resp = BeanUtils.transform(SecurityPersonRelationRespDTO.class, bean);
        return resp;
    }
}
