package com.tce.smart.platform.wrapper.securityzone;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.securityzone.OaAreaAuthListRespDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtOaAreaRelation;
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
public class SecurityOaAuthListWrapper extends BaseWrapper<SmtOaAreaRelation, OaAreaAuthListRespDTO> {


    @Override
    protected OaAreaAuthListRespDTO warp(SmtOaAreaRelation bean) throws IOException {
		OaAreaAuthListRespDTO resp = BeanUtils.transform(OaAreaAuthListRespDTO.class, bean);
        return resp;
    }
}
