package com.tce.smart.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 微信公众号授权令牌
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:12
 */
@Data
@TableName("app_wechat_auth_accesstoken")
@EqualsAndHashCode(callSuper = true)
public class AppWechatAuthAccessToken extends Model<AppWechatAuthAccessToken> {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId
	private Integer id;
	/**
	 * 微信公众号
	 */
	private String wechatAcct;

	/**
	 * 授权令牌
	 */
	private String accesstoken;
	/**
	 * 失效时间毫秒数
	 */
	private Long expiredTime;
	/**
	 * 备用字段1
	 */
	private String remark1;
	/**
	 * 备用字段2
	 */
	private String remark2;
	/**
	 * 备用字段3
	 */
	private String remark3;

}