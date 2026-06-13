package com.tce.smart.platform.core.entity.badge;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 厂牌补领
 *
 * @author fushiping
 * @date 2020-07-07 11:47:58
 */
@Data
@TableName("SMT_BADGE_APPLY")
@EqualsAndHashCode(callSuper = true)
public class SmtBadgeApply extends Model<SmtBadgeApply> {
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
   * 申请时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
   * 申请原因
   */
    private Integer reason;
    /**
   * 办理状态
   */
    private Integer state;
    /**
   * 领取地址
   */
    private String address;
	/**
	 * BUId
	 */
	private String compId;
	/**
	 * 部门ID
	 */
	private String depId;
    /**
   * 备注
   */
    private String remark;
	/**
	 * 价格
	 */
    private BigDecimal price;
    /**
   * 备用字段
   */
    private String blank;
	/**
	 * 拒绝原因
	 */
    private String refuseReason;

}
