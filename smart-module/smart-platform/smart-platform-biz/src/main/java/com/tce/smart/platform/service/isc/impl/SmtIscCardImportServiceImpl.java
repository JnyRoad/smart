package com.tce.smart.platform.service.isc.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.req.isc.IscCardImportBatchPageReqDTO;
import com.tce.smart.platform.api.dto.req.isc.IscCardImportDetailPageReqDTO;
import com.tce.smart.platform.api.dto.req.isc.IscCardImportStartReqDTO;
import com.tce.smart.platform.core.entity.SmtIscCardImportBatch;
import com.tce.smart.platform.core.entity.SmtIscCardImportDetail;
import com.tce.smart.platform.core.entity.SmtIscParkConfig;
import com.tce.smart.platform.core.entity.SmtIscStaffCard;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.enums.DeviceSyncEnum;
import com.tce.smart.platform.core.enums.IscCardImportModeEnum;
import com.tce.smart.platform.core.enums.IscCardImportResultEnum;
import com.tce.smart.platform.core.enums.IscCardImportStaffScopeEnum;
import com.tce.smart.platform.core.enums.IscCardImportStatusEnum;
import com.tce.smart.platform.core.mapper.SmtIscCardImportBatchMapper;
import com.tce.smart.platform.core.mapper.SmtIscCardImportDetailMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.service.SmtIscParkConfigService;
import com.tce.smart.platform.core.service.SmtIscStaffCardService;
import com.tce.smart.platform.service.isc.SmtIscCardImportService;
import com.tce.smart.tool.enums.DeleteStatusEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SmtIscCardImportServiceImpl extends ServiceImpl<SmtIscCardImportBatchMapper, SmtIscCardImportBatch>
		implements SmtIscCardImportService {

	private static final int ISC_QUERY_BATCH_SIZE = 50;
	private static final int ISC_CARD_QUERY_PAGE_SIZE = 100;
	private static final int DB_REASON_LIMIT = 1000;
	private static final String ISC_VIRTUAL_CARD_PREFIX = "999";
	private static final String ISC_CARD_NO_PATTERN = "[0-9A-Z]{8,20}";

	@Autowired
	private SmtIscCardImportDetailMapper smtIscCardImportDetailMapper;

	@Autowired
	private SmtStaffMapper smtStaffMapper;

	@Autowired
	private SmtIscParkConfigService smtIscParkConfigService;

	@Autowired
	private SmtIscStaffCardService smtIscStaffCardService;

	@Autowired
	private RemoteDispatcherService remoteDispatcherService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public SmtIscCardImportBatch createBatch(IscCardImportStartReqDTO reqDTO, String mode, List<Integer> allowedParkIds) {
		validateCreateRequest(reqDTO, mode, allowedParkIds);
		IscCardImportStaffScopeEnum staffScope = requireStaffScope(reqDTO.getStaffScope());
		reqDTO.setStaffScope(staffScope.getCode());
		SmtIscParkConfig config = getAllowedEnabledParkConfig(reqDTO.getParkId());
		LocalDateTime now = LocalDateTime.now();
		SmtIscCardImportBatch batch = new SmtIscCardImportBatch();
		batch.setMode(mode);
		batch.setStatus(IscCardImportStatusEnum.INIT.getCode());
		batch.setParkId(config.getParkId());
		batch.setParkName(config.getParkName());
		batch.setDispatcherParkId(config.getDispatcherParkId());
		batch.setDispatcherParkName(config.getDispatcherParkName());
		batch.setTotalCount(0);
		batch.setSuccessCount(0);
		batch.setSkipCount(0);
		batch.setConflictCount(0);
		batch.setFailCount(0);
		batch.setParamsJson(JSONUtil.toJsonStr(reqDTO));
		batch.setCreateTime(now);
		batch.setUpdateTime(now);
		batch.setOptUser(currentUsername());
		if (!this.save(batch)) {
			throw new TCEException("创建ISC卡片初始化同步批次失败");
		}
		return batch;
	}

	@Async
	@Override
	public void executeBatch(Long batchId) {
		if (batchId == null) {
			throw new TCEException("ISC卡片初始化同步批次ID不能为空");
		}
		SmtIscCardImportBatch batch = this.baseMapper.getById(batchId);
		if (batch == null) {
			throw new TCEException("ISC卡片初始化同步批次不存在");
		}
		long begin = System.currentTimeMillis();
		try {
			markRunning(batch);
			executeBatchInternal(batch);
			markSuccess(batch, begin);
		} catch (Exception e) {
			log.error("ISC卡片初始化同步批次执行失败，batchId={}", batchId, e);
			markFail(batch, begin, e.getMessage());
		}
	}

	@Override
	public IPage<SmtIscCardImportBatch> getBatchPage(Page page, IscCardImportBatchPageReqDTO query) {
		IscCardImportBatchPageReqDTO pageQuery = query == null ? new IscCardImportBatchPageReqDTO() : query;
		if (CollectionUtil.isEmpty(pageQuery.getParkIds())) {
			return emptyBatchPage(page);
		}
		if (pageQuery.getParkId() != null && !pageQuery.getParkIds().contains(pageQuery.getParkId())) {
			return emptyBatchPage(page);
		}
		return this.baseMapper.getPage(page, pageQuery);
	}

	@Override
	public IPage<SmtIscCardImportDetail> getDetailPage(Page page, IscCardImportDetailPageReqDTO query) {
		IscCardImportDetailPageReqDTO pageQuery = query == null ? new IscCardImportDetailPageReqDTO() : query;
		if (CollectionUtil.isEmpty(pageQuery.getParkIds())) {
			return emptyDetailPage(page);
		}
		if (pageQuery.getBatchId() == null) {
			return emptyDetailPage(page);
		}
		return smtIscCardImportDetailMapper.getPage(page, pageQuery);
	}

	private void executeBatchInternal(SmtIscCardImportBatch batch) {
		SmtIscParkConfig config = getBatchEnabledParkConfig(batch.getParkId());
		IscCardImportStartReqDTO params = parseParams(batch.getParamsJson());
		IscCardImportStaffScopeEnum staffScope = requireStaffScope(params.getStaffScope());
		List<SmtStaff> staffList = smtStaffMapper.listIscCardImportStaff(batch.getParkId(),
				normalizeText(params.getBadge()), staffScope.getCode());
		if (CollectionUtil.isEmpty(staffList)) {
			batch.setRemark("目标园区未查询到可同步员工");
			return;
		}
		List<SmtIscStaffCard> localCards = listActiveLocalCards(config.getDispatcherParkId());
		Map<String, SmtIscStaffCard> localCardByActiveKey = localCards.stream()
				.filter(card -> StrUtil.isNotBlank(card.getCardNo()))
				.collect(Collectors.toMap(card -> buildActiveKey(card.getDispatcherParkId(), card.getCardNo()),
						Function.identity(), (left, right) -> left));
		Map<Long, List<SmtIscStaffCard>> localCardsByStaff = localCards.stream()
				.filter(card -> card.getStaffId() != null)
				.collect(Collectors.groupingBy(SmtIscStaffCard::getStaffId));
		Set<String> iscActiveKeys = new HashSet<>();
		Map<Long, SmtStaff> targetStaffById = staffList.stream()
				.filter(staff -> staff.getId() != null)
				.collect(Collectors.toMap(SmtStaff::getId, Function.identity(), (left, right) -> left));
		BatchStats stats = new BatchStats();
		for (List<SmtStaff> part : partition(staffList, ISC_QUERY_BATCH_SIZE)) {
			Map<String, JSONObject> iscPersonByBadge = queryIscPersons(config.getDispatcherParkId(), part);
			Map<String, Set<String>> iscCardNosByPersonId = queryIscCardsByPersonId(config.getDispatcherParkId(),
					iscPersonByBadge.values());
			for (SmtStaff staff : part) {
				processStaff(batch, config, staff, iscPersonByBadge.get(normalizeText(staff.getBadge())),
						localCardByActiveKey, localCardsByStaff.getOrDefault(staff.getId(), Collections.emptyList()),
						iscCardNosByPersonId, iscActiveKeys, targetStaffById, stats);
			}
		}
		recordLocalOnlyCards(batch, localCards, iscActiveKeys, targetStaffById, stats);
		applyStats(batch, stats);
	}

	private void processStaff(SmtIscCardImportBatch batch, SmtIscParkConfig config, SmtStaff staff,
								  JSONObject person, Map<String, SmtIscStaffCard> localCardByActiveKey,
								  List<SmtIscStaffCard> staffLocalCards, Map<String, Set<String>> iscCardNosByPersonId,
								  Set<String> iscActiveKeys, Map<Long, SmtStaff> targetStaffById, BatchStats stats) {
		if (person == null) {
			insertDetail(batch, staff, null, null, firstLocalCardNo(staffLocalCards),
					IscCardImportResultEnum.STAFF_NOT_FOUND, "海康ISC未查询到该工号人员");
			stats.failCount++;
			return;
		}
		if (isDeletedIscPerson(person)) {
			insertDetail(batch, staff, person.getStr("personId"), null, firstLocalCardNo(staffLocalCards),
					IscCardImportResultEnum.STAFF_NOT_FOUND, "海康ISC人员已删除");
			stats.failCount++;
			return;
		}
		Set<String> cardNos = new LinkedHashSet<>(iscCardNosByPersonId.getOrDefault(person.getStr("personId"),
				Collections.emptySet()));
		if (cardNos.isEmpty()) {
			insertDetail(batch, staff, person.getStr("personId"), null, firstLocalCardNo(staffLocalCards),
					IscCardImportResultEnum.ISC_EMPTY, "海康ISC人员未绑定实体卡");
			stats.skipCount++;
			return;
		}
		for (String cardNo : cardNos) {
			processCard(batch, config, staff, person, cardNo, localCardByActiveKey, iscActiveKeys,
					targetStaffById, stats);
		}
	}

	private void processCard(SmtIscCardImportBatch batch, SmtIscParkConfig config, SmtStaff staff, JSONObject person,
							 String cardNo, Map<String, SmtIscStaffCard> localCardByActiveKey,
							 Set<String> iscActiveKeys, Map<Long, SmtStaff> targetStaffById, BatchStats stats) {
		String normalizedCardNo = normalizeCardNo(cardNo);
		if (!isRealCardNo(normalizedCardNo)) {
			insertDetail(batch, staff, person.getStr("personId"), normalizedCardNo, null,
					IscCardImportResultEnum.INVALID, "海康ISC卡号为空、非8-20位数字或大写字母，或为999开头虚拟卡");
			stats.failCount++;
			return;
		}
		String activeKey = buildActiveKey(config.getDispatcherParkId(), normalizedCardNo);
		iscActiveKeys.add(activeKey);
		SmtIscStaffCard localCard = localCardByActiveKey.get(activeKey);
		if (localCard != null && Objects.equals(localCard.getStaffId(), staff.getId())) {
			if (shouldCleanResignedStaffCard(batch, staff)) {
				cleanResignedStaffCard(batch, staff, person.getStr("personId"), normalizedCardNo,
						localCard.getCardNo(), localCard, "离职员工本地与海康ISC已一致，已作废并触发退卡清理", stats);
				return;
			}
			insertDetail(batch, staff, person.getStr("personId"), normalizedCardNo, localCard.getCardNo(),
					IscCardImportResultEnum.SKIP_SAME, "本地与海康ISC已一致");
			stats.skipCount++;
			return;
		}
		if (localCard != null) {
			SmtStaff localCardOwner = targetStaffById.get(localCard.getStaffId());
			if (shouldCleanResignedStaffCard(batch, localCardOwner)) {
				if (cleanResignedStaffCard(batch, localCardOwner, null, normalizedCardNo, localCard.getCardNo(),
						localCard, "离职员工本地卡片已被海康ISC返回给其他人员，已作废并触发退卡清理", stats)) {
					localCardByActiveKey.remove(activeKey);
					importCard(batch, config, staff, person, normalizedCardNo, stats);
				}
				return;
			}
			insertDetail(batch, staff, person.getStr("personId"), normalizedCardNo, localCard.getCardNo(),
					IscCardImportResultEnum.CONFLICT,
					"本地相同卡号已归属工号：" + localCard.getBadge());
			stats.conflictCount++;
			return;
		}
		if (IscCardImportModeEnum.isImport(batch.getMode())) {
			importCard(batch, config, staff, person, normalizedCardNo, stats);
			return;
		}
		insertDetail(batch, staff, person.getStr("personId"), normalizedCardNo, null,
				IscCardImportResultEnum.READY_IMPORT, "预检通过，执行导入后将写入本地卡表");
		stats.successCount++;
	}

	private void importCard(SmtIscCardImportBatch batch, SmtIscParkConfig config, SmtStaff staff,
							JSONObject person, String cardNo, BatchStats stats) {
		try {
			SmtIscStaffCard importedCard = smtIscStaffCardService.importStaffCardFromIsc(staff, config, cardNo,
					"首次从海康ISC初始化导入", batch.getOptUser());
			String resultDesc = "已从海康ISC导入本地卡表";
			if (StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(staff.getStatus())) {
				if (!Boolean.TRUE.equals(smtIscStaffCardService.removeStaffCard(importedCard.getId()))) {
					throw new TCEException("离职员工ISC卡片退卡任务创建失败");
				}
				resultDesc = "已导入离职员工卡片，并触发退卡清理";
			}
			insertDetail(batch, staff, person.getStr("personId"), cardNo, null,
					IscCardImportResultEnum.IMPORTED, resultDesc);
			stats.successCount++;
		} catch (Exception e) {
			insertDetail(batch, staff, person.getStr("personId"), cardNo, null,
					IscCardImportResultEnum.FAIL, e.getMessage());
			stats.failCount++;
		}
	}

	private void recordLocalOnlyCards(SmtIscCardImportBatch batch, List<SmtIscStaffCard> localCards,
									  Set<String> iscActiveKeys, Map<Long, SmtStaff> targetStaffById, BatchStats stats) {
		for (SmtIscStaffCard localCard : localCards) {
			if (localCard.getStaffId() == null) {
				continue;
			}
			SmtStaff staff = targetStaffById.get(localCard.getStaffId());
			if (staff == null) {
				continue;
			}
			String activeKey = buildActiveKey(localCard.getDispatcherParkId(), localCard.getCardNo());
			if (iscActiveKeys.contains(activeKey)) {
				continue;
			}
			if (shouldCleanResignedStaffCard(batch, staff)) {
				cleanResignedStaffCard(batch, staff, null, null, localCard.getCardNo(), localCard,
						"离职员工本地多余卡片已作废，并触发退卡清理", stats);
				continue;
			}
			insertDetail(batch, staff, null, null, localCard.getCardNo(),
					IscCardImportResultEnum.LOCAL_ONLY, "本地存在该卡号，海康ISC本次未返回");
			stats.skipCount++;
		}
	}

	private boolean shouldCleanResignedStaffCard(SmtIscCardImportBatch batch, SmtStaff staff) {
		return IscCardImportModeEnum.isImport(batch.getMode())
				&& staff != null
				&& StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(staff.getStatus());
	}

	private boolean cleanResignedStaffCard(SmtIscCardImportBatch batch, SmtStaff staff, String personId,
										String iscCardNo, String localCardNo, SmtIscStaffCard localCard,
										String reason, BatchStats stats) {
		try {
			if (!Boolean.TRUE.equals(smtIscStaffCardService.removeStaffCard(localCard.getId()))) {
				throw new TCEException("离职员工ISC卡片退卡任务创建失败");
			}
			insertDetail(batch, staff, personId, iscCardNo, localCardNo,
					IscCardImportResultEnum.REMOVED, reason);
			stats.successCount++;
			return true;
		} catch (Exception e) {
			insertDetail(batch, staff, personId, iscCardNo, localCardNo,
					IscCardImportResultEnum.FAIL, e.getMessage());
			stats.failCount++;
			return false;
		}
	}

	private Map<String, JSONObject> queryIscPersons(Integer dispatcherParkId, List<SmtStaff> staffList) {
		List<String> badges = staffList.stream()
				.map(SmtStaff::getBadge)
				.map(this::normalizeText)
				.filter(StrUtil::isNotBlank)
				.collect(Collectors.toList());
		if (badges.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, Object> params = new HashMap<>(2);
		params.put("paramName", "jobNo");
		params.put("paramValue", badges.toArray(new String[0]));
		Result<String> result = dispatch(dispatcherParkId, EventEnum.ISC_PERSON_GET, params);
		if (result == null || !result.isSuccess() || StrUtil.isBlank(result.getData())) {
			throw new TCEException(result == null ? "海康ISC人员查询接口无响应" : result.getMessage());
		}
		JSONArray list = JSONUtil.parseObj(result.getData()).getJSONArray("list");
		if (list == null || list.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, JSONObject> personByBadge = new HashMap<>(list.size());
		for (int i = 0; i < list.size(); i++) {
			JSONObject person = list.getJSONObject(i);
			String jobNo = normalizeText(person.getStr("jobNo"));
			if (StrUtil.isBlank(jobNo)) {
				continue;
			}
			JSONObject oldPerson = personByBadge.get(jobNo);
			if (oldPerson == null || shouldReplaceIscPerson(oldPerson, person)) {
				personByBadge.put(jobNo, person);
			}
		}
		return personByBadge;
	}

	private Map<String, Set<String>> queryIscCardsByPersonId(Integer dispatcherParkId, Collection<JSONObject> persons) {
		if (CollectionUtil.isEmpty(persons)) {
			return Collections.emptyMap();
		}
		Map<String, Set<String>> cardNosByPersonId = new HashMap<>();
		for (JSONObject person : persons) {
			String personId = normalizeText(person.getStr("personId"));
			if (StrUtil.isBlank(personId)) {
				continue;
			}
			Set<String> cardNos = queryIscCardNos(dispatcherParkId, personId);
			if (!cardNos.isEmpty()) {
				cardNosByPersonId.put(personId, cardNos);
			}
		}
		return cardNosByPersonId;
	}

	private Set<String> queryIscCardNos(Integer dispatcherParkId, String personId) {
		Set<String> cardNos = new LinkedHashSet<>();
		int pageNo = 1;
		while (true) {
			Map<String, Object> params = new HashMap<>(3);
			params.put("pageNo", pageNo);
			params.put("pageSize", ISC_CARD_QUERY_PAGE_SIZE);
			params.put("personIds", personId);
			Result<String> result = dispatch(dispatcherParkId, EventEnum.ISC_CARD_LIST_GET, params);
			if (result == null || !result.isSuccess() || StrUtil.isBlank(result.getData())) {
				throw new TCEException(result == null ? "海康ISC卡片查询接口无响应" : result.getMessage());
			}
			JSONObject data = JSONUtil.parseObj(result.getData());
			JSONArray list = data.getJSONArray("list");
			if (list == null || list.isEmpty()) {
				return cardNos;
			}
			for (Object item : list) {
				if (item instanceof JSONObject) {
					JSONObject card = (JSONObject) item;
					if (!isCurrentPersonCard(card, personId)) {
						continue;
					}
					addCardNo(cardNos, card.getStr("cardNo"));
				}
			}
			Integer total = data.getInt("total");
			if (list.size() < ISC_CARD_QUERY_PAGE_SIZE
					|| (total != null && pageNo * ISC_CARD_QUERY_PAGE_SIZE >= total)) {
				return cardNos;
			}
			pageNo++;
		}
	}

	private Result<String> dispatch(Integer dispatcherParkId, EventEnum eventEnum, Map<String, Object> params) {
		DispatcherDTO<Map<String, Object>> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setParkId(dispatcherParkId);
		dispatcherDTO.setEventType(eventEnum.getCode());
		dispatcherDTO.setData(params);
		return remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
	}

	private boolean isCurrentPersonCard(JSONObject card, String personId) {
		return StrUtil.equals(normalizeText(card.getStr("personId")), personId);
	}

	private void addCardNo(Set<String> cardNos, String cardNo) {
		String normalizedCardNo = normalizeCardNo(cardNo);
		if (StrUtil.isNotBlank(normalizedCardNo)) {
			cardNos.add(normalizedCardNo);
		}
	}

	private List<SmtIscStaffCard> listActiveLocalCards(Integer dispatcherParkId) {
		return smtIscStaffCardService.list(new LambdaQueryWrapper<SmtIscStaffCard>()
				.eq(SmtIscStaffCard::getDispatcherParkId, dispatcherParkId)
				.eq(SmtIscStaffCard::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode()));
	}

	private void insertDetail(SmtIscCardImportBatch batch, SmtStaff staff, String personId, String iscCardNo,
							  String localCardNo, IscCardImportResultEnum result, String reason) {
		LocalDateTime now = LocalDateTime.now();
		SmtIscCardImportDetail detail = new SmtIscCardImportDetail();
		detail.setBatchId(batch.getId());
		detail.setStaffId(staff == null ? null : staff.getId());
		detail.setBadge(staff == null ? null : staff.getBadge());
		detail.setName(staff == null ? null : staff.getName());
		detail.setParkId(batch.getParkId());
		detail.setDispatcherParkId(batch.getDispatcherParkId());
		detail.setPersonId(personId);
		detail.setIscCardNo(iscCardNo);
		detail.setLocalCardNo(localCardNo);
		detail.setResultCode(result.getCode());
		detail.setResultDesc(result.getDesc());
		detail.setReason(limitText(reason, DB_REASON_LIMIT));
		detail.setCreateTime(now);
		detail.setUpdateTime(now);
		smtIscCardImportDetailMapper.insert(detail);
	}

	private void markRunning(SmtIscCardImportBatch batch) {
		SmtIscCardImportBatch updatedBatch = new SmtIscCardImportBatch();
		updatedBatch.setId(batch.getId());
		updatedBatch.setStatus(IscCardImportStatusEnum.RUNNING.getCode());
		updatedBatch.setStartTime(LocalDateTime.now());
		updatedBatch.setUpdateTime(LocalDateTime.now());
		this.updateById(updatedBatch);
		batch.setStatus(updatedBatch.getStatus());
		batch.setStartTime(updatedBatch.getStartTime());
	}

	private void markSuccess(SmtIscCardImportBatch batch, long begin) {
		SmtIscCardImportBatch updatedBatch = copyStats(batch);
		updatedBatch.setStatus(IscCardImportStatusEnum.SUCCESS.getCode());
		updatedBatch.setEndTime(LocalDateTime.now());
		updatedBatch.setConsume(System.currentTimeMillis() - begin);
		updatedBatch.setUpdateTime(LocalDateTime.now());
		this.updateById(updatedBatch);
	}

	private void markFail(SmtIscCardImportBatch batch, long begin, String message) {
		SmtIscCardImportBatch updatedBatch = copyStats(batch);
		updatedBatch.setStatus(IscCardImportStatusEnum.FAIL.getCode());
		updatedBatch.setRemark(limitText(message, DB_REASON_LIMIT));
		updatedBatch.setEndTime(LocalDateTime.now());
		updatedBatch.setConsume(System.currentTimeMillis() - begin);
		updatedBatch.setUpdateTime(LocalDateTime.now());
		this.updateById(updatedBatch);
	}

	private SmtIscCardImportBatch copyStats(SmtIscCardImportBatch batch) {
		SmtIscCardImportBatch updatedBatch = new SmtIscCardImportBatch();
		updatedBatch.setId(batch.getId());
		updatedBatch.setTotalCount(batch.getTotalCount());
		updatedBatch.setSuccessCount(batch.getSuccessCount());
		updatedBatch.setSkipCount(batch.getSkipCount());
		updatedBatch.setConflictCount(batch.getConflictCount());
		updatedBatch.setFailCount(batch.getFailCount());
		updatedBatch.setRemark(batch.getRemark());
		return updatedBatch;
	}

	private void applyStats(SmtIscCardImportBatch batch, BatchStats stats) {
		batch.setTotalCount(stats.totalCount());
		batch.setSuccessCount(stats.successCount);
		batch.setSkipCount(stats.skipCount);
		batch.setConflictCount(stats.conflictCount);
		batch.setFailCount(stats.failCount);
	}

	private void validateCreateRequest(IscCardImportStartReqDTO reqDTO, String mode, List<Integer> allowedParkIds) {
		if (reqDTO == null || reqDTO.getParkId() == null) {
			throw new TCEException("园区不能为空");
		}
		if (!IscCardImportModeEnum.DRY_RUN.getCode().equals(mode)
				&& !IscCardImportModeEnum.IMPORT.getCode().equals(mode)) {
			throw new TCEException("ISC卡片初始化同步模式不支持");
		}
		if (CollectionUtil.isEmpty(allowedParkIds) || !allowedParkIds.contains(reqDTO.getParkId())) {
			throw new TCEException("无权操作该园区");
		}
		requireStaffScope(reqDTO.getStaffScope());
	}

	private SmtIscParkConfig getAllowedEnabledParkConfig(Integer parkId) {
		SmtIscParkConfig config = smtIscParkConfigService.getConfigByPark(parkId);
		return requireEnabledParkConfig(config);
	}

	private SmtIscParkConfig getBatchEnabledParkConfig(Integer parkId) {
		SmtIscParkConfig config = smtIscParkConfigService.getOne(new LambdaQueryWrapper<SmtIscParkConfig>()
				.eq(SmtIscParkConfig::getParkId, parkId)
				.eq(SmtIscParkConfig::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode()), false);
		return requireEnabledParkConfig(config);
	}

	private SmtIscParkConfig requireEnabledParkConfig(SmtIscParkConfig config) {
		if (config == null || DeleteStatusEnum.IS_DELETE.getCode().equals(config.getDelFlag())) {
			throw new TCEException("园区未绑定ISC平台");
		}
		if (!DeviceSyncEnum.YES.getCode().equals(config.getCardSyncEnabled())) {
			throw new TCEException("园区未启用ISC卡片同步");
		}
		if (config.getDispatcherParkId() == null) {
			throw new TCEException("园区ISC调度园区不能为空");
		}
		return config;
	}

	private IscCardImportStartReqDTO parseParams(String paramsJson) {
		if (StrUtil.isBlank(paramsJson)) {
			return new IscCardImportStartReqDTO();
		}
		return JSONUtil.toBean(paramsJson, IscCardImportStartReqDTO.class);
	}

	private IscCardImportStaffScopeEnum requireStaffScope(String staffScope) {
		IscCardImportStaffScopeEnum scope = IscCardImportStaffScopeEnum.getByCode(staffScope);
		if (scope == null) {
			throw new TCEException("ISC卡片初始化同步人员范围不支持");
		}
		return scope;
	}

	private String firstLocalCardNo(List<SmtIscStaffCard> cards) {
		return CollectionUtil.isEmpty(cards) ? null : cards.get(0).getCardNo();
	}

	private String buildActiveKey(Integer dispatcherParkId, String cardNo) {
		return dispatcherParkId + ":" + cardNo;
	}

	private String normalizeText(String text) {
		return StrUtil.isBlank(text) ? null : text.trim();
	}

	private String normalizeCardNo(String cardNo) {
		return StrUtil.isBlank(cardNo) ? null : cardNo.trim();
	}

	private String limitText(String text, int maxLength) {
		if (text == null || text.length() <= maxLength) {
			return text;
		}
		return text.substring(0, maxLength);
	}

	private boolean isRealCardNo(String cardNo) {
		return StrUtil.isNotBlank(cardNo)
				&& cardNo.matches(ISC_CARD_NO_PATTERN)
				&& !cardNo.startsWith(ISC_VIRTUAL_CARD_PREFIX);
	}

	private boolean shouldReplaceIscPerson(JSONObject oldPerson, JSONObject newPerson) {
		return isDeletedIscPerson(oldPerson) && !isDeletedIscPerson(newPerson);
	}

	private boolean isDeletedIscPerson(JSONObject person) {
		Integer status = person.getInt("status");
		return status != null && status < 0;
	}

	private <T> List<List<T>> partition(List<T> source, int size) {
		if (CollectionUtil.isEmpty(source)) {
			return Collections.emptyList();
		}
		List<List<T>> result = new ArrayList<>();
		for (int i = 0; i < source.size(); i += size) {
			result.add(source.subList(i, Math.min(i + size, source.size())));
		}
		return result;
	}

	private IPage<SmtIscCardImportBatch> emptyBatchPage(Page page) {
		long current = page == null ? 1 : page.getCurrent();
		long size = page == null ? 10 : page.getSize();
		Page<SmtIscCardImportBatch> emptyPage = new Page<>(current, size);
		emptyPage.setRecords(Collections.emptyList());
		emptyPage.setTotal(0);
		return emptyPage;
	}

	private IPage<SmtIscCardImportDetail> emptyDetailPage(Page page) {
		long current = page == null ? 1 : page.getCurrent();
		long size = page == null ? 10 : page.getSize();
		Page<SmtIscCardImportDetail> emptyPage = new Page<>(current, size);
		emptyPage.setRecords(Collections.emptyList());
		emptyPage.setTotal(0);
		return emptyPage;
	}

	private String currentUsername() {
		try {
			SmartUser user = SecurityUtils.getUser();
			return user == null ? null : user.getUsername();
		} catch (Exception e) {
			log.debug("未获取到当前登录用户，ISC卡片初始化同步操作人置空：{}", e.getMessage());
			return null;
		}
	}

	private static class BatchStats {
		private int successCount;
		private int skipCount;
		private int conflictCount;
		private int failCount;

		private int totalCount() {
			return successCount + skipCount + conflictCount + failCount;
		}
	}
}
