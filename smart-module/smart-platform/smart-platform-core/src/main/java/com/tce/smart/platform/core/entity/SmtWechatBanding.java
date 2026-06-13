package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 微信绑定表
 *
 * @author fushiping
 * @date 2021-10-09 17:20:23
 */
@Data
@TableName("smt_wechat_banding")
@EqualsAndHashCode(callSuper = true)
public class SmtWechatBanding extends Model<SmtWechatBanding> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * OPEN_ID
   */
    private String openId;
    /**
   * UNION_ID
   */
    private String unionId;
    /**
   * 工号
   */
    private String badge;
	/**
	 * 园区ID
	 */
    private Integer parkId;
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
