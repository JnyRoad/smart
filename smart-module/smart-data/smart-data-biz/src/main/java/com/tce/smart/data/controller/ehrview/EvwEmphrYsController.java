package com.tce.smart.data.controller.ehrview;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.EvwEmphrYsDTO;
import com.tce.smart.data.api.dto.ehrview.req.EvwEmphrYsBlackReqDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsBlackRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.YsLeaveRespDTO;
import com.tce.smart.dhrview.core.entity.YutoDhrPsndo;
import com.tce.smart.dhrview.core.service.YutoDhrPsndoService;
import com.tce.smart.ehrview.core.entity.EvwEmphrYs;
import com.tce.smart.ehrview.core.service.IEvwEmphrYsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author WangJinbo
 * @since 2019-05-03
 */
@RestController
@RequestMapping("/emphr/ys")
public class EvwEmphrYsController extends BaseController {

    @Autowired
    private IEvwEmphrYsService iEvwEmphrYsService;

    @Autowired
    private YutoDhrPsndoService yutoDhrPsndoService;

    /**
     * 根据员工号获取员工基本信息
     * @param badge
     * @return
     */
    @Inner
    @GetMapping("/info")
    public Result<EvwEmphrYsRespDTO> info(@RequestParam("badge") String badge) {
        YutoDhrPsndo evwEmphrYs = yutoDhrPsndoService.getByBadge(badge);
        return success(evwEmphrYs, EvwEmphrYsRespDTO.class);
    }
    /**
     * 根据员工号获取员工基本信息(离职)
     * @param badge
     * @return
     */
    @Inner
    @GetMapping("/leave")
    public Result<YsLeaveRespDTO> leave(@RequestParam("badge") String badge) {
        YutoDhrPsndo evwEmphrYs = yutoDhrPsndoService.getByBadge(badge);
        return success(evwEmphrYs, YsLeaveRespDTO.class);
    }
    /**
     * 根据BU获取员工
     * @param compId
     * @return
     */
    @Inner
    @GetMapping("/getByCompId")
    public Result<List<EvwEmphrYsDTO>> getByCompId(@RequestParam("compId") Integer compId) {
        List<YutoDhrPsndo> evwEmphrYs = yutoDhrPsndoService.getByCompId(compId);
        return success(evwEmphrYs, EvwEmphrYsDTO.class);
    }
    /**
     * 根据BU获取员工
     * @param compId
     * @return
     */
    @Inner
    @GetMapping("/getInStaffByCompId")
    public Result<List<EvwEmphrYsDTO>> getInStaffByCompId(@RequestParam("compId") Integer compId) {
        List<YutoDhrPsndo> evwEmphrYs = yutoDhrPsndoService.getInStaffByCompId(compId);
        return success(evwEmphrYs, EvwEmphrYsDTO.class);
    }
    /**
     * 获取黑名单人员
     * @return
     */
    @Inner
    @GetMapping("/getBlack")
    public Result getBlack(Page page,EvwEmphrYsBlackReqDTO req) {
//    	IPage<EvwEmphrYs> evwEmphrYsList = iEvwEmphrYsService.getBlack(page,req.getCerNo(),req.getName());
//        return success(evwEmphrYsList,EvwEmphrYsBlackRespDTO.class);
        IPage<EvwEmphrYsBlackRespDTO> iPage = new Page<>(page.getCurrent(), page.getSize());
        iPage.setPages(0);
        iPage.setTotal(0);
        iPage.setRecords(Lists.newArrayList());
        return success(iPage);
    }

    @GetMapping("/getBlackInfo")
    public Result<List<EvwEmphrYsBlackRespDTO>> getBlackInfo(EvwEmphrYsBlackReqDTO req) {
//    	List<EvwEmphrYs>  evwEmphrYs= iEvwEmphrYsService.list(Wrappers.<EvwEmphrYs> query().lambda()
//				.like(StringUtils.isNotBlank(req.getCerNo()), EvwEmphrYs::getCertno, req.getCerNo())
//				.eq(EvwEmphrYs::getIsBlackList, 1));
//        return success(evwEmphrYs,EvwEmphrYsBlackRespDTO.class);
        return success(Lists.newArrayList());
    }
}
