package com.tce.smart.platform.core.entity.watermeter;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 9:25
 */
@Data
@Builder
@TableName("smt_water_meter_history")
@EqualsAndHashCode(callSuper = true)
public class SmtWaterMeterHistory extends Model<SmtWaterMeterHistory> {
	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 水表ID
	 */
	private Long waterMeterId;
	/**
	 * 采集时间
	 */
	private LocalDateTime collectTime;
	/**
	 * 当前读数
	 */
	private String currentReading;
	/**
	 * 是否异常：0、否；1、是
	 */
	private Integer isError;
	/**
	 * 逻辑删除
	 */
	@TableLogic
	private Integer isDelete;
	/**
	 * 创建用户ID
	 */
	@TableField(value = "CREATE_USER_ID", fill = FieldFill.INSERT)
	private Integer createUserId;
	/**
	 * 创建时间
	 */
	@TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
	private LocalDateTime createTime;
	/**
	 * 更新用户ID
	 */
	@TableField(value = "UPDATE_USER_ID", fill = FieldFill.UPDATE)
	private Integer updateUserId;
	/**
	 * 修改时间
	 */
	@TableField(value = "UPDATE_TIME", fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;
}
