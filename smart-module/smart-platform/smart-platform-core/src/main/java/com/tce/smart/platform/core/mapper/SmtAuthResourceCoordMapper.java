package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.core.entity.SmtAuthResourceCoord;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 权限版本协调的有界查询与短事务行锁。 */
public interface SmtAuthResourceCoordMapper {
    SmtAuthResourceCoord selectById(@Param("id") String id);
    SmtAuthResourceCoord lock(@Param("id") String id);
    int insert(SmtAuthResourceCoord row);
    int update(SmtAuthResourceCoord row);
}
