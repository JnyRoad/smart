package com.tce.smart.bridge.isc.core.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.bridge.isc.core.entity.ExceptionLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 园区异常消息表 Mapper 接口
 * </p>
 */
public interface ExceptionLogMapper extends BaseMapper<ExceptionLog> {
	List<ExceptionLog> getList(@Param("id") Integer id, @Param("size") Integer size);
}
