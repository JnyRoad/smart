package com.tce.smart.platform.core.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 职工调休申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:30:08
 */
@Data
@TableName("smt_breakoff_application")
@EqualsAndHashCode(callSuper = true)
public class SmtBreakoffApplication extends Model<SmtBreakoffApplication> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
   *
   */
    private Long staffId;
    /**
   *
   */
    private String staffBadge;
    /**
   *
   */
    private String staffName;
    /**
   *
   */
    private Date workTime;
    /**
   *
   */
    private Date restTime;
    /**
   * 现在要调休天数
   */
    private String restCount;
    /**
     * 可调休天数
     */
    private String restAbleCount;
    /**
   * 流程编号
   */
    private String processId;
    /**
   * 创建时间
   */
    private Date createTime;

    /**
     * 调休类型
     */
    private Integer type;
    /**
     * 调休原因
     */
    private String cause;

}
