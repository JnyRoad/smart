package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.securityarea.SecurityAreaPersonUpdateReqDTO;
import com.tce.smart.platform.core.dto.SmtSupplierPersonDTO;
import com.tce.smart.platform.core.dto.SmtSupplierPersonUploadDTO;
import com.tce.smart.platform.core.dto.SmtVisitorSupplierFindDTO;
import com.tce.smart.platform.core.entity.SmtSecurityAreaSupplier;
import com.tce.smart.platform.core.entity.SmtSupplierPerson;
import com.tce.smart.platform.core.mapper.SmtSecurityAreaSupplierMapper;
import com.tce.smart.platform.core.mapper.SmtSupplierPersonMapper;
import com.tce.smart.platform.service.SmtSupplierPersonService;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @description: SmtSupplierPersonServiceImpl
 * @date: 2020-07-21 11:06
 * @author: wuling
 * @version: 1.0
 */
@Service
@AllArgsConstructor
public class SmtSupplierPersonServiceImpl extends ServiceImpl<SmtSupplierPersonMapper, SmtSupplierPerson> implements SmtSupplierPersonService {

	private final SmtSupplierPersonMapper smtSupplierPersonMapper;

	private final SmtSecurityAreaSupplierMapper smtSecurityAreaSupplierMapper;

	@Override
	public IPage<List<SmtSupplierPersonDTO>> getSupplierPersonPage(Page page, SmtSupplierPersonDTO smtSupplierPersonDTO) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		return smtSupplierPersonMapper.getSupplierPersonPage(page,smtSupplierPersonDTO,parkIdList);
	}

	/**
	 * 判断访客是否为有效供应商名下的有效人员。
	 */
	@Override
	public Boolean getVisitorSupplier(SmtVisitorSupplierFindDTO dto){
		Long supplierId = Long.parseLong(dto.getSupplierId());
		return smtSupplierPersonMapper.existsActiveSupplierPerson(supplierId, dto.getIdCard()) > 0;
	}

	/**
	 * 使用单条关联查询获取有效供应商名下的有效人员。
	 */
	@Override
	public List<SmtSupplierPerson> getActiveSupplierPersonList(Long supplierId) {
		return smtSupplierPersonMapper.getActiveSupplierPersonList(supplierId);
	}

	/**
	 * 保存人员时校验所属供应商，并禁止调用方写入逻辑删除状态。
	 */
	@Transactional
	@Override
	public boolean saveSupplierPerson(SmtSupplierPerson smtSupplierPerson) {
		int count = this.count(Wrappers.<SmtSupplierPerson>lambdaQuery()
				.eq(SmtSupplierPerson::getIdCard, smtSupplierPerson.getIdCard())
				.ne(smtSupplierPerson.getId() != null, SmtSupplierPerson::getId, smtSupplierPerson.getId()));
		if(count>0){
			throw new TCEException("身份证已存在，请重新输入");
		}
		if(smtSupplierPerson.getId() != null){
			SmtSupplierPerson existingPerson = this.getById(smtSupplierPerson.getId());
			if (Objects.isNull(existingPerson)) {
				throw new TCEException("供应商人员记录不存在");
			}
			ensureSupplierActive(smtSupplierPerson.getSupplierId() == null ? existingPerson.getSupplierId() : smtSupplierPerson.getSupplierId());
			smtSupplierPerson.setDelFlag(null);
			smtSupplierPerson.setUpdateTime(new Date());
			//修改
			return this.updateById(smtSupplierPerson);
		}
		ensureSupplierActive(smtSupplierPerson.getSupplierId());
		smtSupplierPerson.setDelFlag(0);
		smtSupplierPerson.setCreateTime(new Date());
		//添加
		return this.save(smtSupplierPerson);
	}

    /**
     * 批量导入人员前确认所属供应商有效，并固定新记录为有效状态。
	 */
    @Transactional
    @Override
    public boolean saveUploadSupplierPerson(SmtSupplierPersonUploadDTO smtSupplierPersonUploadDTO) {
		ensureSupplierActive(smtSupplierPersonUploadDTO.getSupplierId());

		List<SmtSupplierPerson> supplierPersonList = new ArrayList<>();

		for(SmtSupplierPersonUploadDTO.PersonDetail detail : smtSupplierPersonUploadDTO.getPersonDetails()){
			supplierPersonList.add(SmtSupplierPerson.builder()
					.supplierId(smtSupplierPersonUploadDTO.getSupplierId())
					.personName(detail.getName())
					.idCard(detail.getIdCard())
					.phone(detail.getPhone())
					.delFlag(0)
					.createTime(new Date())
					.build());
		}
		//批量插入
		if(supplierPersonList.size() > 0){
			return this.saveBatch(supplierPersonList);
		}
        return false;
    }

    @Override
	public boolean delSupplierPerson(Long id) {
		return this.removeById(id);
	}

	@Override
	public Boolean removeBatchById(List<Long> ids) {
		return this.removeByIds(ids);
	}

	/**
	 * 修改人员资料前确认该人员及其所属供应商仍有效。
	 */
	@Transactional
	@Override
	public boolean updateById(SecurityAreaPersonUpdateReqDTO personUpdateReqDTO) {
		SmtSupplierPerson existingPerson = this.getById(personUpdateReqDTO.getId());
		if (Objects.isNull(existingPerson)) {
			throw new TCEException("供应商人员记录不存在");
		}
		ensureSupplierActive(existingPerson.getSupplierId());
		SmtSupplierPerson smtSupplierPerson = new SmtSupplierPerson();
		BeanUtil.copyProperties(personUpdateReqDTO,smtSupplierPerson);

		return this.updateById(smtSupplierPerson);
	}

	/**
	 * 锁定并确保人员写入关联的供应商处于有效状态。
	 */
	private void ensureSupplierActive(Long supplierId) {
		if (supplierId == null || smtSecurityAreaSupplierMapper.selectActiveSupplierForUpdate(supplierId) == null) {
			throw new TCEException("供应商记录不存在或已删除");
		}
	}
}
