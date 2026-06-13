package com.tce.smart.platform.wrapper.securityzone;


import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthRelationRespDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthRelation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author fushiping
 * @Date
 */
@Component
@AllArgsConstructor
public class SecurityAuthRelationWrapper extends BaseWrapper<SmtSecurityAuthRelation, SecurityAuthRelationRespDTO> {

    @Override
    protected SecurityAuthRelationRespDTO warp(SmtSecurityAuthRelation bean) throws IOException {
		SecurityAuthRelationRespDTO resp = BeanUtils.transform(SecurityAuthRelationRespDTO.class, bean);
        return resp;
    }
}
