package com.tce.smart.app.api.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author fushiping
 * @date 2019/7/3 18:34
 **/

@Data
@TableName("app_user_device")
@EqualsAndHashCode(callSuper = true)
public class AppUserDevice extends Model<AppUserDevice> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -5364092233588863779L;

	/**
	 * 主键id
	 */
	private Integer id;
	/**
	 * 员工号
	 */
	private String badge;
	/**
	 * 设备名称
	 */
	private String deviceName;

	/**
	 * 设备编码
	 */
	private String deviceNo;

	/**
	 * 推送标识 IOS送DeviceToke，Android送ClientId
	 */
	private String devicePushId;

	/**
	 * 系统类型 1安卓，2-IOS
	 */
	private Integer osType;

	/**
	 * 绑定标识 0-未绑定，1-已绑定
	 */
	private Integer bindFlag;
	/**
	 *
	 */
	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;
}
