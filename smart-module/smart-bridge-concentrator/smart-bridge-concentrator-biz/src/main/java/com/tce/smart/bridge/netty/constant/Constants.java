package com.tce.smart.bridge.netty.constant;

/**
 * @author Li.JiaJun
 * @since 2021/12/17 14:12
 */
public class Constants {
	/**
	 * 电表闸门 通
	 */
	public static final String ZERO = "0";
	public static final Integer TWO = 2;
	public static final Integer EIGHT = 8;
	/**
	 * 16进制帧头符
	 */
	public static final String HEAD_FRAME = "68";
	/**
	 * 16进制结束符
	 */
	public static final String END = "16";
	/**
	 * 水表集中器内置阀门阀控开阀
	 */
	public static final String WATER_VALVE_OPEN = "55";
	/**
	 * 水表集中器内置阀门阀控关阀
	 */
	public static final String WATER_VALVE_CLOSE = "99";
	/**
	 * 水表集中器内置阀门阀控命令消息认证码字段默认值：16字节00
	 */
	public static final String PW = "00000000000000000000000000000000";
}
