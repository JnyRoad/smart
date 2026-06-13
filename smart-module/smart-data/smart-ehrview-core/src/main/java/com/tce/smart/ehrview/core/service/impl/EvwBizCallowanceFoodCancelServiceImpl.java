package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwBizCallowanceFoodCancel;
import com.tce.smart.ehrview.core.mapper.EvwBizCallowanceFoodCancelMapper;
import com.tce.smart.ehrview.core.service.IEvwBizCallowanceFoodCancelService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 11:08
 */
@Service
public class EvwBizCallowanceFoodCancelServiceImpl extends ServiceImpl<EvwBizCallowanceFoodCancelMapper, EvwBizCallowanceFoodCancel> implements IEvwBizCallowanceFoodCancelService {
	@Override
	public List<EvwBizCallowanceFoodCancel> list(String badge, String queryMonth) {
		return this.getBaseMapper().list(badge, queryMonth);
	}
}
