package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应聘者工作经验
 *
 * @author 齐佩
 * @date 2019-04-19 14:37:00
 */
@Data
@TableName("smt_application_email")
@EqualsAndHashCode(callSuper = true)
public class SmtApplicationEmail extends Model<SmtApplicationEmail> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
	@TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**

   * 应聘者ID
   */
    private Long applicationId;


    /**
     * 邮件地址
     */
    private String email;

}
