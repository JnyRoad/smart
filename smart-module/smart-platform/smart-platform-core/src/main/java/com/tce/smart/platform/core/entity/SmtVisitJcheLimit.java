package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2020-08-06 15:30:50
 */
@Data
@TableName("smt_visit_jche_limit")
@EqualsAndHashCode(callSuper = true)
public class SmtVisitJcheLimit extends Model<SmtVisitJcheLimit> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(type = IdType.ID_WORKER)
    private Long id;
    /**
   * 园区id
   */
    private Integer parkId;
    /**
   * 级层id
   */
    private String jcheId;
    /**
   * 级层描述
   */
    private String jcheDesc;
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

	/**
	 * 限制类型  1:访客预约   2:入厂申请
	 */
	private Integer limitType;

}
