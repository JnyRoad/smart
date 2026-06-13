package com.tce.smart.ehrview.core.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwBizAregotRegister;
import com.tce.smart.ehrview.core.mapper.EvwBizAregotRegisterMapper;
import com.tce.smart.ehrview.core.service.EvwBizAregotRegisterService;

import java.util.List;


@Service
public class EvwBizAregotRegisterServiceImpl extends ServiceImpl<EvwBizAregotRegisterMapper,  EvwBizAregotRegister> implements  EvwBizAregotRegisterService {

	@Override
	public List<EvwBizAregotRegister> list(String badge, String queryMonth) {
		return this.baseMapper.list(badge, queryMonth);
	}
}
