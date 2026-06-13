package com.tce.smart.platform.wrapper;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.ExternalDeptRespDTO;
import com.tce.smart.platform.core.entity.SmtExDeptC6;
import com.tce.smart.platform.core.entity.SmtExternalDept;
import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.service.SmtExDeptC6Service;
import com.tce.smart.platform.service.SmtOrganizeRelationService;
import com.tce.smart.platform.service.SmtStaffService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Objects;

/**
 *
 */
@Component
@AllArgsConstructor
public class ExternalDeptWrapper extends BaseWrapper<SmtExternalDept, ExternalDeptRespDTO> {

	private final SmtStaffService smtStaffService;

	private final SmtExDeptC6Service smtExDeptC6Service;

	private final SmtOrganizeRelationService smtOrganizeRelationService;

    @Override
    protected ExternalDeptRespDTO warp(SmtExternalDept smtExternalDept) throws IOException {
		ExternalDeptRespDTO respDTO = BeanUtils.transform(ExternalDeptRespDTO.class, smtExternalDept);
		if(StringUtils.isNotEmpty(smtExternalDept.getDirector())){
			SmtStaff smtStaff = smtStaffService.getById(smtExternalDept.getDirector());
			if(Objects.nonNull(smtStaff)){
				respDTO.setDirectorBadge(smtStaff.getBadge());
			}
		}
		SmtExDeptC6 c6 = smtExDeptC6Service.getOne(Wrappers.<SmtExDeptC6>query().lambda()
				.eq(SmtExDeptC6::getDId, smtExternalDept.getId()), false);
		if(Objects.nonNull(c6)) {
			respDTO.setC6DeptNo(c6.getC6DptNo());
		}
        return respDTO;
    }
}
