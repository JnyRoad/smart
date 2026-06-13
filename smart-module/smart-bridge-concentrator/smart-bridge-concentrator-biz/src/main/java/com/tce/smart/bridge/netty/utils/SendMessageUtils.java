package com.tce.smart.bridge.netty.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.bridge.netty.constant.Constants;
import com.tce.smart.bridge.netty.constant.DA;
import com.tce.smart.bridge.netty.constant.DT;
import com.tce.smart.bridge.netty.enums.ControlCodeEnum;
import com.tce.smart.bridge.netty.enums.FunctionCodeEnum;
import com.tce.smart.bridge.netty.message.AddressMessage;
import lombok.experimental.UtilityClass;

/**
 * 请求报文组装工具类
 *
 * @author Li.JiaJun
 * @since 2021/12/17 9:38
 */
@UtilityClass
public class SendMessageUtils {

    /**
     * 组装水电表注册响应帧|心跳响应帧
     *
     * @return
     */
    public String getRegisterHeartBeat(AddressMessage waterEleMessage) {
        // 区县码
        String countyCode = waterEleMessage.getCountyCode();
        // 地址码
        String addressCode = waterEleMessage.getAddress();
        // 备用地址码
        String standbyAddressCode = waterEleMessage.getStandbyAddress();
        // 长度
        String hexString = ControlCodeEnum.REGISTER_HEARTBEAT_RESPONSE.getCode()
                + countyCode + addressCode + standbyAddressCode + FunctionCodeEnum.REGISTER_HEARTBEAT_RESPONSE.getCode()
                + SeqUtils.getWaterRegisterSeq() + DA.P0 + DT.F1;
        String length = ConvertCodeUtils.getLength(hexString.length() / 2);
        // 校验和
        String checkSum = ConvertCodeUtils.getCheckSum(hexString);
        return Constants.HEAD_FRAME + length + length + Constants.HEAD_FRAME + hexString + checkSum + Constants.END;
    }

    /**
     * 获取时间标签
     *
     * @return
     */
    public String getTp() {
        String now = DateUtil.now();
        // yyyy-MM-dd HH:mm:ss
        String seconds = now.substring(17, 19);
        String minutes = now.substring(14, 16);
        String hour = now.substring(11, 13);
        String day = now.substring(8, 10);
        return "00" + seconds + minutes + hour + day + "00";
    }

    /**
     * 获取日月年时间标签
     *
     * @return
     */
    public String getDayMonthYear() {
        String yesterday = DateUtil.yesterday().toDateStr();
        // yyyy-MM-dd
        String day = yesterday.substring(8);
        String month = yesterday.substring(5, 7);
        String year = yesterday.substring(2, 4);
        return day + month + year;
    }

    /**
     * 获取分时日月年时间标签
     *
     * @return
     */
    public String getMinHourDayMonthYear() {
        // yyyy-MM-dd
        String min = "00";
        int hour = DateUtil.thisHour(true);
        if (hour == 0) {
            hour = 23;
        } else {
            hour = hour - 1;
        }
        int day = DateUtil.thisDayOfMonth();
        int month = DateUtil.thisMonth() + 1;
        int year = DateUtil.thisYear();
        return min + fillZero(hour) + fillZero(day) + fillZero(month) + String.valueOf(year).substring(2);
    }

    public String fillZero(int num) {
        String value = String.valueOf(num);
        if (value.length() < Constants.TWO) {
            value = "0" + num;
        }
        return value;
    }

    /**
     * 通信速率(默认2400)及端口号
     *
     * @param port
     * @return
     */
    public String getRateAndPort(Integer port) {
        String hexPort = Integer.toHexString(port);
        if (hexPort.length() > 1) {
            return "7" + hexPort.substring(1);
        } else {
            return "6" + hexPort;
        }
    }

    /**
     * 转换16进制数量
     *
     * @param size
     * @return
     */
    public String transHex(Integer size) {
        int firstHex = size % 255;
        int nextHex = size / 255;
        return ConvertCodeUtils.hexFillZero(Integer.toHexString(firstHex))
                + ConvertCodeUtils.hexFillZero(Integer.toHexString(nextHex));
    }

    /**
     * 集中器下载档案通信地址反转
     *
     * @param hexStr
     * @return
     */
    public String reverseAddress(String hexStr, Integer length) {
        // 先补0
        StringBuilder hexStrBuilder = new StringBuilder(hexStr);
        while (hexStrBuilder.length() < length) {
            hexStrBuilder.insert(0, "0");
        }
        hexStr = hexStrBuilder.toString();
        StringBuilder reverseHex = new StringBuilder();
        // 再反转
        while (length > 0) {
            reverseHex.append(hexStr, length - 2, length);
            length -= 2;
        }
        return reverseHex.toString();
    }

    /**
     * 获取集中器查询档案序号组成
     *
     * @param jsonArray
     * @return
     */
    public StringBuilder getQueryFileDataUnit(JSONArray jsonArray) {
        StringBuilder queryFileDataUnit = new StringBuilder();
        for (Object o : jsonArray) {
            JSONObject jsonObject = JSONUtil.parseObj(o);
            Integer seq = jsonObject.getInt("seq");
            queryFileDataUnit.append(transHex(seq));
        }
        return queryFileDataUnit;
    }
}
