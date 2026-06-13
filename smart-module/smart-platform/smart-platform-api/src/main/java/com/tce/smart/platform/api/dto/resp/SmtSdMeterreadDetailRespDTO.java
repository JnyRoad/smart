package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: SmtSdMeterreadDetailRespDTO
 * @date: 2020-07-13 16:21
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtSdMeterreadDetailRespDTO implements Serializable {

	private static final long serialVersionUID = -9118561113574227847L;

	/**
	 * 房间抄表记录标识
	 */
	private Long mrId;

	/**
	 * 抄表月份
	 */
	@JsonFormat(pattern="yyyy-MM")
	private Date meterMonth;

	/**
	 * 收费项目详细数据
	 */
	private List<MeterReadDetail> meterReadDetailList;

	@Data
	@NoArgsConstructor
	public static class MeterReadDetail{

		/**
		 * 抄表详情ID
		 */
		private Long id;
		/**
		 * 上月止度
		 */
		private Double preMonthNum;

		/**
		 * 本月止度
		 */
		private Double curMonthNum;

		/**
		 * 收费项目
		 */
		private Integer categoryId;

		/**
		 * 本月入住总天数
		 */
		private Integer totalStayDays;

		/**
		 * 上月修正止度
		 */
		private Double revPreMonthNum;

		/**
		 * 上月止度是否修正过 0.未修正 1.已修正
		 */
		private Integer isRevise;

		/**
		 * 抄表开始时间
		 */
		private Date startTime;

		/**
		 * 抄表结束时间
		 */
		private Date endTime;

		/**
		 * 抄表人员
		 */
		private String meterUser;
	}
}
