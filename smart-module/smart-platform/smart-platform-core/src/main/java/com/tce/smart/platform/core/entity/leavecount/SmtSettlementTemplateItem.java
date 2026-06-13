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
 * @date 2022-06-21 11:01:40
 */
@Data
@TableName("smt_settlement_template_item")
@EqualsAndHashCode(callSuper = true)
public class SmtSettlementTemplateItem extends Model<SmtSettlementTemplateItem> {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 模板ID
   */
    private Long tempId;


}
