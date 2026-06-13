package com.tce.smart.bridge.component;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.bridge.core.dto.DeviceDataDTO;
import com.tce.smart.bridge.core.dto.MeterFileDTO;
import com.tce.smart.bridge.core.enums.EventEnum;
import com.tce.smart.bridge.kafka.KafkaProducer;
import com.tce.smart.bridge.netty.constant.Constants;
import com.tce.smart.bridge.netty.dto.EleReadingDTO;
import com.tce.smart.bridge.netty.dto.WaterReadingDTO;
import com.tce.smart.bridge.netty.tcp.NettyClient;
import com.tce.smart.bridge.netty.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Li.JiaJun
 * @since 2021/12/21 10:36
 */
@Slf4j
@Component
@EnableAsync
public class WaterEleHelper {

    @Autowired
    private KafkaProducer kafkaProducer;

    private final ThreadPoolExecutor threadPoolExecutor = ThreadUtil.newExecutor(2,100);

    /**
     * 获取传递过来的JSON字符串
     *
     * @param br
     * @return
     */
    public String getBody(BufferedReader br) {
        //body部分
        String inputLine;
        StringBuilder str = new StringBuilder();
        try {
            while ((inputLine = br.readLine()) != null) {
                str.append(inputLine);
            }
            br.close();
        } catch (IOException e) {
            log.error("IOException: ", e);
        }
        return str.toString();
    }

    /**
     * 转换DTO
     *
     * @param json
     * @return
     */
    private DeviceDataDTO transToDto(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        DeviceDataDTO dto = new DeviceDataDTO();
        Object deviceCode = jsonObject.get("deviceCode");
        Object deviceType = jsonObject.get("deviceType");
        Object deviceIp = jsonObject.get("deviceIp");
        Object devicePort = jsonObject.get("devicePort");
        Object waterMeterSeq = jsonObject.get("waterMeterSeq");
        Object valveSeq = jsonObject.get("valveSeq");
        Object valveOnOff = jsonObject.get("valveOnOff");
        Object electricMeterSeq = jsonObject.get("electricMeterSeq");
        Object eleMeterAddress = jsonObject.get("eleMeterAddress");
        Object eleMeterPort = jsonObject.get("eleMeterPort");
        if (Objects.nonNull(deviceCode)) {
            dto.setDeviceCode(deviceCode.toString());
        }
        if (Objects.nonNull(deviceType)) {
            dto.setDeviceType(Integer.parseInt(deviceType.toString()));
        }
        if (Objects.nonNull(deviceIp)) {
            dto.setDeviceIp(deviceIp.toString());
        }
        if (Objects.nonNull(devicePort)) {
            dto.setDevicePort(Integer.parseInt(devicePort.toString()));
        }
        if (Objects.nonNull(waterMeterSeq)) {
            dto.setWaterMeterSeq(Integer.parseInt(waterMeterSeq.toString()));
        }
        if (Objects.nonNull(valveSeq)) {
            dto.setValveSeq(Integer.parseInt(valveSeq.toString()));
        }
        if (Objects.nonNull(valveOnOff)) {
            dto.setValveOnOff(Integer.parseInt(valveOnOff.toString()));
        }
        if (Objects.nonNull(electricMeterSeq)) {
            dto.setElectricMeterSeq(Integer.parseInt(electricMeterSeq.toString()));
        }
        if (Objects.nonNull(eleMeterAddress)) {
            dto.setEleMeterAddress(eleMeterAddress.toString());
        }
        if (Objects.nonNull(eleMeterPort)) {
            dto.setEleMeterPort(Integer.parseInt(eleMeterPort.toString()));
        }
        return dto;
    }

    /**
     * 转换电表集中器下载档案DTO
     *
     * @param json
     * @return
     */
    private MeterFileDTO transToMeterFileDto(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        MeterFileDTO dto = new MeterFileDTO();
        Object deviceIp = jsonObject.get("deviceIp");
        Object devicePort = jsonObject.get("devicePort");
        Object meterNum = jsonObject.get("meterNum");
        Object concentratorAddress = jsonObject.get("concentratorAddress");
        Object meterJson = jsonObject.get("meterJson");
        if (Objects.nonNull(deviceIp)) {
            dto.setDeviceIp(deviceIp.toString());
        }
        if (Objects.nonNull(devicePort)) {
            dto.setDevicePort(Integer.parseInt(devicePort.toString()));
        }
        if (Objects.nonNull(meterNum)) {
            dto.setMeterNum(Integer.parseInt(meterNum.toString()));
        }
        if (Objects.nonNull(concentratorAddress)) {
            dto.setConcentratorAddress(concentratorAddress.toString());
        }
        if (Objects.nonNull(meterJson)) {
            dto.setMeterJson(meterJson.toString());
        }
        return dto;
    }

    //---------------------------- 水电表在线状态 -------------------------------------

    /**
     * 检测水电表集中器是否在线
     *
     * @param json
     * @return
     */
    public Boolean checkOnline(String json) {
        DeviceDataDTO dataDTO = transToDto(json);
        return NettyServerUtils.checkClient(dataDTO.getDeviceIp());
    }

    /**
     * 检测外置阀门集中器是否在线
     *
     * @param json
     * @return
     */
    public Boolean checkOnlineValve(String json) {
        DeviceDataDTO dataDTO = transToDto(json);
        try {
            String clientId = dataDTO.getDeviceIp() + ":" + dataDTO.getDevicePort();
            boolean isOnline = NettyTcpClientUtils.checkClient(clientId);
            if (!isOnline) {
                threadPoolExecutor.execute(() -> {
                    try {
                        NettyClient.init(dataDTO.getDeviceIp(), dataDTO.getDevicePort().toString());
                    } catch (Exception e) {
                        log.error("连接外置阀门集中器失败", e);
                    }
                });
                return Boolean.FALSE;
            }
            log.info("外置阀门集中器在线,{}",clientId);
        } catch (Exception e) {
            log.error("连接外置阀门集中器失败", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    //---------------------------- 电表 -------------------------------------

    /**
     * 下发电表集中器档案
     *
     * @param json
     * @return
     */
    public Boolean eleIssueFile(String json) {
        MeterFileDTO dataDTO = transToMeterFileDto(json);
        try {
            log.info("下发电表集中器档案请求数据：{}", dataDTO);
            return SendEleMessageUtils.eleIssueFile(dataDTO);
        } catch (Exception e) {
            log.error("下发电表集中器档案失败", e);
            return Boolean.FALSE;
        }
    }

    /**
     * 下发电表集中器档案
     *
     * @param json
     * @return
     */
    public Boolean eleDelFile(String json) {
        MeterFileDTO dataDTO = transToMeterFileDto(json);
        try {
            log.info("删除电表集中器档案请求数据：{}", dataDTO);
            return SendEleMessageUtils.eleDelFile(dataDTO);
        } catch (Exception e) {
            log.error("删除电表集中器档案失败", e);
            return Boolean.FALSE;
        }
    }

    /**
     * 查询电表集中器档案
     *
     * @param json
     * @return
     */
    public Boolean eleQueryFile(String json) {
        MeterFileDTO dataDTO = transToMeterFileDto(json);
        try {
            log.info("查询电表集中器档案请求数据：{}", dataDTO);
            return SendEleMessageUtils.eleQueryFile(dataDTO);
        } catch (Exception e) {
            log.error("查询电表集中器档案失败", e);
            return Boolean.FALSE;
        }
    }

    /**
     * 智能电表读数
     *
     * @param json
     * @return
     */
    public Boolean eleRead(String json) {
        DeviceDataDTO dataDTO = transToDto(json);
        try {
            log.info("电表读取请求数据：{}", dataDTO);
            EleReadingDTO eleReadingDTO = SendEleMessageUtils.eleHourReading(dataDTO.getDeviceIp(), dataDTO.getElectricMeterSeq());
            if (Objects.isNull(eleReadingDTO)) {
                return Boolean.FALSE;
            }
            log.info("电表读取成功：{}", eleReadingDTO);
            JSONObject eleReading = new JSONObject();
            eleReading.put("deviceCode", dataDTO.getDeviceCode());
            eleReading.put("eleMeterSeq", dataDTO.getElectricMeterSeq());
            // 当前电表读数
            eleReading.put("eleMeterCurrVal", eleReadingDTO.getCurrentReading());
            // 采集时间
            eleReading.put("collectTime", eleReadingDTO.getCollectTime());
            kafka(EventEnum.ELE_REPEATER_READ.getKey(), JSONUtil.toJsonStr(eleReading));
        } catch (Exception e) {
            log.error("电表集中器连接超时，读取失败", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public Boolean eleMeterStatusQuery(String json) {
        DeviceDataDTO dataDTO = transToDto(json);
        try {
            JSONObject eleControl = new JSONObject();
            log.info("电表闸门请求数据：{}", dataDTO);
            Integer state = SendEleMessageUtils.getMeterStatus(dataDTO.getDeviceIp(),
                    dataDTO.getEleMeterAddress(), dataDTO.getEleMeterPort());
            log.info("电表闸门状态查询成功：{}，电表地址：{}", state, dataDTO.getEleMeterAddress());
            eleControl.put("deviceCode", dataDTO.getDeviceCode());
            eleControl.put("eleMeterSeq", dataDTO.getElectricMeterSeq());
            eleControl.put("brakeState", state);
            kafka(EventEnum.ELE_REPEATER_BRAKE_STATE.getKey(), JSONUtil.toJsonStr(eleControl));
        } catch (Exception e) {
            log.error("电表闸门状态查询失败", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Async
    public void brakeControl(String json) {
        DeviceDataDTO dataDTO = transToDto(json);
        JSONObject eleControl = new JSONObject();
        eleControl.put("deviceCode", dataDTO.getDeviceCode());
        eleControl.put("eleMeterSeq", dataDTO.getElectricMeterSeq());
        try {
            log.info("电表闸门控制请求数据：{}", dataDTO);
            Boolean change = SendEleMessageUtils.changeBrakeStatus(dataDTO.getDeviceIp(),
                    dataDTO.getEleMeterAddress(), dataDTO.getEleMeterPort(), dataDTO.getValveOnOff());
            if (change) {
                // 闸门控制成功
                eleControl.put("brakeState", dataDTO.getValveOnOff());
            } else {
                // 闸门控制失败
                eleControl.put("brakeState", dataDTO.getValveOnOff() == 0 ? 1 : 0);
            }
        } catch (Exception e) {
            log.error("连接超时，电表闸门控制失败", e);
            // 闸门控制失败
            eleControl.put("brakeState", dataDTO.getValveOnOff() == 0 ? 1 : 0);
        }
        kafka(EventEnum.ELE_REPEATER_BRAKE_STATE.getKey(), JSONUtil.toJsonStr(eleControl));
    }

    //---------------------------- 水表 -------------------------------------

    /**
     * 下发水表集中器档案
     *
     * @param json
     * @return
     */
    public Boolean waterIssueFile(String json) {
        MeterFileDTO dataDTO = transToMeterFileDto(json);
        try {
            log.info("下发水表集中器档案请求数据：{}", dataDTO);
            return SendWaterMessageUtils.waterIssueFile(dataDTO);
        } catch (Exception e) {
            log.error("下发水表集中器档案失败", e);
            return Boolean.FALSE;
        }
    }

    /**
     * 删除水表集中器档案
     *
     * @param json
     * @return
     */
    public Boolean waterDelFile(String json) {
        MeterFileDTO dataDTO = transToMeterFileDto(json);
        try {
            log.info("删除水表集中器档案请求数据：{}", dataDTO);
            return SendWaterMessageUtils.waterDelFile(dataDTO);
        } catch (Exception e) {
            log.error("删除水表集中器档案失败", e);
            return Boolean.FALSE;
        }
    }

    /**
     * 查询水表集中器档案
     *
     * @param json
     * @return
     */
    public Boolean waterQueryFile(String json) {
        MeterFileDTO dataDTO = transToMeterFileDto(json);
        try {
            log.info("查询水表集中器档案请求数据：{}", dataDTO);
            return SendWaterMessageUtils.waterQueryFile(dataDTO);
        } catch (Exception e) {
            log.error("查询水表集中器档案失败", e);
            return Boolean.FALSE;
        }
    }

    /**
     * 智能水表读数
     *
     * @param json
     * @return
     */
    public Boolean waterRead(String json) {
        DeviceDataDTO dataDTO = transToDto(json);
        try {
            log.info("水表读取请求数据：{}", dataDTO);
            WaterReadingDTO waterReadingDTO = SendWaterMessageUtils.waterDayReading(dataDTO.getDeviceIp(), dataDTO.getWaterMeterSeq());
            if (Objects.isNull(waterReadingDTO)) {
                return Boolean.FALSE;
            }
            log.info("水表读取成功：{}", waterReadingDTO);
            JSONObject waterReading = new JSONObject();
            waterReading.put("deviceCode", dataDTO.getDeviceCode());
            waterReading.put("waterMeterSeq", dataDTO.getWaterMeterSeq());
            // 当前水表读数
            waterReading.put("waterMeterCurrVal", waterReadingDTO.getCurrentReading());
            // 阀门状态
            waterReading.put("valveState", waterReadingDTO.getIsOpen());
            // 采集时间
            waterReading.put("collectTime", waterReadingDTO.getCollectTime());
            kafka(EventEnum.WATER_REPEATER_READ.getKey(), JSONUtil.toJsonStr(waterReading));
        } catch (Exception e) {
            log.error("水表集中器连接超时，读取失败", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 内置阀门控制
     *
     * @param json
     * @return
     */
    @Async
    public void inValveControl(String json) {
        DeviceDataDTO dataDTO = transToDto(json);
        JSONObject waterReading = new JSONObject();
        waterReading.put("deviceCode", dataDTO.getDeviceCode());
        waterReading.put("waterMeterSeq", dataDTO.getWaterMeterSeq());
        try {
            Boolean change = SendWaterMessageUtils.changeValveStatus(dataDTO.getDeviceIp(), dataDTO.getWaterMeterSeq(), dataDTO.getValveOnOff());
            if (change) {
                // 阀门控制成功
                waterReading.put("valveState", dataDTO.getValveOnOff());
            } else {
                // 阀门控制失败
                waterReading.put("valveState", dataDTO.getValveOnOff() == 0 ? 1 : 0);
            }
        } catch (Exception e) {
            log.error("连接超时，内置阀门控制失败", e);
            // 阀门控制失败
            waterReading.put("valveState", dataDTO.getValveOnOff() == 0 ? 1 : 0);
        }
        kafka(EventEnum.WATER_REPEATER_IN_VALVE_STATE.getKey(), JSONUtil.toJsonStr(waterReading));
    }

    /**
     * 外置阀门开关控制
     *
     * @param json
     * @return
     */
    public Boolean outValveControl(String json) {
        DeviceDataDTO dataDTO = transToDto(json);
        JSONObject waterReading = new JSONObject();
        Integer valveSeq = dataDTO.getValveSeq();
        String hexSeq = Integer.toHexString(valveSeq);
        if (hexSeq.length() < Constants.TWO) {
            hexSeq = "0" + hexSeq;
        }
        Integer valveState = dataDTO.getValveOnOff();
        String stopMessage = hexSeq + "06000000FA";
        String message;
        // 关闭
        if (valveState == 0) {
            message = hexSeq + "0600000000";
            // 开启
        } else {
            message = hexSeq + "06000000C8";
        }
        waterReading.put("deviceCode", dataDTO.getDeviceCode());
        waterReading.put("valveSeq", dataDTO.getValveSeq());
        try {
            byte[] stopMessageBytes = ConvertCodeUtils.hexString2Bytes(stopMessage);
            String stopMessageCrc = ConvertCodeUtils.getCRC(stopMessageBytes);
            byte[] messageBytes = ConvertCodeUtils.hexString2Bytes(message);
            String messageCrc = ConvertCodeUtils.getCRC(messageBytes);
            String clientId = dataDTO.getDeviceIp() + ":" + dataDTO.getDevicePort();

            //先发送停止指令
            log.info("外置阀门控制停止帧指令：{}", stopMessage + stopMessageCrc);
            String stopRespFrame = NettyTcpClientUtils.sendSyncMessage(clientId, stopMessage + stopMessageCrc);
            log.info("外置阀门控制停止帧返回：{}", stopRespFrame);

            //再发送操作指令
            log.info("外置阀门开关控制操作帧指令：{}", message + messageCrc);
            String respFrame = NettyTcpClientUtils.sendSyncMessage(clientId, message + messageCrc);
            log.info("外置阀门开关控制响应帧返回：{}", respFrame);

            //两个指令有响应 则表示操作成功
            if (Objects.isNull(stopRespFrame) || Objects.isNull(respFrame)) {
                waterReading.put("valveState", dataDTO.getValveOnOff() == 0 ? 1 : 0);
                return Boolean.TRUE;
            } else {
                waterReading.put("valveState", dataDTO.getValveOnOff());
            }
        } catch (Exception e) {
            log.error("外置阀门控制失败", e);
            waterReading.put("valveState", dataDTO.getValveOnOff() == 0 ? 1 : 0);
        }

        //kafka(EventEnum.WATER_REPEATER_OUT_VALVE_STATE.getKey(), JSONUtil.toJsonStr(waterReading));

        return Boolean.FALSE;
    }

    /**
     * 外置阀门远程功能控制
     *
     * @param json
     * @return
     */
    public Boolean outValveRemoteControl(String json) {
        DeviceDataDTO dataDTO = transToDto(json);
        JSONObject waterReading = new JSONObject();
        Integer valveSeq = dataDTO.getValveSeq();
        String hexSeq = Integer.toHexString(valveSeq);
        if (hexSeq.length() < Constants.TWO) {
            hexSeq = "0" + hexSeq;
        }
        Integer valveState = dataDTO.getValveOnOff();
        String stopMessage = hexSeq + "06000000FA";
        String message;
        // 关闭
        if (valveState == 0) {
            message = hexSeq + "0500000000";
            // 开启
        } else {
            message = hexSeq + "0500FF0000";
        }
        String searchMsg = "070100000018";
        waterReading.put("deviceCode", dataDTO.getDeviceCode());
        waterReading.put("valveSeq", dataDTO.getValveSeq());
        try {
            byte[] stopMessageBytes = ConvertCodeUtils.hexString2Bytes(stopMessage);
            String stopMessageCrc = ConvertCodeUtils.getCRC(stopMessageBytes);
            byte[] messageBytes = ConvertCodeUtils.hexString2Bytes(message);
            String messageCrc = ConvertCodeUtils.getCRC(messageBytes);
            byte[] searchMsgBytes = ConvertCodeUtils.hexString2Bytes(searchMsg);
            String searchMsgCrc = ConvertCodeUtils.getCRC(searchMsgBytes);
            String clientId = dataDTO.getDeviceIp() + ":" + dataDTO.getDevicePort();

            //先发送停止指令
            log.info("外置阀门控制停止帧指令：{}", stopMessage + stopMessageCrc);
            String stopRespFrame = NettyTcpClientUtils.sendSyncMessage(clientId, stopMessage + stopMessageCrc);
            log.info("外置阀门控制停止帧返回：{}", stopRespFrame);

            //再发送操作指令
            log.info("外置阀门远程控制操作帧指令：{}", message + messageCrc);
            String respFrame = NettyTcpClientUtils.sendSyncMessage(clientId, message + messageCrc);
            log.info("外置阀门远程控制响应帧返回：{}", respFrame);

//            String stopRespFrame = null;
//            String respFrame = null;
//            log.info("外置阀门查询操作帧指令：{}", searchMsg + searchMsgCrc);
//            String searchRespFrame = NettyTcpClientUtils.sendSyncMessage(clientId, searchMsg + searchMsgCrc);
//            log.info("外置阀门查询响应帧返回：{}", searchRespFrame);


            if (Objects.isNull(stopRespFrame) || Objects.isNull(respFrame)) {
                waterReading.put("valveState", dataDTO.getValveOnOff() == 0 ? 1 : 0);
                return Boolean.TRUE;
            } else {
                waterReading.put("valveState", dataDTO.getValveOnOff());
            }
        } catch (Exception e) {
            log.error("外置阀门控制失败", e);
            waterReading.put("valveState", dataDTO.getValveOnOff() == 0 ? 1 : 0);
        }
        //kafka(EventEnum.WATER_REPEATER_OUT_VALVE_STATE.getKey(), JSONUtil.toJsonStr(waterReading));
        return Boolean.FALSE;
    }

    /**
     * 外置阀门状态查询控制
     *
     * @param json
     * @return
     */
    public Boolean outValveSearchControl(String json) {
        DeviceDataDTO dataDTO = transToDto(json);
        JSONObject waterReading = new JSONObject();
        Integer valveSeq = dataDTO.getValveSeq();
        String hexSeq = Integer.toHexString(valveSeq);
        if (hexSeq.length() < Constants.TWO) {
            hexSeq = "0" + hexSeq;
        }
        String stopMessage = hexSeq + "06000000FA";
        String searchMsg = hexSeq + "0100000018";
        waterReading.put("deviceCode", dataDTO.getDeviceCode());
        waterReading.put("valveSeq", dataDTO.getValveSeq());
        try {
            byte[] stopMessageBytes = ConvertCodeUtils.hexString2Bytes(stopMessage);
            String stopMessageCrc = ConvertCodeUtils.getCRC(stopMessageBytes);
            byte[] searchMsgBytes = ConvertCodeUtils.hexString2Bytes(searchMsg);
            String searchMsgCrc = ConvertCodeUtils.getCRC(searchMsgBytes);
            String clientId = dataDTO.getDeviceIp() + ":" + dataDTO.getDevicePort();

            //先发送停止指令
            log.info("外置阀门控制停止帧指令：{}", stopMessage + stopMessageCrc);
            String stopRespFrame = NettyTcpClientUtils.sendSyncMessage(clientId, stopMessage + stopMessageCrc);
            log.info("外置阀门控制停止帧返回：{}", stopRespFrame);

            String respFrame = null;
            log.info("外置阀门查询操作帧指令：{}", searchMsg + searchMsgCrc);
            String searchRespFrame = NettyTcpClientUtils.sendSyncMessage(clientId, searchMsg + searchMsgCrc);
            log.info("外置阀门查询响应帧返回：{}", searchRespFrame);


            if (Objects.isNull(stopRespFrame) || Objects.isNull(respFrame)) {
                waterReading.put("valveState", dataDTO.getValveOnOff() == 0 ? 1 : 0);
                return Boolean.TRUE;
            } else {
                waterReading.put("valveState", dataDTO.getValveOnOff());
            }
        } catch (Exception e) {
            log.error("外置阀门控制失败", e);
            waterReading.put("valveState", dataDTO.getValveOnOff() == 0 ? 1 : 0);
        }
        //kafka(EventEnum.WATER_REPEATER_OUT_VALVE_STATE.getKey(), JSONUtil.toJsonStr(waterReading));
        return Boolean.FALSE;
    }


    /**
     * 推送kafka消息
     *
     * @param key
     * @param data
     * @return
     */
    private void kafka(String key, String data) {
        kafkaProducer.sendMessage("BRIDGE_EVENT_TOPIC", key, data);
    }
}
