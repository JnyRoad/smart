package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.platform.core.entity.SmtOutSrcApplyDetails;
import com.tce.smart.platform.core.mapper.SmtOutSrcApplyDetailsMapper;
import com.tce.smart.platform.service.SmtOutSrcApplyDetailsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/2 17:00
 */
@Service
@AllArgsConstructor
@Slf4j
public class SmtOutSrcApplyDetailsImpl extends ServiceImpl<SmtOutSrcApplyDetailsMapper, SmtOutSrcApplyDetails> implements SmtOutSrcApplyDetailsService {

	@Override
	public IPage<SmtOutSrcApplyDetails> getByApplyId(Page page, Long applyId) {
		return this.page(page, Wrappers.<SmtOutSrcApplyDetails>lambdaQuery()
				.eq(SmtOutSrcApplyDetails::getApplyId, applyId));
	}

	@Override
	public List<SmtOutSrcApplyDetails> getByApplyId(Long applyId) {
		return this.list(Wrappers.<SmtOutSrcApplyDetails>lambdaQuery()
				.eq(SmtOutSrcApplyDetails::getApplyId, applyId));
	}

	@Override
	public SmtOutSrcApplyDetails getByBadgeAndApplyId(String badge, List<Long> legalApplyIds) {
		if(CollectionUtils.isEmpty(legalApplyIds)) {
			return null;
		}
		return this.getOne(Wrappers.<SmtOutSrcApplyDetails>lambdaQuery()
				.eq(SmtOutSrcApplyDetails::getBadge, badge)
				.in(SmtOutSrcApplyDetails::getApplyId, legalApplyIds));
	}
}
