package com.tce.smart.platform.core.entity.badge;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 厂牌领取设置
 *
 * @author fushiping
 * @date 2020-07-07 11:47:51
 */
@Data
@TableName("SMT_BADGE_CONFIG")
@EqualsAndHashCode(callSuper = true)
public class SmtBadgeConfig extends Model<SmtBadgeConfig> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
    @TableId(type = IdType.ID_WORKER)
    private Long id;
    /**
   * 价格
   */
    private BigDecimal price;
    /**
   * 园区
   */
    private Integer parkId;
    /**
   * 园区名
   */
    private String parkName;
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
   * 创建人ID
   */
    private Integer createrId;
    /**
   * 备用字段
   */
    private String blank;

}
