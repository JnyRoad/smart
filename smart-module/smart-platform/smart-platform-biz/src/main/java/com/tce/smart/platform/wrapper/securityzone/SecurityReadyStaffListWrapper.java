package com.tce.smart.platform.wrapper.securityzone;


import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityStaffQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityPersonRelationRespDTO;
import com.tce.smart.platform.core.dto.SecurityPersonRelationDTO;
import com.tce.smart.platform.core.entity.SmtStaff;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: SecurityReadyStaffListtWrapper
 * @Author fushiping
 * @Date
 */
@Component
@AllArgsConstructor
public class SecurityReadyStaffListWrapper extends BaseWrapper<SmtStaff, SecurityStaffQueryReqDTO> {

    @Override
    protected SecurityStaffQueryReqDTO warp(SmtStaff bean) throws IOException {
		SecurityStaffQueryReqDTO resp = BeanUtils.transform(SecurityStaffQueryReqDTO.class, bean);
        return resp;
    }
}
