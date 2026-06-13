package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.util.Date;

/**
 * @description: 公摊水电抄表记录
 * @date: 2020-09-29 13:37
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_COMMON_SD_METERREAD")
@EqualsAndHashCode(callSuper = true)
public class SmtCommonSDMeterread extends Model<SmtCommonSDMeterread> {

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 公摊水电表记录ID
	 */
	private Long commonId;

	/**
     * 抄表月份
	 */
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
     * 本月止度
	 */
	private Double curMonthNum;

	/**
	 * 结算状态 0.未结算 1.以结算
	 */
	private Integer status;

	/**
	 * 上月止度是否修正过 0.未修正 1.已修正
	 */
	private Integer isRevise;

	/**
	 * 房间数量
	 */
	private Integer roomCount;

	/**
	 * 本次抄表开始时间
	 */
	private Date startTime;

	/**
	 * 本次抄表结束时间
	 */
	private Date endTime;

	/**
     * 创建时间
	 */
	private Date createTime;

	/**
	 * 所有公摊房间的入住总人天
	 */
	private Integer totalStayDays;

	/**
	 * 抄表人
	 */
	private String meterUser;

	/**
	 * 房间配置规则信息
	 */
	private String roomRuleInfo;
}
