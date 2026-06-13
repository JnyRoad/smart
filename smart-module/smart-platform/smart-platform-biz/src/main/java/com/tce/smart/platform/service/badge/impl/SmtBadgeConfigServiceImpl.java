package com.tce.smart.platform.service.badge.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.badge.EditBadgeConfigReqDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.badge.SmtBadgeConfig;
import com.tce.smart.platform.core.mapper.SmtBadgeConfigMapper;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.badge.SmtBadgeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 厂牌领取设置
 *
 * @author fushiping
 * @date 2020-07-07 11:47:51
 */
@Service
public class SmtBadgeConfigServiceImpl extends ServiceImpl<SmtBadgeConfigMapper, SmtBadgeConfig> implements SmtBadgeConfigService {

	@Autowired
	private SmtParkService smtParkService;

	@Override
	public Boolean edit(EditBadgeConfigReqDTO reqDTO) {
		SmtBadgeConfig reConfig = this.getConfigByPark(reqDTO.getParkId());
		if(Objects.nonNull(reConfig)) {
			if((Objects.nonNull(reqDTO.getId()) && !reConfig.getId().equals(reqDTO.getId()))){
				throw new SmartException("该园区已存在厂牌补领配置");
			}
			if(Objects.isNull(reqDTO.getId())) {
				throw new SmartException("该园区已存在厂牌补领配置");
			}
		}
		SmtBadgeConfig smtBadgeConfig = BeanUtils.transform(SmtBadgeConfig.class, reqDTO);
		SmtPark park = smtParkService.getById(reqDTO.getParkId());
		if(Objects.nonNull(park)) {
			smtBadgeConfig.setParkName(park.getParkName());
		}
		if(Objects.isNull(smtBadgeConfig.getId())) {
			smtBadgeConfig.setCreateTime(LocalDateTime.now());
		}
		return this.saveOrUpdate(smtBadgeConfig);
	}

	@Override
	public IPage<SmtBadgeConfig> getPage(Page page, Integer parkId) {
		List<Integer> parkList = SecurityUtils.getUser().getParkIdList();
		return this.page(page, Wrappers.<SmtBadgeConfig>query().lambda()
						.eq(Objects.nonNull(parkId), SmtBadgeConfig::getParkId, parkId)
						.in(CollectionUtils.isNotEmpty(parkList), SmtBadgeConfig::getParkId, parkList)
						.orderByDesc(SmtBadgeConfig::getCreateTime));
	}

	@Override
	public SmtBadgeConfig getConfigByPark(Integer parkId) {
		return this.getOne(Wrappers.<SmtBadgeConfig>query().lambda().eq(SmtBadgeConfig::getParkId, parkId));
	}
}
