package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import com.tce.smart.ehrview.core.entity.OvwYscomp;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: OvwYscompVOWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class OvwYscompVOWrapper extends BaseWrapper<OvwYscomp, OvwYscompRespDTO> {
    @Override
    protected OvwYscompRespDTO warp(OvwYscomp evwEmphrYs) throws IOException {
        OvwYscompRespDTO ovwYscompVO = new OvwYscompRespDTO();
        ovwYscompVO.setCompid(evwEmphrYs.getCompid());
        ovwYscompVO.setTitle(evwEmphrYs.getTitle());
        ovwYscompVO.setCompAbbr(evwEmphrYs.getCompAbbr());
        ovwYscompVO.setCompGrade(evwEmphrYs.getCompGrade());
        ovwYscompVO.setAdminId(evwEmphrYs.getAdminID());
        ovwYscompVO.setEzid(evwEmphrYs.getEzid());
        return ovwYscompVO;
    }
}
