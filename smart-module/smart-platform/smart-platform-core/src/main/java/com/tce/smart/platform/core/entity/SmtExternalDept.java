package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 外部部门
 *
 * @author
 * @date 2019-04-15 11:34:58
 */
@Data
@TableName("SMT_EXTERNAL_DEPT")
@EqualsAndHashCode(callSuper = true)
public class SmtExternalDept extends Model<SmtExternalDept> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
	@TableId(type = IdType.ID_WORKER)
    private Long id;
    /**
   * 部门名
   */
    private String deptName ;
    /**
   * 上级部门id
   */
    private Long parentDept;
	/**
	 * 主管姓名
	 */
    private String directorName;
	/**
	 * 上级bu
	 */
    private Long compId;
	/**
	 * 主管工号
	 */
    private String director;
	/**
	 * 逻辑删除
	 */
/*    @TableLogic
    private Integer isDelete;*/
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
