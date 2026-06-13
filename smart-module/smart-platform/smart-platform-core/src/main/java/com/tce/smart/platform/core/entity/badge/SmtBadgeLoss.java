package com.tce.smart.platform.core.entity.badge;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 厂牌挂失
 *
 * @author fushiping
 * @date 2020-07-07 11:47:43
 */
@Data
@TableName("SMT_BADGE_LOSS")
@EqualsAndHashCode(callSuper = true)
public class SmtBadgeLoss extends Model<SmtBadgeLoss> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
	@TableId(type = IdType.ID_WORKER)
	private Long id;
    /**
   * 员工工号
   */
    private String badge;
    /**
   * 员工姓名
   */
    private String name;
    /**
   * BU名
   */
    private String compName;
    /**
   * 部门名
   */
    private String depName;
	/**
	 * 园区
	 */
	private Integer parkId;
    /**
   * 园区名
   */
    private String parkName;
	/**
	 * BUId
	 */
	private String compId;
	/**
	 * 部门ID
	 */
	private String depId;
    /**
   * 挂失时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
   * 备用字段
   */
    private String blank;

}
