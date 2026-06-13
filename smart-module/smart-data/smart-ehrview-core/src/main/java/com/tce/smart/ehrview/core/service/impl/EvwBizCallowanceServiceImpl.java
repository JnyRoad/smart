package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwBizCallowance;
import com.tce.smart.ehrview.core.mapper.EvwBizCallowanceMapper;
import com.tce.smart.ehrview.core.service.IEvwBizCallowanceService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 09:37
 */
@Service
public class EvwBizCallowanceServiceImpl extends ServiceImpl<EvwBizCallowanceMapper, EvwBizCallowance> implements IEvwBizCallowanceService {
	@Override
	public List<EvwBizCallowance> list(String badge, String queryMonth) {
		return this.getBaseMapper().list(badge, queryMonth);
	}
}
