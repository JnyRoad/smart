package com.tce.smart.platform.core.client.release;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

import javax.sql.DataSource;

/**
 * 保密物品放行的 JDBC 事务仓储。
 *
 * 每个业务方法自管一条连接和一个事务，不参与外层 Spring 事务。公开方法只允许调用
 * 既有领域工作流完成业务动作，不提供绕过规则的任意快照保存入口。
 */
public final class JdbcConfidentialReleaseStore {

	private static final int RELEASE_ID_LENGTH = 128;
	private static final int ACTOR_ID_LENGTH = 128;
	private static final int SCOPE_LENGTH = 128;
	private static final int KEY_LENGTH = 200;
	private static final int EVENT_ID_LENGTH = 128;
	private static final String COMMAND_CONSTRAINT = "SMT_CREL_CMD_PK";

	private final DataSource dataSource;
	private final ConfidentialReleaseWorkflow workflow;
	private final ReleasePersistenceCodec codec;

	public JdbcConfidentialReleaseStore(DataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException("DataSource 不能为空");
		}
		this.dataSource = dataSource;
		this.workflow = new ConfidentialReleaseWorkflow();
		this.codec = new ReleasePersistenceCodec();
	}

	public ConfidentialRelease create(String serviceScope, String idempotencyKey,
			ReleaseApplicationRequest request, ReleaseCreationContext context, Instant now,
			String releaseId, String eventId) throws SQLException {
		if (context == null || context.getApplicant() == null) {
			return workflow.create(request, context, now, releaseId, eventId);
		}
		String actorId = required(context.getApplicant().getActorId(), ACTOR_ID_LENGTH, "操作人");
		String digest = codec.digestCreate(request);
		CommandSpec command = command(serviceScope, actorId, idempotencyKey, digest, releaseId);
		AuthorityCheck authority = current -> workflow.create(request, context, now,
				"IDEMPOTENCY-AUTH-CHECK", "IDEMPOTENCY-AUTH-CHECK");
		return execute(command, authority, connection -> {
			ConfidentialRelease result = workflow.create(request, context, now, releaseId, eventId);
			validateSnapshotEnvelope(result);
			insertRelease(connection, result, now);
			insertEvent(connection, lastEvent(result));
			return result;
		});
	}

	public ConfidentialRelease approve(String serviceScope, String idempotencyKey, String releaseId,
			ReleasePrincipal approver, long expectedVersion, Instant now, String eventId) throws SQLException {
		String targetId = required(releaseId, RELEASE_ID_LENGTH, "放行单号");
		ReleasePrincipal actor = normalizePrincipal(approver);
		String actorId = actorId(actor);
		CommandSpec command = command(serviceScope, actorId, idempotencyKey,
				codec.digestApproval(ReleaseAction.APPROVE, targetId, expectedVersion, null), targetId);
		AuthorityCheck authority = current -> validateApprovalAuthority(current, actor);
		return execute(command, authority, connection -> transition(connection, targetId, expectedVersion,
				current -> workflow.approve(current, actor, expectedVersion, now, eventId), now));
	}

	public ConfidentialRelease reject(String serviceScope, String idempotencyKey, String releaseId,
			ReleasePrincipal approver, long expectedVersion, String rejectionReason, Instant now,
			String eventId) throws SQLException {
		String targetId = required(releaseId, RELEASE_ID_LENGTH, "放行单号");
		ReleasePrincipal actor = normalizePrincipal(approver);
		String actorId = actorId(actor);
		CommandSpec command = command(serviceScope, actorId, idempotencyKey,
				codec.digestApproval(ReleaseAction.REJECT, targetId, expectedVersion, rejectionReason), targetId);
		AuthorityCheck authority = current -> validateApprovalAuthority(current, actor);
		return execute(command, authority, connection -> transition(connection, targetId, expectedVersion,
				current -> workflow.reject(current, actor, expectedVersion, rejectionReason, now, eventId), now));
	}

	public ConfidentialRelease depart(String serviceScope, String idempotencyKey, String releaseId,
			ReleasePrincipal operator, long expectedVersion, EscortMode escortMode, String positioningLockId,
			CardEvidence securityEvidence, CardEvidence escortEvidence, Instant now, String eventId)
			throws SQLException {
		String targetId = required(releaseId, RELEASE_ID_LENGTH, "放行单号");
		ReleasePrincipal actor = normalizePrincipal(operator);
		String actorId = actorId(actor);
		CommandSpec command = command(serviceScope, actorId, idempotencyKey,
				codec.digestTransfer(ReleaseAction.DEPART, targetId, expectedVersion, escortMode,
						positioningLockId, securityEvidence, escortEvidence), targetId);
		AuthorityCheck authority = current -> validateExecutionAuthority(actor, current.getOriginPostId());
		return execute(command, authority, connection -> transition(connection, targetId, expectedVersion,
				current -> workflow.depart(current, actor, expectedVersion, escortMode, positioningLockId,
						securityEvidence, escortEvidence, now, eventId), now));
	}

	public ConfidentialRelease arrive(String serviceScope, String idempotencyKey, String releaseId,
			ReleasePrincipal operator, long expectedVersion, EscortMode escortMode, String positioningLockId,
			CardEvidence securityEvidence, CardEvidence escortEvidence, Instant now, String eventId)
			throws SQLException {
		String targetId = required(releaseId, RELEASE_ID_LENGTH, "放行单号");
		ReleasePrincipal actor = normalizePrincipal(operator);
		String actorId = actorId(actor);
		CommandSpec command = command(serviceScope, actorId, idempotencyKey,
				codec.digestTransfer(ReleaseAction.ARRIVE, targetId, expectedVersion, escortMode,
						positioningLockId, securityEvidence, escortEvidence), targetId);
		AuthorityCheck authority = current -> validateExecutionAuthority(actor, current.getDestinationPostId());
		return execute(command, authority, connection -> transition(connection, targetId, expectedVersion,
				current -> workflow.arrive(current, actor, expectedVersion, escortMode, positioningLockId,
						securityEvidence, escortEvidence, now, eventId), now));
	}

	public ConfidentialRelease find(String releaseId) throws SQLException {
		String targetId = required(releaseId, RELEASE_ID_LENGTH, "放行单号");
		try (Connection connection = dataSource.getConnection()) {
			return loadRelease(connection, targetId);
		}
	}

	/**
	 * 读取最近的有限数量放行单快照。调用方必须再按认证主体、园区和岗位做授权过滤；
	 * 此仓储不接受客户端传入的查询条件，避免把未授权 SQL 条件带入持久化边界。
	 */
	public java.util.List<ConfidentialRelease> listRecent(int limit) throws SQLException {
		if (limit <= 0 || limit > 200) {
			throw new IllegalArgumentException("查询数量超出允许范围");
		}
		java.util.List<ConfidentialRelease> result = new java.util.ArrayList<>();
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"SELECT SNAPSHOT_JSON FROM (SELECT SNAPSHOT_JSON FROM SMT_CLIENT_RELEASE "
								+ "ORDER BY UPDATED_AT DESC, RELEASE_ID DESC) WHERE ROWNUM <= ?")) {
			statement.setInt(1, limit);
			try (ResultSet rows = statement.executeQuery()) {
				while (rows.next()) {
					result.add(decodeRelease(rows.getString(1)));
				}
			}
		}
		return java.util.Collections.unmodifiableList(result);
	}

	private ConfidentialRelease execute(CommandSpec command, AuthorityCheck authority, Work work)
			throws SQLException {
		try {
			return inTransaction(connection -> {
				CommandRecord existing = findCommand(connection, command);
				if (existing != null) {
					return replay(connection, command, existing, authority);
				}
				insertCommand(connection, command);
				ConfidentialRelease result = work.apply(connection);
				completeCommand(connection, command, result);
				return result;
			});
		} catch (SQLException error) {
			if (!isCommandKeyConflict(error)) {
				throw error;
			}
			return recoverConcurrentCommand(command, authority);
		}
	}

	private ConfidentialRelease recoverConcurrentCommand(CommandSpec command, AuthorityCheck authority)
			throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			CommandRecord existing = findCommand(connection, command);
			if (existing == null || existing.responseJson == null) {
				throw new SQLException("幂等命令冲突后未找到已提交原回复");
			}
			return replay(connection, command, existing, authority);
		}
	}

	private ConfidentialRelease replay(Connection connection, CommandSpec command, CommandRecord existing,
			AuthorityCheck authority) throws SQLException {
		ConfidentialRelease current = loadRequiredRelease(connection, existing.releaseId);
		authority.validate(current);
		if (!command.digest.equals(existing.digest)) {
			throw new IdempotencyConflictException("同一幂等键不能用于不同请求");
		}
		if (existing.responseJson == null) {
			throw new SQLException("幂等命令尚无可返回的原回复");
		}
		return decodeRelease(existing.responseJson);
	}

	private ConfidentialRelease transition(Connection connection, String releaseId, long expectedVersion,
			Transition transition, Instant now) throws SQLException {
		ConfidentialRelease current = loadRequiredRelease(connection, releaseId);
		ConfidentialRelease result = transition.apply(current);
		validateSnapshotEnvelope(result);
		String json = encodeRelease(result);
		try (PreparedStatement statement = connection.prepareStatement(
				"UPDATE SMT_CLIENT_RELEASE SET STATUS = ?, RELEASE_VERSION = ?, SNAPSHOT_JSON = ?, UPDATED_AT = ? "
						+ "WHERE RELEASE_ID = ? AND RELEASE_VERSION = ?")) {
			statement.setString(1, result.getStatus().name());
			statement.setLong(2, result.getVersion());
			setClob(statement, 3, json);
			statement.setTimestamp(4, Timestamp.from(now));
			statement.setString(5, releaseId);
			statement.setLong(6, expectedVersion);
			if (statement.executeUpdate() != 1) {
				throw violation(ReleaseRuleViolation.Code.VERSION_CONFLICT, "放行单版本已变化");
			}
		}
		insertEvent(connection, lastEvent(result));
		return result;
	}

	private void insertRelease(Connection connection, ConfidentialRelease release, Instant now)
			throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO SMT_CLIENT_RELEASE "
						+ "(RELEASE_ID, STATUS, RELEASE_VERSION, SNAPSHOT_JSON, CREATED_AT, UPDATED_AT) "
						+ "VALUES (?, ?, ?, ?, ?, ?)")) {
			statement.setString(1, release.getReleaseId());
			statement.setString(2, release.getStatus().name());
			statement.setLong(3, release.getVersion());
			setClob(statement, 4, encodeRelease(release));
			statement.setTimestamp(5, Timestamp.from(now));
			statement.setTimestamp(6, Timestamp.from(now));
			statement.executeUpdate();
		}
	}

	private void insertEvent(Connection connection, ReleaseAuditEvent event) throws SQLException {
		validateEventEnvelope(event);
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO SMT_CLIENT_RELEASE_EVENT "
						+ "(EVENT_ID, RELEASE_ID, RELEASE_VERSION, ACTION, ACTOR_ID, OCCURRED_AT, EVENT_JSON) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
			statement.setString(1, event.getEventId());
			statement.setString(2, event.getReleaseId());
			statement.setLong(3, event.getVersion());
			statement.setString(4, event.getAction().name());
			statement.setString(5, event.getActorId());
			statement.setTimestamp(6, Timestamp.from(event.getOccurredAt()));
			setClob(statement, 7, encodeEvent(event));
			statement.executeUpdate();
		}
	}

	private void insertCommand(Connection connection, CommandSpec command) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO SMT_CLIENT_RELEASE_COMMAND "
						+ "(SERVICE_SCOPE, ACTOR_ID, IDEMPOTENCY_KEY, REQUEST_DIGEST, RELEASE_ID, RESPONSE_JSON, CREATED_AT) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
			statement.setString(1, command.scope);
			statement.setString(2, command.actorId);
			statement.setString(3, command.key);
			statement.setString(4, command.digest);
			statement.setString(5, command.releaseId);
			statement.setNull(6, Types.CLOB);
			statement.setTimestamp(7, Timestamp.from(Instant.now()));
			statement.executeUpdate();
		}
	}

	private void completeCommand(Connection connection, CommandSpec command, ConfidentialRelease result)
			throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"UPDATE SMT_CLIENT_RELEASE_COMMAND SET RELEASE_ID = ?, RESPONSE_JSON = ? "
						+ "WHERE SERVICE_SCOPE = ? AND ACTOR_ID = ? AND IDEMPOTENCY_KEY = ?")) {
			statement.setString(1, result.getReleaseId());
			setClob(statement, 2, encodeRelease(result));
			statement.setString(3, command.scope);
			statement.setString(4, command.actorId);
			statement.setString(5, command.key);
			if (statement.executeUpdate() != 1) {
				throw new SQLException("未能保存幂等命令原回复");
			}
		}
	}

	private CommandRecord findCommand(Connection connection, CommandSpec command) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT REQUEST_DIGEST, RELEASE_ID, RESPONSE_JSON FROM SMT_CLIENT_RELEASE_COMMAND "
						+ "WHERE SERVICE_SCOPE = ? AND ACTOR_ID = ? AND IDEMPOTENCY_KEY = ?")) {
			statement.setString(1, command.scope);
			statement.setString(2, command.actorId);
			statement.setString(3, command.key);
			try (ResultSet rows = statement.executeQuery()) {
				if (!rows.next()) {
					return null;
				}
				return new CommandRecord(rows.getString(1), rows.getString(2), rows.getString(3));
			}
		}
	}

	private ConfidentialRelease loadRequiredRelease(Connection connection, String releaseId) throws SQLException {
		ConfidentialRelease release = loadRelease(connection, releaseId);
		if (release == null) {
			throw new SQLException("找不到放行单：" + releaseId);
		}
		return release;
	}

	private ConfidentialRelease loadRelease(Connection connection, String releaseId) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT SNAPSHOT_JSON FROM SMT_CLIENT_RELEASE WHERE RELEASE_ID = ?")) {
			statement.setString(1, releaseId);
			try (ResultSet rows = statement.executeQuery()) {
				return rows.next() ? decodeRelease(rows.getString(1)) : null;
			}
		}
	}

	private ConfidentialRelease inTransaction(Transaction transaction) throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			connection.setAutoCommit(false);
			try {
				ConfidentialRelease result = transaction.apply(connection);
				connection.commit();
				return result;
			} catch (SQLException | RuntimeException error) {
				try {
					connection.rollback();
				} catch (SQLException rollbackError) {
					error.addSuppressed(rollbackError);
				}
				throw error;
			}
		}
	}

	private CommandSpec command(String scope, String actorId, String key, String digest, String releaseId) {
		return new CommandSpec(required(scope, SCOPE_LENGTH, "服务作用域"),
				required(actorId, ACTOR_ID_LENGTH, "操作人"), required(key, KEY_LENGTH, "幂等键"), digest,
				required(releaseId, RELEASE_ID_LENGTH, "放行单号"));
	}

	private String actorId(ReleasePrincipal principal) {
		if (principal == null) {
			throw violation(ReleaseRuleViolation.Code.MISSING_PERMISSION, "缺少服务端认证操作人");
		}
		return required(principal.getActorId(), ACTOR_ID_LENGTH, "操作人");
	}

	/** 查询、幂等归属和领域审计使用同一规范化主体，不改变权限与岗位集合。 */
	private ReleasePrincipal normalizePrincipal(ReleasePrincipal principal) {
		return ReleasePrincipal.authenticated(actorId(principal), principal.getPermissions(),
				principal.getAuthorizedPostIds());
	}

	private void validateApprovalAuthority(ConfidentialRelease release, ReleasePrincipal principal) {
		String actor = actorId(principal);
		if (!principal.hasPermission("item-pass:approve")) {
			throw violation(ReleaseRuleViolation.Code.MISSING_PERMISSION, "缺少权限：item-pass:approve");
		}
		if (!release.getAssignedApproverId().equals(actor)) {
			throw violation(ReleaseRuleViolation.Code.NOT_ASSIGNED_APPROVER, "仅当前指派审批人可以处理");
		}
		if (release.getApplicantId().equals(actor)) {
			throw violation(ReleaseRuleViolation.Code.SELF_APPROVAL, "申请人不得审批本人申请");
		}
	}

	private void validateExecutionAuthority(ReleasePrincipal principal, String postId) {
		actorId(principal);
		if (!principal.hasPermission("item-pass:execute")) {
			throw violation(ReleaseRuleViolation.Code.MISSING_PERMISSION, "缺少权限：item-pass:execute");
		}
		if (!principal.isAuthorizedForPost(postId)) {
			throw violation(ReleaseRuleViolation.Code.UNAUTHORIZED_POST, "操作人未获授权办理当前岗位");
		}
	}

	private void validateSnapshotEnvelope(ConfidentialRelease release) {
		required(release.getReleaseId(), RELEASE_ID_LENGTH, "放行单号");
		required(release.getApplicantId(), ACTOR_ID_LENGTH, "申请人");
		required(release.getAssignedApproverId(), ACTOR_ID_LENGTH, "审批人");
		if (release.getAuditTrail().isEmpty()) {
			throw violation(ReleaseRuleViolation.Code.INVALID_INPUT, "审计事件不能为空");
		}
		validateEventEnvelope(lastEvent(release));
	}

	private void validateEventEnvelope(ReleaseAuditEvent event) {
		required(event.getEventId(), EVENT_ID_LENGTH, "事件编号");
		required(event.getReleaseId(), RELEASE_ID_LENGTH, "事件放行单号");
		required(event.getActorId(), ACTOR_ID_LENGTH, "事件操作人");
	}

	private ReleaseAuditEvent lastEvent(ConfidentialRelease release) {
		return release.getAuditTrail().get(release.getAuditTrail().size() - 1);
	}

	private String required(String value, int maxLength, String label) {
		if (value == null || value.trim().isEmpty() || value.length() > maxLength) {
			throw violation(ReleaseRuleViolation.Code.INVALID_INPUT, label + "为空或超过长度限制");
		}
		return value.trim();
	}

	private String encodeRelease(ConfidentialRelease release) throws SQLException {
		try {
			return codec.encodeRelease(release);
		} catch (IOException error) {
			throw new SQLException("无法编码放行快照", error);
		}
	}

	private String encodeEvent(ReleaseAuditEvent event) throws SQLException {
		try {
			return codec.encodeEvent(event);
		} catch (IOException error) {
			throw new SQLException("无法编码放行事件", error);
		}
	}

	private ConfidentialRelease decodeRelease(String json) throws SQLException {
		try {
			return codec.decodeRelease(json);
		} catch (IOException | RuntimeException error) {
			throw new SQLException("无法恢复放行快照", error);
		}
	}

	private void setClob(PreparedStatement statement, int index, String value) throws SQLException {
		statement.setCharacterStream(index, new StringReader(value), value.length());
	}

	private boolean isCommandKeyConflict(SQLException error) {
		for (SQLException current = error; current != null; current = current.getNextException()) {
			String message = current.getMessage();
			if (current.getErrorCode() == 1 && message != null && message.contains(COMMAND_CONSTRAINT)) {
				return true;
			}
		}
		return false;
	}

	private ReleaseRuleViolation violation(ReleaseRuleViolation.Code code, String message) {
		return new ReleaseRuleViolation(code, message);
	}

	/**
	 * 同一幂等键收到不同请求时的明确拒绝。
	 */
	public static final class IdempotencyConflictException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private IdempotencyConflictException(String message) {
			super(message);
		}
	}

	private interface Transaction {
		ConfidentialRelease apply(Connection connection) throws SQLException;
	}

	private interface Work {
		ConfidentialRelease apply(Connection connection) throws SQLException;
	}

	private interface Transition {
		ConfidentialRelease apply(ConfidentialRelease current);
	}

	private interface AuthorityCheck {
		void validate(ConfidentialRelease current);
	}

	private static final class CommandSpec {
		private final String scope;
		private final String actorId;
		private final String key;
		private final String digest;
		private final String releaseId;

		private CommandSpec(String scope, String actorId, String key, String digest, String releaseId) {
			this.scope = scope;
			this.actorId = actorId;
			this.key = key;
			this.digest = digest;
			this.releaseId = releaseId;
		}
	}

	private static final class CommandRecord {
		private final String digest;
		private final String releaseId;
		private final String responseJson;

		private CommandRecord(String digest, String releaseId, String responseJson) {
			this.digest = digest;
			this.releaseId = releaseId;
			this.responseJson = responseJson;
		}
	}
}
