package com.tce.smart.platform.core.entity.watermeter;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author Li.JiaJun
 * @since 2022/5/12 10:41
 */
@Data
@Builder
@TableName("smt_ele_meter_change")
@EqualsAndHashCode(callSuper = true)
public class SmtEleMeterChange extends Model<SmtEleMeterChange> {
	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 电表ID
	 */
	private Long eleMeterId;
	/**
	 * 更换前电表ID
	 */
	private Long beforeMeterId;
	/**
	 * 更换前通信地址
	 */
	private String beforeAddress;
	/**
	 * 更换前电表序号
	 */
	private Integer beforeSeq;
	/**
	 * 更换前倍率
	 */
	private Integer beforeRatio;
	/**
	 * 更换前电表通信端口号
	 */
	private String beforePort;
	/**
	 * 更换前集中器
	 */
	private String beforeConcentrator;
	/**
	 * 更换后通信地址
	 */
	private String afterAddress;
	/**
	 * 更换后电表序号
	 */
	private Integer afterSeq;
	/**
	 * 更换后倍率
	 */
	private Integer afterRatio;
	/**
	 * 更换后电表通信端口号
	 */
	private String afterPort;
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
