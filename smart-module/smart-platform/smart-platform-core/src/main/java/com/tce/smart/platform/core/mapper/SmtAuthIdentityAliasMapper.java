package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.core.entity.SmtAuthIdentityAlias;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 权限版本协调的有界查询与短事务行锁。 */
public interface SmtAuthIdentityAliasMapper {
    SmtAuthIdentityAlias selectById(@Param("id") String id);
    int insert(SmtAuthIdentityAlias row);
    List<SmtAuthIdentityAlias> resolve(@Param("scope") com.tce.smart.platform.core.dto.authversion.AuthVersion.ResourceKey scope, @Param("kind") String kind, @Param("value") String value);
}
