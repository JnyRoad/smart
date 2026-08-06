package com.tce.smart.platform.service.energy;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.core.entity.energy.SmtEnergyIngestionLedger;
import com.tce.smart.platform.core.mapper.energy.SmtEnergyIngestionLedgerMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * 以来源事件标识建立读数接入账本。该服务只登记事件，调用方必须在同一事务内完成历史入库和投影入队。
 */
@Slf4j
@Service
public class EnergyReadingIngestionService {
	private static final int SOURCE_EVENT_ID_MAX_LENGTH = 128;
	private final SmtEnergyIngestionLedgerMapper ledgerMapper;

	public EnergyReadingIngestionService(SmtEnergyIngestionLedgerMapper ledgerMapper) {
		this.ledgerMapper = ledgerMapper;
	}

	/**
	 * 原子登记事件。返回 true 表示本次首次接入，false 表示相同载荷的安全重投。
	 */
	public boolean register(RegisterCommand command, String payloadHash, String eventPayload) {
		if (command == null || isBlank(payloadHash) || eventPayload == null) {
			throw new SmartException("能耗读数事件参数不完整");
		}
		String sourceEventId = resolveSourceEventId(command);
		SmtEnergyIngestionLedger ledger = SmtEnergyIngestionLedger.builder()
				.id(IdWorker.getId())
				.sourceEventId(sourceEventId)
				.eventType(command.getEventType())
				.parkId(command.getParkId())
				.meterSource(command.getMeterSource())
				.meterId(command.getMeterId())
				.eventTime(command.getEventTime())
				.payloadHash(payloadHash)
				.eventPayload(eventPayload)
				.ingestedAt(LocalDateTime.now())
				.build();
		if (ledgerMapper.insertIgnoreDuplicate(ledger) == 1) {
			return true;
		}
		String existingHash = ledgerMapper.selectPayloadHash(sourceEventId);
		if (payloadHash.equals(existingHash)) {
			return false;
		}
		throw new SmartException("能耗读数来源事件标识冲突");
	}

	/**
	 * 旧集中器没有事件标识时，以完整业务载荷生成稳定哈希，保证同一旧消息可安全重投。
	 */
	public String resolveSourceEventId(RegisterCommand command) {
		String provided = command.getSourceEventId();
		if (provided != null) {
			if (isBlank(provided)) {
				throw new SmartException("能耗读数来源事件标识不能为空白");
			}
			if (provided.length() > SOURCE_EVENT_ID_MAX_LENGTH) {
				throw new SmartException("能耗读数来源事件标识长度超过128");
			}
			return provided;
		}
		String fallback = sha256(command.getMeterSource() + "\u001f" + command.getDeviceCode() + "\u001f"
				+ command.getSequence() + "\u001f" + command.getEventTime() + "\u001f" + command.getReading() + "\u001f" + command.getValveState());
		log.warn("旧版集中器读数缺少sourceEventId，使用稳定兼容标识, meterSource={}, deviceCode={}, sequence={}, collectTime={}",
				command.getMeterSource(), command.getDeviceCode(), command.getSequence(), command.getEventTime());
		return fallback;
	}

	/** 对原始载荷生成审计哈希。 */
	public String hashPayload(String eventPayload) {
		return sha256(eventPayload);
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private static String sha256(String value) {
		try {
			byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder result = new StringBuilder(64);
			for (byte item : bytes) {
				result.append(String.format("%02x", item));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("当前运行环境不支持SHA-256", ex);
		}
	}

	/** 已完成输入校验并可入账的统一读数事件。 */
	@Getter
	@AllArgsConstructor
	public static class RegisterCommand {
		private final String sourceEventId;
		private final String eventType;
		private final String meterSource;
		private final Long parkId;
		private final Long meterId;
		private final String deviceCode;
		private final Integer sequence;
		private final String reading;
		private final Integer valveState;
		private final LocalDateTime eventTime;
	}
}
