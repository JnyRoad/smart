package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.ExternalDeptReqDTO;
import com.tce.smart.platform.api.dto.resp.ExternalDepTree;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtExternalDeptMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.enums.VisitorProcessEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 外部部门设置
 *
 * @author
 * @date 2019-04-15 11:34:58
 */
@Slf4j
@Service
public class SmtExternalDeptServiceImpl extends ServiceImpl<SmtExternalDeptMapper, SmtExternalDept> implements SmtExternalDeptService {
	@Autowired
	private SmtOrganizeRelationService smtOrganizeRelationService;
	@Autowired
	private SmtStaffMapper smtStaffMapper;
	@Autowired
	private SmtVisitorProcessRecordService smtVisitorProcessRecordService;
	@Autowired
	private SmtExDeptC6Service smtExDeptC6Service;

	@Override
	public Boolean editDept(ExternalDeptReqDTO externalDeptReqDTO) {
		SmtExternalDept dept = BeanUtils.transform(SmtExternalDept.class, externalDeptReqDTO);
		Integer userId = SecurityUtils.getUser().getId();
		SmtOrganizeRelation relation = smtOrganizeRelationService.getByUserId(userId);
		if(Objects.isNull(relation)) {
			throw new SmartException("您尚未与组织关联");
		}
		SmtExternalDept reDept = this.getByName(externalDeptReqDTO.getDeptName(), relation.getId());
		dept.setCompId(relation.getId());
		//修改信息
		if(Objects.nonNull(externalDeptReqDTO.getId())) {
			if(Objects.nonNull(reDept) && !reDept.getId().equals(externalDeptReqDTO.getId())) {
				throw new SmartException("该部门名已存在");
			}
			//修改smtStaff表中相应字段
			SmtStaff smtStaff = new SmtStaff();
			smtStaff.setDepName(dept.getDeptName());
			if(StringUtils.isNotEmpty(dept.getDirector())){
				String director = smtStaffMapper.selectById(dept.getDirector()).getBadge();
				smtStaff.setReportTo(director);
			}
			smtStaffMapper.update(smtStaff, Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getDepId,Long.toString(externalDeptReqDTO.getId())));

			if(this.updateById(dept)){
				if(StringUtils.isNotEmpty(dept.getDirector())){
					SmtStaff staff = smtStaffMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getId,dept.getDirector()));
					if(String.valueOf(staff.getCompId()).equals(dept.getCompId().toString())){
						smtStaffMapper.updateReportTo(staff.getId());
					}
					List<SmtVisitorProcessRecord> records = smtVisitorProcessRecordService.list(Wrappers.<SmtVisitorProcessRecord>query().lambda()
							.eq(SmtVisitorProcessRecord::getStatus, VisitorProcessEnum.WATING_2.getCode())
							.eq(SmtVisitorProcessRecord::getRecordNode, 2).eq(SmtVisitorProcessRecord::getStaffBadge, staff.getBadge()));
					for(SmtVisitorProcessRecord record: records){
						record.setStaffBadge(dept.getDirector());
						record.setStaffName(dept.getDirectorName());
						record.setStaffJche(staff.getJcheName());
						record.updateById();
					}
				}
				smtExDeptC6Service.remove(Wrappers.<SmtExDeptC6>query().lambda().eq(SmtExDeptC6::getDId, dept.getId()));
				if(StringUtils.isNotEmpty(externalDeptReqDTO.getC6DeptNo())) {
					smtExDeptC6Service.save(SmtExDeptC6.builder().c6DptNo(externalDeptReqDTO.getC6DeptNo()).dId(dept.getId()).build());
				}
			}
			return true;
		}else{
			//新增信息
			if(Objects.nonNull(reDept)) {
				throw new SmartException("该部门名已存在");
			}
			dept.setCreateTime(LocalDateTime.now());
			this.save(dept);
			//添加c6关联
			if(StringUtils.isNotEmpty(externalDeptReqDTO.getC6DeptNo())) {
				smtExDeptC6Service.save(SmtExDeptC6.builder().c6DptNo(externalDeptReqDTO.getC6DeptNo()).dId(dept.getId()).build());
			}
			return Boolean.TRUE;
		}
	}

	@Override
	public SmtExternalDept getByName(String deptName, Long compId) {
		return this.getOne(Wrappers.<SmtExternalDept>query().lambda()
				.eq(Objects.nonNull(deptName), SmtExternalDept::getDeptName, deptName).eq(SmtExternalDept::getCompId, compId));
	}

	@Override
	public List<SmtExternalDept> getList() {
		Integer userId = SecurityUtils.getUser().getId();
		SmtOrganizeRelation smtOrganizeRelation = smtOrganizeRelationService.getByUserId(userId);
		return this.list(Wrappers.<SmtExternalDept>query().lambda()
				.eq(Objects.nonNull(smtOrganizeRelation), SmtExternalDept::getCompId, smtOrganizeRelation.getId()));
	}

	@Override
	public List<SmtExternalDept> getList(Long compId) {
		Integer userId = SecurityUtils.getUser().getId();
		return this.list(Wrappers.<SmtExternalDept>query().lambda()
				.eq(SmtExternalDept::getCompId, compId));
	}

	@Override
	public Boolean deleteDept(Long id) {
		Integer count = smtStaffMapper.selectCount(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getDepId, id.toString()));
		if(count > 0) {
			throw  new SmartException("请先移除与该部门相关联的人员");
		}
		return this.removeById(id);
	}

	@Override
	public Boolean deleteDirector(Long id) {
		return this.baseMapper.deleteDirector(id);
	}

	@Override
	public List<ExternalDepTree> getCompTree(List<Integer> parkIds) {
		return this.getComp(parkIds);
	}

	private List<ExternalDepTree> getComp(List<Integer> parkIds) {
		Integer userId = SecurityUtils.getUser().getId();
		List<SmtOrganizeRelation> listBu = smtOrganizeRelationService.list(Wrappers.<SmtOrganizeRelation>query().lambda()
				.in(CollectionUtil.isNotEmpty(parkIds), SmtOrganizeRelation::getParkId, parkIds)
				.eq(SmtOrganizeRelation::getUserId, userId));
		List<ExternalDepTree> listTree = new ArrayList<>();
		if (CollUtil.isNotEmpty(listBu)) {
			listBu.forEach(bu -> {
				ExternalDepTree depTree = new ExternalDepTree();
				depTree.setLabel(bu.getCompName());
				depTree.setType(bu.getCompType());
				depTree.setValue(bu.getId());
				depTree.setChildren(getDepTree(bu.getId()));
				listTree.add(depTree);
			});
		}
		return listTree;
	}

	public List<ExternalDepTree> getDepTree(Long id) {
		List<SmtExternalDept> depts = this.list(Wrappers.<SmtExternalDept>query().lambda()
				.eq(Objects.nonNull(id), SmtExternalDept::getCompId, id).isNull(SmtExternalDept::getParentDept));
		List<ExternalDepTree> listTree = new ArrayList<>();
		if (CollectionUtil.isNotEmpty(depts)) {
			depts.forEach(dept -> {
				ExternalDepTree depTree = new ExternalDepTree();
				depTree.setLabel(dept.getDeptName());
				depTree.setValue(dept.getId());
				depTree.setChildren(getDepChild(dept.getId()));
				listTree.add(depTree);
			});
		}
		return listTree;
	}

	public List<ExternalDepTree> getDepChild(Long id) {
		List<SmtExternalDept> depts = this.list(Wrappers.<SmtExternalDept>query().lambda()
				.eq(Objects.nonNull(id), SmtExternalDept::getParentDept, id));
		List<ExternalDepTree> listTree = new ArrayList<>();
		if (CollectionUtil.isNotEmpty(depts)) {
			depts.forEach(dept -> {
				ExternalDepTree depTree = new ExternalDepTree();
				depTree.setLabel(dept.getDeptName());
				depTree.setValue(dept.getId());
				depTree.setChildren(getDepChild(dept.getId()));
				listTree.add(depTree);
			});
		}
		return listTree;
	}
}
