package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwLdxRegLeaveAll;
import com.tce.smart.ehrview.core.mapper.EvwLdxRegLeaveAllMapper;
import com.tce.smart.ehrview.core.service.IEvwLdxRegLeaveAllService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 11:37
 */
@Service
public class EvwLdxRegLeaveAllServiceImpl extends ServiceImpl<EvwLdxRegLeaveAllMapper, EvwLdxRegLeaveAll> implements IEvwLdxRegLeaveAllService {
	@Override
	public List<EvwLdxRegLeaveAll> list(String badge, String queryMonth) {
		return this.getBaseMapper().list(badge, queryMonth);
	}

	@Override
	public List<EvwLdxRegLeaveAll> listByDay(String badge, String queryMonth) {
		return this.getBaseMapper().listByDay(badge, queryMonth);
	}
}
