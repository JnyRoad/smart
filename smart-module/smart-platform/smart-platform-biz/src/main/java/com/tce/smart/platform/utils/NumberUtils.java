package com.tce.smart.platform.utils;

import cn.hutool.core.util.NumberUtil;
import com.tce.smart.common.core.constant.NumberConstants;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.api.dto.resp.commonsd.DormitorySDMeterreadNewRespDTO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author Li.JiaJun
 * @since 2022/3/24 15:33
 */
@Slf4j
@UtilityClass
public class NumberUtils {

	/**
	 * 格式化水电表读数
	 *
	 * @param value
	 * @return
	 */
	public String strFormat(String value) {
		if (StringUtils.isBlank(value)) {
			return StringUtils.EMPTY;
		}
		double num;
		try {
			num = Double.parseDouble(value);
		} catch (Exception e) {
			log.error("读数异常：{}", value);
			num = 0;
		}
		return NumberUtil.roundStr(num, NumberConstants.TWO);
	}

	/**
	 * 日期格式转换
	 *
	 * @param time
	 * @return
	 */
	public LocalDateTime convertTime(String time) {
		if (StringUtils.isEmpty(time)) {
			return null;
		} else {
			return DateUtils.parseLocalDateTime(time);
		}
	}

	/**
	 * 宿舍水电金额格式化
	 * @param respDTOList
	 */
	public void formatDormitorySdMeterRead(List<DormitorySDMeterreadNewRespDTO> respDTOList) {
		for (DormitorySDMeterreadNewRespDTO newResp : respDTOList) {
			newResp.setColdPreMonthNum(doubleFormat(newResp.getColdPreMonthNum()));
			newResp.setColdCurMonthNum(doubleFormat(newResp.getColdCurMonthNum()));
			newResp.setColdUse(doubleFormat(newResp.getColdUse()));
			newResp.setColdQty(doubleFormat(newResp.getColdQty()));
			newResp.setColdOverUse(doubleFormat(newResp.getColdOverUse()));
			newResp.setColdOverFee(doubleFormat(newResp.getColdOverFee()));
			newResp.setHotPreMonthNum(doubleFormat(newResp.getHotPreMonthNum()));
			newResp.setHotCurMonthNum(doubleFormat(newResp.getHotCurMonthNum()));
			newResp.setHotUse(doubleFormat(newResp.getHotUse()));
			newResp.setHotQty(doubleFormat(newResp.getHotQty()));
			newResp.setHotOverFee(doubleFormat(newResp.getHotOverFee()));
			newResp.setHotOverUse(doubleFormat(newResp.getHotOverUse()));
			newResp.setElePreMonthNum(doubleFormat(newResp.getElePreMonthNum()));
			newResp.setEleCurMonthNum(doubleFormat(newResp.getEleCurMonthNum()));
			newResp.setEleUse(doubleFormat(newResp.getEleUse()));
			newResp.setEleQty(doubleFormat(newResp.getEleQty()));
			newResp.setEleOverUse(doubleFormat(newResp.getEleOverUse()));
			newResp.setEleOverFee(doubleFormat(newResp.getEleOverFee()));
			newResp.setTotalAmount(doubleFormat(newResp.getTotalAmount()));
			newResp.setAvgAmount(doubleFormat(newResp.getAvgAmount()));
		}
	}

	/**
	 * 保留2位小数，四舍五入
	 * @param value
	 * @return
	 */
	public double doubleFormat(Double value) {
		if (Objects.isNull(value)) {
			return 0.0;
		}
		BigDecimal b = new BigDecimal(value);
		return b.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * 字符串转double
	 * @param value
	 * @return
	 */
	public double transDouble(String value) {
		try {
			return Double.parseDouble(value);
		} catch (Exception e) {
			return 0.0;
		}
	}
}
