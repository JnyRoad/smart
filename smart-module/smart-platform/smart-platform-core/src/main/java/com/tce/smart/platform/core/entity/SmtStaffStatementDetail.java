package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 员工水电结算详情表
 * @date: 2020-07-16 15:33
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_STAFF_STATEMENTDETAIL")
@EqualsAndHashCode(callSuper = true)
public class SmtStaffStatementDetail extends Model<SmtStaffStatementDetail> {
	private static final long serialVersionUID = 8918903640086514716L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 房间抄表记录标识
	 */
	private Long mrId;
	/**
	 * 房间抄表明细记录标识
	 */
	private Long mrdetailId;
	/**
	 * 收费类型
	 */
	private Integer categoryId;
	/**
	 * 员工编号
	 */
	private String staffBadge;
	/**
	 * 员工姓名
	 */
	private String staffName;
	/*
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 房间id
	 */
	private Integer roomId;
	/**
	 * 房间名称
	 */
	private Integer roomName;
	/**
	 * 床位id
	 */
	private Integer bedId;
	/**
	 * 床位名称
	 */
	private Integer bedName;
	/**
	 * 入住时间
	 */
	private Date inTime;
	/**
	 * 实际天数
	 */
	private Integer stayDays;
	/**
	 * 备注天数
	 */
	private Integer remarkDays;
	/**
	 * 当月在本宿舍的费用
	 */
	private BigDecimal fee;

	/**
	 * 抄表月份
	 */
	private Date meterMonth;

	/**
	 * 抄表类型 1.房间抄表 2.公摊抄表
	 */
	private Integer meterType;
	/**
	 * 使用量
	 */
	private Double usage;

	/**
	 * 结算状态 0.未结算 1.已结算
	 */
	private Integer statementStatus;
	/**
	 * 结算时间
	 */
	private Date statementDate;
}
