package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ISC 任务终态聚合回写器：入厂申请「状态诚实化」的最终落点。
 *
 * <p>背景：{@code smt_isc_device_task} 每条任务记录的是单人单设备的下发/删除结果，
 * 而 {@code smt_admittance_apply.device_status} 需要反映整批申请（同一 {@code iscSubmitBatch}）
 * 的真实进度。本类在任务状态被写为「终态」后，按人员维度聚合出批次级判定，
 * 并把判定结果谨慎地回写到申请单，避免覆盖人工重发等新状态。回写守卫按判定结果
 * 非对称（详见 {@link #SUCCESS_OVERWRITABLE_DEVICE_STATUSES}）：FAIL 判定只能覆盖
 * 过渡态，SUCCESS 判定额外允许覆盖 FAIL——因为 DEVICE_OFFLINE 会被调度自动重新拾取，
 * 一次「先离线判假失败、设备恢复后又成功」的时序如果不允许 SUCCESS 覆盖 FAIL，
 * 申请单会永久冻结在假失败状态，重试机制对这种条件性不匹配无能为力。</p>
 *
 * <p>判定规则（业务口径，非技术细节）：</p>
 * <ul>
 *   <li>人员维度：同一人（{@code cardNo} 对应的 fellowId）名下任一任务 SUCCESS 即该人成功；
 *       全部任务终态且没有 SUCCESS 即该人失败；否则该人仍在途。</li>
 *   <li>批次维度：只统计当前批次（{@code apply.iscSubmitBatch}）参与判定的人员——
 *       批次内全部人员成功 → 整批成功；任一人员失败 → 整批失败；否则在途，不回写。</li>
 *   <li>批次内完全没有任务、或没有任何人员参与判定，视为在途，不回写（避免误判空批次为失败）。</li>
 * </ul>
 */
@Slf4j
public class AdmittanceDispatchAggregator {

	/**
	 * 终态集合：{@link DeviceTaskStatusEnum} 中除 SUCCESS（成功终态另计）与非终态
	 * （INIT 初始化、DOING 处理中）外的全部状态，即 FAIL/CANCEL/EXPIRED/DEVICE_OFFLINE。
	 * 以枚举实际取值为准，避免硬编码魔法数字。
	 */
	private static final Set<Integer> FAILURE_TERMINAL_STATUSES = EnumSet.of(
			DeviceTaskStatusEnum.FAIL,
			DeviceTaskStatusEnum.CANCEL,
			DeviceTaskStatusEnum.EXPIRED,
			DeviceTaskStatusEnum.DEVICE_OFFLINE
	).stream().map(DeviceTaskStatusEnum::getCode).collect(Collectors.toSet());

	/** 回写失败后的重试次数（不含首次调用） */
	private static final int WRITEBACK_RETRY_TIMES = 2;

	/** 过渡态/在途语义 device_status：下发中(3)、已下发(4)。两个守卫集合的公共基线。 */
	private static final Set<Integer> OVERWRITABLE_DEVICE_STATUSES = CollUtil.newHashSet(
			DeviceDownStatusEnum.IN_WORK.getCode(), DeviceDownStatusEnum.ALRAEDY.getCode());

	/**
	 * SUCCESS 判定的回写守卫集合：过渡态(3,4) + 失败终态(2)。
	 *
	 * <p>非对称设计的原因——{@code DeviceTaskStatusEnum.DEVICE_OFFLINE} 被计入
	 * {@link #FAILURE_TERMINAL_STATUSES}（失败终态），但设备离线的任务会被调度自动
	 * 重新拾取重试（{@code getCardDown} 把 status=6 与 status=0 同等对待，视为待下发）。
	 * 这意味着聚合可能先因设备离线判定 FAIL 并把 device_status 写成 2，随后设备恢复、
	 * 任务重试成功，聚合重算得到 SUCCESS——此时如果 UPDATE 守卫仍是 {@code IN (3,4)}，
	 * 条件永远匹配不到当前值 2 的行，重试机制对这种「条件性不匹配」无能为力
	 * （不是并发竞争，是逻辑上永久不满足），申请单会被冻结在假失败状态，只能人工重发。</p>
	 *
	 * <p>因此 SUCCESS 判定允许覆盖 FAIL(2)：成功是比失败更确定的终态判定——
	 * 全部参与人员的任务都拿到了 SUCCESS，没有理由让一个可能已经过时的失败标记挡住它。
	 * 不覆盖 SUCCESS(1) 本身，保持幂等（同一批次重复聚合出 SUCCESS 不应重复触发 UPDATE 语义）。</p>
	 */
	private static final Set<Integer> SUCCESS_OVERWRITABLE_DEVICE_STATUSES = CollUtil.newHashSet(
			DeviceDownStatusEnum.FAIL.getCode(),
			DeviceDownStatusEnum.IN_WORK.getCode(), DeviceDownStatusEnum.ALRAEDY.getCode());

	/**
	 * FAIL 判定的回写守卫集合：只允许覆盖过渡态(3,4)，维持原有保守口径。
	 *
	 * <p>失败判定不得推翻任何「更进一步」的状态——不覆盖 SUCCESS(1)（已经成功的不该被
	 * 事后判失败打回去），也不覆盖 FAIL(2) 本身（保持幂等，避免重复聚合反复触发 UPDATE）。
	 * 这与 SUCCESS 守卫的非对称正是问题修复的核心：失败推翻成功没有正当性，
	 * 但成功推翻（可能因离线自动重拾取而过时的）失败是合理的。</p>
	 */
	private static final Set<Integer> FAIL_OVERWRITABLE_DEVICE_STATUSES = OVERWRITABLE_DEVICE_STATUSES;

	private final SmtIscDeviceTaskService smtIscDeviceTaskService;

	private final SmtAdmittanceApplyMapper smtAdmittanceApplyMapper;

	public AdmittanceDispatchAggregator(SmtIscDeviceTaskService smtIscDeviceTaskService,
			SmtAdmittanceApplyMapper smtAdmittanceApplyMapper) {
		this.smtIscDeviceTaskService = smtIscDeviceTaskService;
		this.smtAdmittanceApplyMapper = smtAdmittanceApplyMapper;
	}

	/**
	 * 批次级判定结果。
	 */
	public enum BatchVerdict {
		/** 批次内参与判定的人员全部成功 */
		SUCCESS,
		/** 批次内至少一名参与判定的人员判定为失败 */
		FAIL,
		/** 仍有人员在途（存在非终态任务），或没有任何人员参与判定 */
		IN_PROGRESS
	}

	/**
	 * 单人判定结果。
	 */
	private enum PersonVerdict {
		SUCCESS, FAIL, IN_PROGRESS
	}

	/**
	 * 对外主入口：按入厂申请ID聚合其当前批次（{@code apply.iscSubmitBatch}）的全部 ISC 任务，
	 * 终态才回写 {@code device_status}（成功写1，失败写2）；在途不动。
	 *
	 * <p>只对 {@code task.getApplyId() != null} 的任务生效——非入厂申请来源的ISC任务不参与聚合。</p>
	 *
	 * @param applyId 入厂申请单ID
	 */
	public void aggregate(Long applyId) {
		if (applyId == null) {
			return;
		}
		SmtAdmittanceApply apply = smtAdmittanceApplyMapper.selectById(applyId);
		if (apply == null || apply.getIscSubmitBatch() == null) {
			// 申请单不存在，或从未成功提交过ISC批次：无批次可聚合
			return;
		}
		Long batchId = apply.getIscSubmitBatch();

		// 一次SQL取批次内全部ISC任务（card_no, status），apply_id为空的任务不参与（非入厂申请来源）
		List<SmtIscDeviceTask> batchTasks = smtIscDeviceTaskService.list(
				new LambdaQueryWrapper<SmtIscDeviceTask>()
						.select(SmtIscDeviceTask::getCardNo, SmtIscDeviceTask::getStatus)
						.eq(SmtIscDeviceTask::getApplyId, applyId)
						.eq(SmtIscDeviceTask::getBatchId, batchId));

		Map<Long, List<Integer>> tasksByFellow = groupTaskStatusByFellow(batchTasks);
		BatchVerdict verdict = verdict(tasksByFellow);
		if (verdict == BatchVerdict.IN_PROGRESS) {
			// 仍在途或无有效批次任务：不回写，保持申请单当前device_status
			return;
		}
		Integer targetDeviceStatus = verdict == BatchVerdict.SUCCESS
				? DeviceDownStatusEnum.SUCCESS.getCode()
				: DeviceDownStatusEnum.FAIL.getCode();
		writebackDeviceStatus(smtAdmittanceApplyMapper, applyId, batchId, verdict, targetDeviceStatus);
	}

	/**
	 * 把批次任务按人员（cardNo 解析为 fellowId）分组为 {fellowId -> 任务状态列表}。
	 * cardNo 解析失败（既有约定：cardNo 存 fellowId 数字字符串）的任务跳过并 WARN，不参与聚合。
	 */
	private Map<Long, List<Integer>> groupTaskStatusByFellow(List<SmtIscDeviceTask> batchTasks) {
		Map<Long, List<Integer>> tasksByFellow = new LinkedHashMap<>();
		if (CollUtil.isEmpty(batchTasks)) {
			return tasksByFellow;
		}
		for (SmtIscDeviceTask task : batchTasks) {
			Long fellowId = parseFellowId(task.getCardNo());
			if (fellowId == null) {
				log.warn("ISC任务聚合：cardNo[{}]无法解析为fellowId，跳过该任务", task.getCardNo());
				continue;
			}
			tasksByFellow.computeIfAbsent(fellowId, key -> new ArrayList<>()).add(task.getStatus());
		}
		return tasksByFellow;
	}

	private Long parseFellowId(String cardNo) {
		if (StrUtil.isBlank(cardNo)) {
			return null;
		}
		try {
			return Long.parseLong(cardNo);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 判定纯函数：输入「人员 -> 任务状态列表」，输出批次级判定。
	 *
	 * <p>规则：</p>
	 * <ul>
	 *   <li>没有任何参与判定的人员（Map为空，或全部人员的任务列表都为空）→ IN_PROGRESS（不回写）。</li>
	 *   <li>任一人员判定为 FAIL（该人全部任务终态且无SUCCESS）→ 整批 FAIL。</li>
	 *   <li>存在人员判定为 IN_PROGRESS（该人有非终态任务）且没有人员判定为 FAIL → 整批 IN_PROGRESS。</li>
	 *   <li>其余情况（全部参与判定的人员都判定为 SUCCESS）→ 整批 SUCCESS。</li>
	 * </ul>
	 *
	 * @param tasksByFellow 人员维度的任务状态列表；value 为空 List 表示该人员在本批次没有ISC任务，不参与判定
	 */
	public static BatchVerdict verdict(Map<Long, List<Integer>> tasksByFellow) {
		if (CollUtil.isEmpty(tasksByFellow)) {
			return BatchVerdict.IN_PROGRESS;
		}
		boolean hasParticipant = false;
		boolean hasInProgress = false;
		for (List<Integer> statuses : tasksByFellow.values()) {
			if (CollUtil.isEmpty(statuses)) {
				// 该人员在本批次没有ISC任务，边界排除，不参与判定
				continue;
			}
			hasParticipant = true;
			PersonVerdict personVerdict = personVerdict(statuses);
			if (personVerdict == PersonVerdict.FAIL) {
				// 任一人员失败，整批立即判定为失败
				return BatchVerdict.FAIL;
			}
			if (personVerdict == PersonVerdict.IN_PROGRESS) {
				hasInProgress = true;
			}
		}
		if (!hasParticipant) {
			// 整批没有任何参与判定的人员（如批次内全无任务）：视为在途，不误判失败
			return BatchVerdict.IN_PROGRESS;
		}
		return hasInProgress ? BatchVerdict.IN_PROGRESS : BatchVerdict.SUCCESS;
	}

	/**
	 * 单人判定纯函数：任一任务 SUCCESS → 成功；全部任务终态且无SUCCESS → 失败；否则在途。
	 */
	private static PersonVerdict personVerdict(List<Integer> statuses) {
		boolean anySuccess = false;
		boolean allTerminal = true;
		for (Integer status : statuses) {
			if (DeviceTaskStatusEnum.SUCCESS.getCode().equals(status)) {
				anySuccess = true;
				continue;
			}
			if (!FAILURE_TERMINAL_STATUSES.contains(status)) {
				// 非终态（INIT/DOING，或未知状态一律按在途处理，不冒险判定）
				allTerminal = false;
			}
		}
		if (anySuccess) {
			return PersonVerdict.SUCCESS;
		}
		return allTerminal ? PersonVerdict.FAIL : PersonVerdict.IN_PROGRESS;
	}

	/**
	 * 回写 {@code device_status}：守卫集合按判定结果非对称（见
	 * {@link #SUCCESS_OVERWRITABLE_DEVICE_STATUSES} / {@link #FAIL_OVERWRITABLE_DEVICE_STATUSES}
	 * 上的注释）——SUCCESS 判定额外允许覆盖 FAIL(2)，FAIL 判定只能覆盖过渡态(3,4)。
	 *
	 * <p>UPDATE 返回 0 行时含义不再单一：可能是「真正的写冲突/异常」（行值仍在守卫集合内，
	 * 但这一次 UPDATE 没生效，值得重试），也可能是「条件性不匹配」（申请单当前 device_status
	 * 已经不在本次 verdict 的守卫集合内，例如 SUCCESS 判定却发现库里已经是 SUCCESS(1)，
	 * 或者 FAIL 判定却发现库里已经被 SUCCESS 判定覆盖为 1）——后一种情况重试没有意义，
	 * 因为条件不会在下一次尝试中变化，重试 3 次只是白白空转。因此 0 行时先查一次当前
	 * device_status，按其是否仍落在守卫集合内分流：不在集合内 → 记 INFO 直接返回；
	 * 仍在集合内却没更新成功 → 视为真正异常，继续重试。</p>
	 *
	 * <p>包可见 + static，便于单测直接驱动"重试仍失败记ERROR"这一行为，不依赖聚合器完整装配。</p>
	 */
	static void writebackDeviceStatus(SmtAdmittanceApplyMapper applyMapper, Long applyId, Long batchId,
			BatchVerdict verdict, Integer targetDeviceStatus) {
		Set<Integer> guardStatuses = verdict == BatchVerdict.SUCCESS
				? SUCCESS_OVERWRITABLE_DEVICE_STATUSES
				: FAIL_OVERWRITABLE_DEVICE_STATUSES;
		int attempts = 1 + WRITEBACK_RETRY_TIMES;
		for (int attempt = 1; attempt <= attempts; attempt++) {
			int updated = applyMapper.update(null, new LambdaUpdateWrapper<SmtAdmittanceApply>()
					.set(SmtAdmittanceApply::getDeviceStatus, targetDeviceStatus)
					.eq(SmtAdmittanceApply::getId, applyId)
					.in(SmtAdmittanceApply::getDeviceStatus, guardStatuses));
			if (updated > 0) {
				log.info("ISC任务聚合回写device_status成功：applyId={}, batchId={}, verdict={}, targetDeviceStatus={}, 尝试次数={}",
						applyId, batchId, verdict, targetDeviceStatus, attempt);
				return;
			}
			// 0 行：先查当前值，区分「条件性不匹配（不值得重试）」与「真正的写冲突（值得重试）」
			SmtAdmittanceApply current = applyMapper.selectById(applyId);
			Integer currentDeviceStatus = current == null ? null : current.getDeviceStatus();
			if (!guardStatuses.contains(currentDeviceStatus)) {
				log.info("ISC任务聚合回写device_status跳过（当前device_status={}已不在本次判定的守卫集合{}内，视为已被更进一步的状态推进，不重试）："
								+ "applyId={}, batchId={}, verdict={}, targetDeviceStatus={}",
						currentDeviceStatus, guardStatuses, applyId, batchId, verdict, targetDeviceStatus);
				return;
			}
			log.warn("ISC任务聚合回写device_status未生效（当前device_status={}仍在守卫集合内，判定为写冲突，将重试）：applyId={}, batchId={}, verdict={}, targetDeviceStatus={}, 第{}次尝试",
					currentDeviceStatus, applyId, batchId, verdict, targetDeviceStatus, attempt);
		}
		log.error("ISC任务聚合回写device_status彻底失败，已重试{}次：applyId={}, batchId={}, verdict={}, targetDeviceStatus={}",
				WRITEBACK_RETRY_TIMES, applyId, batchId, verdict, targetDeviceStatus);
	}
}
