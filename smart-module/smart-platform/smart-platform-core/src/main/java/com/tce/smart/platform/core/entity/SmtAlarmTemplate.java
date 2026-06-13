package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import cn.hutool.core.date.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 警报模板
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@Data
@TableName("smt_alarm_template")
@EqualsAndHashCode(callSuper = true)
public class SmtAlarmTemplate extends Model<SmtAlarmTemplate> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
   * 标题
   */
    private String title;
    /**
   * 内容
   */
    private String content;
    /**
   * 创建时间
   */
    private DateTime createTime;

}
