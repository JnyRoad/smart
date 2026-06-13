package com.tce.smart.ehrview.core.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.CvwCcdAllowance;
import com.tce.smart.ehrview.core.mapper.CvwCcdAllowanceMapper;
import com.tce.smart.ehrview.core.service.CvwCcdAllowanceService;

@Service
public class CvwCcdAllowanceServiceImpl extends ServiceImpl<CvwCcdAllowanceMapper, CvwCcdAllowance> implements CvwCcdAllowanceService {

	@Override
	public CvwCcdAllowance getByName(String allowanceName) {
		// TODO Auto-generated method stub
		return this.baseMapper.selectOne(Wrappers.<CvwCcdAllowance> query().lambda().eq(CvwCcdAllowance::getTitle, allowanceName));
	}

}
