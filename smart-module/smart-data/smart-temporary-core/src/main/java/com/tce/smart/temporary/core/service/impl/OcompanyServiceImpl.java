package com.tce.smart.temporary.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.temporary.core.entity.Ocompany;
import com.tce.smart.temporary.core.mapper.OcompanyMapper;
import com.tce.smart.temporary.core.service.OcompanyService;

@Service
public class OcompanyServiceImpl  extends ServiceImpl<OcompanyMapper, Ocompany> implements OcompanyService{

	@Autowired
	private OcompanyMapper mapper;


	@Override
	public Ocompany getByComId(Integer compId) {
		// TODO Auto-generated method stub
		return mapper.selectOne(Wrappers.<Ocompany> query().lambda().eq(Ocompany::getCompID, compId));
	}

}
