package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.EvwJjitemRespDTO;
import com.tce.smart.ehrview.core.entity.EvwJjitem;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: EvwEappraisVOWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class EvwJjitemVOWrapper extends BaseWrapper<EvwJjitem, EvwJjitemRespDTO> {
    @Override
    protected EvwJjitemRespDTO warp(EvwJjitem evwJjitem) {
        EvwJjitemRespDTO evwJjitemVO = new EvwJjitemRespDTO();
		evwJjitemVO.setJjItemId(evwJjitem.getJJItemId());
        evwJjitemVO.setEmpzone(evwJjitem.getEmpzone());
        evwJjitemVO.setEzid(evwJjitem.getEZID());
        evwJjitemVO.setJjItem(evwJjitem.getJJItem());
        evwJjitemVO.setJjr(evwJjitem.getJJR());
        evwJjitemVO.setJjrName(evwJjitem.getJJRName());
        evwJjitemVO.setZrdep(evwJjitem.getZRDep());
        evwJjitemVO.setZrdepName(evwJjitem.getZRDepName());
        evwJjitemVO.setJe(evwJjitem.getJE());
        evwJjitemVO.setJjremark(evwJjitem.getJJremark());
        return evwJjitemVO;
    }
}
