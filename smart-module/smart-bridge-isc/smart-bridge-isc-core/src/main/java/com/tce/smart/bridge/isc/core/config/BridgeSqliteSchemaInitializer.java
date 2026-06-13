package com.tce.smart.bridge.isc.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Ensures the local SQLite exception table exists when the bridge database file
 * is created by a fresh Docker volume.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BridgeSqliteSchemaInitializer {
	private static final String SQLITE_PREFIX = "jdbc:sqlite:";

	private final DataSource dataSource;

	@Value("${spring.datasource.url:}")
	private String datasourceUrl;

	@PostConstruct
	public void initSchema() {
		if (datasourceUrl == null || !datasourceUrl.startsWith(SQLITE_PREFIX)) {
			return;
		}
		try (Connection connection = dataSource.getConnection();
			 Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE IF NOT EXISTS e_exception_log ("
					+ "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
					+ "event_type TEXT(100),"
					+ "message TEXT,"
					+ "create_time TEXT,"
					+ "update_time TEXT"
					+ ")");
		} catch (Exception e) {
			log.error("Initialize bridge SQLite schema failed: {}", e.getMessage(), e);
			throw new IllegalStateException("Initialize bridge SQLite schema failed", e);
		}
	}
}
