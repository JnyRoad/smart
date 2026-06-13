package com.tce.smart.platform.core.entity.leavecount;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@AllArgsConstructor
@TableName("smt_settlement_template_jche")
@EqualsAndHashCode(callSuper = true)
public class SmtSettlementTemplateJche extends Model<SmtSettlementTemplateJche> {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 级层id
   */
    private String jcheId;
    /**
   * 级层名
   */
    private String jcheName;
    /**
   * 项ID
   */
    private Long itemId;

}
