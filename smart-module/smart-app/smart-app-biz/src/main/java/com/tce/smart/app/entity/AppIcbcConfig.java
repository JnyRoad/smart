package com.tce.smart.app.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工商银行接入配置
 *
 * @author mkwu
 * @date 2019-08-23
 */
@Data
@TableName("app_icbc_config")
@EqualsAndHashCode(callSuper = true)
public class AppIcbcConfig extends Model<AppIcbcConfig> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -6085160886603779096L;

	/**
	 * 主键ID
	 */
	@TableId
	private Integer id;

	/**
	 * 平台appId
	 */
	private String appId;

	/**
	 * AES密钥
	 */
	private String aesKey;

	/**
	 * RSA私钥
	 */
	private String rsaPrivKey;

	/**
	 * 工行网关公钥
	 */
	private String gatePubKey;

	/**
	 * 启用状态:0-未启用，1-已启用
	 */
	private Character enabled;

	/**
	 * 删除状态:0-已删除，1-正常
	 */
	private Character deleteFlag;

	/**
	 * 测试请求时间-测试环境可能用到
	 */
	private Date testReqTime;

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