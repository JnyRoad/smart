package com.tce.smart.platform.core.entity.watermeter;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:00
 */
@Data
@Builder
@TableName("smt_ele_meter")
@EqualsAndHashCode(callSuper = true)
public class SmtEleMeter extends Model<SmtEleMeter> {
	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 电表集中器ID
	 */
	private Long concentratorId;
	/**
	 * 通信地址
	 */
	private String address;
	/**
	 * 电表序号
	 */
	private Integer seq;
	/**
	 * 电表通信端口号
	 */
	private String port;
	/**
	 * 电表名称
	 */
	private String name;
	/**
	 * 房间名称
	 */
	private String roomName;
	/**
	 * 房间ID
	 */
	private Integer roomId;
	/**
	 * 楼栋名称
	 */
	private String dormitoryName;
	/**
	 * 楼栋ID
	 */
	private Integer dormitoryId;
	/**
	 * 园区名称
	 */
	private String parkName;
	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 设备状态：0、离线；1、在线
	 */
	private Integer isOnline;
	/**
	 * 当前读数
	 */
	private String currentReading;
	/**
	 * 区域类型：0、宿舍；1、厂区
	 */
	private Integer placeType;
	/**
	 * 厂区ID
	 */
	private Integer areaId;
	/**
	 * 厂区名称
	 */
	private String areaName;
	/**
	 * 闸门是否开启：0、关闭；1、开启
	 */
	private Integer isOpen;
	/**
	 * 倍率
	 */
	private Integer ratio;
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
