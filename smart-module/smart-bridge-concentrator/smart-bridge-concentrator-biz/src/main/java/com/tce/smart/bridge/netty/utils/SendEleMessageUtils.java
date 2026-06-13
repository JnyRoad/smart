package com.tce.smart.bridge.netty.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.bridge.core.dto.MeterFileDTO;
import com.tce.smart.bridge.core.enums.MeterStatusEnum;
import com.tce.smart.bridge.core.enums.ValveStatusEnum;
import com.tce.smart.bridge.core.exception.SmartException;
import com.tce.smart.bridge.netty.constant.Constants;
import com.tce.smart.bridge.netty.constant.DA;
import com.tce.smart.bridge.netty.constant.DT;
import com.tce.smart.bridge.netty.dto.ConcentratorEventDTO;
import com.tce.smart.bridge.netty.dto.EleReadingDTO;
import com.tce.smart.bridge.netty.enums.ControlCodeEnum;
import com.tce.smart.bridge.netty.enums.EleControlCodeEnum;
import com.tce.smart.bridge.netty.enums.EleFunctionCodeEnum;
import com.tce.smart.bridge.netty.enums.FunctionCodeEnum;
import com.tce.smart.bridge.netty.message.AddressMessage;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author Li.JiaJun
 * @since 2022/4/8 9:07
 */
@Slf4j
@UtilityClass
public class SendEleMessageUtils {

    //---------------------------- 电表 -------------------------------------

    /**
     * 读取电表读数
     *
     * @param clientId
     * @param seq
     * @return
     */
    public EleReadingDTO eleReading(String clientId, Integer seq) {
        EleReadingDTO eleReadingDTO = new EleReadingDTO();
        ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
        eventDTO.setClientId(clientId);
        // 计算时间标签Tp
        String tp = SendMessageUtils.getTp();
        // 拼接请求帧
        eventDTO.setMessageFrame(getEleReadingReqFrame(clientId, seq, tp));
        eventDTO.setTp(tp);
        log.info("电表实时读数请求帧：{}，电表序号：{}", eventDTO.getMessageFrame(), seq);
        // 发送消息，获取响应帧
        String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
        if (Objects.isNull(respFrame)) {
            log.error("电表实时读数响应帧为空");
            return null;
        }
        log.info("电表实时读数响应帧：{}", respFrame);
        String functionCode = respFrame.substring(24, 26);
        if (FunctionCodeEnum.DOWNLOAD_FILE_RESPONSE.getCode().equals(functionCode)) {
            log.error("电表档案错误，请确认档案信息");
            return null;
        }
        eleReadingDTO.setCollectTime(DateUtil.now());
        // 正常响应
        // 解析请求帧
        eleReadingDTO.setIsOnline(MeterStatusEnum.ONLINE.getCode());
        String decimalThousandHex = respFrame.substring(48, 50);
        String decimalTenHex = respFrame.substring(50, 52);
        String tenHex = respFrame.substring(52, 54);
        String thousandHex = respFrame.substring(54, 56);
        String tenThousandHex = respFrame.substring(56, 58);
        eleReadingDTO.setCurrentReading(tenThousandHex + thousandHex + tenHex + "." + decimalTenHex + decimalThousandHex);
        return eleReadingDTO;
    }

    /**
     * 读取电表读数请求帧拼接
     *
     * @param clientId
     * @param seq
     * @param tp
     * @return
     */
    private static String getEleReadingReqFrame(String clientId, Integer seq, String tp) {
        AddressMessage addressMessage = NettyServerUtils.getMessage(clientId);
        // 长度
        String hexString = ControlCodeEnum.READING_QUERY_REQUEST.getCode()
                + addressMessage.getCountyCode() + addressMessage.getAddress() + addressMessage.getStandbyAddress()
                + EleFunctionCodeEnum.QUERY_REQUEST_RESPONSE.getCode()
                + SeqUtils.getEleReadingReqSeq() + ConvertCodeUtils.getDA(seq) + DT.F129
                + tp;
        String length = ConvertCodeUtils.getLength(hexString.length() / 2);
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(hexString);
        return Constants.HEAD_FRAME + length + length + Constants.HEAD_FRAME + hexString + checkSum + Constants.END;
    }

    /**
     * 读取日冻结电表读数
     *
     * @param clientId
     * @param seq
     * @return
     */
    public EleReadingDTO eleDayReading(String clientId, Integer seq) {
        EleReadingDTO eleReadingDTO = new EleReadingDTO();
        ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
        eventDTO.setClientId(clientId);
        // 计算时间标签Tp
        String tp = SendMessageUtils.getTp();
        // 拼接请求帧
        eventDTO.setMessageFrame(getEleDayReadingReqFrame(clientId, seq, tp));
        eventDTO.setTp(tp);
        log.info("日冻结电表请求帧：{}，电表序号：{}", eventDTO.getMessageFrame(), seq);
        // 发送消息，获取响应帧
        String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
        if (Objects.isNull(respFrame)) {
            log.error("日冻结电表响应帧为空");
            return null;
        }
        log.info("日冻结电表响应帧：{}", respFrame);
        String functionCode = respFrame.substring(24, 26);
        if (FunctionCodeEnum.DOWNLOAD_FILE_RESPONSE.getCode().equals(functionCode)) {
            log.error("日冻结电表档案错误，请确认档案信息");
            return null;
        }
        eleReadingDTO.setCollectTime(DateUtil.now());
        // 正常响应
        // 解析请求帧
        eleReadingDTO.setIsOnline(MeterStatusEnum.ONLINE.getCode());
        String readingHex = respFrame.substring(54, 64);
        String decimalThousandHex = readingHex.substring(0, 2);
        String decimalTenHex = readingHex.substring(2, 4);
        String tenHex = readingHex.substring(4, 6);
        String thousandHex = readingHex.substring(6, 8);
        String tenThousandHex = readingHex.substring(8);
        String currentReading = tenThousandHex + thousandHex + tenHex + "." + decimalTenHex + decimalThousandHex;
        if ("EEEEEE.EEEE".equals(currentReading)) {
            log.error("日冻结电表读数：{}，日冻结电表读取失败", currentReading);
            return null;
        } else {
            log.info("日冻结电表读数：{}", currentReading);
        }
        eleReadingDTO.setCurrentReading(currentReading);
        return eleReadingDTO;
    }

    /**
     * 读取日冻结电表读数请求帧拼接
     *
     * @param clientId
     * @param seq
     * @param tp
     * @return
     */
    private static String getEleDayReadingReqFrame(String clientId, Integer seq, String tp) {
        AddressMessage addressMessage = NettyServerUtils.getMessage(clientId);
        // 长度
        String hexString = ControlCodeEnum.READING_QUERY_REQUEST.getCode()
                + addressMessage.getCountyCode() + addressMessage.getAddress() + addressMessage.getStandbyAddress()
                + EleFunctionCodeEnum.QUERY_DAY_REQUEST_RESPONSE.getCode()
                + SeqUtils.getEleReadingReqSeq() + ConvertCodeUtils.getDA(seq) + DT.F161
                + SendMessageUtils.getDayMonthYear() + tp;
        String length = ConvertCodeUtils.getLength(hexString.length() / 2);
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(hexString);
        return Constants.HEAD_FRAME + length + length + Constants.HEAD_FRAME + hexString + checkSum + Constants.END;
    }

    /**
     * 读取小时冻结电表读数
     *
     * @param clientId
     * @param seq
     * @return
     */
    public EleReadingDTO eleHourReading(String clientId, Integer seq) {
        EleReadingDTO eleReadingDTO = new EleReadingDTO();
        ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
        eventDTO.setClientId(clientId);
        // 计算时间标签Tp
        String tp = SendMessageUtils.getTp();
        // 拼接请求帧
        eventDTO.setMessageFrame(getEleHourReadingReqFrame(clientId, seq, tp));
        eventDTO.setTp(tp);
        log.info("小时冻结电表请求帧：{}，电表序号：{}", eventDTO.getMessageFrame(), seq);
        // 发送消息，获取响应帧
        String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
        if (Objects.isNull(respFrame)) {
            log.error("小时冻结电表响应帧为空");
            return null;
        }
        log.info("小时冻结电表响应帧：{}", respFrame);
        String functionCode = respFrame.substring(24, 26);
        if (FunctionCodeEnum.DOWNLOAD_FILE_RESPONSE.getCode().equals(functionCode)) {
            log.error("小时冻结电表档案错误，请确认档案信息");
            return null;
        }
        eleReadingDTO.setCollectTime(DateUtil.now());
        // 解析请求帧
        eleReadingDTO.setIsOnline(MeterStatusEnum.ONLINE.getCode());
        String readingHex = respFrame.substring(36, 58);
        String hourScale = readingHex.substring(0, 14);
        String decimalTenHex = readingHex.substring(14, 16);
        String tenHex = readingHex.substring(16, 18);
        String thousandHex = readingHex.substring(18, 20);
        String tenThousandHex = readingHex.substring(20);
        String currentReading = tenThousandHex + thousandHex + tenHex + "." + decimalTenHex;
        if ("EEEEEE.EE".equals(currentReading)) {
            log.error("小时冻结电表读数：{}，小时冻结电表读取失败", currentReading);
            return null;
        } else {
            log.info("小时冻结电表时标：{}，读数：{}", hourScale, currentReading);
        }
        eleReadingDTO.setCurrentReading(currentReading);
        return eleReadingDTO;
    }

    /**
     * 读取小时冻结电表读数请求帧拼接
     *
     * @param clientId
     * @param seq
     * @param tp
     * @return
     */
    private static String getEleHourReadingReqFrame(String clientId, Integer seq, String tp) {
        AddressMessage addressMessage = NettyServerUtils.getMessage(clientId);
        // 长度
        String hexString = ControlCodeEnum.READING_QUERY_REQUEST.getCode()
                + addressMessage.getCountyCode() + addressMessage.getAddress() + addressMessage.getStandbyAddress()
                + "0D" + SeqUtils.getEleReadingReqSeq() + ConvertCodeUtils.getDA(seq) + DT.F101
                + SendMessageUtils.getMinHourDayMonthYear() + "0301" + tp;
        String length = ConvertCodeUtils.getLength(hexString.length() / 2);
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(hexString);
        return Constants.HEAD_FRAME + length + length + Constants.HEAD_FRAME + hexString + checkSum + Constants.END;
    }

    /**
     * 电表集中器下载档案
     *
     * @param dataDTO
     * @return
     */
    public static Boolean eleIssueFile(MeterFileDTO dataDTO) {
        String meterJson = dataDTO.getMeterJson();
        JSONArray jsonArray = JSONUtil.parseArray(meterJson);
        if (CollUtil.isNotEmpty(jsonArray)) {
            // 计算时间标签Tp
            String tp = SendMessageUtils.getTp();
            ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
            eventDTO.setClientId(dataDTO.getDeviceIp());
            // 拼接下载档案请求帧
            eventDTO.setMessageFrame(getEleFileReqFrame(dataDTO.getDeviceIp(), dataDTO.getConcentratorAddress(),
                    jsonArray, tp, false));
            eventDTO.setTp(tp);
            log.info("电表集中器IP：{}，电表集中器下载档案请求帧：{}", dataDTO.getDeviceIp(), eventDTO.getMessageFrame());
            // 发送下载档案消息，获取响应帧
            String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
            if (Objects.isNull(respFrame)) {
                log.error("电表集中器下载档案响应帧为空");
                return Boolean.FALSE;
            }
            log.info("电表集中器下载档案响应帧：{}", respFrame);
            String dt = respFrame.substring(respFrame.length() - 20, respFrame.length() - 16);
            if (DT.F1.equals(dt)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * 电表集中器删除档案
     *
     * @param dataDTO
     * @return
     */
    public static Boolean eleDelFile(MeterFileDTO dataDTO) {
        String meterJson = dataDTO.getMeterJson();
        JSONArray jsonArray = JSONUtil.parseArray(meterJson);
        if (CollUtil.isNotEmpty(jsonArray)) {
            // 计算时间标签Tp
            String tp = SendMessageUtils.getTp();
            ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
            eventDTO.setClientId(dataDTO.getDeviceIp());
            // 拼接删除档案请求帧
            eventDTO.setMessageFrame(getEleFileReqFrame(dataDTO.getDeviceIp(), dataDTO.getConcentratorAddress(),
                    jsonArray, tp, true));
            eventDTO.setTp(tp);
            log.info("电表集中器IP：{}，电表集中器删除档案请求帧：{}", dataDTO.getDeviceIp(), eventDTO.getMessageFrame());
            // 发送删除档案消息，获取响应帧
            String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
            if (Objects.isNull(respFrame)) {
                log.error("电表集中器删除档案响应帧为空");
                return Boolean.FALSE;
            }
            log.info("电表集中器删除档案响应帧：{}", respFrame);
            String dt = respFrame.substring(respFrame.length() - 20, respFrame.length() - 16);
            if (DT.F1.equals(dt)) {
                return Boolean.TRUE;
            }
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 获取电表集中器下载|删除档案请求帧
     *
     * @param clientId
     * @param jsonArray
     * @return
     */
    private static String getEleFileReqFrame(String clientId, String concentratorAddress, JSONArray jsonArray,
                                             String tp, Boolean isRemove) {
        AddressMessage addressMessage = NettyServerUtils.getMessage(clientId);
        int meterNum = jsonArray.size();
        StringBuilder issueFileDataUnit = new StringBuilder();
        for (Object o : jsonArray) {
            JSONObject jsonObject = JSONUtil.parseObj(o);
            Integer seq = jsonObject.getInt("seq");
            String address = jsonObject.getStr("address");
            Integer port = jsonObject.getInt("port") + 1;
            String rateAndPort = SendMessageUtils.getRateAndPort(port);
            issueFileDataUnit.append(SendMessageUtils.transHex(seq))
                    // 所属测量点号，如为0表示删除
                    .append(isRemove ? "0000" : SendMessageUtils.transHex(seq))
                    // 通信速率(默认2400)及端口号
                    .append(rateAndPort)
                    // 通行协议类型：默认30：DL/T 645—2007
                    .append("1E")
                    // 通信地址
                    .append(SendMessageUtils.reverseAddress(address, 12))
                    // 通信密码 默认 0
                    .append("000000000000")
                    // 电能费率个数 默认 4
                    .append("04")
                    // 有功电能示值的整数位及小数位个数，默认6 2
                    .append("09")
                    // 所属采集器通信地址
                    .append(SendMessageUtils.reverseAddress(concentratorAddress, 12))
                    // 默认用户大类号6，用户小类号2
                    .append("62");
        }
        // 长度
        String hexString = EleControlCodeEnum.DOWNLOAD_FILE_REQUEST.getCode()
                + addressMessage.getCountyCode() + addressMessage.getAddress() + addressMessage.getStandbyAddress()
                + EleFunctionCodeEnum.DOWNLOAD_FILE_REQUEST.getCode()
                + SeqUtils.getDownloadFileSeq() + DA.P0 + DT.F10
                + SendMessageUtils.transHex(meterNum) + issueFileDataUnit
                + Constants.PW + tp;
        String length = ConvertCodeUtils.getLength(hexString.length() / 2);
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(hexString);
        return Constants.HEAD_FRAME + length + length + Constants.HEAD_FRAME + hexString + checkSum + Constants.END;
    }

    /**
     * 电表集中器查询档案
     *
     * @param dataDTO
     * @return
     */
    public static Boolean eleQueryFile(MeterFileDTO dataDTO) {
        String meterJson = dataDTO.getMeterJson();
        JSONArray jsonArray = JSONUtil.parseArray(meterJson);
        if (CollUtil.isNotEmpty(jsonArray)) {
            // 计算时间标签Tp
            String tp = SendMessageUtils.getTp();
            ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
            eventDTO.setClientId(dataDTO.getDeviceIp());
            // 拼接请求帧
            eventDTO.setMessageFrame(getEleQueryFileReqFrame(dataDTO.getDeviceIp(), jsonArray, tp));
            eventDTO.setTp(tp);
            log.info("电表集中器IP：{}，电表集中器查询档案请求帧：{}", dataDTO.getDeviceIp(), eventDTO.getMessageFrame());
            // 发送消息，获取响应帧
            String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
            if (Objects.isNull(respFrame)) {
                log.error("电表集中器查询档案响应帧为空");
                return Boolean.FALSE;
            }
            log.info("电表集中器查询档案响应帧：{}", respFrame);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 获取电表集中器查询档案请求帧
     *
     * @param clientId
     * @param jsonArray
     * @return
     */
    private static String getEleQueryFileReqFrame(String clientId, JSONArray jsonArray, String tp) {
        AddressMessage addressMessage = NettyServerUtils.getMessage(clientId);
        StringBuilder queryFileDataUnit = SendMessageUtils.getQueryFileDataUnit(jsonArray);
        // 长度
        String hexString = "4B" + addressMessage.getCountyCode() + addressMessage.getAddress()
                + addressMessage.getStandbyAddress() + "0A" + SeqUtils.getDownloadFileSeq() + DA.P0 + DT.F10
                + SendMessageUtils.transHex(jsonArray.size()) + queryFileDataUnit + tp;
        String length = ConvertCodeUtils.getLength(hexString.length() / 2);
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(hexString);
        return Constants.HEAD_FRAME + length + length + Constants.HEAD_FRAME + hexString + checkSum + Constants.END;
    }

    /**
     * 电表集中器闸门控制
     *
     * @param clientId 集中器ID
     * @param address  闸表序号
     * @return
     */
    public static Boolean changeBrakeStatus(String clientId, String address, Integer port, Integer isOpen) {
        ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
        eventDTO.setClientId(clientId);
        // 计算时间标签Tp
        String tp = SendMessageUtils.getTp();
        // 拼接请求帧
        eventDTO.setMessageFrame(getBrakeReqFrame(clientId, address, port, isOpen, tp));
        eventDTO.setTp(tp);
        log.info("电表集中器闸门控制请求帧：{}，电表地址：{}", eventDTO.getMessageFrame(), address);
        // 发送消息，获取响应帧
        String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
        if (Objects.isNull(respFrame)) {
            log.error("电表集中器闸门控制响应帧为空");
            return Boolean.FALSE;
        }
        log.info("电表集中器闸门控制响应帧：{}", respFrame);
        String respCode = respFrame.substring(58, 60);
        String right = "9C";
        String error = "DC";
        if (right.equals(respCode)) {
            return Boolean.TRUE;
        } else if (error.equals(respCode)) {
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }

    /**
     * 电表集中器闸门请求帧拼接
     *
     * @param clientId
     * @param meterAddress
     * @param isOpen
     * @return
     */
    private static String getBrakeReqFrame(String clientId, String meterAddress, Integer port, Integer isOpen, String tp) {
        AddressMessage addressMessage = NettyServerUtils.getMessage(clientId);
        String address = addressMessage.getCountyCode() + addressMessage.getAddress() + addressMessage.getStandbyAddress();
        String content = getContent(meterAddress, isOpen);
        // 长度           控制码 + 地址域
        String hexString = "41" + address
                // 功能码 + 帧序列域 + 数据单元标识
                + "10" + SeqUtils.getWaterReadingReqSeq() + DA.P0 + DT.F1
                // 数据单元【终端通信端口号(1) + 透明转发通信控制字(1) + 透明转发接收等待报文超时时间(1) + 透明转发接收等待字节超时时间(1)
                + "0" + (port + 1) + "6B" + "BC" + "01"
                // + 透明转发内容字节数k(2) + 透明转发内容(k)】
                + "1C" + "00" + content;
        hexString += Constants.PW + tp;
        String length = ConvertCodeUtils.getLength(hexString.length() / 2);
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(hexString);
        return Constants.HEAD_FRAME + length + length + Constants.HEAD_FRAME + hexString + checkSum + Constants.END;
    }

    /**
     * 闸门控制透明转发内容获取
     *
     * @param meterAddress 电表地址
     * @param isOpen       闸门控制状态
     * @return
     */
    private static String getContent(String meterAddress, Integer isOpen) {
        // 控制码 + 数据域长度
        String content = "1C" + "10";
        // 合闸
        if (ValveStatusEnum.OPEN.getCode().equals(isOpen)) {
                    // 数据域
                    // PA(02：普通表) + P0(00) + P1(00) + P2(00)
            content += "35" + "33" + "33" + "33"
                    // C0(01) + C1(00) + C2(00) + C3(00)
                    + "34" + "33" + "33" + "33"
                    // N1(1C：合闸) + N2(00)
                    + "4F" + "33"
                    // N3(ss:20) + N4(mm:14) + N5(HH:09) + N6(dd:16) + N7(MM:05) + N8(yy:22) = 22-05-16 09:14:20
                    + getExpiration();
            // 跳闸
        } else if (ValveStatusEnum.CLOSE.getCode().equals(isOpen)) {
                    // 数据域
                    // PA(02：普通表) + P0(00) + P1(00) + P2(00)
            content += "35" + "33" + "33" + "33"
                    // C0(01) + C1(00) + C2(00) + C3(00)
                    + "34" + "33" + "33" + "33"
                    // N1(1A：跳闸) + N2(00)
                    + "4D" + "33"
                    // N3(ss:20) + N4(mm:14) + N5(HH:09) + N6(dd:16) + N7(MM:05) + N8(yy:22) = 22-05-16 09:14:20
                    + getExpiration();
        }
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(Constants.HEAD_FRAME + SendMessageUtils.reverseAddress(meterAddress, 12)
                + Constants.HEAD_FRAME + content);
        return Constants.HEAD_FRAME + SendMessageUtils.reverseAddress(meterAddress, 12) + Constants.HEAD_FRAME
                + content + checkSum + Constants.END;
    }

    /**
     * 获取命令有效截止时间：N3～N8 代
     * 表命令有效截止时间，格式为 ssmmhhDDMMYY，当电表当前时间已经超过这个时间时，该命令
     * 将被认为无效，不被执行，命令有效截止时间由主站根据信道的时延特性进行设置。
     * @return
     */
    private String getExpiration() {
        String now = DateUtil.tomorrow().toString();
        String yy = now.substring(2, 4);
        String MM = now.substring(5, 7);
        String dd = now.substring(8, 10);
        String HH = now.substring(11, 13);
        String mm = now.substring(14, 16);
        String ss = now.substring(17, 19);
        return plusHex(ss) + plusHex(mm) + plusHex(HH) + plusHex(dd) + plusHex(MM) + plusHex(yy);
    }

    /**
     * 每个16进制字符都加上33(16进制)
     * @param hex
     * @return
     */
    private String plusHex(String hex) {
        StringBuilder plusHex = new StringBuilder(Integer.toHexString(Integer.parseInt(hex, 16)
                + Integer.parseInt("33", 16)));
        while (plusHex.length() < Constants.TWO) {
            plusHex.insert(0, "0");
        }
        return plusHex.toString().toUpperCase();
    }

    /**
     * 每个16进制字符都减去33(16进制)
     * @param hex
     * @return
     */
    private String minusHex(String hex) {
        StringBuilder minusHex = new StringBuilder(Integer.toHexString(Integer.parseInt(hex, 16)
                - Integer.parseInt("33", 16)));
        while (minusHex.length() < Constants.TWO) {
            minusHex.insert(0, "0");
        }
        return minusHex.toString().toUpperCase();
    }

    /**
     * 电表闸门状态查询
     */
    public Integer getMeterStatus(String clientId, String meterAddress, Integer port) {
        ConcentratorEventDTO eventDTO = new ConcentratorEventDTO();
        eventDTO.setClientId(clientId);
        // 计算时间标签Tp
        String tp = SendMessageUtils.getTp();
        // 拼接请求帧
        eventDTO.setMessageFrame(getMeterStatusFrame(clientId, meterAddress, port, tp));
        eventDTO.setTp(tp);
        log.info("电表闸门状态查询请求帧：{}，电表地址：{}", eventDTO.getMessageFrame(), meterAddress);
        // 发送消息，获取响应帧
        String respFrame = NettyServerUtils.sendFrame(eventDTO, false);
        if (Objects.isNull(respFrame)) {
            log.error("电表闸门状态查询响应帧为空");
            throw new SmartException("电表闸门状态查询失败");
        }
        log.info("电表闸门状态查询响应帧：{}", respFrame);
        String respCode = respFrame.substring(58, 60);
        String operateCode = respFrame.substring(70, 74);
        String right = "91";
        String rightOther = "B1";
        // String error = "D1";
        if (right.equals(respCode) || rightOther.equals(respCode)) {
            String realOperateCode = minusHex(operateCode.substring(2)) + minusHex(operateCode.substring(0, 2));
            StringBuilder binaryOperateCode = new StringBuilder(Integer.toBinaryString(Integer.parseInt(realOperateCode, 16)));
            while (binaryOperateCode.length() < 16) {
                binaryOperateCode.insert(0, "0");
            }
            binaryOperateCode.reverse();
            String status = binaryOperateCode.substring(4, 5);
            if (Constants.ZERO.equals(status)) {
                return ValveStatusEnum.OPEN.getCode();
            }
        }
        return ValveStatusEnum.CLOSE.getCode();
    }

    /**
     * 电表闸门状态查询请求帧
     *
     * @param clientId 集中器IP
     * @param meterAddress 表地址
     * @param port 端口号
     * @param tp 时间戳
     */
    private String getMeterStatusFrame(String clientId, String meterAddress, Integer port, String tp) {
        AddressMessage addressMessage = NettyServerUtils.getMessage(clientId);
        String address = addressMessage.getCountyCode() + addressMessage.getAddress() + addressMessage.getStandbyAddress();
        String content = getMeterStatusContent(meterAddress);
                // 控制码 + 地址域
        String hexString = "41" + address
                // 功能码 + 帧序列域 + 数据单元标识
                + "10" + SeqUtils.getWaterReadingReqSeq() + DA.P0 + DT.F1
                // 数据单元【终端通信端口号(1) + 透明转发通信控制字(1) + 透明转发接收等待报文超时时间(1) + 透明转发接收等待字节超时时间(1)
                + "0" + (port + 1) + "6B" + "BC" + "01"
                // 透明转发内容字节数k(2) + 透明转发内容(k)】
                + "10" + "00" + content;
        hexString += Constants.PW + tp;
        String length = ConvertCodeUtils.getLength(hexString.length() / 2);
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(hexString);
        return Constants.HEAD_FRAME + length + length + Constants.HEAD_FRAME + hexString + checkSum + Constants.END;
    }

    /**
     * 电表闸门状态查询透明转发内容获取
     *
     * @param meterAddress 电表地址
     * @return
     */
    private static String getMeterStatusContent(String meterAddress) {
                // 控制码 + 数据域长度
        String content = "11" + "04"
                // 数据域
                // DI0(03 + 33) + DI1(05 + 33) + DI2(00 + 33) + DI3(04 + 33)
                + "36" + "38" + "33" + "37";
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(Constants.HEAD_FRAME + SendMessageUtils.reverseAddress(meterAddress, 12) + Constants.HEAD_FRAME + content);
        return Constants.HEAD_FRAME + SendMessageUtils.reverseAddress(meterAddress, 12) + Constants.HEAD_FRAME
                + content + checkSum + Constants.END;
    }
}
