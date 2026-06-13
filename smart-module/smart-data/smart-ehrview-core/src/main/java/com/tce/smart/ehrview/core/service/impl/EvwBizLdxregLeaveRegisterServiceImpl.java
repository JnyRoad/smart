package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwBizLdxregLeaveRegister;
import com.tce.smart.ehrview.core.mapper.EvwBizLdxregLeaveRegisterMapper;
import com.tce.smart.ehrview.core.service.IEvwBizLdxregLeaveRegisterService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 11:42
 */
@Service
public class EvwBizLdxregLeaveRegisterServiceImpl extends ServiceImpl<EvwBizLdxregLeaveRegisterMapper, EvwBizLdxregLeaveRegister> implements IEvwBizLdxregLeaveRegisterService {
	@Override
	public List<EvwBizLdxregLeaveRegister> list(String badge, String queryMonth) {
		return this.getBaseMapper().list(badge, queryMonth);
	}
}
