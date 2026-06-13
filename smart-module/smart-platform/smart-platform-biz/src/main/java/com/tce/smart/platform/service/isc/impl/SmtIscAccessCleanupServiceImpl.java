package com.tce.smart.platform.service.isc.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.isc.IscAccessCleanupExecuteReqDTO;
import com.tce.smart.platform.api.dto.req.isc.IscAccessCleanupPageReqDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscAccessCleanupExecuteRespDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscAccessCleanupSummaryRespDTO;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtIscAccessCleanupMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.platform.core.vo.IscAccessCleanupRecordVO;
import com.tce.smart.platform.service.isc.SmtIscAccessCleanupService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SmtIscAccessCleanupServiceImpl implements SmtIscAccessCleanupService {

	private static final int EXECUTE_LIMIT = 5000;

	private static final String PERSON_TYPE_VISITOR = "VISITOR";

	private static final String PERSON_TYPE_STAFF = "STAFF";

	private static final String CLEANUP_EXECUTABLE = "EXECUTABLE";

	private static final String CLEANUP_PROTECTED = "PROTECTED";

	private static final String DELETE_TASK_MISSING = "MISSING";

	private static final String DELETE_TASK_WAITING = "WAITING";

	private static final String DELETE_TASK_DOING = "DOING";

	private static final String DELETE_TASK_RETRY = "RETRY";

	private final SmtIscAccessCleanupMapper cleanupMapper;

	private final SmtIscDownRecordService downRecordService;

	private final SmtIscDeviceTaskService deviceTaskService;

	private final SmtStaffMapper staffMapper;

	@Override
	public IPage<IscAccessCleanupRecordVO> getPage(Page page, IscAccessCleanupPageReqDTO query, List<Integer> allowedParkIds) {
		IscAccessCleanupPageReqDTO pageQuery = normalizeQuery(query, allowedParkIds);
		if (isNoParkAllowed(pageQuery)) {
			return new Page<>(page.getCurrent(), page.getSize());
		}
		Date now = new Date();
		IscAccessCleanupSummaryRespDTO summary = cleanupMapper.getSummary(pageQuery, now);
		page.setSearchCount(false);
		IPage<IscAccessCleanupRecordVO> recordPage = cleanupMapper.getPage(page, pageQuery, now);
		recordPage.setTotal(summary == null || summary.getTotalCount() == null ? 0 : summary.getTotalCount());
		decorateRecords(recordPage.getRecords());
		return recordPage;
	}

	@Override
	public IscAccessCleanupSummaryRespDTO getSummary(IscAccessCleanupPageReqDTO query, List<Integer> allowedParkIds) {
		IscAccessCleanupPageReqDTO pageQuery = normalizeQuery(query, allowedParkIds);
		if (isNoParkAllowed(pageQuery)) {
			return new IscAccessCleanupSummaryRespDTO();
		}
		IscAccessCleanupSummaryRespDTO summary = cleanupMapper.getSummary(pageQuery, new Date());
		return summary == null ? new IscAccessCleanupSummaryRespDTO() : summary;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public IscAccessCleanupExecuteRespDTO execute(IscAccessCleanupExecuteReqDTO reqDTO, List<Integer> allowedParkIds) {
		IscAccessCleanupExecuteReqDTO executeReq = reqDTO == null ? new IscAccessCleanupExecuteReqDTO() : reqDTO;
		List<Long> downRecordIds = resolveExecuteRecordIds(executeReq, allowedParkIds);
		IscAccessCleanupExecuteRespDTO result = new IscAccessCleanupExecuteRespDTO();
		for (Long downRecordId : downRecordIds) {
			increaseTotal(result);
			SmtIscDownRecord downRecord = downRecordService.getById(downRecordId);
			if (downRecord == null || !canAccessPark(downRecord.getParkId(), allowedParkIds)) {
				result.setSkipCount(result.getSkipCount() + 1);
				continue;
			}
			if (!canExecuteCleanup(downRecord)) {
				result.setSkipCount(result.getSkipCount() + 1);
				continue;
			}
			try {
				upsertDeleteTask(downRecord, result);
			} catch (Exception e) {
				result.setFailCount(result.getFailCount() + 1);
				log.error("生成ISC权限残留删除任务失败，downRecordId={}", downRecordId, e);
			}
		}
		return result;
	}

	private List<Long> resolveExecuteRecordIds(IscAccessCleanupExecuteReqDTO executeReq, List<Integer> allowedParkIds) {
		if (CollUtil.isNotEmpty(executeReq.getDownRecordIds())) {
			return executeReq.getDownRecordIds().stream()
					.filter(Objects::nonNull)
					.distinct()
					.collect(Collectors.toList());
		}
		IscAccessCleanupPageReqDTO query = new IscAccessCleanupPageReqDTO();
		BeanUtil.copyProperties(executeReq, query);
		query.setCleanupStatus(CLEANUP_EXECUTABLE);
		query = normalizeQuery(query, allowedParkIds);
		if (isNoParkAllowed(query)) {
			return Collections.emptyList();
		}
		return cleanupMapper.listRecords(query, new Date(), EXECUTE_LIMIT).stream()
				.filter(record -> !Boolean.TRUE.equals(hasActiveSamePerson(record)))
				.map(IscAccessCleanupRecordVO::getDownRecordId)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
	}

	private void upsertDeleteTask(SmtIscDownRecord downRecord, IscAccessCleanupExecuteRespDTO result) {
		List<SmtIscDeviceTask> existingDeleteTasks = deviceTaskService.list(buildDeleteTaskQuery(downRecord));
		List<SmtIscDeviceTask> deleteTasks = existingDeleteTasks == null
				? Collections.emptyList()
				: existingDeleteTasks.stream()
				.filter(task -> !hasReachedAuthConfigMaxRetryTimes(task))
				.collect(Collectors.toList());
		if (CollUtil.isNotEmpty(deleteTasks)) {
			for (SmtIscDeviceTask task : deleteTasks) {
				refreshDeleteTask(task);
				deviceTaskService.updateById(task);
				result.setUpdatedCount(result.getUpdatedCount() + 1);
			}
			return;
		}
		SmtIscDeviceTask deleteTask = buildDeleteTask(downRecord);
		if (deviceTaskService.save(deleteTask)) {
			result.setCreatedCount(result.getCreatedCount() + 1);
		} else {
			result.setFailCount(result.getFailCount() + 1);
		}
	}

	private SmtIscDeviceTask buildDeleteTask(SmtIscDownRecord downRecord) {
		long nowSeconds = DateUtil.currentSeconds();
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setAction(DeviceTaskActionEnum.DEL.getCode());
		task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		task.setDeviceType(downRecord.getDeviceType());
		task.setStartTime(nowSeconds);
		task.setOverTime(nowSeconds);
		task.setDeviceCode(downRecord.getDeviceCode());
		task.setCardNo(downRecord.getCardNo());
		task.setGeneral(downRecord.getGeneral());
		task.setImageId(downRecord.getImageId());
		task.setServiceType(deleteTaskServiceType(downRecord));
		task.setCreateTime(LocalDateTime.now());
		task.setOptUser(currentOptUser());
		task.setBadge(downRecord.getBadge());
		task.setPersonId(downRecord.getPersonId());
		return task;
	}

	private void refreshDeleteTask(SmtIscDeviceTask task) {
		task.setOverTime(DateUtil.currentSeconds());
		if (!Objects.equals(task.getStatus(), DeviceTaskStatusEnum.DOING.getCode())) {
			task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
			task.setIscTaskId(null);
			task.setRemark(null);
			task.setCode(null);
		}
		task.setOptUser(currentOptUser());
		task.setUpdateTime(LocalDateTime.now());
	}

	private LambdaQueryWrapper<SmtIscDeviceTask> buildDeleteTaskQuery(SmtIscDownRecord downRecord) {
		LambdaQueryWrapper<SmtIscDeviceTask> query = new LambdaQueryWrapper<SmtIscDeviceTask>()
				.eq(SmtIscDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
				.eq(SmtIscDeviceTask::getDeviceType, downRecord.getDeviceType())
				.eq(SmtIscDeviceTask::getDeviceCode, downRecord.getDeviceCode())
				.and(wrapper -> wrapper.isNull(SmtIscDeviceTask::getTimes)
						.or().lt(SmtIscDeviceTask::getTimes, DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES))
				.and(wrapper -> wrapper.isNull(SmtIscDeviceTask::getStatus)
						.or()
						.in(SmtIscDeviceTask::getStatus, reusableDeleteStatusCodes()));
		if (isStaffAccessRecord(downRecord)) {
			query.eq(SmtIscDeviceTask::getCardNo, downRecord.getCardNo())
					.in(SmtIscDeviceTask::getServiceType,
							DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskConstants.UPDATE_FACE);
			return query;
		}
		query.eq(SmtIscDeviceTask::getServiceType, downRecord.getServiceType());
		if (StrUtil.isNotBlank(downRecord.getPersonId())) {
			return query.eq(SmtIscDeviceTask::getPersonId, downRecord.getPersonId());
		}
		if (StrUtil.isNotBlank(downRecord.getBadge())) {
			return query.eq(SmtIscDeviceTask::getBadge, downRecord.getBadge());
		}
		return query.eq(SmtIscDeviceTask::getCardNo, downRecord.getCardNo());
	}

	private boolean hasActiveSameTemporaryAccess(SmtIscDownRecord downRecord) {
		LambdaQueryWrapper<SmtIscDownRecord> query = new LambdaQueryWrapper<SmtIscDownRecord>()
				.ne(SmtIscDownRecord::getId, downRecord.getId())
				.eq(SmtIscDownRecord::getDeviceType, DeviceTaskConstants.CARD)
				.in(SmtIscDownRecord::getServiceType,
						DeviceTaskConstants.CARD_VISITOR, DeviceTaskConstants.CARD_ADMITTANCE)
				.eq(SmtIscDownRecord::getDeviceCode, downRecord.getDeviceCode())
				.gt(SmtIscDownRecord::getOverTime, new Date());
		if (downRecord.getParkId() != null) {
			query.eq(SmtIscDownRecord::getParkId, downRecord.getParkId());
		} else {
			query.isNull(SmtIscDownRecord::getParkId);
		}
		query.and(wrapper -> {
			boolean hasIdentity = false;
			if (StrUtil.isNotBlank(downRecord.getPersonId())) {
				wrapper.eq(SmtIscDownRecord::getPersonId, downRecord.getPersonId());
				hasIdentity = true;
			}
			if (StrUtil.isNotBlank(downRecord.getBadge())) {
				if (hasIdentity) {
					wrapper.or();
				}
				wrapper.eq(SmtIscDownRecord::getBadge, downRecord.getBadge());
				hasIdentity = true;
			}
			if (!hasIdentity) {
				wrapper.eq(SmtIscDownRecord::getCardNo, downRecord.getCardNo());
			}
			return wrapper;
		});
		return downRecordService.count(query) > 0;
	}

	private void decorateRecords(List<IscAccessCleanupRecordVO> records) {
		if (CollUtil.isEmpty(records)) {
			return;
		}
		for (IscAccessCleanupRecordVO record : records) {
			record.setCleanupStatus(Boolean.TRUE.equals(hasActiveSamePerson(record)) ? CLEANUP_PROTECTED : CLEANUP_EXECUTABLE);
			record.setCleanupStatusDesc(CLEANUP_PROTECTED.equals(record.getCleanupStatus()) ? "保留" : "待处理");
			record.setReason(buildReason(record));
			decorateDeleteTask(record);
		}
	}

	private void decorateDeleteTask(IscAccessCleanupRecordVO record) {
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setCardNo(record.getCardNo());
		downRecord.setBadge(record.getBadge());
		downRecord.setPersonId(record.getPersonId());
		downRecord.setDeviceCode(record.getDeviceCode());
		downRecord.setDeviceType(DeviceTaskConstants.CARD);
		downRecord.setServiceType(record.getServiceType());
		List<SmtIscDeviceTask> deleteTasks = deviceTaskService.list(buildDeleteTaskQuery(downRecord));
		if (CollUtil.isEmpty(deleteTasks)) {
			record.setDeleteTaskStatus(DELETE_TASK_MISSING);
			record.setDeleteTaskStatusDesc("未生成");
			return;
		}
		SmtIscDeviceTask deleteTask = deleteTasks.get(0);
		record.setDeleteTaskId(deleteTask.getId());
		Integer status = deleteTask.getStatus();
		if (Objects.equals(status, DeviceTaskStatusEnum.DOING.getCode())) {
			record.setDeleteTaskStatus(DELETE_TASK_DOING);
			record.setDeleteTaskStatusDesc("删除中");
			return;
		}
		if (Objects.equals(status, DeviceTaskStatusEnum.FAIL.getCode())
				|| Objects.equals(status, DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode())) {
			record.setDeleteTaskStatus(DELETE_TASK_RETRY);
			record.setDeleteTaskStatusDesc("待重试");
			return;
		}
		record.setDeleteTaskStatus(DELETE_TASK_WAITING);
		record.setDeleteTaskStatusDesc("待执行");
	}

	private String buildReason(IscAccessCleanupRecordVO record) {
		if (CLEANUP_PROTECTED.equals(record.getCleanupStatus())) {
			return "同一人员在当前设备存在未到期权限";
		}
		if (PERSON_TYPE_STAFF.equals(record.getPersonType())) {
			return "人员已离职但设备下发记录仍存在";
		}
		return "访客权限已到期且未发现同人同设备有效权限";
	}

	private Boolean hasActiveSamePerson(IscAccessCleanupRecordVO record) {
		return record != null && Integer.valueOf(1).equals(record.getHasActiveSamePerson());
	}

	private IscAccessCleanupPageReqDTO normalizeQuery(IscAccessCleanupPageReqDTO query, List<Integer> allowedParkIds) {
		IscAccessCleanupPageReqDTO pageQuery = query == null ? new IscAccessCleanupPageReqDTO() : query;
		pageQuery.setParkIds(allowedParkIds);
		return pageQuery;
	}

	private boolean isNoParkAllowed(IscAccessCleanupPageReqDTO query) {
		return query.getParkIds() != null && query.getParkIds().isEmpty();
	}

	private boolean canAccessPark(Integer parkId, List<Integer> allowedParkIds) {
		return allowedParkIds == null || allowedParkIds.contains(parkId);
	}

	private boolean isSupportedCleanupRecord(SmtIscDownRecord downRecord) {
		return isTemporaryAccessRecord(downRecord) || isStaffAccessRecord(downRecord);
	}

	private boolean canExecuteCleanup(SmtIscDownRecord downRecord) {
		if (!isSupportedCleanupRecord(downRecord)) {
			return false;
		}
		if (isTemporaryAccessRecord(downRecord)) {
			return downRecord.getOverTime() != null
					&& !downRecord.getOverTime().after(new Date())
					&& !hasActiveSameTemporaryAccess(downRecord);
		}
		return isQuitStaff(downRecord.getCardNo());
	}

	private boolean isQuitStaff(String cardNo) {
		Long staffId = parseStaffId(cardNo);
		if (staffId == null) {
			return false;
		}
		SmtStaff staff = staffMapper.selectById(staffId);
		return staff != null && Objects.equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode(), staff.getStatus());
	}

	private Long parseStaffId(String cardNo) {
		if (StrUtil.isBlank(cardNo)) {
			return null;
		}
		try {
			return Long.parseLong(cardNo);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private boolean isTemporaryAccessRecord(SmtIscDownRecord downRecord) {
		return downRecord != null
				&& DeviceTaskConstants.CARD.equals(downRecord.getDeviceType())
				&& (DeviceTaskConstants.CARD_VISITOR.equals(downRecord.getServiceType())
				|| DeviceTaskConstants.CARD_ADMITTANCE.equals(downRecord.getServiceType()));
	}

	private boolean isStaffAccessRecord(SmtIscDownRecord downRecord) {
		return downRecord != null
				&& DeviceTaskConstants.CARD.equals(downRecord.getDeviceType())
				&& (DeviceTaskConstants.CARD_STAFF_IMPORT.equals(downRecord.getServiceType())
				|| DeviceTaskConstants.UPDATE_FACE.equals(downRecord.getServiceType()));
	}

	private Integer deleteTaskServiceType(SmtIscDownRecord downRecord) {
		if (isStaffAccessRecord(downRecord)) {
			return DeviceTaskConstants.CARD_STAFF_IMPORT;
		}
		return downRecord.getServiceType();
	}

	private List<Integer> reusableDeleteStatusCodes() {
		return Arrays.asList(
				DeviceTaskStatusEnum.INIT.getCode(),
				DeviceTaskStatusEnum.DOING.getCode(),
				DeviceTaskStatusEnum.FAIL.getCode(),
				DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode());
	}

	private boolean hasReachedAuthConfigMaxRetryTimes(SmtIscDeviceTask task) {
		return task.getTimes() != null && task.getTimes() >= DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES;
	}

	private void increaseTotal(IscAccessCleanupExecuteRespDTO result) {
		result.setTotalCount(result.getTotalCount() + 1);
	}

	private String currentOptUser() {
		try {
			return SecurityUtils.getUser() == null ? "sys" : SecurityUtils.getUser().getUsername();
		} catch (Exception e) {
			return "sys";
		}
	}
}
