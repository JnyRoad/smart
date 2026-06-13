package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.SearchEhrToStaffDTO;
import com.tce.smart.platform.core.entity.SmtEhrToStaff;


public interface SmtEhrToStaffService  extends IService<SmtEhrToStaff>{

	IPage<SmtEhrToStaff> page(Page page, SearchEhrToStaffDTO searchEhrToStaffDTO);

}
