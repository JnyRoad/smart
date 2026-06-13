package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwCotherAllowanceAll;
import com.tce.smart.ehrview.core.mapper.EvwCotherAllowanceAllMapper;
import com.tce.smart.ehrview.core.service.IEvwCotherAllowanceAllService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 09:39
 */
@Service
public class EvwCotherAllowanceAllServiceImpl extends ServiceImpl<EvwCotherAllowanceAllMapper, EvwCotherAllowanceAll> implements IEvwCotherAllowanceAllService {
	@Override
	public List<EvwCotherAllowanceAll> list(String badge, String queryMonth) {
		return this.getBaseMapper().list(badge, queryMonth);
	}
}
