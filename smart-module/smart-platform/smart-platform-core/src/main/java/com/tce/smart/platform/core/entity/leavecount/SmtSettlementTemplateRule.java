package com.tce.smart.platform.core.entity.leavecount;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:40
 */
@Data
@TableName("smt_settlement_template_rule")
@EqualsAndHashCode(callSuper = true)
public class SmtSettlementTemplateRule extends Model<SmtSettlementTemplateRule> {
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
	/**
	 * 项ID
	 */
	private Long itemId;
    /**
   * 收费项目ID 2 冷水 3 电
   */
    private Integer categoryId;
    /**
   * 月份
   */
    private Integer monthNum;
    /**
   * 标准用量
   */
    private Double standardQty;
    /**
   * 添加时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


}
