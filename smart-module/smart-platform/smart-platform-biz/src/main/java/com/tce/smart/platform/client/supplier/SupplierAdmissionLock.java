package com.tce.smart.platform.client.supplier;

import com.tce.smart.platform.core.client.supplier.SupplierQualificationSnapshot;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.transaction.Transaction;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.function.Function;

/** 申请→人员行锁覆盖资格重读到通行仓储事务结束，阻止撤销或资格改写穿过该窗口。 */
public class SupplierAdmissionLock {
    private final DataSource dataSource;
    private final SqlSessionFactory sessions;

    public SupplierAdmissionLock(DataSource dataSource, SqlSessionFactory sessions) {
        if (dataSource == null || sessions == null || sessions.getConfiguration().getEnvironment() == null
                || sessions.getConfiguration().getEnvironment().getDataSource() != dataSource) {
            throw new IllegalArgumentException("资格行锁和资料Mapper必须使用同一DataSource");
        }
        this.dataSource = dataSource; this.sessions = sessions;
    }

    public <T> T withQualification(SupplierAdmissionSource source, String badgeId,
            SupplierAccessProperties.Post post, Clock clock, Function<SupplierQualificationSnapshot, T> action) {
        Long badge = SupplierAdmissionSource.parseBadge(badgeId);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long applyId = readId(connection, "SELECT VISITOR_ID FROM SMT_ADMITTANCE_FELLOW WHERE ID = ?", badge);
                readId(connection, "SELECT ID FROM SMT_ADMITTANCE_APPLY WHERE ID = ? FOR UPDATE WAIT 5", applyId);
                long lockedApply = readId(connection, "SELECT VISITOR_ID FROM SMT_ADMITTANCE_FELLOW WHERE ID = ? FOR UPDATE WAIT 5", badge);
                if (lockedApply != applyId) throw new SupplierAccessHttpException(409);
                // 显式使用锁连接构造会话，避免跨连接陈旧缓存或不同数据源的资料查询。
                // SpringManagedTransactionFactory不支持openSession(Connection)。此处只替换本会话的
                // JDBC事务所有者，保留生产Configuration内原Mapper、类型处理器与插件，绝不修改共享配置。
                try (SqlSession session = new DefaultSqlSession(sessions.getConfiguration(),
                        sessions.getConfiguration().newExecutor(new CallerManagedTransaction(connection), ExecutorType.SIMPLE), false)) {
                    SupplierQualificationSnapshot qualification = source.loadUsingSession(session, badgeId, post, clock.instant());
                    if (!Long.toString(applyId).equals(qualification.getAdmissionId())) throw new SupplierAccessHttpException(409);
                    T result = action.apply(qualification);
                    // action内的通行仓储已经提交/返回原幂等回复，之后才释放来源资格行锁。
                    connection.commit();
                    return result;
                }
            } catch (SQLException | RuntimeException failure) {
                try { if (!connection.isClosed()) connection.rollback(); } catch (SQLException ignored) { }
                throw failure;
            }
        } catch (SQLException unavailable) {
            throw new SupplierAccessHttpException(503);
        }
    }

    private long readId(Connection connection, String sql, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.setQueryTimeout(6);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) throw new SupplierAccessHttpException(404);
                long result = rows.getLong(1);
                if (rows.wasNull() || result <= 0) throw new SupplierAccessHttpException(404);
                return result;
            }
        }
    }

    /** MyBatis 会话只读取资格资料，锁连接的提交、回滚和关闭必须由外层统一负责。 */
    private static final class CallerManagedTransaction implements Transaction {
        private final Connection connection;

        private CallerManagedTransaction(Connection connection) { this.connection = connection; }
        @Override public Connection getConnection() { return connection; }
        @Override public void commit() { }
        @Override public void rollback() { }
        @Override public void close() { }
        @Override public Integer getTimeout() { return null; }
    }
}
