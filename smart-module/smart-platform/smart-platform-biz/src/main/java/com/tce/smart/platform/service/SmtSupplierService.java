package com.tce.smart.platform.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtSupplier;

/**
 * 园区供应商
 * @author QIPEI
 *
 */
public interface SmtSupplierService extends IService<SmtSupplier> {

	IPage<SmtSupplier> searchPage(Page page, SmtSupplier smtSupplier);

}
