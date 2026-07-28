package com.tce.smart.data.controller.ehrview;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @Autowired
    private OpenApiAuthenticationAdapter openApiAuthenticationAdapter;

    /** Platform 服务客户端由 Nacos 精确指定；空配置必须拒绝访客黑名单查询。 */
    @Value("${security.inner.visitor-blacklist.platform-client-id:}")
    private String platformServiceClientId;

    /**
     * 根据员工号获取员工基本信息
     * @param badge
     * @return
     */
    @Inner
    @OpenApi("server")
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
    @OpenApi("server")
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
    @OpenApi("server")
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
    @OpenApi("server")
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
    @OpenApi("server")
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

    /** 访客预约链只需要黑名单命中布尔结果，不能把员工黑名单 DTO 透传到 Platform。 */
    @Inner
    @OpenApi("server")
    @GetMapping("/internal/visitor-blacklist-status")
    public Result<Boolean> getVisitorBlacklistStatus(@RequestParam("cerNo") String certNo,
            @RequestHeader(value = SecurityConstants.FROM, required = false) String from,
            @RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
        assertPlatformBlacklistCaller(from, purpose);
        String normalizedCertNo = certNo.replaceAll("\\s+", "").toUpperCase(java.util.Locale.ROOT);
        List<EvwEmphrYs> blacklistedEmployees = iEvwEmphrYsService.list(Wrappers.<EvwEmphrYs>query().lambda()
                .eq(EvwEmphrYs::getCertno, normalizedCertNo).eq(EvwEmphrYs::getIsBlackList, "1"));
        return success(blacklistedEmployees != null && !blacklistedEmployees.isEmpty());
    }

    /** 精确 client_id、用途和 client_credentials 缺一不可；Nacos 空配置按 fail-closed 处理。 */
    private void assertPlatformBlacklistCaller(String from, String purpose) {
        Authentication authentication = SecurityUtils.getAuthentication();
        if (!SecurityConstants.FROM_IN.equals(from) || !"visitor-blacklist".equals(purpose)
                || StringUtils.isBlank(platformServiceClientId) || authentication == null
                || !openApiAuthenticationAdapter.isClientOnly(authentication)
                || !platformServiceClientId.equals(openApiAuthenticationAdapter.clientId(authentication))) {
            throw new AccessDeniedException("访客黑名单内部调用未获授权");
        }
    }
}
