package com.tce.smart.ehrview.core.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwBizLregleaveRegister;
import com.tce.smart.ehrview.core.mapper.EvwBizLregleaveRegisterMapper;
import com.tce.smart.ehrview.core.service.EvwBizLregleaveRegisterService;

import java.util.List;

@Service
public class EvwBizLregleaveRegisterServiceImpl extends ServiceImpl<EvwBizLregleaveRegisterMapper, EvwBizLregleaveRegister> implements EvwBizLregleaveRegisterService {
	@Override
	public List<EvwBizLregleaveRegister> list(String badge, String queryMonth) {
		return this.getBaseMapper().list(badge, queryMonth);
	}
}
