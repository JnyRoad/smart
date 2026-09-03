package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SmtSecurityAreaSupplierDTO;
import com.tce.smart.platform.core.dto.securityarea.SecuritySupplierDTO;
import com.tce.smart.platform.core.entity.SmtSdTemplates;
import com.tce.smart.platform.core.entity.SmtSecurityAreaSupplier;
import com.tce.smart.platform.core.vo.SmtSDTemplateVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: SmtSecurityAreaSupplierMapper
 * @date: 2020-07-21 9:16
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSecurityAreaSupplierMapper extends BaseMapper<SmtSecurityAreaSupplier> {

	IPage<SmtSecurityAreaSupplierDTO> getSecurityAreaSupplierPage(Page page, @Param("query") SmtSecurityAreaSupplierDTO smtSecurityAreaSupplierDTO, @Param("park") List<Integer> parkIdList);

	List<SmtSecurityAreaSupplierDTO> getNotifyList(@Param("parkId")Integer parkId,@Param("days")Integer days);

	List<SecuritySupplierDTO> getSupplierList(@Param("compName") String compName,@Param("parkId")Integer parkId,@Param("parks") List<Integer> parkIdList);

	/**
	 * 锁定有效供应商行，使人员新增与供应商删除串行执行。
	 *
	 * @param id 供应商标识
	 * @return 已锁定的有效供应商；不存在或已删除时返回空
	 */
	SmtSecurityAreaSupplier selectActiveSupplierForUpdate(@Param("id") Long id);
}
