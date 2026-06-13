package com.tce.smart.platform.core.entity.leavecount;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:50
 */
@Data
@TableName("smt_settlement_template_range")
@EqualsAndHashCode(callSuper = true)
public class SmtSettlementTemplateRange extends Model<SmtSettlementTemplateRange> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 模板id
   */
    private Long tempId;
    /**
   * 范围类型 1 房间 2 bu
   */
    private Integer type;
    /**
   * 值
   */
    private String value;
    /**
   * 园区id
   */
    private Integer parkId;

}
