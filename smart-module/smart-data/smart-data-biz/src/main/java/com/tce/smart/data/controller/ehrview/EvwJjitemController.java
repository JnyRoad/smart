package com.tce.smart.data.controller.ehrview;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.resp.EvwJjitemRespDTO;
import com.tce.smart.ehrview.core.entity.EvwJjitem;
import com.tce.smart.ehrview.core.service.IEvwJjitemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@RestController
@RequestMapping("/jjitem")
public class EvwJjitemController extends BaseController {

    @Autowired
    public IEvwJjitemService iEvwJjitemService;

    /**
     * 根据人事区域获取工作交接项信息
     * @param ezid
     * @return
     */
    @Inner
    @GetMapping("/{ezid}")
    public Result info(@PathVariable("ezid") Integer ezid){
        List<EvwJjitem> evwJjitem = iEvwJjitemService.getEvwJjitem(ezid);
        return success(evwJjitem, EvwJjitemRespDTO.class);
    }
}
