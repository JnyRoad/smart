package com.tce.smart.platform.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 职工出差申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@Data
@TableName("smt_travel_application")
@EqualsAndHashCode(callSuper = true)
public class SmtTravelApplication extends Model<SmtTravelApplication> {
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
    private Date startTime;
    /**
   *
   */
    private Date endTime;
    /**
   * 加班时长
   */
    private Integer duration;
    /**
   * 原因
   */
    private String cause;
    /**
   * 流程编号
   */
    private String processId;
    /**
   * 创建时间
   */
    private Date createTime;
    /**
   *
   */
    private String travleLocal;

}
