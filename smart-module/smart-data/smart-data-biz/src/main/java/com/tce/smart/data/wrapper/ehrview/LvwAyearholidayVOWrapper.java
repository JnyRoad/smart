package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAyearholidayRespDTO;
import com.tce.smart.ehrview.core.entity.LvwAyearholiday;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: LvwAyearholidayVOWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class LvwAyearholidayVOWrapper extends BaseWrapper<LvwAyearholiday, LvwAyearholidayRespDTO> {
    @Override
    protected LvwAyearholidayRespDTO warp(LvwAyearholiday lvwAyearholiday) throws IOException {
		LvwAyearholidayRespDTO lvwAyearholidayVO = new LvwAyearholidayRespDTO();
        lvwAyearholidayVO.setThisbalance(lvwAyearholiday.getThisbalance());
        return lvwAyearholidayVO;
    }
}
