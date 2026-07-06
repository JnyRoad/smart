package com.tce.smart.platform.controller.securityzone;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthApplyPageQueryReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthApplyReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthApplyDetailRespDTO;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.core.service.SmtSecurityAreaService;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthApplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2021-07-29 11:13:31
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-保密区门禁申请")
@RequestMapping("/security/auth/apply")
public class SmtSecurityAuthApplyController extends BaseController {

    private final SmtSecurityAuthApplyService smtSecurityAuthApplyService;

    private final SmtSecurityAreaService smtSecurityAreaService;

    /**
     * 分页查询
     *
     * @param page  分页对象
     * @param query
     * @return
     */
    @PostMapping("/page")
    @ApiOperation("分页查询")
    public Result getSmtSecurityAuthApplyPage(Page page, @RequestBody(required = false) SecurityAuthApplyPageQueryReqDTO query) {
        return success(smtSecurityAuthApplyService.getPage(page, query));
    }


    /**
     * 通过id查询
     *
     * @param id id
     * @return Result
     */
    @GetMapping("/{id}")
    @ApiOperation("通过id查询")
    public Result getById(@PathVariable("id") String id) {
        return success(smtSecurityAuthApplyService.getById(Long.parseLong(id)), SecurityAuthApplyDetailRespDTO.class);
    }

    /**
     * 新增
     *
     * @param reqDTO
     * @return Result
     */
    @SysLog("新增")
    @ApiOperation("新增")
    @PostMapping("/save")
    public Result save(@RequestBody SecurityAuthApplyReqDTO reqDTO) {
        return success(smtSecurityAuthApplyService.saveApply(reqDTO));
    }


    /**
     * 通过id删除
     *
     * @param id id
     * @return Result
     */
    @SysLog("删除")
    @PostMapping("/{id}")
    public Result removeById(@PathVariable Integer id) {
        return success(smtSecurityAuthApplyService.removeById(id));
    }

    /**
     * 手动下发（管理端操作，需菜单按钮权限码，PR #118/#119 评审安全后续项）
     *
     * @param id
     * @return
     */
    @PreAuthorize("@pms.hasPermission('platform_security_auth_down')")
    @GetMapping("/down/{id}")
    @ApiOperation("手动下发")
    public Result downDevice(@PathVariable("id") String id) {
        return success(smtSecurityAuthApplyService.downDevice(Long.parseLong(id)));
    }

    /**
     * 提示信息推送（仅供 smart-schedule 定时任务 Feign 调用，对齐 /oa/status/task 的 @Inner）
     *
     * @param
     * @return
     */
    @Inner
    @GetMapping("/msg")
    @ApiOperation("下发提示信息推送")
    public void sendMessage() {
        smtSecurityAuthApplyService.sendMessage();
    }


    /**
     * 保密门禁申请 OA 审批状态对账任务（内部调用，供 smart-schedule 定时触发，spec §3.1.3/§3.1.4）
     *
     * @return Result
     */
    @Inner
    @GetMapping("/oa/status/task")
    public Result updateOaStatusTask() {
        smtSecurityAuthApplyService.updateOaStatusTask();
        return success();
    }

    @ApiOperation("OA人员证件类型枚举")
    @GetMapping("/enum/factory/type")
    public Result<List<Map<String, Object>>> getFactoryType(@RequestParam(value = "flag", required = false) Integer flag) {
        if (Objects.nonNull(flag)) {
            flag = flag == 1 ? 1 : 2;
        }
        List<Map<String, Object>> list = smtSecurityAreaService.list(flag).stream().map(smtSecurityArea -> {
            Map<String, Object> map = BeanUtil.beanToMap(smtSecurityArea);
            map.remove("id");
            return map;
        }).collect(Collectors.toList());
        return success(list);
    }

}
