package com.tce.smart.ehrview.core.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwCcdFlstandard;
import com.tce.smart.ehrview.core.mapper.EvwCcdFlstandardMapper;
import com.tce.smart.ehrview.core.service.EvwCcdFlstandardService;


@Service
public class EvwCcdFlstandardServiceImpl extends ServiceImpl<EvwCcdFlstandardMapper, EvwCcdFlstandard> implements EvwCcdFlstandardService {


	@Autowired
	private EvwCcdFlstandardMapper mapper;
	@Override
	public EvwCcdFlstandard getFlById(String id) {
		// TODO Auto-generated method stub
		List<EvwCcdFlstandard> selectList = mapper.selectList(Wrappers.<EvwCcdFlstandard> query().lambda().eq(EvwCcdFlstandard::getJchenid, id));
		if(selectList.size()>0)
		{
			return selectList.get(0);
		}
		return null;
	}

}
