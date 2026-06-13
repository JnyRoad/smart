package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.securityarea.SecurityAreaPersonUpdateReqDTO;
import com.tce.smart.platform.core.dto.SmtSupplierPersonDTO;
import com.tce.smart.platform.core.dto.SmtSupplierPersonUploadDTO;
import com.tce.smart.platform.core.dto.SmtVisitorSupplierFindDTO;
import com.tce.smart.platform.core.entity.SmtSecurityAreaSupplier;
import com.tce.smart.platform.core.entity.SmtSupplierPerson;

import java.util.List;

/**
 * @description: SmtSupplierPersonService
 * @date: 2020-07-21 11:03
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSupplierPersonService extends IService<SmtSupplierPerson> {

	/**
	 * 根据条件分页查询保密区供应商人员信息
	 * @param page
	 * @param smtSecurityAreaSupplier
	 * @return
	 */
	IPage<List<SmtSupplierPersonDTO>> getSupplierPersonPage(Page page, SmtSupplierPersonDTO smtSupplierPersonDTO);


	/**
	 * 根据访客信息查询是否已存在于保密区供应商人员
	 * @param dto
	 * @return
	 */
	Boolean getVisitorSupplier(SmtVisitorSupplierFindDTO dto);

	/**
	 * 保存保密区供应商人员数据
	 * 新记录添加
	 * 已存在的记录则修改
	 * @param smtSupplierPerson
	 * @return
	 */
	boolean saveSupplierPerson(SmtSupplierPerson smtSupplierPerson);

	/**
	 * 保存批量上传的保密区供应商人员数据
	 * @param smtSupplierPerson
	 * @return
	 */
	boolean saveUploadSupplierPerson(SmtSupplierPersonUploadDTO smtSupplierPersonUploadDTO);

	/**
	 * 删除保密区供应商人员数据
	 * @param id
	 * @return
	 */
	boolean delSupplierPerson(Long id);

	/**
	 * 通过Id批量删除保密区供应商人员
	 * @param ids
	 * @return
	 */
	Boolean removeBatchById(List<Long> ids);

	/**
	 * 修改保密区信息
	 * @param personUpdateReqDTO
	 * @return
	 */
	boolean updateById(SecurityAreaPersonUpdateReqDTO personUpdateReqDTO);
}
