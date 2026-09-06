package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtAuthResultEvent;
import org.apache.ibatis.annotations.Param;

/**
 * 权限结果证据数据访问接口。
 */
public interface SmtAuthResultEventMapper extends BaseMapper<SmtAuthResultEvent> {

	SmtAuthResultEvent selectByAttemptAndEventKey(@Param("attemptId") Long attemptId,
			@Param("eventKey") String eventKey);
}
