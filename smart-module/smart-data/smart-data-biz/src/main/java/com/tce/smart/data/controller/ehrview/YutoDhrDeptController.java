package com.tce.smart.data.controller.ehrview;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsdepRespDTO;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.entity.YutoDhrDept;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import com.tce.smart.ehrview.core.service.YutoDhrDeptService;
import com.tce.smart.ehrview.core.service.YutoDhrOrgsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("/ys/dep")
public class YutoDhrDeptController extends BaseController {

    @Autowired
    private YutoDhrDeptService yutoDhrDeptService;

    /**
     * 根据compId获取公司信息
     * @param compId
     * @return
     */
    @Inner
    @OpenApi("server")
    @GetMapping("/comp")
    public Result<List<OvwYsdepRespDTO>> getByCompId(@RequestParam("compId") Integer compId){
        List<OvwYsdep> ovwYsdepList = yutoDhrDeptService.getByCompId(compId);
        return success(ovwYsdepList, OvwYsdepRespDTO.class);
    }
    /**
     * 根据depId获取公司信息
     * @return
     */
    @Inner
    @OpenApi("server")
    @GetMapping("/info")
    public Result<OvwYsdepRespDTO> getByDepId(@RequestParam("depId") Integer depId){
        OvwYsdep ovwYsdep = yutoDhrDeptService.getByDepId(depId);
        return success(ovwYsdep, OvwYsdepRespDTO.class);
    }


    @Inner
    @OpenApi("server")
    @GetMapping("/parentDep")
    public Result<List<OvwYsdepRespDTO>> getParentDep(@RequestParam("depId") Integer depId){
	 List<OvwYsdep> ovwYsdepList = yutoDhrDeptService.getParentDep(depId);
         return success(ovwYsdepList, OvwYsdepRespDTO.class);    }


}
