package com.tce.smart.platform.client.supplier;

import com.tce.smart.platform.core.client.supplier.*;
import java.time.Clock;
import java.util.List;
import java.util.Set;

/** 业务接入的持久化边界；正式配置仅使用JdbcSupplierAccessRepository。 */
public interface SupplierAccessRepository {
    SupplierVerification verifyOrInitialize(SupplierQualificationSnapshot qualification, SupplierOperator operator,
            SupplierPostAreaMapping postArea, Clock clock, String verificationId);
    SupplierVerification findVerification(String verificationId);
    List<SupplierPassageEvent> listEvents(Set<String> authorizedPostIds, int limit);
    SupplierPassageResult record(String scopeId, String idempotencyKey, String verificationId,
            SupplierQualificationSnapshot currentQualification, SupplierOperator currentOperator,
            SupplierPostAreaMapping currentPostArea, SupplierDirection direction, Clock clock, String eventId);
}
