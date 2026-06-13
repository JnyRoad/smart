package com.tce.smart.businesstrip.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.businesstrip.core.entity.FormTableMain182Dt2;
import com.tce.smart.businesstrip.core.mapper.FormTableMain182Dt2Mapper;
import com.tce.smart.businesstrip.core.service.FormTableMain182Dt2Service;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/10 11:53
 */
@Service
public class FormTableMain182Dt2ServiceImpl extends ServiceImpl<FormTableMain182Dt2Mapper, FormTableMain182Dt2> implements FormTableMain182Dt2Service {

	@Override
	public List<FormTableMain182Dt2> getByMainId(Integer mainId) {
		return this.baseMapper.getByMainId(mainId);
	}

	@Override
	public Boolean updateFcsj(List<FormTableMain182Dt2> dt2List) {
		return this.baseMapper.updateFcsj(dt2List);
	}
}
