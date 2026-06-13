package com.tce.smart.businesstrip.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.businesstrip.core.entity.FormTableMain182Dt1;
import com.tce.smart.businesstrip.core.mapper.FormTableMain182Dt1Mapper;
import com.tce.smart.businesstrip.core.service.FormTableMain182Dt1Service;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/10 11:53
 */
@Service
public class FormTableMain182Dt1ServiceImpl extends ServiceImpl<FormTableMain182Dt1Mapper, FormTableMain182Dt1> implements FormTableMain182Dt1Service {

	@Override
	public List<FormTableMain182Dt1> getByMainId(Integer mainId) {
		return this.baseMapper.getByMainId(mainId);
	}

	@Override
	public Boolean updateFcsj(List<FormTableMain182Dt1> dt1List) {
		return this.baseMapper.updateFcsj(dt1List);
	}
}
