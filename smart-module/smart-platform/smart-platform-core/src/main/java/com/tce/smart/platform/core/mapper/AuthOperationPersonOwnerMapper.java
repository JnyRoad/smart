package com.tce.smart.platform.core.mapper;
import com.tce.smart.platform.core.entity.*;import org.apache.ibatis.annotations.Param;import java.util.*;
/** 所有锁查询禁用一级缓存；调用方始终先锁owner，再取得主体/资源/phase锁。 */
public interface AuthOperationPersonOwnerMapper {
 SmtAuthPersonOwner lock(@Param("id") String id);
 SmtAuthPersonOwner lockByPhase(@Param("phase") Long phase);
 int insert(SmtAuthPersonOwner owner);
 int setPhase(@Param("id") String id,@Param("phase") Long phase);
 int start(@Param("id") String id,@Param("phase") Long phase,@Param("token") String token,@Param("request") String request);
 int accepted(@Param("id") String id,@Param("phase") Long phase,@Param("token") String token,@Param("request") String request,@Param("person") String person);
 int mark(@Param("id") String id,@Param("old") String old,@Param("state") String state,@Param("reason") String reason);
 List<SmtAuthTransportPhase> history(@Param("p") SmtAuthTransportPhase phase);
 List<SmtAuthPersonOwner> reviews(@Param("instance") String instance,@Param("after") String after,@Param("limit") int limit);
}
