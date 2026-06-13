package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.constant.enums.SexType;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.YsLeaveRespDTO;
import com.tce.smart.data.api.enums.DHREmpJChenEnum;
import com.tce.smart.data.api.enums.DHREmpTypeEnum;
import com.tce.smart.dhrview.core.entity.YutoDhrPsndo;
import com.tce.smart.ehrview.core.entity.OvwYscomp;
import com.tce.smart.ehrview.core.service.YutoDhrOrgsService;
import com.tce.smart.tool.enums.EvwEmphrYsEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: EvwEmphrYsWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Slf4j
@Component
public class YutoDhrPsndoYsLeaveWrapper extends BaseWrapper<YutoDhrPsndo, YsLeaveRespDTO> {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private YutoDhrOrgsService yutoDhrOrgsService;

    @Override
    protected YsLeaveRespDTO warp(YutoDhrPsndo evwEmphrYs) {
        YsLeaveRespDTO ysLeaveRespDTO = new YsLeaveRespDTO();
        ysLeaveRespDTO.setBadge(evwEmphrYs.getCode());
        ysLeaveRespDTO.setName(evwEmphrYs.getName());
        ysLeaveRespDTO.setCompId(evwEmphrYs.getPkOrg());
        ysLeaveRespDTO.setDepId(evwEmphrYs.getPkDept());
        ysLeaveRespDTO.setJobId(evwEmphrYs.getPkPost());
        ysLeaveRespDTO.setJchenId(DHREmpJChenEnum.code(evwEmphrYs.getJchen()));
        try {
            ysLeaveRespDTO.setJoinDate(FORMAT.parse(evwEmphrYs.getGlbdef7()));
        } catch (Exception ignored) {
        }
        OvwYscomp ovwYscompResult = yutoDhrOrgsService.getByCompId(evwEmphrYs.getPkOrg().toString());
        if (Objects.nonNull(ovwYscompResult)) {
            log.warn("查询BU信息：Badge = {}", evwEmphrYs.getCode());
            ysLeaveRespDTO.setEzId(ovwYscompResult.getEzid());
        }

        return ysLeaveRespDTO;
    }
}
