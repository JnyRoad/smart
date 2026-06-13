package com.tce.smart.data.api.feign.intergration;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.intergration.req.BridgeAnswerDTO;
import com.tce.smart.data.api.dto.intergration.req.EhrSyncPersonReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @author: luohongwen.
 * @Date:Created in 2019/11/7 .
 * @Description: 提供给分发服务通知调用
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteIntergrationService {

    /**
     * 接收卡片新增结果
     * @param dto
     * @param from
     * @return
     */
    @PostMapping("/access/inner/card/add/answer")
    Result<Boolean> acsCardAddAnswer(@RequestBody BridgeAnswerDTO dto, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 接收卡片更新结果
     * @param dto
     * @param from
     * @return
     */
    @PostMapping("/access/inner/card/update/answer")
    Result<Boolean> acsCardUpdateAnswer(@RequestBody BridgeAnswerDTO dto, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 接收卡片删除结果
     * @param dto
     * @param from
     * @return
     */
    @PostMapping("/access/inner/card/del/answer")
    Result<Boolean> acsCardDelAnswer(@RequestBody BridgeAnswerDTO dto, @RequestHeader(SecurityConstants.FROM) String from);


    /**
     * 接收人员通行记录通知
     * @param dto
     * @param from
     * @return
     */
    @PostMapping("/access/inner/log")
    Result<Boolean> accessLogNotify(@RequestBody BridgeAnswerDTO dto, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 接收设备状态变更通知
     * @param dto
     * @param from
     * @return
     */
    @PostMapping("/common/inner/device/state")
    Result<Boolean> deviceStateNotify(@RequestBody BridgeAnswerDTO dto, @RequestHeader(SecurityConstants.FROM) String from);


    /**
     * 接收车辆通行记录通知
     * @param dto
     * @param from
     * @return
     */
    @PostMapping("/gate/inner/log")
    Result<Boolean> gateLogNotify(@RequestBody BridgeAnswerDTO dto, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 接收越界报警
     * @param dto
     * @param from
     * @return
     */
    @PostMapping("/camera/inner/cross/border/alarm")
    Result<Boolean> borderAlarm(@RequestBody BridgeAnswerDTO dto, @RequestHeader(SecurityConstants.FROM) String from);


    /**
     * 接收人证比对结果
     * @param dto
     * @param from
     * @return
     */
    @PostMapping("/terminal/inner/res")
    Result<Boolean> pidcResult(@RequestBody BridgeAnswerDTO dto, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 接收人脸抓拍结果
     * @param dto
     * @param from
     * @return
     */
    @PostMapping("/camera/inner/face/snap")
    Result<Boolean> faceSnapNotify(@RequestBody BridgeAnswerDTO dto, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 同步ehr员工数据
     * @param reqDTO
     * @param from
     * @return
     */
    @PostMapping("/common/inner/syncPerson")
    Result<Boolean> syncPerson(@RequestBody EhrSyncPersonReqDTO reqDTO, @RequestHeader(SecurityConstants.FROM) String from);
}
