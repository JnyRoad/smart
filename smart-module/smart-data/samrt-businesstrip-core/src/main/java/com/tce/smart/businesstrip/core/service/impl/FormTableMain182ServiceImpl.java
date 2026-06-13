package com.tce.smart.businesstrip.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.businesstrip.core.entity.FormTableMain182;
import com.tce.smart.businesstrip.core.mapper.FormTableMain182Mapper;
import com.tce.smart.businesstrip.core.service.FormTableMain182Service;
import org.springframework.stereotype.Service;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/10 11:53
 */
@Service
public class FormTableMain182ServiceImpl extends ServiceImpl<FormTableMain182Mapper, FormTableMain182> implements FormTableMain182Service {

	@Override
	public FormTableMain182 getByRequestId(String requestId) {
		return this.baseMapper.getByRequestId(requestId);
	}
}
