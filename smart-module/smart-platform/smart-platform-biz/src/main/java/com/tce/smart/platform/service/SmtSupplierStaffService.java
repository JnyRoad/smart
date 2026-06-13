package com.tce.smart.platform.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtSupplierStaff;
import com.tce.smart.platform.core.vo.SearchSupplierStaffVO;

/**
 * 园区供应商
 * @author QIPEI
 *
 */
public interface SmtSupplierStaffService extends IService<SmtSupplierStaff> {

	IPage<SearchSupplierStaffVO> searchPage(Page page, SmtSupplierStaff smtSupplierStaff);

}
