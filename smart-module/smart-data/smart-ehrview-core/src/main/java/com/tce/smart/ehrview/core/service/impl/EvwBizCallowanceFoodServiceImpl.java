package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwBizCallowanceFood;
import com.tce.smart.ehrview.core.mapper.EvwBizCallowanceFoodMapper;
import com.tce.smart.ehrview.core.service.IEvwBizCallowanceFoodService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 10:52iz
 */
@Service
public class EvwBizCallowanceFoodServiceImpl extends ServiceImpl<EvwBizCallowanceFoodMapper, EvwBizCallowanceFood> implements IEvwBizCallowanceFoodService {
	@Override
	public List<EvwBizCallowanceFood> list(String badge, String queryMonth) {
		return this.getBaseMapper().list(badge, queryMonth);
	}
}
