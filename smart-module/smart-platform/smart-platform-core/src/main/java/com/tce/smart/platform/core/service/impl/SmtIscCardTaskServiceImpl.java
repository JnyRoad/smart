package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.isc.IscCardTaskPageReqDTO;
import com.tce.smart.platform.core.entity.SmtIscCardTask;
import com.tce.smart.platform.core.mapper.SmtIscCardTaskMapper;
import com.tce.smart.platform.core.service.SmtIscCardTaskService;
import com.tce.smart.platform.core.service.SmtIscStaffCardService;
import com.tce.smart.platform.core.vo.IscCardTaskPageVO;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

@Slf4j
@Service
public class SmtIscCardTaskServiceImpl extends ServiceImpl<SmtIscCardTaskMapper, SmtIscCardTask>
		implements SmtIscCardTaskService {

	private static final String SOURCE_TYPE_STAFF = "STAFF";
	private static final String ISC_VIRTUAL_CARD_PREFIX = "999";
	private static final String ISC_CARD_NO_PATTERN = "[0-9A-Z]{8,20}";
	private static final int ADD_CARD_PRIORITY = 50;
	private static final int DELETE_CARD_PRIORITY = 100;

	@Autowired
	private SmtIscStaffCardService smtIscStaffCardService;

	@Override
	public boolean createAddStaffCardTask(Long staffId, String badge, Integer parkId, String cardNo) {
		return createStaffCardTask(staffId, badge, parkId, cardNo, DeviceTaskActionEnum.DOWN.getCode(), null);
	}

	@Override
	public boolean createDeleteStaffCardTask(Long staffId, String badge, Integer parkId, String cardNo) {
		String normalizedCardNo = normalizeCardNo(cardNo);
		if (StrUtil.isBlank(normalizedCardNo) || isVirtualCardNo(normalizedCardNo)) {
			return true;
		}
		validateRequired(staffId, badge, parkId);
		validateCardNo(normalizedCardNo);
		String personId = this.baseMapper.getLatestPersonId(badge, parkId, normalizedCardNo);
		return createStaffCardTask(staffId, badge, parkId, normalizedCardNo, DeviceTaskActionEnum.DEL.getCode(), personId);
	}

	@Override
	public boolean isCurrentStaffCardAddTask(SmtIscCardTask task) {
		if (task == null || !DeviceTaskActionEnum.DOWN.getCode().equals(task.getAction())) {
			return true;
		}
		if (!SOURCE_TYPE_STAFF.equals(task.getSourceType()) || task.getSourceId() == null) {
			return true;
		}
		if (task.getParkId() == null) {
			return false;
		}
		return smtIscStaffCardService.isActiveStaffCard(task.getSourceId(), normalizeText(task.getBadge()),
				task.getParkId(), normalizeCardNo(task.getCardNo()));
	}

	@Override
	public boolean updateDoingTask(SmtIscCardTask task, String expectedActiveKey, String expectedLeaseToken) {
		if (task == null || task.getId() == null) {
			return false;
		}
		LambdaUpdateWrapper<SmtIscCardTask> wrapper = new LambdaUpdateWrapper<SmtIscCardTask>()
				.eq(SmtIscCardTask::getId, task.getId())
				.eq(SmtIscCardTask::getStatus, DeviceTaskStatusEnum.DOING.getCode());
		if (StrUtil.isBlank(expectedActiveKey)) {
			wrapper.isNull(SmtIscCardTask::getActiveKey);
		} else {
			wrapper.eq(SmtIscCardTask::getActiveKey, expectedActiveKey);
		}
		if (StrUtil.isBlank(expectedLeaseToken)) {
			wrapper.isNull(SmtIscCardTask::getLeaseToken);
		} else {
			wrapper.eq(SmtIscCardTask::getLeaseToken, expectedLeaseToken);
		}
		return this.update(task, wrapper);
	}

	@Override
	public IPage<SmtIscCardTask> getPendingTasks(Page page, long currentTime, int maxRetryTimes) {
		return this.baseMapper.getPendingTasks(page, currentTime, maxRetryTimes);
	}

	@Override
	public boolean stopExceededRetryCardTasks(int maxRetryTimes, String remark) {
		if (maxRetryTimes <= 0) {
			log.warn("ISC卡片任务最大重试次数配置无效：{}", maxRetryTimes);
			return false;
		}
		// 先查后停：新增类任务停止后需同步收敛SMT_ISC_STAFF_CARD的同步状态，否则卡记录停留在"同步中"
		List<SmtIscCardTask> exceededTasks = this.list(new LambdaQueryWrapper<SmtIscCardTask>()
				.and(wrapper -> wrapper.in(SmtIscCardTask::getStatus,
								DeviceTaskStatusEnum.INIT.getCode(), DeviceTaskStatusEnum.DOING.getCode())
						.or().isNull(SmtIscCardTask::getStatus))
				.and(wrapper -> wrapper.ne(SmtIscCardTask::getCode, 200)
						.or().isNull(SmtIscCardTask::getCode))
				.ge(SmtIscCardTask::getTimes, maxRetryTimes));
		if (exceededTasks.isEmpty()) {
			return false;
		}
		boolean anyStopped = false;
		int stoppedCount = 0;
		for (SmtIscCardTask task : exceededTasks) {
			// 守卫与查询条件一致：list与update之间任务可能已被并发实例处理成功，不能改回FAIL
			boolean stopped = this.update(new LambdaUpdateWrapper<SmtIscCardTask>()
					.set(SmtIscCardTask::getStatus, DeviceTaskStatusEnum.FAIL.getCode())
					.set(SmtIscCardTask::getRemark, remark)
					.set(SmtIscCardTask::getActiveKey, null)
					.set(SmtIscCardTask::getLeaseToken, null)
					.set(SmtIscCardTask::getRunningKey, null)
					.set(SmtIscCardTask::getUpdateTime, LocalDateTime.now())
					.eq(SmtIscCardTask::getId, task.getId())
					.ge(SmtIscCardTask::getTimes, maxRetryTimes)
					.and(wrapper -> wrapper.in(SmtIscCardTask::getStatus,
									DeviceTaskStatusEnum.INIT.getCode(), DeviceTaskStatusEnum.DOING.getCode())
							.or().isNull(SmtIscCardTask::getStatus))
					.and(wrapper -> wrapper.ne(SmtIscCardTask::getCode, 200)
							.or().isNull(SmtIscCardTask::getCode)));
			if (!stopped) {
				continue;
			}
			anyStopped = true;
			stoppedCount++;
			if (DeviceTaskActionEnum.DOWN.getCode().equals(task.getAction())) {
				task.setStatus(DeviceTaskStatusEnum.FAIL.getCode());
				task.setRemark(remark);
				smtIscStaffCardService.markAddTaskFailed(task, false);
			}
		}
		if (anyStopped) {
			log.info("已停止达到最大重试次数的ISC卡片任务：{}条，最大重试次数：{}", stoppedCount, maxRetryTimes);
		}
		return anyStopped;
	}

	@Override
	public IPage<IscCardTaskPageVO> getPage(Page page, IscCardTaskPageReqDTO query) {
		IscCardTaskPageReqDTO pageQuery = query == null ? new IscCardTaskPageReqDTO() : query;
		if (CollectionUtils.isEmpty(pageQuery.getParkIds())) {
			return emptyPage(page);
		}
		if (pageQuery.getParkId() != null && !pageQuery.getParkIds().contains(pageQuery.getParkId())) {
			return emptyPage(page);
		}
		return this.baseMapper.getPage(page, pageQuery);
	}

	@Override
	public boolean markDoing(SmtIscCardTask task, long currentTime, long doingDeadlineTime, int maxRetryTimes) {
		if (task == null || task.getId() == null) {
			return false;
		}
		String leaseToken = IdUtil.simpleUUID();
		String runningKey = buildRunningKey(task);
		LocalDateTime updateTime = LocalDateTime.now();
		try {
			this.baseMapper.releaseExpiredRunningTask(runningKey, currentTime, updateTime);
			boolean marked = this.baseMapper.markDoing(task.getId(), currentTime, doingDeadlineTime,
					leaseToken, runningKey, updateTime, maxRetryTimes) > 0;
			if (marked) {
				task.setStatus(DeviceTaskStatusEnum.DOING.getCode());
				task.setOverTime(doingDeadlineTime);
				task.setLeaseToken(leaseToken);
				task.setRunningKey(runningKey);
				task.setUpdateTime(updateTime);
			}
			return marked;
		} catch (DuplicateKeyException e) {
			log.info("ISC卡片任务同组已有执行中任务，taskId={}, runningKey={}", task.getId(), runningKey);
			return false;
		}
	}

	private boolean createStaffCardTask(Long staffId, String badge, Integer parkId, String cardNo,
										Integer action, String personId) {
		String normalizedCardNo = normalizeCardNo(cardNo);
		if (StrUtil.isBlank(normalizedCardNo) || isVirtualCardNo(normalizedCardNo)) {
			return true;
		}
		validateRequired(staffId, badge, parkId);
		validateCardNo(normalizedCardNo);
		String activeKey = buildActiveKey(staffId, badge, parkId, normalizedCardNo, action);
		if (activeTaskExists(activeKey)) {
			log.info("ISC卡片任务已存在，staffId={}, badge={}, parkId={}, cardNo={}, action={}",
					staffId, badge, parkId, normalizedCardNo, action);
			return true;
		}
		SmtIscCardTask task = new SmtIscCardTask();
		task.setAction(action);
		task.setPriority(taskPriority(action));
		task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		task.setParkId(parkId);
		task.setSourceType(SOURCE_TYPE_STAFF);
		task.setSourceId(staffId);
		task.setBadge(badge);
		task.setPersonId(personId);
		task.setCardNo(normalizedCardNo);
		task.setActiveKey(activeKey);
		task.setTimes(0);
		task.setOverTime(DateUtil.currentSeconds());
		task.setCreateTime(LocalDateTime.now());
		task.setOptUser(currentUsername());
		try {
			return this.save(task);
		} catch (DuplicateKeyException e) {
			log.info("ISC卡片任务已被并发创建，staffId={}, badge={}, parkId={}, cardNo={}, action={}",
					staffId, badge, parkId, normalizedCardNo, action);
			return true;
		}
	}

	private boolean activeTaskExists(String activeKey) {
		return this.count(new LambdaQueryWrapper<SmtIscCardTask>()
				.eq(SmtIscCardTask::getActiveKey, activeKey)
				.in(SmtIscCardTask::getStatus, DeviceTaskStatusEnum.INIT.getCode(), DeviceTaskStatusEnum.DOING.getCode())) > 0;
	}

	private int taskPriority(Integer action) {
		if (DeviceTaskActionEnum.DEL.getCode().equals(action)) {
			return DELETE_CARD_PRIORITY;
		}
		return ADD_CARD_PRIORITY;
	}

	private void validateRequired(Long staffId, String badge, Integer parkId) {
		if (staffId == null) {
			throw new TCEException("员工ID不能为空");
		}
		if (StrUtil.isBlank(badge)) {
			throw new TCEException("员工工号不能为空");
		}
		if (parkId == null) {
			throw new TCEException("ISC卡片同步园区不能为空");
		}
	}

	private void validateCardNo(String cardNo) {
		if (!cardNo.matches(ISC_CARD_NO_PATTERN)) {
			throw new TCEException("ISC卡号必须为8-20位数字或大写字母");
		}
	}

	private String normalizeCardNo(String cardNo) {
		return StrUtil.isBlank(cardNo) ? null : cardNo.trim();
	}

	private String normalizeText(String text) {
		return StrUtil.isBlank(text) ? null : text.trim();
	}

	private boolean isVirtualCardNo(String cardNo) {
		return StrUtil.isNotBlank(cardNo) && cardNo.startsWith(ISC_VIRTUAL_CARD_PREFIX);
	}

	private String buildActiveKey(Long staffId, String badge, Integer parkId, String cardNo, Integer action) {
		return SOURCE_TYPE_STAFF + "|" + staffId + "|" + badge + "|" + parkId + "|" + cardNo + "|" + action;
	}

	private String buildRunningKey(SmtIscCardTask task) {
		if (StrUtil.isNotBlank(task.getSourceType()) && task.getSourceId() != null) {
			return task.getSourceType() + "|" + task.getSourceId();
		}
		return "TASK|" + task.getId();
	}

	private String currentUsername() {
		try {
			SmartUser user = SecurityUtils.getUser();
			return user == null ? null : user.getUsername();
		} catch (Exception e) {
			log.debug("未获取到当前登录用户，ISC卡片任务操作人置空：{}", e.getMessage());
			return null;
		}
	}

	private IPage<IscCardTaskPageVO> emptyPage(Page page) {
		long current = page == null ? 1 : page.getCurrent();
		long size = page == null ? 10 : page.getSize();
		Page<IscCardTaskPageVO> emptyPage = new Page<>(current, size);
		emptyPage.setRecords(Collections.emptyList());
		emptyPage.setTotal(0);
		return emptyPage;
	}
}
