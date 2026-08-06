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
	List<SmtEnergyProjectionQueue> selectCandidates(@Param("limit") int limit, @Param("now") LocalDateTime now);
	int claim(@Param("id") Long id, @Param("expectedRequestCount") Integer expectedRequestCount,
			  @Param("claimedAt") LocalDateTime claimedAt, @Param("leaseExpiresAt") LocalDateTime leaseExpiresAt, @Param("leaseToken") String leaseToken);
	int finish(@Param("id") Long id, @Param("expectedRequestCount") Integer expectedRequestCount, @Param("status") String status,
			   @Param("processedAt") LocalDateTime processedAt, @Param("lastError") String lastError, @Param("leaseToken") String leaseToken);
	Long verifyCurrentLeaseForUpdate(@Param("id") Long id, @Param("expectedRequestCount") Integer expectedRequestCount, @Param("leaseToken") String leaseToken);
	int failOrRetry(@Param("id") Long id, @Param("expectedRequestCount") Integer expectedRequestCount, @Param("leaseToken") String leaseToken,
					@Param("failedAt") LocalDateTime failedAt, @Param("nextRetryAt") LocalDateTime nextRetryAt, @Param("maxRetryCount") int maxRetryCount, @Param("lastError") String lastError);
}
