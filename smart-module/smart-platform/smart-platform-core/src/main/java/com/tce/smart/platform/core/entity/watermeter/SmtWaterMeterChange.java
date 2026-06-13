package com.tce.smart.platform.core.entity.watermeter;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author Li.JiaJun
 * @since 2022/5/12 10:47
 */
@Data
@Builder
@TableName("smt_water_meter_change")
@EqualsAndHashCode(callSuper = true)
public class SmtWaterMeterChange extends Model<SmtWaterMeterChange> {
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
	 * 更换前水表ID
	 */
	private Long beforeMeterId;
	/**
	 * 更换前通信地址
	 */
	private String beforeAddress;
	/**
	 * 更换前水表序号
	 */
	private Integer beforeSeq;
	/**
	 * 更换前水表下行通道
	 */
	private String beforePort;
	/**
	 * 更换前用户大类
	 */
	private String beforeLargeClass;
	/**
	 * 更换前集中器
	 */
	private String beforeConcentrator;
	/**
	 * 更换后通信地址
	 */
	private String afterAddress;
	/**
	 * 更换后水表序号
	 */
	private Integer afterSeq;
	/**
	 * 更换后水表下行通道
	 */
	private String afterPort;
	/**
	 * 更换后用户大类
	 */
	private String afterLargeClass;
	/**
	 * 更换后集中器
	 */
	private String afterConcentrator;
	/**
	 * 更换人
	 */
	private String createUserName;
	/**
	 * 更换用户ID
	 */
	@TableField(value = "CREATE_USER_ID", fill = FieldFill.INSERT)
	private Integer createUserId;
	/**
	 * 更换时间
	 */
	@TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
	private LocalDateTime createTime;
}
