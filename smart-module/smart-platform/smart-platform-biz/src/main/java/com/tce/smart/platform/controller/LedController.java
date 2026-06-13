package com.tce.smart.platform.controller;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.Led;
import com.tce.smart.platform.api.dto.QueryLedDTO;
import com.tce.smart.platform.api.dto.resp.LedLineRespDTO;
import com.tce.smart.platform.service.LedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 显示信息
 */
@Slf4j
@RestController
@RequestMapping("/led")
public class LedController extends BaseController {
    @Autowired
	LedService ledService;

    /**
     * 设置显示信息
     * @param led 显示信息
     */
    @PostMapping("/set")
    public Result set(@RequestBody @Valid Led led){
        Result result = ledService.set(led);
		log.info("led:{}",led);
        return result;
    }

    /**
     * 获取显示信息
     * @param queryLedDTO 显示信息
     */
    @PostMapping("/get")
    public Result get(@RequestBody QueryLedDTO queryLedDTO){
		Led led = ledService.get(queryLedDTO);
		log.info("led:{}",led);
        return success(led, LedLineRespDTO.class);
    }
}
