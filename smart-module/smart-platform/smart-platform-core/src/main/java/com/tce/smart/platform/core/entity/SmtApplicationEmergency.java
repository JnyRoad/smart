package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 招聘者紧急联系人
 *
 * @author 齐佩
 * @date 2019-04-19 14:37:15
 */
@Data
@TableName("smt_application_emergency")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SmtApplicationEmergency extends Model<SmtApplicationEmergency> {
    private static final long serialVersionUID = 8773456993848271129L;

    /**
   *
   */
    @TableId
    private Integer id;
    /**
   * 招聘者ID
   */
    private Long applicationId;
    /**
   * 联系人关系
   */
    @NotBlank(message = "联系人关系不能为空")
    private String relation;
    /**
   * 联系人姓名
   */
    @NotBlank(message = "联系人姓名不能为空")
    private String emergencyName;
    /**
   * 联系人电话
   */
    @NotBlank(message = "联系人电话不能为空")
    private String telephont;
}
