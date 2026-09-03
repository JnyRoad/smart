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

	/**
	 * 查询有效供应商名下的有效人员，避免两次读取之间发生供应商删除。
	 *
	 * @param supplierId 供应商标识
	 * @return 可展示的人员列表
	 */
	List<SmtSupplierPerson> getActiveSupplierPersonList(@Param("supplierId") Long supplierId);

	/**
	 * 判断身份证是否属于有效供应商名下的有效人员。
	 *
	 * @param supplierId 供应商标识
	 * @param idCard 身份证号
	 * @return 命中数量
	 */
	Integer existsActiveSupplierPerson(@Param("supplierId") Long supplierId, @Param("idCard") String idCard);
}
