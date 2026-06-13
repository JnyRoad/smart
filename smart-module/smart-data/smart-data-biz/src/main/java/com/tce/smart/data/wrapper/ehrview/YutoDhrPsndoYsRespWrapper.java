package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.constant.enums.SexType;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsRespDTO;
import com.tce.smart.data.api.enums.DHREmpJChenEnum;
import com.tce.smart.data.api.enums.DHREmpTypeEnum;
import com.tce.smart.dhrview.core.entity.YutoDhrPsndo;
import com.tce.smart.tool.enums.EvwEmphrYsEnum;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: EvwEmphrYsWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class YutoDhrPsndoYsRespWrapper extends BaseWrapper<YutoDhrPsndo, EvwEmphrYsRespDTO> {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected EvwEmphrYsRespDTO warp(YutoDhrPsndo evwEmphrYs) {
        EvwEmphrYsRespDTO evwEmphrYsVO = new EvwEmphrYsRespDTO();
        evwEmphrYsVO.setBadge(evwEmphrYs.getCode());
        evwEmphrYsVO.setName(evwEmphrYs.getName());
        evwEmphrYsVO.setCompID(evwEmphrYs.getPkOrg());
        evwEmphrYsVO.setCompname(evwEmphrYs.getBuName());
        evwEmphrYsVO.setDepid(evwEmphrYs.getPkDept());
        evwEmphrYsVO.setDepname(evwEmphrYs.getDepName());
        evwEmphrYsVO.setJobid(evwEmphrYs.getPkPost());
        evwEmphrYsVO.setJobname(evwEmphrYs.getPostName());
        evwEmphrYsVO.setReportTo(evwEmphrYs.getJobglbdef18());
        evwEmphrYsVO.setStatus(evwEmphrYs.getJobglbdef1());
        evwEmphrYsVO.setStatusName(EvwEmphrYsEnum.desc(evwEmphrYs.getJobglbdef1()));

        evwEmphrYsVO.setEmpType(DHREmpTypeEnum.code(evwEmphrYs.getPsntype()));
        evwEmphrYsVO.setEmptypeName(evwEmphrYs.getPsntype());

        evwEmphrYsVO.setJchenID(DHREmpJChenEnum.code(evwEmphrYs.getJchen()));
        evwEmphrYsVO.setJchenName(evwEmphrYs.getJchen());
        try {
            evwEmphrYsVO.setJoindate(FORMAT.parse(evwEmphrYs.getGlbdef7()));
        } catch (Exception ignored) {
        }
        evwEmphrYsVO.setFlcj(evwEmphrYs.getJobglbdef21());
        evwEmphrYsVO.setResidentaddress(evwEmphrYs.getCensusaddr());
        evwEmphrYsVO.setNation(evwEmphrYs.getGender());
        evwEmphrYsVO.setCertno(evwEmphrYs.getGlbdef2());
        evwEmphrYsVO.setGender(evwEmphrYs.getSex());
        evwEmphrYsVO.setGenderName(SexType.desc(evwEmphrYs.getSex()));
        evwEmphrYsVO.setPhone(evwEmphrYs.getMobile());
        evwEmphrYsVO.setEmail(evwEmphrYs.getEmail());
        try {
            evwEmphrYsVO.setLeaDate(FORMAT.parse(evwEmphrYs.getEnddate()));
        } catch (Exception ignored) {
        }
        evwEmphrYsVO.setSalarytypeName(evwEmphrYs.getJobglbdef12());
        return evwEmphrYsVO;
    }
}
