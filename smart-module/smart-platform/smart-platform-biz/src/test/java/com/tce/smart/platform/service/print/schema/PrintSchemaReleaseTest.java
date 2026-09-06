package com.tce.smart.platform.service.print.schema;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.UUID;

import static org.junit.Assert.*;

/** 内存 H2 Oracle 模式只覆盖发布状态和内部 SQL；不证明 Oracle 生产兼容。 */
public class PrintSchemaReleaseTest {
    private Connection db;
    private final PrintSchemaRelease release = new PrintSchemaRelease();

    @Before public void open() throws Exception {
        db = DriverManager.getConnection("jdbc:h2:mem:print_release_" + UUID.randomUUID() + ";MODE=Oracle", "sa", "");
    }

    @After public void close() throws Exception { db.close(); }

    @Test public void inspectionOfEmptySchemaNeverCreatesReleaseLedgerOrBusinessTables() throws Exception {
        assertEquals("ABSENT", release.inspect(db, "PUBLIC"));
        assertEquals(0, tableCount());
    }

    @Test public void explicitReleaseCreatesFourteenBusinessTablesAndOneLedgerAndIsRepeatable() throws Exception {
        assertEquals("APPLIED", release.apply(db, "PUBLIC", "test-release-001"));
        assertEquals(15, tableCount());
        assertEquals("APPLIED", release.inspect(db, "PUBLIC"));
        assertEquals("APPLIED", release.apply(db, "PUBLIC", "test-release-001"));
        assertEquals(1, scalar("SELECT COUNT(*) FROM SMT_PRINT_SCHEMA_RELEASE"));
        assertEquals(14, scalar("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME LIKE 'SMT_PRINT_%' AND TABLE_NAME <> 'SMT_PRINT_SCHEMA_RELEASE'"));
    }

    @Test public void wrongSchemaAndInvalidReleaseIdFailBeforeFirstWrite() throws Exception {
        rejected(() -> release.apply(db, "OTHER", "test-release-001"));
        rejected(() -> release.apply(db, "PUBLIC", ""));
        rejected(() -> release.apply(db, "PUBLIC", "release\nsecret"));
        assertEquals(0, tableCount());
    }

    @Test public void untrackedExistingTableIsNeverAdoptedOrChanged() throws Exception {
        sql("CREATE TABLE SMT_PRINT_JOB (KEEP_ME VARCHAR(20))");
        sql("INSERT INTO SMT_PRINT_JOB VALUES ('evidence')");
        rejected(() -> release.inspect(db, "PUBLIC"));
        rejected(() -> release.apply(db, "PUBLIC", "test-release-001"));
        assertEquals(1, tableCount());
        assertEquals(1, scalar("SELECT COUNT(*) FROM SMT_PRINT_JOB WHERE KEEP_ME='evidence'"));
    }

    @Test public void anUnknownLedgerIsNeverClaimedEvenWhenEmpty() throws Exception {
        sql("CREATE TABLE SMT_PRINT_SCHEMA_RELEASE (KEEP_ME VARCHAR(20))");
        rejected(() -> release.apply(db, "PUBLIC", "test-release-001"));
        assertEquals(1, tableCount());
    }

    @Test public void appliedReleaseMustMatchChecksumAndOriginalReleaseIdentity() throws Exception {
        release.apply(db, "PUBLIC", "test-release-001");
        rejected(() -> release.apply(db, "PUBLIC", "different-release"));
        sql("UPDATE SMT_PRINT_SCHEMA_RELEASE SET CHECKSUM='tampered'");
        rejected(() -> release.inspect(db, "PUBLIC"));
    }

    @Test public void schemaDriftIsRejectedWithoutRepairingOrRemovingEvidence() throws Exception {
        release.apply(db, "PUBLIC", "test-release-001");
        sql("ALTER TABLE SMT_PRINT_OPERATION ALTER COLUMN PRINCIPAL_ID VARCHAR(64)");
        rejected(() -> release.inspect(db, "PUBLIC"));
        rejected(() -> release.apply(db, "PUBLIC", "test-release-001"));
        assertEquals(15, tableCount());
    }

    @Test public void missingTableAfterSuccessfulReleaseIsNotAutomaticallyRecreated() throws Exception {
        release.apply(db, "PUBLIC", "test-release-001");
        sql("DROP TABLE SMT_PRINT_PREVIEW");
        rejected(() -> release.apply(db, "PUBLIC", "test-release-001"));
        assertEquals(14, tableCount());
    }

    @Test public void releaseSupportsDevicePrincipalAndRejectsDuplicateOperationAndCommand() throws Exception {
        release.apply(db, "PUBLIC", "test-release-001");
        String principal = "device:" + repeat('d', 128);
        try (PreparedStatement s = db.prepareStatement("INSERT INTO SMT_PRINT_OPERATION (OPERATION_ID, PRINCIPAL_ID, IDEMPOTENCY_KEY) VALUES (?,?,?)")) {
            s.setString(1, UUID.randomUUID().toString()); s.setString(2, principal); s.setString(3, repeat('k', 128)); s.executeUpdate();
            s.setString(1, UUID.randomUUID().toString()); rejected(s::executeUpdate);
        }
        sql("INSERT INTO SMT_PRINT_ATTEMPT (ATTEMPT_ID,JOB_ID,COMMAND_ID,ATTEMPT_NO) VALUES ('a','j','c',1)");
        rejected(() -> sql("INSERT INTO SMT_PRINT_ATTEMPT (ATTEMPT_ID,JOB_ID,COMMAND_ID,ATTEMPT_NO) VALUES ('b','j2','c',1)"));
        rejected(() -> sql("INSERT INTO SMT_PRINT_ATTEMPT (ATTEMPT_ID,JOB_ID,COMMAND_ID,ATTEMPT_NO) VALUES ('b','j','d',1)"));
        sql("INSERT INTO SMT_PRINT_JOB (JOB_ID) VALUES ('j1')");
        sql("INSERT INTO SMT_PRINT_JOB (JOB_ID) VALUES ('j2')");
    }

    @Test public void failedDdlKeepsPriorTablesAndFailedLedgerAndRefusesAutomaticRetry() throws Exception {
        Connection failing = failOnBusinessDdl(db);
        rejected(() -> release.apply(failing, "PUBLIC", "test-release-001"));
        assertEquals("FAILED", string("SELECT STATUS FROM SMT_PRINT_SCHEMA_RELEASE"));
        assertTrue(tableCount() > 1);
        int count = tableCount();
        rejected(() -> release.apply(db, "PUBLIC", "test-release-001"));
        assertEquals(count, tableCount());
    }

    @Test public void interruptedReleaseIsNotTreatedAsAppliedOrAutomaticallyResumed() throws Exception {
        release.apply(db, "PUBLIC", "test-release-001");
        sql("UPDATE SMT_PRINT_SCHEMA_RELEASE SET STATUS='STARTED', FINISHED_AT=NULL");
        rejected(() -> release.inspect(db, "PUBLIC"));
        rejected(() -> release.apply(db, "PUBLIC", "test-release-001"));
        assertEquals("STARTED", string("SELECT STATUS FROM SMT_PRINT_SCHEMA_RELEASE"));
    }

    @Test public void missingUniqueConstraintAndMissingOperationalIndexAreRejected() throws Exception {
        release.apply(db, "PUBLIC", "test-release-001");
        sql("ALTER TABLE SMT_PRINT_OPERATION DROP CONSTRAINT UK_PRT_OPERATION_KEY");
        rejected(() -> release.inspect(db, "PUBLIC"));
    }

    @Test public void droppedQueueIndexIsDetectedByReadOnlyValidation() throws Exception {
        release.apply(db, "PUBLIC", "test-release-001");
        sql("DROP INDEX IX_PRT_JOB_QUEUE");
        rejected(() -> release.inspect(db, "PUBLIC"));
    }

    @Test public void publisherNeverCommitsAnExistingCallerTransaction() throws Exception {
        db.setAutoCommit(false);
        rejected(() -> release.apply(db, "PUBLIC", "test-release-001"));
        assertEquals(0, tableCount());
        assertFalse(db.getAutoCommit());
        db.rollback();
    }

    @Test public void indexNameCollisionFailsBeforeCreatingLedger() throws Exception {
        sql("CREATE TABLE UNRELATED (ID INT)");
        sql("CREATE INDEX IX_PRT_JOB_QUEUE ON UNRELATED (ID)");
        rejected(() -> release.apply(db, "PUBLIC", "test-release-001"));
        assertEquals(1, tableCount());
    }

    @Test public void chineseTextLargeClobBinaryAndCanonicalHashSurviveReleasedSchema() throws Exception {
        release.apply(db, "PUBLIC", "test-release-001");
        String name=repeat('旅', 100), content=repeat('途', 5000), hash="sha256:"+repeat('a',64);
        byte[] bytes=new byte[]{0,1,2,-1,-128};
        try (PreparedStatement s=db.prepareStatement("INSERT INTO SMT_PRINT_TEMPLATE (TEMPLATE_ID,NAME) VALUES (?,?)")) {
            s.setString(1,UUID.randomUUID().toString()); s.setString(2,name); s.executeUpdate();
        }
        try (PreparedStatement s=db.prepareStatement("INSERT INTO SMT_PRINT_JOB (JOB_ID,SNAPSHOT_JSON,TEMPLATE_SNAPSHOT_HASH) VALUES (?,?,?)")) {
            s.setString(1,"j"); s.setString(2,content); s.setString(3,hash); s.executeUpdate();
        }
        try (PreparedStatement s=db.prepareStatement("INSERT INTO SMT_PRINT_JOB_ARTIFACT (ARTIFACT_ID,CONTENT_HASH,CONTENT_BYTES) VALUES (?,?,?)")) {
            s.setString(1,"a"); s.setString(2,hash); s.setBytes(3,bytes); s.executeUpdate();
        }
        assertEquals(name,string("SELECT NAME FROM SMT_PRINT_TEMPLATE"));
        assertEquals(content,string("SELECT SNAPSHOT_JSON FROM SMT_PRINT_JOB"));
        assertEquals(hash,string("SELECT TEMPLATE_SNAPSHOT_HASH FROM SMT_PRINT_JOB"));
        try (Statement s=db.createStatement(); ResultSet r=s.executeQuery("SELECT CONTENT_BYTES FROM SMT_PRINT_JOB_ARTIFACT")) { assertTrue(r.next()); assertArrayEquals(bytes,r.getBytes(1)); }
        assertEquals("APPLIED",new PrintSchemaRelease().inspect(db,"PUBLIC"));
    }

    @Test public void underscoreInSchemaMustNotMatchAnotherSchemasPrintObjects() throws Exception {
        sql("CREATE SCHEMA TEST_A"); sql("CREATE SCHEMA TESTXA");
        sql("CREATE TABLE TESTXA.SMT_PRINT_JOB (KEEP_ME VARCHAR(20))");
        sql("INSERT INTO TESTXA.SMT_PRINT_JOB VALUES ('evidence')");
        db.setSchema("TEST_A");
        assertEquals("APPLIED",release.apply(db,"TEST_A","schema-pattern-release"));
        assertEquals(1,scalar("SELECT COUNT(*) FROM TESTXA.SMT_PRINT_JOB WHERE KEEP_ME='evidence'"));
        assertEquals("APPLIED",release.inspect(db,"TEST_A"));
    }

    private Connection failOnBusinessDdl(Connection delegate) {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (proxy, method, args) -> {
            try {
                Object result = method.invoke(delegate, args);
                if (!"createStatement".equals(method.getName())) return result;
                Statement statement = (Statement) result;
                return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Statement.class}, (p, m, a) -> {
                    if ("execute".equals(m.getName()) && a[0].toString().startsWith("CREATE TABLE SMT_PRINT_TEMPLATE_VER ")) throw new SQLException("injected DDL failure", "TEST", 999);
                    try { return m.invoke(statement, a); } catch (InvocationTargetException e) { throw e.getCause(); }
                });
            } catch (InvocationTargetException e) { throw e.getCause(); }
        });
    }

    private int tableCount() throws Exception { return scalar("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'"); }
    private int scalar(String query) throws Exception { try (Statement s=db.createStatement(); ResultSet r=s.executeQuery(query)) { assertTrue(r.next()); return r.getInt(1); } }
    private String string(String query) throws Exception { try (Statement s=db.createStatement(); ResultSet r=s.executeQuery(query)) { assertTrue(r.next()); return r.getString(1); } }
    private void sql(String query) throws SQLException { try (Statement s=db.createStatement()) { s.execute(query); } }
    private static String repeat(char c, int count) { char[] chars=new char[count]; java.util.Arrays.fill(chars,c); return new String(chars); }
    private interface Checked { void run() throws Exception; }
    private static void rejected(Checked action) throws Exception { try { action.run(); fail("应拒绝存在歧义或缺少明确授权的发布"); } catch (SQLException | IllegalArgumentException expected) { assertNotNull(expected.getMessage()); } }
}
