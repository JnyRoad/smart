package com.tce.smart.platform.core.entity;


import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社保表
 * @author 齐佩
 *
 */
@Data
@TableName("smt_social_security")
@EqualsAndHashCode(callSuper = true)
public class SmtSocialSecurity extends Model<SmtSocialSecurity> {


	   /**
	   * 主键
	   */
	    @TableId(value = "id", type = IdType.AUTO)
	    private Integer id;

	    /**
	     * 社保标题
	     */
	    private String title;

	    /**
	     * 图片
	     */
	    private byte[] image;

	    /**
	     * url地址
	     */
	    private String url;

	    /**
	     * 创建时间
	     */
	    private LocalDateTime createTime;


}
