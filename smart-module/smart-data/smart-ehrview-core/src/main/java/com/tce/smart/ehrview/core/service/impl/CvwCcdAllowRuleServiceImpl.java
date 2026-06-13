package com.tce.smart.ehrview.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.CvwCcdAllowRule;
import com.tce.smart.ehrview.core.entity.CvwCcdAllowance;
import com.tce.smart.ehrview.core.mapper.CvwCcdAllowRuleMapper;
import com.tce.smart.ehrview.core.service.CvwCcdAllowRuleService;

@Service
public class CvwCcdAllowRuleServiceImpl extends ServiceImpl<CvwCcdAllowRuleMapper, CvwCcdAllowRule> implements CvwCcdAllowRuleService {

	@Autowired
	private CvwCcdAllowRuleMapper mapper;

	@Override
	public CvwCcdAllowRule getById(String id) {
		// TODO Auto-generated method stub
		CvwCcdAllowRule selectById = mapper.selectById(id);
		return selectById;
	}

	@Override
	public CvwCcdAllowRule getByTitle(String title) {
		// TODO Auto-generated method stub
		CvwCcdAllowRule selectOne = mapper.selectOne(Wrappers.<CvwCcdAllowRule> query().lambda().eq(CvwCcdAllowRule::getTitle, title));
		return selectOne;
	}

}
