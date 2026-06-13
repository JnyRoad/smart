package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.api.dto.req.StaffFamilyDormitoryReqDTO;
import com.tce.smart.platform.core.entity.SmtStaffFamilyDormitory;
import com.tce.smart.platform.core.mapper.SmtStaffFamilyDormitoryMapper;
import com.tce.smart.platform.service.SmtStaffFamilyDormitoryService;
import com.tce.smart.tool.enums.DeleteStatusEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @description: SmtStaffFamilyDormitoryServiceImpl
 * @date: 2020-12-08 17:27
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtStaffFamilyDormitoryServiceImpl extends ServiceImpl<SmtStaffFamilyDormitoryMapper, SmtStaffFamilyDormitory> implements SmtStaffFamilyDormitoryService {

	@Override
	public Boolean addFamily(StaffFamilyDormitoryReqDTO staffFamilyDormitoryReqDTO) {
		if(Objects.nonNull(staffFamilyDormitoryReqDTO.getId())) {
			//防止某字段被置空而无法更改
			return this.update(Wrappers.<SmtStaffFamilyDormitory>update().lambda()
					.set(SmtStaffFamilyDormitory::getBadge, staffFamilyDormitoryReqDTO.getBadge())
					.set(SmtStaffFamilyDormitory::getCertno, staffFamilyDormitoryReqDTO.getCertno())
					.set(SmtStaffFamilyDormitory::getName, staffFamilyDormitoryReqDTO.getName())
					.set(SmtStaffFamilyDormitory::getPhone, staffFamilyDormitoryReqDTO.getPhone())
					.set(SmtStaffFamilyDormitory::getRelation, staffFamilyDormitoryReqDTO.getRelation())
					.set(SmtStaffFamilyDormitory::getStaffBadge, staffFamilyDormitoryReqDTO.getStaffBadge())
					.eq(SmtStaffFamilyDormitory::getId, staffFamilyDormitoryReqDTO.getId()));
		}
		//查询家属是否已添加
		SmtStaffFamilyDormitory staffFamilyDormitory = this.getOne(new LambdaQueryWrapper<SmtStaffFamilyDormitory>()
				.eq(SmtStaffFamilyDormitory::getCertno, staffFamilyDormitoryReqDTO.getCertno())
				.eq(SmtStaffFamilyDormitory::getDelFlag,DeleteStatusEnum.NOT_DELETE.getCode())
		);
		if(null != staffFamilyDormitory){
			//家属已添加
			log.error("家属不能重复添加");
			throw new TCEException("家属身份证号已存在，请重新核实");
		}

		SmtStaffFamilyDormitory smtStaffFamilyDormitory = SmtStaffFamilyDormitory.builder()
				.name(staffFamilyDormitoryReqDTO.getName())
				.certno(staffFamilyDormitoryReqDTO.getCertno())
				.badge(staffFamilyDormitoryReqDTO.getBadge())
				.staffBadge(staffFamilyDormitoryReqDTO.getStaffBadge())
				.phone(staffFamilyDormitoryReqDTO.getPhone())
				.relation(staffFamilyDormitoryReqDTO.getRelation())
				.delFlag(DeleteStatusEnum.NOT_DELETE.getCode())
				.createTime(new Date())
				.build();

		return this.save(smtStaffFamilyDormitory);
	}



	@Override
	public Boolean delFamily(Long id) {
		//查询家属是否已添加
		SmtStaffFamilyDormitory staffFamilyDormitory = this.getById(id);
		if(null == staffFamilyDormitory){
			//家属不存在
			log.error("家属不存在:id={}",id);
			throw new TCEException("家属不存在");
		}

		return this.updateById(SmtStaffFamilyDormitory.builder()
				.id(id)
				.delFlag(DeleteStatusEnum.IS_DELETE.getCode())
				.build()
		);
	}

	@Override
	public List<StaffFamilyDormitoryReqDTO> queryFamily(String staffBadge) {
		List<SmtStaffFamilyDormitory> staffFamilyDormitories = this.list(new LambdaQueryWrapper<SmtStaffFamilyDormitory>()
				.eq(SmtStaffFamilyDormitory::getStaffBadge, staffBadge)
				.eq(SmtStaffFamilyDormitory::getDelFlag,DeleteStatusEnum.NOT_DELETE.getCode())
		);
		List<StaffFamilyDormitoryReqDTO> familyDormitoryReqDTOS = new ArrayList<>();
		for(SmtStaffFamilyDormitory familyDormitory : staffFamilyDormitories){
			familyDormitoryReqDTOS.add(StaffFamilyDormitoryReqDTO.builder()
					.id(familyDormitory.getId())
					.name(familyDormitory.getName())
					.certno(familyDormitory.getCertno())
					.badge(familyDormitory.getBadge())
					.phone(familyDormitory.getPhone())
					.relation(familyDormitory.getRelation())
					.staffBadge(familyDormitory.getStaffBadge())
					.build()
			);
		}
		return familyDormitoryReqDTOS;
	}
}
