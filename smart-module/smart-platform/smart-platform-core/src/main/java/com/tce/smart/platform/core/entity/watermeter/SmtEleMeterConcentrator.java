package com.tce.smart.platform.core.entity.watermeter;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:04
 */
@Data
@Builder
@TableName("smt_ele_meter_concentrator")
@EqualsAndHashCode(callSuper = true)
public class SmtEleMeterConcentrator extends Model<SmtEleMeterConcentrator> {
	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 通信地址
	 */
	private String address;
	/**
	 * 集中器名称
	 */
	private String name;
	/**
	 * IP
	 */
	private String ip;
	/**
	 * 电表集中器通信端口号
	 */
	private String port;
	/**+
	 * 备注
	 */
	private String remark;
	/**
	 * 设备状态：0、离线；1、在线
	 */
	private Integer isOnline;
	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 园区名称
	 */
	private String parkName;
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
