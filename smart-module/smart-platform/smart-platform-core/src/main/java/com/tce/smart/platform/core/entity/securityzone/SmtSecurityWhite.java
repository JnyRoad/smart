package com.tce.smart.platform.core.entity.securityzone;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("smt_security_white")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityWhite extends Model<SmtSecurityWhite> {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
    /**
   * 员工id
   */
    private Long staffId;
    /**
   * 配置id
   */
    private Long deleteConfigId;
    /**
   * 员工工号
   */
    private String staffBadge;
    /**
   * 员工姓名
   */
    private String staffName;
	/**
	 * 园区id
	 */
    private Integer parkId;
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
