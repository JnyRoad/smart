package com.tce.smart.data.wrapper.ehrview;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAttendYcxxSimpleRespDTO;
import com.tce.smart.ehrview.core.entity.LvwAttendYcxx;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: LvwAttendYcxxVOWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class LvwAttendYcxxVOWrapper extends BaseWrapper<LvwAttendYcxx, LvwAttendYcxxSimpleRespDTO> {
    @Override
    protected LvwAttendYcxxSimpleRespDTO warp(LvwAttendYcxx lvwAttendYcxx) {
        LvwAttendYcxxSimpleRespDTO lvwAttendYcxxVO = new LvwAttendYcxxSimpleRespDTO();
        BeanUtil.copyProperties(lvwAttendYcxx, lvwAttendYcxxVO);
        return lvwAttendYcxxVO;
    }
}
