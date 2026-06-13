package com.tce.smart.platform.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.entity.SmtSupplierStaff;
import com.tce.smart.platform.core.mapper.SmtSupplierStaffMapper;
import com.tce.smart.platform.core.vo.SearchSupplierStaffVO;
import com.tce.smart.platform.service.SmtSupplierStaffService;

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
public class SmtSupplierStaffServiceImpl extends ServiceImpl< SmtSupplierStaffMapper,  SmtSupplierStaff> implements  SmtSupplierStaffService {



	@Override
	public IPage<SearchSupplierStaffVO> searchPage(Page page, SmtSupplierStaff smtSupplierStaff) {
		// TODO Auto-generated method stub

		List<Integer> parkIdList=null;
		if(ObjectUtil.isNotNull(SecurityUtils.getUser()))
		{

			parkIdList = SecurityUtils.getUser().getParkIdList();
		}
		IPage<SearchSupplierStaffVO> result=this.baseMapper.searchPage(page,smtSupplierStaff,parkIdList);
		return result;
	}



}
