package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.entity.SmtIscCardTask;
import com.tce.smart.platform.core.enums.ISCDeviceTaskEnum;
import com.tce.smart.platform.core.service.SmtIscCardTaskService;
import com.tce.smart.platform.core.service.SmtIscStaffCardService;
import com.tce.smart.schedule.service.platform.ISCCardTaskService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ISCCardTaskServiceImpl implements ISCCardTaskService {

	private static final long RETRY_DELAY_SECONDS = 60 * 60;
	private static final int MAX_RETRY_TIMES = DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES;
	private static final int CARD_OWNERSHIP_QUERY_PAGE_SIZE = 500;
	private static final int CARD_OWNERSHIP_QUERY_MAX_PAGES = 20;
	private static final String CARD_TASK_MAX_RETRY_REMARK = "ISC卡片任务失败已达到最大重试次数"
			+ DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES + "次，停止自动重试，请人工介入处理";
	private static final long DOING_TIMEOUT_SECONDS = 10 * 60;
	private static final int PAGE_SIZE = 500;
	private static final String ISC_VIRTUAL_CARD_PREFIX = "999";
	private static final String ISC_CARD_NO_PATTERN = "[0-9A-Z]{8,20}";
	private static final int ISC_PARAMETER_FORMAT_ERROR_CODE = 0x00072003;
	private static final int ISC_RUNTIME_CARD_NOT_EXISTS_CODE = 0x04a12023;
	private static final int ISC_OFFICIAL_CARD_EXISTS_CODE = 0x04a12700;
	private static final int ISC_OFFICIAL_CARD_NOT_EXISTS_CODE = 0x04a12701;
	private static final int ISC_OFFICIAL_LOSS_CARD_CANNOT_BACK_CODE = 0x04a12702;
	private static final int ISC_OFFICIAL_CARD_PERSON_MISMATCH_CODE = 0x04a12703;
	private static final int ISC_OFFICIAL_RECORD_NOT_FOUND_CODE = 0x0531f00e;
	private static final int ISC_OFFICIAL_CARD_PARAM_ERROR_CODE = 0x04a12600;
	private static final int ISC_OFFICIAL_CARD_LIST_TOO_LARGE_CODE = 0x04a12601;

	private final RemoteDispatcherService remoteDispatcherService;
	private final SmtIscCardTaskService smtIscCardTaskService;
	private final SmtIscStaffCardService smtIscStaffCardService;

	@Override
	public void syncCardTasks() {
		if (smtIscCardTaskService.stopExceededRetryCardTasks(MAX_RETRY_TIMES, CARD_TASK_MAX_RETRY_REMARK)) {
			log.info("本轮停止达到最大重试次数的ISC卡片任务，最大重试次数：{}", MAX_RETRY_TIMES);
		}
		Page<SmtIscCardTask> page = new Page<>(1, PAGE_SIZE);
		IPage<SmtIscCardTask> taskPage = smtIscCardTaskService.getPendingTasks(page, DateUtil.currentSeconds(), MAX_RETRY_TIMES);
		List<SmtIscCardTask> tasks = taskPage == null ? null : taskPage.getRecords();
		if (tasks == null || tasks.isEmpty()) {
			return;
		}
		Set<String> blockedGroupKeys = new HashSet<>();
		for (SmtIscCardTask task : tasks) {
			String groupKey = groupKey(task);
			if (blockedGroupKeys.contains(groupKey)) {
				continue;
			}
			if (!syncOneTask(task)) {
				blockedGroupKeys.add(groupKey);
			}
		}
	}

	private boolean syncOneTask(SmtIscCardTask task) {
		long begin = System.currentTimeMillis();
		try {
			if (!markDoing(task)) {
				return false;
			}
			validateTask(task);
			if (isObsoleteAddTask(task)) {
				markCancel(task, "ISC卡片新增任务已不是当前卡号", begin, false);
				return true;
			}
			String personId = resolvePersonId(task);
			if (StrUtil.isBlank(personId)) {
				markRetry(task, "未解析到ISC人员ID", begin);
				return false;
			}
			task.setPersonId(personId);
			EventEnum eventEnum = resolveCardEvent(task.getAction());
			if (isObsoleteAddTask(task)) {
				markCancel(task, "ISC卡片新增任务已不是当前卡号", begin, false);
				return true;
			}
			Result<String> result = dispatch(task.getParkId(), eventEnum, buildCardParams(task, personId));
			if (result == null || !result.isSuccess()) {
				return handleFailedDispatch(task, result, begin);
			}
			markSuccess(task, personId, begin);
			return true;
		} catch (IllegalArgumentException e) {
			log.warn("ISC卡片任务参数非法，taskId={}, message={}", task.getId(), e.getMessage());
			markCancel(task, e.getMessage(), begin, true);
			return true;
		} catch (Exception e) {
			log.error("ISC卡片任务执行异常，taskId={}", task.getId(), e);
			markRetry(task, "ISC卡片任务执行异常：" + e.getMessage(), begin);
			return false;
		}
	}

	private boolean markDoing(SmtIscCardTask task) {
		long currentTime = DateUtil.currentSeconds();
		long doingDeadlineTime = currentTime + DOING_TIMEOUT_SECONDS;
		return smtIscCardTaskService.markDoing(task, currentTime, doingDeadlineTime, MAX_RETRY_TIMES);
	}

	private void validateTask(SmtIscCardTask task) {
		if (task.getParkId() == null) {
			throw new IllegalArgumentException("ISC卡片任务缺少园区ID");
		}
		if (StrUtil.isBlank(task.getCardNo())) {
			throw new IllegalArgumentException("ISC卡片任务缺少卡号");
		}
		String cardNo = task.getCardNo().trim();
		task.setCardNo(cardNo);
		if (!cardNo.matches(ISC_CARD_NO_PATTERN)) {
			throw new IllegalArgumentException("ISC卡号必须为8-20位数字或大写字母");
		}
		if (cardNo.startsWith(ISC_VIRTUAL_CARD_PREFIX)) {
			throw new IllegalArgumentException("ISC虚拟卡号不执行同步");
		}
		resolveCardEvent(task.getAction());
	}

	private boolean isObsoleteAddTask(SmtIscCardTask task) {
		return DeviceTaskActionEnum.DOWN.getCode().equals(task.getAction())
				&& !smtIscCardTaskService.isCurrentStaffCardAddTask(task);
	}

	private String resolvePersonId(SmtIscCardTask task) {
		if (StrUtil.isNotBlank(task.getPersonId())) {
			return task.getPersonId();
		}
		if (StrUtil.isBlank(task.getBadge())) {
			return null;
		}
		Map<String, Object> params = new HashMap<>(2);
		params.put("paramName", "jobNo");
		params.put("paramValue", new String[]{task.getBadge()});
		Result<String> result = dispatch(task.getParkId(), EventEnum.ISC_PERSON_GET, params);
		if (result == null || !result.isSuccess() || StrUtil.isBlank(result.getData())) {
			log.warn("ISC卡片任务查询人员失败，taskId={}, badge={}, message={}",
					task.getId(), task.getBadge(), result == null ? "接口无响应" : result.getMessage());
			return null;
		}
		JSONArray list = JSONUtil.parseObj(result.getData()).getJSONArray("list");
		if (list == null || list.isEmpty()) {
			return null;
		}
		String deletedPersonId = null;
		for (int i = 0; i < list.size(); i++) {
			String personId = list.getJSONObject(i).getStr("personId");
			if (StrUtil.isBlank(personId)) {
				continue;
			}
			Integer status = list.getJSONObject(i).getInt("status");
			if (status == null || status >= 0) {
				return personId;
			}
			if (deletedPersonId == null) {
				deletedPersonId = personId;
			}
		}
		return DeviceTaskActionEnum.DEL.getCode().equals(task.getAction()) ? deletedPersonId : null;
	}

	private EventEnum resolveCardEvent(Integer action) {
		if (DeviceTaskActionEnum.DOWN.getCode().equals(action)) {
			return EventEnum.ISC_CARD_ADD;
		}
		if (DeviceTaskActionEnum.DEL.getCode().equals(action)) {
			return EventEnum.ISC_CARD_DELETE;
		}
		throw new IllegalArgumentException("ISC卡片任务动作不支持：" + action);
	}

	private Map<String, Object> buildCardParams(SmtIscCardTask task, String personId) {
		if (DeviceTaskActionEnum.DOWN.getCode().equals(task.getAction())) {
			Map<String, Object> card = new HashMap<>(3);
			card.put("cardNo", task.getCardNo());
			card.put("personId", personId);
			card.put("cardType", 1);
			Map<String, Object> params = new HashMap<>(1);
			params.put("cardList", Collections.singletonList(card));
			return params;
		}
		Map<String, Object> params = new HashMap<>(2);
		params.put("cardNumber", task.getCardNo());
		params.put("personId", personId);
		return params;
	}

	private Result<String> dispatch(Integer parkId, EventEnum eventEnum, Map<String, Object> params) {
		DispatcherDTO<Map<String, Object>> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setEventType(eventEnum.getCode());
		dispatcherDTO.setData(params);
		return remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	private void markSuccess(SmtIscCardTask task, String personId, long begin) {
		markSuccess(task, personId, begin, ISCDeviceTaskEnum.DEVICE_OK.getDesc());
	}

	private void markSuccess(SmtIscCardTask task, String personId, long begin, String remark) {
		String expectedActiveKey = task.getActiveKey();
		String expectedLeaseToken = task.getLeaseToken();
		task.setStatus(DeviceTaskStatusEnum.SUCCESS.getCode());
		task.setCode(ISCDeviceTaskEnum.DEVICE_OK.getCode());
		task.setRemark(remark);
		task.setPersonId(personId);
		task.setActiveKey(null);
		task.setLeaseToken(null);
		task.setRunningKey(null);
		task.setConsume(System.currentTimeMillis() - begin);
		task.setUpdateTime(LocalDateTime.now());
		if (updateDoingTask(task, expectedActiveKey, expectedLeaseToken)) {
			updateAddTaskSuccess(task);
		}
	}

	private boolean handleFailedDispatch(SmtIscCardTask task, Result<String> result, long begin) {
		if (result == null) {
			markRetry(task, "ISC卡片接口无响应", begin);
			return false;
		}
		String remark = StrUtil.blankToDefault(result.getMessage(), "ISC卡片接口返回失败");
		if (isDeleteCardNotExists(task, result)) {
			markSuccess(task, task.getPersonId(), begin, "ISC卡片已不存在，按删除成功处理：" + remark);
			return true;
		}
		if (isAddCardAlreadyExists(task, result)) {
			return handleAddCardAlreadyExists(task, remark, begin);
		}
		if (isPermanentCardError(result)) {
			markCancel(task, remark, begin, shouldRemoveLocalCard(result));
			return true;
		}
		markRetry(task, remark, begin);
		return false;
	}

	private boolean isDeleteCardNotExists(SmtIscCardTask task, Result<String> result) {
		return DeviceTaskActionEnum.DEL.getCode().equals(task.getAction())
				&& (matchesResultCode(result, ISC_RUNTIME_CARD_NOT_EXISTS_CODE)
				|| matchesResultCode(result, ISC_OFFICIAL_CARD_NOT_EXISTS_CODE)
				|| containsCardNotExistsMessage(result.getMessage()));
	}

	private boolean isAddCardAlreadyExists(SmtIscCardTask task, Result<String> result) {
		return DeviceTaskActionEnum.DOWN.getCode().equals(task.getAction())
				&& matchesResultCode(result, ISC_OFFICIAL_CARD_EXISTS_CODE);
	}

	/**
	 * 卡号已存在（0x04a12700）不代表绑定在本人名下：按本人personId回查ISC卡列表确认归属。
	 * 归属本人按成功处理；归属他人按永久失败；回查失败保留重试。
	 */
	private boolean handleAddCardAlreadyExists(SmtIscCardTask task, String remark, long begin) {
		Boolean ownedByPerson = isCardOwnedByPerson(task);
		if (ownedByPerson == null) {
			markRetry(task, "ISC卡片已存在，回查卡片归属失败，保留重试：" + remark, begin);
			return false;
		}
		if (ownedByPerson) {
			markSuccess(task, task.getPersonId(), begin, "ISC卡片已存在且归属本人，按下发成功处理：" + remark);
			return true;
		}
		markCancel(task, "ISC卡片已存在且归属其他人员，请先在ISC侧退卡：" + remark, begin, false);
		return true;
	}

	/**
	 * @return true=卡在本人名下；false=本人名下无此卡；null=回查失败（应重试）
	 */
	private Boolean isCardOwnedByPerson(SmtIscCardTask task) {
		String personId = task.getPersonId();
		if (StrUtil.isBlank(personId)) {
			return null;
		}
		int pageNo = 1;
		while (pageNo <= CARD_OWNERSHIP_QUERY_MAX_PAGES) {
			Map<String, Object> params = new HashMap<>(3);
			params.put("pageNo", pageNo);
			params.put("pageSize", CARD_OWNERSHIP_QUERY_PAGE_SIZE);
			params.put("personIds", personId);
			Result<String> result = dispatch(task.getParkId(), EventEnum.ISC_CARD_LIST_GET, params);
			if (result == null || !result.isSuccess() || StrUtil.isBlank(result.getData())) {
				log.warn("ISC卡片归属回查失败，taskId={}, personId={}, message={}",
						task.getId(), personId, result == null ? "接口无响应" : result.getMessage());
				return null;
			}
			JSONArray list;
			Integer total;
			try {
				cn.hutool.json.JSONObject data = JSONUtil.parseObj(result.getData());
				list = data.getJSONArray("list");
				total = data.getInt("total");
			} catch (Exception e) {
				log.warn("ISC卡片归属回查响应解析失败，taskId={}, personId={}", task.getId(), personId, e);
				return null;
			}
			if (list == null || list.isEmpty()) {
				return false;
			}
			for (int i = 0; i < list.size(); i++) {
				cn.hutool.json.JSONObject card = list.getJSONObject(i);
				if (!personId.equals(card.getStr("personId"))) {
					continue;
				}
				if (task.getCardNo().equalsIgnoreCase(StrUtil.trim(card.getStr("cardNo")))) {
					return true;
				}
			}
			if (list.size() < CARD_OWNERSHIP_QUERY_PAGE_SIZE
					|| (total != null && pageNo * CARD_OWNERSHIP_QUERY_PAGE_SIZE >= total)) {
				return false;
			}
			pageNo++;
		}
		log.warn("ISC卡片归属回查超过最大页数{}仍未确认，taskId={}, personId={}",
				CARD_OWNERSHIP_QUERY_MAX_PAGES, task.getId(), task.getPersonId());
		return null;
	}

	private boolean isPermanentCardError(Result<String> result) {
		return matchesResultCode(result, ISC_PARAMETER_FORMAT_ERROR_CODE)
				|| matchesResultCode(result, ISC_OFFICIAL_LOSS_CARD_CANNOT_BACK_CODE)
				|| matchesResultCode(result, ISC_OFFICIAL_CARD_PERSON_MISMATCH_CODE)
				|| matchesResultCode(result, ISC_OFFICIAL_RECORD_NOT_FOUND_CODE)
				|| matchesResultCode(result, ISC_OFFICIAL_CARD_PARAM_ERROR_CODE)
				|| matchesResultCode(result, ISC_OFFICIAL_CARD_LIST_TOO_LARGE_CODE)
				|| containsPermanentCardErrorMessage(result.getMessage());
	}

	private boolean matchesResultCode(Result<String> result, int errorCode) {
		return result != null && Integer.valueOf(errorCode).equals(result.getCode());
	}

	private boolean containsCardNotExistsMessage(String message) {
		if (StrUtil.isBlank(message)) {
			return false;
		}
		String normalizedMessage = message.toLowerCase();
		return normalizedMessage.contains("cardno") && normalizedMessage.contains("not exists")
				|| message.contains("卡号不存在");
	}

	private boolean containsPermanentCardErrorMessage(String message) {
		if (StrUtil.isBlank(message)) {
			return false;
		}
		String normalizedMessage = message.toLowerCase();
		return message.contains("参数格式不正确")
				|| normalizedMessage.contains("cardno format error")
				|| normalizedMessage.contains("parameter cardno format error");
	}

	private boolean shouldRemoveLocalCard(Result<String> result) {
		return matchesResultCode(result, ISC_PARAMETER_FORMAT_ERROR_CODE)
				|| matchesResultCode(result, ISC_OFFICIAL_CARD_PARAM_ERROR_CODE)
				|| containsPermanentCardErrorMessage(result == null ? null : result.getMessage());
	}

	private void markRetry(SmtIscCardTask task, String remark, long begin) {
		String expectedActiveKey = task.getActiveKey();
		String expectedLeaseToken = task.getLeaseToken();
		int nextTimes = task.getTimes() == null ? 1 : task.getTimes() + 1;
		task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		task.setCode(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode());
		task.setRemark(remark);
		task.setTimes(nextTimes);
		task.setOverTime(DateUtil.currentSeconds() + RETRY_DELAY_SECONDS);
		task.setLeaseToken(null);
		task.setRunningKey(null);
		task.setConsume(System.currentTimeMillis() - begin);
		task.setUpdateTime(LocalDateTime.now());
		updateDoingTask(task, expectedActiveKey, expectedLeaseToken);
	}

	private void markCancel(SmtIscCardTask task, String remark, long begin, boolean removeLocalCard) {
		String expectedActiveKey = task.getActiveKey();
		String expectedLeaseToken = task.getLeaseToken();
		task.setStatus(DeviceTaskStatusEnum.CANCEL.getCode());
		task.setCode(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode());
		task.setRemark(remark);
		task.setActiveKey(null);
		task.setLeaseToken(null);
		task.setRunningKey(null);
		task.setConsume(System.currentTimeMillis() - begin);
		task.setUpdateTime(LocalDateTime.now());
		if (updateDoingTask(task, expectedActiveKey, expectedLeaseToken)
				&& DeviceTaskActionEnum.DOWN.getCode().equals(task.getAction())) {
			smtIscStaffCardService.markAddTaskFailed(task, removeLocalCard);
		}
	}

	private boolean updateDoingTask(SmtIscCardTask task, String expectedActiveKey, String expectedLeaseToken) {
		if (!smtIscCardTaskService.updateDoingTask(task, expectedActiveKey, expectedLeaseToken)) {
			log.warn("ISC卡片任务状态已变化，跳过本次结果更新，taskId={}, expectedActiveKey={}, expectedLeaseToken={}",
					task.getId(), expectedActiveKey, expectedLeaseToken);
			return false;
		}
		return true;
	}

	private void updateAddTaskSuccess(SmtIscCardTask task) {
		if (DeviceTaskActionEnum.DOWN.getCode().equals(task.getAction())) {
			smtIscStaffCardService.markAddTaskSuccess(task);
		}
	}

	private String groupKey(SmtIscCardTask task) {
		if (StrUtil.isNotBlank(task.getSourceType()) && task.getSourceId() != null) {
			return task.getSourceType() + "|" + task.getSourceId();
		}
		return "TASK|" + task.getId();
	}
}
