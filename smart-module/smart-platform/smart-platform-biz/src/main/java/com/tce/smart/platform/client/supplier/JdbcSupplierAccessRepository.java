package com.tce.smart.platform.client.supplier;

import com.tce.smart.platform.core.client.supplier.*;
import java.time.Clock;
import java.util.List;
import java.util.Set;

/** 委托已有事务仓储，幂等、一次性核验和人员区域CAS保持在同一事务中。 */
public final class JdbcSupplierAccessRepository implements SupplierAccessRepository {
    private final JdbcSupplierAccessStore store;
    public JdbcSupplierAccessRepository(JdbcSupplierAccessStore store) { this.store = store; }
    @Override public SupplierVerification verifyOrInitialize(SupplierQualificationSnapshot qualification, SupplierOperator operator,
            SupplierPostAreaMapping postArea, Clock clock, String verificationId) {
        return store.verifyOrInitializeAtCurrentTime(qualification, operator, postArea, clock, verificationId);
    }
    @Override public SupplierVerification findVerification(String verificationId) { return store.findVerification(verificationId); }
    @Override public List<SupplierPassageEvent> listEvents(Set<String> authorizedPostIds, int limit) { return store.listEvents(authorizedPostIds, limit); }
    @Override public SupplierPassageResult record(String scopeId, String idempotencyKey, String verificationId,
            SupplierQualificationSnapshot currentQualification, SupplierOperator currentOperator,
            SupplierPostAreaMapping currentPostArea, SupplierDirection direction, Clock clock, String eventId) {
        return store.recordAtCurrentTime(scopeId, idempotencyKey, verificationId, currentQualification, currentOperator, currentPostArea, direction, clock, eventId);
    }
}
