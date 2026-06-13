package com.tce.smart.platform.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description: SmtSdMeterreadVO
 * @date: 2020-07-10 11:08
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtSdMeterreadVO {
	/**
	 * 记录Id
	 */
	private Long Id;

	/**
	 * 园区Id
	 */
	private Integer parkId;

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 * 楼栋Id
	 */
	private Integer dormitoryId;

	/**
	 * 楼栋名称
	 */
	private String dormitoryName;

	/**
	 * 楼层Id
	 */
	private Integer floorId;

	/**
	 * 楼层名称
	 */
	private String floorName;

	/**
	 * 房间Id
	 */
	private Integer roomId;

	/**
	 * 房间号
	 */
	private Integer roomName;

	/**
	 * 抄表月份
	 */
	@JsonFormat(pattern="yyyy-MM")
	private Date meterMonth;

	/**
	 * 抄表状态
	 */
	private Integer status;

	/**
	 * 结算状态
	 */
	private Integer statementStatus;

	/**
	 * 热水用量
	 */
	private Double hotWater;

	/**
	 * 冷水用量
	 */
	private Double coldWater;

	/**
	 * 电用量
	 */
	private Double electric;

	/**
	 * 是否重置
	 */
	private Integer isRevise;

	/**
	 * 重置记录
	 */
	private List<ReviseInfo> reviseInfo;

	@Data
	public static class ReviseInfo{
		/**
		 * 抄表人
		 */
		private String meterUser;

		/**
		 * 收费项目
		 */
		private Integer categoryId;

		/**
		 * 抄表月份
		 */
		@JsonFormat(pattern="yyyy-MM")
		private Date meterMonth;

		/**
		 * 上月止度
		 */
		private Double preMonthNum;

		/**
		 * 上月止度修正数据
		 */
		private Double revPreMonthNum;

		/**
		 * 抄表时间
		 */
		private Date createTime;
	}
}
