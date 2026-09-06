package com.tce.smart.platform.service.print.schema;

import org.junit.Test;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

/** 命令行未满足全部确认条件时，在解析阶段拒绝，不能进入 JDBC 写入。 */
public class PrintSchemaCliTest {
    @Test public void defaultModeIsReadOnlyAndApplyRequiresExactVersionAndChecksum() {
        assertEquals("validate",PrintSchemaReleaseCli.Options.parse(new String[]{"--expected-schema","TECH_PLATFORM"}).mode);
        reject("--apply","--expected-schema","TECH_PLATFORM","--release-id","r1");
        reject("--apply","--expected-schema","TECH_PLATFORM","--release-id","r1","--confirm-version","wrong","--confirm-checksum",PrintSchemaRelease.checksum());
        reject("--apply","--expected-schema","TECH_PLATFORM","--release-id","r1","--confirm-version",PrintSchemaRelease.VERSION,"--confirm-checksum","wrong");
        assertEquals("apply",PrintSchemaReleaseCli.Options.parse(new String[]{"--apply","--expected-schema","TECH_PLATFORM","--release-id","r1","--confirm-version",PrintSchemaRelease.VERSION,"--confirm-checksum",PrintSchemaRelease.checksum()}).mode);
    }

    @Test public void unknownDuplicateMixedAndInjectionArgumentsAreRejected() {
        reject("--expected-schema","TECH_PLATFORM","--plan","--apply");
        reject("--expected-schema","TECH_PLATFORM","--expected-schema","OTHER");
        reject("--expected-schema","TECH_PLATFORM;DROP TABLE X");
        reject("--expected-schema","TECH_PLATFORM","--resume");
        reject("--expected-schema","TECH_PLATFORM","--validate","--release-id","r1");
    }

    @Test public void errorsNeverEchoJdbcCredentialsOrArbitraryArguments() throws Exception {
        Map<String,String> env=new HashMap<>();
        env.put("PRINT_SCHEMA_JDBC_URL","jdbc:h2:mem:private-secret-url"); env.put("PRINT_SCHEMA_JDBC_USER","TECH_PLATFORM"); env.put("PRINT_SCHEMA_JDBC_PASSWORD","private-secret-password");
        ByteArrayOutputStream output=new ByteArrayOutputStream(); PrintStream stream=new PrintStream(output,true,"UTF-8");
        assertEquals(1,PrintSchemaReleaseCli.run(new String[]{"--expected-schema","TECH_PLATFORM"},env,stream,stream));
        assertFalse(output.toString("UTF-8").contains("private-secret"));
        assertTrue(output.toString("UTF-8").contains("PRINT_SCHEMA_RELEASE_REJECTED"));
    }

    @Test public void helpNeedsNoCredentialsOrDatabaseConnection() throws Exception {
        ByteArrayOutputStream output=new ByteArrayOutputStream(); PrintStream stream=new PrintStream(output,true,"UTF-8");
        assertEquals(0,PrintSchemaReleaseCli.run(new String[]{"--help"},Collections.emptyMap(),stream,stream));
    }

    private static void reject(String... args) { try { PrintSchemaReleaseCli.Options.parse(args); fail("不完整授权应在连接前拒绝"); } catch(IllegalArgumentException expected) { assertNotNull(expected.getMessage()); } }
}
