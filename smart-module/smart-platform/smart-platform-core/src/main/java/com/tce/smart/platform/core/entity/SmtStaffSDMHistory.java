package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.util.Date;

/**
 * @description: 员工水电结算天数修改历史表
 * @date: 2020-11-21 14:52
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_STAFF_SDMHISTORY")
@EqualsAndHashCode(callSuper = true)
public class SmtStaffSDMHistory extends Model<SmtStaffSDMHistory> {
	private static final long serialVersionUID = 2741460830973596081L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 员工工号
	 */
	private String staffBadge;


	/**
	 * 员工名称
	 */
	private String staffName;

	/**
	 * 房间ID
	 */
	private Integer roomId;

	/**
	 * 房间名称
	 */
	private Integer roomName;

	/**
	 * 收费项目
	 */
	private Integer categoryId;

	/**
	 * 旧入住天数
	 */
	private Integer oldDays;

	/**
	 * 新入住天数
	 */
	private Integer newDays;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 抄表月份
	 */
	private Date meterMonth;

	/**
	 * 抄表类型
	 */
	private Integer meterType;

	/**
	 * 修改人
	 */
	private String userName;

	/**
	 * 添加时间
	 */
	private Date createTime;
}
