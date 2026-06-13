package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.isc.EditIscStaffCardReqDTO;
import com.tce.smart.platform.core.entity.SmtIscCardTask;
import com.tce.smart.platform.core.entity.SmtIscParkConfig;
import com.tce.smart.platform.core.entity.SmtIscStaffCard;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.enums.DeviceSyncEnum;
import com.tce.smart.platform.core.mapper.SmtIscStaffCardMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.service.SmtIscCardTaskService;
import com.tce.smart.platform.core.service.SmtIscParkConfigService;
import com.tce.smart.platform.core.service.SmtIscStaffCardService;
import com.tce.smart.tool.enums.DeleteStatusEnum;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class SmtIscStaffCardServiceImpl extends ServiceImpl<SmtIscStaffCardMapper, SmtIscStaffCard>
		implements SmtIscStaffCardService {

	private static final String ISC_CARD_NO_PATTERN = "[0-9A-Z]{8,20}";
	private static final String ISC_VIRTUAL_CARD_PREFIX = "999";
	private static final String SOURCE_TYPE_STAFF = "STAFF";
	private static final int SYNC_PENDING = 0;
	private static final int SYNC_SUCCESS = 1;
	private static final int SYNC_FAILED = 2;
	private static final int SYNC_CANCEL = 3;

	@Autowired
	private SmtStaffMapper smtStaffMapper;

	@Autowired
	private SmtIscParkConfigService smtIscParkConfigService;

	@Autowired
	private SmtIscCardTaskService smtIscCardTaskService;

	@Override
	public List<SmtIscStaffCard> listStaffCards(Long staffId) {
		if (staffId == null) {
			throw new TCEException("员工ID不能为空");
		}
		return this.list(new LambdaQueryWrapper<SmtIscStaffCard>()
				.eq(SmtIscStaffCard::getStaffId, staffId)
				.eq(SmtIscStaffCard::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode())
				.orderByDesc(SmtIscStaffCard::getCreateTime)
				.orderByDesc(SmtIscStaffCard::getId));
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean saveStaffCard(EditIscStaffCardReqDTO reqDTO) {
		validateRequest(reqDTO);
		String cardNo = normalizeCardNo(reqDTO.getCardNo());
		validateCardNo(cardNo);
		SmtIscStaffCard oldCard = null;
		Long staffId = reqDTO.getStaffId();
		if (reqDTO.getId() != null) {
			oldCard = getActiveCard(reqDTO.getId());
			staffId = oldCard.getStaffId();
		}
		SmtStaff staff = getActiveStaff(staffId);
		SmtIscParkConfig config = getEnabledParkConfig(reqDTO.getParkId());
		if (oldCard != null && Objects.equals(oldCard.getCardNo(), cardNo)
				&& Objects.equals(oldCard.getParkId(), config.getParkId())
				&& Objects.equals(oldCard.getDispatcherParkId(), config.getDispatcherParkId())) {
			return Boolean.TRUE;
		}
		assertNoActiveDuplicate(reqDTO.getId(), staff, config.getDispatcherParkId(), cardNo);
		if (oldCard != null) {
			softDelete(oldCard);
		}
		SmtIscStaffCard newCard = buildCard(staff, config, cardNo);
		if (!this.save(newCard)) {
			throw new TCEException("保存员工ISC卡片失败");
		}
		if (oldCard != null) {
			createDeleteTask(oldCard);
		}
		createAddTask(staff, config.getDispatcherParkId(), cardNo);
		return Boolean.TRUE;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean removeStaffCard(Long id) {
		SmtIscStaffCard card = getActiveCard(id);
		softDelete(card);
		createDeleteTask(card);
		return Boolean.TRUE;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean removeStaffCardsByStaffId(Long staffId) {
		if (staffId == null) {
			throw new TCEException("员工ID不能为空");
		}
		List<SmtIscStaffCard> cards = listStaffCards(staffId);
		for (SmtIscStaffCard card : cards) {
			softDelete(card);
			createDeleteTask(card);
		}
		return Boolean.TRUE;
	}

	@Override
	public boolean isActiveStaffCard(Long staffId, String badge, Integer dispatcherParkId, String cardNo) {
		String normalizedCardNo = normalizeCardNo(cardNo);
		if (staffId == null || StrUtil.isBlank(badge) || dispatcherParkId == null
				|| !isRealCardNo(normalizedCardNo)) {
			return false;
		}
		return this.count(new LambdaQueryWrapper<SmtIscStaffCard>()
				.eq(SmtIscStaffCard::getStaffId, staffId)
				.eq(SmtIscStaffCard::getBadge, badge.trim())
				.eq(SmtIscStaffCard::getDispatcherParkId, dispatcherParkId)
				.eq(SmtIscStaffCard::getCardNo, normalizedCardNo)
				.eq(SmtIscStaffCard::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode())) > 0;
	}

	@Override
	public void markAddTaskSuccess(SmtIscCardTask task) {
		updateAddTaskSyncResult(task, SYNC_SUCCESS, false);
	}

	@Override
	public void markAddTaskFailed(SmtIscCardTask task, boolean removeLocalCard) {
		updateAddTaskSyncResult(task, SYNC_FAILED, removeLocalCard);
	}

	@Override
	public String getFirstActiveCardNoByBadge(String badge) {
		if (StrUtil.isBlank(badge)) {
			return null;
		}
		List<SmtIscStaffCard> cards = this.list(new LambdaQueryWrapper<SmtIscStaffCard>()
				.eq(SmtIscStaffCard::getBadge, badge.trim())
				.eq(SmtIscStaffCard::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode())
				.eq(SmtIscStaffCard::getSyncStatus, SYNC_SUCCESS)
				.orderByDesc(SmtIscStaffCard::getUpdateTime)
				.orderByDesc(SmtIscStaffCard::getCreateTime));
		return cards.isEmpty() ? null : cards.get(0).getCardNo();
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public SmtIscStaffCard importStaffCardFromIsc(SmtStaff staff, SmtIscParkConfig config, String cardNo, String remark) {
		return importStaffCardFromIsc(staff, config, cardNo, remark, currentUsername());
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public SmtIscStaffCard importStaffCardFromIsc(SmtStaff staff, SmtIscParkConfig config, String cardNo,
												  String remark, String optUser) {
		validateImportRequest(staff, config);
		String normalizedCardNo = normalizeCardNo(cardNo);
		validateCardNo(normalizedCardNo);
		SmtIscStaffCard activeCard = this.getOne(new LambdaQueryWrapper<SmtIscStaffCard>()
				.eq(SmtIscStaffCard::getDispatcherParkId, config.getDispatcherParkId())
				.eq(SmtIscStaffCard::getCardNo, normalizedCardNo)
				.eq(SmtIscStaffCard::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode()));
		if (activeCard != null) {
			if (isSameBadge(activeCard, staff)) {
				return activeCard;
			}
			throw duplicateCardException(activeCard);
		}
		SmtIscStaffCard importedCard = buildCard(staff, config, normalizedCardNo);
		importedCard.setSyncStatus(SYNC_SUCCESS);
		importedCard.setRemark(remark);
		importedCard.setOptUser(optUser);
		if (!this.save(importedCard)) {
			throw new TCEException("导入员工ISC卡片失败");
		}
		return importedCard;
	}

	private void validateRequest(EditIscStaffCardReqDTO reqDTO) {
		if (reqDTO == null) {
			throw new TCEException("员工ISC卡片不能为空");
		}
		if (reqDTO.getStaffId() == null) {
			throw new TCEException("员工ID不能为空");
		}
		if (reqDTO.getParkId() == null) {
			throw new TCEException("园区不能为空");
		}
	}

	private SmtIscStaffCard getActiveCard(Long id) {
		if (id == null) {
			throw new TCEException("员工ISC卡片ID不能为空");
		}
		SmtIscStaffCard card = this.getById(id);
		if (card == null || DeleteStatusEnum.IS_DELETE.getCode().equals(card.getDelFlag())) {
			throw new TCEException("员工ISC卡片不存在");
		}
		return card;
	}

	private SmtStaff getActiveStaff(Long staffId) {
		SmtStaff staff = smtStaffMapper.selectById(staffId);
		if (staff == null) {
			throw new TCEException("员工不存在");
		}
		if (StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(staff.getStatus())) {
			throw new TCEException("员工已离职");
		}
		if (StrUtil.isBlank(staff.getBadge())) {
			throw new TCEException("员工工号不能为空");
		}
		return staff;
	}

	private void validateImportRequest(SmtStaff staff, SmtIscParkConfig config) {
		if (staff == null) {
			throw new TCEException("员工不能为空");
		}
		if (staff.getId() == null) {
			throw new TCEException("员工ID不能为空");
		}
		if (StrUtil.isBlank(staff.getBadge())) {
			throw new TCEException("员工工号不能为空");
		}
		if (config == null || DeleteStatusEnum.IS_DELETE.getCode().equals(config.getDelFlag())) {
			throw new TCEException("园区未绑定ISC平台");
		}
		if (!DeviceSyncEnum.YES.getCode().equals(config.getCardSyncEnabled())) {
			throw new TCEException("园区未启用ISC卡片同步");
		}
		if (config.getParkId() == null) {
			throw new TCEException("园区不能为空");
		}
		if (config.getDispatcherParkId() == null) {
			throw new TCEException("园区ISC调度园区不能为空");
		}
	}

	private SmtIscParkConfig getEnabledParkConfig(Integer parkId) {
		SmtIscParkConfig config = smtIscParkConfigService.getConfigByPark(parkId);
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

	private void assertNoActiveDuplicate(Long id, SmtStaff staff, Integer dispatcherParkId, String cardNo) {
		LambdaQueryWrapper<SmtIscStaffCard> wrapper = new LambdaQueryWrapper<SmtIscStaffCard>()
				.eq(SmtIscStaffCard::getDispatcherParkId, dispatcherParkId)
				.eq(SmtIscStaffCard::getCardNo, cardNo)
				.eq(SmtIscStaffCard::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode());
		if (id != null) {
			wrapper.ne(SmtIscStaffCard::getId, id);
		}
		List<SmtIscStaffCard> duplicateCards = this.list(wrapper);
		if (duplicateCards == null || duplicateCards.isEmpty()) {
			return;
		}
		SmtIscStaffCard duplicateCard = duplicateCards.get(0);
		if (isSameBadge(duplicateCard, staff)) {
			throw new TCEException("该员工已存在相同ISC卡号");
		}
		throw duplicateCardException(duplicateCard);
	}

	private boolean isSameBadge(SmtIscStaffCard card, SmtStaff staff) {
		return card != null && staff != null
				&& Objects.equals(normalizeText(card.getBadge()), normalizeText(staff.getBadge()));
	}

	private TCEException duplicateCardException(SmtIscStaffCard duplicateCard) {
		String duplicateBadge = StrUtil.blankToDefault(duplicateCard.getBadge(), "-");
		return new TCEException("该卡号已被其他员工占用，工号：" + duplicateBadge);
	}

	private SmtIscStaffCard buildCard(SmtStaff staff, SmtIscParkConfig config, String cardNo) {
		LocalDateTime now = LocalDateTime.now();
		SmtIscStaffCard card = new SmtIscStaffCard();
		card.setStaffId(staff.getId());
		card.setBadge(staff.getBadge().trim());
		card.setParkId(config.getParkId());
		card.setParkName(config.getParkName());
		card.setDispatcherParkId(config.getDispatcherParkId());
		card.setDispatcherParkName(config.getDispatcherParkName());
		card.setCardNo(cardNo);
		card.setDelFlag(DeleteStatusEnum.NOT_DELETE.getCode());
		card.setActiveKey(buildActiveKey(config.getDispatcherParkId(), cardNo));
		card.setSyncStatus(SYNC_PENDING);
		card.setCreateTime(now);
		card.setUpdateTime(now);
		card.setOptUser(currentUsername());
		return card;
	}

	private void softDelete(SmtIscStaffCard card) {
		SmtIscStaffCard deletedCard = new SmtIscStaffCard();
		deletedCard.setId(card.getId());
		deletedCard.setDelFlag(DeleteStatusEnum.IS_DELETE.getCode());
		deletedCard.setSyncStatus(SYNC_CANCEL);
		deletedCard.setActiveKey(null);
		deletedCard.setDeleteTime(LocalDateTime.now());
		deletedCard.setUpdateTime(LocalDateTime.now());
		deletedCard.setOptUser(currentUsername());
		if (!this.updateById(deletedCard)) {
			throw new TCEException("删除员工ISC卡片失败");
		}
		card.setDelFlag(DeleteStatusEnum.IS_DELETE.getCode());
		card.setSyncStatus(SYNC_CANCEL);
		card.setActiveKey(null);
	}

	private void updateAddTaskSyncResult(SmtIscCardTask task, int syncStatus, boolean removeLocalCard) {
		if (!isStaffAddTask(task)) {
			return;
		}
		String normalizedCardNo = normalizeCardNo(task.getCardNo());
		if (StrUtil.isBlank(normalizedCardNo)) {
			return;
		}
		LocalDateTime now = LocalDateTime.now();
		LambdaUpdateWrapper<SmtIscStaffCard> wrapper = new LambdaUpdateWrapper<SmtIscStaffCard>()
				.eq(SmtIscStaffCard::getStaffId, task.getSourceId())
				.eq(SmtIscStaffCard::getBadge, normalizeText(task.getBadge()))
				.eq(SmtIscStaffCard::getDispatcherParkId, task.getParkId())
				.eq(SmtIscStaffCard::getCardNo, normalizedCardNo)
				.eq(SmtIscStaffCard::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode())
				.set(SmtIscStaffCard::getSyncStatus, syncStatus)
				.set(SmtIscStaffCard::getLastTaskId, task.getId())
				.set(SmtIscStaffCard::getLastSyncCode, task.getCode())
				.set(SmtIscStaffCard::getLastSyncRemark, task.getRemark())
				.set(SmtIscStaffCard::getLastSyncTime, now)
				.set(SmtIscStaffCard::getUpdateTime, now);
		if (removeLocalCard) {
			wrapper.set(SmtIscStaffCard::getDelFlag, DeleteStatusEnum.IS_DELETE.getCode())
					.set(SmtIscStaffCard::getActiveKey, null)
					.set(SmtIscStaffCard::getDeleteTime, now);
		}
		this.update(wrapper);
	}

	private boolean isStaffAddTask(SmtIscCardTask task) {
		return task != null
				&& DeviceTaskActionEnum.DOWN.getCode().equals(task.getAction())
				&& SOURCE_TYPE_STAFF.equals(task.getSourceType())
				&& task.getSourceId() != null
				&& StrUtil.isNotBlank(task.getBadge())
				&& task.getParkId() != null;
	}

	private String buildActiveKey(Integer dispatcherParkId, String cardNo) {
		if (dispatcherParkId == null || StrUtil.isBlank(cardNo)) {
			return null;
		}
		return dispatcherParkId + ":" + cardNo;
	}

	private void createAddTask(SmtStaff staff, Integer dispatcherParkId, String cardNo) {
		if (!smtIscCardTaskService.createAddStaffCardTask(staff.getId(), staff.getBadge(), dispatcherParkId, cardNo)) {
			throw new TCEException("创建ISC卡片新增任务失败");
		}
	}

	private void createDeleteTask(SmtIscStaffCard card) {
		if (isRealCardNo(card.getCardNo()) && !smtIscCardTaskService.createDeleteStaffCardTask(
				card.getStaffId(), card.getBadge(), card.getDispatcherParkId(), card.getCardNo())) {
			throw new TCEException("创建ISC卡片删除任务失败");
		}
	}

	private void validateCardNo(String cardNo) {
		if (StrUtil.isBlank(cardNo)) {
			throw new TCEException("ISC卡号不能为空");
		}
		if (!cardNo.matches(ISC_CARD_NO_PATTERN)) {
			throw new TCEException("ISC卡号必须为8-20位数字或大写字母");
		}
		if (!isRealCardNo(cardNo)) {
			throw new TCEException("ISC虚拟卡号不允许维护");
		}
	}

	private boolean isRealCardNo(String cardNo) {
		return StrUtil.isNotBlank(cardNo)
				&& cardNo.matches(ISC_CARD_NO_PATTERN)
				&& !cardNo.startsWith(ISC_VIRTUAL_CARD_PREFIX);
	}

	private String normalizeCardNo(String cardNo) {
		return StrUtil.isBlank(cardNo) ? null : cardNo.trim();
	}

	private String normalizeText(String text) {
		return StrUtil.isBlank(text) ? null : text.trim();
	}

	private String currentUsername() {
		try {
			SmartUser user = SecurityUtils.getUser();
			return user == null ? null : user.getUsername();
		} catch (Exception e) {
			log.debug("未获取到当前登录用户，员工ISC卡片操作人置空：{}", e.getMessage());
			return null;
		}
	}
}
