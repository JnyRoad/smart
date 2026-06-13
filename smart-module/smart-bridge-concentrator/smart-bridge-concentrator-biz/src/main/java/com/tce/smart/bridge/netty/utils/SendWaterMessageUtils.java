package com.tce.smart.bridge.netty.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.bridge.core.dto.MeterFileDTO;
import com.tce.smart.bridge.core.enums.MeterStatusEnum;
import com.tce.smart.bridge.core.enums.ValveStatusEnum;
import com.tce.smart.bridge.netty.constant.Constants;
import com.tce.smart.bridge.netty.constant.DA;
import com.tce.smart.bridge.netty.constant.DT;
import com.tce.smart.bridge.netty.dto.ConcentratorEventDTO;
import com.tce.smart.bridge.netty.dto.WaterReadingDTO;
import com.tce.smart.bridge.netty.enums.ControlCodeEnum;
import com.tce.smart.bridge.netty.enums.FunctionCodeEnum;
import com.tce.smart.bridge.netty.enums.WaterControlCodeEnum;
import com.tce.smart.bridge.netty.enums.WaterFunctionCodeEnum;
import com.tce.smart.bridge.netty.message.AddressMessage;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author Li.JiaJun
 * @since 2022/4/8 9:06
 */
@Slf4j
@UtilityClass
public class SendWaterMessageUtils {

    //---------------------------- 水表 -------------------------------------

    /**
     * 读取水表读数
     *
     * @param clientId 水表集中器 IP
     * @param seq      水表序号
     * @return
     */
    public static WaterReadingDTO waterReading(String clientId, Integer seq) {
        WaterReadingDTO waterReadingDTO = new WaterReadingDTO();
        ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
        eventDTO.setClientId(clientId);
        // 计算时间标签Tp
        String tp = SendMessageUtils.getTp();
        // 拼接请求帧
        eventDTO.setMessageFrame(getWaterReadingReqFrame(clientId, seq, tp));
        eventDTO.setTp(tp);
        log.info("水表实时读数请求帧：{}，水表序号：{}", eventDTO.getMessageFrame(), seq);
        // 发送消息，获取响应帧
        String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
        if (Objects.isNull(respFrame)) {
            log.error("水表实时读数响应帧为空");
            return null;
        }
        log.info("水表实时读数响应帧：{}", respFrame);
        String functionCode = respFrame.substring(24, 26);
        if (FunctionCodeEnum.DOWNLOAD_FILE_RESPONSE.getCode().equals(functionCode)) {
            log.error("水表档案错误，请确认档案信息");
            return null;
        }
        waterReadingDTO.setCollectTime(DateUtil.now());
        // 解析请求帧
        // 正常响应
        waterReadingDTO.setIsOnline(MeterStatusEnum.ONLINE.getCode());
        String readingBitHex = respFrame.substring(42, 44);
        String readingThousandHex = respFrame.substring(40, 42);
        String readingTenHex = respFrame.substring(38, 40);
        String readingDecimalHex = respFrame.substring(36, 38);
        String meterFlat = respFrame.substring(44, 48);
        int meterFlatTen = ConvertCodeUtils.scale2Decimal(meterFlat, 16);
        StringBuilder binaryString = new StringBuilder(Integer.toBinaryString(meterFlatTen));
        while (binaryString.length() < Constants.EIGHT * Constants.TWO) {
            binaryString.insert(0, "0");
        }
        String binaryFlat = binaryString.substring(0, 2);
        waterReadingDTO.setCurrentReading(readingBitHex + readingThousandHex + readingTenHex + "." + readingDecimalHex);
        waterReadingDTO.setIsOpen("00".equals(binaryFlat) ? ValveStatusEnum.OPEN.getCode() : ValveStatusEnum.CLOSE.getCode());
        return waterReadingDTO;
    }

    /**
     * 水表请求帧拼接
     *
     * @param clientId
     * @param seq
     * @return
     */
    private static String getWaterReadingReqFrame(String clientId, Integer seq, String tp) {
        AddressMessage addressMessage = NettyServerUtils.getMessage(clientId);
        // 长度
        String hexString = ControlCodeEnum.READING_QUERY_REQUEST.getCode()
                + addressMessage.getCountyCode() + addressMessage.getAddress() + addressMessage.getStandbyAddress()
                + WaterFunctionCodeEnum.QUERY_REQUEST_RESPONSE.getCode()
                + SeqUtils.getWaterReadingReqSeq() + ConvertCodeUtils.getDA(seq) + DT.F12
                + tp;
        String length = ConvertCodeUtils.getLength(hexString.length() / 2);
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(hexString);
        return Constants.HEAD_FRAME + length + length + Constants.HEAD_FRAME + hexString + checkSum + Constants.END;
    }

    /**
     * 读取水表日冻结读数
     *
     * @param clientId
     * @param seq
     * @return
     */
    public static WaterReadingDTO waterDayReading(String clientId, Integer seq) {
        WaterReadingDTO waterReadingDTO = new WaterReadingDTO();
        ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
        eventDTO.setClientId(clientId);
        // 计算时间标签Tp
        String tp = SendMessageUtils.getTp();
        // 拼接请求帧
        eventDTO.setMessageFrame(getWaterDayReadingReqFrame(clientId, seq, tp));
        eventDTO.setTp(tp);
        log.info("水表日冻结读数请求帧：{}，水表序号：{}", eventDTO.getMessageFrame(), seq);
        // 发送消息，获取响应帧
        String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
        if (Objects.isNull(respFrame)) {
            log.error("水表日冻结读数响应帧为空");
            return null;
        }
        log.info("水表日冻结读数响应帧：{}", respFrame);
        String functionCode = respFrame.substring(24, 26);
        if (FunctionCodeEnum.DOWNLOAD_FILE_RESPONSE.getCode().equals(functionCode)) {
            log.error("水表日冻结读数档案错误，请确认档案信息");
            return null;
        }
        waterReadingDTO.setCollectTime(DateUtil.now());
        // 解析请求帧
        // 正常响应
        waterReadingDTO.setIsOnline(MeterStatusEnum.ONLINE.getCode());
        String readingHex = respFrame.substring(respFrame.length() - 28, respFrame.length() - 16);
        String readingBitHex = readingHex.substring(6, 8);
        String readingThousandHex = readingHex.substring(4, 6);
        String readingTenHex = readingHex.substring(2, 4);
        String readingDecimalHex = readingHex.substring(0, 2);
        String currentReading = readingBitHex + readingThousandHex + readingTenHex + "." + readingDecimalHex;
        if ("EEEEEE.EE".equals(currentReading)) {
            log.error("水表日冻结读数：{}，日冻结水表读取失败", currentReading);
            return null;
        } else {
            log.info("水表日冻结读数：{}", currentReading);
        }
        String meterFlat = readingHex.substring(8);
        int meterFlatTen = ConvertCodeUtils.scale2Decimal(meterFlat, 16);
        StringBuilder binaryString = new StringBuilder(Integer.toBinaryString(meterFlatTen));
        while (binaryString.length() < Constants.EIGHT * Constants.TWO) {
            binaryString.insert(0, "0");
        }
        String binaryFlat = binaryString.substring(0, 2);
        waterReadingDTO.setCurrentReading(currentReading);
        waterReadingDTO.setIsOpen("00".equals(binaryFlat) ? ValveStatusEnum.OPEN.getCode() : ValveStatusEnum.CLOSE.getCode());
        return waterReadingDTO;
    }

    /**
     * 获取水表日冻结读数请求帧
     *
     * @param clientId
     * @param seq
     * @param tp
     * @return
     */
    private static String getWaterDayReadingReqFrame(String clientId, Integer seq, String tp) {
        AddressMessage addressMessage = NettyServerUtils.getMessage(clientId);
        // 长度
        String hexString = "4B" + addressMessage.getCountyCode() + addressMessage.getAddress()
                + addressMessage.getStandbyAddress() + "8D" + SeqUtils.getWaterHistoryReadingReqSeq()
                + ConvertCodeUtils.getDA(seq) + DT.F219 + SendMessageUtils.getDayMonthYear() + tp;
        String length = ConvertCodeUtils.getLength(hexString.length() / 2);
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(hexString);
        return Constants.HEAD_FRAME + length + length + Constants.HEAD_FRAME + hexString + checkSum + Constants.END;
    }

    /**
     * 水表集中器内置阀门控制
     *
     * @param clientId 集中器ID
     * @param seq      水表序号
     * @return
     */
    public static Boolean changeValveStatus(String clientId, Integer seq, Integer isOpen) {
        ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
        eventDTO.setClientId(clientId);
        // 计算时间标签Tp
        String tp = SendMessageUtils.getTp();
        // 拼接请求帧
        eventDTO.setMessageFrame(getValveReqFrame(clientId, seq, isOpen, tp));
        eventDTO.setTp(tp);
        log.info("水表集中器内置阀门控制请求帧：{}，水表序号：{}", eventDTO.getMessageFrame(), seq);
        // 发送消息，获取响应帧
        String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
        if (Objects.isNull(respFrame)) {
            log.error("水表集中器内置阀门控制响应帧为空");
            return Boolean.FALSE;
        }
        log.info("水表集中器内置阀门控制响应帧：{}", respFrame);
        String respCode = respFrame.substring(32, 34);
        String right = "01";
        String error = "02";
        if (right.equals(respCode)) {
            return Boolean.TRUE;
        } else if (error.equals(respCode)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 水表集中器内置阀门请求帧拼接
     *
     * @param clientId
     * @param seq
     * @param isOpen
     * @return
     */
    private static String getValveReqFrame(String clientId, Integer seq, Integer isOpen, String tp) {
        AddressMessage addressMessage = NettyServerUtils.getMessage(clientId);
        // 长度
        String hexString = WaterControlCodeEnum.VALVE_REQUEST.getCode()
                + addressMessage.getCountyCode() + addressMessage.getAddress() + addressMessage.getStandbyAddress()
                + WaterFunctionCodeEnum.VALVE_REQUEST.getCode()
                + SeqUtils.getWaterReadingReqSeq() + ConvertCodeUtils.getDA(seq) + DT.F233;
        if (ValveStatusEnum.OPEN.getCode().equals(isOpen)) {
            hexString += Constants.WATER_VALVE_OPEN;
        } else if (ValveStatusEnum.CLOSE.getCode().equals(isOpen)) {
            hexString += Constants.WATER_VALVE_CLOSE;
        }
        hexString += Constants.PW + tp;
        String length = ConvertCodeUtils.getLength(hexString.length() / 2);
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(hexString);
        return Constants.HEAD_FRAME + length + length + Constants.HEAD_FRAME + hexString + checkSum + Constants.END;
    }

    /**
     * 水表集中器下载档案
     *
     * @param dataDTO
     * @return
     */
    public static Boolean waterIssueFile(MeterFileDTO dataDTO) {
        String meterJson = dataDTO.getMeterJson();
        JSONArray jsonArray = JSONUtil.parseArray(meterJson);
        if (CollUtil.isNotEmpty(jsonArray)) {
            // 计算时间标签Tp
            String tp = SendMessageUtils.getTp();
            ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
            eventDTO.setClientId(dataDTO.getDeviceIp());
            // 拼接下载档案请求帧
            eventDTO.setMessageFrame(getWaterFileReqFrame(dataDTO.getDeviceIp(), dataDTO.getConcentratorAddress(),
                    jsonArray, tp, false));
            eventDTO.setTp(tp);
            log.info("水表集中器IP：{}，水表集中器下载档案请求帧：{}", dataDTO.getDeviceIp(), eventDTO.getMessageFrame());
            // 发送下载档案消息，获取响应帧
            String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
            if (Objects.isNull(respFrame)) {
                log.error("水表集中器下载档案响应帧为空");
                return Boolean.FALSE;
            }
            log.info("水表集中器下载档案响应帧：{}", respFrame);
            String dt = respFrame.substring(respFrame.length() - 20, respFrame.length() - 16);
            if (DT.F1.equals(dt)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * 水表集中器删除档案
     *
     * @param dataDTO
     * @return
     */
    public static Boolean waterDelFile(MeterFileDTO dataDTO) {
        String meterJson = dataDTO.getMeterJson();
        JSONArray jsonArray = JSONUtil.parseArray(meterJson);
        if (CollUtil.isNotEmpty(jsonArray)) {
            // 计算时间标签Tp
            String tp = SendMessageUtils.getTp();
            ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
            eventDTO.setClientId(dataDTO.getDeviceIp());
            // 拼接删除档案请求帧
            eventDTO.setMessageFrame(getWaterFileReqFrame(dataDTO.getDeviceIp(), dataDTO.getConcentratorAddress(),
                    jsonArray, tp, true));
            eventDTO.setTp(tp);
            log.info("水表集中器IP：{}，水表集中器删除档案请求帧：{}", dataDTO.getDeviceIp(), eventDTO.getMessageFrame());
            // 发送删除档案消息，获取响应帧
            String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
            if (Objects.isNull(respFrame)) {
                log.error("水表集中器删除档案响应帧为空");
                return Boolean.FALSE;
            }
            log.info("水表集中器删除档案响应帧：{}", respFrame);
            String dt = respFrame.substring(respFrame.length() - 20, respFrame.length() - 16);
            if (DT.F1.equals(dt)) {
                return Boolean.TRUE;
            }
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 水表集中器删除|下载档案请求帧拼接
     *
     * @param clientId
     * @param concentratorAddress
     * @param jsonArray
     * @param tp
     * @param isRemove
     * @return
     */
    private static String getWaterFileReqFrame(String clientId, String concentratorAddress, JSONArray jsonArray,
                                               String tp, Boolean isRemove) {
        AddressMessage addressMessage = NettyServerUtils.getMessage(clientId);
        int meterNum = jsonArray.size();
        StringBuilder issueFileDataUnit = new StringBuilder();
        for (Object o : jsonArray) {
            JSONObject jsonObject = JSONUtil.parseObj(o);
            Integer seq = jsonObject.getInt("seq");
            String address = jsonObject.getStr("address");
            Integer port = jsonObject.getInt("port");
            String largeClass = jsonObject.getStr("largeClass");
            String rateAndPort = SendMessageUtils.getRateAndPort(port);
            issueFileDataUnit.append(SendMessageUtils.transHex(seq))
                    .append(isRemove ? "0000" : SendMessageUtils.transHex(seq))
                    // 通信速率及端口号
                    .append(rateAndPort)
                    // 通信协议类型
                    .append("09")
                    // 通信地址
                    .append(SendMessageUtils.reverseAddress(address, 16))
                    // 备用
                    .append("00000000")
                    // 通信密码
                    .append("000000000000")
                    // 阶梯费率号
                    .append("00")
                    // 保留
                    .append("00")
                    // 所属采集器通信地址
                    .append(SendMessageUtils.reverseAddress(concentratorAddress, 12))
                    // 用户大类号
                    .append(largeClass)
                    // 用户小类号
                    .append("0");
        }
        // 长度
        String hexString = WaterControlCodeEnum.DOWNLOAD_FILE_REQUEST.getCode()
                + addressMessage.getCountyCode() + addressMessage.getAddress() + addressMessage.getStandbyAddress()
                + WaterFunctionCodeEnum.DOWNLOAD_FILE_REQUEST.getCode()
                + SeqUtils.getDownloadFileSeq() + DA.P0 + DT.F10
                + SendMessageUtils.transHex(meterNum) + issueFileDataUnit
                + Constants.PW + tp;
        String length = ConvertCodeUtils.getLength(hexString.length() / 2);
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(hexString);
        return Constants.HEAD_FRAME + length + length + Constants.HEAD_FRAME + hexString + checkSum + Constants.END;
    }

    /**
     * 水表集中器查询档案
     *
     * @param dataDTO
     * @return
     */
    public static Boolean waterQueryFile(MeterFileDTO dataDTO) {
        String meterJson = dataDTO.getMeterJson();
        JSONArray jsonArray = JSONUtil.parseArray(meterJson);
        if (CollUtil.isNotEmpty(jsonArray)) {
            // 计算时间标签Tp
            String tp = SendMessageUtils.getTp();
            ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
            eventDTO.setClientId(dataDTO.getDeviceIp());
            // 拼接请求帧
            eventDTO.setMessageFrame(getWaterQueryFileReqFrame(dataDTO.getDeviceIp(), jsonArray, tp));
            eventDTO.setTp(tp);
            log.info("水表集中器IP：{}，水表集中器查询档案请求帧：{}", dataDTO.getDeviceIp(), eventDTO.getMessageFrame());
            // 发送消息，获取响应帧
            String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
            if (Objects.isNull(respFrame)) {
                log.error("水表集中器查询档案响应帧为空");
                return Boolean.FALSE;
            }
            log.info("水表集中器查询档案响应帧：{}", respFrame);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 获取水表集中器查询档案请求帧
     *
     * @param clientId
     * @param jsonArray
     * @return
     */
    private static String getWaterQueryFileReqFrame(String clientId, JSONArray jsonArray, String tp) {
        AddressMessage addressMessage = NettyServerUtils.getMessage(clientId);
        StringBuilder queryFileDataUnit = SendMessageUtils.getQueryFileDataUnit(jsonArray);
        // 长度
        String hexString = "4B" + addressMessage.getCountyCode() + addressMessage.getAddress()
                + addressMessage.getStandbyAddress() + "8A" + SeqUtils.getDownloadFileSeq() + DA.P0 + DT.F10
                + SendMessageUtils.transHex(jsonArray.size()) + queryFileDataUnit + tp;
        String length = ConvertCodeUtils.getLength(hexString.length() / 2);
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(hexString);
        return Constants.HEAD_FRAME + length + length + Constants.HEAD_FRAME + hexString + checkSum + Constants.END;
    }
}
