package com.tce.smart.platform.core.entity.leavecount;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:02:04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("smt_settlement_log")
@EqualsAndHashCode(callSuper = true)
public class SmtSettlementLog extends Model<SmtSettlementLog> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 调用事务id
   */
    private Long infoId;
    /**
   * 调用方名称
   */
    private String requestName;
    /**
   * 调用方IP
   */
    private String requestIp;
    /**
   * 调用报文
   */
    private String requestLog;
    /**
   * 调用时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime requestTime;
    /**
   * 响应状态
   */
    private String responseStatus;
    /**
   * 响应描述
   */
    private String responseDesc;
    /**
   * 响应报文
   */
    private String responseLog;
    /**
   * 响应时间
   */
    private LocalDateTime responseTime;

}
