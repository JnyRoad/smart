package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


/**
 * (SmtArticlesReleaseThing)实体类
 *
 * @author sunfujian
 * @date 2021-08-17 14:25:00
 */
@Data
@TableName("SMT_ARTICLES_RELEASE_THING")
@EqualsAndHashCode(callSuper = true)
public class SmtArticlesReleaseThing extends Model<SmtArticlesReleaseThing> {
	private static final long serialVersionUID = 1L;
	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	private Long releaseId;
	private Long mainId;
	/**
	 * 申请人工号
	 */
	private String badge;
	/**
	 * 资产编码
	 */
	private String wpbm;
	/**
	 * 物品名称
	 */
	private String wpmc;
	/**
	 * 物品单位
	 */
	private String wpdw;
	/**
	 * 物品数量
	 */
	private String wpsl;
	/**
	 * 物品流向
	 */
	private String wplx;
	/**
	 * 接收单位
	 */
	private String jsdw;
	/**
	 * 备注(原因)
	 */
	private String bz;
	/**
	 * 运输方式
	 */
	private String ysfs;
	/**
	 * 姓名
	 */
	private String xm;
	/**
	 * 车牌号
	 */
	private String cph;
	/**
	 * 放行日期
	 */
	private String fxrq;
	/**
	 * 是否返厂
	 */
	private String wpsffc;
	/**
	 * 返厂日期
	 */
	private String wpfcrq;
	/**
	 * 返厂时间
	 */
	private String wpfcsj;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;
	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;

}
