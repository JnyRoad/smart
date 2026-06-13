package com.tce.smart.platform.core.entity.leavecount;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:56
 */
@Data
@TableName("smt_settlement_template")
@EqualsAndHashCode(callSuper = true)
public class SmtSettlementTemplate extends Model<SmtSettlementTemplate> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 模板名称
   */
    private String templateName;
    /**
   * 所属园区id
   */
    private Integer parkId;
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
