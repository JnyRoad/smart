package com.tce.smart.platform.core.entity;


import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 邮件推送模板表
 * @author 齐佩
 *
 */
@Data
@TableName("smt_email_template")
@EqualsAndHashCode(callSuper = true)
public class SmtEmailTemplate  extends Model<SmtDevice>{
	private static final long serialVersionUID = 1L;
    /**
   * 主键
   */
	@TableId(value = "id", type = IdType.AUTO)
    private String id;

    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板内容
     */
    private String templateContent;
    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date  createTime;

}
