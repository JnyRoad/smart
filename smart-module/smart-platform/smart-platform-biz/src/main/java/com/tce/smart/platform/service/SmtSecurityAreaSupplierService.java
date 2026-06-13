package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.SmtSecurityAreaSupplierReqDTO;
import com.tce.smart.platform.api.dto.req.securityarea.*;
import com.tce.smart.platform.api.dto.resp.SmtSecurityAreaSupplierPersonRespDTO;
import com.tce.smart.platform.api.dto.resp.SmtSecurityAreaSupplierRespDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaSupplierDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaSupplierDetailDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaSupplierListDTO;
import com.tce.smart.platform.core.dto.SmtSecurityAreaSupplierDTO;
import com.tce.smart.platform.core.entity.SmtSecurityAreaSupplier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @description: SmtSecurityAreaSupplierService
 * @date: 2020-07-21 9:18
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSecurityAreaSupplierService extends IService<SmtSecurityAreaSupplier> {

	/**
	 * 根据条件分页查询保密区供应商信息
	 * @param page
	 * @param smtSecurityAreaSupplierReqDTO
	 * @return
	 */
	IPage<SmtSecurityAreaSupplierDTO> getSecurityAreaSupplierPage(Page page, SmtSecurityAreaSupplierReqDTO smtSecurityAreaSupplierReqDTO);

	/**
	 * 查询所有可用的供应商列表
	 * @return
	 */
	List<SecurityAreaSupplierDTO> getSecurityAreaSupplierList(String supplierName,Integer parkId);

	/**
	 * 查询所有可用的供应商列表
	 * @return
	 */
	List<SecurityAreaSupplierListDTO> getSecurityAreaSupplier(String supplierName, Integer parkId);

	/**
	 * excel下载
	 * @param parkId
	 * @return
	 */
	ResponseEntity<byte[]> downLoadExcel(Integer parkId);

	/**
	 * 根据保密区供应商标识查询保密区人员列表
	 * @param spId
	 * @return
	 */
	List<SmtSecurityAreaSupplierPersonRespDTO> getSecurityAreaSupplierPersonList(Long spId);

	/**
	 * 保存保密区供应商数据
	 * 新记录添加
	 * 已存在的记录则修改
	 * @param supplierAddReqDTO
	 * @return
	 */
	boolean saveSecurityAreaSupplier(SecurityAreaSupplierAddReqDTO supplierAddReqDTO);

	/**
	 * 批量导入保密区供应商
	 * @param supplierUploadReqDTO
	 * @return
	 */
	boolean uploadSecurityAreaSupplier(SecurityAreaSupplierUploadReqDTO supplierUploadReqDTO);

	/**
	 * 删除保密区供应商数据
	 * @param id
	 * @return
	 */
	boolean delSecurityAreaSupplier(Long id);

	/**
	 * 查询保密区供应商详情
	 * @param id
	 * @return
	 */
	SecurityAreaSupplierDetailDTO getDetailById(Long id);

	/**
	 * 批量删除供应商
	 * @param ids
	 * @return
	 */
	boolean delBatchSupplier(List<Long> ids);

	/**
	 * 批量设置供应商授权项目
	 * @param authorReqDTO
	 * @return
	 */
	boolean batchSetAuthor(SecuritySupplierAddAuthorReqDTO authorReqDTO);

	/**
	 * 更新通知状态
	 * @param statusReqDTO
	 * @return
	 */
	boolean updateNotifyStatus(SupplierNotifyStatusReqDTO statusReqDTO);

	/**
	 * 待通知供应商列表
	 * @param notifyListDTO
	 * @return
	 */
	List<SmtSecurityAreaSupplierRespDTO> notifyList(SecurityAreaNotifyListDTO notifyListDTO);

	/**
	 * 通知配置
	 * @param configReqDTO
	 * @return
	 */
	boolean notifyConfig(SecurityAreaNotifyConfigDTO configReqDTO);

	/**
	 * 获取保密区通知配置
	 * @param parkId
	 * @return
	 */
	SecurityAreaNotifyConfigDTO getNotifyConfig(Integer parkId);

	/**
	 * 获取所有保密区通知配置
	 * @return
	 */
	List<SecurityAreaNotifyConfigDTO> getNotifyAllConfig();
}
