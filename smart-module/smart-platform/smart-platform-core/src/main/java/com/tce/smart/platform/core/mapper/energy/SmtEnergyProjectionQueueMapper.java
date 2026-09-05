package com.tce.smart.platform.core.mapper.energy;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.energy.SmtEnergyProjectionQueue;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 能耗日投影请求队列访问接口。
 */
public interface SmtEnergyProjectionQueueMapper extends BaseMapper<SmtEnergyProjectionQueue> {
	int insertIfAbsent(@Param("id") Long id, @Param("meterSource") String meterSource,
				 @Param("meterId") Long meterId, @Param("statDate") LocalDate statDate, @Param("requestedAt") LocalDateTime requestedAt);
	int requeueExisting(@Param("meterSource") String meterSource, @Param("meterId") Long meterId, @Param("statDate") LocalDate statDate, @Param("requestedAt") LocalDateTime requestedAt);
	/** 补齐只重启终态请求，不能清除活跃请求的租约、重试计数或延迟。 */
	int requeueIdle(@Param("meterSource") String meterSource, @Param("meterId") Long meterId, @Param("statDate") LocalDate statDate, @Param("requestedAt") LocalDateTime requestedAt);
	/** 检查活跃请求，以便跳过无需重复读取源数据的补齐项。 */
	int countActiveRequest(@Param("meterSource") String meterSource, @Param("meterId") Long meterId, @Param("statDate") LocalDate statDate);
	/** 按业务日期区间取有界候选，空边界表示不限制该方向。 */
	List<SmtEnergyProjectionQueue> selectCandidatesByDate(@Param("limit") int limit, @Param("now") LocalDateTime now,
			@Param("fromDate") LocalDate fromDate, @Param("beforeDate") LocalDate beforeDate);
	List<SmtEnergyProjectionQueue> selectCandidates(@Param("limit") int limit, @Param("now") LocalDateTime now);
	int claim(@Param("id") Long id, @Param("expectedRequestCount") Integer expectedRequestCount,
			  @Param("claimedAt") LocalDateTime claimedAt, @Param("leaseExpiresAt") LocalDateTime leaseExpiresAt, @Param("leaseToken") String leaseToken);
	int finish(@Param("id") Long id, @Param("expectedRequestCount") Integer expectedRequestCount, @Param("status") String status,
			   @Param("processedAt") LocalDateTime processedAt, @Param("lastError") String lastError, @Param("leaseToken") String leaseToken);
	Long verifyCurrentLeaseForUpdate(@Param("id") Long id, @Param("expectedRequestCount") Integer expectedRequestCount, @Param("leaseToken") String leaseToken);
	int failOrRetry(@Param("id") Long id, @Param("expectedRequestCount") Integer expectedRequestCount, @Param("leaseToken") String leaseToken,
					@Param("failedAt") LocalDateTime failedAt, @Param("nextRetryAt") LocalDateTime nextRetryAt, @Param("maxRetryCount") int maxRetryCount, @Param("lastError") String lastError);
}
