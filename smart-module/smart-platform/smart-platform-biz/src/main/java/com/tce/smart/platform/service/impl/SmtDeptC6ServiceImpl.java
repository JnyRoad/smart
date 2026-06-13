package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.api.dto.resp.ExternalDepC6Tree;
import com.tce.smart.platform.core.entity.SmtDeptC6;
import com.tce.smart.platform.core.entity.SmtDeptC6;
import com.tce.smart.platform.core.mapper.SmtDeptC6Mapper;
import com.tce.smart.platform.core.mapper.SmtDeptC6Mapper;
import com.tce.smart.platform.service.SmtDeptC6Service;
import com.tce.smart.platform.service.SmtDeptC6Service;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author fushiping
 * @date 2021-04-08 16:25:24
 */
@Service
public class SmtDeptC6ServiceImpl extends ServiceImpl<SmtDeptC6Mapper, SmtDeptC6> implements SmtDeptC6Service {


	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;

	public List<ExternalDepC6Tree> getC6List(Integer parkId) {
		List<SmtDeptC6> depts = this.list(Wrappers.<SmtDeptC6>query().lambda()
				.eq(Objects.nonNull(parkId), SmtDeptC6::getParkId, parkId)
				.eq(SmtDeptC6::getParentC6No, OneOrZeroEnum.ZERO.getCode()));
		List<ExternalDepC6Tree> listTree = new ArrayList<>();
		if (CollectionUtil.isNotEmpty(depts)) {
			depts.forEach(dept -> {
				ExternalDepC6Tree depTree = new ExternalDepC6Tree();
				depTree.setLabel(dept.getDeptName());
				depTree.setValue(dept.getC6DptNo());
				depTree.setChildren(getDepChild(dept.getC6DptNo()));
				listTree.add(depTree);
			});
		}
		return listTree;
	}

	@Override
	public List<ExternalDepC6Tree> getC6List() {
		return this.getC6List(xcParkId);
	}

	@Override
	public SmtDeptC6 getByC6No(String no) {
		return this.getOne(Wrappers.<SmtDeptC6>query().lambda().eq(StrUtil.isNotBlank(no), SmtDeptC6::getC6DptNo, no));
	}

	public List<ExternalDepC6Tree> getDepChild(String id) {
		List<SmtDeptC6> depts = this.list(Wrappers.<SmtDeptC6>query().lambda()
				.eq(Objects.nonNull(id), SmtDeptC6::getParentC6No, id));
		List<ExternalDepC6Tree> listTree = new ArrayList<>();
		if (CollectionUtil.isNotEmpty(depts)) {
			depts.forEach(dept -> {
				ExternalDepC6Tree depTree = new ExternalDepC6Tree();
				depTree.setLabel(dept.getDeptName());
				depTree.setValue(dept.getC6DptNo());
				depTree.setChildren(getDepChild(dept.getC6DptNo()));
				listTree.add(depTree);
			});
		}
		return listTree;
	}
}
