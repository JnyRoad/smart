package com.tce.smart.bridge.netty.utils;

/**
 * 帧序列域获取
 * @author Li.JiaJun
 * @since 2021/12/17 11:17
 */
public class SeqUtils {

	/**
	 * 帧时间标签有效位TpV
	 * TpV=0：表示在附加信息域中无时间标签Tp；
	 * TpV=1：表示在附加信息域中带有时间标签Tp
	 */
	private static String tpv;
	/**
	 * FIR、FIN组合状态所表示的含义：
	 * FIR	FIN	应用说明
	 * 0	0	多帧：中间帧
	 * 0	1	多帧：结束帧
	 * 1	0	多帧：第1帧，有后续帧。
	 * 1	1	单帧
	 * FIR：置“1”，报文的第一帧
	 */
	private static String fir;
	/**
	 * FIN：置“1”，报文的最后一帧
	 */
	private static String fin;
	/**
	 * 请求确认标志位CON：
	 * 在所收到的报文中，CON位置“1”，表示需要对该帧报文进行确认；置“0”，表示不需要对该帧报文进行确认。
	 */
	private static String con;
	/**
	 * 启动帧序号PSEQ
	 * a）	启动帧序号PSEQ
	 * PSEQ取自1字节的启动帧计数器PFC的低4位计数值0～15。
	 * b）	启动帧帧序号计数器PFC
	 * 每一对启动站和从动站之间均有1个独立的、由1字节构成的计数范围为0～255的启动帧帧序号计数器PFC，
	 * 用于记录当前启动帧的序号。启动站每发送1帧报文，该计数器加1，从0～255循环加1递增；重发帧则不加1。
	 */
	private static final String pseq = "0001";
	/**
	 * 响应帧序号RSEQ
	 * c）	响应帧序号RSEQ
	 * 响应帧序号RSEQ以启动报文中的PSEQ作为第一个响应帧序号，后续响应帧序号在RSEQ的基础上循环加1递增，数值范围为0～15。
	 * d）	帧序号改变规则
	 * 1）	启动站发送报文后，当一个期待的响应在超时规定的时间内没有被收到，如果允许启动站重发，则该重发的启动帧序号PSEQ不变。
	 *      重发次数可设置，最多3次；重发次数为0，则不允许重发。
	 * 2）	当TpV=0时，如果从动站连续收到两个具有相同启动帧序号PSEQ的启动报文，通常意味着报文的响应未被对方站收到。在这种情况下，
	 *      则重发响应（不必重新处理该报文）。
	 * 3）	当TpV=0时，如果启动站连续收到两个具有相同响应帧序号RSEQ的响应帧，则不处理第二个响应。
	 * 4） 终端在开始响应第二个请求之前，必须将前一个请求处理结束。终端不能同时处理多个请求。
	 */
	private static final String rseq = "0000";

	/**
	 * 水表注册|心跳帧序列号16进制获取
	 * @return
	 */
	public static String getWaterRegisterSeq() {
		tpv = "0";
		fir = "1";
		fin = "1";
		con = "0";
		String binary = tpv + fir + fin + con + rseq;
		return Integer.toHexString(ConvertCodeUtils.scale2Decimal(binary, 2));
	}

	/**
	 * 获取水表读数(内置阀门控制)帧序列号
	 * @return
	 */
	public static String getWaterReadingReqSeq() {
		tpv = "1";
		fir = "1";
		fin = "1";
		con = "0";
		String binary = tpv + fir + fin + con + rseq;
		return Integer.toHexString(ConvertCodeUtils.scale2Decimal(binary, 2));
	}

	/**
	 * 获取水表日冻结读数帧序列号
	 * @return
	 */
	public static String getWaterHistoryReadingReqSeq() {
		tpv = "1";
		fir = "1";
		fin = "1";
		con = "0";
		String binary = tpv + fir + fin + con + "1111";
		return Integer.toHexString(ConvertCodeUtils.scale2Decimal(binary, 2));
	}

	/**
	 * 获取电表读数帧序列号
	 * @return
	 */
	public static String getEleReadingReqSeq() {
		tpv = "1";
		fir = "1";
		fin = "1";
		con = "0";
		String binary = tpv + fir + fin + con;
		return Integer.toHexString(ConvertCodeUtils.scale2Decimal(binary + "0010", 2));
	}

	/**
	 * 获取下载档案帧序列号
	 * @return
	 */
	public static String getDownloadFileSeq() {
		tpv = "1";
		fir = "1";
		fin = "1";
		con = "0";
		String binary = tpv + fir + fin + con + pseq;
		return Integer.toHexString(ConvertCodeUtils.scale2Decimal(binary, 2));
	}
}
