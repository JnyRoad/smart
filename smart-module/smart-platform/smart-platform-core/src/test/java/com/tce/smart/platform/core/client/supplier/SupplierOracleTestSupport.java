package com.tce.smart.platform.core.client.supplier;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Assume;

/** 规格 008 供应商 Oracle 测试的独立、失败关闭辅助代码。 */
final class SupplierOracleTestSupport {

	private static final String ENABLE_PROPERTY = "smart.client.008.oracle.test";
	private static final String ENV_FILE_PROPERTY = "smart.client.008.oracle.envFile";
	private static final String EXPECTED_PORT = "15218";
	private static final String EXPECTED_USER = "SMART_CLIENT_008";
	private static final String EXPECTED_PDB = "FREEPDB1";
	private static final String EXPECTED_VERSION = "23.26.3.0.0";
	private static final String CREATE_SQL_RESOURCE = "db/client008/V002__supplier.sql";
	private static final String UNKNOWN_SQL_RESOURCE = "db/client008/V003__supplier_unknown.sql";

	private static final List<String> TARGET_TABLES = Arrays.asList(
			"SMT_CLIENT_SUP_COMMAND", "SMT_CLIENT_SUP_EVENT",
			"SMT_CLIENT_SUP_PRESENCE", "SMT_CLIENT_SUP_VERIFY");

	private SupplierOracleTestSupport() {
	}

	static DataSource connectAndPrepare() throws Exception {
		Assume.assumeTrue("Oracle 集成测试默认关闭",
				Boolean.parseBoolean(System.getProperty(ENABLE_PROPERTY, "false")));
		String configuredPath = System.getProperty(ENV_FILE_PROPERTY);
		if (configuredPath == null || configuredPath.trim().isEmpty()) {
			Assert.fail("显式启用 Oracle 集成测试时必须设置 " + ENV_FILE_PROPERTY);
		}

		Path envFile = Paths.get(configuredPath).toAbsolutePath().normalize();
		Assert.assertEquals("只允许读取本任务指定的本机配置文件", expectedLocalEnvironment(), envFile);
		Assert.assertTrue("Oracle 集成测试配置必须是普通文件",
				Files.isRegularFile(envFile, LinkOption.NOFOLLOW_LINKS));
		Assert.assertFalse("Oracle 集成测试配置不能是符号链接", Files.isSymbolicLink(envFile));
		Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(envFile,
				LinkOption.NOFOLLOW_LINKS);
		Assert.assertEquals("Oracle 集成测试配置权限必须为 0600",
				EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE), permissions);

		Map<String, String> environment = loadWhitelistedEnvironment(envFile);
		String port = required(environment, "SMART_CLIENT_008_ORACLE_HOST_PORT");
		String user = required(environment, "SMART_CLIENT_008_ORACLE_APP_USER");
		String password = required(environment, "SMART_CLIENT_008_ORACLE_APP_PASSWORD");
		Assert.assertEquals("Oracle 端口不属于本任务目标", EXPECTED_PORT, port);
		Assert.assertEquals("Oracle 用户不属于本任务目标", EXPECTED_USER, user);
		Class.forName("oracle.jdbc.OracleDriver");
		DataSource dataSource = new DriverManagerDataSource(
				"jdbc:oracle:thin:@//127.0.0.1:" + port + "/" + EXPECTED_PDB, user, password);

		try (Connection connection = dataSource.getConnection()) {
			verifyTarget(connection);
			initializeOrVerifySchema(connection);
		}
		return dataSource;
	}

	private static Path expectedLocalEnvironment() {
		Path directory = Paths.get("").toAbsolutePath().normalize();
		while (directory != null) {
			if (Files.isDirectory(directory.resolve("specs/008-unified-client-foundation"))) {
				return directory.resolve("docker/client-integration/.env.client-local");
			}
			directory = directory.getParent();
		}
		throw new AssertionError("当前工作目录不属于规格008工作区");
	}

	static void seedOutsideBaseline(DataSource dataSource, String personId, String areaId, Instant now)
			throws SQLException {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"INSERT INTO SMT_CLIENT_SUP_PRESENCE "
								+ "(PERSON_ID, AREA_ID, PRESENCE_STATE, VERSION_NO, UPDATED_AT) "
								+ "VALUES (?, ?, 'OUTSIDE', 0, ?)")) {
			statement.setString(1, personId);
			statement.setString(2, areaId);
			statement.setTimestamp(3, Timestamp.from(now));
			Assert.assertEquals(1, statement.executeUpdate());
		}
	}

	static PresenceRow readPresence(DataSource dataSource, String personId, String areaId)
			throws SQLException {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"SELECT PRESENCE_STATE, VERSION_NO FROM SMT_CLIENT_SUP_PRESENCE "
								+ "WHERE PERSON_ID = ? AND AREA_ID = ?")) {
			statement.setString(1, personId);
			statement.setString(2, areaId);
			try (ResultSet rows = statement.executeQuery()) {
				return rows.next() ? new PresenceRow(SupplierPresence.valueOf(rows.getString(1)),
						rows.getLong(2)) : null;
			}
		}
	}

	static int countEvents(DataSource dataSource, String personId) throws SQLException {
		return count(dataSource, "SELECT COUNT(*) FROM SMT_CLIENT_SUP_EVENT WHERE PERSON_ID = ?", personId);
	}

	static int countCommands(DataSource dataSource, String scopeId) throws SQLException {
		return count(dataSource, "SELECT COUNT(*) FROM SMT_CLIENT_SUP_COMMAND WHERE SCOPE_ID = ?", scopeId);
	}

	static int countVerifications(DataSource dataSource, String verificationId) throws SQLException {
		return count(dataSource,
				"SELECT COUNT(*) FROM SMT_CLIENT_SUP_VERIFY WHERE VERIFICATION_ID = ?", verificationId);
	}

	static boolean isVerificationConsumed(DataSource dataSource, String verificationId) throws SQLException {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"SELECT CONSUMED_FLAG FROM SMT_CLIENT_SUP_VERIFY WHERE VERIFICATION_ID = ?")) {
			statement.setString(1, verificationId);
			try (ResultSet rows = statement.executeQuery()) {
				Assert.assertTrue("测试核验记录不存在", rows.next());
				return rows.getInt(1) == 1;
			}
		}
	}

	static void cleanSyntheticRows(DataSource dataSource, String prefix) throws SQLException {
		if (dataSource == null || prefix == null
				|| (!prefix.startsWith("T059_") && !prefix.startsWith("T063_"))) {
			return;
		}
		try (Connection connection = dataSource.getConnection()) {
			connection.setAutoCommit(false);
			try {
				deleteByExactPrefix(connection, "SMT_CLIENT_SUP_COMMAND", "SCOPE_ID", prefix);
				deleteByExactPrefix(connection, "SMT_CLIENT_SUP_EVENT", "PERSON_ID", prefix);
				deleteByExactPrefix(connection, "SMT_CLIENT_SUP_VERIFY", "VERIFICATION_ID", prefix);
				deleteByExactPrefix(connection, "SMT_CLIENT_SUP_PRESENCE", "PERSON_ID", prefix);
				connection.commit();
			} catch (SQLException error) {
				connection.rollback();
				throw error;
			}
		}
	}

	private static int count(DataSource dataSource, String sql, String value) throws SQLException {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, value);
			try (ResultSet rows = statement.executeQuery()) {
				Assert.assertTrue(rows.next());
				return rows.getInt(1);
			}
		}
	}

	private static void deleteByExactPrefix(Connection connection, String table, String column,
			String prefix) throws SQLException {
		String sql = "DELETE FROM " + table + " WHERE SUBSTR(" + column + ", 1, ?) = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, prefix.length());
			statement.setString(2, prefix);
			statement.executeUpdate();
		}
	}

	private static Map<String, String> loadWhitelistedEnvironment(Path path) throws Exception {
		Set<String> allowed = new LinkedHashSet<>(Arrays.asList(
				"SMART_CLIENT_008_ORACLE_HOST_PORT",
				"SMART_CLIENT_008_ORACLE_APP_USER",
				"SMART_CLIENT_008_ORACLE_APP_PASSWORD"));
		Map<String, String> values = new LinkedHashMap<>();
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#")) {
					continue;
				}
				int separator = trimmed.indexOf('=');
				if (separator <= 0) {
					continue;
				}
				String key = trimmed.substring(0, separator).trim();
				if (!allowed.contains(key)) {
					continue;
				}
				Assert.assertFalse("Oracle 集成测试配置含重复键：" + key, values.containsKey(key));
				values.put(key, unquote(trimmed.substring(separator + 1).trim()));
			}
		}
		return values;
	}

	private static String unquote(String value) {
		if (value.length() >= 2) {
			char first = value.charAt(0);
			char last = value.charAt(value.length() - 1);
			if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
				return value.substring(1, value.length() - 1);
			}
		}
		return value;
	}

	private static String required(Map<String, String> values, String key) {
		String value = values.get(key);
		if (value == null || value.trim().isEmpty()) {
			Assert.fail("Oracle 集成测试配置缺少：" + key);
		}
		return value;
	}

	private static void verifyTarget(Connection connection) throws Exception {
		DatabaseMetaData metadata = connection.getMetaData();
		Assert.assertTrue("必须连接真实 Oracle",
				metadata.getDatabaseProductName().contains("Oracle"));
		Assert.assertTrue("Oracle 版本必须匹配本任务已核实实例",
				metadata.getDatabaseProductVersion().contains(EXPECTED_VERSION));
		try (Statement statement = connection.createStatement();
				ResultSet rows = statement.executeQuery(
						"SELECT SYS_CONTEXT('USERENV','CON_NAME'), USER FROM DUAL")) {
			Assert.assertTrue(rows.next());
			Assert.assertEquals(EXPECTED_PDB, rows.getString(1));
			Assert.assertEquals(EXPECTED_USER, rows.getString(2));
			Assert.assertFalse(rows.next());
		}
	}

	private static void initializeOrVerifySchema(Connection connection) throws Exception {
		List<String> actual = targetTables(connection);
		if (actual.isEmpty()) {
			executeSqlResource(connection, CREATE_SQL_RESOURCE);
			actual = targetTables(connection);
		}
		Assert.assertEquals("供应商目标表只能全部不存在或全部精确存在", TARGET_TABLES, actual);

		verifyColumns(connection, "SMT_CLIENT_SUP_PRESENCE", Arrays.asList(
				"PERSON_ID|VARCHAR2|128|N", "AREA_ID|VARCHAR2|128|N",
				"PRESENCE_STATE|VARCHAR2|16|N", "VERSION_NO|NUMBER|19,0|N",
				"UPDATED_AT|TIMESTAMP(6)||N"));
		verifyColumns(connection, "SMT_CLIENT_SUP_VERIFY", Arrays.asList(
				"VERIFICATION_ID|VARCHAR2|128|N", "OPERATOR_ID|VARCHAR2|128|N",
				"POST_ID|VARCHAR2|128|N", "AREA_ID|VARCHAR2|128|N",
				"BADGE_ID|VARCHAR2|128|N", "PERSON_ID|VARCHAR2|128|N",
				"COMPANY_ID|VARCHAR2|128|N", "ADMISSION_ID|VARCHAR2|128|N",
				"QUALIFICATION_JSON|CLOB||N", "PRESENCE_STATE|VARCHAR2|16|N",
				"PRESENCE_VERSION|NUMBER|19,0|N", "VERIFIED_AT|TIMESTAMP(6)||N",
				"EXPIRES_AT|TIMESTAMP(6)||N", "CONSUMED_FLAG|NUMBER|1,0|N",
				"CONSUMED_EVENT_ID|VARCHAR2|128|Y", "CONSUMED_AT|TIMESTAMP(6)||Y"));
		verifyColumns(connection, "SMT_CLIENT_SUP_EVENT", Arrays.asList(
				"EVENT_ID|VARCHAR2|128|N", "VERIFICATION_ID|VARCHAR2|128|N",
				"OPERATOR_ID|VARCHAR2|128|N", "POST_ID|VARCHAR2|128|N",
				"AREA_ID|VARCHAR2|128|N", "DIRECTION_CODE|VARCHAR2|16|N",
				"PERSON_ID|VARCHAR2|128|N", "BADGE_ID|VARCHAR2|128|N",
				"COMPANY_ID|VARCHAR2|128|N", "ADMISSION_ID|VARCHAR2|128|N",
				"QUALIFICATION_JSON|CLOB||N", "OCCURRED_AT|TIMESTAMP(6)||N",
				"VERSION_NO|NUMBER|19,0|N"));
		verifyColumns(connection, "SMT_CLIENT_SUP_COMMAND", Arrays.asList(
				"SCOPE_ID|VARCHAR2|128|N", "OPERATOR_ID|VARCHAR2|128|N",
				"IDEMPOTENCY_KEY|VARCHAR2|128|N", "REQUEST_HASH|VARCHAR2|64|N",
				"REPLY_JSON|CLOB||Y", "CREATED_AT|TIMESTAMP(6)||N"));

		Map<String, String> expectedConstraints = new LinkedHashMap<>();
		expectedConstraints.put("CK_SCS_EVT_DIR", "SMT_CLIENT_SUP_EVENT|C|");
		expectedConstraints.put("CK_SCS_EVT_VERSION", "SMT_CLIENT_SUP_EVENT|C|");
		expectedConstraints.put("CK_SCS_PRES_STATE", "SMT_CLIENT_SUP_PRESENCE|C|");
		expectedConstraints.put("CK_SCS_PRES_VER", "SMT_CLIENT_SUP_PRESENCE|C|");
		expectedConstraints.put("CK_SCS_VER_STATE", "SMT_CLIENT_SUP_VERIFY|C|");
		expectedConstraints.put("CK_SCS_VER_USED", "SMT_CLIENT_SUP_VERIFY|C|");
		expectedConstraints.put("CK_SCS_VER_VERSION", "SMT_CLIENT_SUP_VERIFY|C|");
		expectedConstraints.put("FK_SCS_EVT_VERIFY", "SMT_CLIENT_SUP_EVENT|R|PK_SCS_VERIFY");
		expectedConstraints.put("FK_SCS_VER_PRES", "SMT_CLIENT_SUP_VERIFY|R|PK_SCS_PRES");
		expectedConstraints.put("PK_SCS_COMMAND", "SMT_CLIENT_SUP_COMMAND|P|");
		expectedConstraints.put("PK_SCS_EVENT", "SMT_CLIENT_SUP_EVENT|P|");
		expectedConstraints.put("PK_SCS_PRES", "SMT_CLIENT_SUP_PRESENCE|P|");
		expectedConstraints.put("PK_SCS_VERIFY", "SMT_CLIENT_SUP_VERIFY|P|");
		expectedConstraints.put("UQ_SCS_EVT_VERIFY", "SMT_CLIENT_SUP_EVENT|U|");
		expectedConstraints.put("UQ_SCS_EVT_VERSION", "SMT_CLIENT_SUP_EVENT|U|");
		verifyConstraints(connection, expectedConstraints);
		verifyConstraintColumns(connection, "PK_SCS_COMMAND",
				Arrays.asList("SCOPE_ID", "OPERATOR_ID", "IDEMPOTENCY_KEY"));
		verifyConstraintColumns(connection, "PK_SCS_EVENT", Collections.singletonList("EVENT_ID"));
		verifyConstraintColumns(connection, "PK_SCS_PRES", Arrays.asList("PERSON_ID", "AREA_ID"));
		verifyConstraintColumns(connection, "PK_SCS_VERIFY", Collections.singletonList("VERIFICATION_ID"));
		verifyConstraintColumns(connection, "UQ_SCS_EVT_VERIFY",
				Collections.singletonList("VERIFICATION_ID"));
		verifyConstraintColumns(connection, "UQ_SCS_EVT_VERSION",
				Arrays.asList("PERSON_ID", "AREA_ID", "VERSION_NO"));
		verifyConstraintColumns(connection, "FK_SCS_VER_PRES", Arrays.asList("PERSON_ID", "AREA_ID"));
		verifyConstraintColumns(connection, "FK_SCS_EVT_VERIFY",
				Collections.singletonList("VERIFICATION_ID"));
		migrateOrVerifyUnknownStates(connection);
	}

	private static void migrateOrVerifyUnknownStates(Connection connection) throws Exception {
		boolean presenceAllowsUnknown = constraintAllowsUnknown(connection, "CK_SCS_PRES_STATE");
		boolean verificationAllowsUnknown = constraintAllowsUnknown(connection, "CK_SCS_VER_STATE");
		if (!presenceAllowsUnknown && !verificationAllowsUnknown) {
			executeSqlResource(connection, UNKNOWN_SQL_RESOURCE);
			presenceAllowsUnknown = constraintAllowsUnknown(connection, "CK_SCS_PRES_STATE");
			verificationAllowsUnknown = constraintAllowsUnknown(connection, "CK_SCS_VER_STATE");
		}
		Assert.assertTrue("供应商状态约束必须同时允许 UNKNOWN，不能处于部分迁移状态",
				presenceAllowsUnknown && verificationAllowsUnknown);
	}

	private static boolean constraintAllowsUnknown(Connection connection, String constraint)
			throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT SEARCH_CONDITION_VC FROM USER_CONSTRAINTS WHERE CONSTRAINT_NAME = ? "
						+ "AND CONSTRAINT_TYPE = 'C'")) {
			statement.setString(1, constraint);
			try (ResultSet rows = statement.executeQuery()) {
				Assert.assertTrue("缺少供应商状态约束：" + constraint, rows.next());
				String condition = rows.getString(1);
				Assert.assertNotNull("供应商状态约束条件为空：" + constraint, condition);
				Assert.assertFalse("供应商状态约束重复：" + constraint, rows.next());
				String normalized = condition.toUpperCase();
				Assert.assertTrue("供应商状态约束必须保留 OUTSIDE：" + constraint,
						normalized.contains("OUTSIDE"));
				Assert.assertTrue("供应商状态约束必须保留 INSIDE：" + constraint,
						normalized.contains("INSIDE"));
				return normalized.contains("UNKNOWN");
			}
		}
	}

	private static List<String> targetTables(Connection connection) throws SQLException {
		List<String> actual = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME IN "
						+ "('SMT_CLIENT_SUP_COMMAND','SMT_CLIENT_SUP_EVENT',"
						+ "'SMT_CLIENT_SUP_PRESENCE','SMT_CLIENT_SUP_VERIFY') ORDER BY TABLE_NAME");
				ResultSet rows = statement.executeQuery()) {
			while (rows.next()) {
				actual.add(rows.getString(1));
			}
		}
		return actual;
	}

	private static void verifyColumns(Connection connection, String table, List<String> expected)
			throws SQLException {
		List<String> actual = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT COLUMN_NAME, DATA_TYPE, CHAR_LENGTH, DATA_PRECISION, DATA_SCALE, NULLABLE, CHAR_USED "
						+ "FROM USER_TAB_COLUMNS WHERE TABLE_NAME = ? ORDER BY COLUMN_ID")) {
			statement.setString(1, table);
			try (ResultSet rows = statement.executeQuery()) {
				while (rows.next()) {
					String type = rows.getString(2);
					String dimensions = "";
					if ("VARCHAR2".equals(type)) {
						Assert.assertEquals("字符列必须按CHAR声明长度", "C", rows.getString(7));
						dimensions = Long.toString(rows.getLong(3));
					} else if ("NUMBER".equals(type)) {
						dimensions = rows.getLong(4) + "," + rows.getLong(5);
					}
					actual.add(rows.getString(1) + "|" + type + "|" + dimensions + "|" + rows.getString(6));
				}
			}
		}
		Assert.assertEquals(table + " 列定义不匹配", expected, actual);
	}

	private static void verifyConstraints(Connection connection, Map<String, String> expected)
			throws SQLException {
		Map<String, String> actual = new LinkedHashMap<>();
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT CONSTRAINT_NAME, TABLE_NAME, CONSTRAINT_TYPE, R_CONSTRAINT_NAME, STATUS, VALIDATED, DEFERRABLE "
						+ "FROM USER_CONSTRAINTS WHERE TABLE_NAME IN "
						+ "('SMT_CLIENT_SUP_COMMAND','SMT_CLIENT_SUP_EVENT',"
						+ "'SMT_CLIENT_SUP_PRESENCE','SMT_CLIENT_SUP_VERIFY') "
						+ "AND GENERATED = 'USER NAME' ORDER BY CONSTRAINT_NAME");
				ResultSet rows = statement.executeQuery()) {
			while (rows.next()) {
				String referenced = rows.getString(4);
				Assert.assertEquals("约束必须启用", "ENABLED", rows.getString(5));
				Assert.assertEquals("约束必须经过验证", "VALIDATED", rows.getString(6));
				Assert.assertEquals("约束不能延迟检查", "NOT DEFERRABLE", rows.getString(7));
				actual.put(rows.getString(1), rows.getString(2) + "|" + rows.getString(3) + "|"
						+ (referenced == null ? "" : referenced));
			}
		}
		Assert.assertEquals("供应商表约束不匹配", expected, actual);
	}

	private static void verifyConstraintColumns(Connection connection, String constraint,
			List<String> expected) throws SQLException {
		List<String> actual = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT COLUMN_NAME FROM USER_CONS_COLUMNS WHERE CONSTRAINT_NAME = ? ORDER BY POSITION")) {
			statement.setString(1, constraint);
			try (ResultSet rows = statement.executeQuery()) {
				while (rows.next()) {
					actual.add(rows.getString(1));
				}
			}
		}
		Assert.assertEquals(constraint + " 列顺序不匹配", expected, actual);
	}

	private static void executeSqlResource(Connection connection, String resource) throws Exception {
		InputStream stream = SupplierOracleTestSupport.class.getClassLoader().getResourceAsStream(resource);
		Assert.assertNotNull("缺少数据库初始化资源：" + resource, stream);
		StringBuilder sql = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("--")) {
					continue;
				}
				sql.append(line).append('\n');
				if (trimmed.endsWith(";")) {
					String statementSql = sql.substring(0, sql.lastIndexOf(";"));
					try (Statement statement = connection.createStatement()) {
						statement.execute(statementSql);
					}
					sql.setLength(0);
				}
			}
		}
		Assert.assertEquals("SQL 资源末尾存在未执行内容", "", sql.toString().trim());
	}

	static final class PresenceRow {
		private final SupplierPresence presence;
		private final long version;

		private PresenceRow(SupplierPresence presence, long version) {
			this.presence = presence;
			this.version = version;
		}

		SupplierPresence getPresence() {
			return presence;
		}

		long getVersion() {
			return version;
		}
	}

	private static final class DriverManagerDataSource implements DataSource {
		private final String url;
		private final String user;
		private final String password;

		private DriverManagerDataSource(String url, String user, String password) {
			this.url = url;
			this.user = user;
			this.password = password;
		}

		@Override
		public Connection getConnection() throws SQLException {
			return DriverManager.getConnection(url, user, password);
		}

		@Override
		public Connection getConnection(String username, String suppliedPassword) throws SQLException {
			return DriverManager.getConnection(url, username, suppliedPassword);
		}

		@Override
		public PrintWriter getLogWriter() throws SQLException {
			return DriverManager.getLogWriter();
		}

		@Override
		public void setLogWriter(PrintWriter out) throws SQLException {
			DriverManager.setLogWriter(out);
		}

		@Override
		public void setLoginTimeout(int seconds) throws SQLException {
			DriverManager.setLoginTimeout(seconds);
		}

		@Override
		public int getLoginTimeout() throws SQLException {
			return DriverManager.getLoginTimeout();
		}

		@Override
		public Logger getParentLogger() {
			return Logger.getLogger("global");
		}

		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			throw new SQLException("不支持 unwrap");
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) {
			return false;
		}
	}
}
