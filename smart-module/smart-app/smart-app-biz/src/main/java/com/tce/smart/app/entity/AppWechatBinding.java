package com.tce.smart.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;
import java.util.Date;

/**
 * @description: 微信绑定记录表
 * @date: 2020-08-06 17:39
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("APP_WECHAT_BINDING")
@EqualsAndHashCode(callSuper = true)
public class AppWechatBinding extends Model<AppWechatBinding> {

	private static final long serialVersionUID = -5576400858757851413L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 微信openId
	 */
	private String openId;

	/**
	 * 预约人电话
	 */
	private String visitPhone;

	/**
	 * 预约人头像图片code
	 */
	private String imageCode;

	/**
	 * 添加时间
	 */
	private Date createTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;
}
