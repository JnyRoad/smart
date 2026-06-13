package com.tce.smart.platform.wrapper.securityzone;


import cn.hutool.core.collection.CollUtil;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.securityzone.AllStaffListRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityStaffRespDTO;
import com.tce.smart.platform.core.dto.SecurityAllStaffListDTO;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityZone;
import com.tce.smart.platform.service.securityzone.SmtSecurityZoneService;
import com.tce.smart.tool.enums.SecuritySignStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @Description:
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author
 * @Date
 */
@Component
@AllArgsConstructor
public class SecurityPersonDetailWrapper extends BaseWrapper<SmtStaff, SecurityStaffRespDTO> {

	@Autowired
	private SmtSecurityZoneService smtSecurityZoneService;

    @Override
    protected SecurityStaffRespDTO warp(SmtStaff bean) throws IOException {
		SecurityStaffRespDTO resp = BeanUtils.transform(SecurityStaffRespDTO.class, bean);
		List<SmtSecurityZone> zone =  smtSecurityZoneService.getSecurityZoneByStaff(bean.getId());
		if(CollUtil.isNotEmpty(zone)) {
			List<SecurityStaffRespDTO.SecurityZone> list = BeanUtils.batchTransform(SecurityStaffRespDTO.SecurityZone.class, zone);
			resp.setSecurityZones(list);
		}
        return resp;
    }
}
