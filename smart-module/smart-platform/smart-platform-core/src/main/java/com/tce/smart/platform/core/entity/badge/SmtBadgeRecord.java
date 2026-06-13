package com.tce.smart.platform.core.entity.badge;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 厂牌补领流程表
 *
 * @author fushiping
 * @date 2020-07-07 11:47:27
 */
@Data
@TableName("SMT_BADGE_RECORD")
@EqualsAndHashCode(callSuper = true)
public class SmtBadgeRecord extends Model<SmtBadgeRecord> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(type = IdType.ID_WORKER)
	private Long id;
    /**
   * 处理人ID
   */
    private Integer createrId;
    /**
   * 处理人账号
   */
    private String createrName;
    /**
   * 处理人角色
   */
    private String createRole;
    /**
   * 处理类型
   */
    private Integer operateType;
	/**
	 * 厂牌补领Id
	 */
    private Long applyId;
	/**
	 * 备注
	 */
    private String remark;
    /**
   * 处理时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
   * 备用字段
   */
    private String blank;

}
