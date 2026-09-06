package com.tce.smart.platform.core.mapper;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSchedulerData.*;
import com.tce.smart.platform.core.entity.SmtAuthSourceResource;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.*;

/** 实例行锁、公平候选和恢复游标均在本库完成。 */
public interface AuthOperationSchedulerMapper {
    LocalDateTime now();
    String windowKey();
    int insertState(@Param("id") String id);
    State lockState(@Param("id") String id);
    int saveState(State state);
    List<Route> routes(@Param("parks") List<Integer> parks,@Param("access") String access);
    int insertRoute(@Param("park") Integer park,@Param("access") String access,@Param("instance") String instance);
    String routeOwner(@Param("park") Integer park,@Param("access") String access);
    int insertJob(@Param("id") String id,@Param("lane") String lane);
    Job lockJob(@Param("id") String id,@Param("lane") String lane);
    int saveJob(Job job);
    List<Candidate> candidates(@Param("p") Policy policy,@Param("priority") String priority,
        @Param("after") String after,@Param("now") LocalDateTime now,@Param("limit") int limit);
    int deferSaturated(@Param("p") Policy policy,@Param("priority") String priority,@Param("globalBlocked") boolean globalBlocked,
        @Param("now") LocalDateTime now,@Param("retryAt") LocalDateTime retryAt,@Param("limit") int limit);
    List<Count> counts(@Param("p") Policy policy);
    List<Count> parkCounts(@Param("p") Policy policy);
    List<PhaseWork> phaseWork(@Param("p") Policy policy,@Param("lane") String lane,@Param("priority") String priority,@Param("after") String after,@Param("afterId") Long afterId,@Param("window") String window,@Param("cost") int cost);
    List<Long> phaseGroup(@Param("p") Policy policy,@Param("lane") String lane,@Param("id") Long id,@Param("limit") int limit);
    Integer quotaUsed(@Param("instance") String instance,@Param("park") Integer park,@Param("device") String device,@Param("kind") String kind,@Param("lane") String lane,@Param("window") String window);
    int saveQuota(@Param("instance") String instance,@Param("park") Integer park,@Param("device") String device,@Param("kind") String kind,@Param("lane") String lane,@Param("window") String window,@Param("used") int used);
    List<Long> expansionBatches(@Param("p") Policy policy,@Param("priority") String priority,@Param("after") Long after,@Param("limit") int limit);
    List<Job> jobs(@Param("id") String id);
    List<Long> expiredClaims(@Param("p") Policy policy,@Param("after") Long after,@Param("now") LocalDateTime now,@Param("limit") int limit);
    String lockTarget(@Param("id") Long id);
    int expireUnsubmittedAttempt(@Param("id") Long id,@Param("now") LocalDateTime now);
    int requeueExpiredTarget(@Param("id") Long id,@Param("now") LocalDateTime now);
    List<SmtAuthSourceResource> recoveries(@Param("p") Policy policy,@Param("after") String after,@Param("limit") int limit);
    List<SmtAuthSourceResource> convergences(@Param("p") Policy policy,@Param("after") String after,@Param("limit") int limit);
    List<Long> refreshTargets(@Param("p") Policy policy,@Param("after") Long after,@Param("limit") int limit);
}
