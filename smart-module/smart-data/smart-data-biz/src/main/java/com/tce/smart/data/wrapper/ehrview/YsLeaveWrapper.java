package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.util.StringUtils;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.EvwEappraisDTO;
import com.tce.smart.data.api.dto.ehrview.resp.YsLeaveRespDTO;
import com.tce.smart.ehrview.core.entity.*;
import com.tce.smart.ehrview.core.service.IEvwEappraisService;
import com.tce.smart.ehrview.core.service.ILvwAyearholidayService;
import com.tce.smart.ehrview.core.service.IOvwYscompService;
import com.tce.smart.ehrview.core.service.IOvwYsjobService;
import com.tce.smart.tool.constant.NumberConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: OvwYsdepVOWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Slf4j
@Component
public class YsLeaveWrapper extends BaseWrapper<EvwEmphrYs, YsLeaveRespDTO> {
    @Autowired
    private IOvwYsjobService iOvwYsjobService;
    @Autowired
    private IOvwYscompService iOvwYscompService;
    @Autowired
    private IEvwEappraisService iEvwEappraisService;
    @Autowired
    private ILvwAyearholidayService iLvwAyearholidayService;
    @Override
    protected YsLeaveRespDTO warp(EvwEmphrYs evwEmphrYs) {
        YsLeaveRespDTO ysLeaveVO = new YsLeaveRespDTO();
        ysLeaveVO.setBadge(evwEmphrYs.getBadge());
        ysLeaveVO.setName(evwEmphrYs.getName());
        ysLeaveVO.setCompId(evwEmphrYs.getCompID());
        ysLeaveVO.setDepId(evwEmphrYs.getDepid());
        ysLeaveVO.setJobId(evwEmphrYs.getJobid());
        ysLeaveVO.setJchenId(evwEmphrYs.getJchenID());
        ysLeaveVO.setJoinDate(evwEmphrYs.getJoindate());
        ysLeaveVO.setEId(evwEmphrYs.getEID());
        {
            OvwYsjob ovwYsjobResult = iOvwYsjobService.getByJobId(evwEmphrYs.getJobid());
            if (Objects.nonNull(ovwYsjobResult)) {
                log.warn("查询岗位信息：Badge = {}", evwEmphrYs.getBadge());
                ysLeaveVO.setJxianId(ovwYsjobResult.getJXianID());
            }
        }
        {
            LvwAyearholiday yearholidayResult = iLvwAyearholidayService.getByBadge(evwEmphrYs.getBadge());
            if (Objects.nonNull(yearholidayResult)) {
                log.warn("查询年假信息：Badge = {}", evwEmphrYs.getBadge());
                ysLeaveVO.setYearDay(yearholidayResult.getThisbalance()+"");
            }
        }
        {
            List<EvwEapprais> eappraiResult = iEvwEappraisService.getListByBadge(evwEmphrYs.getBadge());
			List<EvwEappraisDTO> evwEappraisDtoList = null;
            if (Objects.nonNull(eappraiResult)) {
                log.warn("查询评优信息：Badge = {}", evwEmphrYs.getBadge());
                ysLeaveVO.setIfPy(NumberConstants.STRING_ONE);

				evwEappraisDtoList = new ArrayList<>();
				EvwEappraisDTO evwEappraisDTO = null;
                for(EvwEapprais tempEle : eappraiResult) {
	//转换Bean
					evwEappraisDTO = new EvwEappraisDTO();
					BeanUtils.copyProperties(tempEle,evwEappraisDTO);
					evwEappraisDtoList.add(evwEappraisDTO);

	                if(StringUtils.startWithIgnoreCase(tempEle.getBadge(), "CY")){
	                    ysLeaveVO.setIsCgpy(NumberConstants.STRING_ONE);
	                    break;
	                }
                }

                ysLeaveVO.setEvwEapprais(evwEappraisDtoList);
//                ysLeaveVO.setPrize(eappraiResult.getPrize());
//                ysLeaveVO.setEffectdate(eappraiResult.getEffectdate());
            }
        }
        {
            OvwYscomp ovwYscompResult = iOvwYscompService.getByCompId(evwEmphrYs.getCompID().toString());
            if (Objects.nonNull(ovwYscompResult)) {
                log.warn("查询BU信息：Badge = {}", evwEmphrYs.getBadge());
                ysLeaveVO.setEzId(ovwYscompResult.getEzid());
            }
        }

        return ysLeaveVO;
    }
}
