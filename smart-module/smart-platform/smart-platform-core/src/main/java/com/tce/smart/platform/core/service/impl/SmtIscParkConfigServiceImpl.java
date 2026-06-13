package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.isc.EditIscParkConfigReqDTO;
import com.tce.smart.platform.core.entity.SmtIscParkConfig;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.enums.DeviceSyncEnum;
import com.tce.smart.platform.core.mapper.SmtIscParkConfigMapper;
import com.tce.smart.platform.core.mapper.SmtParkMapper;
import com.tce.smart.platform.core.service.SmtIscParkConfigService;
import com.tce.smart.tool.enums.DeleteStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SmtIscParkConfigServiceImpl extends ServiceImpl<SmtIscParkConfigMapper, SmtIscParkConfig>
		implements SmtIscParkConfigService {

	@Autowired
	private SmtParkMapper smtParkMapper;

	@Override
	public Boolean editConfig(EditIscParkConfigReqDTO reqDTO) {
		validateEdit(reqDTO);
		assertParkExists(reqDTO.getParkId(), "园区不存在");
		assertParkExists(reqDTO.getDispatcherParkId(), "ISC调度园区不存在");
		assertParkAllowed(reqDTO.getParkId());
		assertParkAllowed(reqDTO.getDispatcherParkId());
		if (reqDTO.getId() != null) {
			SmtIscParkConfig oldConfig = getActiveConfigById(reqDTO.getId());
			if (oldConfig == null) {
				throw new TCEException("ISC平台绑定配置不存在");
			}
			assertParkAllowed(oldConfig.getParkId());
			assertParkAllowed(oldConfig.getDispatcherParkId());
		}
		SmtIscParkConfig existConfig = getActiveConfigByPark(reqDTO.getParkId());
		if (existConfig != null && !Objects.equals(existConfig.getId(), reqDTO.getId())) {
			throw new TCEException("该园区已存在ISC平台绑定配置");
		}
		SmtIscParkConfig config = buildConfig(reqDTO);
		return this.saveOrUpdate(config);
	}

	@Override
	public IPage<SmtIscParkConfig> getPage(Page page, Integer parkId) {
		List<Integer> userParkIds = currentUserParkIdsForConfig();
		return this.page(page, Wrappers.<SmtIscParkConfig>lambdaQuery()
				.eq(SmtIscParkConfig::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode())
				.eq(parkId != null, SmtIscParkConfig::getParkId, parkId)
				.in(SmtIscParkConfig::getParkId, userParkIds)
				.in(SmtIscParkConfig::getDispatcherParkId, userParkIds)
				.orderByDesc(SmtIscParkConfig::getCreateTime));
	}

	@Override
	public SmtIscParkConfig getConfigByPark(Integer parkId) {
		if (parkId == null) {
			return null;
		}
		assertParkAllowed(parkId);
		SmtIscParkConfig config = getActiveConfigByPark(parkId);
		if (config != null) {
			assertParkAllowed(config.getDispatcherParkId());
		}
		return config;
	}

	@Override
	public SmtIscParkConfig getActiveConfigById(Long id) {
		if (id == null) {
			return null;
		}
		List<Integer> userParkIds = currentUserParkIdsForConfig();
		SmtIscParkConfig config = this.getOne(Wrappers.<SmtIscParkConfig>lambdaQuery()
				.eq(SmtIscParkConfig::getId, id)
				.eq(SmtIscParkConfig::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode())
				.in(SmtIscParkConfig::getParkId, userParkIds)
				.in(SmtIscParkConfig::getDispatcherParkId, userParkIds), false);
		if (config != null && (!userParkIds.contains(config.getParkId())
				|| !userParkIds.contains(config.getDispatcherParkId()))) {
			return null;
		}
		return config;
	}

	@Override
	public Boolean removeConfigById(Long id) {
		if (id == null) {
			throw new TCEException("删除操作ID不能为空");
		}
		SmtIscParkConfig oldConfig = getActiveConfigById(id);
		if (oldConfig == null) {
			throw new TCEException("ISC平台绑定配置不存在");
		}
		SmtIscParkConfig config = new SmtIscParkConfig();
		config.setId(id);
		config.setDelFlag(DeleteStatusEnum.IS_DELETE.getCode());
		config.setActiveKey(null);
		config.setUpdateTime(LocalDateTime.now());
		config.setOptUser(currentUsername());
		return this.updateById(config);
	}

	@Override
	public Set<Integer> getCardSyncDispatcherParkIds(Collection<Integer> businessParkIds) {
		if (CollectionUtils.isEmpty(businessParkIds)) {
			return new LinkedHashSet<>();
		}
		Set<Integer> parkIds = businessParkIds.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		if (CollectionUtils.isEmpty(parkIds)) {
			return new LinkedHashSet<>();
		}
		List<SmtIscParkConfig> configs = this.list(new LambdaQueryWrapper<SmtIscParkConfig>()
				.in(SmtIscParkConfig::getParkId, parkIds)
				.eq(SmtIscParkConfig::getCardSyncEnabled, DeviceSyncEnum.YES.getCode())
				.eq(SmtIscParkConfig::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode()));
		if (CollectionUtils.isEmpty(configs)) {
			throw new TCEException("授权ISC设备所在园区未启用ISC卡片同步配置: " + parkIds);
		}
		Set<Integer> configuredParkIds = configs.stream()
				.filter(config -> config.getDispatcherParkId() != null)
				.map(SmtIscParkConfig::getParkId)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		Set<Integer> missingParkIds = new LinkedHashSet<>(parkIds);
		missingParkIds.removeAll(configuredParkIds);
		if (CollectionUtils.isNotEmpty(missingParkIds)) {
			throw new TCEException("授权ISC设备所在园区未启用ISC卡片同步配置: " + missingParkIds);
		}
		return configs.stream()
				.map(SmtIscParkConfig::getDispatcherParkId)
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private void validateEdit(EditIscParkConfigReqDTO reqDTO) {
		if (reqDTO == null) {
			throw new TCEException("ISC平台绑定配置不能为空");
		}
		if (reqDTO.getParkId() == null) {
			throw new TCEException("园区不能为空");
		}
		if (reqDTO.getDispatcherParkId() == null) {
			throw new TCEException("ISC调度园区不能为空");
		}
		if (reqDTO.getCardSyncEnabled() == null) {
			throw new TCEException("卡片同步开关不能为空");
		}
		if (!DeviceSyncEnum.YES.getCode().equals(reqDTO.getCardSyncEnabled())
				&& !DeviceSyncEnum.NO.getCode().equals(reqDTO.getCardSyncEnabled())) {
			throw new TCEException("卡片同步开关必须是0或1");
		}
	}

	private SmtIscParkConfig buildConfig(EditIscParkConfigReqDTO reqDTO) {
		LocalDateTime now = LocalDateTime.now();
		SmtIscParkConfig config = new SmtIscParkConfig();
		config.setId(reqDTO.getId());
		config.setParkId(reqDTO.getParkId());
		config.setParkName(getParkName(reqDTO.getParkId()));
		config.setDispatcherParkId(reqDTO.getDispatcherParkId());
		config.setDispatcherParkName(getParkName(reqDTO.getDispatcherParkId()));
		config.setCardSyncEnabled(reqDTO.getCardSyncEnabled());
		config.setDelFlag(DeleteStatusEnum.NOT_DELETE.getCode());
		config.setActiveKey(buildActiveKey(reqDTO.getParkId()));
		config.setRemark(reqDTO.getRemark());
		config.setOptUser(currentUsername());
		if (reqDTO.getId() == null) {
			config.setCreateTime(now);
		}
		config.setUpdateTime(now);
		return config;
	}

	private SmtIscParkConfig getActiveConfigByPark(Integer parkId) {
		return this.getOne(Wrappers.<SmtIscParkConfig>lambdaQuery()
				.eq(SmtIscParkConfig::getParkId, parkId)
				.eq(SmtIscParkConfig::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode()), false);
	}

	private String buildActiveKey(Integer parkId) {
		return parkId == null ? null : String.valueOf(parkId);
	}

	private void assertParkAllowed(Integer parkId) {
		List<Integer> userParkIds = currentUserParkIdsForConfig();
		if (!userParkIds.contains(parkId)) {
			throw new TCEException("无权限操作该园区ISC平台绑定配置");
		}
	}

	private void assertParkExists(Integer parkId, String message) {
		if (parkId == null) {
			throw new TCEException(message);
		}
		if (smtParkMapper == null || smtParkMapper.selectById(parkId) == null) {
			throw new TCEException(message);
		}
	}

	private String getParkName(Integer parkId) {
		if (parkId == null || smtParkMapper == null) {
			return null;
		}
		SmtPark park = smtParkMapper.selectById(parkId);
		return park == null ? null : park.getParkName();
	}

	private List<Integer> currentUserParkIdsForConfig() {
		try {
			SmartUser user = SecurityUtils.getUser();
			if (user == null || CollectionUtils.isEmpty(user.getParkIdList())) {
				throw new TCEException("未获取到当前登录用户园区权限");
			}
			return user.getParkIdList();
		} catch (TCEException e) {
			throw e;
		} catch (Exception e) {
			log.warn("未获取到当前登录用户园区权限，拒绝ISC平台绑定配置操作：{}", e.getMessage());
			throw new TCEException("未获取到当前登录用户园区权限");
		}
	}

	private String currentUsername() {
		try {
			SmartUser user = SecurityUtils.getUser();
			return user == null ? null : user.getUsername();
		} catch (Exception e) {
			log.debug("未获取到当前登录用户，ISC平台绑定配置操作人置空：{}", e.getMessage());
			return null;
		}
	}
}
