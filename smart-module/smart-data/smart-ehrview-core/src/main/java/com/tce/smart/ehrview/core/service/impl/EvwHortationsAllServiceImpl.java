package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwHortationsAll;
import com.tce.smart.ehrview.core.mapper.EvwHortationsAllMapper;
import com.tce.smart.ehrview.core.service.IEvwHortationsAllService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Descripition:
 * @Auther: guohongtai
 * @Date: 2020-07-14 09:12
 */
@Service
public class EvwHortationsAllServiceImpl extends ServiceImpl<EvwHortationsAllMapper, EvwHortationsAll> implements IEvwHortationsAllService {
	@Override
	public List<EvwHortationsAll> list(String badge, String queryMonth) {
		return this.baseMapper.list(badge, queryMonth);
	}
}
