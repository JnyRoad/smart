package com.tce.smart.platform.core.mapper;
import com.tce.smart.platform.core.entity.*;
import org.apache.ibatis.annotations.Param;
import java.util.*;
/** 接入阶段与旧任务互斥查询，所有外部号均限定接入实例和园区。 */
public interface AuthOperationTransportMapper {
 List<SmtAuthTransportPhase> prepared(@Param("park") int park,@Param("instance") String instance,@Param("priority") String priority,@Param("after") Long after,@Param("limit") int limit);
 List<SmtAuthTransportPhase> staleIntents(@Param("park") int park,@Param("instance") String instance,@Param("limit") int limit);
 int holdAttempt(@Param("p") SmtAuthTransportPhase p,@Param("reason") String reason);
 int hold(@Param("p") SmtAuthTransportPhase p,@Param("reason") String reason);
 int historicalExternal(@Param("p") SmtAuthTransportPhase p,@Param("external") String external);
 int associate(@Param("p") SmtAuthTransportPhase p,@Param("external") String external);
 int waiting(@Param("p") SmtAuthTransportPhase p);
 int updateIscTask(@Param("p") SmtAuthTransportPhase p,@Param("external") String external);
 int insert(SmtAuthTransportPhase phase);
 SmtAuthTransportPhase phase(@Param("attempt") Long attempt,@Param("phase") String phase);
 SmtAuthTransportPhase byId(@Param("id") Long id);
 List<SmtAuthTransportPhase> byTask(@Param("access") String access,@Param("task") String task);
 List<SmtAuthTransportPhase> scan(@Param("park") int park,@Param("instance") String instance,@Param("phase") String phase,@Param("state") String state,@Param("after") Long after,@Param("limit") int limit);
 List<SmtAuthTransportPhase> group(@Param("park") int park,@Param("instance") String instance,@Param("request") String request,@Param("phase") String phase);
 int transition(@Param("id") Long id,@Param("old") String old,@Param("state") String state,@Param("external") String external,@Param("error") String error);
 int start(@Param("id") Long id,@Param("request") String request,@Param("person") String person);
 int page(@Param("id") Long id,@Param("old") int old,@Param("next") int next);
 List<String> knownPersons(@Param("p") SmtAuthTransportPhase p);
 int personBinding(@Param("p") SmtAuthTransportPhase phase,@Param("operation") String operation,@Param("hash") String hash,@Param("org") String org,@Param("person") String person,@Param("proof") Long proof);
 int routeTableInstalled();
 int routeMatches(@Param("p") SmtAuthTransportPhase p);
 int conflictingPerson(@Param("p") SmtAuthTransportPhase p,@Param("person") String person);
 int claimPerson(@Param("p") SmtAuthTransportPhase p,@Param("person") String person);
 int ownsPerson(@Param("p") SmtAuthTransportPhase p,@Param("person") String person);
 int deferAttempt(@Param("c") com.tce.smart.platform.core.dto.authoperation.AuthOperationClaimedTarget claim);
 int deferTarget(@Param("c") com.tce.smart.platform.core.dto.authoperation.AuthOperationClaimedTarget claim);
 int historicSubject(@Param("park") int park,@Param("subject") String subject);
}
