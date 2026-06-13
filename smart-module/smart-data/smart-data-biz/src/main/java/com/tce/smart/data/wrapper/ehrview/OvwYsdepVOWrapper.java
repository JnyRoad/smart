package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsdepRespDTO;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: OvwYsdepVOWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class OvwYsdepVOWrapper extends BaseWrapper<OvwYsdep, OvwYsdepRespDTO> {
    @Override
    protected OvwYsdepRespDTO warp(OvwYsdep ovwYsdep) throws IOException {
        OvwYsdepRespDTO ovwYsdepVO = new OvwYsdepRespDTO();
        ovwYsdepVO.setDepid(ovwYsdep.getDepid());
        ovwYsdepVO.setDepname(ovwYsdep.getDepname());
        ovwYsdepVO.setDepAbbr(ovwYsdep.getDepAbbr());
        ovwYsdepVO.setCompId(ovwYsdep.getCompID());
        ovwYsdepVO.setDirector(ovwYsdep.getDirector());
        ovwYsdepVO.setDirecName(ovwYsdep.getDirecName());
        ovwYsdepVO.setDepGrade(ovwYsdep.getDepGrade());
        ovwYsdepVO.setAdminId(ovwYsdep.getAdminID());
        ovwYsdepVO.setDepCost(ovwYsdep.getDepCost());

        return ovwYsdepVO;
    }
}
