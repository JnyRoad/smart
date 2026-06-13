package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.api.dto.resp.ExternalDepC6Tree;
import com.tce.smart.platform.api.dto.resp.ExternalDepTree;
import com.tce.smart.platform.core.entity.SmtExDeptC6;
import com.tce.smart.platform.core.entity.SmtExternalDept;
import com.tce.smart.platform.core.mapper.SmtExDeptC6Mapper;
import com.tce.smart.platform.service.SmtExDeptC6Service;
import com.tce.smart.platform.service.SmtExternalDeptService;
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
public class SmtExDeptC6ServiceImpl extends ServiceImpl<SmtExDeptC6Mapper, SmtExDeptC6> implements SmtExDeptC6Service {


	@Override
	public SmtExDeptC6 getForDId(Long dId) {
		return this.getOne(Wrappers.<SmtExDeptC6>query().lambda().eq(SmtExDeptC6::getDId, dId),false);
	}


}
