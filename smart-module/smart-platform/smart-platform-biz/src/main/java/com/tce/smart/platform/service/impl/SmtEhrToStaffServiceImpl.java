package com.tce.smart.platform.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.dto.SearchEhrToStaffDTO;
import com.tce.smart.platform.core.entity.SmtEhrToStaff;
import com.tce.smart.platform.core.mapper.SmtEhrToStaffMapper;
import com.tce.smart.platform.service.SmtEhrToStaffService;

@Service
public class SmtEhrToStaffServiceImpl extends ServiceImpl<SmtEhrToStaffMapper, SmtEhrToStaff> implements SmtEhrToStaffService  {

	@Override
	public IPage<SmtEhrToStaff> page(Page page, SearchEhrToStaffDTO searchEhrToStaffDTO) {
		// TODO Auto-generated method stub
		return this.baseMapper.page(page, searchEhrToStaffDTO);
	}

}
