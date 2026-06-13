package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwCallowanceAllt;
import com.tce.smart.ehrview.core.mapper.EvwCallowanceAlltMapper;
import com.tce.smart.ehrview.core.service.IEvwCallowanceAlltService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 10:52
 */
@Service
public class EvwCallowanceAlltServiceImpl extends ServiceImpl<EvwCallowanceAlltMapper, EvwCallowanceAllt> implements IEvwCallowanceAlltService {
	@Override
	public List<EvwCallowanceAllt> list(String badge, String queryMonth) {
		return this.getBaseMapper().list(badge, queryMonth);
	}
}
