package com.tce.smart.bridge.controller;

import com.tce.smart.bridge.component.WaterEleHelper;
import com.tce.smart.bridge.core.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;

/**
 * 水电表(物联网关Java)接口
 *
 * @author Li.JiaJun
 * @since 2021/12/21 10:29
 */
@RestController
@RequestMapping("/bridge")
public class BridgeController {

    @Autowired
    private WaterEleHelper waterEleHelper;

    //---------------------------- 水电表在线状态 -------------------------------------
    /**
     * 检测水电表集中器是否在线
     *
     * @param br
     * @return
     */
    @PostMapping("/checkOnline")
    public Result<Boolean> checkOnline(BufferedReader br) {
        return Result.success(waterEleHelper.checkOnline(waterEleHelper.getBody(br)));
    }

    /**
     * 检测外置阀门集中器是否在线
     *
     * @param br
     * @return
     */
    @PostMapping("/checkOnlineValve")
    public Result<Boolean> checkOnlineValve(BufferedReader br) {
        return Result.success(waterEleHelper.checkOnlineValve(waterEleHelper.getBody(br)));
    }

    //---------------------------- 电表 -------------------------------------

    /**
     * 下发电表集中器档案
     *
     * @param br
     * @return
     */
    @PostMapping("/ele/issue/file")
    public Result<Boolean> eleIssueFile(BufferedReader br) {
        return Result.success(waterEleHelper.eleIssueFile(waterEleHelper.getBody(br)));
    }

    /**
     * 删除电表集中器档案
     *
     * @param br
     * @return
     */
    @PostMapping("/ele/del/file")
    public Result<Boolean> eleDelFile(BufferedReader br) {
        return Result.success(waterEleHelper.eleDelFile(waterEleHelper.getBody(br)));
    }

    /**
     * 查询电表集中器档案
     *
     * @param br
     * @return
     */
    @PostMapping("/ele/query/file")
    public Result<Boolean> eleQueryFile(BufferedReader br) {
        return Result.success(waterEleHelper.eleQueryFile(waterEleHelper.getBody(br)));
    }

    /**
     * 智能电表读数
     *
     * @param br
     * @return
     */
    @PostMapping("/elemeter/read")
    public Result<Boolean> eleRead(BufferedReader br) {
        return Result.success(waterEleHelper.eleRead(waterEleHelper.getBody(br)));
    }

    /**
     * 智能电表闸门状态读取
     *
     * @param br
     * @return
     */
    @PostMapping("/ele/meter/brake/read")
    public Result<Boolean> brakeRead(BufferedReader br) {
        return Result.success(waterEleHelper.eleMeterStatusQuery(waterEleHelper.getBody(br)));
    }

    /**
     * 电表闸门控制
     *
     * @param br
     * @return
     */
    @PostMapping("/ele/brake/control")
    public Result<Boolean> brakeControl(BufferedReader br) {
        waterEleHelper.brakeControl(waterEleHelper.getBody(br));
        return Result.success(Boolean.TRUE);
    }

    //---------------------------- 水表 -------------------------------------

    /**
     * 下发水表集中器档案
     *
     * @param br
     * @return
     */
    @PostMapping("/water/issue/file")
    public Result<Boolean> waterIssueFile(BufferedReader br) {
        return Result.success(waterEleHelper.waterIssueFile(waterEleHelper.getBody(br)));
    }

    /**
     * 删除水表集中器档案
     *
     * @param br
     * @return
     */
    @PostMapping("/water/del/file")
    public Result<Boolean> waterDelFile(BufferedReader br) {
        return Result.success(waterEleHelper.waterDelFile(waterEleHelper.getBody(br)));
    }

    /**
     * 查询水表集中器档案
     *
     * @param br
     * @return
     */
    @PostMapping("/water/query/file")
    public Result<Boolean> waterQueryFile(BufferedReader br) {
        return Result.success(waterEleHelper.waterQueryFile(waterEleHelper.getBody(br)));
    }

    /**
     * 智能水表读数
     *
     * @param br
     * @return
     */
    @PostMapping("/watermeter/read")
    public Result<Boolean> waterRead(BufferedReader br) {
        return Result.success(waterEleHelper.waterRead(waterEleHelper.getBody(br)));
    }

    /**
     * 内置阀门控制
     *
     * @param br
     * @return
     */
    @PostMapping("/watermeter/in_valve/control")
    public Result<Boolean> inValveControl(BufferedReader br) {
        waterEleHelper.inValveControl(waterEleHelper.getBody(br));
        return Result.success(Boolean.TRUE);
    }

    /**
     * 外置阀门控制
     *
     * @param br
     * @return
     */
    @PostMapping("/watermeter/out_valve/control")
    public Result<Boolean> outValveControl(BufferedReader br) {
        Boolean res = waterEleHelper.outValveControl(waterEleHelper.getBody(br));
        return Result.success(res);
    }

    /**
     * 外置阀门远程功能控制
     *
     * @param br
     * @return
     */
    @PostMapping("/watermeter/out_valve_remote/control")
    public Result<Boolean> outValveRemoteControl(BufferedReader br) {
        Boolean res = waterEleHelper.outValveRemoteControl(waterEleHelper.getBody(br));
        return Result.success(res);
    }
}
