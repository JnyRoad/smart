package com.tce.smart.ehrview.core.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwAcardlostAll;
import com.tce.smart.ehrview.core.mapper.EvwAcardlostAllMapper;
import com.tce.smart.ehrview.core.service.EvwAcardlostAllService;

import java.util.List;

@Service
public class EvwAcardlostAllServiceImpl extends ServiceImpl<EvwAcardlostAllMapper, EvwAcardlostAll> implements EvwAcardlostAllService {

	@Override
	public List<EvwAcardlostAll> list(String badge, String queryMonth) {
		return this.getBaseMapper().list(badge, queryMonth);
	}
}
