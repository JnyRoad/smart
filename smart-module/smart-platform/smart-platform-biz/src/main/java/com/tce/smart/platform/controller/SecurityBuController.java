package com.tce.smart.platform.controller;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.Led;
import com.tce.smart.platform.api.dto.QueryLedDTO;
import com.tce.smart.platform.api.dto.req.SmtSecurityBuReqDTO;
import com.tce.smart.platform.api.dto.resp.LedLineRespDTO;
import com.tce.smart.platform.core.entity.SmtSecurityBu;
import com.tce.smart.platform.service.LedService;
import com.tce.smart.platform.service.SmtSecurityBuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.field.FieldDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 显示信息
 */
@Slf4j
@RestController
@RequestMapping("/security/bu")
@Api(value = "BU权限策略关联", tags = "BU权限策略关联")
public class SecurityBuController extends BaseController {

    @Autowired
	SmtSecurityBuService smtSecurityBuService;

    @PostMapping("/edit")
	@ApiOperation(value = "编辑")
    public Result set(@RequestBody List<SmtSecurityBuReqDTO> reqDTO){
        return success(smtSecurityBuService.editRelation(reqDTO));
    }

    @GetMapping("/get/{parkId}")
	@ApiOperation(value = "获取bu权限策略列表")
    public Result get(@PathVariable("parkId")Integer parkId){
		return success(smtSecurityBuService.getBuList(parkId));
    }
}
