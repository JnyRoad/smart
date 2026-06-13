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
import com.tce.smart.platform.core.entity.SmtSupplierPerson;
import com.tce.smart.platform.core.mapper.SmtSupplierPersonMapper;
import com.tce.smart.platform.service.SmtSupplierPersonService;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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

	@Override
	public IPage<List<SmtSupplierPersonDTO>> getSupplierPersonPage(Page page, SmtSupplierPersonDTO smtSupplierPersonDTO) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		return smtSupplierPersonMapper.getSupplierPersonPage(page,smtSupplierPersonDTO,parkIdList);
	}

	@Override
	public Boolean getVisitorSupplier(SmtVisitorSupplierFindDTO dto){
		SmtSupplierPerson list = this.getOne(Wrappers.<SmtSupplierPerson>query().lambda()
				.eq(SmtSupplierPerson::getIdCard, dto.getIdCard()).eq(SmtSupplierPerson::getSupplierId, Long.parseLong(dto.getSupplierId())));
		if(Objects.nonNull(list)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public boolean saveSupplierPerson(SmtSupplierPerson smtSupplierPerson) {
		int count = this.count(Wrappers.<SmtSupplierPerson>lambdaQuery()
				.eq(SmtSupplierPerson::getIdCard, smtSupplierPerson.getIdCard())
				.ne(smtSupplierPerson.getId() != null, SmtSupplierPerson::getId, smtSupplierPerson.getId()));
		if(count>0){
			throw new TCEException("身份证已存在，请重新输入");
		}
		if(smtSupplierPerson.getId() != null){
			smtSupplierPerson.setUpdateTime(new Date());
			//修改
			return this.updateById(smtSupplierPerson);
		}
		smtSupplierPerson.setCreateTime(new Date());
		//添加
		return this.save(smtSupplierPerson);
	}

    @Override
    public boolean saveUploadSupplierPerson(SmtSupplierPersonUploadDTO smtSupplierPersonUploadDTO) {

		List<SmtSupplierPerson> supplierPersonList = new ArrayList<>();

		for(SmtSupplierPersonUploadDTO.PersonDetail detail : smtSupplierPersonUploadDTO.getPersonDetails()){
			supplierPersonList.add(SmtSupplierPerson.builder()
					.supplierId(smtSupplierPersonUploadDTO.getSupplierId())
					.personName(detail.getName())
					.idCard(detail.getIdCard())
					.phone(detail.getPhone())
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

	@Override
	public boolean updateById(SecurityAreaPersonUpdateReqDTO personUpdateReqDTO) {
		SmtSupplierPerson smtSupplierPerson = new SmtSupplierPerson();
		BeanUtil.copyProperties(personUpdateReqDTO,smtSupplierPerson);

		return this.updateById(smtSupplierPerson);
	}
}
