package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.entity.SmtVisitJcheLimit;
import com.tce.smart.platform.core.mapper.SmtVisitJcheLimitMapper;
import com.tce.smart.platform.service.SmtVisitJcheLimitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 *
 * @author fushiping
 * @date 2020-08-06 15:30:50
 */
@Service
public class SmtVisitJcheLimitServiceImpl extends ServiceImpl<SmtVisitJcheLimitMapper, SmtVisitJcheLimit> implements SmtVisitJcheLimitService {

	@Override
	public List<SmtVisitJcheLimit> listByParkId(Integer parkId, Integer type) {
		return this.list(Wrappers.<SmtVisitJcheLimit>query().lambda()
				.eq(SmtVisitJcheLimit::getParkId, parkId)
				.eq(SmtVisitJcheLimit::getLimitType, type));
	}

	@Override
	public Boolean removeByParkId(Integer parkId, Integer type) {
		return this.remove(Wrappers.<SmtVisitJcheLimit>query().lambda()
				.eq(SmtVisitJcheLimit::getParkId, parkId)
				.eq(SmtVisitJcheLimit::getLimitType, type));
	}

	@Override
	public List<SmtVisitJcheLimit> listByJcheId(Integer parkId, String jcheId, Integer type) {
		return this.list(Wrappers.<SmtVisitJcheLimit>query().lambda()
				.eq(SmtVisitJcheLimit::getJcheId, jcheId)
				.eq(SmtVisitJcheLimit::getLimitType, type)
				.eq(Objects.nonNull(parkId), SmtVisitJcheLimit::getParkId, parkId));
	}

	@Override
	public List<String> getJcheIds(Integer parkId, Integer type) {
		List<SmtVisitJcheLimit> list = this.listByParkId(parkId, type);
		return list.stream().map(SmtVisitJcheLimit::getJcheId).sorted().collect(Collectors.toList());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveList(Integer parkId, List<String> jcheList, Integer type) {
		this.removeByParkId(parkId, type);
		SmtVisitJcheLimit smtVisitJcheLimit;
		if(CollectionUtil.isNotEmpty(jcheList)) {
			for (String element : jcheList) {
				smtVisitJcheLimit = new SmtVisitJcheLimit();
				smtVisitJcheLimit.setParkId(parkId);
				smtVisitJcheLimit.setJcheId(element);
				smtVisitJcheLimit.setLimitType(type);
				smtVisitJcheLimit.setCreateTime(LocalDateTime.now());
				smtVisitJcheLimit.insert();
			}
		}
		return Boolean.TRUE;
	}

	@Override
	public IPage getList(Page page, Integer type) {
		List<Integer> parkId = SecurityUtils.getUser().getParkIdList();
		return this.baseMapper.getPage(page, parkId, type);
	}
}
