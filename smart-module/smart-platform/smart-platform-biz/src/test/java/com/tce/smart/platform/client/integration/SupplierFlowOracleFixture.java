package com.tce.smart.platform.client.integration;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.junit.Assert;
import org.junit.Assume;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.mapper.SmtAdmittanceFellowMapper;

/** 仅连接本任务Oracle，初始化平台合成入厂资料；不复制第三方系统或真实人员。 */
final class SupplierFlowOracleFixture {
    final DataSource dataSource;
    final String suffix = UUID.randomUUID().toString().replace("-", "");
    final String postId = "flow-" + suffix;
    final String otherPostId = "flow-other-" + suffix;
    final String areaId = "area-" + suffix;
    final String otherAreaId = "area-other-" + suffix;
    final long applyId = 7000000000000000000L + Math.floorMod(UUID.randomUUID().getLeastSignificantBits(), 1000000000000000L);
    final long fellowId = applyId + 1L;
    final long secondFellowId = applyId + 2L;
    final long shortFellowId = 500000L + Math.floorMod(applyId, 400000L);
    final String mainDocument = syntheticIdCard((int) Math.floorMod(applyId, 998L));
    final String fellowDocument = syntheticIdCard((int) Math.floorMod(applyId, 998L) + 1);

    private SupplierFlowOracleFixture(DataSource dataSource) { this.dataSource = dataSource; }

    static SupplierFlowOracleFixture open() throws Exception {
        Assume.assumeTrue("Oracle业务接口测试默认关闭", Boolean.getBoolean("smart.client.008.oracle.test"));
        String configured = System.getProperty("smart.client.008.oracle.envFile");
        Assert.assertTrue("显式测试必须提供本任务env文件位置", configured != null && !configured.trim().isEmpty());
        Path root = Paths.get("").toAbsolutePath().normalize();
        while (root != null && !Files.isDirectory(root.resolve("specs/008-unified-client-foundation"))) root = root.getParent();
        Assert.assertNotNull("仅允许在规格008工作区执行", root);
        Path env = Paths.get(configured).toAbsolutePath().normalize();
        Assert.assertEquals(root.resolve("docker/client-integration/.env.client-local"), env);
        Assert.assertTrue("配置必须是普通文件", Files.isRegularFile(env, LinkOption.NOFOLLOW_LINKS));
        Assert.assertEquals(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE), Files.getPosixFilePermissions(env));
        Map<String, String> values = new HashMap<>();
        for (String line : Files.readAllLines(env, StandardCharsets.UTF_8)) {
            int split = line.indexOf('=');
            if (split > 0 && line.startsWith("SMART_CLIENT_008_ORACLE_")) values.put(line.substring(0, split), line.substring(split + 1));
        }
        Assert.assertEquals("15218", values.get("SMART_CLIENT_008_ORACLE_HOST_PORT"));
        Assert.assertEquals("SMART_CLIENT_008", values.get("SMART_CLIENT_008_ORACLE_APP_USER"));
        String password = values.get("SMART_CLIENT_008_ORACLE_APP_PASSWORD");
        Assert.assertTrue("本机配置缺少应用凭据", password != null && !password.isEmpty());
        DriverManagerDataSource candidate = new DriverManagerDataSource("jdbc:oracle:thin:@//127.0.0.1:15218/FREEPDB1", "SMART_CLIENT_008", password);
        candidate.setDriverClassName("oracle.jdbc.OracleDriver");
        try (Connection connection = candidate.getConnection(); Statement statement = connection.createStatement()) {
            try (ResultSet rows = statement.executeQuery("SELECT USER, SYS_CONTEXT('USERENV','CON_NAME') FROM DUAL")) {
                Assert.assertTrue(rows.next());
                Assert.assertEquals("SMART_CLIENT_008", rows.getString(1));
                Assert.assertEquals("FREEPDB1", rows.getString(2));
            }
            try (ResultSet rows = statement.executeQuery("SELECT VERSION_FULL FROM PRODUCT_COMPONENT_VERSION WHERE PRODUCT LIKE 'Oracle Database%' OR PRODUCT LIKE 'Oracle AI Database%'")) {
                Assert.assertTrue("无法核对Oracle版本", rows.next());
                Assert.assertEquals("23.26.3.0.0", rows.getString(1));
            }
            try (ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME IN ('SMT_CLIENT_SUP_VERIFY','SMT_CLIENT_SUP_EVENT','SMT_CLIENT_SUP_PRESENCE','SMT_CLIENT_SUP_COMMAND')")) {
                Assert.assertTrue(rows.next());
                Assert.assertEquals("先执行本任务V002/V003仓储测试初始化", 4, rows.getInt(1));
            }
            int present;
            try (ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME IN ('SMT_ADMITTANCE_APPLY','SMT_ADMITTANCE_FELLOW')")) {
                Assert.assertTrue(rows.next()); present = rows.getInt(1);
            }
            Assert.assertTrue("入厂fixture结构不完整时不自动补表", present == 0 || present == 2);
            if (present == 0) {
                String sql = new String(Files.readAllBytes(root.resolve("smart-module/smart-platform/smart-platform-biz/src/test/resources/client008/admittance-fixture.sql")), StandardCharsets.UTF_8);
                for (String part : sql.replaceAll("(?m)^--.*$", "").split(";")) if (!part.trim().isEmpty()) statement.execute(part.trim());
            }
            verifyColumns(connection, "SMT_ADMITTANCE_APPLY", "ID,PARK_ID,VISITOR_NAME,VISITOR_PHOTO_ID,VISITOR_PHONE,CERT_NO,STATUS,START_TIME,END_TIME,RECEPTIONIST_BADGE,RECEPTIONIST_NAME,RECEPTIONIST_PHONE,IS_SEND,CREATE_TIME,REMARK,SMS_CODE,COMPANY,PERSON_TYPE,CAUSE,THING,PROCESS_ID,DEVICE_STATUS,IS_VEHICLE,UNION_ID,PERMIT_FACTORY_TYPE,AREA_TYPE,PERMIT_AREA,PERMIT_OLD_AREA,APPLY_TYPE,ISC_SUBMIT_BATCH");
            verifyColumns(connection, "SMT_ADMITTANCE_FELLOW", "ID,VISITOR_ID,FELLOW_NAME,FELLOW_PHOTO_ID,CERT_NO,CERT_TYPE,FRONT_PHOTO_ID,NATIVE_PLACE,IS_MAIN");
        }
        // 所有目标与结构检查通过以后才向清理逻辑交出连接工厂。
        return new SupplierFlowOracleFixture(candidate);
    }

    private static void verifyColumns(Connection connection, String table, String expected) throws Exception {
        Set<String> found = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT COLUMN_NAME FROM USER_TAB_COLUMNS WHERE TABLE_NAME = ?")) {
            statement.setString(1, table);
            try (ResultSet rows = statement.executeQuery()) { while (rows.next()) found.add(rows.getString(1)); }
        }
        Assert.assertEquals("仅接受已定义的合成入厂表字段", new HashSet<>(Arrays.asList(expected.split(","))), found);
    }

    org.apache.ibatis.session.SqlSessionFactory mapperFactory() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLocalCacheScope(org.apache.ibatis.session.LocalCacheScope.STATEMENT);
        configuration.setEnvironment(new Environment("client008-local", new SpringManagedTransactionFactory(), dataSource));
        configuration.addMapper(SmtAdmittanceApplyMapper.class);
        configuration.addMapper(SmtAdmittanceFellowMapper.class);
        return new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    void seed(LocalDateTime start, LocalDateTime end) throws Exception {
        execute("INSERT INTO SMT_ADMITTANCE_APPLY (ID,PARK_ID,VISITOR_NAME,VISITOR_PHONE,CERT_NO,STATUS,START_TIME,END_TIME,RECEPTIONIST_NAME,RECEPTIONIST_PHONE,COMPANY,AREA_TYPE,APPLY_TYPE,PERSON_TYPE,CREATE_TIME) VALUES (?,7,'合成主访客','测试联系方式',?,0,?,?,'合成被访人','测试被访电话','测试入厂单位','0,11',1,3,?)", applyId, mainDocument, Timestamp.valueOf(start), Timestamp.valueOf(end), Timestamp.valueOf(start));
        execute("INSERT INTO SMT_ADMITTANCE_FELLOW (ID,VISITOR_ID,FELLOW_NAME,CERT_NO,CERT_TYPE,IS_MAIN,FELLOW_PHOTO_ID) VALUES (?,?,'合成主访客',?,NULL,1,'synthetic-main-photo')", fellowId, applyId, mainDocument);
        execute("INSERT INTO SMT_ADMITTANCE_FELLOW (ID,VISITOR_ID,FELLOW_NAME,CERT_NO,CERT_TYPE,IS_MAIN,FELLOW_PHOTO_ID) VALUES (?,?,'合成随行访客',?,NULL,0,'synthetic-fellow-photo')", secondFellowId, applyId, fellowDocument);
    }

    /** 使用非真实行政区99开头的结构合成号码，复现H5只提交证件号、类型为空的历史记录。 */
    private static String syntheticIdCard(int serial) {
        String body = "99000020000101" + String.format(java.util.Locale.ROOT, "%03d", serial);
        int[] weights = {7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2};
        int sum = 0;
        for (int i = 0; i < weights.length; i++) sum += (body.charAt(i) - '0') * weights[i];
        return body + "10X98765432".charAt(sum % 11);
    }

    void execute(String sql, Object... params) throws Exception {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) statement.setObject(i + 1, params[i]);
            statement.executeUpdate();
        }
    }

    int eventCount() throws Exception {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM SMT_CLIENT_SUP_EVENT WHERE POST_ID IN (?,?)")) {
            statement.setString(1, postId); statement.setString(2, otherPostId);
            try (ResultSet rows = statement.executeQuery()) { rows.next(); return rows.getInt(1); }
        }
    }

    void cleanup() throws Exception {
        // 仅清理本用例随机岗位/区域与两个人员ID；不按姓名、全表或固定ID清理。
        execute("DELETE FROM SMT_CLIENT_SUP_COMMAND WHERE OPERATOR_ID IN (SELECT OPERATOR_ID FROM SMT_CLIENT_SUP_VERIFY WHERE POST_ID IN (?,?))", postId, otherPostId);
        execute("DELETE FROM SMT_CLIENT_SUP_EVENT WHERE POST_ID IN (?,?)", postId, otherPostId);
        execute("DELETE FROM SMT_CLIENT_SUP_VERIFY WHERE POST_ID IN (?,?)", postId, otherPostId);
        execute("DELETE FROM SMT_CLIENT_SUP_PRESENCE WHERE AREA_ID IN (?,?)", areaId, otherAreaId);
        execute("DELETE FROM SMT_ADMITTANCE_FELLOW WHERE ID IN (?,?,?)", fellowId, secondFellowId, shortFellowId);
        execute("DELETE FROM SMT_ADMITTANCE_APPLY WHERE ID = ?", applyId);
    }
}
