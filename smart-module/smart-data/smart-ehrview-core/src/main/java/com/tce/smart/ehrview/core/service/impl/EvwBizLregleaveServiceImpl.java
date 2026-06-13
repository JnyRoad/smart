package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwBizLregleave;
import com.tce.smart.ehrview.core.mapper.EvwBizLregleaveMapper;
import com.tce.smart.ehrview.core.service.EvwBizLregleaveService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-20 19:02
 */
@Service
public class EvwBizLregleaveServiceImpl extends ServiceImpl<EvwBizLregleaveMapper, EvwBizLregleave> implements EvwBizLregleaveService {

	@Override
	public List<EvwBizLregleave> list(String badge, String queryMonth) {
		return this.getBaseMapper().list(badge, queryMonth);
	}
}
