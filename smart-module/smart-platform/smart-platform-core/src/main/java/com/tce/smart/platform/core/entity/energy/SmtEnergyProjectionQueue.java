package com.tce.smart.platform.core.entity.energy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 能耗日投影请求队列，同一表计日可通过合并更新重复入队。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_ENERGY_PROJECTION_QUEUE")
@EqualsAndHashCode(callSuper = true)
public class SmtEnergyProjectionQueue extends Model<SmtEnergyProjectionQueue> {
	private static final long serialVersionUID = 1L;

	/** 队列主键。 */
	@TableId(value = "ID", type = IdType.ID_WORKER)
	private Long id;
	/** 表计来源，ELE 或 WATER。 */
	private String meterSource;
	/** 表计标识。 */
	private Long meterId;
	/** 待投影的业务日期。 */
	private LocalDate statDate;
	/** 队列处理状态。 */
	private String queueStatus;
	/** 合并后的请求次数。 */
	private Integer requestCount;
	/** 最近一次请求时间。 */
	private LocalDateTime lastRequestedAt;
	/** 最近一次处理完成时间。 */
	private LocalDateTime processedAt;
	/** 当前处理租约令牌，只有持有者才能提交处理结果。 */
	private String leaseToken;
	/** 当前事件后的失败重试次数。 */
	private Integer retryCount;
	/** 受控重试的最早可处理时间。 */
	private LocalDateTime nextRetryAt;
	/** 最近一次处理错误。 */
	private String lastError;
}
