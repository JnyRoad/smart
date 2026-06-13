package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SmtSecurityAreaSupplierDTO;
import com.tce.smart.platform.core.dto.SmtSupplierPersonDTO;
import com.tce.smart.platform.core.dto.SmtVisitorSupplierFindDTO;
import com.tce.smart.platform.core.entity.SmtSupplierPerson;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: SmtSupplierPersonMapper
 * @date: 2020-07-21 10:41
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSupplierPersonMapper extends BaseMapper<SmtSupplierPerson> {

	IPage<List<SmtSupplierPersonDTO>> getSupplierPersonPage(Page page, @Param("query") SmtSupplierPersonDTO smtSupplierPersonDTO, @Param("park") List<Integer> parkIdList);


}
