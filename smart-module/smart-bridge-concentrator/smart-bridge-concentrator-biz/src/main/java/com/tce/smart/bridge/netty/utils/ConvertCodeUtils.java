package com.tce.smart.bridge.netty.utils;

import com.tce.smart.bridge.core.exception.SmartException;
import com.tce.smart.bridge.netty.constant.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Li.JiaJun
 * @since 2021/12/16 9:01
 */
public class ConvertCodeUtils {

    /**
     * @param value 字节
     * @return 16进制字符串
     * @throws
     * @Title:bytes2HexString
     * @Description:字节转16进制字符串
     */
    public static String byte2HexString(byte value) {
        String hex = Integer.toHexString(value & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        return hex.toUpperCase();
    }

    /**
     * @param b 字节数组
     * @return 16进制字符串
     * @throws
     * @Title:bytes2HexString
     * @Description:字节数组转16进制字符串
     */
    public static String bytes2HexString(byte[] b) {
        StringBuilder result = new StringBuilder();
        String hex;
        for (byte value : b) {
            hex = Integer.toHexString(value & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result.append(hex.toUpperCase());
        }
        return result.toString();
    }

    /**
     * @param src 16进制字符串
     * @return 字节数组
     * @Title:hexString2Bytes
     * @Description:16进制字符串转字节数组
     */
    public static byte[] hexString2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

    /**
     * @param strPart 字符串
     * @return 16进制字符串
     * @Title:string2HexString
     * @Description:字符串转16进制字符串
     */
    public static String string2HexString(String strPart) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < strPart.length(); i++) {
            int ch = strPart.charAt(i);
            String strHex = Integer.toHexString(ch);
            hexString.append(strHex);
        }
        return hexString.toString();
    }

    /**
     * @param src 16进制字符串
     * @return 字节数组
     * @throws
     * @Title:hexString2String
     * @Description:16进制字符串转字符串
     */
    public static String hexString2String(String src) {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < src.length() / Constants.TWO; i++) {
            temp.append((char) Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue());
        }
        return temp.toString();
    }

    /**
     * @param a   转化数据
     * @param len 占用字节数
     * @return
     * @throws
     * @Title:intToHexString
     * @Description:10进制数字转成16进制
     */
    public static String intToHexString(int a, int len) {
        len <<= 1;
        StringBuilder hexString = new StringBuilder(Integer.toHexString(a));
        int b = len - hexString.length();
        if (b > 0) {
            for (int i = 0; i < b; i++) {
                hexString.insert(0, "0");
            }
        }
        return hexString.toString();
    }

    /**
     * Convert byte[] to hex string.这里我们可以将byte转换成int
     *
     * @param src byte[] data
     * @return hex string
     */
    public static String bytes2Str(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * @return 接收字节数据并转为16进制字符串
     */
    public static String receiveHexToString(byte[] by) {
        try {
            String str = bytes2Str(by);
            str = str.toUpperCase();
            return str;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("接收字节数据并转为16进制字符串异常");
        }
        return null;
    }

    /**
     * 计算16进制长度
     *
     * @param size
     * @return
     */
    public static String getLength(int size) {
        StringBuilder binary = new StringBuilder(Integer.toBinaryString(size));
        while (binary.length() < 8) {
            binary.insert(0, "0");
        }

        String leftBinary = binary.substring(binary.length() - 6, binary.length()) + "10";
        StringBuilder rightBinary = new StringBuilder(binary.substring(0, binary.length() - 6));
        while (rightBinary.length() < 8) {
            rightBinary.insert(0, "0");
        }
        String leftHex = Integer.toHexString(Integer.parseInt(leftBinary, 2));
        String rightHex = Integer.toHexString(Integer.parseInt(rightBinary.toString(), 2));
        return hexFillZero(leftHex) + hexFillZero(rightHex);
    }

    /**
     * 16进制字符串补0
     *
     * @param hexStr
     * @return
     */
    public static String hexFillZero(String hexStr) {
        if (hexStr.length() < Constants.TWO) {
            hexStr = "0" + hexStr;
        }
        return hexStr;
    }

    /**
     * 2进制字符串补0
     *
     * @param binaryStr
     * @return
     */
    public static String binaryFillZero(String binaryStr) {
        StringBuilder binaryStrBuilder = new StringBuilder(binaryStr);
        while (binaryStrBuilder.length() < Constants.EIGHT) {
            binaryStrBuilder.insert(0, "0");
        }
        return binaryStrBuilder.toString();
    }

    /**
     * 通过测量点号获取信息点DA的16进制字符串
     * @param seq
     * @return
     */
    public static String getDA(Integer seq) {
        int DA1 = seq % 8;
        int DA2 = seq / 8 + 1;
        StringBuilder DA1Binary = new StringBuilder();
        if (DA1 == 0) {
            DA2 -= 1;
            DA1Binary.append("10000000");
        }
        for (int i = 0; i < DA1; i++) {
            if (i == DA1 - 1) {
                DA1Binary.insert(0, "1");
            } else {
                DA1Binary.insert(0, "0");
            }
        }
        while (DA1Binary.length() < 8) {
            DA1Binary.insert(0, "0");
        }
        return hexFillZero(Integer.toHexString(Integer.parseInt(DA1Binary.toString(), 2))) + hexFillZero(Integer.toHexString(DA2));
    }

    public static String getCheckSum(String hexStr) {
        if (hexStr.length() % 2 != 0) {
            throw new SmartException("用户数据区拼装不正确");
        }
        List<String> hexList = new ArrayList<>();
        for (int i = 0; i < hexStr.length(); i += 2) {
            hexList.add(hexStr.substring(i, i + 2));
        }
        String checkSum = Integer.toBinaryString(Integer.parseInt(hexList.get(0), 16));
        for (int i = 1; i < hexList.size(); i++) {
            checkSum = sumBinary(checkSum, Integer.toBinaryString(Integer.parseInt(hexList.get(i), 16)));
        }
        // 校验和
        return ConvertCodeUtils.hexFillZero(Integer.toHexString(Integer.parseInt(checkSum, 2)));
    }

    private static String sumBinary(String basicBin, String addBin) {
        basicBin = ConvertCodeUtils.binaryFillZero(basicBin);
        addBin = ConvertCodeUtils.binaryFillZero(addBin);
        StringBuilder binarySum = new StringBuilder();
        int carry = 0;
        for (int i = basicBin.length() - 1; i >= 0; i--) {
            int binaryAdd;
            if (i == basicBin.length() - 1) {
                binaryAdd = Integer.parseInt(basicBin.substring(i)) + Integer.parseInt(addBin.substring(i)) + carry;
            } else {
                binaryAdd = Integer.parseInt(basicBin.substring(i, i + 1)) + Integer.parseInt(addBin.substring(i, i + 1)) + carry;
            }
            if (binaryAdd >= 2) {
                if (binaryAdd == 3) {
                    binaryAdd = 1;
                } else {
                    binaryAdd = 0;
                }
                carry = 1;
            } else {
                carry = 0;
            }
            binarySum.insert(0, binaryAdd);
        }
        return binarySum.toString();
    }

    /**
     * 其他进制转十进制
     *
     * @param number
     * @param scale  进制类型
     * @return
     */
    public static int scale2Decimal(String number, int scale) {
        checkNumber(number);
        if (Constants.TWO > scale || scale > 32) {
            throw new IllegalArgumentException("scale is not in range");
        }
        // 不同其他进制转十进制,修改这里即可
        int total = 0;
        String[] ch = number.split("");
        int chLength = ch.length;
        for (int i = 0; i < chLength; i++) {
            total += Integer.valueOf(ch[i]) * Math.pow(scale, chLength - 1 - i);
        }
        return total;
    }

    public static void checkNumber(String number) {
        String regexp = "^\\d+$";
        if (null == number || !number.matches(regexp)) {
            throw new IllegalArgumentException("input is not a number");
        }
    }

    /**
     * 计算CRC16校验码
     *
     * @param bytes 字节数组
     * @return {@link String} 校验码
     * @since 1.0
     */
    public static String getCRC(byte[] bytes) {
        // CRC寄存器全为1
        int CRC = 0x0000ffff;
        // 多项式校验值
        int POLYNOMIAL = 0x0000a001;
        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        // 结果转换为16进制
        String result = Integer.toHexString(CRC).toUpperCase();
        if (result.length() != 4) {
            StringBuilder sb = new StringBuilder("0000");
            result = sb.replace(4 - result.length(), 4, result).toString();
        }
        //高位在前地位在后
        // 交换高低位，低位在前高位在后
        return result.substring(2, 4) + result.substring(0, 2);
    }
}
