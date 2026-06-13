package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *
 * @author fushiping
 * @date 2020-08-05 18:22:56
 */
@Data
@TableName("SMT_JCHE_AUTH")
@EqualsAndHashCode(callSuper = true)
public class SmtJcheAuth extends Model<SmtJcheAuth> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(type = IdType.ID_WORKER)
    private Long id;
    /**
   * 业务code
   */
    private Integer businessCode;
    /**
   * 权限配置id
   */
    private Integer jcheId;
    /**
   * 园区id
   */
    private Integer parkId;

}
