package com.tce.smart.platform.api.dto;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 职工调休申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:30:08
 */
@Data
public class SmtBreakoffApplicationDTO implements Serializable {
private static final long serialVersionUID = 1L;

    /**
   *
   */
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
