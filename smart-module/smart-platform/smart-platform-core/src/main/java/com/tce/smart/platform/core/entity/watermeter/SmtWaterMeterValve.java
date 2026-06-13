package com.tce.smart.platform.core.entity.watermeter;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 9:26
 */
@Data
@Builder
@TableName("smt_water_meter_valve")
@EqualsAndHashCode(callSuper = true)
public class SmtWaterMeterValve extends Model<SmtWaterMeterValve> {
	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 阀门集中器ID
	 */
	private Long concentratorId;
	/**
	 * 阀门序号
	 */
	private Integer seq;
	/**
	 * 阀门名称
	 */
	private String name;
	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 阀门是否开启：0、关闭；1、开启
	 */
	private Integer isOpen;

	/**
	 * 远程功能状态 0.使本地 1.使远程
	 */
	private Integer remoteStatus;

	/**
	 * 备注
	 */
	private String remark;
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
