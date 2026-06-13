package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.OvwYsCallOwanceDetails;
import com.tce.smart.ehrview.core.mapper.OvwYsCallOwanceDetailsMapper;
import com.tce.smart.ehrview.core.service.IOvwYsCallOwanceDetailsService;
import org.springframework.stereotype.Service;


@Service
public class OvwYsCallOwanceDetailsServiceImpl extends ServiceImpl<OvwYsCallOwanceDetailsMapper, OvwYsCallOwanceDetails> implements IOvwYsCallOwanceDetailsService {


	@Override
	public OvwYsCallOwanceDetails getByBadge(String badge) {
		return this.baseMapper.getByBadge(badge);
	}
}
