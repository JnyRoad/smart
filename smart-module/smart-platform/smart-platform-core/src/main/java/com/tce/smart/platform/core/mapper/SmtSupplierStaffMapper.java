package com.tce.smart.platform.core.mapper;



import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtSupplierStaff;
import com.tce.smart.platform.core.vo.SearchSupplierStaffVO;


/**
 * 园区供应商
 *
 * @author QIPEI
 * @date 2020/02/11
 */
public interface SmtSupplierStaffMapper extends BaseMapper<SmtSupplierStaff> {

	IPage<SearchSupplierStaffVO> searchPage(Page page, @Param("query") SmtSupplierStaff smtSupplierStaff,@Param("park") List<Integer> parkIdList);



}
