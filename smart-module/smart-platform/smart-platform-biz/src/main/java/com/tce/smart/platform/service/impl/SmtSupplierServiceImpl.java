package com.tce.smart.platform.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.entity.SmtSupplier;
import com.tce.smart.platform.core.mapper.SmtSupplierMapper;
import com.tce.smart.platform.service.SmtSupplierService;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * 园区供应商
 * @author QIPEI
 *
 */

@Slf4j
@Service
public class SmtSupplierServiceImpl extends ServiceImpl< SmtSupplierMapper,  SmtSupplier> implements  SmtSupplierService {



	@Override
	public IPage<SmtSupplier> searchPage(Page page, SmtSupplier smtSupplier) {
		// TODO Auto-generated method stub
		List<Integer> parkIdList=null;
		if(ObjectUtil.isNotNull(SecurityUtils.getUser()))
		{

			parkIdList = SecurityUtils.getUser().getParkIdList();
		}
		IPage<SmtSupplier> result=this.baseMapper.searchPage(page,smtSupplier,parkIdList);
		return result;
	}



}
