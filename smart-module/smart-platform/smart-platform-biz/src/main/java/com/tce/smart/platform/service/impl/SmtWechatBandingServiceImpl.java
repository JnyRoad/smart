package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.WechatBandingReqDTO;
import com.tce.smart.platform.core.entity.SmtWechatBanding;
import com.tce.smart.platform.core.mapper.SmtWechatBandingMapper;
import com.tce.smart.platform.service.SmtWechatBandingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 微信绑定表
 *
 * @author fushiping
 * @date 2021-10-09 17:20:23
 */
@Service
@Slf4j
public class SmtWechatBandingServiceImpl extends ServiceImpl<SmtWechatBandingMapper, SmtWechatBanding> implements SmtWechatBandingService {

	@Override
	public String getUnionId(String openId) {
		SmtWechatBanding banding = this.getOne(Wrappers.<SmtWechatBanding>query().lambda().eq(SmtWechatBanding::getOpenId, openId));
		if(Objects.isNull(banding)) {
			return banding.getUnionId();
		}
		return null;
	}

	@Override
	public IPage<SmtWechatBanding> getPage(Page page, WechatBandingReqDTO reqDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return baseMapper.queryPage(page, reqDTO, parkIds);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveInfo(WechatBandingReqDTO req) {
		SmtWechatBanding reBanding = this.getOne(Wrappers.<SmtWechatBanding>query().lambda()
				.eq(StringUtils.isNotEmpty(req.getOpenId()), SmtWechatBanding::getOpenId, req.getOpenId()));
		if(Objects.nonNull(reBanding)) {
			reBanding.setBadge(req.getBadge());
			reBanding.setUnionId(req.getUnionId());
			return this.updateById(reBanding);
		}
		SmtWechatBanding banding = BeanUtils.transform(SmtWechatBanding.class, req);
		banding.setCreateTime(LocalDateTime.now());
		return this.save(banding);
	}

}
