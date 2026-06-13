package com.tce.smart.ehrview.core.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwBizLcardlost;
import com.tce.smart.ehrview.core.mapper.EvwBizLcardlostMapper;
import com.tce.smart.ehrview.core.service.EvwBizLcardlostService;

import java.util.List;


@Service
public class EvwBizLcardlostServiceImpl extends ServiceImpl<EvwBizLcardlostMapper, EvwBizLcardlost> implements EvwBizLcardlostService  {

	@Override
	public List<EvwBizLcardlost> list(String badge, String queryMonth) {
		return this.getBaseMapper().list(badge, queryMonth);
	}
}
