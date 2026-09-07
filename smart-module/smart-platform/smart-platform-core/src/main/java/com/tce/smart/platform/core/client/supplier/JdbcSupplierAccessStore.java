package com.tce.smart.platform.core.client.supplier;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.Clock;
import java.util.function.Supplier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.TimeZone;

import javax.sql.DataSource;

/**
 * 供应商核验、人员区域状态、通行事件与命令幂等记录的 JDBC 事务仓储。
 *
 * 每个公开方法使用一条连接和一个本地事务，并且只通过既有 {@link SupplierAccessWorkflow}
	 * 生成核验或通行结果。旧核验入口要求人员区域基线已建立；首次核验入口只在领域资格检查
	 * 通过后建立 UNKNOWN 状态。本类不推断初始进出方向，也不提供厂牌解析或资格查询实现。
 */
public final class JdbcSupplierAccessStore {

	private static final int IDENTIFIER_LENGTH = 128;
	private static final int HASH_LENGTH = 64;

	private final DataSource dataSource;
	private final SupplierAccessWorkflow workflow;
	private final SupplierPersistenceCodec codec;

	public JdbcSupplierAccessStore(DataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException("DataSource 不能为空");
		}
		this.dataSource = dataSource;
		this.workflow = new SupplierAccessWorkflow();
		this.codec = new SupplierPersistenceCodec();
	}

	public SupplierVerification verify(SupplierQualificationSnapshot qualification,
			SupplierOperator operator, SupplierPostAreaMapping postArea, Instant now,
			String verificationId) {
		validateQualificationEnvelope(qualification);
		String personId = required(qualification.getPersonId(), "人员标识");
		String areaId = required(postArea == null ? null : postArea.getAreaId(), "区域标识");
		String normalizedVerificationId = required(verificationId, "核验编号");

		return inTransaction(new SqlWork<SupplierVerification>() {
			@Override
			public SupplierVerification apply(Connection connection) throws SQLException {
				SupplierPresenceSnapshot presence = loadPresence(connection, personId, areaId, true);
				if (presence == null) {
					throw failure(SupplierPersistenceException.Code.PRESENCE_NOT_INITIALIZED,
							"人员区域状态尚未明确建立");
				}
				SupplierVerification verification = workflow.verify(qualification, operator, postArea,
						presence, now, normalizedVerificationId);
				validateVerificationEnvelope(verification);
				insertVerification(connection, verification);
				return verification;
			}
		});
	}

	/**
	 * 核验当前资格，并在人员区域状态不存在时建立 UNKNOWN/0 基线。
	 *
	 * 首次基线只在领域层完成资格、权限、岗位和区域检查后写入；并发唯一键冲突会重新读取
	 * 已提交状态继续核验，失败事务不会留下孤立状态或核验记录。
	 */
	public SupplierVerification verifyOrInitialize(SupplierQualificationSnapshot qualification,
			SupplierOperator operator, SupplierPostAreaMapping postArea, Instant now,
			String verificationId) {
        return verifyOrInitializeUsingTime(qualification, operator, postArea, () -> now, verificationId);
    }

    /** HTTP资格行锁入口在人员状态锁实际获得后读取时钟，避免排队后沿用请求开始时间。 */
    public SupplierVerification verifyOrInitializeAtCurrentTime(SupplierQualificationSnapshot qualification,
            SupplierOperator operator, SupplierPostAreaMapping postArea, Clock clock, String verificationId) {
        if (clock == null) throw failure(SupplierPersistenceException.Code.INVALID_INPUT, "操作时钟不能为空");
        return verifyOrInitializeUsingTime(qualification, operator, postArea, clock::instant, verificationId);
    }

    private SupplierVerification verifyOrInitializeUsingTime(SupplierQualificationSnapshot qualification,
            SupplierOperator operator, SupplierPostAreaMapping postArea, Supplier<Instant> time, String verificationId) {
		validateQualificationEnvelope(qualification);
		String personId = required(qualification.getPersonId(), "人员标识");
		String areaId = required(postArea == null ? null : postArea.getAreaId(), "区域标识");
		String normalizedVerificationId = required(verificationId, "核验编号");

		try {
			return inTransaction(new SqlWork<SupplierVerification>() {
				@Override
				public SupplierVerification apply(Connection connection) throws SQLException {
					SupplierPresenceSnapshot presence = loadPresence(connection, personId, areaId, true);
					boolean initialize = presence == null;
					if (initialize) {
						presence = SupplierPresenceSnapshot.current(personId, areaId,
								SupplierPresence.UNKNOWN, 0L);
					}
                    Instant now = time.get();
					SupplierVerification verification = workflow.verify(qualification, operator, postArea,
							presence, now, normalizedVerificationId);
					validateVerificationEnvelope(verification);
					if (initialize) {
						insertInitialPresenceOrSignalCollision(connection, presence, now);
					}
					insertVerification(connection, verification);
					return verification;
				}
			});
		} catch (PresenceKeyCollision collision) {
			return recoverConcurrentInitialization(qualification, operator, postArea, time,
					normalizedVerificationId, personId, areaId);
		}
	}

	/**
	 * 按核验编号恢复内部持久化对象；不存在时返回 null。
	 *
	 * 调用方在向 HTTP 客户端返回其中的人员资料前，必须校验当前操作人与核验岗位绑定。
	 */
	public SupplierVerification findVerification(String verificationId) {
		String normalizedVerificationId = required(verificationId, "核验编号");
		return inTransaction(new SqlWork<SupplierVerification>() {
			@Override
			public SupplierVerification apply(Connection connection) throws SQLException {
				StoredVerification stored = loadVerification(connection, normalizedVerificationId, false);
				return stored == null ? null : stored.verification;
			}
		});
	}

	/**
	 * 只读取调用方已授权岗位产生的事件，按发生时间和事件编号稳定降序返回。
	 */
	public List<SupplierPassageEvent> listEvents(Set<String> allowedPostIds, int limit) {
		if (allowedPostIds == null) {
			throw failure(SupplierPersistenceException.Code.INVALID_INPUT, "允许岗位集合不能为空");
		}
		if (limit < 1 || limit > 100) {
			throw failure(SupplierPersistenceException.Code.INVALID_INPUT, "事件查询条数必须在 1 到 100 之间");
		}
		if (allowedPostIds.isEmpty()) {
			return Collections.emptyList();
		}
		Set<String> normalizedPostIds = new TreeSet<>();
		for (String postId : allowedPostIds) {
			normalizedPostIds.add(required(postId, "岗位标识"));
		}
		return inTransaction(new SqlWork<List<SupplierPassageEvent>>() {
			@Override
			public List<SupplierPassageEvent> apply(Connection connection) throws SQLException {
				return loadEvents(connection, normalizedPostIds, limit);
			}
		});
	}

	public SupplierPassageResult record(String scopeId, String idempotencyKey, String verificationId,
			SupplierQualificationSnapshot currentQualification, SupplierOperator currentOperator,
			SupplierPostAreaMapping currentPostArea, SupplierDirection direction, Instant now,
			String eventId) {
        return recordUsingTime(scopeId, idempotencyKey, verificationId, currentQualification,
                currentOperator, currentPostArea, direction, () -> now, eventId);
    }

    /** 新接入在核验/人员状态行锁获得后重取操作时间；旧Instant方法保留固定时刻语义。 */
    public SupplierPassageResult recordAtCurrentTime(String scopeId, String idempotencyKey, String verificationId,
            SupplierQualificationSnapshot qualification, SupplierOperator operator, SupplierPostAreaMapping postArea,
            SupplierDirection direction, Clock clock, String eventId) {
        if (clock == null) throw failure(SupplierPersistenceException.Code.INVALID_INPUT, "操作时钟不能为空");
        return recordUsingTime(scopeId, idempotencyKey, verificationId, qualification, operator,
                postArea, direction, clock::instant, eventId);
    }

    private SupplierPassageResult recordUsingTime(String scopeId, String idempotencyKey, String verificationId,
            SupplierQualificationSnapshot currentQualification, SupplierOperator currentOperator,
            SupplierPostAreaMapping currentPostArea, SupplierDirection direction, Supplier<Instant> time, String eventId) {
        Instant now = time.get();
		String normalizedScope = required(scopeId, "服务作用域");
		String actorId = operatorId(currentOperator);
		String normalizedKey = required(idempotencyKey, "幂等键");
		String normalizedVerificationId = required(verificationId, "核验编号");
		String normalizedEventId = required(eventId, "事件编号");
		validateQualificationEnvelope(currentQualification);
		required(currentPostArea == null ? null : currentPostArea.getPostId(), "岗位标识");
		required(currentPostArea == null ? null : currentPostArea.getAreaId(), "区域标识");
		String digest = codec.digestRecord(normalizedVerificationId, direction, currentQualification,
				currentPostArea);
		CommandSpec command = new CommandSpec(normalizedScope, actorId, normalizedKey, digest);
		ReplayContext replayContext = new ReplayContext(normalizedVerificationId, currentQualification,
				currentOperator, currentPostArea, direction, time);

		try {
			return inTransaction(new SqlWork<SupplierPassageResult>() {
				@Override
				public SupplierPassageResult apply(Connection connection) throws SQLException {
					CommandRecord existing = findCommand(connection, command);
					if (existing != null) {
						return replay(connection, command, existing, replayContext);
					}
					insertCommandOrSignalCollision(connection, command, now);
					return recordFresh(connection, command, replayContext, normalizedEventId);
				}
			});
		} catch (CommandKeyCollision collision) {
			return recoverConcurrentCommand(command, replayContext);
		}
	}

	private SupplierPassageResult recordFresh(Connection connection, CommandSpec command,
			ReplayContext context, String eventId) throws SQLException {
		StoredVerification stored = loadVerification(connection, context.verificationId, true);
		if (stored == null) {
			throw failure(SupplierPersistenceException.Code.VERIFICATION_NOT_FOUND,
					"找不到当前厂牌核验");
		}
		if (stored.consumed) {
			throw failure(SupplierPersistenceException.Code.VERIFICATION_CONSUMED,
					"当前厂牌核验已被使用");
		}

		SupplierVerification verification = stored.verification;
		SupplierPresenceSnapshot presence = loadPresence(connection,
				verification.getQualificationSnapshot().getPersonId(), verification.getAreaId(), true);
		if (presence == null) {
			throw failure(SupplierPersistenceException.Code.PRESENCE_NOT_INITIALIZED,
					"人员区域状态尚未明确建立");
		}
        Instant now = context.time.get();
		SupplierPassageResult result = workflow.record(verification, context.qualification,
				context.operator, context.postArea, presence, context.direction, now, eventId);
		validateResultEnvelope(result);
		updatePresence(connection, presence, result.getPresence(), now);
		consumeVerification(connection, verification.getVerificationId(), result.getEvent().getEventId(),
				now);
		insertEvent(connection, result.getEvent());
		completeCommand(connection, command, result);
		return result;
	}

	private SupplierPassageResult recoverConcurrentCommand(CommandSpec command, ReplayContext context) {
		return inTransaction(new SqlWork<SupplierPassageResult>() {
			@Override
			public SupplierPassageResult apply(Connection connection) throws SQLException {
				CommandRecord existing = findCommand(connection, command);
				if (existing == null) {
					throw failure(SupplierPersistenceException.Code.CONCURRENT_MODIFICATION,
							"幂等命令冲突后未找到已提交原回复");
				}
				return replay(connection, command, existing, context);
			}
		});
	}

	private SupplierVerification recoverConcurrentInitialization(
			SupplierQualificationSnapshot qualification, SupplierOperator operator,
			SupplierPostAreaMapping postArea, Supplier<Instant> time, String verificationId,
			String personId, String areaId) {
		return inTransaction(new SqlWork<SupplierVerification>() {
			@Override
			public SupplierVerification apply(Connection connection) throws SQLException {
				SupplierPresenceSnapshot presence = loadPresence(connection, personId, areaId, true);
				if (presence == null) {
					throw failure(SupplierPersistenceException.Code.CONCURRENT_MODIFICATION,
							"人员区域状态并发建立后未找到已提交记录");
				}
                Instant now = time.get();
				SupplierVerification verification = workflow.verify(qualification, operator, postArea,
						presence, now, verificationId);
				validateVerificationEnvelope(verification);
				insertVerification(connection, verification);
				return verification;
			}
		});
	}

	private SupplierPassageResult replay(Connection connection, CommandSpec command,
			CommandRecord existing, ReplayContext context) throws SQLException {
		StoredVerification stored = loadVerification(connection, context.verificationId, false);
		if (stored == null) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
					"幂等命令引用的核验不存在");
		}
		SupplierVerification verification = stored.verification;
		SupplierPresenceSnapshot verifiedPresence = SupplierPresenceSnapshot.current(
				verification.getQualificationSnapshot().getPersonId(), verification.getAreaId(),
				verification.getPresence(), verification.getPresenceVersion());
		workflow.verify(context.qualification, context.operator, context.postArea, verifiedPresence,
				context.time.get(), "IDEMPOTENCY-REPLAY-CHECK");
		if (!verification.getOperatorId().equals(context.operator.getOperatorId())
				|| !verification.getPostId().equals(context.postArea.getPostId())
				|| !verification.getAreaId().equals(context.postArea.getAreaId())
				|| !sameQualificationIdentity(verification.getQualificationSnapshot(), context.qualification)) {
			throw new SupplierRuleViolation(SupplierRuleViolation.Code.VERIFICATION_MISMATCH,
					"厂牌核验与当前操作人、岗位、区域或资格主体不匹配");
		}
		if (!command.digest.equals(existing.digest)) {
			throw failure(SupplierPersistenceException.Code.IDEMPOTENCY_CONFLICT,
					"同一幂等键不能用于不同请求");
		}
		if (existing.replyJson == null) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
					"幂等命令缺少已提交原回复");
		}
		SupplierPassageResult result = decodeResult(existing.replyJson);
		validateStoredReply(result, verification, context.direction);
		return result;
	}

	private SupplierPresenceSnapshot loadPresence(Connection connection, String personId, String areaId,
			boolean forUpdate) throws SQLException {
		String sql = "SELECT PRESENCE_STATE, VERSION_NO FROM SMT_CLIENT_SUP_PRESENCE "
				+ "WHERE PERSON_ID = ? AND AREA_ID = ?" + (forUpdate ? " FOR UPDATE" : "");
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, personId);
			statement.setString(2, areaId);
			try (ResultSet rows = statement.executeQuery()) {
				if (!rows.next()) {
					return null;
				}
				String state = rows.getString(1);
				long version = rows.getLong(2);
				if (rows.wasNull() || version < 0L) {
					throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
							"人员区域状态版本无效");
				}
				try {
					return SupplierPresenceSnapshot.current(personId, areaId,
							SupplierPresence.valueOf(state), version);
				} catch (RuntimeException error) {
					throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
							"人员区域状态值无效", error);
				}
			}
		}
	}

	private StoredVerification loadVerification(Connection connection, String verificationId,
			boolean forUpdate) throws SQLException {
		String sql = "SELECT OPERATOR_ID, POST_ID, AREA_ID, BADGE_ID, PERSON_ID, COMPANY_ID, "
				+ "ADMISSION_ID, QUALIFICATION_JSON, PRESENCE_STATE, PRESENCE_VERSION, VERIFIED_AT, "
				+ "EXPIRES_AT, CONSUMED_FLAG FROM SMT_CLIENT_SUP_VERIFY WHERE VERIFICATION_ID = ?"
				+ (forUpdate ? " FOR UPDATE" : "");
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, verificationId);
			try (ResultSet rows = statement.executeQuery()) {
				if (!rows.next()) {
					return null;
				}
				String operatorId = rows.getString(1);
				String postId = rows.getString(2);
				String areaId = rows.getString(3);
				String badgeId = rows.getString(4);
				String personId = rows.getString(5);
				String companyId = rows.getString(6);
				String admissionId = rows.getString(7);
				SupplierQualificationSnapshot qualification = decodeQualification(readClob(rows, 8));
				SupplierPresence presence;
				try {
					presence = SupplierPresence.valueOf(rows.getString(9));
				} catch (RuntimeException error) {
					throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
							"核验中的人员区域状态无效", error);
				}
				long version = rows.getLong(10);
				if (rows.wasNull() || version < 0L) {
					throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
							"核验中的人员区域版本无效");
				}
				Instant verifiedAt = instant(rows, 11);
				Instant expiresAt = instant(rows, 12);
				int consumedFlag = rows.getInt(13);
				if (rows.wasNull() || (consumedFlag != 0 && consumedFlag != 1)) {
					throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
							"核验消耗标志无效");
				}
				if (!badgeId.equals(qualification.getBadgeId())
						|| !personId.equals(qualification.getPersonId())
						|| !companyId.equals(qualification.getCompanyId())
						|| !admissionId.equals(qualification.getAdmissionId())
						|| !qualification.getAuthorizedAreaIds().contains(areaId)) {
					throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
							"核验冗余身份与资格快照不一致");
				}
				SupplierVerification verification = new SupplierVerification(verificationId, operatorId,
						postId, areaId, qualification, presence, version, verifiedAt, expiresAt);
				validateVerificationEnvelope(verification);
				return new StoredVerification(verification, consumedFlag == 1);
			}
		}
	}

	private List<SupplierPassageEvent> loadEvents(Connection connection, Set<String> postIds,
			int limit) throws SQLException {
		StringBuilder sql = new StringBuilder("SELECT EVENT_ID, VERIFICATION_ID, OPERATOR_ID, POST_ID, "
				+ "AREA_ID, DIRECTION_CODE, PERSON_ID, BADGE_ID, COMPANY_ID, ADMISSION_ID, "
				+ "QUALIFICATION_JSON, OCCURRED_AT, VERSION_NO FROM SMT_CLIENT_SUP_EVENT WHERE POST_ID IN (");
		int placeholder = 0;
		for (String ignored : postIds) {
			if (placeholder++ > 0) {
				sql.append(',');
			}
			sql.append('?');
		}
		sql.append(") ORDER BY OCCURRED_AT DESC, EVENT_ID DESC FETCH FIRST ? ROWS ONLY");

		try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
			int parameter = 1;
			for (String postId : postIds) {
				statement.setString(parameter++, postId);
			}
			statement.setInt(parameter, limit);
			List<SupplierPassageEvent> events = new ArrayList<>();
			try (ResultSet rows = statement.executeQuery()) {
				while (rows.next()) {
					events.add(readEvent(rows));
				}
			}
			return events;
		}
	}

	private SupplierPassageEvent readEvent(ResultSet rows) throws SQLException {
		String eventId = rows.getString(1);
		String verificationId = rows.getString(2);
		String operatorId = rows.getString(3);
		String postId = rows.getString(4);
		String areaId = rows.getString(5);
		SupplierDirection direction;
		try {
			direction = SupplierDirection.valueOf(rows.getString(6));
		} catch (RuntimeException error) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
					"通行事件方向无效", error);
		}
		String personId = rows.getString(7);
		String badgeId = rows.getString(8);
		String companyId = rows.getString(9);
		String admissionId = rows.getString(10);
		SupplierQualificationSnapshot qualification = decodeQualification(readClob(rows, 11));
		Instant occurredAt = instant(rows, 12);
		long version = rows.getLong(13);
		if (rows.wasNull() || !personId.equals(qualification.getPersonId())
				|| !badgeId.equals(qualification.getBadgeId())
				|| !companyId.equals(qualification.getCompanyId())
				|| !admissionId.equals(qualification.getAdmissionId())
				|| !qualification.getAuthorizedAreaIds().contains(areaId)) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
					"通行事件冗余身份与资格快照不一致");
		}
		SupplierPassageEvent event = new SupplierPassageEvent(eventId, verificationId, operatorId,
				postId, areaId, direction, occurredAt, version, qualification);
		validateEventEnvelope(event);
		return event;
	}

	private void insertVerification(Connection connection, SupplierVerification verification)
			throws SQLException {
		SupplierQualificationSnapshot qualification = verification.getQualificationSnapshot();
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO SMT_CLIENT_SUP_VERIFY "
						+ "(VERIFICATION_ID, OPERATOR_ID, POST_ID, AREA_ID, BADGE_ID, PERSON_ID, "
						+ "COMPANY_ID, ADMISSION_ID, QUALIFICATION_JSON, PRESENCE_STATE, "
						+ "PRESENCE_VERSION, VERIFIED_AT, EXPIRES_AT, CONSUMED_FLAG) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)")) {
			statement.setString(1, verification.getVerificationId());
			statement.setString(2, verification.getOperatorId());
			statement.setString(3, verification.getPostId());
			statement.setString(4, verification.getAreaId());
			statement.setString(5, qualification.getBadgeId());
			statement.setString(6, qualification.getPersonId());
			statement.setString(7, qualification.getCompanyId());
			statement.setString(8, qualification.getAdmissionId());
			setClob(statement, 9, encodeQualification(qualification));
			statement.setString(10, verification.getPresence().name());
			statement.setLong(11, verification.getPresenceVersion());
			setInstant(statement, 12, verification.getVerifiedAt());
			setInstant(statement, 13, verification.getExpiresAt());
			if (statement.executeUpdate() != 1) {
				throw new SQLException("核验写入行数异常");
			}
		}
	}

	private void insertInitialPresenceOrSignalCollision(Connection connection,
			SupplierPresenceSnapshot presence, Instant now) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO SMT_CLIENT_SUP_PRESENCE "
						+ "(PERSON_ID, AREA_ID, PRESENCE_STATE, VERSION_NO, UPDATED_AT) "
						+ "VALUES (?, ?, ?, 0, ?)")) {
			statement.setString(1, presence.getPersonId());
			statement.setString(2, presence.getAreaId());
			statement.setString(3, SupplierPresence.UNKNOWN.name());
			setInstant(statement, 4, now);
			statement.executeUpdate();
		} catch (SQLException error) {
			if (isUniqueConstraint(error)) {
				throw new PresenceKeyCollision(error);
			}
			throw error;
		}
	}

	private void insertCommandOrSignalCollision(Connection connection, CommandSpec command, Instant now)
			throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO SMT_CLIENT_SUP_COMMAND "
						+ "(SCOPE_ID, OPERATOR_ID, IDEMPOTENCY_KEY, REQUEST_HASH, REPLY_JSON, CREATED_AT) "
						+ "VALUES (?, ?, ?, ?, NULL, ?)")) {
			statement.setString(1, command.scope);
			statement.setString(2, command.actorId);
			statement.setString(3, command.key);
			statement.setString(4, command.digest);
			setInstant(statement, 5, now);
			statement.executeUpdate();
		} catch (SQLException error) {
			if (isUniqueConstraint(error)) {
				throw new CommandKeyCollision(error);
			}
			throw error;
		}
	}

	private CommandRecord findCommand(Connection connection, CommandSpec command) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT REQUEST_HASH, REPLY_JSON FROM SMT_CLIENT_SUP_COMMAND "
						+ "WHERE SCOPE_ID = ? AND OPERATOR_ID = ? AND IDEMPOTENCY_KEY = ?")) {
			statement.setString(1, command.scope);
			statement.setString(2, command.actorId);
			statement.setString(3, command.key);
			try (ResultSet rows = statement.executeQuery()) {
				return rows.next() ? new CommandRecord(rows.getString(1), readClob(rows, 2)) : null;
			}
		}
	}

	private void updatePresence(Connection connection, SupplierPresenceSnapshot before,
			SupplierPresenceSnapshot after, Instant now) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"UPDATE SMT_CLIENT_SUP_PRESENCE SET PRESENCE_STATE = ?, VERSION_NO = ?, UPDATED_AT = ? "
						+ "WHERE PERSON_ID = ? AND AREA_ID = ? AND PRESENCE_STATE = ? AND VERSION_NO = ?")) {
			statement.setString(1, after.getPresence().name());
			statement.setLong(2, after.getVersion());
			setInstant(statement, 3, now);
			statement.setString(4, before.getPersonId());
			statement.setString(5, before.getAreaId());
			statement.setString(6, before.getPresence().name());
			statement.setLong(7, before.getVersion());
			if (statement.executeUpdate() != 1) {
				throw failure(SupplierPersistenceException.Code.CONCURRENT_MODIFICATION,
						"人员区域状态版本已变化");
			}
		}
	}

	private void consumeVerification(Connection connection, String verificationId, String eventId,
			Instant now) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"UPDATE SMT_CLIENT_SUP_VERIFY SET CONSUMED_FLAG = 1, CONSUMED_EVENT_ID = ?, CONSUMED_AT = ? "
						+ "WHERE VERIFICATION_ID = ? AND CONSUMED_FLAG = 0")) {
			statement.setString(1, eventId);
			setInstant(statement, 2, now);
			statement.setString(3, verificationId);
			if (statement.executeUpdate() != 1) {
				throw failure(SupplierPersistenceException.Code.VERIFICATION_CONSUMED,
						"当前厂牌核验已被使用");
			}
		}
	}

	private void insertEvent(Connection connection, SupplierPassageEvent event) throws SQLException {
		SupplierQualificationSnapshot qualification = event.getQualificationSnapshot();
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO SMT_CLIENT_SUP_EVENT "
						+ "(EVENT_ID, VERIFICATION_ID, OPERATOR_ID, POST_ID, AREA_ID, DIRECTION_CODE, "
						+ "PERSON_ID, BADGE_ID, COMPANY_ID, ADMISSION_ID, QUALIFICATION_JSON, "
						+ "OCCURRED_AT, VERSION_NO) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
			statement.setString(1, event.getEventId());
			statement.setString(2, event.getVerificationId());
			statement.setString(3, event.getOperatorId());
			statement.setString(4, event.getPostId());
			statement.setString(5, event.getAreaId());
			statement.setString(6, event.getDirection().name());
			statement.setString(7, qualification.getPersonId());
			statement.setString(8, qualification.getBadgeId());
			statement.setString(9, qualification.getCompanyId());
			statement.setString(10, qualification.getAdmissionId());
			setClob(statement, 11, encodeQualification(qualification));
			setInstant(statement, 12, event.getOccurredAt());
			statement.setLong(13, event.getVersion());
			statement.executeUpdate();
		}
	}

	private void completeCommand(Connection connection, CommandSpec command, SupplierPassageResult result)
			throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"UPDATE SMT_CLIENT_SUP_COMMAND SET REPLY_JSON = ? "
						+ "WHERE SCOPE_ID = ? AND OPERATOR_ID = ? AND IDEMPOTENCY_KEY = ? "
						+ "AND REQUEST_HASH = ? AND REPLY_JSON IS NULL")) {
			setClob(statement, 1, encodeResult(result));
			statement.setString(2, command.scope);
			statement.setString(3, command.actorId);
			statement.setString(4, command.key);
			statement.setString(5, command.digest);
			if (statement.executeUpdate() != 1) {
				throw failure(SupplierPersistenceException.Code.CONCURRENT_MODIFICATION,
						"未能保存幂等命令原回复");
			}
		}
	}

	private <T> T inTransaction(SqlWork<T> work) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setAutoCommit(false);
			try {
				T result = work.apply(connection);
				connection.commit();
				return result;
			} catch (SQLException error) {
				rollback(connection, error);
				throw failure(SupplierPersistenceException.Code.STORAGE_FAILURE,
						"供应商通行持久化失败", error);
			} catch (RuntimeException error) {
				rollback(connection, error);
				throw error;
			}
		} catch (SQLException error) {
			throw failure(SupplierPersistenceException.Code.STORAGE_FAILURE,
					"供应商通行持久化连接失败", error);
		}
	}

	private void rollback(Connection connection, Throwable original) {
		try {
			connection.rollback();
		} catch (SQLException rollbackError) {
			original.addSuppressed(rollbackError);
		}
	}

	private void validateQualificationEnvelope(SupplierQualificationSnapshot qualification) {
		if (qualification == null) {
			throw failure(SupplierPersistenceException.Code.INVALID_INPUT, "可信供应商资格快照不能为空");
		}
		required(qualification.getBadgeId(), "厂牌标识");
		required(qualification.getPersonId(), "人员标识");
		required(qualification.getCompanyId(), "单位标识");
		required(qualification.getAdmissionId(), "入厂申请标识");
		for (String areaId : qualification.getAuthorizedAreaIds()) {
			required(areaId, "授权区域标识");
		}
	}

	private void validateVerificationEnvelope(SupplierVerification verification) {
		required(verification.getVerificationId(), "核验编号");
		required(verification.getOperatorId(), "操作人标识");
		required(verification.getPostId(), "岗位标识");
		required(verification.getAreaId(), "区域标识");
		validateQualificationEnvelope(verification.getQualificationSnapshot());
		if (verification.getPresence() == null || verification.getPresenceVersion() < 0L
				|| verification.getVerifiedAt() == null || verification.getExpiresAt() == null) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA, "核验持久化字段无效");
		}
	}

	private void validateResultEnvelope(SupplierPassageResult result) {
		if (result == null || result.getPresence() == null || result.getEvent() == null) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA, "通行结果字段无效");
		}
		SupplierPresenceSnapshot presence = result.getPresence();
		SupplierPassageEvent event = result.getEvent();
		required(presence.getPersonId(), "人员标识");
		required(presence.getAreaId(), "区域标识");
		required(event.getEventId(), "事件编号");
		required(event.getVerificationId(), "核验编号");
		required(event.getOperatorId(), "操作人标识");
		required(event.getPostId(), "岗位标识");
		required(event.getAreaId(), "区域标识");
		validateQualificationEnvelope(event.getQualificationSnapshot());
		if (presence.getPresence() == null || presence.getVersion() <= 0L
				|| event.getDirection() == null || event.getOccurredAt() == null || event.getVersion() <= 0L) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA, "通行结果字段无效");
		}
	}

	private void validateEventEnvelope(SupplierPassageEvent event) {
		required(event.getEventId(), "事件编号");
		required(event.getVerificationId(), "核验编号");
		required(event.getOperatorId(), "操作人标识");
		required(event.getPostId(), "岗位标识");
		required(event.getAreaId(), "区域标识");
		validateQualificationEnvelope(event.getQualificationSnapshot());
		if (event.getDirection() == null || event.getOccurredAt() == null || event.getVersion() <= 0L) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA, "通行事件字段无效");
		}
	}

	private void validateStoredReply(SupplierPassageResult result, SupplierVerification verification,
			SupplierDirection requestedDirection) {
		validateResultEnvelope(result);
		SupplierPassageEvent event = result.getEvent();
		SupplierPresenceSnapshot presence = result.getPresence();
		SupplierPresence expectedPresence = event.getDirection() == SupplierDirection.ENTER
				? SupplierPresence.INSIDE : SupplierPresence.OUTSIDE;
		if (!event.getVerificationId().equals(verification.getVerificationId())
				|| !event.getOperatorId().equals(verification.getOperatorId())
				|| !event.getPostId().equals(verification.getPostId())
				|| !event.getAreaId().equals(verification.getAreaId())
				|| event.getDirection() != requestedDirection
				|| event.getVersion() != verification.getPresenceVersion() + 1L
				|| presence.getVersion() != event.getVersion()
				|| presence.getPresence() != expectedPresence
				|| !presence.getPersonId().equals(verification.getQualificationSnapshot().getPersonId())
				|| !presence.getAreaId().equals(verification.getAreaId())
				|| !sameQualificationIdentity(event.getQualificationSnapshot(),
						verification.getQualificationSnapshot())) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
					"幂等命令原回复与核验不一致");
		}
	}

	private boolean sameQualificationIdentity(SupplierQualificationSnapshot left,
			SupplierQualificationSnapshot right) {
		return left.getBadgeId().equals(right.getBadgeId())
				&& left.getPersonId().equals(right.getPersonId())
				&& left.getCompanyId().equals(right.getCompanyId())
				&& left.getAdmissionId().equals(right.getAdmissionId());
	}

	private String operatorId(SupplierOperator operator) {
		if (operator == null) {
			throw new SupplierRuleViolation(SupplierRuleViolation.Code.MISSING_PERMISSION,
					"缺少服务端认证操作人");
		}
		return required(operator.getOperatorId(), "操作人标识");
	}

	private String required(String value, String label) {
		if (value == null || value.trim().isEmpty() || !value.equals(value.trim())
				|| value.length() > IDENTIFIER_LENGTH || containsControlCharacter(value)) {
			throw failure(SupplierPersistenceException.Code.INVALID_INPUT,
					label + "为空、含控制字符或超过长度限制");
		}
		return value;
	}

	private boolean containsControlCharacter(String value) {
		for (int index = 0; index < value.length(); index++) {
			if (Character.isISOControl(value.charAt(index))) {
				return true;
			}
		}
		return false;
	}

	private String encodeQualification(SupplierQualificationSnapshot qualification) {
		try {
			return codec.encodeQualification(qualification);
		} catch (IOException | RuntimeException error) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
					"无法编码供应商资格快照", error);
		}
	}

	private SupplierQualificationSnapshot decodeQualification(String json) {
		try {
			return codec.decodeQualification(json);
		} catch (IOException | RuntimeException error) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
					"无法恢复供应商资格快照", error);
		}
	}

	private String encodeResult(SupplierPassageResult result) {
		try {
			return codec.encodeResult(result);
		} catch (IOException | RuntimeException error) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
					"无法编码供应商通行原回复", error);
		}
	}

	private SupplierPassageResult decodeResult(String json) {
		try {
			return codec.decodeResult(json);
		} catch (IOException | RuntimeException error) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA,
					"无法恢复供应商通行原回复", error);
		}
	}

	private void setClob(PreparedStatement statement, int index, String value) throws SQLException {
		statement.setCharacterStream(index, new StringReader(value), value.length());
	}

	private String readClob(ResultSet rows, int index) throws SQLException {
		Reader reader = rows.getCharacterStream(index);
		if (reader == null) {
			return null;
		}
		try (Reader source = reader) {
			StringBuilder value = new StringBuilder();
			char[] buffer = new char[2048];
			int count;
			while ((count = source.read(buffer)) >= 0) {
				value.append(buffer, 0, count);
			}
			return value.toString();
		} catch (IOException error) {
			throw new SQLException("无法读取供应商持久化 JSON", error);
		}
	}

	private void setInstant(PreparedStatement statement, int index, Instant value) throws SQLException {
		statement.setTimestamp(index, Timestamp.from(value), utcCalendar());
	}

	private Instant instant(ResultSet rows, int index) throws SQLException {
		Timestamp value = rows.getTimestamp(index, utcCalendar());
		if (value == null) {
			throw failure(SupplierPersistenceException.Code.CORRUPT_DATA, "持久化时间字段为空");
		}
		return value.toInstant();
	}

	private Calendar utcCalendar() {
		return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}

	private boolean isUniqueConstraint(SQLException error) {
		for (SQLException current = error; current != null; current = current.getNextException()) {
			if (current.getErrorCode() == 1) {
				return true;
			}
		}
		return false;
	}

	private SupplierPersistenceException failure(SupplierPersistenceException.Code code,
			String message) {
		return new SupplierPersistenceException(code, message);
	}

	private SupplierPersistenceException failure(SupplierPersistenceException.Code code,
			String message, Throwable cause) {
		return new SupplierPersistenceException(code, message, cause);
	}

	private interface SqlWork<T> {
		T apply(Connection connection) throws SQLException;
	}

	private static final class StoredVerification {
		private final SupplierVerification verification;
		private final boolean consumed;

		private StoredVerification(SupplierVerification verification, boolean consumed) {
			this.verification = verification;
			this.consumed = consumed;
		}
	}

	private static final class CommandSpec {
		private final String scope;
		private final String actorId;
		private final String key;
		private final String digest;

		private CommandSpec(String scope, String actorId, String key, String digest) {
			this.scope = scope;
			this.actorId = actorId;
			this.key = key;
			this.digest = digest;
		}
	}

	private static final class CommandRecord {
		private final String digest;
		private final String replyJson;

		private CommandRecord(String digest, String replyJson) {
			this.digest = digest;
			this.replyJson = replyJson;
		}
	}

	private static final class ReplayContext {
		private final String verificationId;
		private final SupplierQualificationSnapshot qualification;
		private final SupplierOperator operator;
		private final SupplierPostAreaMapping postArea;
		private final SupplierDirection direction;
		private final Supplier<Instant> time;

		private ReplayContext(String verificationId, SupplierQualificationSnapshot qualification,
				SupplierOperator operator, SupplierPostAreaMapping postArea, SupplierDirection direction,
				Supplier<Instant> time) {
			this.verificationId = verificationId;
			this.qualification = qualification;
			this.operator = operator;
			this.postArea = postArea;
			this.direction = direction;
			this.time = time;
		}
	}

	private static final class CommandKeyCollision extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private CommandKeyCollision(SQLException cause) {
			super(cause);
		}
	}

	private static final class PresenceKeyCollision extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private PresenceKeyCollision(SQLException cause) {
			super(cause);
		}
	}
}
