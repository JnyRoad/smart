package com.tce.smart.platform.core.entity.manage;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-27 10:45:36
 */
@Data
@TableName("SMT_EHR_SET_UP")
@EqualsAndHashCode(callSuper = true)
public class SmtEhrSetUp extends Model<SmtEhrSetUp> {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@TableId(type = IdType.ID_WORKER)
    private Long id;
    /**
   * 园区
   */
    private Integer parkId;
    /**
   * 园区名
   */
    private String parkName;
    /**
   * 签收截止类型
   */
    private Integer deadlineType;
    /**
   * 签收截止时间
   */
    private Integer deadline;
    /**
   * 签收策略
   */
    private Integer strategy;
    /**
   * 短信发送是否开启
   */
    private Integer isMessage;
    /**
   * 自动确认时间
   */
    private Integer delayLine;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;
    /**
   * 修改时间
   */
    private LocalDateTime updateTime;
    /**
   * 设置类型
   */
    private Integer setType;

}
