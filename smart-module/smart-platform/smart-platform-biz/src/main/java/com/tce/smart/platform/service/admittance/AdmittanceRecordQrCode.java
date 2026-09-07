package com.tce.smart.platform.service.admittance;

import com.tce.smart.tool.util.QRCodeUtils;

/**
 * 入厂人员记录二维码生成器。
 *
 * <p>二维码只编码逐人记录ID，不接受预约码或主申请ID。</p>
 */
public final class AdmittanceRecordQrCode {

	private AdmittanceRecordQrCode() {
	}

	/**
	 * 生成只包含人员记录ID十进制字符串的PNG Base64。
	 *
	 * @param fellowId 入厂人员记录ID
	 * @return PNG图片的Base64正文
	 */
	public static String create(Long fellowId) {
		String content = validateAndFormat(fellowId);
		try {
			return QRCodeUtils.wordsCreateQRCode(content);
		} catch (Exception exception) {
			throw new IllegalStateException("生成访客记录二维码失败", exception);
		}
	}

	private static String validateAndFormat(Long fellowId) {
		if (fellowId == null || fellowId <= 0) {
			throw new IllegalArgumentException("访客记录ID必须是1至19位正整数");
		}
		return fellowId.toString();
	}
}
