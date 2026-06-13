package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 园区BU关系表
 *
 * @author mckaywu
 * @date 2019-11-20 10:35:16
 */
@Data
@TableName("smt_security_bu")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityBu extends Model<SmtSecurityBu> {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 园区编号
   */
    private Integer parkId;
    /**
   * BU编号
   */
    private String compId;
	/**
	 * 权限策略id
	 */
	private Integer securityId;
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
