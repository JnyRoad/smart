package com.tce.smart.platform.core.entity.securityzone;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:00
 */
@Data
@TableName("smt_security_person_relation")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityPersonRelation extends Model<SmtSecurityPersonRelation> {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
    /**
   * 保密区id
   */
    private Long securityId;
    /**
   * 关联人员id
   */
    private Long staffId;
    /**
   * 人员工号
   */
    private String staffBadge;
    /**
   * 人员姓名
   */
    private String staffName;

    private Integer parkId;

	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
