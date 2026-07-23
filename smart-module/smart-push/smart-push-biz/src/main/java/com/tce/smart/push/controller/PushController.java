package com.tce.smart.push.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.push.constant.PushConstants;
import com.tce.smart.push.dto.ApnsMessageDTO;
import com.tce.smart.push.dto.NoticeMessageDTO;
import com.tce.smart.push.dto.PushMessageDTO;
import com.tce.smart.push.service.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/7/2 .
 * @Modified By:
 */
@Slf4j
@RestController
@RequestMapping("/push")
public class PushController {

    @Autowired
    private PushService pushService;

    @PostMapping("/notice")
    @Inner
    @OpenApi("server")
    public Result notice(@RequestBody NoticeMessageDTO noticeMessageDTO){
        Result result = Result.builder().code(HttpStatus.BAD_REQUEST.value()).data("").build();
        try {
            boolean success  = pushService.notice(noticeMessageDTO);
            if(success) {
				result.setCode(HttpStatus.OK.value());
			}
        }catch (IllegalArgumentException ae){
            result.setMsg(ae.getMessage());
            log.warn("{}",PushConstants.PushDesc.PUSH_ILLEGAL_ARG_ERROR,ae);
        }
        catch (Exception e) {
            result.setMsg(PushConstants.PushDesc.PUSH_NOTICE_ERROR);
            log.warn("{}",PushConstants.PushDesc.PUSH_NOTICE_ERROR,e);
        }
        return result;
    }

    @PostMapping("/transmission")
    @Inner
    @OpenApi("server")
    @ResponseBody
    public Result transmission(@RequestBody ApnsMessageDTO apnsMessageDTO){
        Result result = Result.builder().code(HttpStatus.BAD_REQUEST.value()).build();
        try {
            boolean success = pushService.transmission(apnsMessageDTO);
            if(success)
                result.setCode(HttpStatus.OK.value());
        }catch (IllegalArgumentException ae){
            result.setMsg(ae.getMessage());
            log.warn("{}",PushConstants.PushDesc.PUSH_ILLEGAL_ARG_ERROR,ae);
        }
        catch (Exception e) {
            result.setMsg(PushConstants.PushDesc.PUSH_TRANSMISSION_ERROR);
            log.warn("{}",PushConstants.PushDesc.PUSH_TRANSMISSION_ERROR,e);
        }
        return result;
    }

    @PostMapping("/pushAll")
    @Inner
    @OpenApi("server")
    @ResponseBody
    public Result pushAll(@RequestBody PushMessageDTO pushMessageDTO){
        Result result = Result.builder().code(HttpStatus.BAD_REQUEST.value()).build();
        try {
            boolean success = pushService.pushAll(pushMessageDTO);
            if(success)
                result.setCode(HttpStatus.OK.value());
        }catch (IllegalArgumentException ae){
            result.setMsg(ae.getMessage());
            log.warn("{}",PushConstants.PushDesc.PUSH_ILLEGAL_ARG_ERROR,ae);
        }
        catch (Exception e) {
            result.setMsg(PushConstants.PushDesc.PUSH_ALL_ERROR);
            log.warn("{}",PushConstants.PushDesc.PUSH_ALL_ERROR,e);
        }
        return result;
    }

}
