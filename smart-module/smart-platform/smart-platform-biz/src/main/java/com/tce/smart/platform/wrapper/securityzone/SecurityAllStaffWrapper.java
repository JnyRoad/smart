package com.tce.smart.platform.wrapper.securityzone;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.securityzone.AllStaffListRespDTO;
import com.tce.smart.platform.core.dto.SecurityAllStaffListDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityPersonRelation;
import com.tce.smart.platform.service.securityzone.SmtSecurityPersonRelationService;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import com.tce.smart.tool.enums.SecuritySignStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.sql.Wrapper;

/**
 * @Description:
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author
 * @Date
 */
@Component
@AllArgsConstructor
public class SecurityAllStaffWrapper extends BaseWrapper<SecurityAllStaffListDTO, AllStaffListRespDTO> {

	@Autowired
	private SmtSecurityPersonRelationService smtSecurityPersonRelationService;

    @Override
    protected AllStaffListRespDTO warp(SecurityAllStaffListDTO bean) throws IOException {
		AllStaffListRespDTO resp = BeanUtils.transform(AllStaffListRespDTO.class, bean);
		Integer count = smtSecurityPersonRelationService.count(Wrappers.<SmtSecurityPersonRelation>query().lambda()
				.eq(SmtSecurityPersonRelation::getStaffId, bean.getStaffId()));
		resp.setSignStatus(count > 0 ? SecuritySignStatusEnum.SIGN.getCode() : SecuritySignStatusEnum.NON_SING.getCode());
		resp.setSignStatusDesc(SecuritySignStatusEnum.desc(resp.getSignStatus()));
        return resp;
    }
}
