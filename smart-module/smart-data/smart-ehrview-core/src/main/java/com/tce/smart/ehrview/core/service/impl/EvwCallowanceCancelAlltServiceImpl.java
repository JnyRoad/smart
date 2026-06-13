package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwCallowanceCancelAllt;
import com.tce.smart.ehrview.core.mapper.EvwCallowanceCancelAlltMapper;
import com.tce.smart.ehrview.core.service.IEvwCallowanceCancelAlltService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 10:52
 */
@Service
public class EvwCallowanceCancelAlltServiceImpl extends ServiceImpl<EvwCallowanceCancelAlltMapper, EvwCallowanceCancelAllt> implements IEvwCallowanceCancelAlltService {
	@Override
	public List<EvwCallowanceCancelAllt> list(String badge, String queryMonth) {
		return this.getBaseMapper().list(badge, queryMonth);
	}
}
