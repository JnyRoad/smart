package com.tce.smart.platform.core.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SearchEhrToStaffDTO;
import com.tce.smart.platform.core.entity.SmtEhrToStaff;

public interface SmtEhrToStaffMapper  extends BaseMapper<SmtEhrToStaff>{

	IPage<SmtEhrToStaff> page(Page page, @Param("query") SearchEhrToStaffDTO searchEhrToStaffDTO);

}
