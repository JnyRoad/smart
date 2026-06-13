package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * (SmtArticlesReleasePerson)实体类
 *
 * @author sunfujian
 * @date 2021-08-16 15:50:40
 */
@Data
@TableName("SMT_ARTICLES_RELEASE_PERSON")
@EqualsAndHashCode(callSuper = true)
public class SmtArticlesReleasePerson extends Model<SmtArticlesReleasePerson> {
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
	 * 姓名
	 */
	private String xm;
	/**
	 * 工号
	 */
	private String gh;
	/**
	 * 离厂事由
	 */
	private String lcsy;
	/**
	 * 离厂日期
	 */
	private String lcrq;
	/**
	 * 离厂时间
	 */
	private String lcsj;
	/**
	 * 返厂日期
	 */
	private String fcrq;
	/**
	 * 返厂时间
	 */
	private String fcsj;
	/**
	 * 级别
	 */
	private String jb;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;
	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;

}
