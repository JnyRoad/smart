package com.tce.smart.platform.service.dormitoryconfig.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.dormitoryconfig.DormitoryConfigEditReqDTO;
import com.tce.smart.platform.core.entity.dormitoryconfig.SmtDormitoryConfig;
import com.tce.smart.platform.core.mapper.SmtDormitoryConfigMapper;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryConfigService;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.util.ToolUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 *
 * @author fushiping
 * @date 2021-09-14 20:14:53
 */
@Service
public class SmtDormitoryConfigServiceImpl extends ServiceImpl<SmtDormitoryConfigMapper, SmtDormitoryConfig> implements SmtDormitoryConfigService {

	@Autowired
	private SmtDormitoryPersonService smtDormitoryPersonService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean editConfig(DormitoryConfigEditReqDTO editReqDTO) {
		if(Objects.isNull(editReqDTO)) {
			throw new SmartException("保存对象为空");
		}
		SmtDormitoryConfig config = BeanUtils.transform(SmtDormitoryConfig.class, editReqDTO);
		if(CollUtil.isNotEmpty(editReqDTO.getRelationBus())) {
			config.setRelationBus(StringUtils.join(editReqDTO.getRelationBus(), SymbolConstants.COMMA));
		}
		this.saveOrUpdate(config);
		Integer count = this.count(Wrappers.<SmtDormitoryConfig>query().lambda().eq(SmtDormitoryConfig::getParkId, editReqDTO.getParkId()));
		if(count > 0) {
			throw new SmartException("该园区已存在宿舍设置");
		}
		//保存后台数据权限
		return smtDormitoryPersonService.editPerson(editReqDTO.getPersonList(), config.getId());
	}

	@Override
	public IPage<SmtDormitoryConfig> getPage(Page page, Integer parkId) {
		return this.page(page, Wrappers.<SmtDormitoryConfig>query().lambda()
				.in(SmtDormitoryConfig::getParkId, SecurityUtils.getUser().getParkIdList())
				.eq(SmtDormitoryConfig::getParkId, parkId)
				.orderByDesc(SmtDormitoryConfig::getCreateTime));
	}

	@Override
	public SmtDormitoryConfig getByParkId(Integer parkId) {
		return this.getOne(Wrappers.<SmtDormitoryConfig>query().lambda().eq(SmtDormitoryConfig::getParkId, parkId));
	}

	@Override
	public List<String> getRelationBu(Integer parkId) {
		SmtDormitoryConfig config = this.getByParkId(parkId);
		if(Objects.nonNull(config)) {
			if(StringUtils.isNotEmpty(config.getRelationBus())) {
				return ToolUtils.splitStr(config.getRelationBus());
			}
		}
		return null;
	}

	@Override
	public List<String> getRelationBus(List<Integer> parkId) {
		List<SmtDormitoryConfig> configs = this.list(Wrappers.<SmtDormitoryConfig>query().lambda().in(SmtDormitoryConfig::getParkId, parkId));
		if(CollUtil.isNotEmpty(configs)) {
			List<String> bus = new ArrayList<>();
			for (SmtDormitoryConfig config : configs) {
				List<String> configBu = ToolUtils.splitStr(config.getRelationBus());
				bus.addAll(configBu);
			}
			return bus;
		}
		return new ArrayList<>();
	}

}
