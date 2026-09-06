package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.core.entity.SmtAuthSourceCoord;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 权限版本协调的有界查询与短事务行锁。 */
public interface SmtAuthSourceCoordMapper {
    SmtAuthSourceCoord identity(@Param("id") String id);
    SmtAuthSourceCoord selectById(@Param("id") String id);
    SmtAuthSourceCoord lock(@Param("id") String id);
    int insert(SmtAuthSourceCoord row);
    int update(SmtAuthSourceCoord row);
    List<SmtAuthSourceCoord> pending(@Param("parkId") int parkId, @Param("after") String after, @Param("limit") int limit);
}
