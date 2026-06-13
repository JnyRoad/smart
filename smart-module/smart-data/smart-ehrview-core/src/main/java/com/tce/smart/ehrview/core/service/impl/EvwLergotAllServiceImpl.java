package com.tce.smart.ehrview.core.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwLergotAll;
import com.tce.smart.ehrview.core.mapper.EvwLergotAllMapper;
import com.tce.smart.ehrview.core.service.EvwLergotAllService;

import java.util.List;

@Service
public class EvwLergotAllServiceImpl extends ServiceImpl<EvwLergotAllMapper, EvwLergotAll> implements EvwLergotAllService  {

	@Override
	public List<EvwLergotAll> list(String badge, String queryMonth) {
		return this.getBaseMapper().list(badge, queryMonth);
	}
}
