package com.tce.smart.platform.core.mapper;
import com.tce.smart.platform.core.entity.*;
import org.apache.ibatis.annotations.Param;
import java.util.*;
/** 通用投影持久接口。既有方法的 SQL 保留员工兼容命名空间，由其子接口代理执行。 */
public interface AuthSelectionMapper {
 List<SmtAuthSelectionSource> sourcesForTarget(@Param("targetId") Long targetId);
 List<SmtAuthSelectionResource> resourcesForTarget(@Param("targetId") Long targetId);
 int markVerification(@Param("batch") Long batch,@Param("reason") String reason);
 String verificationReason(@Param("batch") Long batch);
 int sourceVerificationCount(@Param("batch") Long batch);
 List<SmtAuthSelectionSource> verificationSources(@Param("batch") Long batch,@Param("after") long after,@Param("limit") int limit);
 List<Long> pendingExpansionBatches(@Param("parks") List<Integer> parks,@Param("after") Long after,@Param("limit") int limit);
 List<SmtAuthSelectionSource> operation(@Param("key") String key);
 int insertSources(@Param("rows") List<SmtAuthSelectionSource> rows);
 int insertResources(@Param("rows") List<SmtAuthSelectionResource> rows);
 SmtAuthSelectionSource source(@Param("batch") Long batch,@Param("ordinal") long ordinal);
 List<SmtAuthSelectionResource> resources(@Param("batch") Long batch,@Param("after") long after,@Param("limit") int limit);
 int bindSource(@Param("batch") Long batch,@Param("ordinal") long ordinal,@Param("source") String source,@Param("generation") long generation);
 int bindResource(@Param("batch") Long batch,@Param("ordinal") long ordinal,@Param("resource") String resource);
 List<String> lanes(@Param("batch") Long batch,@Param("after") String after,@Param("limit") int limit);
 long resourceCount(@Param("batch") Long batch);
 long selectionCursor(@Param("batch") Long batch);
 long cursor(@Param("batch") Long batch);
 SmtAuthSelectionSource exactSource(@Param("source") String source,@Param("generation") long generation);
 int complete(@Param("batch") Long batch,@Param("ordinal") long ordinal);
 int unboundSelectionCount(@Param("batch") Long batch);
 int pendingTypedSubject(@Param("park") int park,@Param("subjectType") String subjectType,@Param("subject") String subject);
}
