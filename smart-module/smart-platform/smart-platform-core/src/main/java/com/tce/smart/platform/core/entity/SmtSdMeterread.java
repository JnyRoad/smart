package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * @description: 水电抄表记录表
 * @date: 2020-07-10 8:51
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_SD_METERREAD")
@EqualsAndHashCode(callSuper = true)
public class SmtSdMeterread extends Model<SmtSdMeterread> {
	private static final long serialVersionUID = 3877969728068028354L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 房间号标识
	 */
	@NotBlank(message="房间号标识不能为空")
	private Integer roomId;

	/**
	 * 抄表月份
	 */
	@NotBlank(message="抄表月份不能为空")
	@JsonFormat(pattern="yyyy-MM")
	private Date meterMonth;

	/**
	 * 抄表状态 0：未抄表	1：部分抄表	2：抄表完成
	 */
	private Integer status;

	/**
	 * 生成结算状态 0：未生成	1：已生成
	 */
	private Integer statementStatus;

	/**
	 * 添加时间
	 */
	private Date createTime;
}
