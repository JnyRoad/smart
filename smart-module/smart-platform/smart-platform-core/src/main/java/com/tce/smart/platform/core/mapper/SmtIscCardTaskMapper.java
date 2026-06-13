package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.isc.IscCardTaskPageReqDTO;
import com.tce.smart.platform.core.entity.SmtIscCardTask;
import com.tce.smart.platform.core.vo.IscCardTaskPageVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface SmtIscCardTaskMapper extends BaseMapper<SmtIscCardTask> {

	IPage<SmtIscCardTask> getPendingTasks(Page page, @Param("currentTime") long currentTime,
										 @Param("maxRetryTimes") int maxRetryTimes);

	IPage<IscCardTaskPageVO> getPage(Page page, @Param("query") IscCardTaskPageReqDTO query);

	String getLatestPersonId(@Param("badge") String badge, @Param("parkId") Integer parkId,
							 @Param("cardNo") String cardNo);

	int releaseExpiredRunningTask(@Param("runningKey") String runningKey,
								  @Param("currentTime") long currentTime,
								  @Param("updateTime") LocalDateTime updateTime);

	int markDoing(@Param("id") Long id, @Param("currentTime") long currentTime,
				  @Param("doingDeadlineTime") long doingDeadlineTime,
				  @Param("leaseToken") String leaseToken,
				  @Param("runningKey") String runningKey,
				  @Param("updateTime") LocalDateTime updateTime,
				  @Param("maxRetryTimes") int maxRetryTimes);
}
