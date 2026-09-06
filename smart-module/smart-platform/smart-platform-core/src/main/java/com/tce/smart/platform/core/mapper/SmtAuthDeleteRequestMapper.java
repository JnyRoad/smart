package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtAuthDeleteRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限删除来源请求数据访问接口。
 */
public interface SmtAuthDeleteRequestMapper extends BaseMapper<SmtAuthDeleteRequest> {

	List<SmtAuthDeleteRequest> selectByBatchIdAndIds(@Param("batchId") Long batchId,
			@Param("parkId") Integer parkId, @Param("requestIds") List<Long> requestIds);
}
