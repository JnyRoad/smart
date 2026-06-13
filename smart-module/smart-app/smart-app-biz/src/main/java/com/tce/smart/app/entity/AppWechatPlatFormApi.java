package com.tce.smart.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 微信公众号平台Api
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:12
 */
@Data
@TableName("app_wechat_platform_api")
@EqualsAndHashCode(callSuper = true)
public class AppWechatPlatFormApi extends Model<AppWechatPlatFormApi> {
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
	 * AppId
	 */
	private String appId;
	/**
	 * appSecret
	 */
	private String appSecret;
	/**
	 * 获取TokenUrl
	 */
	private String tokenUrl;
	/**
	 * 创建菜单URL
	 */
	private String createMenuUrl;
	/**
	 * 发送模板消息URL
	 */
	private String sendTempUrl;

	/**
	 * 发送客服消息URL
	 */
	private String sendCustomUrl;
	/**
	 * 查询微信用户信息URL
	 */
	private String queryUserinfoUrl;
	/**
	 * 获取票据URL
	 */
	private String getJsticketUrl;
	/**
	 * 下载多媒体URL
	 */
	private String downMediaUrl;
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