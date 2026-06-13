package com.tce.smart.data.wrapper.businesstrip;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.tce.smart.businesstrip.core.entity.CcdFormtableMain;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainRespDTO;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: EvwEmphrYsWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@Slf4j
public class CcdFormtableMainWrapper extends BaseWrapper<CcdFormtableMain, CcdFormtableMainRespDTO> {
	/**
	 * yyyy-MM-dd
	 */
	private static final Pattern DATE_PATTERN_1 = Pattern.compile("^[1-9]+[0-9]{3}-\\d{2}-\\d{2}$");

	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
	private static final Pattern DATE_PATTERN_2 = Pattern.compile("^[1-9]+[0-9]{3}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d" +
			"{2" +
			"}$");

	/**
	 * yyyy/MM/dd
	 */
	private static final Pattern DATE_PATTERN_3 = Pattern.compile("^[1-9]+[0-9]{3}/\\d{2}/\\d{2}$");

	/**
	 * yyyy/MM/dd  HH:mm:ss
	 */
	private static final Pattern DATE_PATTERN_4 = Pattern.compile("^[1-9]+[0-9]{3}/\\d{2}/\\d{2}\\s\\d{2}:\\d{2}:\\d" +
			"{2" +
			"}$");

	@Override
	protected CcdFormtableMainRespDTO warp(CcdFormtableMain ccdFormtableMain) {
		CcdFormtableMainRespDTO ccdFormtableMainRespDTO = new CcdFormtableMainRespDTO();
		BeanUtils.copyProperties(ccdFormtableMain, ccdFormtableMainRespDTO);

		//转换时间类型
		ccdFormtableMainRespDTO.setTripBeginTime(changeToUtilDate(ccdFormtableMain.getTripBeginTime()));
		ccdFormtableMainRespDTO.setTripEndTime(changeToUtilDate(ccdFormtableMain.getTripEndTime()));
		ccdFormtableMainRespDTO.setApplicationTime(changeToUtilDate(ccdFormtableMain.getApplicationTime()));
		ccdFormtableMainRespDTO.setActualReturnTime(changeToUtilDate(ccdFormtableMain.getActualReturnTime()));

		return ccdFormtableMainRespDTO;
	}

	/**
	 * 把sqlDate转成utilDate
	 *
	 * @param sqlDate sqlDate
	 * @return 把sqlDate转成utilDate
	 */
	private java.util.Date changeToUtilDate(String sqlDate) {
		Date date = null;
		if (!StringUtil.isNullOrEmpty(sqlDate)) {
			if (DATE_PATTERN_1.matcher(sqlDate).matches()) { //时间格式 yyyy-MM-dd
				date = DateUtil.parse(sqlDate, DatePattern.NORM_DATE_PATTERN);
			} else if (DATE_PATTERN_2.matcher(sqlDate).matches()) {//时间格式 yyyy-MM-dd HH:mm:ss
				date = DateUtil.parse(sqlDate, DatePattern.NORM_DATETIME_PATTERN);
			} else if (DATE_PATTERN_3.matcher(sqlDate).matches()) { //时间格式 yyyy/MM/dd
				date = DateUtil.parse(sqlDate, "yyyy/MM/dd");
			} else if (DATE_PATTERN_4.matcher(sqlDate).matches()) {//时间格式 yyyy/MM/dd  HH:mm:ss
				date = DateUtil.parse(sqlDate, "yyyy/MM/dd  HH:mm:ss");
			} else {
				log.warn("未明确的时间格式:{}", sqlDate);
			}
		}
		return date;
	}

	/**
	 * 把sqlDate转成utilDate
	 *
	 * @param sqlDate sqlDate
	 * @return 把sqlDate转成utilDate
	 */
	private java.util.Date changeToUtilDate(java.sql.Date sqlDate) {
		return Objects.nonNull(sqlDate) ? new Date(sqlDate.getTime()) : null;
	}

//	public static void main(String[] args) {
//		String dateStr = "2019-12-12";
//		String dateStr = "2019-12-12 12:12:12";
//		String dateStr = "2019/12/12";
//		String dateStr = "2019/12/12 12:12:12";
//		System.out.println("yyyy-MM-dd==[" + dateStr + "]==" + DATE_PATTERN_1.matcher(dateStr).matches());
//		System.out.println("yyyy-MM-dd HH:mm:ss==[" + dateStr + "]==" + DATE_PATTERN_2.matcher(dateStr).matches());
//		System.out.println("yyyy/MM/dd==[" + dateStr + "]==" + DATE_PATTERN_3.matcher(dateStr).matches());
//		System.out.println("yyyy/MM/dd  HH:mm:ss==[" + dateStr + "]==" + DATE_PATTERN_4.matcher(dateStr).matches());
//	}
}
