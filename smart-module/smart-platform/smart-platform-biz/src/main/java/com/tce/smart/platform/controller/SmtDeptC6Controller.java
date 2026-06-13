package com.tce.smart.platform.controller;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.Led;
import com.tce.smart.platform.api.dto.QueryLedDTO;
import com.tce.smart.platform.api.dto.resp.LedLineRespDTO;
import com.tce.smart.platform.service.LedService;
import com.tce.smart.platform.service.SmtDeptC6Service;
import com.tce.smart.platform.service.SmtExDeptC6Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 显示信息
 */
@Slf4j
@RestController
@RequestMapping("/c6/dept")
public class SmtDeptC6Controller extends BaseController {

    @Autowired
	SmtDeptC6Service smtDeptC6Service;

    /**
     * 设置显示信息
     * @param
     */
    @GetMapping("/tree")
    public Result getTree(){
        return success(smtDeptC6Service.getC6List());
    }

}
