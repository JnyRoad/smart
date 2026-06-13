package com.tce.smart.platform.core.dto.commonsd;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 宿舍水电抄表记录DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class DormitorySDMeterreadDTO {
	/**
	 * 抄表详情记录ID
	 */
	private Long id;

	/**
	 * 抄表记录ID
	 */
	private Long mrId;

	/**
	 * 楼栋Id
	 */
	private Integer dormitoryId;

	/**
	 * 楼层Id
	 */
	private Integer floorId;

	/**
	 * 房间编号
	 */
	private Integer roomId;

	/**
	 * 房间名称
	 */
	private String roomName;

	/**
	 * 房间性别
	 */
	private Integer roomSex;

	/**
	 * 抄表月份
	 */
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
	 * 收费项目
	 */
	private Integer categoryId;

	/**
	 * 水电模板Id
	 */
	private Long sdTemplateId;


	/**
	 * 上月止度
	 */
	private Double preMonthNum;

	/**
	 * 上月止度修正数据
	 */
	private Double revPreMonthNum;

	/**
	 * 本月止度/今日使用
	 */
	private Double curMonthNum;

	/**
	 * 超出用量
	 */
	private Double curOverNum;

	/**
	 * 上月止度是否修正
	 */
	private Integer isRevise;

	/**
	 * 抄表人
	 */
	private String meterUser;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 入住总天数
	 */
	private Integer totalInStay;

	/**
	 * 标准用量
	 */
	private Double curStdQty;

	/**
	 * 单价
	 */
	private Double curOverFee;

}
